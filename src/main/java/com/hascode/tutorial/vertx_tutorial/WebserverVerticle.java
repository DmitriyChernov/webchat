package com.hascode.tutorial.vertx_tutorial;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


public class WebserverVerticle extends AbstractVerticle {
	Logger logger = LoggerFactory.getLogger(WebserverVerticle.class);

	@Override
	public void start() {
		vertx = getVertx();

		final Pattern chatUrlPattern = Pattern.compile("/chat/(\\w+)");
		final EventBus eventBus = vertx.eventBus();
		final IDataSource dataSource = new DataSource(vertx);

		Router httpRouteMatcher = router(vertx);
		httpRouteMatcher.get("/").handler(new Handler<RoutingContext>() {
			@Override
			public void handle(RoutingContext routingContext) {
				routingContext.response().sendFile("web/chat.html");
			}
		});
		httpRouteMatcher.getWithRegex(".*\\.(css|js)$").handler(new Handler<RoutingContext>() {
			@Override
			public void handle(RoutingContext routingContext) {
				routingContext.response().sendFile("web/" + new File(routingContext.request().path()));
			}
		});

		vertx.createHttpServer().requestHandler(httpRouteMatcher).listen(8089, "localhost");

		vertx.createHttpServer().webSocketHandler(new Handler<ServerWebSocket>() {
			@Override
			public void handle(final ServerWebSocket ws) {
				final Matcher m = chatUrlPattern.matcher(ws.path());
				if (!m.matches()) {
					ws.reject();
					return;
				}

				final String chatRoom = m.group(1);
				final String id = ws.textHandlerID();

				logger.info("registering new connection with id: " + id + " for chat-room: " + chatRoom);

				vertx.sharedData().getLocalMap("chats." + chatRoom).putIfAbsent(id, "0");
				dataSource.putChat(chatRoom);
				dataSource.putUser(id);

				ws.handler(new Handler<Buffer>() {
					@Override
					public void handle(final Buffer data) {
						ObjectMapper m = new ObjectMapper();
						try {
							JsonNode rootNode = m.readTree(data.toString());
							((ObjectNode) rootNode).put("received", new Date().toString());
							String jsonOutput = m.writeValueAsString(rootNode);
							logger.info("json generated: " + jsonOutput);

							for (Object chatter : vertx.sharedData().getLocalMap("chats." + chatRoom).keySet()) {
								logger.info("chatter: " + chatter);
								eventBus.send((String) chatter, jsonOutput);
							}
						} catch (IOException e) {
							ws.reject();
						}
					}
				});

				ws.closeHandler(new Handler<Void>() {
					@Override
					public void handle(final Void event) {
						logger.info("un-registering connection with id: " + id + " from chat-room: " + chatRoom);
						vertx.sharedData().getLocalMap("chats." + chatRoom).remove(id);
					}
				});



			}
		}).listen(8090);
	}

	public static Router router(Vertx vertx) {
		Router router= Router.router(vertx);
		return router;
	}

}
