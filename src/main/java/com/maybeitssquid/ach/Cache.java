package com.maybeitssquid.ach;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * A {@link Chainable} implementation that caches the results of character processing to improve performance.
 * <p>
 * This class maintains a cache of processed results to avoid re-computing values for the same inputs.
 * It employs a hybrid storage strategy:
 * <ul>
 *     <li>A direct array is used for fast lookup of ASCII characters.</li>
 *     <li>A {@link HashMap} is used for all other Unicode codepoints.</li>
 * </ul>
 * This structure ensures minimal overhead for common ASCII characters while supporting the full Unicode range.
 */
public class Cache extends Chainable {
    private final CharSequence[] ascii = new CharSequence[ASCII];
    private final Map<Integer, CharSequence> cache = new HashMap<>();

    /**
     * Creates a new Cache instance.
     *
     * @param delegate the next function in the chain to compute values for cache misses
     */
    public Cache(final IntFunction<CharSequence> delegate) {
        super(delegate);
    }

    /**
     * Manually adds a value to the cache for a specific codepoint.
     *
     * @param codepoint the Unicode codepoint to cache
     * @param value     the processed string value to associate with the codepoint
     */
    public void cache(final int codepoint, final CharSequence value) {
        if (codepoint < ASCII) {
            ascii[codepoint] = value;
        } else {
            cache.put(codepoint, value);
        }
    }

    /**
     * Retrieves a cached value for the given codepoint.
     *
     * @param codepoint the character to look up
     * @return the cached {@link CharSequence} if available, otherwise {@code null}
     */
    @Override
    protected CharSequence process(final int codepoint) {
        return codepoint < ASCII ? ascii[codepoint] : cache.get(codepoint);
    }

    /**
     * Returns the processed string for a given codepoint, using the cache if available.
     * <p>
     * If the value is found in the cache, it is returned immediately. If not, the request
     * is delegated to the upstream handler, and the result is cached for future use.
     *
     * @param value the codepoint to process
     * @return the processed {@link CharSequence}
     */
    @Override
    public CharSequence apply(final int value) {
        final CharSequence cached = process(value);
        if (cached == null) {
            final CharSequence result = delegate(value);
            cache(value, result);
            return result;
        } else {
            return cached;
        }
    }
}
