# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

A Java library that implements the ACH (Automated Clearing House) character set as a `java.nio.charset.Charset` SPI provider. The library transliterates Unicode to the ACH-safe subset of ASCII (0x20–0x7E, plus optional newlines) rather than simply rejecting non-ASCII input. It is published to GitHub Packages as `com.maybeitssquid:achcharset`.

## Commands

```bash
./gradlew build          # compile, run tests, spotless check
./gradlew test           # tests only
./gradlew spotlessApply  # auto-format Java source (required before commit)
./gradlew javadoc        # generate Javadoc

# Run a single test class
./gradlew test --tests "com.maybeitssquid.ach.CacheTest"
```

Build versions are timestamped (`1.0.0-YYYYMMDDHHMMSS`); this is intentional for snapshot publishing.

## Architecture

The library wires together two subsystems: a `Charset` implementation and a configurable transliteration pipeline.

### Charset layer

- **`TransliteratingASCIIProvider`** — `CharsetProvider` SPI entry point, registered via `src/main/resources/META-INF/services/java.nio.charset.spi.CharsetProvider`. Provides four charsets lazily:
  - `ACH` / `X-ACH` — strict ACH (0x20–0x7E only, controls blocked)
  - `X-ACH-Newlines` — same but allows LF and CR
  - `X-Transliterating` — aggressive Unicode-to-ASCII transliteration
  - `X-Transliterating-Single-Byte` — same but guarantees 1:1 character output
- **`TransliteratingASCII`** — extends `java.nio.charset.Charset`. Takes an `IntFunction<CharSequence>` transliterator at construction; the encoder/decoder delegate all codepoint mapping to it.

### Transliterator pipeline

Each step implements `IntFunction<CharSequence>` and chains to the next. Processing flows right-to-left through the chain:

```
Cache → Decompose → Name → Categorize → ASCIIFilter
```

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

Transitive dependency CVEs are pinned in `gradle/libs.versions.toml` as `patch-*` library entries collected in the `security-patches` bundle. `build.gradle` applies them as `implementation` constraints. `settings.gradle` also loads them into the buildscript classpath via regex. New CVE patches follow the same `patch-cve-XXXX-NNNN` naming convention.

The OWASP dependency check plugin (`./gradlew dependencyCheckAnalyze`) fails the build at CVSS ≥ 7.
