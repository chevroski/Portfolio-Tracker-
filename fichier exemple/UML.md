# UML Diagrams - PortfolioTracker

> **Version**: 1.1  
> **Format**: Mermaid  
> **Last Updated**: 11 January 2026

---

## 1. Class Diagram - Complete

```mermaid
classDiagram
    direction TB

    %% ==================== ENUMS ====================
    class AssetType {
        <<enumeration>>
        STOCK
        CRYPTO
    }

    class TransactionType {
        <<enumeration>>
        BUY
        SELL
        CONVERT
        REWARD
    }

    %% ==================== MODELS ====================
    class Transaction {
        -String id
        -TransactionType type
        -double quantity
        -double pricePerUnit
        -LocalDateTime date
        -double fees
        -String notes
        +getTotalCost() double
        +getId() String
        +getType() TransactionType
        +getQuantity() double
        +getPricePerUnit() double
        +getDate() LocalDateTime
        +getFees() double
        +getNotes() String
    }

    class Asset {
        -String id
        -String ticker
        -String name
        -AssetType type
        -List~Transaction~ transactions
        +getTotalQuantity() double
        +getAverageBuyPrice() double
        +addTransaction(Transaction t) void
        +removeTransaction(String id) void
        +getId() String
        +getTicker() String
        +getName() String
        +getType() AssetType
        +getTransactions() List~Transaction~
    }

    class Portfolio {
        -String id
        -String name
        -String description
        -String currency
        -LocalDateTime createdAt
        -List~Asset~ assets
        +addAsset(Asset a) void
        +removeAsset(String id) void
        +getAssetByTicker(String ticker) Asset
        +clone() Portfolio
        +getId() String
        +getName() String
        +getDescription() String
        +getCurrency() String
        +getCreatedAt() LocalDateTime
        +getAssets() List~Asset~
    }

    class Event {
        -String id
        -String title
        -String description
        -LocalDateTime date
        -String portfolioId
        +getId() String
        +getTitle() String
        +getDescription() String
        +getDate() LocalDateTime
        +getPortfolioId() String
    }

    class PricePoint {
        -LocalDateTime timestamp
        -double price
        +getTimestamp() LocalDateTime
        +getPrice() double
    }

    %% ==================== RELATIONSHIPS - MODELS ====================
    Portfolio "1" *-- "0..*" Asset : contains
    Asset "1" *-- "0..*" Transaction : contains
    Asset --> AssetType : has type
    Transaction --> TransactionType : has type
    Event "0..*" --> "0..1" Portfolio : linked to
```

---

## 2. Class Diagram - Services

```mermaid
classDiagram
    direction TB

    %% ==================== SERVICES ====================
    class EncryptionService {
        <<singleton>>
        -EncryptionService instance$
        -String passphrase
        -boolean enabled
        +getInstance()$ EncryptionService
        +encrypt(byte[] data, String passphrase) byte[]
        +decrypt(byte[] data, String passphrase) byte[]
        +isEncryptionEnabled() boolean
        +setPassphrase(String passphrase) void
        +setEnabled(boolean enabled) void
    }

    class PersistenceService {
        <<singleton>>
        -PersistenceService instance$
        -Gson gson
        -String DATA_PATH$
        +getInstance()$ PersistenceService
        +savePortfolio(Portfolio p) void
        +loadPortfolio(String id) Portfolio
        +loadAllPortfolios() List~Portfolio~
        +deletePortfolio(String id) void
        +saveEvents(List~Event~ events) void
        +loadEvents() List~Event~
    }

    class CacheService {
        <<singleton>>
        -CacheService instance$
        -String CACHE_PATH$
        +getInstance()$ CacheService
        +cachePrice(String ticker, LocalDate date, double price) void
        +getCachedPrice(String ticker, LocalDate date) Optional~Double~
        +isCached(String ticker, LocalDate date) boolean
        +clearCache() void
    }

    class MarketDataService {
        <<singleton>>
        -MarketDataService instance$
        -CoinGeckoClient coinGeckoClient
        -YahooFinanceClient yahooClient
        -ExchangeRateClient exchangeClient
        +getInstance()$ MarketDataService
        +getPrice(String ticker, AssetType type, String currency) double
        +getPriceHistory(String ticker, AssetType type, String currency, int days) List~PricePoint~
        +convertCurrency(double amount, String from, String to) double
    }

    class PortfolioService {
        <<singleton>>
        -PortfolioService instance$
        +getInstance()$ PortfolioService
        +createPortfolio(String name, String desc, String currency) Portfolio
        +getAllPortfolios() List~Portfolio~
        +getPortfolio(String id) Portfolio
        +updatePortfolio(Portfolio p) void
        +deletePortfolio(String id) void
        +clonePortfolio(String id) Portfolio
        +addAssetToPortfolio(String portfolioId, Asset asset) void
        +addTransaction(String portfolioId, String assetId, Transaction t) void
        +calculatePortfolioValue(String portfolioId, String currency) double
        +importFromCoinbaseCSV(String portfolioId, File csvFile) void
    }

    class AnalysisService {
        <<singleton>>
        -AnalysisService instance$
        +getInstance()$ AnalysisService
        +calculateROI(Portfolio p) double
        +calculatePnL(Portfolio p) double
        +getAllocation(Portfolio p) Map~AssetType, Double~
        +getAssetAllocation(Portfolio p) Map~String, Double~
        +getProfitablePeriods(Portfolio p) int
        +getDeficitPeriods(Portfolio p) int
        +isProfitable(Portfolio p) boolean
    }

    %% ==================== RELATIONSHIPS - SERVICES ====================
    PersistenceService --> EncryptionService : uses
    PortfolioService --> PersistenceService : uses
    PortfolioService --> MarketDataService : uses
    AnalysisService --> MarketDataService : uses
    MarketDataService --> CacheService : uses
```

