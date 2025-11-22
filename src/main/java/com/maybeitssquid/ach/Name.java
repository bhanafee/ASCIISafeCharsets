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
    private static final Pattern latin = Pattern.compile("LATIN (SMALL |CAPITAL )?LETTER ([A-Z]+ )*(?<letter>\\p{Upper}\\p{Upper}?)\\b");

    public static final int UNICODE_CIRCLED_ZERO_WITH_SLASH = 0x1F10D;

    public static final int UNICODE_COLON_EQUALS = 0x2254;
    public static final int UNICODE_EQUALS_COLON = 0x2255;
    public static final int UNICODE_COLON_SIGN = 0x20A1;

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
     * Transliterates a codepoint based on its type and name.
     *
     * @param codepoint the Unicode codepoint to process
     * @return the transliterated ASCII string, or the result of the superclass processing
     *         if no specific name-based rule applies
     */
    @Override
    protected CharSequence process(final int codepoint) {
        return switch (Character.getType(codepoint)) {
            case UPPERCASE_LETTER -> uppercase(codepoint);
            case LOWERCASE_LETTER -> lowercase(codepoint);
            case START_PUNCTUATION -> startPunctuation(codepoint);
            case END_PUNCTUATION -> endPunctuation(codepoint);
            case INITIAL_QUOTE_PUNCTUATION, FINAL_QUOTE_PUNCTUATION -> quotePunctuation(codepoint);
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
     * @param codepoint the codepoint to process
     * @return the base letter if found in the name, otherwise an empty string
     */
    protected CharSequence uppercase(final int codepoint) {
        return letter(codepoint);
    }

    /**
     * Extracts the base ASCII character for a lowercase letter from its name.
     *
     * @param codepoint the codepoint to process
     * @return the base letter converted to lowercase if found, otherwise an empty string
     */
    protected CharSequence lowercase(final int codepoint) {
        return letter(codepoint).toLowerCase(Locale.ROOT);
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
    protected CharSequence quotePunctuation(final int codepoint) {
        final String name = Character.getName(codepoint);
        if (name.contains("DOUBLE") || name.contains("DOTTED")) {
            return "\"";
        } else {
            return "'";
        }
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
     *   <li>AMPERSAND → {@code &}
     *   <li>FULL STOP → {@code .}
     *   <li>APOSTROPHE → {@code '}
     *   <li>EXCLAMATION MARK → {@code !}
     *   <li>QUESTION → {@code ?}
     *   <li>ASTERISK → {@code *}
     *   <li>SEMICOLON → {@code ;}
     *   <li>PERCENT → {@code %}
     *   <li>PLUS SIGN → {@code +}
     *   <li>MULTIPLICATION → {@code X}
     *   <li>REVERSE SOLIDUS, BACKSLASH → {@code \}
     *   <li>SOLIDUS, SLASH → {@code /} (except circled zero with slash)
     *   <li>COMMA → {@code ,}
     *   <li>EQUAL → {@code =} (special cases for colon equals and equals colon)
     *   <li>COLON → {@code :} (except Colombian currency symbol)
     *   <li>TILDE → {@code ~}
     * </ul>
     */
    protected CharSequence byName(final int codepoint) {
        final String name = Character.getName(codepoint);
        if (name.contains("AMPERSAND")) {
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
        } else if (name.contains("REVERSE SOLIDUS") || name.contains("BACKSLASH")) {
            return "\\";
        } else if (name.contains("SOLIDUS") || name.contains("SLASH") && codepoint != UNICODE_CIRCLED_ZERO_WITH_SLASH) {
            return "/";
        } else if (name.contains("COMMA")) {
            return ",";
        } else if (name.contains("EQUAL")) {
            if (codepoint == UNICODE_COLON_EQUALS) {
                return ":=";
            } else if (codepoint == UNICODE_EQUALS_COLON) {
                return "=:";
            } else {
                return "=";
            }
        } else if (name.contains("COLON") && codepoint != UNICODE_COLON_SIGN) {
            return ":";
        } else if (name.contains("TILDE")) {
            return "~";
        } else {
            return identity(codepoint);
        }
    }
}