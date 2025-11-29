package com.maybeitssquid.ach;

import java.util.function.IntFunction;

/**
 * Filters out results that are not single characters.
 */
public class SingleCharacterFilter implements IntFunction<CharSequence> {

    /**
     * The next function in the processing chain.
     */
    private final IntFunction<CharSequence> delegate;

    public SingleCharacterFilter(final IntFunction<CharSequence> delegate) {
        this.delegate = delegate;
    }

    @Override
    public CharSequence apply(final int value) {
        final CharSequence result = delegate.apply(value);
        return result.length() == 1 ? result : "";
    }
}
