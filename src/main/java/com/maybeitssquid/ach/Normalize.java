package com.maybeitssquid.ach;

import java.text.Normalizer;
import java.util.function.IntFunction;

/**
 * A {@link Chainable} step that normalizes Unicode characters to a specific form.
 * <p>
 * This class uses {@link java.text.Normalizer} to decompose or compose characters.
 * By default, it uses {@link java.text.Normalizer.Form#NFKD} (Compatibility Decomposition),
 * which is useful for converting compatibility characters (like ligatures or wide characters)
 * into their base components before further processing.
 * </p>
 *
 * @see java.text.Normalizer
 * @see java.text.Normalizer.Form
 */
public class Normalize extends Chainable {

    /**
     * The lowest codepoint value that is not normalized in {@link Normalizer.Form#NFKD}, corresponding to NO-BREAK SPACE
     */
    public static final int LOWEST_COMPOSED_CODEPOINT = 0x00A0;

    private final Normalizer.Form form;

    /**
     * Creates a new Normalize instance with the specified normalization form.
     *
     * @param delegate the next step in the processing chain
     * @param form     the {@link Normalizer.Form} to use for character normalization
     */
    public Normalize(final IntFunction<CharSequence> delegate, final Normalizer.Form form) {
        super(delegate);
        this.form = form;
    }

    /**
     * Creates a new Normalize instance using the default {@link Normalizer.Form#NFKD} normalization form.
     *
     * @param delegate the next step in the processing chain
     */
    public Normalize(final IntFunction<CharSequence> delegate) {
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
     * <p>
     * This method includes an optimization to skip normalization for characters
     * below {@link #LOWEST_COMPOSED_CODEPOINT}, assuming they are invariant
     * under the configured normalization form (typically NFKD).
     * </p>
     *
     * @param value the input codepoint
     * @return the processed character sequence from the delegate chain
     */
    @Override
    public CharSequence apply(final int value) {
        return value < LOWEST_COMPOSED_CODEPOINT ? delegate(value) : super.apply(value);
    }
}