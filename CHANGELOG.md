# Changelog

All notable changes to RS Polymorph are documented here.

---

## [1.0.5] - 2026-04-02

### Fixed

- **Pattern Grid on dedicated server** — Recipe selection now works on Pattern Grid in multiplayer. The server-side packet handler could not find the BlockEntity because PatternGrid uses phantom/filter slots; added fallback strategies (menu Grid accessor + reverse container lookup). Also, the selected recipe ID is now propagated to the server so crafting patterns get correctly tagged for autocrafting.

### Performance

- **Per-frame caching** — Container discovery and input hash computation are now cached per render frame, eliminating 2-3 redundant slot scans per frame.
- **ConcurrentHashMap** — Replaced `synchronized(WeakHashMap)` with `ConcurrentHashMap` for the container registry maps, removing global lock contention.
- **Matrix lookup caching** — `RsGridRecipeData.getMatrices()` results are now cached after first successful lookup.
- **Error logging** — Silent exception catch in recipe sync now logs warnings instead of swallowing errors.

### Correctifs

- **Pattern Grid sur serveur dédié** — La sélection de recette fonctionne maintenant sur la Grille de Patrons en multijoueur. Le handler de paquet côté serveur ne trouvait pas le BlockEntity car le PatternGrid utilise des slots fantômes/filtres ; ajout de stratégies de fallback (accessor Grid du menu + recherche inversée des containers). De plus, l'ID de recette sélectionnée est maintenant propagé au serveur pour que les patrons soient correctement tagués pour l'autocraft.

### Performance

- **Cache par frame** — La découverte des containers et le calcul du hash d'entrée sont maintenant cachés par frame de rendu, éliminant 2-3 scans de slots redondants par frame.
- **ConcurrentHashMap** — Remplacement de `synchronized(WeakHashMap)` par `ConcurrentHashMap` pour les maps de registre des containers, supprimant la contention de lock global.
- **Cache du lookup de matrices** — Les résultats de `RsGridRecipeData.getMatrices()` sont maintenant cachés après le premier lookup réussi.
- **Logging d'erreurs** — Le catch silencieux dans la sync des recettes log maintenant des warnings au lieu d'avaler les erreurs.

---

## [1.0.4] - 2026-03-31

### Fixed

- **Pattern Grid on dedicated server** — Further fix for Pattern Grid support on multiplayer servers. Improved container discovery by using the menu's Grid accessor as a fallback, then reverse-looking up registered containers from the block entity. This resolves cases where phantom/filter slots prevented direct slot scanning.

### Correctifs

- **Pattern Grid sur serveur dédié** — Correction supplémentaire du support de la Grille de Patrons sur les serveurs multijoueurs. Amélioration de la découverte des containers via l'accessor Grid du menu en fallback, puis recherche inversée des containers enregistrés depuis le block entity. Cela résout les cas où les slots fantômes/filtres empêchaient le scan direct des slots.

---

## [1.0.3] - 2026-03-30

### Fixed

- **Multiplayer / Dedicated server** — Recipe selection in the Polymorph popup now correctly updates the crafting grid output on dedicated servers. Added a client→server network packet so the server applies the selection.
- **Multiplayer / Dedicated server** — The Polymorph button now works on dedicated servers for both Crafting Grid and Pattern Grid. The widget no longer depends on server-side maps that may be empty on the client.
- **Pattern Grid on dedicated server** — Fixed the Polymorph button not appearing on the Pattern Grid in multiplayer. The widget now discovers the grid's block entity via the menu accessor as a fallback when slot scanning fails (phantom/filter slots).
- **Vanilla crafting table** — Polymorph's recipe selector no longer breaks on the vanilla crafting table after having used an RS2 grid. The rendering override is now limited to RS2 grid screens only.

### Correctifs

- **Multijoueur / Serveur dédié** — La sélection de recette dans le popup Polymorph met désormais correctement à jour la sortie de la grille de craft sur les serveurs dédiés. Ajout d'un paquet réseau client→serveur pour appliquer la sélection.
- **Multijoueur / Serveur dédié** — Le bouton Polymorph fonctionne désormais sur les serveurs dédiés pour la Grille de Craft et la Grille de Patrons. Le widget ne dépend plus de maps côté serveur potentiellement vides côté client.
- **Pattern Grid sur serveur dédié** — Correction du bouton Polymorph absent sur la Grille de Patrons en multijoueur. Le widget découvre désormais le block entity de la grille via l'accessor du menu en fallback quand le scan des slots échoue (slots fantômes/filtres).
- **Table de craft vanilla** — Le sélecteur de recettes Polymorph ne se bloque plus sur la table de craft vanilla après avoir utilisé une grille RS2. L'interception du rendu est désormais limitée aux grilles RS2 uniquement.

---

## [1.0.1] - 2026-03-24

### Fixed

- **Dedicated server crash** — The mod no longer crashes on server startup. All client-only code is now isolated in a separate class so the JVM doesn't try to load GUI types on the server.
- **Recipe selection not applying** — Fixed a case where selecting a recipe had no effect when the open grid's block entity could not be identified.
- **Server stability** — Added defensive error handling to server-side recipe sync to prevent Polymorph API issues from crashing the server.

### Correctifs

- **Crash au démarrage du serveur dédié** — Le mod ne crashe plus au démarrage du serveur. Tout le code client est désormais isolé dans une classe séparée pour éviter que le JVM charge des types GUI côté serveur.
- **Sélection de recette sans effet** — Correction d'un cas où la sélection d'une recette n'avait aucun effet lorsque le block entity de la grille ouverte ne pouvait pas être identifié.
- **Stabilité serveur** — Ajout d'une gestion d'erreur défensive dans la synchronisation des recettes côté serveur pour éviter qu'un problème de l'API Polymorph ne fasse crasher le serveur.

---

## [1.0.0] - 2026-03-20

### Added

- Initial release for Minecraft 1.21.1 (NeoForge 21.1.219+).
- Polymorph recipe selection support for the RS2 Crafting Grid.
- Polymorph recipe selection support for the RS2 Pattern Grid.
- Pattern items are tagged with the selected recipe so autocrafting uses the correct output.
- Multi-matrix support for both Crafting and Smithing table patterns.
- Stale recipe entries no longer appear in the popup when the grid contents change.
- Fixed item duplication / ghost item bug caused by evaluating non-empty recipes on empty matrices.

### Ajouts

- Première version pour Minecraft 1.21.1 (NeoForge 21.1.219+).
- Support de la sélection de recettes Polymorph pour la Grille de Craft RS2.
- Support de la sélection de recettes Polymorph pour la Grille de Patrons RS2.
- Les patrons sont marqués avec la recette sélectionnée pour que l'autocraft utilise le bon résultat.
- Support multi-matrices pour les patrons de table de craft et d'enclume.
- Les recettes obsolètes n'apparaissent plus dans le popup lorsque le contenu de la grille change.
- Correction d'un bug de duplication d'objets / objets fantômes causé par l'évaluation de recettes sur des matrices vides.
