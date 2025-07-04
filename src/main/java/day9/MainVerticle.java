package day9;

import day9.handler.OrderHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        OrderHandler orderHandler = new OrderHandler();

        router.post("/orders").handler(orderHandler::placeOrder);
        router.put("/orders/:orderId/status").handler(orderHandler::updateOrderStatus);
        router.get("/orders/user/:userId").handler(orderHandler::getOrderHistory);
        router.get("/orders/aggregate").handler(orderHandler::aggregateSales);

        vertx.createHttpServer().requestHandler(router).listen(8888);
    }
}