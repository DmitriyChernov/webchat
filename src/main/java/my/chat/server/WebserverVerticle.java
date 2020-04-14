package my.chat.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;


import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import my.chat.datasource.IDataSource;
import my.chat.server.httphandlers.HttpResourceHandler;
import my.chat.server.httphandlers.HttpRootHandler;
import my.chat.server.wshandlers.WSOpenHandler;


public class WebserverVerticle extends AbstractVerticle {
	private final Logger logger = LoggerFactory.getLogger(WebserverVerticle.class);

	public EventBus eventBus;
	public IDataSource dataSource;

	public WebserverVerticle(IDataSource dataSource) {
		this.dataSource = dataSource;
		logger.info(dataSource);
	}

	@Override
	public void start() {
		//init
		vertx = getVertx();
		eventBus = vertx.eventBus();

		//http routing
		final Router httpRouteMatcher = Router.router(vertx);
		httpRouteMatcher.get("/").handler(new HttpRootHandler());
		httpRouteMatcher.getWithRegex(".*\\.(css|js)$").handler(new HttpResourceHandler());

		//http handling
		vertx.createHttpServer().requestHandler(httpRouteMatcher).listen(8089, "localhost");

		//ws handling
		vertx.createHttpServer().webSocketHandler(new WSOpenHandler(this)).listen(8090);
	}
}
