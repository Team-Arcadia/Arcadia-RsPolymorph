# Project Rules & AI/IDE Instructions

## 1. Project Identity

| Field | Value |
|-------|-------|
| Project | RS Polymorph |
| Mod ID | `rspolymorph` |
| Package | `com.vyrriox.rspolymorph` |
| Tech Stack | Java 21, NeoForge 1.21.1, Gradle 8.x |
| Author | vyrriox |
| Organization | Team Arcadia |
| License | All Rights Reserved |
| Version | 1.0.8 |
| Dependencies | Polymorph >= 1.1.0, Refined Storage 2 >= 2.0.1 |

## 2. Git Workflow

| Branch | Purpose | Merges into |
|--------|---------|-------------|
| `main` | Stable releases, tagged versions | - |
| `staging` | Pre-release testing & QA | `main` |
| `develop` | Active development, feature integration | `staging` |
| `feat/*` | New features | `develop` |
| `fix/*` | Bug fixes | `develop` |
| `hotfix` | Critical production patches | `main` + `develop` |

**Commit conventions:** `type: descriptive message` (feat, fix, refactor, docs, perf, release)

**Release process:**
1. Bump `mod_version` in `gradle.properties`
2. Move changelog entries into the new `[X.Y.Z]` section
3. Generate `TEST_PROCEDURE_vX.Y.Z.html`
4. Tag `vX.Y.Z` on `main` → triggers `release.yml` workflow

## 3. Code Conventions

- **Language:** Code, variables, comments in English. UI text in EN + FR via lang files.
- **Naming:** PascalCase (classes), camelCase (methods/fields), UPPER_SNAKE (constants)
- **Indentation:** 4 spaces
- **Architecture:**
  - Main `@Mod` class (`RsPolymorph`) must NEVER reference client-only types directly — not even inside lambdas. The JVM verifier resolves types at class-loading time, and client classes don't exist on a dedicated server.
  - All client-only code lives in `com.vyrriox.rspolymorph.client.*` and is reached only via `FMLEnvironment.dist.isClient()`.
  - Mixins that target client classes (Screens, Widgets) are declared in the `"client"` block of `mixins.rspolymorph.json`; common mixins stay in the `"mixins"` block.
  - Polymorph ↔ RS2 bridge: `RsGridRecipeData` persists the user's selection per `RecipeType` in the Polymorph `IBlockEntityRecipeData` capability.
  - `MixinRecipeMatrix` overrides RS2's result post-resolve and MUST sync `currentRecipe` via the accessor, otherwise RS2's `currentRecipe.matches(input)` fast path will revert the preview.
  - Selection packet: `SelectRecipePacket` is the unified client→server path for both SP (local loopback) and MP. Never schedule `matrix.updateResult` with a client-side BlockEntity — always resolve the server BE via `player.containerMenu`.
- **Do NOT:**
  - Import `net.minecraft.client.*` or `com.mojang.blaze3d.*` from any common-side class or mixin
  - Use class-name string matching to detect slot types — use `instanceof` (anonymous inner classes like `PatternGridContainerMenu$5` break `contains("DisabledSlot")`)
  - Tag patterns with recipe IDs read only from the static `selectedRecipeId` — always fall back to `RsGridRecipeData.selections` (the static is cleared between the packet and the `createCraftingPattern` call on dedicated servers)
  - Store raw client-level BlockEntity references on the server thread

## 4. Project Structure

