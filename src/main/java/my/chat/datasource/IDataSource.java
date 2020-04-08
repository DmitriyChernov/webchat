package my.chat.datasource;


import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.List;

public interface IDataSource {
    public void putUser(String userName);
    public void putChat(String userName);
    public void putMessage(String user, String chat, Date date, String message);
    public void getHistory(String chatName, Handler<List<JsonObject>> done);
}
