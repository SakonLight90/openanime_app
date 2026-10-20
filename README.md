# OpenAnime

**OpenAnime** è un'applicazione Android per la visione di anime con sottotitoli in italiano (SUB ITA) e doppiaggio italiano (DUB ITA). Offre un catalogo organizzato, un player video con controlli avanzati e strumenti per gestire la propria esperienza di visione, senza richiedere un account né dipendere dai servizi Google.

**Versione corrente:** `1.0.9`

## Requisiti

- **Android:** versione 12.0 (API 31) o superiore.
- **Connessione Internet:** necessaria per consultare il catalogo e riprodurre i contenuti online.

## Installazione

1. Apri la pagina delle [release di OpenAnime](https://github.com/sakonlight90/openanime_app/releases/latest).
2. Scarica l'APK dell'ultima versione disponibile.
3. Apri il file scaricato e completa l'installazione, autorizzando se necessario l'installazione da origini sconosciute nelle impostazioni di Android.

## Funzionalità

### Home e catalogo

- Carosello in evidenza con anime di tendenza e titoli popolari.
- Sezioni dedicate agli ultimi episodi pubblicati, agli anime popolari, aggiornati, in corso, imminenti e nuovi.
- Contenuti organizzati in sezioni orizzontali per genere.
- Catalogo stagionale suddiviso per stagione e anno.
- Indice alfabetico A–Z con caricamento automatico dei risultati.
- Ricerca con debounce, filtri per tipologia, stato e anno elaborati dal server e caricamento progressivo tramite **Mostra altri**.
- Visualizzazione dell'intero catalogo disponibile quando il campo di ricerca è vuoto.
- Aggiornamento dei contenuti tramite pull-to-refresh.

I generi vengono sincronizzati automaticamente con il sito, così da mantenere aggiornate le relative sezioni senza dover modificare manualmente l'applicazione.

### Dettaglio anime

- Copertina, titolo, valutazione, tipologia, numero di episodi, stato di pubblicazione, generi e sinossi.
- Selezione delle versioni linguistiche disponibili: SUB e DUB.
- Episodi raggruppati per stagione, con indicatore di avanzamento.
- Possibilità di contrassegnare un episodio come visto tramite pressione prolungata.
- Aggiunta alla watchlist e alle liste personalizzate.
- Condivisione tramite deep link.

### Player video

- Riproduzione e pausa, con avanzamento e riavvolgimento di 10 secondi.
- Barra di avanzamento interattiva.
- Velocità di riproduzione regolabile da 0,5× a 2,0×.
- Timer di sospensione da 30 o 60 minuti.
- Picture-in-Picture con controlli remoti.
- Modalità di ridimensionamento video: **Adatta**, **Riempi** e **Zoom**.
- Blocco dei controlli e orientamento orizzontale.
- Riproduzione automatica dell'episodio successivo, con conto alla rovescia di 5 secondi.
- Accesso rapido all'elenco degli episodi.

### Gestione delle visioni

**Watchlist**
- Sezione dedicata agli anime preferiti, visualizzati in una griglia.

**Continua a guardare**
- Episodi lasciati in sospeso, con ripresa automatica dall'ultima posizione salvata.
- Possibilità di rimuovere gli elementi tramite il menu contestuale.

**Cronologia**
- Registro degli episodi guardati, con data e ora.
- Possibilità di cancellare l'intera cronologia.

**Liste personalizzate**
- Creazione e gestione di liste personali.
- Aggiunta, rimozione e riordino degli anime.

### Personalizzazione e impostazioni

- Tema scuro.
- Sei colori d'accento selezionabili.
- Gestione della cache locale, con possibilità di svuotarla.
- Attivazione e disattivazione della riproduzione automatica.
- Onboarding introduttivo con possibilità di saltarlo.

### Aggiornamenti e collegamenti diretti

**Controllo della versione**

L'app verifica la versione minima consentita dal backend e blocca l'utilizzo se quella installata non soddisfa i requisiti richiesti, rendendo necessario l'aggiornamento.

**Deep link**

OpenAnime supporta collegamenti diretti alle schermate degli anime nel formato:

`openanime://anime/{id}`

## Architettura e funzionamento

OpenAnime interroga l'API `api/v2` per recuperare i dati del catalogo, senza disporre di un backend autonomo per tale funzione.

I dati di utilizzo vengono conservati localmente sul dispositivo e comprendono:

- Catalogo visitato e relativa cache.
- Watchlist e liste personalizzate.
- Cronologia delle visioni.
- Posizioni di riproduzione.

La cache consente di conservare localmente i dati già consultati e viene aggiornata in background. La disponibilità dei contenuti online dipende dai servizi utilizzati dall'applicazione e dalla connessione Internet.

## Stack tecnologico

| Componente | Tecnologia |
| --- | --- |
| Linguaggio | Kotlin 2.3.21 |
| Interfaccia utente | Jetpack Compose, Material 3 |
| Compose BOM | 2026.06.00 |
| Architettura | Moduli `:app` → `:data` → `:domain` |
| Dependency Injection | Hilt 2.59.2 |
| Persistenza locale | Room 2.8.4 |
| Networking | Retrofit 3.0.0, OkHttp 5.4.0 |
| Serializzazione | kotlinx.serialization |
| Player video | AndroidX Media3 ExoPlayer 1.10.1 |
| Caricamento immagini | Coil 3.5.0 |
| Build system | Android Gradle Plugin 9.2.1 |

### Configurazione Android

| Parametro | Valore |
| --- | --- |
| `minSdk` | 31 |
| `compileSdk` | 36 |
| `targetSdk` | 36 |