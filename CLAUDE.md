# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

A Java library providing ASCII-safe `Charset` SPI implementations that transliterate Unicode to
ASCII subsets rather than simply rejecting non-ASCII input. Published to GitHub Packages as
`com.maybeitssquid:ascii-safe-charsets`.

## Commands

```bash
./gradlew build          # compile, run tests, spotless check
./gradlew test           # tests only
./gradlew spotlessApply  # auto-format Java source (required before commit)
./gradlew javadoc        # generate Javadoc
./gradlew dependencyCheckAnalyze  # OWASP CVE scan (slow; fails build at CVSS >= 7)

# Run a single test class
./gradlew test --tests "com.maybeitssquid.safeascii.CacheTest"
```

On Windows, use `gradlew.bat` (or `.\gradlew` in PowerShell).

The build uses a Java 25 toolchain and compiles to Java 17 bytecode (`release = "17"`). CI tests on Java 17, 21, and 25 on every push/PR to `main`.

Build versions are timestamped (`1.0.0-YYYYMMDDHHMMSS`); this is intentional for snapshot publishing.

## Architecture

The library wires together two subsystems: a `Charset` implementation and a configurable transliteration pipeline.

### Module and package layout

The library is a named JPMS module (`module-info.java`, module `com.maybeitssquid.safeascii`).
Only the charset API package `com.maybeitssquid.safeascii` is exported. The transliteration
pipeline lives in `com.maybeitssquid.safeascii.internal`, which is **not** exported — those classes
are implementation details, not public API. The provider is declared two ways so it works on both
paths: `provides java.nio.charset.spi.CharsetProvider with …` in `module-info.java` (module path)
and `META-INF/services/java.nio.charset.spi.CharsetProvider` (classpath).

When working on the pipeline, keep new internal classes in `…internal`; only add to the exported
package if it is genuinely part of the public charset API. Pipeline unit tests live in
`src/test/java/com/maybeitssquid/safeascii/internal` so they retain same-package/protected access.

### Charset layer

- **`TransliteratingASCIIProvider`** — `CharsetProvider` SPI entry point in the exported package, registered via `src/main/resources/META-INF/services/java.nio.charset.spi.CharsetProvider` (classpath) and the `provides` directive in `module-info.java` (module path). Provides four charsets lazily:
  - `ASCII-Printable` — strict printable ASCII (0x20–0x7E only, controls blocked)
  - `ASCII-Plain` — same but allows LF and normalises CRLF to LF
  - `X-Transliterating` — aggressive Unicode-to-ASCII transliteration
  - `X-Transliterating-Single-Byte` (alias `ACH`) — same but guarantees 1:1 character output
- **`TransliteratingASCII`** — extends `java.nio.charset.Charset`. Takes an `IntFunction<CharSequence>` transliterator at construction; the encoder/decoder delegate all codepoint mapping to it.

### Transliterator pipeline

Each step implements `IntFunction<CharSequence>` and chains to the next. All pipeline classes below
live in the non-exported `com.maybeitssquid.safeascii.internal` package. The actual pipelines
assembled by the provider are:

- **ASCII-Printable / ASCII-Plain**: `Cache → ASCIIFilter`
- **X-Transliterating**: `Cache → Decompose → Name → ASCIIFilter`
- **X-Transliterating-Single-Byte**: `Cache → SingleCharacterFilter → Decompose → Name → ASCIIFilter`

- **`ASCIIFilter`** — terminal step; passes ASCII codepoints not in the blocked Unicode categories, rejects everything else with `""`.
- **`Categorize`** — maps Unicode categories (digits, spaces, dashes, brackets, quotes, etc.) to ASCII equivalents; passes ASCII straight to delegate.
- **`Name`** — extends `Categorize`; uses `Character.getName()` to match LATIN LETTERs, brackets, quotation marks, punctuation by name keyword.
- **`Decompose`** — extends `Chainable`; applies NFKD (or NFD) normalization before further processing; skips codepoints below U+00A0 as an optimization.
- **`Cache`** — extends `Chainable`; caches results in a `CharSequence[128]` array for ASCII and a `HashMap` for the rest; supports manual pre-population via `cache(int, CharSequence)`.
- **`Chainable`** — abstract base; holds the `delegate`, implements `apply()` which calls `process()` then fans out the result's codepoints through the delegate chain.
- **`SingleCharacterFilter`** — wraps another transliterator; returns `""` for any input that produces a result length ≠ 1, ensuring length-preserving (fixed-width) output.

## Code style

Spotless enforces Google Java Format. Run `./gradlew spotlessApply` before committing. The formatter excludes `module-info.java`.

## Security patches

Transitive dependency CVEs are pinned in `gradle/libs.versions.toml` as `patch-*` library entries collected in the `security-patches` bundle. `build.gradle` applies them as `implementation` constraints. `settings.gradle` also loads them into the buildscript classpath via regex. New CVE patches follow the same `patch-cve-XXXX-NNNNN` naming convention.

The OWASP dependency check plugin (`./gradlew dependencyCheckAnalyze`) fails the build at CVSS ≥ 7.
