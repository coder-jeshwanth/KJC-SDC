package day9.service;

import day9.model.Order;
import day9.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public class OrderService {
    private final OrderRepository repository = new OrderRepository();

    public Order placeOrder(String userId, List<String> productIds, Double totalAmount) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setProductIds(productIds);
        order.setStatus("PLACED");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);

        repository.save(order);
        return order;
    }

    public boolean updateOrderStatus(String orderId, String status) {
        Order order = repository.findById(orderId);
        if (order == null) {
            return false;
        }

        order.setStatus(status);
        repository.save(order);
        return true;
    }

    public List<Order> getOrderHistoryByUser(String userId) {
        return repository.findByUserId(userId);
    }

    public double aggregateTotalSales(String productId, LocalDateTime from, LocalDateTime to) {
        return repository.findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(from) && order.getOrderDate().isBefore(to))
                .filter(order -> order.getProductIds().contains(productId))
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }
}