```
arcadia-rspolymorph/
├── .github/
│   ├── CODE_OF_CONDUCT.md, COMMUNICATION.md, CONTRIBUTING.md
│   ├── FUNDING.yml, PULL_REQUEST_TEMPLATE.md, SECURITY.md
│   ├── ISSUE_TEMPLATE/ (bug_report, feature_request, config)
│   └── workflows/ (build.yml, release.yml)
├── libs/
│   ├── polymorph.jar      # Polymorph API (tracked for CI)
│   └── rs2.jar            # Refined Storage 2 (tracked for CI)
├── src/main/java/com/vyrriox/rspolymorph/
│   ├── RsPolymorph.java           # Main @Mod class (server-safe)
│   ├── RsGridRecipeData.java      # Polymorph IBlockEntityRecipeData impl
│   ├── IRsRecipeMatrix.java       # Duck-type interface for RecipeMatrix accessor
│   ├── client/                    # Client-only: ClientSetup, RsGridRecipeWidget, PolymorphSideButton
│   ├── mixin/                     # Mixins (common + client split via mixins.rspolymorph.json)
│   │   ├── MixinRecipeMatrix, MixinCraftingGrid, MixinPatternGrid  # common
│   │   ├── MixinPatternResolver, AccessorRecipeMatrix              # common
│   │   ├── AccessorAbstractGridContainerMenu                       # common
│   │   ├── MixinAbstractBaseScreen, MixinSelectionWidget           # client-only
│   └── network/
│       └── SelectRecipePacket.java  # C2S packet, server handler
├── src/main/resources/
│   ├── mixins.rspolymorph.json
│   ├── assets/rspolymorph/lang/ (en_us.json, fr_fr.json)
│   └── META-INF/ via src/main/templates/META-INF/neoforge.mods.toml
├── build.gradle, gradle.properties, settings.gradle
└── README.md, RULES.md, CHANGELOG.md, LICENSE, TEST_PROCEDURE_vX.Y.Z.html
```

## 5. Adding a New Feature (Step by Step)

1. Create branch `feat/my-feature` from `develop`
2. If the feature touches RS2 internals, decompile the relevant `rs2.jar` class first (`javap -p -c`) to verify field/method signatures before writing the Mixin
3. Implement common logic first (package `com.vyrriox.rspolymorph`)
4. If UI is needed, add under `client/` and register via `ClientSetup.init()`
5. If a new mixin is introduced, add to `mixins.rspolymorph.json` under `"mixins"` (common) or `"client"` (client-only)
6. If a new packet is needed, register under `RsPolymorph.registerPayloads`
7. Add translations to `assets/rspolymorph/lang/{en_us,fr_fr}.json`
8. Run `./gradlew build`
9. Test in singleplayer AND dedicated server
10. Commit and PR into `develop`

## 6. Testing Checklist

- [ ] `./gradlew build` passes with no warnings related to missing types
- [ ] Pattern Grid: recipe selection updates the preview immediately (no need to print an intermediate pattern)
- [ ] Pattern Grid: printed pattern is tagged with the selected recipe ID
- [ ] Autocraft resolves the tagged recipe via `MixinPatternResolver`
- [ ] Crafting Grid: recipe selection produces the chosen output on craft
- [ ] Dedicated server starts cleanly — no `ClassNotFoundException` / `NoClassDefFoundError` mentioning `net.minecraft.client.*`
- [ ] Multiplayer: two players can open separate grids without selection bleed
- [ ] No client-only class referenced from `RsPolymorph`, `RsGridRecipeData`, `SelectRecipePacket`, or common mixins
- [ ] `mixins.rspolymorph.json` correctly separates common vs client mixins

## 7. Environment Setup

```bash
git clone https://github.com/Team-Arcadia/Arcadia-RsPolymorph.git
cd Arcadia-RsPolymorph
# Verify libs/polymorph.jar and libs/rs2.jar exist (tracked in the repo)
./gradlew build
./gradlew runClient
./gradlew runServer
```

## 8. AI Assistant Instructions

1. Never add client imports to common-side code (`RsPolymorph`, `RsGridRecipeData`, `SelectRecipePacket`, `mixin/MixinPatternResolver`, `mixin/MixinPatternGrid`, `mixin/MixinCraftingGrid`, `mixin/MixinRecipeMatrix`, accessors)
2. When overriding RS2's recipe result, ALWAYS update `currentRecipe` via `AccessorRecipeMatrix.rspolymorph$setCurrentRecipe` alongside `invokeSetResult` — otherwise the preview reverts on the next tick
3. For recipe selection, always dispatch `SelectRecipePacket` (works in both SP and MP via loopback)
4. Decompile `rs2.jar` / `polymorph.jar` before speculating about their internal API — field and method signatures change between RS2 versions
5. Keep `createCraftingPattern` tagging dual-source: read the static `selectedRecipeId` first, fall back to `RsGridRecipeData.selections`
6. Use `instanceof` for slot type detection — never `getClass().getName().contains(...)` (fails on anonymous inner classes)
7. Always add EN + FR translations for any new user-facing string
8. Run `./gradlew build` before committing
9. Bump the mod version ONLY when the user explicitly asks for it; default is VERSION LOCK
10. Generate `TEST_PROCEDURE_vX.Y.Z.html` on every version bump
