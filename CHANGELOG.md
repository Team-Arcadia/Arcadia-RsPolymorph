# Changelog

All notable changes to RS Polymorph are documented here.

---

## [1.0.9] - 2026-05-05

### Fixed

- **Root cause of the Create encased fan crash** (issue #1, reported by @djsime1) — Polymorph's `PolymorphApi.registerBlockEntity(Class, IRecipeDataFactory)` records the class as metadata only and stores every factory in a flat list; `createBlockEntityRecipeData(be)` then iterates that list and returns the first factory's non-null result, regardless of the BE's actual class. Our factories `be -> new RsGridRecipeData(be)` always returned non-null, so Polymorph attached `RsGridRecipeData` to every block entity in the world (encased fans, hoppers, etc.). Combined with Polymorph's `RecipeCache` being keyed by input alone — not by `RecipeType` — a SMOKING lookup primed the cache and the next SMELTING query with the same input returned SMOKING recipes, propagating a wrong-typed `RecipeHolder` until Create's `BlastingType.process` blew up on the implicit cast. Factories now guard with `instanceof` so non-RS2 block entities fall through to the next factory and never receive our data. The 1.0.8 `MixinRecipeManagerSafety` is kept as a defense-in-depth backstop.

### Correctifs

- **Cause racine du crash du ventilateur encastré Create** (issue #1, signalée par @djsime1) — `PolymorphApi.registerBlockEntity(Class, IRecipeDataFactory)` enregistre la classe comme simple métadonnée et stocke toutes les factories dans une liste plate ; `createBlockEntityRecipeData(be)` itère cette liste et renvoie le premier non-null, sans vérifier la classe réelle du BE. Nos factories `be -> new RsGridRecipeData(be)` renvoyaient toujours non-null, donc Polymorph attachait `RsGridRecipeData` à **tous** les block entities (ventilateurs Create, entonnoirs, etc.). Comme le `RecipeCache` interne de Polymorph est indexé par input seul — pas par `RecipeType` — une requête SMOKING amorçait le cache, et la requête SMELTING suivante avec le même input renvoyait des recettes SMOKING ; un `RecipeHolder` du mauvais type se propageait jusqu'à `BlastingType.process` de Create qui plantait sur le cast implicite. Les factories sont maintenant protégées par un `instanceof` pour que les block entities non-RS2 passent à la factory suivante et ne reçoivent jamais nos données. Le `MixinRecipeManagerSafety` ajouté en 1.0.8 reste en place comme deuxième ligne de défense.

---

## [1.0.8] - 2026-04-27

### Fixed

- **ClassCastException with Create encased fan + Polymorph 1.1.0** — Reported as a server-side crash in `AllFanProcessingTypes$BlastingType.process` (`SmeltingRecipe cannot be cast to SmokingRecipe`). Polymorph 1.1.0's `MixinRecipeManager` injects on `RecipeManager.getRecipeFor(RecipeType, RecipeInput, Level, RecipeHolder)` and, under specific cross-block-entity tick orderings, can resolve a stored recipe of a different `RecipeType` than the one requested. The caller (Create's blasting/smoking dual lookup) holds the result via Java type erasure and crashes at the first use-site cast. Added `MixinRecipeManagerSafety`, an `@Inject(at = RETURN, cancellable = true)` on the same overload that validates the runtime recipe type matches the requested `RecipeType`; on mismatch it falls back to a fresh `getRecipesFor(...).stream().findFirst()` lookup scoped to the requested type. Polymorph behavior is preserved on all matching-type paths.

### Correctifs

- **ClassCastException avec le ventilateur encastré de Create + Polymorph 1.1.0** — Signalé comme crash serveur dans `AllFanProcessingTypes$BlastingType.process` (`SmeltingRecipe cannot be cast to SmokingRecipe`). Le `MixinRecipeManager` de Polymorph 1.1.0 injecte sur `RecipeManager.getRecipeFor(RecipeType, RecipeInput, Level, RecipeHolder)` et, dans certains ordres de tick croisés entre block entities, peut renvoyer une recette enregistrée d'un `RecipeType` différent de celui demandé. L'appelant (la double recherche blasting/smoking de Create) reçoit le résultat via l'effacement de type Java et plante au premier cast utilisé. Ajout de `MixinRecipeManagerSafety`, un `@Inject(at = RETURN, cancellable = true)` sur la même surcharge qui vérifie que le type runtime de la recette correspond au `RecipeType` demandé ; en cas d'incohérence, il retombe sur une recherche `getRecipesFor(...).stream().findFirst()` scopée au type demandé. Le comportement de Polymorph est préservé sur tous les chemins où les types correspondent.

---

## [1.0.7] - 2026-04-20

### Fixed

- **Pattern Grid preview stuck on previous recipe (RS 2.0.2)** — After selecting a different recipe via the Polymorph popup, the Pattern Grid preview kept showing the previous recipe's output until the pattern was printed once. `MixinRecipeMatrix` only updated the `ResultContainer` via `setResult` but left `currentRecipe` unchanged, so the next `updateResult` short-circuit (`currentRecipe.matches(input)` returns true) re-assembled the old recipe and overwrote the preview. Now also syncs `currentRecipe` via the accessor whenever a Polymorph override is applied.
- **Singleplayer selection not applied server-side** — On singleplayer, `selectRecipe()` used `server.execute` with an `activeBlockEntity` captured from the client-side menu scan. That BE lives on the client level, so the subsequent `isClientSide` guard either skipped `updateResult` or ran it on the wrong level; the selection worked only as a side-effect of later `matrixChanged()` triggers. Unified the SP and MP paths: both now dispatch `SelectRecipePacket` over the local loopback (SP) or network (MP). The handler resolves the server-side BlockEntity via `player.containerMenu`, correctly scoped to the server level, and persists the selection in `RsGridRecipeData.selections`.

### Correctifs

- **Aperçu du Pattern Grid figé sur la recette précédente (RS 2.0.2)** — Après avoir choisi une autre recette via le popup Polymorph, l'aperçu du Pattern Grid gardait le résultat de la recette précédente jusqu'à la prochaine impression de patron. `MixinRecipeMatrix` ne mettait à jour que le `ResultContainer` via `setResult` sans toucher à `currentRecipe` ; le prochain `updateResult` court-circuitait (`currentRecipe.matches(input)` retourne vrai) et ré-assemblait l'ancienne recette, écrasant l'aperçu. Synchronise désormais aussi `currentRecipe` via l'accessor à chaque override Polymorph.
- **Sélection non appliquée côté serveur en solo** — En solo, `selectRecipe()` utilisait `server.execute` avec un `activeBlockEntity` capturé depuis le scan du menu côté client. Ce BE vit sur le niveau client, donc le garde `isClientSide` qui suivait faisait soit sauter `updateResult`, soit le lançait sur le mauvais niveau ; la sélection ne fonctionnait que par effet de bord des déclenchements ultérieurs de `matrixChanged()`. Chemins SP et MP unifiés : les deux expédient maintenant `SelectRecipePacket` via la boucle locale (SP) ou le réseau (MP). Le handler résout le BlockEntity côté serveur via `player.containerMenu`, correctement scopé sur le niveau serveur, et persiste la sélection dans `RsGridRecipeData.selections`.

### Cleanup

- **Nested duplicate clone removed** — An accidental inner `Arcadia-RsPolymorph/` git clone was sitting inside the working tree. It shadowed the main repo with a stale copy (one commit behind) and caused confusion. Deleted.

### Nettoyage

- **Clone imbriqué en double supprimé** — Un clone git interne accidentel `Arcadia-RsPolymorph/` se trouvait dans l'arbre de travail. Il masquait le dépôt principal avec une copie périmée (un commit de retard) et semait la confusion. Supprimé.

---

## [1.0.6] - 2026-04-09

### Fixed

- **Pattern Grid widget not created on dedicated server** — The Polymorph widget was never created for the Pattern Grid because the result slot detection relied on class name matching (`contains("DisabledSlot")`), which fails for anonymous inner classes like `PatternGridContainerMenu$5`. Now uses `instanceof` checks instead.
- **Client-side BlockEntity discovery on dedicated server** — On a dedicated server the menu's Grid field is null (client constructor uses GridData, not the real BE). Added a proximity-based fallback that finds the nearest grid BlockEntity registered in `CONTAINER_TO_BE` from chunk sync.
- **Pattern recipe tagging on dedicated server** — `createCraftingPattern()` reads the selected recipe ID from a static volatile field that was already cleared by the time RS2's create-pattern packet arrives. Now falls back to reading from `RsGridRecipeData.selections` (persisted by `SelectRecipePacket`), ensuring patterns are correctly tagged for autocrafting.

### Correctifs

- **Widget Pattern Grid non créé sur serveur dédié** — Le widget Polymorph n'était jamais créé pour la Grille de Patrons car la détection du slot résultat utilisait la correspondance par nom de classe (`contains("DisabledSlot")`), qui échoue pour les classes anonymes internes comme `PatternGridContainerMenu$5`. Utilise maintenant des vérifications `instanceof`.
- **Découverte du BlockEntity côté client sur serveur dédié** — Sur un serveur dédié, le champ Grid du menu est null (le constructeur client utilise GridData, pas le vrai BE). Ajout d'un fallback par proximité qui trouve le BlockEntity de grille le plus proche enregistré dans `CONTAINER_TO_BE` depuis la synchronisation de chunk.
- **Tagging de recette des patrons sur serveur dédié** — `createCraftingPattern()` lisait l'ID de recette sélectionnée depuis un champ static volatile déjà effacé au moment où le paquet create-pattern de RS2 arrive. Lit maintenant depuis `RsGridRecipeData.selections` (persisté par `SelectRecipePacket`), garantissant que les patrons sont correctement tagués pour l'autocraft.

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
