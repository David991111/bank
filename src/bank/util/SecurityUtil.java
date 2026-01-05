package bank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class SecurityUtil {
    private SecurityUtil() {}

    public static String hashPassword(String salt, String password) {
        return sha256(salt + ":" + password);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
