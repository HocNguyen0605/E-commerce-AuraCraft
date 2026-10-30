package controller.vnp;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class HmacSHA512 {
        public HmacSHA512() {
        }

        public String encrypt(String keyStr, String data) {
            try {
                if (keyStr == null || data == null) return null;
                Mac mac = Mac.getInstance("HmacSHA512");
                SecretKey secretKey = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
                mac.init(secretKey);
                byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02x", b & 0xff));
                }
                return sb.toString();
            }catch (Exception e){
                throw new RuntimeException("Lỗi mã hóa HmacSHA512", e);
            }
        }
    }
