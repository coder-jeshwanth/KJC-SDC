package day9.handler;

import day9.model.Order;
import day9.repository.OrderRepository;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OrderHandler {
    private final OrderRepository orderRepository = new OrderRepository();

    public void placeOrder(RoutingContext context) {
        Order order = Json.decodeValue(context.getBody(), Order.class);
        order.setOrderId(UUID.randomUUID().toString());
        Order savedOrder = orderRepository.save(order);
        context.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(201)
                .end(Json.encodePrettily(savedOrder));
    }

    public void updateOrderStatus(RoutingContext context) {
        String orderId = context.pathParam("orderId");
        String status = context.getBodyAsJson().getString("status");
        Order updatedOrder = orderRepository.updateStatus(orderId, status);
        if (updatedOrder != null) {
            context.response()
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(updatedOrder));
        } else {
            context.response().setStatusCode(404).end();
        }
    }

    public void getOrderHistory(RoutingContext context) {
        String userId = context.pathParam("userId");
        context.response()
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(orderRepository.findByUserId(userId)));
    }

    public void aggregateSales(RoutingContext context) {
        String productId = context.queryParam("productId").get(0);
        LocalDateTime startDate = LocalDateTime.parse(context.queryParam("startDate").get(0));
        LocalDateTime endDate = LocalDateTime.parse(context.queryParam("endDate").get(0));

        double total = orderRepository.aggregateSales(productId, startDate, endDate);
        context.response()
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(new AggregateResponse(total)));
    }

    private static class AggregateResponse {
        private final double total;
        public AggregateResponse(double total) { this.total = total; }
        public double getTotal() { return total; }
    }
}