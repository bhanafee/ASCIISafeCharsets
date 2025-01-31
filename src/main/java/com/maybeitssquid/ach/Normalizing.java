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
 * <p>Example usage:
 * <pre>
 * Normalizing normalizer = new Normalizing();
 * normalizer.encode(0x00E9, 'e');  // Map é to e
 * char[] result = normalizer.apply(0x00E9);
 * </pre>
 *
 * @see java.text.Normalizer
 * @see java.text.Normalizer.Form
 */
public class Normalizing extends Filtering {

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
     * Creates a new Normalizing instance using the default NFKD normalization form.
     */
    public Normalizing() {
        this(Normalizer.Form.NFKD);
    }

    /**
     * Returns the normalization form being used by this instance.
     *
     * @return the current {@link Normalizer.Form}
     */
    @SuppressWarnings("unused")
    public Normalizer.Form getForm() {
        return form;
    }

    /**
     * Maps a Unicode codepoint to a single ASCII character.
     *
     * @param codepoint the Unicode codepoint to encode
     * @param as the ASCII character to map to
     * @return this instance for method chaining
     */
    @Override
    public Normalizing encode(final int codepoint, final char as) {
        if (codepoint <= 0x0080) {
            super.encode(codepoint, as);
        } else if (as <= 0x0080) {
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
     * @param as the array of ASCII characters to map to
     * @return this instance for method chaining
     */
    @Override
    public Normalizing encode(final int codepoint, final char[] as) {
        if (codepoint <= 0x0080) {
            super.encode(codepoint, as);
        } else {
            encodings.put(codepoint, as);
        }
        return this;
    }

    @Override
    public Normalizing encode(final int codepoint, final String as) {
        super.encode(codepoint, as);
        return this;
    }

    /**
     * Blocks a specific Unicode codepoint from being converted.
     *
     * @param codepoint the Unicode codepoint to block
     * @return this instance for method chaining
     */
    @Override
    public Normalizing block(final int codepoint) {
        if (codepoint > 0x0080) {
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
        if (value < 0x80) {
            return ASCII[value];
        } else if (this.encodings.containsKey(value)) {
            return this.encodings.get(value);
        } else {
            final String normalized = Normalizer.normalize(Character.toString(value), this.form);
            final CharBuffer buffer = CharBuffer.allocate(20);
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

    protected char[] dispatch(final int codepoint) {
        if (codepoint < 0x80) {
            return ASCII[codepoint];
        } else {
            return NOTHING;
        }
    }
}
