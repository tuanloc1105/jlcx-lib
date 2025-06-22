package vn.com.lcx.common.mail;

import java.util.List;

public class EmailInfo {

    private String id;
    private List<String> toUsers;
    private List<String> ccUsers;
    private List<String> bccUser;
    private String subject;
    private String body;
    private List<String> fileAttachments;

    public EmailInfo() {
    }

    public EmailInfo(String id,
                     List<String> toUsers,
                     List<String> ccUsers,
                     List<String> bccUser,
                     String subject,
                     String body,
                     List<String> fileAttachments) {
        this.id = id;
        this.toUsers = toUsers;
        this.ccUsers = ccUsers;
        this.bccUser = bccUser;
        this.subject = subject;
        this.body = body;
        this.fileAttachments = fileAttachments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getToUsers() {
        return toUsers;
    }

    public void setToUsers(List<String> toUsers) {
        this.toUsers = toUsers;
    }

    public List<String> getCcUsers() {
        return ccUsers;
    }

    public void setCcUsers(List<String> ccUsers) {
        this.ccUsers = ccUsers;
    }

    public List<String> getBccUser() {
        return bccUser;
    }

    public void setBccUser(List<String> bccUser) {
        this.bccUser = bccUser;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getFileAttachments() {
        return fileAttachments;
    }

    public void setFileAttachments(List<String> fileAttachments) {
        this.fileAttachments = fileAttachments;
    }

    public static class EmailInfoBuilder {
        private String id;
        private List<String> toUsers;
        private List<String> ccUsers;
        private List<String> bccUser;
        private String subject;
        private String body;
        private List<String> fileAttachments;

        public EmailInfoBuilder id(List<String> bccUser) {
            this.bccUser = bccUser;
            return this;
        }

        public EmailInfoBuilder toUsers(String id) {
            this.id = id;
            return this;
        }

        public EmailInfoBuilder ccUsers(List<String> toUsers) {
            this.toUsers = toUsers;
            return this;
        }

        public EmailInfoBuilder bccUser(List<String> ccUsers) {
            this.ccUsers = ccUsers;
            return this;
        }

        public EmailInfoBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailInfoBuilder body(String body) {
            this.body = body;
            return this;
        }

        public EmailInfoBuilder fileAttachments(List<String> fileAttachments) {
            this.fileAttachments = fileAttachments;
            return this;
        }

        public EmailInfo build() {
            return new EmailInfo(id, toUsers, ccUsers, bccUser, subject, body, fileAttachments);
        }
    }

}
