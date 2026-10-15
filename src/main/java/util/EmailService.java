package util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static Properties properties = new Properties();
    static {
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (input == null) {
                throw new RuntimeException("Rất tiếc, không tìm thấy file db.properties trên Classpath!");
            }

            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String MAIL_USER = properties.getProperty("mail.user");
    private static final String MAIL_APP_PASSWORD = properties.getProperty("mail.pass");
    public static void sendOtp(String to, String otp, int validSeconds) throws Exception {
        final String user = MAIL_USER;
        final String pass = MAIL_APP_PASSWORD;
        System.out.println("MAIL DEBUG user=[" + user + "] passLength="
                + (pass == null ? "null" : pass.replace(" ", "").length()));

        if (user == null || user.isBlank() || pass == null || pass.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình biến môi trường MAIL_USER / MAIL_APP_PASSWORD");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.replace(" ", ""));
            }
        });

        String body = "Xin chào,\n\n"
                + "Mã xác thực email AuraCraft của bạn là: " + otp + "\n\n"
                + "Mã có hiệu lực trong " + (validSeconds / 60) + " phút. "
                + "Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n"
                + "Nếu bạn không yêu cầu đăng ký, hãy bỏ qua email này.";

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(user, "AuraCraft", "UTF-8"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject("Mã xác thực đăng ký AuraCraft: " + otp, "UTF-8");
        msg.setText(body, "UTF-8");
        Transport.send(msg);
    }
}