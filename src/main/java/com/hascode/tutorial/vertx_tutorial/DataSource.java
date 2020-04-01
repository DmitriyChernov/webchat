package com.hascode.tutorial.vertx_tutorial;


import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;


public class DataSource implements IDataSource {
    private final SQLClient client;

    public DataSource(Vertx vertx) {
        JsonObject mySQLClientConfig = new JsonObject().put("localhost:53400", "chat");
        client = MySQLClient.createShared(vertx, mySQLClientConfig);
    }

    @Override
    public void putUser(String userName) {
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String statement = "INSERT IGNORE INTO USERS VALUES (?)";
                JsonArray params = new JsonArray().add(userName);
                connection.updateWithParams(statement, params, res -> {
                    if (res.succeeded()) {
                        UpdateResult rs = res.result();
                        System.out.println("Updated no. of rows: " + rs.getUpdated());
                        System.out.println("Generated keys: " + rs.getKeys());
                    }
                });
            } else {
                System.err.println("Exception caught:");
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
                String statement = "INSERT IGNORE INTO CHATS VALUES (?)";
                JsonArray params = new JsonArray().add(chatName);
                connection.updateWithParams(statement, params, res -> {
                    if (res.succeeded()) {
                        UpdateResult rs = res.result();
                        System.out.println("Updated no. of rows: " + rs.getUpdated());
                        System.out.println("Generated keys: " + rs.getKeys());
                    }
                });
            } else {
                System.err.println("Exception caught:");
                con.cause().printStackTrace();
                System.exit(-1);
            }
        });
    }

    @Override
    public void getHistory(String chatName) {
        client.getConnection(con -> {
            if (con.succeeded()) {
                SQLConnection connection = con.result();
                String statement = "SELECT u.name AS user, c.name AS chat, uc.date, uc.message FROM chat.users_chats uc JOIN chat.chats c ON uc.chat_id = c.id JOIN chat.users u ON uc.user_id = u.id WHERE c.name= ?";
                JsonArray params = new JsonArray().add(chatName);
                connection.queryWithParams(statement, params, res -> {
                    if (res.succeeded()) {
                        ResultSet rs = res.result();
                    }
                });
            } else {
                System.err.println("Exception caught:");
                con.cause().printStackTrace();
                System.exit(-1);
            }
        });
    }

}
