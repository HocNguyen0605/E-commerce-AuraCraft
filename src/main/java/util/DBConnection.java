package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        Properties properties = new Properties();
        // Đọc file db.properties từ classpath (thư mục resources hoặc src)
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("Rất tiếc, không tìm thấy file db.properties trên Classpath!");
            }

            // Tải dữ liệu từ file properties
            properties.load(input);

            url = properties.getProperty("db.url");
            user = properties.getProperty("db.user");
            password = properties.getProperty("db.password");
            String driver = properties.getProperty("db.driver");

            // Nạp JDBC Driver
            Class.forName(driver);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi nạp file cấu hình db.properties: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Lỗi không tìm thấy JDBC Driver: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}