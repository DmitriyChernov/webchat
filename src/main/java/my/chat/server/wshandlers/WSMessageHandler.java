package my.chat.server.wshandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import my.chat.exceptions.NoSuchMethodException;
import my.chat.server.WebserverVerticle;

import java.io.IOException;

public class WSMessageHandler implements Handler<Buffer> {
    private final Logger logger = LoggerFactory.getLogger(WSSendMessageHandler.class);
    private final WebserverVerticle wv;
    private final String chat;
    private final String user;
    private final String id;
    private final ServerWebSocket ws;

    public WSMessageHandler(WebserverVerticle wv, final ServerWebSocket ws, String chat, String user, String id) {
        this.wv = wv;
        this.ws = ws;
        this.chat = chat;
        this.user = user;
        this.id = id;
    }

    @Override
    public void handle(final Buffer data) {
        logger.info("send_message");
        ObjectMapper m = new ObjectMapper();
        try {
            JsonNode rootNode = m.readTree(data.toString());
            String method = rootNode.get("method").asText();

            logger.info(user + " calls method " + method);
            switch (method) {
                case "send_message":
                    sendMessage(rootNode, new WSSendMessageHandler(wv, ws, chat, user, id));
                    break;
                case "get_history":
                    wv.dataSource.getHistory(chat, new WSGetHistoryHandler(wv, chat, user, id));
                    break;
                case "close":
                    ws.close();
                    break;
                default:
                    throw new NoSuchMethodException(method);
            }
        } catch (IOException ex) {
            logger.error("Error when receiving ws data!");
            ex.printStackTrace();
            ws.reject();
        } catch (NoSuchMethodException ex) {
            logger.error("Error parsing ws data!");
            ex.printStackTrace();
            ws.reject();
        }
    }

    public void sendMessage(JsonNode node, Handler<JsonNode> done) {
        done.handle(node);
    }
}
