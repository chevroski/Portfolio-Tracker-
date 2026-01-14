# 📋 Implementation Plan - PortfolioTracker

> **Version**: 1.1  
> **Stack**: Java 17 + JavaFX 21 + Maven + Gson  
> **UI Language**: English  
> **Theme**: Dark Mode

---

## Phase 1: Project Setup

### Step 1.1: Create Maven Project Structure
**Files to create:**
- `pom.xml`

**Actions:**
1. Create `pom.xml` with JavaFX 21, Gson 2.10.1, OpenCSV 5.9 dependencies
2. Configure `javafx-maven-plugin` with mainClass `com.portfoliotracker.App`
3. Set Java 17 compiler source/target

**Validation Test:**
```bash
mvn validate
```
✅ Expected: `BUILD SUCCESS`

---

### Step 1.2: Create Directory Structure
**Directories to create:**
```
src/main/java/com/portfoliotracker/
├── model/enums/
├── controller/
├── service/
├── api/
└── util/
src/main/resources/
├── fxml/
├── css/
└── images/
data/
├── portfolios/
├── cache/
├── events/
└── config/
```

**Validation Test:**
- All directories exist
- No compilation errors

---

## Phase 2: Model Layer

### Step 2.1: Create Enums
**Files to create:**
- `model/enums/AssetType.java` (STOCK, CRYPTO)
- `model/enums/TransactionType.java` (BUY, SELL, CONVERT, REWARD)

**Validation Test:**
```java
AssetType.CRYPTO.name() // returns "CRYPTO"
TransactionType.BUY.name() // returns "BUY"
```

---

### Step 2.2: Create Transaction Model
**File:** `model/Transaction.java`

**Fields:**
- `String id`
- `TransactionType type`
- `double quantity`
- `double pricePerUnit`
- `LocalDateTime date`
- `double fees`
- `String notes`

**Methods:**
- Constructor with all fields
- Getters/Setters
- `getTotalCost()` → returns `quantity * pricePerUnit + fees`

**Validation Test:**
```java
Transaction t = new Transaction("1", TransactionType.BUY, 0.5, 35000, LocalDateTime.now(), 10, "");
assert t.getTotalCost() == 17510.0;
```

---

### Step 2.3: Create Asset Model
**File:** `model/Asset.java`

**Fields:**
- `String id`
- `String ticker`
- `String name`
- `AssetType type`
- `List<Transaction> transactions`

**Methods:**
- `getTotalQuantity()` → sum of BUY - SELL quantities
- `getAverageBuyPrice()` → weighted average
- `addTransaction(Transaction t)`

---

### Step 2.4: Create Portfolio Model
**File:** `model/Portfolio.java`

**Fields:**
- `String id`
- `String name`
- `String description`
- `String currency` (EUR, USD)
- `LocalDateTime createdAt`
- `List<Asset> assets`

**Methods:**
- `addAsset(Asset a)`
- `removeAsset(String assetId)`
- `getAssetByTicker(String ticker)`
- `clone()` → deep copy

---

### Step 2.5: Create Event Model
**File:** `model/Event.java`

**Fields:**
- `String id`
- `String title`
- `String description`
- `LocalDateTime date`
- `String portfolioId` (nullable - global if null)

---

### Step 2.6: Create PricePoint Model
**File:** `model/PricePoint.java`

**Fields:**
- `LocalDateTime timestamp`
- `double price`

---

## Phase 3: Persistence Layer

### Step 3.1: Create EncryptionService
**File:** `service/EncryptionService.java`

**Pattern:** Singleton

**Methods:**
- `encrypt(byte[] data, String passphrase)` → byte[]
- `decrypt(byte[] data, String passphrase)` → byte[]
- `isEncryptionEnabled()` → boolean
- `setPassphrase(String passphrase)`

**Implementation:**
- XOR cipher (symmetric encryption/decryption)
- Store encrypted `.json.enc` files

**Validation Test:**
```java
byte[] encrypted = EncryptionService.getInstance().encrypt("test".getBytes(), "secret");
byte[] decrypted = EncryptionService.getInstance().decrypt(encrypted, "secret");
assert new String(decrypted).equals("test");
```

---

### Step 3.2: Create PersistenceService
**File:** `service/PersistenceService.java`

**Pattern:** Singleton

**Methods:**
- `savePortfolio(Portfolio p)`
- `loadPortfolio(String id)` → Portfolio
- `loadAllPortfolios()` → List<Portfolio>
- `deletePortfolio(String id)`
- `saveEvents(List<Event> events)`
- `loadEvents()` → List<Event>

**Implementation:**
- Use Gson with `GsonBuilder().setPrettyPrinting().create()`
- Save to `data/portfolios/{id}.json` (or `.json.enc` if encrypted)
- Integrate EncryptionService for encrypted mode

---

