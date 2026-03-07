package vn.io.lcx.common.mail;

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
import vn.io.lcx.common.constant.CommonConstant;
import vn.io.lcx.common.utils.ExceptionUtils;
import vn.io.lcx.common.utils.LogUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public final class MailHelper {

    private MailHelper() {
    }

    public static Map<String, String> sendHTMLEmail(final MailProperties mailProperties) {
        validateMailProperties(mailProperties);

        final var resultMap = new HashMap<String, String>();
        final var session = createSession(mailProperties);

        try (Transport transport = session.getTransport("smtp")) {
            transport.connect();
            final var mailInfos = mailProperties.getEmailInfos();

            for (final var mailInfo : mailInfos) {
                try {
                    final var message = createMimeMessage(session, mailProperties, mailInfo);

                    LogUtils.writeLog(
                            MailHelper.class,
                            LogUtils.Level.INFO,
                            buildLogMessage(mailProperties, mailInfo)
                    );

                    transport.sendMessage(message, message.getAllRecipients());
                    resultMap.put(mailInfo.getId(), "SUCCESS");

                    // Sleep to avoid rate limiting if not last email
                    if (mailInfos.indexOf(mailInfo) < mailInfos.size() - 1) {
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    handleMailError(resultMap, mailInfo, e);
                }
            }
        } catch (Exception e) {
            LogUtils.writeLog(MailHelper.class, e.getMessage(), e);
        }
        return resultMap;
    }

    private static void validateMailProperties(MailProperties mailProperties) {
        if (mailProperties == null ||
                StringUtils.isBlank(mailProperties.getHost()) ||
                StringUtils.isBlank(mailProperties.getPort()) ||
                StringUtils.isBlank(mailProperties.getUsername()) ||
                StringUtils.isBlank(mailProperties.getPassword()) ||
                CollectionUtils.isEmpty(mailProperties.getEmailInfos())) {
            throw new MailPropertiesEmptyError("Mail properties empty" + (mailProperties != null ? mailProperties.toString() : ""));
        }
    }

    private static Session createSession(MailProperties mailProperties) {
        final var properties = new Properties();
        properties.setProperty("mail.smtp.host", mailProperties.getHost());
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.port", mailProperties.getPort());
        properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        final String timeoutMs = "10000";
        properties.setProperty("mail.smtp.connectiontimeout", timeoutMs);
        properties.setProperty("mail.smtp.timeout", timeoutMs);
        properties.setProperty("mail.smtp.writetimeout", timeoutMs);

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProperties.getUsername(), mailProperties.getPassword());
            }
        });
    }

    private static MimeMessage createMimeMessage(Session session, MailProperties mailProperties, EmailInfo mailInfo) throws Exception {
        final var message = new MimeMessage(session);
        setFromAddress(message, mailProperties);
        setRecipients(message, mailInfo);

        message.setSentDate(new Date());
        message.setSubject(mailInfo.getSubject(), CommonConstant.UTF_8_STANDARD_CHARSET);

        message.setContent(createMultipartContent(mailInfo));
        message.saveChanges();
        return message;
    }

    private static void setFromAddress(MimeMessage message, MailProperties mailProperties) throws Exception {
        if (StringUtils.isNotBlank(mailProperties.getDisplayName())) {
            final var internetAddress = new InternetAddress(
                    StringUtils.isBlank(mailProperties.getFromAddress()) ? mailProperties.getUsername() : mailProperties.getFromAddress(),
                    mailProperties.getDisplayName(),
                    CommonConstant.UTF_8_STANDARD_CHARSET
            );
            message.setFrom(internetAddress);
        } else {
            message.setFrom(mailProperties.getUsername());
        }
    }

    private static void setRecipients(MimeMessage message, EmailInfo mailInfo) throws Exception {
        // TO
        if (CollectionUtils.isNotEmpty(mailInfo.getToUsers())) {
            message.setRecipients(Message.RecipientType.TO, toInternetAddresses(mailInfo.getToUsers()));
        }

        // CC
        if (CollectionUtils.isNotEmpty(mailInfo.getCcUsers())) {
            message.setRecipients(Message.RecipientType.CC, toInternetAddresses(mailInfo.getCcUsers()));
        }

        // BCC
        if (CollectionUtils.isNotEmpty(mailInfo.getBccUser())) {
            message.setRecipients(Message.RecipientType.BCC, toInternetAddresses(mailInfo.getBccUser()));
        }
    }

    private static InternetAddress[] toInternetAddresses(List<String> emails) throws Exception {
        InternetAddress[] addresses = new InternetAddress[emails.size()];
        for (int i = 0; i < emails.size(); i++) {
            addresses[i] = new InternetAddress(emails.get(i));
        }
        return addresses;
    }

    private static MimeMultipart createMultipartContent(EmailInfo mailInfo) throws Exception {
        final var multipart = new MimeMultipart("related");

        // HTML Body
        final var mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(mailInfo.getBody(), "text/html; charset=utf-8")));
        multipart.addBodyPart(mimeBodyPart);

        // Images from filesystem
        if (mapIsNotEmpty(mailInfo.getImagesMap())) {
            addImages(multipart, mailInfo.getImagesMap());
        }

        // Images from classpath resources
        if (mapIsNotEmpty(mailInfo.getResourceImagesMap())) {
            addResourceImages(multipart, mailInfo.getResourceImagesMap());
        }

        // File attachments
        if (CollectionUtils.isNotEmpty(mailInfo.getFileAttachments())) {
            addAttachments(multipart, mailInfo.getFileAttachments());
        }

        return multipart;
    }

    private static void addImages(MimeMultipart multipart, Map<String, String> imagesMap) {
        imagesMap.forEach((imageId, imagePath) -> {
            File file = new File(imagePath);
            if (!file.exists() || file.isDirectory()) {
                LogUtils.writeLog(MailHelper.class, LogUtils.Level.WARN, "File {} issue: exists={}, isDirectory={}", file.getAbsolutePath(), file.exists(), file.isDirectory());
                return;
            }

            try {
                final var bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                bodyPart.setHeader("Content-ID", "<" + imageId + ">");
                multipart.addBodyPart(bodyPart);
            } catch (Exception e) {
                LogUtils.writeLog(MailHelper.class, e.getMessage(), e);
            }
        });
    }

    private static void addResourceImages(MimeMultipart multipart, Map<String, String> imagesMap) {
        imagesMap.forEach((imageId, imagePath) -> {
            try (var imageStream = MailHelper.class.getClassLoader().getResourceAsStream(imagePath)) {
                if (imageStream == null) {
                    LogUtils.writeLog(MailHelper.class, LogUtils.Level.WARN, "Resource file {} not found", imagePath);
                    return;
                }

                final var imageBodyPart = new MimeBodyPart();
                try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = imageStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }

                    ByteArrayDataSource bds = new ByteArrayDataSource(output.toByteArray(), getContentTypeFromFileName(imagePath));
                    imageBodyPart.setDataHandler(new DataHandler(bds));
                    imageBodyPart.setHeader("Content-ID", "<" + imageId + ">");
                    multipart.addBodyPart(imageBodyPart);
                }
            } catch (Exception e) {
                LogUtils.writeLog(MailHelper.class, e.getMessage(), e);
            }
        });
    }

    private static void addAttachments(MimeMultipart multipart, List<String> filePaths) {
        for (String filePath : filePaths) {
            try {
                final var fileMimeBodyPart = new MimeBodyPart();
                fileMimeBodyPart.attachFile(new File(filePath));
                multipart.addBodyPart(fileMimeBodyPart);
            } catch (Exception e) {
                LogUtils.writeLog(MailHelper.class, LogUtils.Level.WARN, "Error attaching file {}: {}", filePath, e.getMessage());
            }
        }
    }

    private static void handleMailError(Map<String, String> resultMap, EmailInfo mailInfo, Throwable e) {
        final var stackTrace = ExceptionUtils.getStackTrace(e);
        resultMap.put(
                mailInfo.getId(),
                StringUtils.isBlank(stackTrace) ? "Error" :
                        stackTrace.length() > 3500 ? stackTrace.substring(0, 3500) : stackTrace
        );
    }

    private static String buildLogMessage(MailProperties mailProperties, EmailInfo mailInfo) {
        String from = StringUtils.isBlank(mailProperties.getFromAddress()) ? mailProperties.getUsername() : mailProperties.getFromAddress();
        return String.format(
                """
                        Start to send email with information:
                            - from email: %s
                            - to email: %s
                            - cc email: %s
                            - bcc email: %s
                            - subject: %s
                            - file(s): %s""",
                from,
                listToString(mailInfo.getToUsers()),
                listToString(mailInfo.getCcUsers()),
                listToString(mailInfo.getBccUser()),
                mailInfo.getSubject(),
                listToString(mailInfo.getFileAttachments())
        );
    }

    private static String listToString(List<String> list) {
        return CollectionUtils.isEmpty(list) ? "[]" : String.join(", ", list);
    }

    private static boolean mapIsNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
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
            LogUtils.writeLog(MailHelper.class, e.getMessage(), e);
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
