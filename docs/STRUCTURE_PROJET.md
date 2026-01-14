# 📁 Structure Complète du Projet PortfolioTracker

## Vue d'ensemble

```
progjava/
├── pom.xml                    # Configuration Maven
├── src/                       # Code source
│   ├── main/java/             # Code Java
│   ├── main/resources/        # Ressources (FXML, CSS, images)
│   └── test/java/             # Tests unitaires
├── data/                      # Données persistées
├── docs/                      # Documentation
└── target/                    # Fichiers compilés (généré)
```

---

## 📦 pom.xml

**Rôle:** Fichier de configuration Maven - définit les dépendances et la compilation.

**Dépendances principales:**
- `javafx-controls`, `javafx-fxml` - Interface graphique
- `gson` - Sérialisation JSON
- `opencsv` - Import/export CSV
- `ikonli-javafx`, `ikonli-fontawesome5-pack` - Icônes
- `controlsfx` - Composants UI avancés
- `junit-jupiter` - Tests unitaires

---

## 📂 src/main/java/com/portfoliotracker/

### 🚀 App.java

**Rôle:** Point d'entrée de l'application JavaFX.

**Responsabilités:**
- Initialise JavaFX (`Application.launch()`)
- Charge le FXML principal (`main.fxml`)
- Applique le fichier CSS (`styles.css`)
- Configure la fenêtre (titre, dimensions)

---

## 📂 api/ - Clients d'API externes

| Fichier | API utilisée | Rôle |
|---------|--------------|------|
| `CoinGeckoClient.java` | CoinGecko + Binance | Prix temps réel et historiques des cryptos |
| `YahooFinanceClient.java` | Yahoo Finance | Prix des actions et ETFs |
| `ExchangeRateClient.java` | ExchangeRate-API | Taux de change EUR/USD/GBP |
| `WhaleAlertClient.java` | Whale Alert | Grosses transactions crypto ("whales") |

### CoinGeckoClient.java
- `getPrice(symbol, currency)` - Prix actuel d'une crypto
- `getHistoricalPrices(symbol, days)` - Historique des prix
- Fallback vers API Binance si CoinGecko échoue
- Mapping des symboles (BTC → bitcoin, ETH → ethereum)

### YahooFinanceClient.java  
- `getPrice(symbol)` - Prix actuel d'une action
- `getHistoricalData(symbol, range)` - Historique
- Supporte les actions US (AAPL, GOOGL, MSFT)

### ExchangeRateClient.java
- `getExchangeRate(from, to)` - Conversion de devises
- Cache des taux pour éviter trop d'appels

### WhaleAlertClient.java
- `getRecentTransactions()` - Transactions > $1M
- Données mockées si l'API ne répond pas
- Affichées dans la vue Analysis

---

## 📂 controller/ - Contrôleurs JavaFX (MVC)

| Fichier | Vue associée | Rôle |
|---------|--------------|------|
| `MainController.java` | main.fxml | Navigation principale, toolbar |
| `PortfolioController.java` | portfolio-view.fxml | Liste des assets, CRUD |
| `ChartController.java` | chart-view.fxml | Graphiques, analyse |
| `AssetController.java` | asset-form.fxml | Formulaire ajout asset |
| `AnalysisController.java` | analysis-view.fxml | Whale alerts |
| `PassphraseController.java` | passphrase-dialog.fxml | Chiffrement |

### MainController.java
- Gère la toolbar (boutons New, Charts, Analysis...)
- Navigation entre les vues via `loadView()`
- Gère le sélecteur de devise (EUR/USD/GBP)
- Initialise les services au démarrage

### PortfolioController.java
- Affiche la table des assets avec prix temps réel
- Boutons Add Asset, Remove, Clone, Delete
- Calcule valeur totale et P&L du portfolio
- Charge les prix en asynchrone

### ChartController.java
- **LineChart** - Évolution valeur portfolio
- **PieChart** - Répartition des assets
- Sélection de période (1W, 1M, 3M, 1Y)
- Mode "Compare All" (superposition portfolios)
- Affichage des événements (flags) sur le chart
- Calcul Profit Days %

### AssetController.java
- Formulaire d'ajout d'asset
- **Auto-fetch prix** quand on quitte le champ Ticker
- Bouton "Fetch" pour récupérer prix manuel
- Validation des entrées

### AnalysisController.java
- Affiche les whale alerts dans un TableView
- Calcule statistiques (volume 24h, top token...)
- Sentiment marché (Bullish/Bearish)

### PassphraseController.java
- Dialog pour entrer le mot de passe de chiffrement
- Active/désactive l'encryption des données

---

## 📂 model/ - Modèles de données (POJO)

| Fichier | Rôle |
|---------|------|
| `Portfolio.java` | Conteneur d'assets avec nom, devise, description |
| `Asset.java` | Actif financier (ticker, type, transactions) |
| `Transaction.java` | Achat/vente avec date, quantité, prix, frais |
| `Event.java` | Événement marqué sur le chart |
| `PricePoint.java` | Point de prix (timestamp + valeur) |

### Portfolio.java
```java
- id: String (UUID unique)
- name: String ("Crypto Portfolio")
- description: String
- currency: String ("EUR")
- assets: List<Asset>
- createdAt: LocalDateTime
```

### Asset.java
```java
- ticker: String ("BTC", "AAPL")
- name: String ("Bitcoin")
- type: AssetType (CRYPTO, STOCK, ETF)
- transactions: List<Transaction>
```
**Méthodes clés:**
- `getTotalQuantity()` - Somme achats - ventes
- `getAverageBuyPrice()` - Prix moyen pondéré
- `getTotalInvested()` - Total investi (achats + frais)

