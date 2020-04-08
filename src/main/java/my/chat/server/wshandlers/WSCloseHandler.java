package my.chat.server.wshandlers;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import my.chat.server.WebserverVerticle;

public class WSCloseHandler implements Handler<Void> {
    private final Logger logger = LoggerFactory.getLogger(WSGetHistoryHandler.class);
    private final WebserverVerticle wv;
    private final String chat;
    private final String user;
    private final String id;

    public WSCloseHandler(WebserverVerticle wv, String chat, String user, String id) {
        this.wv = wv;
        this.chat = chat;
        this.user = user;
        this.id = id;
    }

    @Override
    public void handle(final Void event) {
        logger.info("close");
        logger.info("Closing connection with user: " + user + " from chat: " + chat);
        wv.getVertx().sharedData().getLocalMap("chats." + chat).remove(id);
    }
}
