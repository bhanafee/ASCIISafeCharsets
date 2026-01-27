package com.maybeitssquid.ach;

import static java.lang.Character.*;

import java.util.function.IntFunction;

/**
 * Converts Unicode characters to their ASCII equivalents based on character categories.
 *
 * <ul>
 *   <li>{@code Nd} {@link Character#DECIMAL_DIGIT_NUMBER} are converted to their ASCII digit
 *       equivalents {@code 0-9}
 *   <li>{@code Zs} {@link Character#SPACE_SEPARATOR} are converted to ASCII space
 *   <li>{@code Zl, Zp, U+0085} {@link Character#LINE_SEPARATOR}, {@link
 *       Character#PARAGRAPH_SEPARATOR} and {@link #UNICODE_NEL} are converted to {@link
 *       #getLineSeparator() line separator}. Carriage return {@code U+000D} and line feed {@code
 *       U+000A} are passed as-is.
 *   <li>{@code Pd} {@link Character#DASH_PUNCTUATION} are converted to ASCII hyphen-minus {@code -}
 *   <li>{@code Ps} {@link Character#START_PUNCTUATION} are converted to ASCII open parenthesis
 *       {@code (}
 *   <li>{@code Pe} {@link Character#END_PUNCTUATION} are converted to ASCII close parenthesis
 *       {@code )}
 *   <li>{@code Pc} {@link Character#CONNECTOR_PUNCTUATION} are converted to ASCII underscore {@code
 *       _}
 *   <li>{@code Pi, Pf} {@link Character#INITIAL_QUOTE_PUNCTUATION} and {@link
 *       Character#FINAL_QUOTE_PUNCTUATION} are converted to ASCII double quote {@code "}
 *   <li>{@code U+FFFD} {@link #UNICODE_REPLACEMENT} is converted to ASCII question mark {@code ?}
 * </ul>
 */
public class Categorize extends Chainable {

  /** The UNICODE replacement character */
  public static final char UNICODE_REPLACEMENT = '\uFFFD';

  /** The UNICODE line separator character */
  public static final int UNICODE_NEL = 0x0085;

  /**
   * Identity function that converts a codepoint to its string representation without
   * transformation.
   */
  protected static final IntFunction<CharSequence> identity = Character::toString;

  private final CharSequence lineSeparator;

  /**
   * Creates a new Categorize instance with the specified delegate and line separator.
   *
   * @param delegate the next step in the processing chain
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

  /** Creates a new Categorizer instance with an identity delegate and default line separator. */
  public Categorize() {
    this(identity);
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
   * Converts start punctuation codepoints to their ASCII equivalent.
   *
   * @param codepoint the start punctuation codepoint
   * @return ASCII open parenthesis {@code (}
   */
  protected CharSequence startPunctuation(final int codepoint) {
    return "(";
  }

  /**
   * Converts end punctuation codepoints to their ASCII equivalent.
   *
   * @param codepoint the end punctuation codepoint
   * @return ASCII close parenthesis {@code )}
   */
  protected CharSequence endPunctuation(final int codepoint) {
    return ")";
  }

  /**
   * Converts quote punctuation codepoints to their ASCII equivalent.
   *
   * @param codepoint the quote punctuation codepoint
   * @return ASCII double quote {@code "}
   */
  protected CharSequence quotePunctuation(final int codepoint) {
    return "\"";
  }

  /**
   * Transforms a codepoint based on its Unicode category.
   *
   * <p>This method maps specific Unicode categories (like punctuation and separators) to their
   * ASCII equivalents. Characters falling into unhandled categories are returned as-is via {@link
   * #identity}.
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
      case START_PUNCTUATION -> startPunctuation(codepoint);
      case END_PUNCTUATION -> endPunctuation(codepoint);
      case CONNECTOR_PUNCTUATION -> "_";
      case INITIAL_QUOTE_PUNCTUATION, FINAL_QUOTE_PUNCTUATION -> quotePunctuation(codepoint);
      case OTHER_SYMBOL -> codepoint == UNICODE_REPLACEMENT ? "?" : identity.apply(codepoint);
      default -> identity.apply(codepoint);
    };
  }

  /**
   * Applies the categorization logic to the input value.
   *
   * <p>This method includes an optimization for ASCII characters (values &lt; {@link #ASCII}). If
   * the input is already ASCII, it skips the categorization process and immediately delegates to
   * the next step in the chain.
   *
   * @param value the input codepoint
   * @return the processed character sequence
   */
  @Override
  public CharSequence apply(final int value) {
    return value < ASCII ? delegate(value) : super.apply(value);
  }
}
