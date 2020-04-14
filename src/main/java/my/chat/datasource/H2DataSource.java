package my.chat.datasource;


import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import my.chat.config.Config;
import my.chat.server.wshandlers.WSGetHistoryHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class H2DataSource implements IDataSource {
    private final static Logger logger = LoggerFactory.getLogger(WSGetHistoryHandler.class);

    private SQLClient client;

    public H2DataSource(Vertx vertx) {
        try {
            JsonObject mySQLClientConfig = new JsonObject()
                    .put("url", "jdbc:h2:mem:" + Config.getDBSchema() + ";INIT=CREATE SCHEMA IF NOT EXISTS CHAT")
                    .put("max_pool_size", 30)
                    .put("username", Config.getDBUsername())
                    .put("password", Config.getDBPassword());
            client = JDBCClient.createShared(vertx, mySQLClientConfig);
            initDatabase();
        } catch (Exception ex) {
            logger.error("Error when connecting to db!");
            ex.printStackTrace();
        }
    }

    private void initDatabase() {
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String sqlChats = "CREATE TABLE chat.chats (\n" +
                        "  id int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  name varchar(50) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (id)\n" +
                        ");\n" +
                        "\n" +
                        "ALTER TABLE chat.chats\n" +
                        "ADD UNIQUE INDEX chat_name (name);";
                String sqlUsers = "CREATE TABLE chat.users (\n" +
                        "  id int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  name varchar(50) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (id)\n" +
                        ");\n" +
                        "\n" +
                        "ALTER TABLE chat.users\n" +
                        "ADD UNIQUE INDEX user_name (name);";
                String sqlUsersChats = "CREATE TABLE chat.users_chats (\n" +
                        "  id int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  chat_id int(11) DEFAULT NULL,\n" +
                        "  sender_id int(11) DEFAULT NULL,\n" +
                        "  date date DEFAULT NULL,\n" +
                        "  message varchar(255) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (id)\n" +
                        ");\n" +
                        "\n" +
                        "ALTER TABLE chat.users_chats\n" +
                        "ADD CONSTRAINT FK_users_chats_chat_id FOREIGN KEY (chat_id)\n" +
                        "REFERENCES chat.chats (id);";
                connection.execute(sqlUsers, execute -> {
                    if (execute.succeeded()) {
                        logger.info("Table user created !");
                    } else {
                        logger.error("Error when creating table user!");
                        con.cause().printStackTrace();
                        System.exit(-1);
                    }
                });
                connection.execute(sqlChats, execute -> {
                    if (execute.succeeded()) {
                        logger.info("Table chat created !");
                    } else {
                        logger.error("Error when creating table chats!");
                        con.cause().printStackTrace();
                        System.exit(-1);
                    }
                });
                connection.execute(sqlUsersChats, execute -> {
                    if (execute.succeeded()) {
                        logger.info("Table users chats created !");
                    } else {
                        logger.error("Error when creating table users chats!");
                        con.cause().printStackTrace();
                        System.exit(-1);
                    }
                });
            } else {
                logger.error("Error when putting user!");
                con.cause().printStackTrace();
                System.exit(-1);
            }
        });
    }

    @Override
    public void putUser(String userName) {
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String statement = "MERGE INTO CHAT.USERS(name) KEY (name) VALUES (?)";
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
                String statement = "MERGE INTO CHAT.CHATS(name) KEY (name) VALUES (?)";
                JsonArray params = new JsonArray().add(chatName);
                updateWithParams(connection, statement, params);
            } else {
                logger.error("Failed to get connection to db!");
                con.cause().printStackTrace();
                System.exit(-1);
            }
        });
    }

    @Override
    public void putMessage(String user, String chat, Date date, String message) {
        logger.info("INSERTING MESSAGE" + message);
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String statement = "INSERT INTO chat.users_chats(chat_id, sender_id, date, message)\n" +
                        "VALUES(\n" +
                        "    (SELECT id FROM chat.chats where name = ?),\n" +
                        "    (SELECT id FROM chat.users where name = ?),\n" +
                        "    PARSEDATETIME(?,'yyyy-MM-dd HH:mm:ss', 'en'),\n" +
                        "    ?\n)";
                JsonArray params = null;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
                    params = new JsonArray().add(chat).add(user).add(dateFormat.format(date)).add(message);
                } catch (Exception ex) {
                    logger.error("Error when formating date to json array object!");
                    ex.printStackTrace();
                    System.exit(-1);
                }
                updateWithParams(connection, statement, params);
            } else {
                logger.error("Failed to get connection to db!");
                con.cause().printStackTrace();
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
                con.cause().printStackTrace();
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
                        res.cause().printStackTrace();
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
