# Graph Report - .  (2026-08-01)

## Corpus Check
- Corpus is ~15,360 words - fits in a single context window. You may not need a graph.

## Summary
- 320 nodes · 736 edges · 18 communities (17 shown, 1 thin omitted)
- Extraction: 83% EXTRACTED · 17% INFERRED · 0% AMBIGUOUS · INFERRED: 128 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Community 0
- Community 1
- Community 2
- Community 3
- Community 4
- Community 5
- Community 6
- Community 7
- Community 8
- Community 9
- Community 10
- Community 11
- Community 12
- Community 13

## God Nodes (most connected - your core abstractions)
1. `NameTest` - 34 edges
2. `TransliteratingASCIITest` - 30 edges
3. `TransliteratingASCIIProviderTest` - 27 edges
4. `Chainable` - 18 edges
5. `CategorizeTest` - 17 edges
6. `Name` - 16 edges
7. `Decompose` - 15 edges
8. `TransliteratingASCIIProvider` - 13 edges
9. `Cache` - 13 edges
10. `Categorize` - 13 edges

## Surprising Connections (you probably didn't know these)
- `TransliteratingASCIIProvider Class` --references--> `X-Transliterating Charset`  [INFERRED]
  CLAUDE.md → README.md
- `Transliterator Pipeline Architecture` --references--> `SingleCharacterFilter Pipeline Stage`  [INFERRED]
  README.md → CLAUDE.md
- `CI Workflow (gradle.yml)` --references--> `ASCII-safe Charsets Project`  [INFERRED]
  .github/workflows/gradle.yml → README.md
- `GitHub Pages Deployment Workflow (pages.yml)` --references--> `ASCII-safe Charsets Project`  [INFERRED]
  .github/workflows/pages.yml → README.md
- `TransliteratingASCIIProvider Class` --references--> `X-ASCII-Printable Charset`  [INFERRED]
  CLAUDE.md → README.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **** — readme_x_ascii_printable_charset, readme_x_ascii_plain_charset, readme_x_ascii_formatted_charset, readme_x_transliterating_charset, readme_x_transliterating_single_byte_charset [EXTRACTED 1.00]
- **** — claude_cache_stage, claude_decompose_stage, claude_name_stage, claude_asciifilter_stage, claude_singlecharacterfilter_stage [EXTRACTED 1.00]
- **** — github_workflows_gradle_ci, github_workflows_gradle_publish, github_workflows_javadoc, github_workflows_pages [EXTRACTED 1.00]

## Communities (18 total, 1 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.12
Nodes (8): Charset, CharsetDecoder, CharsetEncoder, CoderResult, Override, TransliteratingASCII, Test, TransliteratingASCIITest

### Community 1 - "Community 1"
Cohesion: 0.13
Nodes (6): CsvSource, AbstractChainableTest, ParameterizedTest, Test, ValueSource, NameTest

### Community 2 - "Community 2"
Cohesion: 0.08
Nodes (36): Java Charset Service Provider Interface, ASCIIFilter Pipeline Stage, Cache Pipeline Stage, Categorize Pipeline Stage, Chainable Abstract Base Class, Decompose Pipeline Stage, Gradle-Git-Version Release Automation, Claude Code Project Guidance (+28 more)

### Community 3 - "Community 3"
Cohesion: 0.17
Nodes (6): BeforeEach, Charset, ParameterizedTest, Test, ValueSource, TransliteratingASCIIProviderTest

### Community 4 - "Community 4"
Cohesion: 0.14
Nodes (7): Categorize, Override, CategorizeTest, Override, ParameterizedTest, Test, ValueSource

### Community 5 - "Community 5"
Cohesion: 0.16
Nodes (8): Chainable, Override, ChainableTest, DoublingChainable, Override, Test, UppercaseChainable, Override

### Community 6 - "Community 6"
Cohesion: 0.16
Nodes (8): CharsetProvider, ASCIIFilter, Cache, Override, SingleCharacterFilter, Charset, Override, TransliteratingASCIIProvider

### Community 7 - "Community 7"
Cohesion: 0.17
Nodes (6): Form, Decompose, Override, DecomposeTest, Override, Test

### Community 8 - "Community 8"
Cohesion: 0.23
Nodes (3): Pattern, Override, Name

### Community 9 - "Community 9"
Cohesion: 0.33
Nodes (6): DisplayName, Override, ASCIIFilterTest, ParameterizedTest, Test, ValueSource

### Community 10 - "Community 10"
Cohesion: 0.38
Nodes (3): CacheTest, Override, Test

### Community 11 - "Community 11"
Cohesion: 0.42
Nodes (3): Override, Test, SingleCharacterFilterTest

### Community 12 - "Community 12"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **5 isolated node(s):** `TransliteratingASCII Class`, `Dependabot Configuration`, `Mermaid Diagram Theme Configuration`, `Community Code of Conduct`, `GitHub Packages Publication`
  These have ≤1 connection - possible missing edges or undocumented components.
- **1 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Chainable` connect `Community 5` to `Community 0`, `Community 1`, `Community 4`, `Community 6`, `Community 7`?**
  _High betweenness centrality (0.359) - this node is a cross-community bridge._
- **Why does `Decompose` connect `Community 7` to `Community 5`, `Community 6`?**
  _High betweenness centrality (0.155) - this node is a cross-community bridge._
- **Why does `Cache` connect `Community 6` to `Community 10`, `Community 5`?**
  _High betweenness centrality (0.148) - this node is a cross-community bridge._
- **What connects `TransliteratingASCII Class`, `Dependabot Configuration`, `Mermaid Diagram Theme Configuration` to the rest of the system?**
  _5 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.1202020202020202 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.13124274099883856 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.08095238095238096 - nodes in this community are weakly interconnected._