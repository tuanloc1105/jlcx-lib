package vn.com.lcx.common.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class LogbackConfig {

    public static void configure() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);

        try {
            configurator.doConfigure(
                    Objects.requireNonNull(LogbackConfig.class.getClassLoader().getResource("default-logback.xml"))
            );
        } catch (JoranException e) {
            throw new RuntimeException("Failed to load logback config", e);
        }
    }

}
