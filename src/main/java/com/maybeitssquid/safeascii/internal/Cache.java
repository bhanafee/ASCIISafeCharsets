package com.maybeitssquid.safeascii.internal;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntFunction;

/**
 * A {@link Chainable} implementation that caches the results of character processing to improve
 * performance.
 *
 * <p>This class maintains a cache of processed results to avoid re-computing values for the same
 * inputs. It employs a hybrid storage strategy:
 *
 * <ul>
 *   <li>A direct array is used for fast, thread-safe lookup of ASCII characters.
 *   <li>A bounded {@link HashMap} is used for all other codepoints.
 * </ul>
 *
 * This structure ensures minimal overhead for common ASCII characters while limiting memory used by
 * arbitrary non-ASCII input. Once the non-ASCII cache reaches {@link #MAX_NON_ASCII_ENTRIES}, later
 * values are processed but not retained.
 */
public class Cache extends Chainable {
  /** Maximum number of non-ASCII mappings retained by one pipeline. */
  public static final int MAX_NON_ASCII_ENTRIES = 4_096;

  private final AtomicReferenceArray<CharSequence> ascii = new AtomicReferenceArray<>(ASCII);
  private final HashMap<Integer, CharSequence> cache = new HashMap<>();

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
   * @param value the processed string value to associate with the codepoint
   */
  public void cache(final int codepoint, final CharSequence value) {
    Objects.requireNonNull(value, "value");
    if (codepoint >= 0 && codepoint < ASCII) {
      ascii.set(codepoint, value);
    } else {
      synchronized (cache) {
        if (cache.containsKey(codepoint) || cache.size() < MAX_NON_ASCII_ENTRIES) {
          cache.put(codepoint, value);
        }
      }
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
    if (codepoint >= 0 && codepoint < ASCII) {
      return ascii.get(codepoint);
    }
    synchronized (cache) {
      return cache.get(codepoint);
    }
  }

  /**
   * Returns the processed string for a given codepoint, using the cache if available.
   *
   * <p>If the value is found in the cache, it is returned immediately. If not, the request is
   * delegated to the upstream handler, and the result is cached for future use.
   *
   * @param value the codepoint to process
   * @return the processed {@link CharSequence}
   */
  @Override
  public CharSequence apply(final int value) {
    if (value >= 0 && value < ASCII) {
      final CharSequence cached = ascii.get(value);
      if (cached != null) {
        return cached;
      }
      synchronized (ascii) {
        final CharSequence synchronizedCached = ascii.get(value);
        if (synchronizedCached != null) {
          return synchronizedCached;
        }
        final CharSequence result = delegate(value);
        if (result != null) {
          ascii.set(value, result);
        }
        return result;
      }
    }

    synchronized (cache) {
      final CharSequence cached = cache.get(value);
      if (cached != null) {
        return cached;
      }
      final CharSequence result = delegate(value);
      if (result != null && cache.size() < MAX_NON_ASCII_ENTRIES) {
        cache.put(value, result);
      }
      return result;
    }
  }
}
