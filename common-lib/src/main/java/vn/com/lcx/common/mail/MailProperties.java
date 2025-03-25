package vn.com.lcx.common.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MailProperties {
    private String host;
    private String port;
    private String username;
    private String password;
    private MailSendingMethod mailSendingMethod;
    private List<EmailInfo> emailInfos;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class EmailInfo {
        private String id;
        private List<String> toUsers;
        private List<String> ccUsers;
        private List<String> bccUser;
        private String subject;
        private String body;
        private List<String> fileAttachments;
    }
}
