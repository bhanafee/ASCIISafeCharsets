# Codebase Guidance

This file documents key information about the project architecture, build commands, code style, and security practices.

## Project overview

A Java library providing ASCII-safe `Charset` SPI implementations that transliterate Unicode to
ASCII subsets rather than simply rejecting non-ASCII input. Published to GitHub Packages as
`com.maybeitssquid:ascii-safe-charsets`.

## Commands

```bash
./gradlew build                   # compile, test, spotless check
./gradlew test                    # run tests
./gradlew test --tests "..."      # run a single test class
./gradlew spotlessApply           # auto-format (required before commit)
./gradlew javadoc                 # generate Javadoc
./gradlew dependencyCheckAnalyze  # OWASP vulnerability scan (slow; fails at CVSS ≥ 7)
```

Build uses Java 25 toolchain, compiles to Java 17 bytecode (`release = "17"`). CI tests on Java 17, 21, and 25.

## Versioning and Releases

Versions are derived from git tags using [`gradle-git-version`](https://github.com/palantir/gradle-git-version):

- **On a tag** (e.g., `v1.0.0`) → version = `1.0.0`
- **After a tag** → version = tag + distance + commit hash (e.g., `1.0.1-3-gABC1234` = 3 commits after v1.0.0)
- **No tags yet** → version synthesized from git history (e.g., `0.0.1-dev-88-gXYZ`)

**To create a release:**

```bash
# Ensure all commits are pushed
git push origin main

# Create and push the tag (triggers automatic version picking in build)
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0

# Build and publish
./gradlew clean build publish
```

**To delete a release tag:**

```bash
git tag -d v1.0.0              # Delete locally
git push origin :v1.0.0        # Delete from remote
```

Configuration cache is disabled (`org.gradle.configuration-cache=false`) to allow git invocation during the build.

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
  - `X-ASCII-Printable` (alias `ASCII-Printable`) — strict printable ASCII (0x20–0x7E only, controls blocked)
  - `X-ASCII-Plain` (alias `ASCII-Plain`) — same but allows LF; CR is unmappable so CRLF normalises to LF under `IGNORE`
  - `X-ASCII-Formatted` (alias `ASCII-Formatted`) — same as X-ASCII-Plain but also allows TAB (0x09)
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

For CVE patch management, see the `gradle-security-patch` skill. Use `/gradle-security-patch` to pin a CVE fix in the version catalog.
