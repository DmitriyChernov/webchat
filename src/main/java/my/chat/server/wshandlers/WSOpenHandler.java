package my.chat.server.wshandlers;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import my.chat.server.WebserverVerticle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WSOpenHandler implements Handler<ServerWebSocket> {
    private final Logger logger = LoggerFactory.getLogger(WSOpenHandler.class);
    private final WebserverVerticle wv;

    public WSOpenHandler(WebserverVerticle wv) {
        this.wv = wv;
    }

    @Override
    public void handle(final ServerWebSocket ws) {
        logger.info("open");
        final Pattern chatUrlPattern = Pattern.compile("/chat/(\\w+)/(\\w+)");
        final Matcher m = chatUrlPattern.matcher(ws.path());

        if (!m.matches()) {
            ws.reject();
            return;
        }

        final String chat = m.group(1);
        final String user = m.group(2);
        final String id = ws.textHandlerID();

        logger.info("opening new connection with user: " + user + " for chat: " + chat);
        wv.getVertx().sharedData().getLocalMap("chats." + chat).putIfAbsent(id, "0");
        wv.dataSource.putChat(chat);
        wv.dataSource.putUser(user);

        ws.handler(new WSMessageHandler(wv, ws, chat, user, id));
        ws.closeHandler(new WSCloseHandler(wv, chat, user, id));
    }
}
