# Codebase Guidance

This file documents key information about the project architecture, build commands, code style, and security practices.

## Project overview

A Java library providing ASCII-safe `Charset` SPI implementations that transliterate Unicode to
ASCII subsets rather than simply rejecting non-ASCII input. Published to GitHub Packages as
`com.maybeitssquid:ascii-safe-charsets`.

## Commands

**Build and test:**
```bash
./gradlew build              # compile, test, spotless check
./gradlew test               # run all tests
./gradlew test --tests "*CacheTest"           # run tests by class name
./gradlew test --tests "*CacheTest.test*"     # run tests by method pattern
```

**Code quality:**
```bash
./gradlew spotlessApply           # auto-format (required before commit)
./gradlew dependencyCheckAnalyze  # OWASP vulnerability scan (slow; fails at CVSS ≥ 7)
```

**External dependencies:** Standalone library; no runtime dependencies beyond Java.

Build uses Java 25 toolchain, compiles to Java 17 bytecode (`release = "17"`). CI tests on Java 17, 21, and 25.

## Key Entry Points

- **`TransliteratingASCIIProvider`** — main `CharsetProvider` SPI entry point; provides charset instances
- **`Charset.forName("X-Transliterating")`** — retrieve a charset by name (registered via SPI)

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

### Charset layer

- **`TransliteratingASCIIProvider`** — `CharsetProvider` SPI entry point in the exported package, registered via `src/main/resources/META-INF/services/java.nio.charset.spi.CharsetProvider` (classpath) and the `provides` directive in `module-info.java` (module path). Provides four charsets lazily:
  - `X-ASCII-Printable` (alias `ASCII-Printable`) — strict printable ASCII (0x20–0x7E only, controls blocked)
  - `X-ASCII-Plain` (alias `ASCII-Plain`) — same but allows LF; CR is unmappable so CRLF normalises to LF under `IGNORE`
  - `X-ASCII-Formatted` (alias `ASCII-Formatted`) — same as X-ASCII-Plain but also allows TAB (0x09)
  - `X-Transliterating` — aggressive Unicode-to-ASCII transliteration
  - `X-Transliterating-Single-Byte` (alias `ACH`) — same but guarantees 1:1 character output
- **`TransliteratingASCII`** — extends `java.nio.charset.Charset`. Takes an `IntFunction<CharSequence>` transliterator at construction; the encoder/decoder delegate all codepoint mapping to it.

### Transliterator pipeline

Pipeline classes live in the non-exported `com.maybeitssquid.safeascii.internal` package:

- **ASCII-Printable / ASCII-Plain**: `Cache → ASCIIFilter`
- **X-Transliterating**: `Cache → Decompose → Name → ASCIIFilter`
- **X-Transliterating-Single-Byte**: `Cache → SingleCharacterFilter → Decompose → Name → ASCIIFilter`

## Code style

Spotless enforces Google Java Format. Run `./gradlew spotlessApply` before committing. The formatter excludes `module-info.java`.

## Security patches

For CVE patch management, see the `gradle-security-patch` skill. Use `/gradle-security-patch` to pin a CVE fix in the version catalog.
