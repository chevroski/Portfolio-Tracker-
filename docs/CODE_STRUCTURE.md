# 🏗️ Structure du Code - PortfolioTracker

> **Description détaillée de chaque fichier et son objectif**

---

## 📁 Arborescence Complète

```
progjava/
├── pom.xml
├── docs/
│   ├── PRD.md
│   ├── TECH_STACK.md
│   ├── implementation_plan.md
│   ├── CODE_STRUCTURE.md
│   ├── progress.md
│   ├── rules.md
│   └── UML.md
├── data/
│   ├── portfolios/
│   ├── cache/
│   ├── events/
│   └── config/
└── src/main/
    ├── java/com/portfoliotracker/
    │   ├── App.java
    │   ├── model/
    │   ├── controller/
    │   ├── service/
    │   ├── api/
    │   └── util/
    └── resources/
        ├── fxml/
        ├── css/
        └── images/
```

---

## 📦 Package: model/

### model/enums/AssetType.java
**Objectif**: Définir les types d'actifs supportés
- `STOCK` - Actions boursières (AAPL, MSFT, etc.)
- `CRYPTO` - Cryptomonnaies (BTC, ETH, etc.)

### model/enums/TransactionType.java
**Objectif**: Définir les types de transactions
- `BUY` - Achat d'actif
- `SELL` - Vente d'actif

### model/Transaction.java
**Objectif**: Représenter une transaction unique (achat ou vente)
- Stocke: type, quantité, prix unitaire, date, frais
- Calcule le coût total d'une transaction

### model/Asset.java
**Objectif**: Représenter un actif dans un portefeuille
- Contient la liste des transactions
- Calcule la quantité totale détenue
- Calcule le prix moyen d'achat

### model/Portfolio.java
**Objectif**: Représenter un portefeuille d'investissement
- Contient la liste des actifs
- Gère l'ajout/suppression d'actifs
- Supporte le clonage (deep copy)

### model/Event.java
**Objectif**: Représenter un événement marquant (crash, hack, etc.)
- Peut être global ou lié à un portefeuille spécifique
- Affiché sur les graphiques temporels

### model/PricePoint.java
**Objectif**: Représenter un point de prix dans le temps
- Utilisé pour les graphiques historiques
- Stocke: timestamp + prix

---

## 📦 Package: service/

### service/PersistenceService.java
**Objectif**: Gérer la sauvegarde/chargement des données
- Pattern Singleton
- Sauvegarde en JSON avec Gson
- Gère portfolios et events

### service/CacheService.java
**Objectif**: Cache local des prix historiques
- Pattern Singleton
- Évite les appels API répétitifs
- Prix historiques immuables

### service/MarketDataService.java
**Objectif**: Façade pour tous les accès aux données de marché
- Pattern Singleton
- Vérifie le cache avant d'appeler les APIs
- Route vers le bon client (crypto vs stock)

### service/PortfolioService.java
**Objectif**: Logique métier des portefeuilles
- Pattern Singleton
- CRUD complet des portefeuilles
- Import CSV, clonage, calcul de valeur

### service/AnalysisService.java
**Objectif**: Calculs analytiques et statistiques
- Pattern Singleton
- ROI, P&L, allocation
- Périodes rentables vs déficitaires

---

## 📦 Package: api/

### api/CoinGeckoClient.java
**Objectif**: Client HTTP pour l'API CoinGecko
- Prix actuels des cryptos
- Historique des prix
- Recherche de coins

### api/YahooFinanceClient.java
**Objectif**: Client HTTP pour Yahoo Finance
- Prix actuels des actions
- Historique des prix

### api/ExchangeRateClient.java
**Objectif**: Client HTTP pour les taux de change
- Conversion entre devises (EUR, USD, etc.)

---

## 📦 Package: controller/

### controller/MainController.java
**Objectif**: Contrôleur principal de l'application
- Gère la navigation entre vues
- Liste des portefeuilles dans la sidebar
- Chargement dynamique des vues

### controller/PortfolioController.java
**Objectif**: Contrôleur de la vue portfolio
- Affichage des détails d'un portfolio
- TableView des assets
- Actions: ajouter, supprimer, éditer

### controller/AssetController.java
**Objectif**: Contrôleur du formulaire d'asset
- Validation des inputs
- Création de transactions
- Mode édition/création

### controller/ChartController.java
**Objectif**: Contrôleur des graphiques
- LineChart évolution temporelle
- PieChart allocation
- Sélection de période

---

## 📦 Package: util/

### util/DateUtils.java
**Objectif**: Utilitaires pour les dates
- Formatage pour affichage
- Parsing depuis JSON/API

### util/CurrencyUtils.java
**Objectif**: Utilitaires pour les devises
- Formatage des montants (€, $)
- Symboles de devises

---

## 📄 Fichiers Resources

### resources/fxml/main.fxml
**Objectif**: Layout principal de l'application
- BorderPane avec sidebar gauche
- Zone de contenu centrale
- Toolbar supérieure

### resources/fxml/portfolio-view.fxml
**Objectif**: Vue détaillée d'un portefeuille
- Infos du portfolio
- TableView des assets
- Boutons d'action

### resources/fxml/asset-form.fxml
**Objectif**: Formulaire d'ajout/édition d'asset
- Champs: ticker, type, quantité, prix, date
- Boutons: Sauvegarder, Annuler

### resources/fxml/chart-view.fxml
**Objectif**: Vue des graphiques
- LineChart + PieChart
- Sélecteur de période
- Sélection multi-portfolio

### resources/css/styles.css
**Objectif**: Styles visuels de l'application
- Thème cohérent
- Couleurs des graphiques
- Effets hover

---

## 📄 Fichier Racine

### App.java
**Objectif**: Point d'entrée de l'application
- Étend `Application` (JavaFX)
- Charge le FXML principal
- Configure la fenêtre

---

## 📁 Dossiers Data

### data/portfolios/
- Fichiers `{id}.json` pour chaque portfolio

### data/cache/
- Fichiers `{ticker}_{year}.json` pour les prix

### data/events/
- Fichier `events.json` pour tous les événements

### data/config/
- Fichier `settings.json` pour les préférences

---

*Structure mise à jour le 11 Janvier 2026*
