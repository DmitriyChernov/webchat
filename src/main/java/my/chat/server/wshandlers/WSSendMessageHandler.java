package my.chat.server.wshandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import my.chat.server.WebserverVerticle;

import java.io.IOException;
import java.util.Date;

public class WSSendMessageHandler implements Handler<JsonNode> {
    private final Logger logger = LoggerFactory.getLogger(WSSendMessageHandler.class);
    private final WebserverVerticle wv;
    private final String chat;
    private final String user;
    private final String id;
    private final ServerWebSocket ws;

    public WSSendMessageHandler(WebserverVerticle wv, final ServerWebSocket ws, String chat, String user, String id) {
        this.wv = wv;
        this.ws = ws;
        this.chat = chat;
        this.user = user;
        this.id = id;
    }

    public void handle(final JsonNode data) {
        logger.info("send_message");
        ObjectMapper m = new ObjectMapper();
        try {
            Date sendDate = new Date(System.currentTimeMillis());
            ((ObjectNode) data).put("received", sendDate.toString());
            String jsonOutput = m.writeValueAsString(data);

            // Inserting message to DS
            wv.dataSource.putMessage(user, chat, sendDate, jsonOutput);
            logger.info("receiving message: "+ jsonOutput + " sended from " + user + " to chat " + chat);

            // Sending message to all users in chat
            for (Object chatter : wv.getVertx().sharedData().getLocalMap("chats." + chat).keySet()) {
                wv.eventBus.send((String) chatter, jsonOutput);
            }
        } catch (IOException ex) {
            logger.error("Error when receiving ws data!");
            logger.error(ex.getStackTrace());
            ws.reject();
        }
    }
}
