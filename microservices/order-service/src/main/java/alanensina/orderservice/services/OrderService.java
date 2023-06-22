package alanensina.orderservice.services;

import alanensina.orderservice.dtos.InventoryResponse;
import alanensina.orderservice.dtos.OrderLineItemsDTO;
import alanensina.orderservice.dtos.OrderRequest;
import alanensina.orderservice.events.OrderPlacedEvent;
import alanensina.orderservice.models.Order;
import alanensina.orderservice.models.OrderLineItems;
import alanensina.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
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
@Slf4j
public class OrderService {

    private final OrderRepository repository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest request){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(
                request.getOrderLineItemsDTOList()
                        .stream()
                        .map(this::mapToDto)
                        .toList()
        );

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();
        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

        // Call to InventoryService to check if the item is available
        log.info("Calling inventory service...");

        // Tracing the connection between order-service and inventory-service
        try(Tracer.SpanInScope spanInScope= tracer.withSpan(inventoryServiceLookup.start())){
            InventoryResponse[] responseArray = webClientBuilder.build().get()
                    .uri(GET_INVENTORY_BY_SKUCODE, uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean response = Arrays.stream(responseArray).allMatch(InventoryResponse::isInStock);

            if(response){
                repository.save(order);
                log.info("Order placed! ");
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
                return "Order placed successfully!";
            }else{
                log.error("Product is not in stock!");
                throw new IllegalArgumentException("Product is not in stock, try again later.");
            }
        } finally {
            inventoryServiceLookup.end();
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