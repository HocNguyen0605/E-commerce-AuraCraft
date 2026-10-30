package controller.vnp;

import util.ConfigLoader;

public class VNPayConfig {
    public static String vnp_TmnCode = ConfigLoader.getProperty("vnp.tmn_code");
    public static String vnp_HashSecret = ConfigLoader.getProperty("vnp.hash_secret");
    public static String vnp_PayUrl = ConfigLoader.getProperty("vnp.pay_url");
    public static String vnp_ReturnUrl = ConfigLoader.getProperty("vnp.return_url");
}