---

## 3. Class Diagram - API Clients

```mermaid
classDiagram
    direction TB

    class CoinGeckoClient {
        -String BASE_URL$
        -HttpClient httpClient
        -Gson gson
        +getCurrentPrice(String coinId, String currency) double
        +getPriceHistory(String coinId, String currency, int days) List~PricePoint~
        +searchCoin(String query) List~String~
        -sendRequest(String endpoint) String
    }

    class YahooFinanceClient {
        -String BASE_URL$
        -HttpClient httpClient
        -Gson gson
        +getCurrentPrice(String symbol) double
        +getPriceHistory(String symbol, int days) List~PricePoint~
        -sendRequest(String endpoint) String
    }

    class ExchangeRateClient {
        -String BASE_URL$
        -HttpClient httpClient
        -Gson gson
        +getRate(String from, String to) double
        +convert(double amount, String from, String to) double
        -sendRequest(String endpoint) String
    }

    class MarketDataService {
        <<singleton>>
    }

    MarketDataService --> CoinGeckoClient : uses for crypto
    MarketDataService --> YahooFinanceClient : uses for stocks
    MarketDataService --> ExchangeRateClient : uses for conversion
```

---

## 4. Class Diagram - Controllers

```mermaid
classDiagram
    direction TB

    class MainController {
        -ListView~Portfolio~ portfolioListView
        -StackPane contentArea
        -Button newPortfolioBtn
        -Button importCsvBtn
        -Button chartsBtn
        +initialize() void
        +onNewPortfolio() void
        +onPortfolioSelected() void
        +onImportCSV() void
        +onShowCharts() void
        +loadView(String fxmlPath) void
        -showPassphraseDialog() void
    }

    class PortfolioController {
        -TableView~Asset~ assetTableView
        -Label portfolioNameLabel
        -Label portfolioValueLabel
        -Portfolio currentPortfolio
        +setPortfolio(Portfolio p) void
        +onAddAsset() void
        +onRemoveAsset() void
        +onEditAsset() void
        +onClonePortfolio() void
        +onDeletePortfolio() void
        +refreshTable() void
    }

    class AssetController {
        -TextField tickerField
        -TextField nameField
        -ComboBox~AssetType~ typeCombo
        -TextField quantityField
        -TextField priceField
        -DatePicker datePicker
        -TextField feesField
        -Asset currentAsset
        -boolean editMode
        +onSave() void
        +onCancel() void
        +setEditMode(Asset a) void
        +setPortfolio(Portfolio p) void
        -validateInputs() boolean
    }

    class ChartController {
        -LineChart~String, Number~ lineChart
        -PieChart pieChart
        -ComboBox~String~ periodCombo
        -VBox portfolioCheckboxes
        -List~Portfolio~ portfolios
        +setPortfolios(List~Portfolio~ portfolios) void
        +updateLineChart(String period) void
        +updatePieChart(Portfolio p) void
        +onPeriodChange() void
        -calculateHistoricalValues(Portfolio p, int days) List~PricePoint~
    }

    class PassphraseController {
        -PasswordField passphraseField
        -Button unlockBtn
        -Label errorLabel
        +onUnlock() void
        +onCancel() void
        -validatePassphrase(String passphrase) boolean
    }

    %% ==================== CONTROLLER DEPENDENCIES ====================
    MainController --> PortfolioService : uses
    MainController --> EncryptionService : uses
    PortfolioController --> PortfolioService : uses
    PortfolioController --> MarketDataService : uses
    AssetController --> PortfolioService : uses
    ChartController --> PortfolioService : uses
    ChartController --> MarketDataService : uses
    ChartController --> AnalysisService : uses
    PassphraseController --> EncryptionService : uses
```

