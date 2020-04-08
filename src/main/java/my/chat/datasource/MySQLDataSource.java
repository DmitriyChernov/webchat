package my.chat.datasource;


import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import my.chat.config.Config;
import my.chat.server.wshandlers.WSGetHistoryHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MySQLDataSource implements IDataSource {
    private final static Logger logger = LoggerFactory.getLogger(WSGetHistoryHandler.class);

    private SQLClient client;

    public MySQLDataSource(Vertx vertx) {
        try {
            JsonObject mySQLClientConfig = new JsonObject()
                    .put("url", "jdbc:mysql:" + Config.getDBHost() + ":" + Config.getDBPort())
                    .put("max_pool_size", Config.getDBMaxPoolSize())
                    .put("username", Config.getDBUsername())
                    .put("password", Config.getDBPassword())
                    .put("database", Config.getDBName());
            client = MySQLClient.createShared(vertx, mySQLClientConfig);
        } catch (Exception ex) {
            logger.error("Error when connecting to db!");
            ex.printStackTrace();
        }
    }

    @Override
    public void putUser(String userName) {
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String statement = "INSERT IGNORE INTO chat.users (name) VALUES (?)";
                JsonArray params = new JsonArray().add(userName);
                updateWithParams(connection, statement, params);
            } else {
                logger.error("Error when putting user!");
                con.cause().printStackTrace();
                System.exit(-1);
            }
        });
    }

    @Override
    public void putChat(String chatName) {
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String statement = "INSERT IGNORE INTO chat.chats (name) VALUES (?)";
                JsonArray params = new JsonArray().add(chatName);
                updateWithParams(connection, statement, params);
            } else {
                logger.error("Failed to get connection to db!");
                logger.error(con.cause().getStackTrace());
                System.exit(-1);
            }
        });
    }

    @Override
    public void putMessage(String user, String chat, Date date, String message) {
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String statement = "INSERT INTO chat.users_chats(chat_id, sender_id, date, message)\n" +
                        "VALUES(\n" +
                        "    (SELECT id FROM chat.chats where name = ?),\n" +
                        "    (SELECT id FROM chat.users where name = ?),\n" +
                        "    STR_TO_DATE(?,'%Y-%m-%dT%H:%i:%s'),\n" +
                        "    ?\n)";
                JsonArray params = null;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss");
                    params = new JsonArray().add(chat).add(user).add(dateFormat.format(date)).add(message);
                } catch (Exception ex) {
                    logger.error("Error when formating date to json array object!");
                    logger.error(ex.getStackTrace());
                    System.exit(-1);
                }
                updateWithParams(connection, statement, params);
            } else {
                logger.error("Failed to get connection to db!");
                logger.error(con.cause().getStackTrace());
                System.exit(-1);
            }
        });
    }

    @Override
    public void getHistory(String chatName, Handler<List<JsonObject>> done) {
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String statement = "SELECT uc.message FROM chat.users_chats uc JOIN chat.chats c ON uc.chat_id = c.id WHERE c.name= ?";
                JsonArray params = new JsonArray().add(chatName);
                connection.queryWithParams(statement, params, res -> {
                    if (res.succeeded()) {
                        ResultSet rs = res.result();
                        try {
                            done.handle(rs.getRows());
                        } catch (Exception ex) {
                            logger.error("Error when handling history!");
                            logger.error(ex.getStackTrace());
                            System.exit(-1);
                        }
                    }
                });
            } else {
                logger.error("Failed to get connection to db!");
                logger.error(con.cause().getStackTrace());
                System.exit(-1);
            }
        });
    }

    private void updateWithParams(SQLConnection connection, String statement, JsonArray params) {
        connection.setAutoCommit(true, resAutoCommit -> {
            if (resAutoCommit.succeeded()) {
                connection.updateWithParams(statement, params, res -> {
                    if (res.succeeded()) {
                        UpdateResult rs = res.result();
                    } else {
                        logger.error("Updating failed!");
                        logger.error(res.cause().getCause());
                        logger.error(res.cause().getStackTrace());
                        System.exit(-1);
                    }
                });
            } else {
                logger.error("Setting autocommit failed!");
                System.exit(-1);
            }
        });
    }

}
