package vn.com.lcx.common.mail;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.common.utils.LogUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MailHelper {

    private MailHelper() {
    }

    public static Map<String, String> sendHTMLEmail(final MailProperties mailProperties) {
        if (
                mailProperties == null ||
                        StringUtils.isBlank(mailProperties.getHost()) ||
                        StringUtils.isBlank(mailProperties.getPort()) ||
                        StringUtils.isBlank(mailProperties.getUsername()) ||
                        StringUtils.isBlank(mailProperties.getPassword()) ||
                        mailProperties.getMailSendingMethod() == null ||
                        CollectionUtils.isEmpty(mailProperties.getEmailInfos())
        ) {
            throw new MailPropertiesEmptyError("Mail properties empty" + (mailProperties != null ? mailProperties.toString() : ""));
        }
        final var resultMap = new HashMap<String, String>();
        final var properties = System.getProperties();
        switch (mailProperties.getMailSendingMethod()) {
            case LIVE:
                properties.setProperty("mail.smtp.host", mailProperties.getHost());
                properties.setProperty("mail.smtp.auth", "true");
                properties.setProperty("mail.smtp.starttls.enable", "true");
                properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.setProperty("mail.smtp.port", mailProperties.getPort());
                properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

                // properties.setProperty("mail.smtp.host", mailProperties.getHost());
                // properties.setProperty("mail.smtp.auth", "true");
                // properties.setProperty("mail.smtp.starttls.enable", "true");
                // properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                // properties.setProperty("mail.smtp.port", mailProperties.getPort());
                // properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
                break;
            case UAT:
            case LIVE_NO_TRUST:
            default:
                properties.setProperty("mail.smtp.host", mailProperties.getHost());
                properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
                properties.setProperty("mail.smtp.auth", "true");
                properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.setProperty("mail.smtp.port", mailProperties.getPort());

                // properties.setProperty("mail.smtp.auth", "true");
                // properties.setProperty("mail.smtp.starttls.enable", "true");
                // properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
                // properties.setProperty("mail.smtp.host", mailProperties.getHost());
                // properties.setProperty("mail.smtp.port", mailProperties.getPort());
                // properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                // properties.setProperty("mail.smtp.ssl.trust", "*"); // Trust any SSL certificate
                disableSslVerification();
                break;
        }
        final var session = Session.getInstance(
                properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailProperties.getUsername(), mailProperties.getPassword());
                    }
                }
        );
        try {
            Transport transport = session.getTransport("smtp");
            transport.connect();
            final var mailInfos = mailProperties.getEmailInfos();
            for (var mailInfo : mailInfos) {
                try {
                    final var message = new MimeMessage(session);
                    message.setFrom(mailProperties.getUsername());

                    final var toAddresses = new InternetAddress[mailInfo.getToUsers().size()];
                    List<String> toUsers = mailInfo.getToUsers();
                    for (int i = 0; i < toUsers.size(); i++) {
                        String toUser = toUsers.get(i);
                        toAddresses[i] = new InternetAddress(toUser);
                    }

                    message.setRecipients(Message.RecipientType.TO, toAddresses);
                    List<String> ccUsers = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(mailInfo.getCcUsers())) {
                        final var ccAddresses = new InternetAddress[mailInfo.getCcUsers().size()];
                        ccUsers.addAll(mailInfo.getCcUsers());
                        for (int i = 0; i < ccUsers.size(); i++) {
                            String ccUser = ccUsers.get(i);
                            ccAddresses[i] = new InternetAddress(ccUser);
                        }
                        message.setRecipients(Message.RecipientType.CC, ccAddresses);
                    }
                    List<String> bccUsers = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(mailInfo.getBccUser())) {
                        final var bccAddresses = new InternetAddress[mailInfo.getBccUser().size()];
                        bccUsers.addAll(mailInfo.getBccUser());
                        for (int i = 0; i < bccUsers.size(); i++) {
                            String bccUser = bccUsers.get(i);
                            bccAddresses[i] = new InternetAddress(bccUser);
                        }
                        message.setRecipients(Message.RecipientType.CC, bccAddresses);
                    }

                    message.setSentDate(new Date());

                    message.setSubject(mailInfo.getSubject(), CommonConstant.UTF_8_STANDARD_CHARSET);

                    final var multipart = new MimeMultipart("related");
                    final var mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.setDataHandler(
                            new DataHandler(
                                    new ByteArrayDataSource(
                                            mailInfo.getBody(),
                                            "text/html; charset=utf-8"
                                    )
                            )
                    );

                    multipart.addBodyPart(mimeBodyPart);

                    if (Optional.ofNullable(mailInfo.getImagesMap()).filter(it -> !it.isEmpty()).isPresent()) {
                        mailInfo.getImagesMap().forEach((imageId, imagePath) -> {
                            final var bodyPart = new MimeBodyPart();
                            final var file = new File(imagePath);
                            if (!file.exists()) {
                                LogUtils.writeLog(LogUtils.Level.WARN, "File {} does not exist", file.getAbsolutePath());
                                return;
                            }
                            if (file.isDirectory()) {
                                LogUtils.writeLog(LogUtils.Level.WARN, "File {} is a directory", file.getAbsolutePath());
                                return;
                            }
                            final var fds = new FileDataSource(file);
                            try {
                                bodyPart.setDataHandler(new DataHandler(fds));
                                bodyPart.setHeader("Content-ID", "<" + imageId + ">");
                                multipart.addBodyPart(bodyPart);
                            } catch (Exception e) {
                                LogUtils.writeLog(e.getMessage(), e);
                            }
                        });
                    }

                    if (Optional.ofNullable(mailInfo.getResourceImagesMap()).filter(it -> !it.isEmpty()).isPresent()) {
                        mailInfo.getResourceImagesMap().forEach((imageId, imagePath) -> {
                            try {
                                final var imageStream = MailHelper.class.getClassLoader().getResourceAsStream(imagePath);
                                if (imageStream == null) {
                                    LogUtils.writeLog(LogUtils.Level.WARN, "File {} not found", imagePath);
                                    return;
                                }
                                ByteArrayOutputStream output = new ByteArrayOutputStream();
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = imageStream.read(buffer)) != -1) {
                                    output.write(buffer, 0, bytesRead);
                                }
                                byte[] imageBytes = output.toByteArray();
                                imageStream.close();
                                output.close();
                                ByteArrayDataSource bds = new ByteArrayDataSource(imageBytes, getContentTypeFromFileName(imagePath));
                                final var imageBodyPart = new MimeBodyPart();
                                imageBodyPart.setDataHandler(new DataHandler(bds));
                                imageBodyPart.setHeader("Content-ID", "<" + imageId + ">");
                                multipart.addBodyPart(imageBodyPart);
                            } catch (Exception e) {
                                LogUtils.writeLog(e.getMessage(), e);
                            }
                        });
                    }

                    if (Optional.ofNullable(mailInfo.getFileAttachments()).filter(CollectionUtils::isNotEmpty).isPresent()) {
                        for (String filePath : mailInfo.getFileAttachments()) {
                            final var fileMimeBodyPart = new MimeBodyPart();
                            try {
                                fileMimeBodyPart.attachFile(new File(filePath));
                                multipart.addBodyPart(fileMimeBodyPart);
                            } catch (Exception e) {
                                LogUtils.writeLog(LogUtils.Level.WARN, e.getMessage());
                            }
                        }
                    }

                    message.setContent(multipart);
                    message.saveChanges();
                    LogUtils.writeLog(
                            LogUtils.Level.INFO,
                            String.format(
                                    "Start to send email with information:\n" +
                                            "    - from email: %s\n" +
                                            "    - to email: %s\n" +
                                            "    - cc email: %s\n" +
                                            "    - bcc email: %s\n" +
                                            "    - subject: %s\n" +
                                            "    - file(s): %s",
                                    mailProperties.getUsername(),
                                    String.join(", ", toUsers),
                                    String.join(", ", ccUsers),
                                    String.join(", ", bccUsers),
                                    mailInfo.getSubject(),
                                    mailInfo.getFileAttachments()
                                            .stream()
                                            .collect(Collectors.joining(", ", "[", "]"))
                            )
                    );
                    transport.sendMessage(message, message.getAllRecipients());
                    resultMap.put(mailInfo.getId(), "SUCCESS");
                } catch (Throwable e) {
                    final var stackTrace = ExceptionUtils.getStackTrace(e);
                    resultMap.put(
                            mailInfo.getId(),
                            StringUtils.isBlank(stackTrace) ?
                                    "Error" :
                                    stackTrace.length() > 3500 ?
                                            stackTrace.substring(0, 3500) : stackTrace
                    );
                }
            }
            transport.close();
        } catch (Throwable e) {
            // throw new MailSendingError(e);
            LogUtils.writeLog(e.getMessage(), e);
        }
        return resultMap;
    }

    private static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Set a default hostname verifier to trust any host
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            LogUtils.writeLog(e.getMessage(), e);
        }
    }

    private static String getContentTypeFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "application/octet-stream";
        }
        String lowerCaseFileName = fileName.toLowerCase();
        if (lowerCaseFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerCaseFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerCaseFileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerCaseFileName.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

}
