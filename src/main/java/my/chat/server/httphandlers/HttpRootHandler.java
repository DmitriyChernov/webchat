package my.chat.server.httphandlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class HttpRootHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        routingContext.response().sendFile("web/chat.html");
    }
}
