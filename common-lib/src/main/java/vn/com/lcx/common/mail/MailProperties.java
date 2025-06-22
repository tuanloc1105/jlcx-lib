package vn.com.lcx.common.mail;

import java.util.List;

public class MailProperties {
    private String host;
    private String port;
    private String username;
    private String password;
    private MailSendingMethod mailSendingMethod;
    private List<EmailInfo> emailInfos;

    public MailProperties() {
    }

    public MailProperties(String host,
                          String port,
                          String username,
                          String password,
                          MailSendingMethod mailSendingMethod,
                          List<EmailInfo> emailInfos) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.mailSendingMethod = mailSendingMethod;
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

    public MailSendingMethod getMailSendingMethod() {
        return mailSendingMethod;
    }

    public void setMailSendingMethod(MailSendingMethod mailSendingMethod) {
        this.mailSendingMethod = mailSendingMethod;
    }

    public List<EmailInfo> getEmailInfos() {
        return emailInfos;
    }

    public void setEmailInfos(List<EmailInfo> emailInfos) {
        this.emailInfos = emailInfos;
    }

    public static MailPropertiesBuilder builder() {
        return new MailPropertiesBuilder();
    }

    public static class MailPropertiesBuilder {
        private String host;
        private String port;
        private String username;
        private String password;
        private MailSendingMethod mailSendingMethod;
        private List<EmailInfo> emailInfos;

        public MailPropertiesBuilder host(String host) {
            this.host = host;
            return this;
        }

        public MailPropertiesBuilder port(String port) {
            this.port = port;
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

        public MailPropertiesBuilder mailSendingMethod(MailSendingMethod mailSendingMethod) {
            this.mailSendingMethod = mailSendingMethod;
            return this;
        }

        public MailPropertiesBuilder emailInfos(List<EmailInfo> emailInfos) {
            this.emailInfos = emailInfos;
            return this;
        }

        public MailProperties build() {
            return new MailProperties(host, port, username, password, mailSendingMethod, emailInfos);
        }

    }

}