---

## 5. Sequence Diagram - Create Portfolio

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant MainController
    participant Dialog
    participant PortfolioService
    participant PersistenceService

    User->>MainController: Click "New Portfolio"
    MainController->>Dialog: Show creation dialog
    User->>Dialog: Enter name, description, currency
    User->>Dialog: Click "Create"
    Dialog->>PortfolioService: createPortfolio(name, desc, currency)
    PortfolioService->>PortfolioService: Generate UUID
    PortfolioService->>PersistenceService: savePortfolio(portfolio)
    PersistenceService->>PersistenceService: Convert to JSON
    PersistenceService-->>PortfolioService: success
    PortfolioService-->>Dialog: return Portfolio
    Dialog->>Dialog: Close dialog
    Dialog-->>MainController: Portfolio created
    MainController->>MainController: Refresh portfolio list
    MainController-->>User: Display new portfolio
```

---

## 6. Sequence Diagram - Import CSV

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant MainController
    participant FileChooser
    participant PortfolioService
    participant CSVParser
    participant PersistenceService

    User->>MainController: Click "Import CSV"
    MainController->>FileChooser: Show file chooser
    User->>FileChooser: Select Coinbase.csv
    FileChooser-->>MainController: File path
    MainController->>PortfolioService: importFromCoinbaseCSV(portfolioId, file)
    PortfolioService->>CSVParser: Parse CSV file
    CSVParser->>CSVParser: Skip lines 1-7 (headers)
    CSVParser->>CSVParser: Read column headers (line 8)
    loop For each data row
        CSVParser->>CSVParser: Parse timestamp, type, asset, qty, price, fees
        CSVParser->>PortfolioService: Create Transaction
        PortfolioService->>PortfolioService: Add to Asset
    end
    CSVParser-->>PortfolioService: Parsing complete
    PortfolioService->>PersistenceService: savePortfolio(portfolio)
    PersistenceService-->>PortfolioService: success
    PortfolioService-->>MainController: Import complete (11 transactions)
    MainController-->>User: Show success message
```

---

## 7. Sequence Diagram - View Charts

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant ChartController
    participant PortfolioService
    participant MarketDataService
    participant CacheService
    participant CoinGeckoAPI

    User->>ChartController: Select portfolio + period (1M)
    ChartController->>PortfolioService: getPortfolio(id)
    PortfolioService-->>ChartController: Portfolio with assets
    
    loop For each asset in portfolio
        ChartController->>MarketDataService: getPriceHistory(ticker, type, currency, 30)
        MarketDataService->>CacheService: getCachedHistory(ticker)
        alt Cache hit
            CacheService-->>MarketDataService: Cached prices
        else Cache miss
            MarketDataService->>CoinGeckoAPI: GET /coins/{id}/market_chart
            CoinGeckoAPI-->>MarketDataService: Price history JSON
            MarketDataService->>CacheService: cacheHistory(ticker, prices)
        end
        MarketDataService-->>ChartController: List<PricePoint>
    end
    
    ChartController->>ChartController: Calculate portfolio value per day
    ChartController->>ChartController: Update LineChart
    ChartController->>ChartController: Calculate allocation
    ChartController->>ChartController: Update PieChart
    ChartController-->>User: Display charts
```

---

## 8. Sequence Diagram - Encryption Flow

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant App
    participant PassphraseDialog
    participant EncryptionService
    participant PersistenceService
    participant MainController

    User->>App: Launch application
    App->>EncryptionService: isEncryptionEnabled()
    alt Encryption enabled
        EncryptionService-->>App: true
        App->>PassphraseDialog: Show passphrase dialog
        User->>PassphraseDialog: Enter passphrase
        PassphraseDialog->>EncryptionService: setPassphrase(passphrase)
        PassphraseDialog->>PersistenceService: loadAllPortfolios()
        PersistenceService->>EncryptionService: decrypt(data, passphrase)
        alt Valid passphrase
            EncryptionService-->>PersistenceService: Decrypted data
            PersistenceService-->>PassphraseDialog: Portfolios loaded
            PassphraseDialog->>MainController: Initialize main view
        else Invalid passphrase
            EncryptionService-->>PersistenceService: Error
            PersistenceService-->>PassphraseDialog: Decryption failed
            PassphraseDialog-->>User: Show error message
        end
    else Encryption disabled
        EncryptionService-->>App: false
        App->>MainController: Initialize main view directly
    end
    MainController-->>User: Display application
```

---

## 9. Component Diagram

