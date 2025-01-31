package com.maybeitssquid.ach;

/**
 * The byte arrays here are all invalid in UTF-8
 */
public class InvalidEncodingCases {

    public static final byte[] LONE_HIGH_SURROGATE = {};

    public static final byte[] LONE_LOW_SURROGATE = {};

    public static final byte[] UNPAIRED_HIGH_SURROGATE = {};

    public static final byte[] UNPAIRED_LOW_SURROGATE = {};

    public static final byte[] MISSING_SECOND_BYTE = {(byte) 0xE9, (byte) 0x20};

    public static final byte[] MISSING_THIRD_BYTE = {(byte) 0xEF, (byte) 0xBF, (byte) 0x20};

    public static final byte[] MISSING_FOURTH_BYTE = {(byte) 0xF0, (byte) 0x9F, (byte) 0x8F, (byte) 0x20};

    public static final byte[] UNEXPECTED_SECOND_BYTE_1 = {(byte) 0x20, (byte) 0xA9};

    public static final byte[] UNEXPECTED_SECOND_BYTE_2 = {(byte) 0x20, (byte) 0x80};

    public static final byte[] UNEXPECTED_THIRD_BYTE = {(byte) 0x20, (byte) 0xBD};

    public static final byte[][] INVALID_SEQUENCES = {
            {(byte) 0xC3, (byte) 0x28},
            {(byte) 0xF5, (byte) 0x80, (byte) 0x80},
            {(byte) 0xFC, (byte) 0xA1, (byte) 0xA1, (byte) 0xA1}};

    public static final byte[][] UNASSIGNED_SEQUENCES = {};

    /**
     * «Café» in ISO 8859-1
     */
    public static final byte[] PROBABLY_ISO_8859_1 =
            {(byte) 0xAB, (byte) 0x43, (byte) 0x61, (byte) 0x66, (byte) 0xE9, (byte) 0xBB};

    /**
     * “Coffee” with curly quotes in WIN-1262
     */
    public static final byte[] PROBABLY_WIN_1252 =
            {(byte) 0x93, (byte) 0x43, (byte) 0x6F, (byte) 0x66, (byte) 0x66, (byte) 0x65, (byte) 0x65, (byte) 0x94};

    /**
     * «Café» in little-endian UTF-16
     */
    public static final byte[] PROBABLY_LITTLE_ENDIAN_UTF_16 = {
            (byte) 0x00, (byte) 0xAB,
            (byte) 0x00, (byte) 0x43,
            (byte) 0x00, (byte) 0x61,
            (byte) 0x00, (byte) 0x66,
            (byte) 0x00, (byte) 0xE9,
            (byte) 0x00, (byte) 0xBB};

    /**
     * «Café» in little-endian UTF-16
     */
    public static final byte[] PROBABLY_BIG_ENDIAN_UTF_16 = {
            (byte) 0xAB, (byte) 0x00,
            (byte) 0x43, (byte) 0x00,
            (byte) 0x61, (byte) 0x00,
            (byte) 0x66, (byte) 0x00,
            (byte) 0xE9, (byte) 0x00,
            (byte) 0xBB, (byte) 0x00};

    /**
     * «Café» in little-endian UTF-32
     */
    public static final byte[] PROBABLY_UTF_32 = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xAB,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x43,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x61,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x66,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xE9,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xBB};
}
