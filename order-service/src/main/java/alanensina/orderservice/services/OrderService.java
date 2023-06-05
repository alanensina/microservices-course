package alanensina.orderservice.services;

import alanensina.orderservice.dtos.OrderLineItemsDTO;
import alanensina.orderservice.dtos.OrderRequest;
import alanensina.orderservice.models.Order;
import alanensina.orderservice.models.OrderLineItems;
import alanensina.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    public void placeOrder(OrderRequest request){
        var order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(
                request.getOrderLineItemsDTOList()
                        .stream()
                        .map(this::mapToDto)
                        .toList()
        );

        repository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDTO o) {
        var orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(o.getPrice());
        orderLineItems.setQuantity(o.getQuantity());
        orderLineItems.setSkuCode(o.getSkuCode());
        return orderLineItems;
    }
}
