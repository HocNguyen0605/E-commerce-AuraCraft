package util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static Properties prop = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("vnp.properties")) {
            if (input == null) {
                System.out.println("Xin lỗi, không tìm thấy file");
            } else {
                prop.load(input);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return prop.getProperty(key);
    }
}