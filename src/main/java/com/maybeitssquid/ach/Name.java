package com.maybeitssquid.ach;

import java.util.Locale;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.*;

/**
 * A transliteration step that converts Unicode characters to ASCII based on their Unicode names.
 * <p>
 * This class extends {@link Categorize} to provide more granular mappings for characters
 * that cannot be mapped simply by category. It parses the Unicode name of a character
 * (retrieved via {@link Character#getName(int)}) to find ASCII equivalents for:
 * <ul>
 *     <li>Latin letters (including those with diacritics)</li>
 *     <li>Bracket types (square, curly, angle)</li>
 *     <li>Quotation marks</li>
 *     <li>Various symbols and punctuation</li>
 * </ul>
 */
public class Name extends Categorize {
    public static final int UNICODE_CIRCLED_ZERO_WITH_SLASH = 0x1F10D;
    public static final int UNICODE_TRIPLE_SOLIDUS_BINARY_RELATION = 0x2AFB;
    public static final int UNICODE_DOUBLE_SOLIDUS_OPERATOR = 0x2AFD;
    public static final int UNICODE_OCR_DOUBLE_BACKSLASH = 0x244A;
    public static final int UNICODE_COLON_SIGN = 0x20A1;
    public static final int UNICODE_COLON_EQUALS = 0x2254;
    public static final int UNICODE_EQUALS_COLON = 0x2255;
    public static final int UNICODE_CIRCLED_DOLLAR_SIGN_WITH_OVERLAID_BACKSLASH = 0x1F10F;
    public static final int UNICODE_CIRCLED_C_WITH_OVERLAID_BACKSLASH = 0x1F16E;
    private static final Pattern latin = Pattern.compile("LATIN (SMALL |CAPITAL )?LETTER ([A-Z]+ )*(?<letter>\\p{Upper}\\p{Upper}?)\\b");

    /**
     * Creates a new Name transliterator with the specified delegate and line separator.
     *
     * @param delegate      the next step in the processing chain
     * @param lineSeparator the string to use for line separators
     */
    public Name(final IntFunction<CharSequence> delegate, final CharSequence lineSeparator) {
        super(delegate, lineSeparator);
    }

    /**
     * Creates a new Name transliterator with the specified delegate and default line separator.
     *
     * @param delegate the next step in the processing chain
     */
    public Name(final IntFunction<CharSequence> delegate) {
        super(delegate);
    }

    /**
     * Creates a new Name transliterator with an identity delegate and default line separator.
     */
    public Name() {
        super();
    }

    /**
     * Transliterates a codepoint based on its type and name.
     *
     * @param codepoint the Unicode codepoint to process
     * @return the transliterated ASCII string, or the result of the superclass processing
     * if no specific name-based rule applies
     */
    @Override
    protected CharSequence process(final int codepoint) {
        return switch (Character.getType(codepoint)) {
            case UPPERCASE_LETTER -> uppercase(codepoint);
            case LOWERCASE_LETTER -> lowercase(codepoint);
            case TITLECASE_LETTER -> titlecase(codepoint);
            case MODIFIER_LETTER, OTHER_PUNCTUATION, MODIFIER_SYMBOL, MATH_SYMBOL -> byName(codepoint);
            case OTHER_SYMBOL -> codepoint == UNICODE_REPLACEMENT ? super.process(codepoint) : byName(codepoint);
            default -> super.process(codepoint);
        };
    }

    private String letter(final int codepoint) {
        final String name = Character.getName(codepoint);
        final Matcher m = latin.matcher(name);
        return m.find() ? m.group("letter") : "";
    }

    /**
     * Extracts the base ASCII character for an uppercase letter from its name.
     *
     * @param codepoint the uppercase codepoint to process
     * @return the base letter if found in the name, otherwise an empty string
     */
    protected CharSequence uppercase(final int codepoint) {
        return letter(codepoint);
    }

    /**
     * Extracts the base ASCII character for a lowercase letter from its name.
     *
     * @param codepoint the lowercase codepoint to process
     * @return the base letter converted to lowercase if found, otherwise an empty string
     */
    protected CharSequence lowercase(final int codepoint) {
        return letter(codepoint).toLowerCase(Locale.ROOT);
    }

    /**
     * Processes the titlecase characters. There are only four that transliterate to ASCII; the remainder are Greek.
     * This method will have no effect if a prior processing step has already decomposed the codepoint.
     *
     * @param codepoint the title case codepoint to process
     * @return the ASCII equivalent, or an empty string if not found
     */
    protected CharSequence titlecase(final int codepoint) {
        return switch (codepoint) {
            case 0x01C5, 0x01F2 -> "Dz";
            case 0x01C8 -> "Lj";
            case 0x01CB -> "Nj";
            default -> "";
        };
    }

    /**
     * Maps start punctuation to ASCII brackets based on name.
     * <p>
     * Detects:
     * <ul>
     *     <li>Square brackets: [</li>
     *     <li>Curly braces: {</li>
     *     <li>Angle brackets: &lt;</li>
     *     <li>Others (default): (</li>
     * </ul>
     *
     * @param codepoint the codepoint to process
     * @return the corresponding ASCII opening bracket
     */
    @Override
    protected CharSequence startPunctuation(final int codepoint) {
        final String name = Character.getName(codepoint);
        if (name.contains("SQUARE")) {
            return "[";
        } else if (name.contains("CURLY")) {
            return "{";
        } else if (name.contains("ANGLE")) {
            return "<";
        } else {
            return "(";
        }
    }

