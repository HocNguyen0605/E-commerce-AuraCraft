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

/** Gửi email qua Gmail SMTP. Cấu hình đọc từ src/main/resources/mail.properties (mail.user, mail.pass). */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (input == null) {
                throw new RuntimeException("Không tìm thấy file mail.properties trên Classpath!");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String MAIL_USER = clean(properties.getProperty("mail.user"));
    private static final String MAIL_APP_PASSWORD = clean(properties.getProperty("mail.pass"));

    /** File .properties không dùng dấu nháy; nếu lỡ viết thì bỏ đi để khỏi sai mật khẩu. */
    private static String clean(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1).trim();
        }
        return v;
    }

    private static String duration(int seconds) {
        return seconds % 60 == 0 ? (seconds / 60) + " phút" : seconds + " giây";
    }

    /** OTP xác thực email khi đăng ký. */
    public static void sendOtp(String to, String otp, int validSeconds) throws Exception {
        String body = "Xin chào,\n\n"
                + "Mã xác thực email AuraCraft của bạn là: " + otp + "\n\n"
                + "Mã có hiệu lực trong " + duration(validSeconds) + ". "
                + "Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n"
                + "Nếu bạn không yêu cầu đăng ký, hãy bỏ qua email này.";
        send(to, "Mã xác thực đăng ký AuraCraft: " + otp, body);
    }

    /** OTP đặt lại mật khẩu. */
    public static void sendResetOtp(String to, String otp, int validSeconds) throws Exception {
        String body = "Xin chào,\n\n"
                + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản AuraCraft của bạn.\n"
                + "Mã xác thực của bạn là: " + otp + "\n\n"
                + "Mã có hiệu lực trong " + duration(validSeconds) + ". "
                + "Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này, mật khẩu của bạn vẫn được giữ nguyên.";
        send(to, "Mã đặt lại mật khẩu AuraCraft: " + otp, body);
    }

    private static void send(String to, String subject, String body) throws Exception {
        final String user = MAIL_USER;
        final String pass = MAIL_APP_PASSWORD;
        if (user == null || user.isBlank() || pass == null || pass.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình mail.user / mail.pass trong mail.properties");
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

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(user, "AuraCraft", "UTF-8"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");
        Transport.send(msg);
    }
}