### Step 3.3: Create CacheService
**File:** `service/CacheService.java`

**Pattern:** Singleton

**Methods:**
- `cachePrice(String ticker, LocalDate date, double price)`
- `getCachedPrice(String ticker, LocalDate date)` → Optional<Double>
- `isCached(String ticker, LocalDate date)` → boolean

---

## Phase 4: API Layer

### Step 4.1: Create CoinGeckoClient
**File:** `api/CoinGeckoClient.java`

**Methods:**
- `getCurrentPrice(String coinId, String currency)` → double
- `getPriceHistory(String coinId, String currency, int days)` → List<PricePoint>
- `searchCoin(String query)` → List<String>

**Base URL:** `https://api.coingecko.com/api/v3`

---

### Step 4.2: Create YahooFinanceClient
**File:** `api/YahooFinanceClient.java`

**Methods:**
- `getCurrentPrice(String symbol)` → double
- `getPriceHistory(String symbol, int days)` → List<PricePoint>

---

### Step 4.3: Create ExchangeRateClient
**File:** `api/ExchangeRateClient.java`

**Methods:**
- `getRate(String from, String to)` → double
- `convert(double amount, String from, String to)` → double

---

### Step 4.4: Create MarketDataService
**File:** `service/MarketDataService.java`

**Pattern:** Singleton (façade for all API clients)

**Methods:**
- `getPrice(String ticker, AssetType type, String currency)` → double
- `getPriceHistory(String ticker, AssetType type, String currency, int days)` → List<PricePoint>

---

## Phase 5: Business Logic Layer

### Step 5.1: Create PortfolioService
**File:** `service/PortfolioService.java`

**Pattern:** Singleton

**Methods:**
- `createPortfolio(String name, String description, String currency)` → Portfolio
- `getAllPortfolios()` → List<Portfolio>
- `deletePortfolio(String id)`
- `clonePortfolio(String id)` → Portfolio
- `calculatePortfolioValue(String portfolioId, String currency)` → double
- `importFromCoinbaseCSV(String portfolioId, File csvFile)`

---

### Step 5.2: Create CSV Import Logic
**In:** `PortfolioService.importFromCoinbaseCSV()`

**Coinbase CSV Format:**
```
Line 1-7: Headers/Disclaimer (SKIP)
Line 8: Column headers: Timestamp,Transaction Type,Asset,Quantity Transacted,Spot Price Currency,Spot Price at Transaction,Subtotal,Total (inclusive of fees),Fees,Notes
Line 9+: Data
```

**Parsing Rules:**
1. Skip first 7 lines
2. Parse line 8 for column headers
3. Map columns:
   - `Timestamp` → `LocalDateTime` (ISO format)
   - `Transaction Type` → `TransactionType` (Buy, Sell, Convert, Reward)
   - `Asset` → ticker (BTC, ETH, SOL, etc.)
   - `Quantity Transacted` → quantity
   - `Spot Price at Transaction` → pricePerUnit
   - `Fees` → fees
   - `Notes` → notes

---

### Step 5.3: Create AnalysisService
**File:** `service/AnalysisService.java`

**Pattern:** Singleton

**Methods:**
- `calculateROI(Portfolio p)` → double (percentage)
- `calculatePnL(Portfolio p)` → double
- `getAllocation(Portfolio p)` → Map<AssetType, Double>
- `getAssetAllocation(Portfolio p)` → Map<String, Double>
- `getProfitablePeriods(Portfolio p)` → int
- `getDeficitPeriods(Portfolio p)` → int
- `isProfitable(Portfolio p)` → boolean

---

## Phase 6: UI Layer

### Step 6.1: Create Main FXML Layout
**File:** `resources/fxml/main.fxml`

**Components:**
- `BorderPane` root (dark background)
- Left: `VBox` sidebar with portfolio list
- Center: Content area (StackPane)
- Top: Toolbar with buttons

**Controller:** `MainController.java`

---

### Step 6.2: Create MainController
**File:** `controller/MainController.java`

**FXML Injections:**
- `ListView<Portfolio> portfolioListView`
- `StackPane contentArea`
- `Button newPortfolioBtn`

**Methods:**
- `initialize()` → check for passphrase if encryption enabled
- `onNewPortfolio()` → show creation dialog
- `onPortfolioSelected()` → load portfolio view
- `onImportCSV()` → file chooser + import

---

### Step 6.3-6.6: Portfolio View, Asset Form, Controllers
*(Same as before but with English labels)*

---

## Phase 7: Charts

### Step 7.1: Create Chart View
**File:** `resources/fxml/chart-view.fxml`

**Components:**
- `LineChart<String, Number>` for value over time
- `PieChart` for allocation
- `ComboBox` period selector (1W, 1M, 3M, 1Y, ALL)

---

### Step 7.2: Create ChartController
**File:** `controller/ChartController.java`

---

## Phase 8: Application Entry Point

