/**
 * ASCII-safe {@link java.nio.charset.Charset} SPI provider that transliterates Unicode text into
 * ASCII subsets.
 *
 * <p>Only the charset API package {@code com.maybeitssquid.safeascii} is exported. The
 * transliteration pipeline in {@code com.maybeitssquid.safeascii.internal} is an implementation
 * detail and is not exported.
 *
 * <p>The charsets are discovered by name through {@link java.nio.charset.Charset}; the provider is
 * registered both here (for the module path) and via {@code META-INF/services} (for the classpath).
 */
module com.maybeitssquid.safeascii {
  exports com.maybeitssquid.safeascii;

  provides java.nio.charset.spi.CharsetProvider with
      com.maybeitssquid.safeascii.TransliteratingASCIIProvider;
}
