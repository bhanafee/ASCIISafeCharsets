package com.maybeitssquid.ach;

import org.junit.jupiter.api.Test;

import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the {@link Cache} class and its {@link Cache#apply(int)} method.
 *
 * <p>
 * These tests ensure that the caching mechanism functions as expected, both for ASCII and non-ASCII
 * codepoints, along with verifying correct delegation on cache misses.
 */
public class CacheTest {

    @Test
    public void testApplyCachesAsciiValue() {
        IntFunction<CharSequence> delegate = value -> "Processed:" + value;
        Cache cache = new Cache(delegate);

        int asciiCodepoint = 65; // 'A'
        CharSequence expected = "Processed:65";

        // First call (cache miss, delegate should process)
        CharSequence result = cache.apply(asciiCodepoint);
        assertEquals(expected, result);

        // Second call (cache hit)
        CharSequence cachedResult = cache.apply(asciiCodepoint);
        assertEquals(expected, cachedResult);
    }

    @Test
    public void testApplyCachesNonAsciiValue() {
        IntFunction<CharSequence> delegate = value -> "Processed:" + value;
        Cache cache = new Cache(delegate);

        int nonAsciiCodepoint = 2000;
        CharSequence expected = "Processed:2000";

        // First call (cache miss, delegate should process)
        CharSequence result = cache.apply(nonAsciiCodepoint);
        assertEquals(expected, result);

        // Second call (cache hit)
        CharSequence cachedResult = cache.apply(nonAsciiCodepoint);
        assertEquals(expected, cachedResult);
    }

    @Test
    public void testApplyReturnsExistingCachedAsciiValue() {
        IntFunction<CharSequence> delegate = value -> "Processed:" + value;
        Cache cache = new Cache(delegate);

        int asciiCodepoint = 66; // 'B'
        CharSequence preCachedValue = "PreCached:66";

        // Pre-load the cache
        cache.cache(asciiCodepoint, preCachedValue);

        // Call apply (should return the pre-cached value)
        CharSequence result = cache.apply(asciiCodepoint);
        assertEquals(preCachedValue, result);
    }

    @Test
    public void testApplyReturnsExistingCachedNonAsciiValue() {
        IntFunction<CharSequence> delegate = value -> "Processed:" + value;
        Cache cache = new Cache(delegate);

        int nonAsciiCodepoint = 3000;
        CharSequence preCachedValue = "PreCached:3000";

        // Pre-load the cache
        cache.cache(nonAsciiCodepoint, preCachedValue);

        // Call apply (should return the pre-cached value)
        CharSequence result = cache.apply(nonAsciiCodepoint);
        assertEquals(preCachedValue, result);
    }

    @Test
    public void testApplyHandlesNullFromDelegate() {
        IntFunction<CharSequence> delegate = value -> null; // Simulate delegate returning null
        Cache cache = new Cache(delegate);

        int codepoint = 500;

        // First call (delegate returns null and caches it)
        CharSequence result = cache.apply(codepoint);
        assertNull(result);

        // Second call (cache hit with the null value)
        CharSequence cachedResult = cache.apply(codepoint);
        assertNull(cachedResult);
    }

    @Test
    public void testApplyDoesNotInterfereWithSeparateCodepoints() {
        IntFunction<CharSequence> delegate = value -> "Processed:" + value;
        Cache cache = new Cache(delegate);

        int codepoint1 = 100;
        int codepoint2 = 200;
        CharSequence expected1 = "Processed:100";
        CharSequence expected2 = "Processed:200";

        // First call for codepoint1
        CharSequence result1 = cache.apply(codepoint1);
        assertEquals(expected1, result1);

        // First call for codepoint2
        CharSequence result2 = cache.apply(codepoint2);
        assertEquals(expected2, result2);

        // Second call for each (cache hit)
        assertEquals(expected1, cache.apply(codepoint1));
        assertEquals(expected2, cache.apply(codepoint2));
    }
}