### Step 8.1: Create App.java
**File:** `App.java`

**Implementation:**
- Load main.fxml
- Apply dark theme stylesheet
- Show passphrase dialog if encryption enabled

---

## Phase 9: Styling (Dark Theme)

### Step 9.1: Create styles.css
**File:** `resources/css/styles.css`

**Dark Theme Colors:**
```css
:root {
    -fx-primary: #1a1a2e;
    -fx-secondary: #16213e;
    -fx-accent: #0f3460;
    -fx-highlight: #e94560;
    -fx-text: #eaeaea;
    -fx-text-muted: #888888;
}
```

**Styles:**
- Dark backgrounds (#1a1a2e, #16213e)
- Accent color for charts (#e94560, #00d9ff)
- White/light text
- Subtle borders and shadows

---

## Phase 10: Encryption Feature (BONUS)

### Step 10.1: Create Passphrase Dialog
**File:** `resources/fxml/passphrase-dialog.fxml`

**Components:**
- `PasswordField` for passphrase
- `Button` Unlock / Create

**Behavior:**
- Show on app startup if encryption enabled
- Validate passphrase by trying to decrypt a test file

---

### Step 10.2: Integrate Encryption
- Modify `PersistenceService` to use `EncryptionService`
- Save files as `.json.enc` when encrypted
- Ask for passphrase on startup

---

## Phase 11: Final Tests

### Test 11.1: Full Workflow Test
1. Launch app → passphrase dialog (if encrypted)
2. Create new portfolio "Crypto Portfolio"
3. Import Coinbase CSV
4. Verify transactions imported (BTC, LTC, LINK, SOL)
5. View LineChart
6. View PieChart allocation
7. Close and reopen → data restored

### Test 11.2: CSV Import Test
- Import provided Coinbase.csv example
- Expected: 11 transactions across 4 assets (BTC, LTC, LINK, SOL)

---

## Phase 13: Demo Dataset (SOUTENANCE)

### Step 13.1: Create Demo Data Files
**Files to create:**
- `data/demo/demo-portfolios.json`
- `data/demo/demo-events.json`
- `data/demo/demo-cache.json`

**Demo Portfolio 1 - "Crypto Hodler":**
- BTC: 0.5 @ €30,000 (2024-01-15)
- ETH: 5.0 @ €2,200 (2024-02-20)
- SOL: 50.0 @ €80 (2024-03-10)
- Total invested: ~€30,000

**Demo Portfolio 2 - "Trader Actif":**
- BTC: 0.2 @ €35,000 (multiple trades)
- LTC: 10.0 @ €70
- LINK: 100 @ €15
- With sells and rewards

---

### Step 13.2: Create DemoService
**File:** `service/DemoService.java`

**Methods:**
- `loadDemoData()` → loads all demo files
- `createDemoPortfolios()` → creates 2 portfolios
- `createDemoEvents()` → creates sample events
- `preloadCache()` → caches current prices

---

### Step 13.3: Add Load Demo Button
**Modify:** `main.fxml`

**Add button:**
```xml
<Button onAction="#onLoadDemo" styleClass="btn-secondary">
    <graphic><FontIcon iconLiteral="fas-database"/></graphic>
    <text>Load Demo</text>
</Button>
```

**Add method in MainController:**
- `onLoadDemo()` → calls DemoService, refreshes UI

---

## Phase 14: UX Polish

### Step 14.1: Theme Toggle
- Add Dark/Light toggle button
- Store preference in config

### Step 14.2: Currency Switch
- Add EUR/USD toggle in toolbar
- Recalculate all values on switch

### Step 14.3: Loading States
- Add skeleton loading for API calls
- Show "Loading..." while fetching prices

---

## Files Summary

| Phase | Files | Key Files |
|-------|-------|-----------|
| 1. Setup | 1 | `pom.xml` |
| 2. Models | 6 | `Portfolio.java`, `Asset.java`, `Transaction.java` |
| 3. Persistence | 3 | `EncryptionService.java`, `PersistenceService.java`, `CacheService.java` |
| 4. APIs | 4 | `CoinGeckoClient.java`, `MarketDataService.java` |
| 5. Business | 2 | `PortfolioService.java`, `AnalysisService.java` |
| 6. UI | 6 | FXML + Controllers |
| 7. Charts | 2 | `chart-view.fxml`, `ChartController.java` |
| 8. Entry | 1 | `App.java` |
| 9. Styling | 1 | `styles.css` (Dark Theme) |
| 10. Encryption | 1 | `passphrase-dialog.fxml` |
| 12. UI Enhancement | 3 | Ikonli icons, ControlsFX |
| 13. Demo | 4 | `DemoService.java`, demo JSON files |
| **TOTAL** | **34+** | |

---

*Implementation Plan - PortfolioTracker v1.2 - Updated 11 Jan 2026*

