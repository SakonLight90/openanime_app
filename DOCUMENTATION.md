# Documentazione Tecnica — OpenAnime

> Documentazione completa di architettura, implementazione e dettagli tecnici.
>
> **Nota**: i valori riportati sono quelli effettivamente in uso, verificati contro
> `gradle/libs.versions.toml` e i `build.gradle.kts` dei tre moduli.

## Architettura

3 moduli con separazione rigorosa delle dipendenze:

```
:app      → UI (Compose), DI (Hilt), Navigazione
:data     → Retrofit, Room, Repository Impl, Mapper
:domain   → Modelli puri Kotlin, interfacce Repository
```

**Pattern**: MVVM con repository offline-first (stale-while-revalidate).
**DI**: Hilt 2.59.1 (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
**State**: `StateFlow` nei ViewModel, `combine{}` per flussi multipli. Mai LiveData.
**Serialization**: kotlinx-serialization con `JsonNamingStrategy.SnakeCase` configurato
globalmente in `NetworkModule` (non per-annotazione).

## Stack Tecnico

| Componente | Versione |
|---|---|
| Kotlin | 2.3.21 |
| AGP | 9.2.1 |
| KSP | 2.3.9 |
| Compose BOM | 2026.06.00 |
| Material3 | via BOM |
| Room | 2.8.4 (con KSP) |
| Retrofit | 3.0.0 (converter-kotlinx-serialization) |
| OkHttp | 5.4.0 |
| Media3 (ExoPlayer) | 1.10.1 |
| Hilt | 2.59.2 |
| Coil | 3.5.0 (coil3, network-okhttp) |
| Navigation Compose | 2.9.8 |
| kotlinx-serialization | 1.11.0 |
| kotlinx-coroutines | 1.11.0 |
| Activity Compose | 1.13.0 |
| Lifecycle | 2.10.0 |
| SDK min / target / compile | 31 / 36 / 36 |
| Gradle wrapper | 9.4.1 |
| JVM target | 17 |
| versionName / versionCode | 1.0.9 / 9 |

## Configurazione Build

`gradle.properties`:
- `org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m`
- `org.gradle.parallel=false`
- `org.gradle.caching=true`

Release: `isMinifyEnabled = true`, `isShrinkResources = true`, R8 con
`proguard-rules.pro` dedicato (regole standard kotlinx-serialization / Retrofit / OkHttp).

## Backend

| Endpoint | Uso | Risposta |
|---|---|---|
| `GET /api/v2/home` | Bootstrap Home | object |
| `GET /api/v2/anime/newest` | Categoria "Nuovi" | array |
| `GET /api/v2/anime/updated` | Categoria "Aggiornati" | array |
| `GET /api/v2/anime/ongoing` | Categoria "In Corso" | array |
| `GET /api/v2/anime/upcoming?year=&season=` | Categoria "Imminenti" | array |
| `GET /api/v2/anime/az-list/{letter}?page=` | Indice A-Z | **paginato** |
| `GET /api/v2/anime/tops/{period}` | Classifica | array |
| `GET /api/v2/anime/{id}` | Dettaglio anime | object |
| `GET /api/v2/anime/{id}/episodes` | Episodi | array |
| `GET /api/v2/search?q=&type=&status=&year=&page=` | Ricerca con filtri | **paginato** |
| `GET /api/v2/stream?token=` | Risoluzione URL stream | object |
| `GET /api/v2/genres` | Lista generi (dal sito) | array |
| `GET /api/v2/genres/{id}/anime?page=` | Anime per genere | **paginato** |
| `GET /api/v2/app-version` | Controllo versione bloccante | object |

**Envelope paginato** (`PagedAnime`): `{ items, page, page_size, total, total_pages }`.

I filtri della ricerca sono eseguiti **lato server**: non filtrano più la lista già
scaricata, quindi un filtro non riflesso nei dati non può restare silenziosamente
inattivo.

`upcoming` accetta la stagione in italiano o in inglese (`autunno`, `autumn`, `fall`, …).

`HomeResponse.continueWatching` è dichiarato ma **non utilizzato**: il continue-watching
è esclusivamente locale (Room).

## Navigazione

`Screen.kt` dichiara 18 route. `NavRoutes.kt` centralizza la costruzione delle route
(`playerRoute`, `detailRoute`, `genreAnimeRoute`, `azListRoute`, `categoryRoute`,
`customListRoute`) — **mai usare stringhe raw nelle schermate**.

Bottom Navigation Bar: Home, Cerca, Lista, Impostazioni.
Schermate full-screen fuori BottomBar: Detail, Player, Storico, Liste, Stagionali,
Continua a guardare, A-Z, Generi, Genere, Categoria, Ultimi episodi, Onboarding.

Transizioni:
- Onboarding / Detail / Player → slide orizzontale + fade
- Altre → fade in/out

Deep link: `openanime://anime/{id}`, gestito in `NavGraph` con `DisposableEffect`
sull'`Activity.lifecycle` a `ON_RESUME`.

## Database (Room v9, 10 tabelle)

| Tabella | PK | Scopo |
|---|---|---|
| `anime_cache` | `(id, category)` | Cache anime con categoria multipla |
| `episodes_cache` | `id` | Cache episodi |
| `watchlist` | `anime_id` | Lista preferiti |
| `continue_watching` | `(anime_id, episode_id)` | Posizione riproduzione (denormalizzata) |
| `language_preferences` | `anime_id` | Preferenza versione lingua |
| `app_settings` | `key` | Impostazioni chiave-valore |
| `search_cache` | `(query, anime_id)` | Cache ricerca |
| `watch_history` | `(anime_id, episode_id)` | Storico visioni (denormalizzato) |
| `custom_lists` | `id` autoGen | Liste personalizzate |
| `custom_list_items` | `(list_id, anime_id)` | Item liste |

`anime_cache` ha PK composta: lo stesso anime può esistere in più righe, una per
categoria (`trending`, `popular`, `az:a`, `genre:5`, `""` per il dettaglio).
Di conseguenza `getById()` ordina per `(category = '') DESC, LENGTH(synopsis) DESC,
updated_at DESC` per restituire la variante più ricca, e `getByCustomList()` fa il JOIN
`custom_list_items` evitando il pattern N+1.

`sort_order` conserva la posizione dell'anime nella risposta del server, così le query per
categoria restituiscono l'elenco nell'ordine del backend (trending, novità, upcoming).
`getCacheCount()` usa `COUNT(DISTINCT id)` perché la PK composta conterebbe 5 volte lo
stesso anime.

Watchlist e liste personalizzati fanno la JOIN su `anime_cache`: per questo
`clearCache()` non esegue `DELETE FROM anime_cache` ma `clearUnreferenced()`, che
elimina solo le righe non referenziate da `watchlist` o `custom_list_items`. Con il
DELETE pieno le liste si svuotavano in modo permanente.

### Migrations

`MIGRATION_1_2` → `MIGRATION_9_10`, tutte registrate in `DatabaseModule`.
`MIGRATION_7_8` usa `PRAGMA table_info` per rename condizionali delle colonne camelCase.
`MIGRATION_8_9` normalizza lo schema di 5 tabelle (default allineati alle entity).
`MIGRATION_9_10` aggiunge `anime_cache.sort_order` (con `ALTER TABLE`) e gli indici
`idx_anime_cache_category` / `idx_anime_cache_id`. `sort_order` serve a conservare
l'ordinamento semantico del server: senza, le query per categoria leggevano le righe
in ordine di rowid e la Home mostrava gli anime per id invece che per trending.

`fallbackToDestructiveMigration` **non** è attivo in release: ogni nuova versione del
database richiede una migration esplicita.

Test di migrazione: `data/src/test/.../local/MigrationTest.kt` (10 test, semina uno
schema v8 reale e verifica la migrazione fino a v10).

## Modelli Domain

```
Anime(id, title, image, type, episodeCount, rating, releaseDate, status)
AnimeDetail(id, title, synopsis, genres, rating, episodeCount, type, status, isDub,
            language, coverImage, bannerImage, relatedVersions, episodes, releaseDate)
RelatedVersion(id, title, language, isDub)
Episode(id, number: Double, token, language, anime: AnimeRef?)
AnimeRef(id, title, image)
Season(seasonNumber, episodes, episodeRange)
ContinueWatchingItem(animeId, episodeId, positionMs, durationMs, lastWatchedAt,
                     animeTitle, animeImage, episodeNumber)
StreamResponse(success, url, error)
WatchHistoryEntry(animeId, episodeId, watchedAt, animeTitle, animeImage, episodeNumber)
CustomList(id, name, createdAt, items)
CustomListItem(listId, animeId, position)
Genre(id, name, slug)
```

`Episode.number` è `Double`: usare **sempre** `formatEpisodeNumber()` /
`episodeDisplayTitle()` da `utils/Utils.kt`, mai `.toInt()`.

`Anime.releaseYear` è derivato da `releaseDate` (primi 4 caratteri) ed è la base dei
filtri per anno.

I campi opzionali hanno default espliciti (`""`, `0`, `null`) così un campo assente
o `null` nella risposta non provoca una `SerializationException`.

## Componenti UI Condivisi

`ui/components/AnimeGrid.kt`:
- `AnimeGridCard` — card poster con titolo
- `AnimeGrid` — griglia lazy adattiva
- `AnimeGridScreen` — schermata completa: titolo + loading / error+retry / empty / griglia + "Mostra altri"
- `AnimeGridLoading`, `AnimeGridError`, `AnimeGridEmpty`

`ui/components/AnimePosterCard.kt` — card poster orizzontale per le `LazyRow` della Home.

Usati da: CategoryScreen, GenreAnimeScreen, AzListScreen, SearchScreen, SeasonalScreen,
CustomListDetailScreen, WatchlistScreen.

## Schermate

### HomeScreen
HeroBanner carosello (pulsante "Guarda" = resume o dettaglio), Continua a guardare
con menu •••, Ultimi episodi, Watchlist, sezioni per categoria, sezioni per genere
dinamiche. `PullToRefreshBox`. `watchDataState()` per auto-refresh dopo `clearCache()`.

### SearchScreen
Debounce 300ms nel VM. I filtri (tipo, stato, anno) **rilanciano la ricerca sul
server** invece di filtrare la lista già scaricata. Con query vuota viene caricato
l'intero catalogo A-Z (36 lettere) con dedup per ID e i filtri agiscono localmente
su quel set.

Paginazione: server-side quando c'è una query (pagine da 60, "Mostra altri"),
client-side a blocchi da 30 quando si sfoglia il catalogo completo.

### DetailScreen
Banner hero 350dp con gradient, versioni, episodi raggruppati per stagione
(`groupEpisodesIntoSeasons`, chunk da 30), lista personalizzata, condivisione deep link.

### PlayerScreen
Media3 ExoPlayer su TextureView manuale, controlli Compose custom:
- auto-hide UI dopo 4s
- doppio tap skip 10s con accumulo
- swipe luminosità/volume
- resize mode (Adatta / Riempi / Zoom)
- velocità di riproduzione
- sleep timer 30 min
- PiP con `BroadcastReceiver` e `RemoteAction` play/pause
- lock orientamento e togli overlay
- countdown 5s auto-advance al prossimo episodio
- retry stream fino a 3 tentativi con `refreshTokenAndRetry()`
- salvataggio posizione ogni 500ms

### Altre
AzListScreen, ContinueWatchingScreen, WatchlistScreen, GenresScreen, GenreAnimeScreen,
CategoryScreen, SeasonalScreen, HistoryScreen, CustomListsScreen,
CustomListDetailScreen, AllLatestEpisodesScreen, OnboardingScreen, UpdateRequiredScreen.

## Localizzazione

Tutto il testo utente è in `res/values/strings.xml`. Non usare stringhe hardcoded
nel codice Kotlin.

## Test

Unit test (JUnit 4 + MockK):
- `AnimeRepositoryImplTest` (7), `HomeViewModelTest` (4), `HomeUiStateTest` (2)
- `DetailViewModelTest` (5), `SearchViewModelTest` (8), `PlayerViewModelTest` (11)

Integration test (Room in-memory):
- `ContinueWatchingDaoTest` (6), `SettingsDaoTest` (3), `MigrationTest` (7)

API test (MockWebServer):
- `AnimeApiTest` (4)

UI test (Compose):
- `OnboardingScreenTest` (2)

## Build

```bash
./gradlew clean :app:assembleRelease --stacktrace
```

Keystore in `app/keystore/release.keystore`. Credenziali via env:
`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

CI/CD via Codemagic (`codemagic.yaml`), `mac_mini_m2`, Java 17.
