package my.chat.server.httphandlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.io.File;

public class HttpResourceHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        routingContext.response().sendFile("web/" + new File(routingContext.request().path()));
    }
}
