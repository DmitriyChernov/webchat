package my.chat;

import io.vertx.core.Vertx;
import my.chat.config.Config;
import my.chat.config.XMLConfig;
import my.chat.datasource.H2DataSource;
import my.chat.datasource.IDataSource;
import my.chat.datasource.MySQLDataSource;
import my.chat.server.WebserverVerticle;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        // Configuring
        File configFile =  new File("config.xml");
        Config config = new Config();
        config.setConfig(XMLConfig.getConfig(configFile));

        // Running vertx
        Vertx vertx = Vertx.vertx();
        IDataSource dataSource = new H2DataSource(vertx);
        //IDataSource dataSource = new MySQLDataSource(vertx);
        vertx.deployVerticle(new WebserverVerticle(dataSource));
    }
}
