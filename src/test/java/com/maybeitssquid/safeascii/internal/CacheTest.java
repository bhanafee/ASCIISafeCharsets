package com.maybeitssquid.safeascii.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Cache} class and its {@link Cache#apply(int)} method.
 *
 * <p>These tests ensure that the caching mechanism functions as expected, both for ASCII and
 * non-ASCII codepoints, along with verifying correct delegation on cache misses.
 */
class CacheTest extends AbstractChainableTest {

  @Override
  protected Cache createProcessor() {
    IntFunction<CharSequence> delegate = value -> "Processed:" + value;
    return new Cache(delegate);
  }

  @Test
  void applyCachesAsciiValue() {
    Cache cache = createProcessor();
    final int asciiCodepoint = 65;

    // First call (cache miss, delegate should process)
    test(
        cache,
        asciiCodepoint,
        "Processed:65",
        "Expected ASCII codepoint '65' to be processed and cached.");
    // Second call (cache hit)
    test(
        cache,
        asciiCodepoint,
        "Processed:65",
        "Expected ASCII codepoint '65' to be returned from cache.");
  }

  @Test
  void applyCachesNonAsciiValue() {
    Cache cache = createProcessor();
    final int nonAsciiCodepoint = 2000;

    // First call (cache miss, delegate should process)
    test(
        cache,
        nonAsciiCodepoint,
        "Processed:2000",
        "Expected non-ASCII codepoint '2000' to be processed and cached.");
    // Second call (cache hit)
    test(
        cache,
        nonAsciiCodepoint,
        "Processed:2000",
        "Expected non-ASCII codepoint '2000' to be processed and cached.");
  }

  @Test
  void applyReturnsExistingCachedAsciiValue() {
    Cache cache = createProcessor();
    int asciiCodepoint = 66; // 'B'
    CharSequence preCachedValue = "PreCached:66";
    // Pre-load the cache
    cache.cache(asciiCodepoint, preCachedValue);

    // Call apply (should return the pre-cached value)
    test(
        cache,
        asciiCodepoint,
        preCachedValue,
        "Expected pre-cached string 'PreCached66' to be returned.");
  }

  @Test
  void applyReturnsExistingCachedNonAsciiValue() {
    Cache cache = createProcessor();
    int nonAsciiCodepoint = 3000;
    CharSequence preCachedValue = "PreCached:3000";
    // Pre-load the cache
    cache.cache(nonAsciiCodepoint, preCachedValue);

    // Call apply (should return the pre-cached value)
    test(
        cache,
        nonAsciiCodepoint,
        preCachedValue,
        "Expected pre-cached string 'PreCached:3000' to be returned.");
  }

  @Test
  void applyHandlesNullFromDelegate() {
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
  void applyCachesEmptyMappings() {
    AtomicInteger calls = new AtomicInteger();
    Cache cache =
        new Cache(
            value -> {
              calls.incrementAndGet();
              return "";
            });

    assertEquals("", cache.apply(0x00E6));
    assertEquals("", cache.apply(0x00E6));
    assertEquals(1, calls.get());
  }

  @Test
  void nonAsciiCacheIsBounded() {
    AtomicInteger calls = new AtomicInteger();
    Cache cache = new Cache(value -> Integer.toString(calls.incrementAndGet()));

    for (int value = 0; value < Cache.MAX_NON_ASCII_ENTRIES; value++) {
      cache.apply(0x0100 + value);
    }
    CharSequence firstUncached = cache.apply(0x0100 + Cache.MAX_NON_ASCII_ENTRIES);
    CharSequence secondUncached = cache.apply(0x0100 + Cache.MAX_NON_ASCII_ENTRIES);

    assertEquals("4097", firstUncached);
    assertEquals("4098", secondUncached);
  }

  @Test
  void applyDoesNotInterfereWithSeparateCodepoints() {
    Cache cache = createProcessor();

    int codepoint1 = 100;
    int codepoint2 = 200;
    CharSequence expected1 = "Processed:100";
    CharSequence expected2 = "Processed:200";

    // First call for each (cache misses)
    test(cache, codepoint1, expected1, "Expected first codepoint to be processed and cached.");
    test(cache, codepoint2, expected2, "Expected second codepoint to be processed and cached.");

    // Second call for each (cache hit)
    test(cache, codepoint1, expected1, "Expected first codepoint to be returned from cache.");
    test(cache, codepoint2, expected2, "Expected second codepoint to be returned from cache.");
  }
}
