# RS Polymorph

[Consult the full CurseForge description](./CURSEFORGE_PAGE.md)

RS Polymorph is a Minecraft mod for **NeoForge and Fabric** that adds [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) compatibility for [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2). When several recipes match the same ingredients, the Polymorph side button lets you pick which one the Crafting Grid crafts and which one the Pattern Grid prints for autocrafting.

## Features

- **Crafting Grid** — Adds the Polymorph recipe selection button to the Refined Storage 2 Crafting Grid
- **Wireless Crafting Grid** — Recipe selection also works in the [Quartz Arsenal](https://www.curseforge.com/minecraft/mc-mods/refined-storage-quartz-arsenal) Wireless Crafting Grid; the choice is driven through the grid's recipe matrix even though it has no block entity (optional compat, no hard dependency)
- **Pattern Grid** — Adds the Polymorph recipe selection button to the Pattern Grid, with the selection persisted on the printed pattern via a custom data component
- **Autocrafting integration** — `MixinPatternResolver` prefers the pattern's stored recipe ID over the default first-match, so autocrafting always resolves the recipe you chose
- **Server-safe** — All client classes are isolated behind `FMLEnvironment.dist.isClient()`; client mixins are split into the `"client"` block of the mixin config, so dedicated servers start cleanly with no missing-class errors
- **Unified SP / MP path** — Recipe selection goes through a single `SelectRecipePacket`, which works via loopback in singleplayer and over the network in multiplayer
- **Bilingual UI** — English and French lang files

## Commands

This mod does not add any commands — it integrates transparently with the Refined Storage 2 UI.

## Requirements

| Dependency | Version |
|------------|---------|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.219+ **or** Fabric Loader 0.16.10+ (with Fabric API) |
| Java | 21 |
| Polymorph | >= 1.1.0 (NeoForge or Fabric build) |
| Refined Storage 2 | >= 2.0.1 (tested against 2.0.8; NeoForge or Fabric build) |
| Refined Storage - Quartz Arsenal | >= 1.0.7 (optional, for the Wireless Crafting Grid) |

Pick the jar matching your loader: `rspolymorph-neoforge-*.jar` or `rspolymorph-fabric-*.jar`. A single combined jar (`rspolymorph-*.jar`) that loads on both loaders is also available as a convenience.

## Installation

1. Install [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) in your `mods/` folder
2. Install [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2) in your `mods/` folder
3. Place the jar matching your loader (`rspolymorph-neoforge-1.2.0.jar` or `rspolymorph-fabric-1.2.0.jar`) in your `mods/` folder
4. (Optional) Install [Refined Storage - Quartz Arsenal](https://www.curseforge.com/minecraft/mc-mods/refined-storage-quartz-arsenal) for Wireless Crafting Grid support
5. Start the game (singleplayer) or server

## Usage

1. Open a Crafting Grid or Pattern Grid with a Refined Storage 2 network
2. Place ingredients that match multiple recipes (e.g. 4 planks → sticks vs. a dye variant)
3. Click the **Polymorph side button** on the left side of the grid
4. Select your preferred recipe from the popup
5. The grid preview (Crafting Grid) or the printed pattern (Pattern Grid) will use your selection

## Documentation

- [CHANGELOG.md](CHANGELOG.md) — Version history and per-version test procedures
- [RULES.md](RULES.md) — Project conventions, architecture, and AI assistant guidelines
- [CONTRIBUTING.md](.github/CONTRIBUTING.md) — Contribution guide
- [SECURITY.md](.github/SECURITY.md) — Security policy

## Credits

Author: vyrriox
Organization: Team Arcadia
License: LGPL-3.0-or-later — see [LICENSE](LICENSE). Forks and derivative works are welcome under the same license, provided you credit "vyrriox / Team Arcadia" and link back to the upstream repository. Same license as upstream Polymorph.
Discord: [discord.gg/xjF8Rtzyd4](https://discord.gg/xjF8Rtzyd4)
Website: [arcadia-echoes-of-power.fr](https://arcadia-echoes-of-power.fr/)

---

# RS Polymorph (Version Française)

[Consulter la description CurseForge complète](./CURSEFORGE_PAGE.md)

RS Polymorph est un mod NeoForge pour Minecraft qui ajoute la compatibilité [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) pour [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2). Lorsque plusieurs recettes correspondent aux mêmes ingrédients, le bouton latéral Polymorph vous laisse choisir laquelle la Crafting Grid fabrique et laquelle la Pattern Grid imprime pour l'autocraft.

## Caractéristiques

- **Crafting Grid** — Ajoute le bouton de sélection de recette Polymorph à la Crafting Grid de Refined Storage 2
- **Wireless Crafting Grid** — La sélection de recette fonctionne aussi dans la Wireless Crafting Grid de [Quartz Arsenal](https://www.curseforge.com/minecraft/mc-mods/refined-storage-quartz-arsenal) ; le choix est appliqué via la matrice de recette de la grille bien qu'elle n'ait aucun block entity (compat optionnelle, sans dépendance forte)
- **Pattern Grid** — Ajoute le bouton de sélection de recette Polymorph à la Pattern Grid, avec la sélection persistée sur le patron imprimé via un data component custom
- **Intégration autocraft** — `MixinPatternResolver` privilégie l'ID de recette stocké sur le patron plutôt que le premier match par défaut, donc l'autocraft résout toujours la recette choisie
- **Sûr côté serveur** — Toutes les classes client sont isolées derrière `FMLEnvironment.dist.isClient()` ; les mixins client sont dans le bloc `"client"` du fichier mixins, donc les serveurs dédiés démarrent proprement sans erreur de classe manquante
- **Chemin SP / MP unifié** — La sélection de recette passe par un unique `SelectRecipePacket`, fonctionnant en boucle locale en solo et sur le réseau en multijoueur
- **Interface bilingue** — Fichiers de langue anglais et français

## Commandes

Ce mod n'ajoute aucune commande — il s'intègre de manière transparente à l'interface Refined Storage 2.

## Prérequis

| Dépendance | Version |
|------------|---------|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.219+ **ou** Fabric Loader 0.16.10+ (avec Fabric API) |
| Java | 21 |
| Polymorph | >= 1.1.0 (build NeoForge ou Fabric) |
| Refined Storage 2 | >= 2.0.1 (testé avec 2.0.8 ; build NeoForge ou Fabric) |
| Refined Storage - Quartz Arsenal | >= 1.0.7 (optionnel, pour la Wireless Crafting Grid) |

Choisissez le jar correspondant à votre loader : `rspolymorph-neoforge-*.jar` ou `rspolymorph-fabric-*.jar`. Un jar combiné unique (`rspolymorph-*.jar`) chargeable sur les deux loaders est aussi disponible par commodité.

## Installation

1. Installez [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) dans votre dossier `mods/`
2. Installez [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2) dans votre dossier `mods/`
3. Placez le jar correspondant à votre loader (`rspolymorph-neoforge-1.2.0.jar` ou `rspolymorph-fabric-1.2.0.jar`) dans votre dossier `mods/`
4. (Optionnel) Installez [Refined Storage - Quartz Arsenal](https://www.curseforge.com/minecraft/mc-mods/refined-storage-quartz-arsenal) pour le support de la Wireless Crafting Grid
5. Démarrez le jeu (solo) ou le serveur

## Utilisation

1. Ouvrez une Crafting Grid ou Pattern Grid avec un réseau Refined Storage 2
2. Placez des ingrédients qui correspondent à plusieurs recettes (ex. 4 planches → bâtons vs. une variante de teinture)
3. Cliquez sur le **bouton latéral Polymorph** sur la gauche de la grille
4. Sélectionnez votre recette préférée dans le popup
5. L'aperçu de la grille (Crafting Grid) ou le patron imprimé (Pattern Grid) utilisera votre sélection

## Documentation

- [CHANGELOG.md](CHANGELOG.md) — Historique des versions et procédures de test
- [RULES.md](RULES.md) — Conventions du projet, architecture et règles pour les assistants IA
- [CONTRIBUTING.md](.github/CONTRIBUTING.md) — Guide de contribution
- [SECURITY.md](.github/SECURITY.md) — Politique de sécurité

## Credits

Auteur : vyrriox
Organisation : Team Arcadia
Licence : LGPL-3.0-or-later — voir [LICENSE](LICENSE). Les forks et travaux dérivés sont les bienvenus sous la même licence, à condition de créditer « vyrriox / Team Arcadia » et de pointer vers le dépôt d'origine. Même licence que Polymorph en amont.
Discord : [discord.gg/xjF8Rtzyd4](https://discord.gg/xjF8Rtzyd4)
Site web : [arcadia-echoes-of-power.fr](https://arcadia-echoes-of-power.fr/)