```mermaid
flowchart TB
    subgraph Presentation["Presentation Layer"]
        FXML["FXML Views<br/>main.fxml<br/>portfolio-view.fxml<br/>asset-form.fxml<br/>chart-view.fxml"]
        Controllers["Controllers<br/>MainController<br/>PortfolioController<br/>AssetController<br/>ChartController"]
        CSS["styles.css<br/>Dark Theme"]
    end

    subgraph Business["Business Layer"]
        PortfolioSvc["PortfolioService"]
        AnalysisSvc["AnalysisService"]
        MarketDataSvc["MarketDataService"]
    end

    subgraph Data["Data Layer"]
        PersistenceSvc["PersistenceService"]
        CacheSvc["CacheService"]
        EncryptionSvc["EncryptionService"]
    end

    subgraph API["API Layer"]
        CoinGecko["CoinGeckoClient"]
        Yahoo["YahooFinanceClient"]
        Exchange["ExchangeRateClient"]
    end

    subgraph Storage["Local Storage"]
        Portfolios["data/portfolios/*.json"]
        Cache["data/cache/*.json"]
        Events["data/events/events.json"]
    end

    subgraph External["External APIs"]
        CoinGeckoAPI["CoinGecko API<br/>api.coingecko.com"]
        YahooAPI["Yahoo Finance<br/>finance.yahoo.com"]
        ExchangeAPI["ExchangeRate API<br/>exchangerate-api.com"]
    end

    FXML --> Controllers
    CSS --> FXML
    Controllers --> PortfolioSvc
    Controllers --> AnalysisSvc
    Controllers --> MarketDataSvc

    PortfolioSvc --> PersistenceSvc
    PortfolioSvc --> MarketDataSvc
    AnalysisSvc --> MarketDataSvc
    MarketDataSvc --> CacheSvc
    MarketDataSvc --> CoinGecko
    MarketDataSvc --> Yahoo
    MarketDataSvc --> Exchange
    PersistenceSvc --> EncryptionSvc

    CoinGecko --> CoinGeckoAPI
    Yahoo --> YahooAPI
    Exchange --> ExchangeAPI

    PersistenceSvc --> Portfolios
    PersistenceSvc --> Events
    CacheSvc --> Cache
```

---

## 10. State Diagram - Portfolio Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Empty : Create Portfolio
    
    Empty --> WithAssets : Add Asset
    WithAssets --> WithAssets : Add/Remove Asset
    WithAssets --> Empty : Remove All Assets
    
    Empty --> Cloned : Clone
    WithAssets --> Cloned : Clone
    
    Cloned --> WithAssets : Modify Clone
    
    Empty --> [*] : Delete
    WithAssets --> [*] : Delete
    Cloned --> [*] : Delete
    
    state WithAssets {
        [*] --> Tracking
        Tracking --> Tracking : Add Transaction
        Tracking --> Analyzing : View Charts
        Analyzing --> Tracking : Close Charts
    }
```

---

## 11. Entity Relationship Diagram

```mermaid
erDiagram
    PORTFOLIO ||--o{ ASSET : contains
    ASSET ||--o{ TRANSACTION : has
    PORTFOLIO ||--o{ EVENT : "may have"
    
    PORTFOLIO {
        string id PK
        string name
        string description
        string currency
        datetime createdAt
    }
    
    ASSET {
        string id PK
        string portfolioId FK
        string ticker
        string name
        enum type
    }
    
    TRANSACTION {
        string id PK
        string assetId FK
        enum type
        double quantity
        double pricePerUnit
        datetime date
        double fees
        string notes
    }
    
    EVENT {
        string id PK
        string portfolioId FK
        string title
        string description
        datetime date
    }
    
    PRICE_CACHE {
        string ticker PK
        date date PK
        double price
    }
```

---

## 12. Package Diagram

```mermaid
flowchart TB
    subgraph com.portfoliotracker
        subgraph model
            enums["enums<br/>AssetType<br/>TransactionType"]
            models["Portfolio<br/>Asset<br/>Transaction<br/>Event<br/>PricePoint"]
        end
        
        subgraph service
            services["PortfolioService<br/>MarketDataService<br/>AnalysisService<br/>PersistenceService<br/>CacheService<br/>EncryptionService"]
        end
        
        subgraph api
            clients["CoinGeckoClient<br/>YahooFinanceClient<br/>ExchangeRateClient"]
        end
        
        subgraph controller
            controllers["MainController<br/>PortfolioController<br/>AssetController<br/>ChartController<br/>PassphraseController"]
        end
        
        subgraph util
            utils["DateUtils<br/>CurrencyUtils"]
        end
        
        App["App.java"]
    end
    
    App --> controller
    controller --> service
    service --> model
    service --> api
    service --> util
    api --> model
    models --> enums
```

---

*UML Diagrams - PortfolioTracker v1.1 - Mermaid Format*
