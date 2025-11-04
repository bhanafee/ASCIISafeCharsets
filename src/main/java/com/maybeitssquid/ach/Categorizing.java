package com.maybeitssquid.ach;

import static java.lang.Character.*;

/**
 * A text normalization class that converts Unicode characters to their ASCII equivalents based on character categories.
 * Extends {@link Normalizing} to provide category-based character mapping functionality.
 *
 * <p>This class processes Unicode codepoints by their character categories (uppercase, lowercase,
 * punctuation, symbols, etc.) and maps them to simplified ASCII representations. For example:
 * <ul>
 *   <li>Various Unicode spaces are converted to ASCII space</li>
 *   <li>Different forms of quotes are normalized to ASCII quote marks</li>
 *   <li>Unicode dashes and hyphens are converted to ASCII hyphen-minus</li>
 *   <li>Unicode digits are converted to ASCII digits</li>
 *   <li>Line and paragraph separators are converted to system-specific line breaks</li>
 * </ul>
 */
public class Categorizing extends Normalizing {

    /** The UNICODE line separator character */
    private static final int UNICODE_NEL = 0x0085;

    /** The UNICODE replacement character */
    public static final char UNICODE_REPLACEMENT = '\uFFFD';

    /**
     * Creates a new instance of the Categorizing normalizer.
     */
    public Categorizing() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Categorizing encode(final int codepoint, final char as) {
        super.encode(codepoint, as);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Categorizing encode(final int codepoint, final char[] as) {
        super.encode(codepoint, as);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Categorizing encode(final int codepoint, final String as) {
        super.encode(codepoint, as);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Categorizing block(final int codepoint) {
        super.block(codepoint);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Categorizing blockControls() {
        super.blockControls();
        return this;
    }

    /**
     * Returns the system-specific line separator as a character array.
     *
     * @return A character array containing the platform's line separator
     * @see System#lineSeparator()
     */
    public char[] newLine() {
        return System.lineSeparator().toCharArray();
    }

    @Override
    protected char[] dispatch(final int codepoint) {
        if (codepoint < ASCII_BOUNDARY) {
            return ASCII[codepoint];
        } else {
            return switch (Character.getType(codepoint)) {
                case UPPERCASE_LETTER -> uppercase(codepoint);
                case LOWERCASE_LETTER -> lowercase(codepoint);
                case MODIFIER_LETTER -> modifierLetter(codepoint);
                case DECIMAL_DIGIT_NUMBER -> decimalDigit(codepoint);
                case SPACE_SEPARATOR -> spaceSeparator(codepoint);
                case LINE_SEPARATOR -> lineSeparator(codepoint);
                case PARAGRAPH_SEPARATOR -> paragraphSeparator(codepoint);
                case CONTROL -> control(codepoint);
                case DASH_PUNCTUATION -> dashPunctuation(codepoint);
                case START_PUNCTUATION -> startPunctuation(codepoint);
                case END_PUNCTUATION -> endPunctuation(codepoint);
                case CONNECTOR_PUNCTUATION -> connectorPunctuation(codepoint);
                case OTHER_PUNCTUATION -> otherPunctuation(codepoint);
                case MATH_SYMBOL -> mathSymbol(codepoint);
                case MODIFIER_SYMBOL -> modifierSymbol(codepoint);
                case OTHER_SYMBOL -> otherSymbol(codepoint);
                case INITIAL_QUOTE_PUNCTUATION, FINAL_QUOTE_PUNCTUATION -> quotePunctuation(codepoint);
                default -> NOTHING;
            };
        }
    }

    /**
     * Processes uppercase letter characters. By default, only ASCII uppercase letters (A-Z)
     * are preserved as-is; all other uppercase letters are filtered out.
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing the uppercase letter if it's ASCII, otherwise an empty array
     */
    protected char[] uppercase(@SuppressWarnings("unused") final int codepoint) {
        if (codepoint < ASCII_BOUNDARY && Character.isUpperCase(codepoint)) {
            return ASCII[codepoint];
        } else {
            return NOTHING;
        }
    }

    /**
     * Processes lowercase letter characters. By default, only ASCII lowercase letters (a-z)
     * are preserved as-is; all other lowercase letters are filtered out.
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing the lowercase letter if it's ASCII, otherwise an empty array
     */
    protected char[] lowercase(@SuppressWarnings("unused") final int codepoint) {
        if (codepoint < ASCII_BOUNDARY && Character.isLowerCase(codepoint)) {
            return ASCII[codepoint];
        } else {
            return NOTHING;
        }
    }

    /**
     * Processes modifier letter characters. Modifier letters (such as superscript letters or
     * phonetic modifiers) are filtered out by default as they have no direct ASCII equivalents.
     *
     * @param codepoint The Unicode code point to process
     * @return An empty character array (modifier letters are not transliterated)
     */
    protected char[] modifierLetter(@SuppressWarnings("unused") final int codepoint) {
        return NOTHING;
    }

    /**
     * Processes decimal digit characters. Converts Unicode decimal digits from any script
     * (Arabic, Devanagari, etc.) to their ASCII equivalents (0-9) based on their numeric value.
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing the ASCII digit equivalent, or an empty array if
     *         the numeric value is not in the range 0-9
     */
    protected char[] decimalDigit(final int codepoint) {
        final int value = Character.getNumericValue(codepoint);
        if (value >= 0 && value <= 9) {
            return ASCII['0' + value];
        }
        return NOTHING;
    }

    /**
     * Processes space separator characters. All Unicode space separators (including non-breaking
     * spaces, en spaces, em spaces, etc.) are normalized to ASCII space (U+0020).
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing a single ASCII space character
     */
    protected char[] spaceSeparator(@SuppressWarnings("unused") final int codepoint) {
        return ASCII[' '];
    }

    /**
     * Processes line separator characters. Unicode line separators are converted to the
     * platform-specific line separator sequence.
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing the system's line separator
     * @see #newLine()
     */
    protected char[] lineSeparator(@SuppressWarnings("unused") final int codepoint) {
        return newLine();
    }

    /**
     * Processes paragraph separator characters. Unicode paragraph separators are converted to the
     * platform-specific line separator sequence.
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing the system's line separator
     * @see #newLine()
     */
    protected char[] paragraphSeparator(@SuppressWarnings("unused") final int codepoint) {
        return newLine();
    }

    /**
     * Processes control characters. By default, all control characters are filtered out except
     * for the Unicode Next Line character (NEL, U+0085), which is converted to the platform-specific
     * line separator.
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing the line separator for NEL, otherwise an empty array
     */
    protected char[] control(final int codepoint) {
        return codepoint == UNICODE_NEL ? newLine() : NOTHING;
    }

    /**
     * Processes dash punctuation characters. All Unicode dash and hyphen characters
     * (en dash, em dash, figure dash, etc.) are normalized to ASCII hyphen-minus (U+002D).
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing a single ASCII hyphen-minus character
     */
    protected char[] dashPunctuation(@SuppressWarnings("unused") final int codepoint) {
        return ASCII['-'];
    }

    /**
     * Processes start punctuation characters. All Unicode opening brackets, parentheses,
     * and braces are normalized to ASCII left parenthesis (U+0028).
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing a single ASCII left parenthesis
     */
    protected char[] startPunctuation(@SuppressWarnings("unused") final int codepoint) {
        return ASCII['('];
    }

    /**
     * Processes end punctuation characters. All Unicode closing brackets, parentheses,
     * and braces are normalized to ASCII right parenthesis (U+0029).
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing a single ASCII right parenthesis
     */
    protected char[] endPunctuation(@SuppressWarnings("unused") final int codepoint) {
        return ASCII[')'];
    }

    /**
     * Processes connector punctuation characters. All Unicode connector punctuation
     * (such as undertie and character tie) are normalized to ASCII underscore (U+005F).
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing a single ASCII underscore
     */
    protected char[] connectorPunctuation(@SuppressWarnings("unused") final int codepoint) {
        return ASCII['_'];
    }

    /**
     * Processes other punctuation characters. By default, punctuation that doesn't fit
     * into other specific categories is filtered out. Subclasses can override this method
     * to provide specific mappings for individual punctuation marks.
     *
     * @param codepoint The Unicode code point to process
     * @return An empty character array (other punctuation is not transliterated by default)
     */
    protected char[] otherPunctuation(@SuppressWarnings("unused") final int codepoint) {
        return NOTHING;
    }

    /**
     * Processes modifier symbol characters. Modifier symbols (such as circumflex accent or
     * caron) are filtered out by default as they typically modify adjacent characters
     * rather than standing alone.
     *
     * @param codepoint The Unicode code point to process
     * @return An empty character array (modifier symbols are not transliterated)
     */
    protected char[] modifierSymbol(@SuppressWarnings("unused") final int codepoint) {
        return NOTHING;
    }

    /**
     * Processes mathematical symbol characters. By default, mathematical symbols
     * (such as integral, summation, or set theory symbols) are filtered out.
     * Subclasses can override this method to provide specific mappings for commonly
     * used math symbols like plus, minus, or equals.
     *
     * @param codepoint The Unicode code point to process
     * @return An empty character array (math symbols are not transliterated by default)
     */
    protected char[] mathSymbol(@SuppressWarnings("unused") final int codepoint) {
        return NOTHING;
    }

    /**
     * Processes other symbol characters. By default, most symbols are filtered out,
     * except for the Unicode Replacement Character (U+FFFD), which is converted to
     * ASCII question mark (U+003F).
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing an ASCII question mark for the replacement character,
     *         otherwise an empty array
     */
    protected char[] otherSymbol(final int codepoint) {
        return codepoint == UNICODE_REPLACEMENT ? ASCII['?'] : NOTHING;
    }

    /**
     * Processes quotation punctuation characters. All Unicode quotation marks
     * (single quotes, double quotes, angled quotes, etc.) are normalized to
     * ASCII quotation mark (U+0022).
     *
     * @param codepoint The Unicode code point to process
     * @return A character array containing a single ASCII quotation mark
     */
    protected char[] quotePunctuation(@SuppressWarnings("unused") final int codepoint) {
        return ASCII['"'];
    }
}

