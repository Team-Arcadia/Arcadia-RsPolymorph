# Changelog

## [1.1.0] - 2026-03-22
### Fixed
- **EN:** Robust Polymorph button integration. Fixed the "pink square" texture issue by using direct blit rendering and resolved the unresponsive click by implementing a reliable widget tracking system.
- **FR:** Intégration robuste du bouton Polymorph. Correction du problème de texture "carré rose" via un rendu direct (blit) et résolution du clic inactif par la mise en œuvre d'un système de suivi fiable du widget.

## [1.0.0] - 2026-03-20

### Added
- Initial release for Minecraft 1.21.1 (NeoForge 21.1.219+).
- Polymorph compatibility for Refined Storage 2 Crafting Grid.
- Polymorph compatibility for Refined Storage 2 Pattern Grid.
- Fixed: Synchronized recipe selection and preview for all grids.
- Fixed: Multi-matrix support for Crafting and Smithing in Pattern Grids.
- Fixed: Correctly attaching selection UI to active crafting result slots.
- Improved: Force Polymorph update when RS2 recalculates results, ensuring buttons appear.
- Fixed [Critical]: Resolved severe item duplication and ghost item bug caused by non-empty recipe evaluation on empty matrices.
- Fixed [UI]: Polymorph selector button now correctly attaches to RS2 Crafting Grids by removing overly strict slot container checks.
- Fixed [Logic]: Correctly mapping RecipeMatrixContainer to BlockEntity for proper Polymorph recipe evaluation and preventing duplication on empty matrices.

---

# Journal des modifications

## [1.1.0] - 2026-03-22
### Corrigé
- Intégration finale et robuste du bouton Polymorph.
- Correction de la texture manquante (carré rose).
- Correction du clic inactif sur le bouton latéral.

## [1.0.0] - 2026-03-20

### Ajouté
- Version initiale pour Minecraft 1.21.1 (NeoForge 21.1.219+).
- Compatibilité Polymorph pour la grille de fabrication (Crafting Grid) de Refined Storage 2.
- Compatibilité Polymorph pour la grille de modèles (Pattern Grid) de Refined Storage 2.
- Corrigé : Synchronisation de la sélection et de l'aperçu pour toutes les grilles.
- Corrigé : Support multi-matrices pour la Fabrication et la Forge dans les grilles de modèles.
- Corrigé : Attachement correct de l'interface de sélection aux slots de résultat actifs.
- Amélioré : Mise à jour forcée de Polymorph lors du recalcul des résultats de RS2, garantissant l'apparition des boutons.
- Corrigé [Grave] : Résolution d'un bug sévère de duplication d'objets et d'objets fantômes causé par l'évaluation d'une ancienne recette sur une matrice vide.
- Corrigé [Interface] : Le bouton de sélection Polymorph s'attache désormais correctement aux grilles de fabrication RS2 (retrait des restrictions trop strictes sur le type de conteneur).
- Corrigé [Logique] : Mappage correct du conteneur RecipeMatrixContainer vers son bloc (BlockEntity) pour une évaluation fiable des recettes Polymorph.
