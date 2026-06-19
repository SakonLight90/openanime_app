# Documentazione Tecnica — OpenAnime

> Documentazione completa di architettura, implementazione e dettagli tecnici.

## Architettura

3 moduli con separazione rigorosa delle dipendenze:

```
:app      → UI (Compose), DI (Hilt), Navigazione
:data     → Retrofit, Room, Repository Impl, Mapper
:domain   → Modelli puri Kotlin, interfacce Repository
```

**Pattern**: MVVM con repository offline-first (stale-while-revalidate).
**DI**: Hilt 2.57.1 (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
**State**: `StateFlow` nei ViewModel, `combine{}` per flussi multipli.
**Serialization**: kotlinx-serialization con `JsonNamingStrategy.SnakeCase`.

## Stack Tecnico

| Componente | Versione |
|---|---|
| Kotlin | 2.0.21 |
| Compose BOM | 2025.02.00 |
| Material3 | via BOM |
| Room | 2.7.0 (con KSP) |
| Retrofit | 2.9.0 (converter-kotlinx-serialization) |
| OkHttp | 4.12.0 |
| Media3 (ExoPlayer) | 1.5.1 |
| Hilt | 2.57.1 |
| Coil | 2.7.0 |
| Navigation Compose | 2.8.9 |
| Gradle | 8.13 |
| SDK min / target / compile | 24 / 35 / 35 |

## Schermate

### HomeScreen
Schermata principale Netflix-style:
- HeroBanner carosello swipeabile con gradient overlay
- Continua a guardare con progress bar e menu contestuale
- Ultimi episodi con immagine anime
- Watchlist con indicatore cuore
- Sezioni scrollabili (Aggiornati, Popolari, In Corso, Imminenti, Nuovi)
- Pull-to-refresh con Material3
- Auto-refresh su cache clear

### SearchScreen
- Input con debounce 300ms, risultati in griglia adattiva
- Filtri combinabili per tipo, stato e anno
- All anime senza query (caricamento A-Z con deduplica)
- Paginazione client-side 30 risultati
- Pulsanti A-Z e Generi

### AzListScreen
- Barra alfabeto A-Z
- Auto-load lettera "A" all'apertura
- Cache offline per lettera

### DetailScreen
Banner hero `cover_image` 350dp con gradient, titolo in overlay, rating, play button full-width, trama, LanguageSelector, episodi raggruppati per stagione, anime correlati.

### PlayerScreen
Media3 ExoPlayer con controlli Compose custom:
- Auto-hide UI dopo inattività
- Doppio tap skip 10s
- Swipe luminosità/volume
- Bottom bar: SkipPrevious, rewind, play/pause, forward, SkipNext, velocità, episodi
- Prossimo episodio con countdown 5s
- PiP automatico e manuale
- Salvataggio posizione ogni 5s
- Retry stream (3 tentativi)
- Sleep timer 30 minuti

### OnboardingScreen
3 pagine swipeabili stile Netflix, skip link, flag in SharedPreferences.

### ContinueWatchingScreen
Griglia adattiva, menu tre puntini per rimuovere.

### CategoryScreen / GenreAnimeScreen
Griglia adattiva con paginazione client-side 30 item, cache offline.

### SettingsScreen
Sezioni: Cache, Auto-play, Storico, Liste personalizzate, Stagionali, Colore accento, Versione.

### HistoryScreen
Elenco cronologico con poster, titolo, episodio e data/ora.

### CustomListsScreen / CustomListDetailScreen
CRUD liste personalizzate con FAB e cestino.

### SeasonalScreen
Raggruppamento anime per stagione (Inverno/Primavera/Estate/Autunno YYYY).

## Navigazione

Bottom Navigation Bar: Home, Cerca, Lista, Impostazioni. Schermate full-screen fuori BottomBar: Detail, Player, Storico, Liste, Stagionali.

Transizioni:
- Onboarding → slide + fade
- Detail / Player → slide orizzontale + fade
- Altre → fade in/out

Deep link: `openanime://anime/{id}`

## Database (Room)

10 tabelle, 10 DAO, versione 6:

| Tabella | PK | Scopo |
|---|---|---|
| `anime_cache` | `id` | Cache dettagli anime con categoria |
| `episodes_cache` | `id` | Cache episodi |
| `watchlist` | `anime_id` | Lista preferiti |
| `continue_watching` | `anime_id + episode_id` | Posizione riproduzione |
| `language_preferences` | `anime_id` | Preferenza lingua |
| `app_settings` | `key` | Impostazioni chiave-valore |
| `search_cache` | `query + anime_id` | Cache ricerca |
| `watch_history` | `anime_id + episode_id` | Storico visioni |
| `custom_lists` | `id` | Liste personalizzate |
| `custom_list_items` | `list_id + anime_id` | Item liste |

Migration: v1→v6.

## Modelli Domain

```
Anime(id, title, image, type, episodeCount, rating, releaseDate)
AnimeDetail(id, title, synopsis, genres, rating, episodeCount, type, status, isDub, language, coverImage, bannerImage, relatedVersions, episodes, releaseDate)
Episode(id, number, token, language, anime)
Season(seasonNumber, episodes, episodeRange)
ContinueWatchingItem(animeId, episodeId, positionMs, durationMs, lastWatchedAt, animeTitle, animeImage, episodeNumber)
StreamResponse(success, url, error)
WatchHistoryEntry(animeId, episodeId, watchedAt, animeTitle, animeImage, episodeNumber)
CustomList(id, name, createdAt, items)
```

## Flusso Dati

```
API → Retrofit → kotlinx.serialization → Domain Model → Room cache → Flow → ViewModel (combine) → UI
```

- HomeScreen: 9 flow combinati
- DetailScreen: cache + API per episodi
- Search/AzList/Genre: stale-while-revalidate
- Player: stream API → ExoPlayer
- ContinueWatching/History/CustomLists/Settings: flow diretto Room

## Test

Unit test (JUnit 4 + MockK):
- HomeViewModelTest, SearchViewModelTest, DetailViewModelTest
- AnimeRepositoryImplTest, HomeUiStateTest

Integration test (Room + MockWebServer):
- ContinueWatchingDaoTest, SettingsDaoTest, AnimeApiTest

UI test (Compose Test):
- OnboardingScreenTest

## Build

```bash
./gradlew assembleRelease
```

Keystore in `app/keystore/release.keystore`. Credenziali via env: KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD.

CI/CD via Codemagic (`codemagic.yaml`).
