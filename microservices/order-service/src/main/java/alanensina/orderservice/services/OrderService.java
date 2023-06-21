package alanensina.orderservice.services;

import alanensina.orderservice.dtos.InventoryResponse;
import alanensina.orderservice.dtos.OrderLineItemsDTO;
import alanensina.orderservice.dtos.OrderRequest;
import alanensina.orderservice.models.Order;
import alanensina.orderservice.models.OrderLineItems;
import alanensina.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static alanensina.orderservice.constants.UrlConstants.GET_INVENTORY_BY_SKUCODE;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest request){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(
                request.getOrderLineItemsDTOList()
                        .stream()
                        .map(this::mapToDto)
                        .toList()
        );

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

        // Call to InventoryService to check if the item is available
        InventoryResponse[] responseArray = webClientBuilder.build().get()
                .uri(GET_INVENTORY_BY_SKUCODE, uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean response = Arrays.stream(responseArray).allMatch(InventoryResponse::isInStock);

        if(response){
            repository.save(order);
        }else{
            throw new IllegalArgumentException("Product is not in stock, try again later.");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDTO o) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(o.getPrice());
        orderLineItems.setQuantity(o.getQuantity());
        orderLineItems.setSkuCode(o.getSkuCode());
        return orderLineItems;
    }
}