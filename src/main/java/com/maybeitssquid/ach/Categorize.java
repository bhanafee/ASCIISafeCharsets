package com.maybeitssquid.ach;

import java.util.function.IntFunction;

import static java.lang.Character.*;

/**
 * Converts Unicode characters to their ASCII equivalents based on character categories.
 *
 * <ul>
 *     <li>{@code Nd} {@link Character#DECIMAL_DIGIT_NUMBER} are converted to their ASCII digit equivalents {@code 0-9}</li>
 *     <li>{@code Zs} {@link Character#SPACE_SEPARATOR} are converted to ASCII space</li>
 *     <li>{@code Zl, Zp} {@link Character#LINE_SEPARATOR} and {@link Character#PARAGRAPH_SEPARATOR} are converted to
 *     {@link #getLineSeparator() line separator}</li>
 *     <li>{@code Pd} {@link Character#DASH_PUNCTUATION} are converted to ASCII hyphen-minus {@code -}</li>
 *     <li>{@code Ps} {@link Character#START_PUNCTUATION} are converted to ASCII open parenthesis {@code (}</li>
 *     <li>{@code Pe} {@link Character#END_PUNCTUATION} are converted to ASCII close parenthesis {@code )}</li>
 *     <li>{@code Pc} {@link Character#CONNECTOR_PUNCTUATION} are converted to ASCII underscore {@code _}</li>
 *     <li>{@code Pi, Pf} {@link Character#INITIAL_QUOTE_PUNCTUATION} and {@link Character#FINAL_QUOTE_PUNCTUATION} are
 *     converted to ASCII double quote {@code "}</li>
 * </ul>
 * <p>
 * Special cases:
 * <ul>
 *     <li>{@link #UNICODE_NEL} is converted to system-specific line break</li>
 *     <li>{@link #UNICODE_REPLACEMENT} is converted to ASCII question mark</li>
 * </ul>
 */
public class Categorize extends Chainable {

    /**
     * The UNICODE replacement character
     */
    public static final char UNICODE_REPLACEMENT = '\uFFFD';

    /**
     * The UNICODE line separator character
     */
    public static final int UNICODE_NEL = 0x0085;

    protected final IntFunction<CharSequence> identity = Character::toString;

    private final CharSequence lineSeparator;

    /**
     * Creates a new Categorize instance with the specified delegate and line separator.
     *
     * @param delegate      the next step in the processing chain
     * @param lineSeparator the string to use for line separators
     */
    public Categorize(final IntFunction<CharSequence> delegate, final CharSequence lineSeparator) {
        super(delegate);
        this.lineSeparator = lineSeparator;
    }

    /**
     * Creates a new Categorizer instance with the specified delegate and default line separator.
     *
     * @param delegate the next step in the processing chain
     */
    public Categorize(final IntFunction<CharSequence> delegate) {
        this(delegate, System.lineSeparator());
    }

    /**
     * Retrieves the configured line separator.
     *
     * @return the character sequence used for line separation (e.g. for {@link #UNICODE_NEL}).
     */
    public CharSequence getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Transforms a codepoint based on its Unicode category.
     * <p>
     * This method maps specific Unicode categories (like punctuation and separators)
     * to their ASCII equivalents. Characters falling into unhandled categories are
     * returned as-is via {@link #identity}.
     *
     * @param codepoint the Unicode codepoint to process
     * @return the transformed string or the original character
     * @see Character#getType(int)
     */
    @Override
    protected CharSequence process(final int codepoint) {
        return switch (Character.getType(codepoint)) {
            case DECIMAL_DIGIT_NUMBER -> Integer.toString(Character.getNumericValue(codepoint));
            case SPACE_SEPARATOR -> " ";
            case LINE_SEPARATOR, PARAGRAPH_SEPARATOR -> getLineSeparator();
            case CONTROL -> codepoint == UNICODE_NEL ? getLineSeparator() : identity.apply(codepoint);
            case DASH_PUNCTUATION -> "-";
            case START_PUNCTUATION -> "(";
            case END_PUNCTUATION -> ")";
            case CONNECTOR_PUNCTUATION -> "_";
            case INITIAL_QUOTE_PUNCTUATION, FINAL_QUOTE_PUNCTUATION -> "\"";
            case OTHER_SYMBOL -> codepoint == UNICODE_REPLACEMENT ? "?" : identity.apply(codepoint);
            default -> identity.apply(codepoint);
        };
    }

    /**
     * Applies the categorization logic to the input value.
     * <p>
     * This method includes an optimization for ASCII characters (values < {@link #ASCII}).
     * If the input is already ASCII, it skips the categorization process and immediately
     * delegates to the next step in the chain.
     *
     * @param value the input codepoint
     * @return the processed character sequence
     */
    @Override
    public CharSequence apply(final int value) {
        return value < ASCII ? delegate(value) : super.apply(value);
    }
}
