# RS Polymorph

[Consult the full CurseForge description](./CURSEFORGE.md)

RS Polymorph is a NeoForge Minecraft mod that adds [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) compatibility for [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2). When several recipes match the same ingredients, the Polymorph side button lets you pick which one the Crafting Grid crafts and which one the Pattern Grid prints for autocrafting.

## Features

- **Crafting Grid** — Adds the Polymorph recipe selection button to the Refined Storage 2 Crafting Grid
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
| NeoForge | 21.1.219+ |
| Java | 21 |
| Polymorph | >= 1.1.0 |
| Refined Storage 2 | >= 2.0.1 |

## Installation

1. Install [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) in your `mods/` folder
2. Install [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2) in your `mods/` folder
3. Place `rspolymorph-1.0.9.jar` in your `mods/` folder
4. Start the game (singleplayer) or server

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
License: All Rights Reserved
Discord: [discord.gg/xjF8Rtzyd4](https://discord.gg/xjF8Rtzyd4)
Website: [arcadia-echoes-of-power.fr](https://arcadia-echoes-of-power.fr/)

---

# RS Polymorph (Version Française)

[Consulter la description CurseForge complète](./CURSEFORGE.md)

RS Polymorph est un mod NeoForge pour Minecraft qui ajoute la compatibilité [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) pour [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2). Lorsque plusieurs recettes correspondent aux mêmes ingrédients, le bouton latéral Polymorph vous laisse choisir laquelle la Crafting Grid fabrique et laquelle la Pattern Grid imprime pour l'autocraft.

## Caractéristiques

- **Crafting Grid** — Ajoute le bouton de sélection de recette Polymorph à la Crafting Grid de Refined Storage 2
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
| NeoForge | 21.1.219+ |
| Java | 21 |
| Polymorph | >= 1.1.0 |
| Refined Storage 2 | >= 2.0.1 |

## Installation

1. Installez [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph) dans votre dossier `mods/`
2. Installez [Refined Storage 2](https://www.curseforge.com/minecraft/mc-mods/refined-storage-2) dans votre dossier `mods/`
3. Placez `rspolymorph-1.0.9.jar` dans votre dossier `mods/`
4. Démarrez le jeu (solo) ou le serveur

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
Licence : Tous droits réservés
Discord : [discord.gg/xjF8Rtzyd4](https://discord.gg/xjF8Rtzyd4)
Site web : [arcadia-echoes-of-power.fr](https://arcadia-echoes-of-power.fr/)
