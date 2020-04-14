import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import my.chat.config.Config;
import my.chat.config.XMLConfig;
import my.chat.datasource.H2DataSource;
import my.chat.datasource.IDataSource;
import my.chat.server.WebserverVerticle;
import my.chat.server.wshandlers.WSOpenHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
class ChatTest {
    private final Logger logger = LoggerFactory.getLogger(WSOpenHandler.class);

    @BeforeAll
    static void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        // Configuring
        File configFile =  new File("config.xml");
        Config config = new Config();
        config.setConfig(XMLConfig.getConfig(configFile));

        // Running vertx
        IDataSource dataSource = new DataSourceMock();
        vertx.deployVerticle(new WebserverVerticle(dataSource), testContext.completing());
    }

    @Test
    void http_server_was_started_and_sending_responces(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8089, "localhost", "/")
                .as(BodyCodec.string())
                .send(testContext.succeeding(response -> testContext.verify(() -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.body().length() > 0);
                    assertThat(response.body()).contains("chat");
                    testContext.completeNow();
                })));
    }

    @Test
    void user_can_enter_the_chat(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();
        String chatname = "chat";
        String username = "user";
        String url = "/chat/" + chatname + "/" + username;

        client.webSocket(8090, "localhost", url, res -> {
            if (res.succeeded()) {
                WebSocket ws = res.result();
                assertThat(!ws.isClosed());
                testContext.completeNow();
            } else {
                testContext.failNow(new Exception("Connection to websocket failed!"));
            }
        });
    }

    @Test
    void user_can_send_message(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();
        String chatname = "chat";
        String username = "user";
        String url = "/chat/" + chatname + "/" + username;

        client.webSocket(8090, "localhost", url, res -> {
            if (res.succeeded()) {
                WebSocket ws = res.result();
                ws.handler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer data) {
                        ObjectMapper m = new ObjectMapper();
                        try {
                            JsonNode rootNode = m.readTree(data.toString());
                            String jsonOutput = m.writeValueAsString(rootNode);
                            logger.info("received: " + jsonOutput);

                            assertThat(rootNode.get("message").asText()).isEqualTo("test");
                            assertThat(rootNode.get("sender").asText()).isEqualTo("test");
                            testContext.completeNow();
                        } catch (IOException ex) {
                            ws.close();
                            testContext.failNow(ex);
                        }
                    }
                });
                ws.writeTextMessage("{\"method\": \"send_message\", \"message\":\"test\",\"sender\":\"test\",\"received\":\"test\"}");
            } else {
                testContext.failNow(new Exception("Connection to websocket failed!"));
            }
        });
    }

    @Test
    void user_can_get_history(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();
        String chatname = "chat";
        String username = "user";
        String url = "/chat/" + chatname + "/" + username;

        client.webSocket(8090, "localhost", url, res -> {
            if (res.succeeded()) {
                CountDownLatch latch = new CountDownLatch(2);
                WebSocket ws = res.result();
                ws.handler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer data) {
                        ObjectMapper m = new ObjectMapper();
                        logger.info("buffer" + data);
                        try {
                            JsonNode rootNode = m.readTree(data.toString());
                            String jsonOutput = m.writeValueAsString(rootNode);
                            logger.info("rcvd: " + jsonOutput);

                            assertThat(rootNode.get("sender").asText()).isEqualTo("test");
                            latch.countDown();

                            if (latch.getCount()==0) {
                                testContext.completeNow();
                            }
                        } catch (IOException ex) {
                            ws.close();
                            testContext.failNow(ex);
                        }
                    }
                });

                ws.writeTextMessage("{\"method\": \"get_history\"}");
            } else {
                testContext.failNow(new Exception("Connection to websocket failed!"));
            }
        });
    }
}