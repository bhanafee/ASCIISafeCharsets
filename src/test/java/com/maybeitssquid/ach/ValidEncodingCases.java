package com.maybeitssquid.ach;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * A {@link Charset} should have well-defined encodings for all of the {@code char[]} examples in this class. That
 * encoding may be zero bytes.
 */
public class ValidEncodingCases {

    public static final char[] UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static final char[] LOWER_CASE = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public static final char[] CAFE = {'C', 'a', 'f', '\u00E9'};

    public static final char[] CURRENCY = {'\u20AC'};

    public static final char[] HIRAGANA = {'\u3042'};

    public static final char[] COFFEE_EMOJI = {'\u2615'};

    public static final char[] ATM_EMOJI = {'\uD83C', '\uDFE7'};

    public static final char[] GOTHIC_HWAIR = {'\uD800', '\uDF48'};

    public static final char[] US_FLAG_COMPOSED = {'\uD83C', '\uDDFA', '\uD83C', '\uDDF8'};

    public static final char[] PRIVATE_USE_BMP = {'\uE000'};

    public static final char[] PRIVATE_USE_PLANE_15 = {'\uDB80', '\uDC00'};

    public static final char[] PRIVATE_USE_PLANE_16 = {'\uDBC0', '\uDC00'};

    public static final char[] BOM = {'\uFEFF'};

    public static final char[] REPLACEMENT = {'\uFFFD'};

    public static final char[] UNICODE_SPECIALS = {'\uFFF9', '\uFFFA', '\uFFFB', '\uFFFC', '\uFFFD'};

    public static final char[] UNICODE_NONCHARACTERS = {'\uFFFE', '\uFFFF'};

    /**
     *
     * @param charset the character set to use for encoding.
     * @param input the chars to encode.
     * @param expected the expected result of encoding the chars
     * @param message format string for failure message.
     *                Arguments are: 1$ position, 2$ expected byte, 3$ actual byte
     * @return Formatted error message or empty string.
     */
    public String testEncoding(final Charset charset, final char[] input, final byte[] expected, final String message) {
        final ByteBuffer decoded = charset.encode(CharBuffer.wrap(input));
        final byte[] result = new byte[decoded.position()];
        decoded.get(result);
        for (int i = 0; i < result.length; i++) {
            if (i < expected.length ) {
                if (result[i] != expected[i]) {
                    return String.format(message, i, expected[i], result[i]);
                }
            } else {
                return String.format(message, i, 0x00, result[i]);
            }
        }
        return "";
    }

    public boolean isASCII(final byte[] input) {
        for (byte b : input) {
            if (b < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isASCIIPrintable(final byte[] input) {
        for (byte b : input) {
            if (b < 0x20 || b == 0x7F) {
                return false;
            }
        }
        return true;
    }
}
