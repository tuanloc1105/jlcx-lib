package vn.com.lcx.common.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.status.NopStatusListener;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

public final class LogbackConfig {

    public static void configure() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        // Disable startup status messages (same as <statusListener class="...NopStatusListener"/>)
        context.getStatusManager().add(new NopStatusListener()); // :contentReference[oaicite:1]{index=1}

        // === Properties (from XML) ===
        // <property name="APPLICATION_NAME" value="${APPLICATION_NAME}" scope="context"/>
        String appName = System.getProperty("APPLICATION_NAME",
                System.getenv("APPLICATION_NAME"));
        if (appName != null) {
            context.putProperty("APPLICATION_NAME", appName); // :contentReference[oaicite:2]{index=2}
        }

        // <property name="basePath" value="./data/log"/>
        context.putProperty("basePath", "./data/log"); // :contentReference[oaicite:3]{index=3}
        // <property name="fileName" value="app"/>
        context.putProperty("fileName", "app"); // :contentReference[oaicite:4]{index=4}

        // <property name="LOG_PATTERN" value="[...] %m%n\n"/>
        context.putProperty("LOG_PATTERN",
                "[%d{yyyy-MM-dd HH:mm:ss.SSS}] ${LOG_LEVEL_PATTERN:-%5p} ${PID:- } --- [%25t] [%-40.40logger{1}] [%X{trace_id}] [%X{operation_name}] %m%n\n"); // :contentReference[oaicite:5]{index=5}

        // === STDOUT appender (Console) ===
        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(context);
        consoleEncoder.setPattern("${LOG_PATTERN}"); // :contentReference[oaicite:6]{index=6}
        consoleEncoder.start();

        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> stdout = new ConsoleAppender<>();
        stdout.setContext(context);
        stdout.setName("STDOUT");
        stdout.setEncoder(consoleEncoder);
        stdout.start(); // :contentReference[oaicite:7]{index=7}

        // === FILE-AUDIT appender (RollingFileAppender) ===
        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(context);
        fileEncoder.setPattern("${LOG_PATTERN}"); // :contentReference[oaicite:8]{index=8}
        fileEncoder.start();

        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAudit = new RollingFileAppender<>();
        fileAudit.setContext(context);
        fileAudit.setName("FILE-AUDIT");
        fileAudit.setFile("${basePath}/${fileName}.log"); // :contentReference[oaicite:9]{index=9}
        fileAudit.setEncoder(fileEncoder);

        SizeAndTimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent> policy =
                new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(fileAudit);
        policy.setFileNamePattern("${basePath}/rolling_archived/${fileName}/%d{yyyy_MM_dd}/${fileName}.%d{yyyy-MM-dd}.%i.log"); // :contentReference[oaicite:10]{index=10}
        policy.setMaxFileSize(FileSize.valueOf("10MB")); // <maxFileSize>10MB</maxFileSize> // :contentReference[oaicite:11]{index=11}
        policy.setMaxHistory(60); // <maxHistory>60</maxHistory> // :contentReference[oaicite:12]{index=12}
        policy.setTotalSizeCap(FileSize.valueOf("20GB")); // <totalSizeCap>20GB</totalSizeCap> // :contentReference[oaicite:13]{index=13}
        policy.start();

        fileAudit.setRollingPolicy(policy);
        fileAudit.start();

        // === Root logger & specific loggers (Hibernate, p6spy) ===
        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO); // <root level="info"> // :contentReference[oaicite:14]{index=14}
        root.addAppender(stdout);
        root.addAppender(fileAudit); // <appender-ref ref="STDOUT"/>, <appender-ref ref="FILE-AUDIT"/> // :contentReference[oaicite:15]{index=15}

        // <logger name="org.hibernate.SQL" level="debug"/>
        context.getLogger("org.hibernate.SQL").setLevel(Level.DEBUG); // :contentReference[oaicite:16]{index=16}
        // <logger name="org.hibernate.orm.jdbc.bind" level="trace"/>
        context.getLogger("org.hibernate.orm.jdbc.bind").setLevel(Level.TRACE); // :contentReference[oaicite:17]{index=17}
        // <logger name="org.hibernate" level="info"/>
        context.getLogger("org.hibernate").setLevel(Level.INFO); // :contentReference[oaicite:18]{index=18}
        // <logger name="com.p6spy" level="debug"/>
        context.getLogger("com.p6spy").setLevel(Level.DEBUG); // :contentReference[oaicite:19]{index=19}
    }

}
