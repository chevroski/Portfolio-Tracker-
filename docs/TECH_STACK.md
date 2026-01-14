# 🛠️ Stack Technique - PortfolioTracker

> **Version**: 1.0  
> **Dernière mise à jour**: Janvier 2026

---

## 1. Vue d'Ensemble

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                      │
│                         (JavaFX 21)                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Views     │  │  Charts     │  │   Controls/Forms    │  │
│  │   (FXML)    │  │ (LineChart) │  │   (TableView)       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                        │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  MainController │ PortfolioController │ AssetController ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                          │
│  ┌───────────────┐  ┌────────────────┐  ┌────────────────┐  │
│  │ PortfolioSvc  │  │  MarketDataSvc │  │  AnalysisSvc   │  │
│  └───────────────┘  └────────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        DATA LAYER                            │
│  ┌───────────────┐  ┌────────────────┐  ┌────────────────┐  │
│  │  JSON Storage │  │   API Clients  │  │  Cache Manager │  │
│  │   (Gson)      │  │  (HttpClient)  │  │                │  │
│  └───────────────┘  └────────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      EXTERNAL APIS                           │
│     CoinGecko API    │    Yahoo Finance    │  ExchangeRate  │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Technologies Principales

### 2.1 Core

| Composant | Technologie | Version | Justification |
|-----------|-------------|---------|---------------|
| **Langage** | Java | 17+ (LTS) | Stabilité, support long terme, requis par le module |
| **Build Tool** | Maven | 3.9+ | Standard industrie, gestion dépendances simple |
| **UI Framework** | JavaFX | 21 | Graphiques intégrés (Charts), FXML pour séparation vue/logique |

### 2.2 Dépendances Maven

```xml
<!-- pom.xml - Dépendances principales -->
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21</version>
    </dependency>
    
    <!-- JSON Processing -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
    
    <!-- HTTP Client (Java 11+ native) -->
    <!-- java.net.http.HttpClient - inclus dans JDK -->
    
    <!-- CSV Parsing (pour imports exchange) -->
    <dependency>
        <groupId>com.opencsv</groupId>
        <artifactId>opencsv</artifactId>
        <version>5.9</version>
    </dependency>
</dependencies>
```

---

## 3. APIs Externes

### 3.1 CoinGecko API (Crypto)

| Endpoint | Usage | Rate Limit |
|----------|-------|------------|
| `/coins/list` | Liste tous les tokens | 10-30 req/min |
| `/coins/{id}/market_chart` | Historique prix | 10-30 req/min |
| `/simple/price` | Prix actuel | 10-30 req/min |

**Exemple de réponse:**
```json
{
  "bitcoin": {
    "eur": 42000,
    "eur_24h_change": 2.5
  }
}
```

### 3.2 Yahoo Finance (via yfinance API alternative)

> Note: Yahoo Finance n'a pas d'API officielle gratuite. Utilisation d'endpoints non-officiels.

| Endpoint | Usage |
|----------|-------|
| `/v8/finance/chart/{symbol}` | Historique + prix actuel |

### 3.3 ExchangeRate-API (Devises)

| Endpoint | Usage |
|----------|-------|
| `/latest/{base}` | Taux de change actuels |

---

## 4. Stockage Local

### 4.1 Format de Données

**Format choisi: JSON** (via Gson)

Avantages:
- Lisible par l'humain
- Facile à débugger
- Support natif via Gson
- Pas de dépendance externe (vs SQLite)

### 4.2 Structure des Fichiers

```
data/
├── portfolios/
│   ├── portfolio_001.json
│   ├── portfolio_002.json
│   └── ...
├── cache/
│   ├── prices_btc_2024.json
│   ├── prices_aapl_2024.json
│   └── ...
├── events/
│   └── events.json
└── config/
    └── settings.json
```

### 4.3 Schéma Portfolio (JSON)

```json
{
  "id": "uuid-string",
  "name": "Mon Portfolio Crypto",
  "description": "Investissements crypto 2024",
  "currency": "EUR",
  "createdAt": "2024-01-15T10:30:00Z",
  "assets": [
    {
      "id": "uuid-string",
      "ticker": "BTC",
      "type": "CRYPTO",
      "transactions": [
        {
          "type": "BUY",
          "quantity": 0.5,
          "pricePerUnit": 35000,
          "date": "2024-01-15T10:30:00Z",
          "fees": 10
        }
      ]
    }
  ]
}
```

---

## 5. Architecture Pattern

### 5.1 Pattern MVC (Model-View-Controller)

```
┌─────────────┐     Events      ┌─────────────┐
│    View     │ ───────────────▶│ Controller  │
│   (FXML)    │                 │   (Java)    │
│             │◀─────────────── │             │
└─────────────┘    Updates      └─────────────┘
                                       │
                                       │ Calls
                                       ▼
                               ┌─────────────┐
                               │    Model    │
                               │  (Service)  │
                               │             │
                               └─────────────┘
```

### 5.2 Patterns Utilisés

| Pattern | Usage |
|---------|-------|
| **MVC** | Séparation UI/Logique |
| **Singleton** | Services (MarketDataService) |
| **Observer** | Mise à jour auto des graphiques |
| **Factory** | Création des objets Asset |
| **Repository** | Accès aux données persistées |

---

## 6. Outils de Développement

| Outil | Usage |
|-------|-------|
| **IDE** | IntelliJ IDEA / Eclipse |
| **Version Control** | Git |
| **Diagrammes UML** | PlantUML / Draw.io |
| **Tests** | JUnit 5 (optionnel) |

---

## 7. Configuration Projet Maven

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.portfoliotracker</groupId>
    <artifactId>portfolio-tracker</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>21</javafx.version>
    </properties>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.portfoliotracker.App</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 8. Résumé des Choix Techniques

| Décision | Choix | Raison |
|----------|-------|--------|
| Build | Maven | Standard, simple, bien documenté |
| UI | JavaFX + FXML | Charts intégrés, séparation claire |
| JSON | Gson | Léger, performant, simple d'utilisation |
| HTTP | java.net.http | Natif JDK 11+, pas de dépendance |
| CSV | OpenCSV | Parsing robuste pour imports |
| Architecture | MVC | Pattern classique, maintenable |

---

*Stack conçue pour simplicité et maintenabilité - Projet académique II.1102*
