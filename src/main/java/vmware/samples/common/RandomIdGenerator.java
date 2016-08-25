package vmware.samples.common;

import java.util.Random;

public class RandomIdGenerator {

    private static final String LATIN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnoprstuvwxyz";
    private static final String DIGITS = "0123456789";

    /**
     * Returns a string after appending random characters to the provided string.
     *
     * @param value
     * @return string
     */
    public static String rand(String value) {
       return value + getRandomString(5);
    }

    /**
     * Generates a random sequence of characters of the specified length.
     *
     * @param length
     * @return string
     */
    public static String getRandomString(int length) {
       if (length < 1) {
          String message = "Wrong value: " + length + " The value should be 1 or bigger.";
          throw new IllegalArgumentException(message);
       }
       String charsAndDigits = LATIN_CHARS + DIGITS;
       StringBuilder sb = new StringBuilder(length);
       while (sb.length() < length) {
          sb.append(charsAndDigits.charAt((new Random()).nextInt(charsAndDigits.length())));
       }
       return sb.substring(0, length);
    }


}
