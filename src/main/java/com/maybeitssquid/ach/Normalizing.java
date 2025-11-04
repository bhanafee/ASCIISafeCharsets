package com.maybeitssquid.ach;

import java.nio.CharBuffer;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * A character normalization utility that converts Unicode characters to ASCII equivalents.
 * Extends {@link Filtering} to provide character normalization capabilities using Java's
 * {@link Normalizer} functionality.
 *
 * <p>This class provides mechanisms to:
 * <ul>
 *   <li>Convert Unicode characters to their ASCII equivalents</li>
 *   <li>Define custom character mappings</li>
 *   <li>Block specific characters from conversion</li>
 *   <li>Cache normalized character mappings for improved performance</li>
 * </ul>
 *
 * Usage example:
 * {@snippet :
 * Normalizing normalizer = new Normalizing();
 * normalizer.encode(0x00E9, 'e');  // Map é to e
 * char[] result = normalizer.apply(0x00E9);
 * }
 *
 * @see java.text.Normalizer
 * @see java.text.Normalizer.Form
 */
public class Normalizing extends Filtering {
    private static final int NORMALIZATION_BUFFER_SIZE = 20;

    private final Map<Integer, char[]> encodings = new HashMap<>();

    private final Normalizer.Form form;

    /**
     * Creates a new Normalizing instance with the specified normalization form.
     *
     * @param form the {@link Normalizer.Form} to use for character normalization
     */
    public Normalizing(final Normalizer.Form form) {
        this.form = form;
    }

    /**
     * Creates a new Normalizing instance using the default {@code NFKD} normalization form.
     */
    public Normalizing() {
        this(Normalizer.Form.NFKD);
    }

    /**
     * Returns the normalization form being used by this instance.
     *
     * @return the current {@link Normalizer.Form}
     */
    public Normalizer.Form getForm() {
        return form;
    }

    /**
     * Maps a Unicode codepoint to a single ASCII character.
     *
     * @param codepoint the Unicode codepoint to encode
     * @param as        the ASCII character to map to
     * @return {@inheritDoc}
     */
    @Override
    public Normalizing encode(final int codepoint, final char as) {
        if (codepoint <= ASCII_BOUNDARY) {
            super.encode(codepoint, as);
        } else if (as <= ASCII_BOUNDARY) {
            encodings.put(codepoint, ASCII[as]);
        } else {
            encodings.put(codepoint, new char[]{as});
        }
        return this;
    }

    /**
     * Maps a Unicode codepoint to a sequence of ASCII characters.
     *
     * @param codepoint the Unicode codepoint to encode
     * @param as        {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Normalizing encode(final int codepoint, final char[] as) {
        if (codepoint <= ASCII_BOUNDARY) {
            super.encode(codepoint, as);
        } else {
            encodings.put(codepoint, as);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param codepoint {@inheritDoc}
     * @param as        {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Normalizing encode(final int codepoint, final String as) {
        super.encode(codepoint, as);
        return this;
    }

    /**
     * Blocks a specific Unicode codepoint from being converted.
     *
     * @param codepoint the Unicode codepoint to block
     * @return {@inheritDoc}
     */
    @Override
    public Normalizing block(final int codepoint) {
        if (codepoint > ASCII_BOUNDARY) {
            encodings.remove(codepoint);
        } else {
            super.block(codepoint);
        }
        return this;
    }

    /**
     * Applies normalization to convert a Unicode codepoint to its ASCII equivalent(s).
     *
     * @param value the Unicode codepoint to normalize
     * @return an array of ASCII characters representing the normalized form
     */
    @Override
    public char[] apply(final int value) {
        if (value < ASCII_BOUNDARY) {
            return ASCII[value];
        } else if (this.encodings.containsKey(value)) {
            return this.encodings.get(value);
        } else {
            final String normalized = Normalizer.normalize(Character.toString(value), this.form);
            final CharBuffer buffer = CharBuffer.allocate(NORMALIZATION_BUFFER_SIZE);
            normalized.codePoints().forEach(i -> buffer.put(dispatch(i)));
            final char[] result;
            switch (buffer.position()) {
                case 0:
                    result = NOTHING;
                    break;
                case 1:
                    result = ASCII[buffer.get(0)];
                    break;
                default:
                    result = new char[buffer.position()];
                    buffer.position(0);
                    buffer.get(result);
            }
            this.encodings.put(value, result);
            return result;
        }
    }

    /**
     * Converts a single codepoint to its ASCII representation. For ASCII codepoints (below 0x80), returns the
     * corresponding ASCII character. For non-ASCII codepoints, returns an empty character array. This method
     * should be overridden to add custom strategies for handling non-ASCII codepoints.
     *
     * @param codepoint the Unicode codepoint to convert
     * @return char[] containing either the ASCII representation or an empty array
     */
    protected char[] dispatch(final int codepoint) {
        if (codepoint < ASCII_BOUNDARY) {
            return ASCII[codepoint];
        } else {
            return NOTHING;
        }
    }
}