# Contributing to RS Polymorph / Contribuer

Thank you for your interest in contributing! | Merci de votre interet !

## Prerequisites / Prerequis
- Java 21 (Temurin recommended)
- Gradle 8.x
- NeoForge MDK knowledge
- [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) >= 1.1.0
- [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2) >= 2.0.1

## Setup / Installation

```bash
git clone https://github.com/Team-Arcadia/Arcadia-RsPolymorph.git
cd Arcadia-RsPolymorph
./gradlew build
```

`libs/polymorph.jar` and `libs/rs2.jar` are tracked in the repository — no manual download is required.

## Code Conventions

- **Code, variables, logs**: English only
- **Naming**: PascalCase for classes, camelCase for methods/fields
- **Indentation**: 4 spaces
- Never import `net.minecraft.client.*` from common-side code (main `@Mod`, common mixins, packet handlers, `RsGridRecipeData`). Client code lives under `com.vyrriox.rspolymorph.client`.
- Client-only mixins go in the `"client"` block of `mixins.rspolymorph.json`; common mixins go in the `"mixins"` block.
- Use `instanceof` for slot type detection, not class-name string matching.
- When overriding RS2's `RecipeMatrix` result, always sync `currentRecipe` via the accessor so subsequent `updateResult` calls don't revert the preview.

## Commit Messages

```
feat: add new feature
fix: resolve bug
refactor: restructure code
docs: update documentation
perf: improve performance
release: version bump
```

## Branch Strategy

| Branch | Purpose | Merges into |
|--------|---------|-------------|
| main | Stable releases | - |
| staging | Pre-release testing | main |
| develop | Active development | staging |
| feat/* | New features | develop |
| fix/* | Bug fixes | develop |
| hotfix | Critical patches | main + develop |

## Community / Communaute

- [Discord](https://discord.gg/xjF8Rtzyd4)
- [Website](https://arcadia-echoes-of-power.fr/)
