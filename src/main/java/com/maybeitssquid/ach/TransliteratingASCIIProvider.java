package com.maybeitssquid.ach;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;

/**
 * A {@link CharsetProvider} that supplies specialized ASCII character sets for ACH (Automated Clearing House) file
 * processing. This provider offers four distinct character set implementations:
 *
 * <dl>
 *     <dt>ACH, X-ACH</dt>
 *     <dd>A strict interpretation of characters valid for ACH files. Allows for 0x20 through 0x7E, inclusive.
 *     Control characters, including newlines, are reported as unmappable.</dd>
 *     <dt>X-ACH-Newlines</dt>
 *     <dd>Extended ASCII character set that includes linefeed (0x0A) and carriage return (0x0D) characters,
 *      while maintaining the base ACH character restrictions.</dd>
 *     <dt>X-Transliterating</dt>
 *     <dd>Standard US-ASCII decoder with aggressive character transliteration for decoding operations.</dd>
 *     <dt>X-Transliterating-Single-Byte</dt>
 *     <dd>Standard US-ASCII decoder with character transliteration for decoding operations that guarantees single-byte
 *     output per character.</dd>
 * </dl>
 */
public class TransliteratingASCIIProvider extends CharsetProvider {

    public static final String ACH_CHARSET = "X-ACH";
    public static final String ACH_CHARSET_ALIAS = "ACH";
    public static final String ACH_NEWLINES_CHARSET = "X-ACH-Newlines";
    public static final String TRANSLITERATING_CHARSET = "X-Transliterating";
    public static final String TRANSLITERATING_SINGLE_BYTE_CHARSET = "X-Transliterating-Single-Byte";

    private Charset ach;

    private Charset achNewlines;

    private Charset transliterating;

    private Charset transliteratingSingleByte;

    private List<Charset> charsets;

    /**
     * Creates a new TransliteratingASCIIProvider instance.
     * <p>
     * This provider will lazily initialize the available character sets
     * upon the first request.
     */
    public TransliteratingASCIIProvider() {
        // Default constructor
    }

    private Charset getAch() {
        if (ach == null) {
            final ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);
            final Cache transliterator = new Cache(filter);
            ach = new TransliteratingASCII(transliterator,ACH_CHARSET, ACH_CHARSET_ALIAS);
        }
        return ach;
    }

    private Charset getAchNewlines() {
        if (achNewlines == null) {
            final ASCIIFilter filter = new ASCIIFilter(Character.CONTROL);
            final Cache cache = new Cache(filter);
            cache.cache(0x0A, "\n");
            cache.cache(0x0D, "\r");
            achNewlines = new TransliteratingASCII(cache, ACH_NEWLINES_CHARSET);
        }
        return achNewlines;
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
            transliteratingSingleByte = new TransliteratingASCII(cache, TRANSLITERATING_SINGLE_BYTE_CHARSET);
        }
        return transliteratingSingleByte;
    }

    /**
     * Returns an iterator over all available character sets provided by this class.
     * The available charsets are: X-ACH (ACH), X-ACH-Newlines, X-Transliterating,
     * and X-Transliterating-Single-Byte.
     *
     * @return Iterator containing all supported character sets
     * {@code @ThreadSafe} This method is thread-safe and uses lazy initialization
     */
    @Override
    public Iterator<Charset> charsets() {
        synchronized (this) {
            if (charsets == null) {
                charsets = Arrays.asList(getAch(), getAchNewlines(), getTransliterating(), getTransliteratingSingleByte());
            }
        }
        return charsets.iterator();
    }

    /**
     * Retrieves a specific character set by name. Supported charset names are:
     * <ul>
     *     <li>{@link #ACH_CHARSET X-ACH, ACH}</li>
     *     <li>{@link #ACH_NEWLINES_CHARSET X-ACH-Newlines}</li>
     *     <li>{@link #TRANSLITERATING_CHARSET X-Transliterating}</li>
     *     <li>{@link #TRANSLITERATING_SINGLE_BYTE_CHARSET X-Transliterating-Single-Byte}</li>
     * </ul>
     *
     * @param charsetName the name of the requested charset
     * @return the corresponding Charset object, or null if the requested charset is not supported
     * {@code @ThreadSafe} This method is thread-safe
     */
    @Override
    public Charset charsetForName(String charsetName) {
        return switch (charsetName) {
            case ACH_CHARSET, "ACH" -> getAch();
            case ACH_NEWLINES_CHARSET -> getAchNewlines();
            case TRANSLITERATING_CHARSET -> getTransliterating();
            case TRANSLITERATING_SINGLE_BYTE_CHARSET -> getTransliteratingSingleByte();
            default -> null;
        };
    }
}
