package day9.repository;

import day9.model.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OrderRepository {
    private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

    public Order findById(String orderId) {
        return orders.get(orderId);
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public Order save(Order order) {
        orders.put(order.getOrderId(), order);
        return order;
    }

    public List<Order> findByUserId(String userId) {
        return orders.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}