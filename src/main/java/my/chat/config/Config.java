package my.chat.config;

import org.apache.commons.configuration2.XMLConfiguration;

public class Config {
    private static XMLConfiguration config;

    public void setConfig(XMLConfiguration config) {
        this.config = config;
    }

    public static String getServerHttpPort() {
        return config.getString("server/http-port");
    }

    public static String getServerWSPort() {
        return config.getString("server/ws-port");
    }

    public static String getDBHost() {
        return config.getString("db/host");
    }

    public static String getDBPort() {
        return config.getString("db/port");
    }

    public static String getDBMaxPoolSize() {
        return config.getString("db/max-pool-size");
    }

    public static String getDBName() {
        return config.getString("db/db");
    }

    public static String getDBUsername() {
        return config.getString("db/username");
    }

    public static String getDBPassword() {
        return config.getString("db/password");
    }
}
