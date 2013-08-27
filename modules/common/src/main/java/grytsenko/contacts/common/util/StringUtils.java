package grytsenko.contacts.common.util;

/**
 * Utilities for strings.
 */
public final class StringUtils {

    /**
     * Constants for empty string.
     */
    public static final String EMPTY = "";

    /**
     * Determines that string is <code>null</code> or empty.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Extracts digits from the given string.
     */
    public static String digitsOnly(String str) {
        return str.replaceAll("\\D+", "");
    }

    private StringUtils() {
    }

}