### Transaction.java
```java
- type: TransactionType (BUY, SELL, REWARD)
- quantity: double
- pricePerUnit: double
- date: LocalDateTime
- fees: double
- notes: String
```

### Event.java
```java
- id: String
- portfolioId: String
- date: LocalDate
- title: String
- description: String
```

### enums/
- `AssetType.java` - CRYPTO, STOCK, ETF, COMMODITY
- `TransactionType.java` - BUY, SELL, REWARD

---

## 📂 service/ - Logique métier

| Service | Pattern | Rôle |
|---------|---------|------|
| `PortfolioService` | Singleton | CRUD portfolios |
| `MarketDataService` | Singleton | Prix temps réel |
| `PersistenceService` | Singleton | Sauvegarde JSON |
| `EncryptionService` | Singleton | Chiffrement XOR |
| `CacheService` | Singleton | Cache prix historiques |
| `EventService` | Singleton | Gestion événements |
| `AnalysisService` | Singleton | Calculs financiers |
| `DemoService` | Singleton | Données de démo |

### PortfolioService.java
- `createPortfolio(name, description, currency)`
- `getPortfolio(id)`, `getAllPortfolios()`
- `updatePortfolio(portfolio)`
- `deletePortfolio(id)`
- `clonePortfolio(id)` - Duplique un portfolio
- `importFromCSV(file)` - Import transactions

### MarketDataService.java
- `getPrice(ticker, type, currency)` - Prix actuel
- `getHistoricalPrices(ticker, type, days)` - Historique
- Délègue aux clients API selon le type d'asset
- Conversion de devises automatique

### PersistenceService.java
- `savePortfolio(portfolio)` - Sauvegarde en JSON
- `loadPortfolio(id)`, `loadAllPortfolios()`
- Utilise Gson avec adapters LocalDateTime
- Chiffre/déchiffre si passphrase définie

### EncryptionService.java
- `encrypt(data, passphrase)` - Chiffrement XOR
- `decrypt(data, passphrase)` - Déchiffrement
- `isEncryptionEnabled()` - Statut activation

### CacheService.java
- `getCachedPrices(ticker, date)` - Lit cache
- `cachePrices(ticker, prices)` - Écrit cache
- Fichiers JSON dans `/data/cache/`
- Prix historiques immuables (jamais re-téléchargés)

### EventService.java
- `addEvent(portfolioId, date, title, description)`
- `getEventsForPortfolio(portfolioId)`
- `deleteEvent(id)`
- Événements affichés comme flags sur chart

### DemoService.java
- `loadDemoData()` - Charge portfolios démo
- 3 portfolios pré-configurés (Crypto, Stocks, Mixed)
- Transactions exemple avec dates passées

---

## 📂 util/ - Utilitaires

| Fichier | Rôle |
|---------|------|
| `LocalDateTimeAdapter.java` | Adapter Gson pour LocalDateTime |

---

## 📂 src/main/resources/

### fxml/ - Fichiers de vue

| Fichier | Rôle |
|---------|------|
| `main.fxml` | Layout principal (toolbar + zone contenu) |
| `portfolio-view.fxml` | Table des assets d'un portfolio |
| `chart-view.fxml` | Charts (LineChart + PieChart) + stats |
| `asset-form.fxml` | Formulaire ajout asset |
| `analysis-view.fxml` | Whale alerts + statistiques marché |
| `passphrase-dialog.fxml` | Dialog mot de passe |

### css/styles.css

**Thème:** Dark mode moderne avec accents cyan (#00d9ff)

**Sections:**
- Variables couleurs
- Boutons (primary, secondary, danger)
- Tables, Charts, Cards
- Formulaires, Dialogs
- Styles spécifiques par vue

---

## 📂 data/ - Données persistées

```
data/
├── portfolios/              # Fichiers JSON des portfolios
│   ├── portfolio-xxx.json
│   └── ...
├── cache/                   # Cache des prix historiques
│   ├── btc-history.json
│   └── ...
├── events/                  # Événements marqués
│   └── events.json
├── demo/                    # Données de démonstration
│   ├── crypto-demo.json
│   ├── stocks-demo.json
│   └── mixed-demo.json
└── config/                  # Configuration (vide)
```

---

## 📂 src/test/java/ - Tests unitaires

| Fichier | Tests |
|---------|-------|
| `AssetTest.java` | getTotalQuantity, getAverageBuyPrice, getTotalInvested |
| `EncryptionServiceTest.java` | encrypt/decrypt roundtrip, passphrase handling |

**Framework:** JUnit 5 (Jupiter)
**Couverture:** 11 tests, 100% passent

---

## 📂 docs/ - Documentation

| Fichier | Contenu |
|---------|---------|
| `CODE_STRUCTURE.md` | Architecture MVC |
| `TECH_STACK.md` | Technologies utilisées |
| `MANUEL_UTILISATEUR.md` | Guide utilisateur |
| `TESTS.md` | Documentation des tests |
| `progress.md` | Suivi d'avancement |

---

## 🔄 Flux de données simplifié

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Views     │───▶│ Controllers │───▶│  Services   │
│   (FXML)    │◀───│   (Java)    │◀───│   (Logic)   │
└─────────────┘    └─────────────┘    └─────────────┘
                                             │
                   ┌─────────────────────────┼─────────────────────────┐
                   ▼                         ▼                         ▼
            ┌─────────────┐           ┌─────────────┐           ┌─────────────┐
            │   Models    │           │    API      │           │Persistence  │
            │   (POJO)    │           │  Clients    │           │   (JSON)    │
            └─────────────┘           └─────────────┘           └─────────────┘
```
