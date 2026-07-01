package com.maybeitssquid.safeascii;

import com.maybeitssquid.safeascii.internal.ASCIIFilter;
import com.maybeitssquid.safeascii.internal.Cache;
import com.maybeitssquid.safeascii.internal.Decompose;
import com.maybeitssquid.safeascii.internal.Name;
import com.maybeitssquid.safeascii.internal.SingleCharacterFilter;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;

/**
 * A {@link CharsetProvider} that supplies ASCII-safe character sets for encoding Unicode text. This
 * provider offers five distinct character set implementations:
 *
 * <dl>
 *   <dt>ASCII-Printable
 *   <dd>Strict printable ASCII: allows 0x20 through 0x7E inclusive. Control characters, including
 *       tabs and newlines, are reported as unmappable.
 *   <dt>ASCII-Plain
 *   <dd>Printable ASCII with newline support: linefeed (0x0A) passes through; carriage return
 *       (0x0D) is unmappable so CRLF normalises to LF under the {@code IGNORE} error action. All
 *       other control characters are unmappable.
 *   <dt>ASCII-Formatted
 *   <dd>Printable ASCII with tab and newline support: tab (0x09) and linefeed (0x0A) pass through;
 *       carriage return (0x0D) is unmappable so CRLF normalises to LF under the {@code IGNORE}
 *       error action. All other control characters are unmappable.
 *   <dt>X-Transliterating
 *   <dd>Aggressive Unicode-to-ASCII transliteration using NFKD decomposition and character-name
 *       lookup. Output length may vary (one Unicode character may produce multiple ASCII bytes).
 *   <dt>X-Transliterating-Single-Byte (alias: ACH)
 *   <dd>Same transliteration as X-Transliterating but guarantees 1:1 character output. Any
 *       transliteration that would produce more or fewer than one character is rejected as
 *       unmappable.
 * </dl>
 */
public class TransliteratingASCIIProvider extends CharsetProvider {

  /** Canonical name for the strict printable ASCII character set. */
  public static final String ASCII_PRINTABLE_CHARSET = "ASCII-Printable";

  /** Canonical name for the printable ASCII character set with newline support. */
  public static final String ASCII_PLAIN_CHARSET = "ASCII-Plain";

  /** Canonical name for the printable ASCII character set with tab and newline support. */
  public static final String ASCII_FORMATTED_CHARSET = "ASCII-Formatted";

  /** Canonical name for the transliterating character set. */
  public static final String TRANSLITERATING_CHARSET = "X-Transliterating";

  /** Canonical name for the single-byte transliterating character set. */
  public static final String TRANSLITERATING_SINGLE_BYTE_CHARSET = "X-Transliterating-Single-Byte";

  /** ACH alias for the single-byte transliterating character set. */
  public static final String ACH_ALIAS = "ACH";

  private Charset asciiPrintable;

  private Charset asciiPlain;

  private Charset asciiFormatted;

  private Charset transliterating;

  private Charset transliteratingSingleByte;

  private List<Charset> charsets;

  /**
   * Creates a new TransliteratingASCIIProvider instance.
   *
   * <p>This provider will lazily initialize the available character sets upon the first request.
   */
  public TransliteratingASCIIProvider() {
    // Default constructor
  }

  private Charset getAsciiPrintable() {
    if (asciiPrintable == null) {
      final ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);
      final Cache transliterator = new Cache(filter);
      asciiPrintable = new TransliteratingASCII(transliterator, ASCII_PRINTABLE_CHARSET);
    }
    return asciiPrintable;
  }

  private Charset getAsciiPlain() {
    if (asciiPlain == null) {
      final ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);
      final Cache cache = new Cache(filter);
      cache.cache(0x0A, "\n");
      cache.cache(0x0D, ""); // CR is unmappable; CRLF normalises to LF under IGNORE
      asciiPlain = new TransliteratingASCII(cache, ASCII_PLAIN_CHARSET);
    }
    return asciiPlain;
  }

  private Charset getAsciiFormatted() {
    if (asciiFormatted == null) {
      final ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);
      final Cache cache = new Cache(filter);
      cache.cache(0x09, "\t");
      cache.cache(0x0A, "\n");
      cache.cache(0x0D, ""); // CR is unmappable; CRLF normalises to LF under IGNORE
      asciiFormatted = new TransliteratingASCII(cache, ASCII_FORMATTED_CHARSET);
    }
    return asciiFormatted;
  }

  private Charset getTransliterating() {
    if (transliterating == null) {
      final ASCIIFilter filter = new ASCIIFilter();
      final IntFunction<CharSequence> transliterator = new Decompose(new Name(filter));
      final Cache cache = new Cache(transliterator);
      transliterating = new TransliteratingASCII(cache, TRANSLITERATING_CHARSET);
    }
    return transliterating;
  }

  private Charset getTransliteratingSingleByte() {
    if (transliteratingSingleByte == null) {
      final ASCIIFilter filter = new ASCIIFilter();
      final IntFunction<CharSequence> transliterator = new Decompose(new Name(filter));
      final SingleCharacterFilter lengthPreserving = new SingleCharacterFilter(transliterator);
      final Cache cache = new Cache(lengthPreserving);
      transliteratingSingleByte =
          new TransliteratingASCII(cache, TRANSLITERATING_SINGLE_BYTE_CHARSET, ACH_ALIAS);
    }
    return transliteratingSingleByte;
  }

  /**
   * Returns an iterator over all available character sets provided by this class. The available
   * charsets are: ASCII-Printable, ASCII-Plain, ASCII-Formatted, X-Transliterating, and
   * X-Transliterating-Single-Byte.
   *
   * @return Iterator containing all supported character sets {@code @ThreadSafe} This method is
   *     thread-safe and uses lazy initialization
   */
  @Override
  public Iterator<Charset> charsets() {
    synchronized (this) {
      if (charsets == null) {
        charsets =
            Arrays.asList(
                getAsciiPrintable(),
                getAsciiPlain(),
                getAsciiFormatted(),
                getTransliterating(),
                getTransliteratingSingleByte());
      }
    }
    return charsets.iterator();
  }

  /**
   * Retrieves a specific character set by name. Supported charset names are:
   *
   * <ul>
   *   <li>{@link #ASCII_PRINTABLE_CHARSET ASCII-Printable}
   *   <li>{@link #ASCII_PLAIN_CHARSET ASCII-Plain}
   *   <li>{@link #ASCII_FORMATTED_CHARSET ASCII-Formatted}
   *   <li>{@link #TRANSLITERATING_CHARSET X-Transliterating}
   *   <li>{@link #TRANSLITERATING_SINGLE_BYTE_CHARSET X-Transliterating-Single-Byte} (alias: {@link
   *       #ACH_ALIAS ACH})
   * </ul>
   *
   * @param charsetName the name of the requested charset
   * @return the corresponding Charset object, or null if the requested charset is not supported
   *     {@code @ThreadSafe} This method is thread-safe
   */
  @Override
  public Charset charsetForName(String charsetName) {
    return switch (charsetName) {
      case ASCII_PRINTABLE_CHARSET -> getAsciiPrintable();
      case ASCII_PLAIN_CHARSET -> getAsciiPlain();
      case ASCII_FORMATTED_CHARSET -> getAsciiFormatted();
      case TRANSLITERATING_CHARSET -> getTransliterating();
      case TRANSLITERATING_SINGLE_BYTE_CHARSET, ACH_ALIAS -> getTransliteratingSingleByte();
      default -> null;
    };
  }
}
