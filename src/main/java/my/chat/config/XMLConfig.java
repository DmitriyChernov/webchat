package my.chat.config;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import my.chat.server.wshandlers.WSGetHistoryHandler;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class XMLConfig {
    private final static Logger logger = LoggerFactory.getLogger(WSGetHistoryHandler.class);

    public static XMLConfiguration getConfig(File configFile) {
        XMLConfiguration config = null;
        try
        {
            config = new BasicConfigurationBuilder<>(XMLConfiguration.class)
                    .configure(new Parameters().xml().setExpressionEngine(new XPathExpressionEngine()))
                    .getConfiguration();
            FileHandler fh = new FileHandler(config);
            fh.load(new FileInputStream(configFile));

            return config;
        }
        catch (org.apache.commons.configuration2.ex.ConfigurationException ex)
        {
            logger.error("error in parsing config.xml");
            logger.error(ex.getStackTrace());
            System.exit(-1);
        } catch (IOException ex) {
            logger.error("error in file config.xml");
            logger.error(ex.getStackTrace());
            System.exit(-1);
        }

        logger.error("config initialized!");
        return config;
    }
}
