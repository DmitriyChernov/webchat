import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import my.chat.datasource.IDataSource;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DataSourceMock implements IDataSource {
    private final Logger logger = LoggerFactory.getLogger(DataSourceMock.class);

    @Override
    public void putUser(String userName) {
        return;
    }

    @Override
    public void putChat(String userName) {
        return;
    }

    @Override
    public void putMessage(String user, String chat, Date date, String message) {
        return;
    }

    @Override
    public void getHistory(String chatName, Handler<List<JsonObject>> done) {
        List<JsonObject> history = new LinkedList<>();
        history.add(new JsonObject().put("message", "{\"message\":\"test\",\"sender\":\"test\",\"received\":\"test\"}"));
        history.add(new JsonObject().put("message", "{\"message\":\"test\",\"sender\":\"test\",\"received\":\"test\"}"));
        done.handle(history);
    }
}