    /**
     * Maps end punctuation to ASCII brackets based on name.
     * <p>
     * Detects:
     * <ul>
     *     <li>Square brackets: ]</li>
     *     <li>Curly braces: }</li>
     *     <li>Angle brackets: &gt;</li>
     *     <li>Others (default): )</li>
     * </ul>
     *
     * @param codepoint the codepoint to process
     * @return the corresponding ASCII closing bracket
     */
    @Override
    protected CharSequence endPunctuation(final int codepoint) {
        final String name = Character.getName(codepoint);
        if (name.contains("SQUARE")) {
            return "]";
        } else if (name.contains("CURLY")) {
            return "}";
        } else if (name.contains("ANGLE")) {
            return ">";
        } else {
            return ")";
        }
    }

    /**
     * Maps quote punctuation to ASCII quotes based on name.
     * <p>
     * Maps to {@code "} if the name contains "DOUBLE" or "DOTTED", otherwise maps to {@code '}.
     *
     * @param codepoint the codepoint to process
     * @return the corresponding ASCII quote character
     */
    @Override
    protected CharSequence quotePunctuation(final int codepoint) {
        final String name = Character.getName(codepoint);
        if (name.contains("DOUBLE") || name.contains("DOTTED")) {
            return "\"";
        } else {
            return "'";
        }
    }

    protected CharSequence solidus(final String name, final int codepoint) {
        if (name.contains("REVERSE SOLIDUS")) {
            return "\\";
        } else if (name.contains("BACKSLASH")) {
            return switch (codepoint) {
                case UNICODE_OCR_DOUBLE_BACKSLASH -> "\\\\";
                case UNICODE_CIRCLED_DOLLAR_SIGN_WITH_OVERLAID_BACKSLASH -> "$";
                case UNICODE_CIRCLED_C_WITH_OVERLAID_BACKSLASH -> "C";
                default -> "\\";
            };
        } else {
            return switch (codepoint) {
                case UNICODE_CIRCLED_ZERO_WITH_SLASH -> "0";
                case UNICODE_TRIPLE_SOLIDUS_BINARY_RELATION -> "///";
                case UNICODE_DOUBLE_SOLIDUS_OPERATOR -> "//";
                default -> "/";
            };
        }
    }

    protected CharSequence equal(final int codepoint) {
        return switch (codepoint) {
            case UNICODE_COLON_EQUALS -> ":=";
            case UNICODE_EQUALS_COLON -> "=:";
            default -> "=";
        };
    }

    /**
     * Converts a Unicode codepoint to ASCII by analyzing its character name.
     *
     * <p>This method uses {@link Character#getName(int)} to retrieve the Unicode character
     * name and matches it against known naming patterns to determine the appropriate ASCII
     * equivalent. It handles common punctuation marks and symbols by checking if their
     * names contain specific keywords.
     *
     * <p>Recognized patterns include:
     * <ul>
     *   <li>LATIN [CAPITAL|SMALL] LETTER → corresponding letter</li>
     *   <li>REVERSE SOLIDUS, BACKSLASH → {@code \} (exceptions noted)
     *   <li>SOLIDUS, SLASH → {@code /} (exceptions noted)
     *   <li>EQUAL → {@code =} (special cases for colon equals and equals colon)
     *   <li>AMPERSAND → {@code &}
     *   <li>FULL STOP → {@code .} (does not handle composed "[DIGIT|NUMBER] x FULL STOP")
     *   <li>APOSTROPHE → {@code '}
     *   <li>EXCLAMATION MARK → {@code !}
     *   <li>QUESTION → {@code ?}
     *   <li>INTERROBANG → {@code ?!}
     *   <li>ASTERISK → {@code *}
     *   <li>SEMICOLON → {@code ;}
     *   <li>PERCENT → {@code %}
     *   <li>PLUS SIGN → {@code +}
     *   <li>MULTIPLICATION → {@code X}
     *   <li>COMMA → {@code ,}
     *   <li>COLON → {@code :} (except Colombian currency symbol)
     *   <li>TILDE → {@code ~}
     * </ul>
     */
    protected CharSequence byName(final int codepoint) {
        final String name = Character.getName(codepoint);
        if (name.contains("LATIN CAPITAL LETTER")) {
            return uppercase(codepoint);
        } else if (name.contains("LATIN SMALL LETTER")) {
            return lowercase(codepoint);
        } else if (name.contains("SOLIDUS") || name.contains("SLASH")) {
            return solidus(name, codepoint);
        } else if (name.contains("EQUAL")) {
            return equal(codepoint);
        } else if (name.contains("AMPERSAND")) {
            return "&";
        } else if (name.contains("FULL STOP")) {
            return ".";
        } else if (name.contains("APOSTROPHE")) {
            return "'";
        } else if (name.contains("EXCLAMATION MARK")) {
            return "!";
        } else if (name.contains("QUESTION")) {
            return "?";
        } else if (name.contains("INTERROBANG")) {
            return "?!";
        } else if (name.contains("ASTERISK")) {
            return "*";
        } else if (name.contains("SEMICOLON")) {
            return ";";
        } else if (name.contains("PERCENT")) {
            return "%";
        } else if (name.contains("PLUS SIGN")) {
            return "+";
        } else if (name.contains("MULTIPLICATION")) {
            return "X";
        } else if (name.contains("COMMA")) {
            return ",";
        } else if (name.contains("TILDE")) {
            return "~";
        } else if (name.contains("COLON") && codepoint != UNICODE_COLON_SIGN) {
            return ":";
        } else {
            return identity.apply(codepoint);
        }
    }
}