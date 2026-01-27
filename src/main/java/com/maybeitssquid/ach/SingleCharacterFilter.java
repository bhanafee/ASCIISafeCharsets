package com.maybeitssquid.ach;

import java.util.function.IntFunction;

/** Filters out results that are not single characters. */
public class SingleCharacterFilter implements IntFunction<CharSequence> {

  /** The next function in the processing chain. */
  private final IntFunction<CharSequence> delegate;

  /**
   * Creates a new SingleCharacterFilter with the specified delegate.
   *
   * @param delegate the function to apply before filtering
   */
  public SingleCharacterFilter(final IntFunction<CharSequence> delegate) {
    this.delegate = delegate;
  }

  /**
   * Applies the delegate function and returns the result only if it is a single character.
   *
   * @param value the Unicode codepoint to process
   * @return the delegate's result if it is exactly one character, otherwise an empty string
   */
  @Override
  public CharSequence apply(final int value) {
    final CharSequence result = delegate.apply(value);
    return result != null && (result.length() == 1) ? result : "";
  }
}
