package vn.com.lcx.common.mail;

import java.util.List;

public class MailProperties {
    private String host;
    private String port;
    private String displayName;
    private String fromAddress;
    private String username;
    private String password;
    private List<EmailInfo> emailInfos;

    public static MailPropertiesBuilder builder() {
        return new MailPropertiesBuilder();
    }

    public MailProperties() {
    }

    public MailProperties(String host,
                          String port,
                          String displayName,
                          String fromAddress,
                          String username,
                          String password,
                          List<EmailInfo> emailInfos) {
        this.host = host;
        this.port = port;
        this.displayName = displayName;
        this.fromAddress = fromAddress;
        this.username = username;
        this.password = password;
        this.emailInfos = emailInfos;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<EmailInfo> getEmailInfos() {
        return emailInfos;
    }

    public void setEmailInfos(List<EmailInfo> emailInfos) {
        this.emailInfos = emailInfos;
    }

    public static class MailPropertiesBuilder {
        private String host;
        private String port;
        private String displayName;
        private String fromAddress;
        private String username;
        private String password;
        private List<EmailInfo> emailInfos;

        public MailPropertiesBuilder host(String host) {
            this.host = host;
            return this;
        }

        public MailPropertiesBuilder port(String port) {
            this.port = port;
            return this;
        }

        public MailPropertiesBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public MailPropertiesBuilder fromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }

        public MailPropertiesBuilder username(String username) {
            this.username = username;
            return this;
        }

        public MailPropertiesBuilder password(String password) {
            this.password = password;
            return this;
        }

        public MailPropertiesBuilder emailInfos(List<EmailInfo> emailInfos) {
            this.emailInfos = emailInfos;
            return this;
        }

        public MailProperties build() {
            return new MailProperties(host, port, displayName, fromAddress, username, password, emailInfos);
        }

    }

}
