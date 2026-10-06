package util;

import org.mindrot.jbcrypt.BCrypt;

public class GenHash {
    public static void main(String[] args) {
        String hash = BCrypt.hashpw("123456", BCrypt.gensalt());
        System.out.println(hash);
        System.out.println(BCrypt.checkpw("123456", hash)); // phải in true
    }
}