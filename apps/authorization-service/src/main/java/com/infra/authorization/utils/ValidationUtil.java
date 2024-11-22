package com.infra.authorization.utils;

import java.util.regex.Pattern;
import java.util.UUID;
public class ValidationUtil {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        try {
            UUID uuid = UUID.fromString(str);
            return str.equals(uuid.toString()); // Ensures canonical representation
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return Pattern.matches(EMAIL_REGEX, email);
    }
}
