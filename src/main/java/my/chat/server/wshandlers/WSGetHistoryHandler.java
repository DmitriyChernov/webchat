package my.chat.server.wshandlers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import my.chat.server.WebserverVerticle;

import java.util.List;

public class WSGetHistoryHandler implements Handler<List<JsonObject>> {
    private final Logger logger = LoggerFactory.getLogger(WSGetHistoryHandler.class);
    private final WebserverVerticle wv;
    private final String chat;
    private final String user;
    private final String id;

    public WSGetHistoryHandler(WebserverVerticle wv, String chat, String user, String id) {
        this.wv = wv;
        this.chat = chat;
        this.user = user;
        this.id = id;
    }

    @Override
    public void handle(List<JsonObject> messages) {
        logger.info("get_history");
        for (JsonObject m : messages) {
            String msg = m.getString("MESSAGE") == null ? m.getString("message") : m.getString("MESSAGE");
            if (msg == null) {
                logger.error("something went wrong on parsing history message.");
                System.exit(-1);
            }
            try {
                logger.info("receiving message: " + msg + " sended for " + user + " to chat " + chat);
                wv.eventBus.send(id, msg);
            } catch(Exception ex) {
                logger.error("something went wrong on sending history message.");
                ex.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
