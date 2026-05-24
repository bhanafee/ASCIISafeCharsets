package com.maybeitssquid.safeascii;

import java.text.Normalizer;
import java.util.function.IntFunction;

/**
 * A {@link Chainable} step that normalizes Unicode characters to a specific form.
 *
 * <p>This class uses {@link java.text.Normalizer} to decompose characters. By default, it uses
 * {@link java.text.Normalizer.Form#NFKD} (Compatibility Decomposition), which is useful for
 * converting compatibility characters (like ligatures or wide characters) into their base
 * components before further processing. Only decomposition is supported, not composition.
 *
 * @see java.text.Normalizer
 * @see java.text.Normalizer.Form
 */
public class Decompose extends Chainable {

  /** The lowest codepoint value that is not decomposed, corresponding to NO-BREAK SPACE */
  public static final int LOWEST_COMPOSED_CODEPOINT = 0x00A0;

  private final Normalizer.Form form;

  /**
   * Creates a new Decompose instance with the specified normalization (decomposition) form.
   *
   * @param delegate the next step in the processing chain
   * @param form the {@link Normalizer.Form} to use for character decomposition, which must be NFD
   *     or NFKD.
   */
  public Decompose(final IntFunction<CharSequence> delegate, final Normalizer.Form form) {
    super(delegate);
    this.form =
        switch (form) {
          case NFD, NFKD -> form;
          default -> throw new IllegalArgumentException("Unsupported Normalizer.Form: " + form);
        };
  }

  /**
   * Creates a new Decompose instance using the default {@link Normalizer.Form#NFKD} normalization
   * form.
   *
   * @param delegate the next step in the processing chain
   */
  public Decompose(final IntFunction<CharSequence> delegate) {
    this(delegate, Normalizer.Form.NFKD);
  }

  /**
   * Normalizes a single codepoint.
   *
   * @param codepoint the Unicode codepoint to process
   * @return the normalized string representation of the codepoint
   */
  @Override
  protected CharSequence process(final int codepoint) {
    final CharSequence input = Character.toString(codepoint);
    return Normalizer.isNormalized(input, form) ? input : Normalizer.normalize(input, form);
  }

  /**
   * Applies normalization to the input value.
   *
   * <p>This method includes an optimization to skip normalization for characters below {@link
   * #LOWEST_COMPOSED_CODEPOINT}, because they are invariant under all normalization forms.
   *
   * @param value the input codepoint
   * @return the processed character sequence from the delegate chain
   */
  @Override
  public CharSequence apply(final int value) {
    return value < LOWEST_COMPOSED_CODEPOINT ? delegate(value) : super.apply(value);
  }
}
