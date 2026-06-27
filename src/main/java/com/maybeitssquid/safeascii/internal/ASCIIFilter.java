package com.maybeitssquid.safeascii.internal;

import static com.maybeitssquid.safeascii.internal.Chainable.ASCII;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * A filter function that selectively preserves ASCII characters.
 *
 * <p>This class processes Unicode codepoints, retaining them only if they are within the ASCII
 * range (codepoints less than 128) and do not belong to any of the blocked Unicode categories
 * defined at construction (see {@link Character#getType(int)}). Codepoints that do not meet these
 * criteria are transformed into an empty string.
 */
public class ASCIIFilter implements IntFunction<CharSequence> {
  private final Set<Integer> blockTypes;

  /**
   * Create an ASCII filter that blocks characters of the specified Unicode categories.
   *
   * <p>To create an instance that allows all ASCII characters, use:
   *
   * {@snippet :
   *   ASCIIFilter ascii = new ASCIIFilter();
   * }
   *
   * <p>To create an instance that disallows ASCII control characters, use:
   *
   * {@snippet :
   *   ASCIIFilter blockControls = new ASCIIFilter(Character.CONTROL);
   * }
   *
   * @param block Unicode categories to block
   * @see Character#getType(int)
   */
  public ASCIIFilter(final byte... block) {
    if (block == null || block.length == 0) {
      blockTypes = Set.of();
    } else {
      final Set<Integer> tmp = new HashSet<>();
      for (byte b : block) {
        tmp.add((int) b);
      }
      blockTypes = Set.copyOf(tmp);
    }
  }

  /**
   * Processes a Unicode codepoint.
   *
   * @param value the codepoint to process.
   * @return a string containing the character if it is a permitted ASCII character; otherwise, an
   *     empty string.
   */
  @Override
  public CharSequence apply(final int value) {
    return value < ASCII && !blockTypes.contains(Character.getType(value))
        ? Character.toString(value)
        : "";
  }
}
