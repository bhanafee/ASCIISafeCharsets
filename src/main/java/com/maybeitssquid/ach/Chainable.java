package com.maybeitssquid.ach;

import java.util.function.IntFunction;

/**
 * Abstract base class for creating a chain of character processing functions.
 * <p>
 * This class implements {@link IntFunction} to transform Unicode codepoints into {@link CharSequence}s.
 * Implementations can define specific transformation logic in the {@link #process(int)} method.
 * The default {@link #apply(int)} implementation facilitates chaining by feeding the output of
 * the processing step into a delegate function.
 */
public abstract class Chainable implements IntFunction<CharSequence> {
    /**
     * The upper bound for ASCII codepoints (exclusive).
     */
    public static final int ASCII = 128;

    /**
     * The next function in the processing chain.
     */
    protected final IntFunction<CharSequence> delegate;

    /**
     * Creates a new Chainable instance with the specified delegate.
     *
     * @param delegate the next function to apply in the chain
     */
    protected Chainable(final IntFunction<CharSequence> delegate) {
        this.delegate = delegate;
    }

    /**
     * Processes a single codepoint according to the specific logic of this class.
     *
     * @param codepoint the Unicode codepoint to process
     * @return the transformed character sequence
     */
    protected abstract CharSequence process(final int codepoint);

    /**
     * Invokes the delegate function on the given codepoint.
     *
     * @param codepoint the codepoint to pass to the delegate
     * @return the result from the delegate
     */
    protected final CharSequence delegate(final int codepoint) {
        return delegate.apply(codepoint);
    }

    /**
     * Applies this processing step and then the delegate to the input value.
     * <p>
     * This implementation first transforms the input {@code value} using {@link #process(int)}.
     * The resulting {@link CharSequence} is then decomposed into codepoints, each of which
     * is passed to the {@link #delegate(int)} method. The results are concatenated and returned.
     *
     * @param value the input codepoint
     * @return the final processed character sequence
     */
    @Override
    public CharSequence apply(final int value) {
        final CharSequence result = process(value);
        return switch (result.length()) {
            case 0 -> "";
            case 1 -> delegate(result.charAt(0));
            default -> {
                final StringBuilder builder = new StringBuilder(result.length());
                for (int i = 0; i < result.length(); i++) {
                    if (Character.isLowSurrogate(result.charAt(i))) {
                        continue;
                    }
                    final int nextPoint = Character.codePointAt(result, i);
                    final CharSequence chunk = delegate(nextPoint);
                    builder.append(chunk);
                }
                yield builder.toString();
            }
        };
    }
}
