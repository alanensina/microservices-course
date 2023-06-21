package alanensina.orderservice.controllers;

import alanensina.orderservice.dtos.OrderRequest;
import alanensina.orderservice.services.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory-cb", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory-cb")
    @Retry(name = "inventory-cb")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest request) {
        return CompletableFuture.supplyAsync(() -> service.placeOrder(request));
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException exception){
        return CompletableFuture.supplyAsync(() -> "Oops! Sorry, something went wrong, please try again later!");
    }
}