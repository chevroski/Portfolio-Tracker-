# Rapport Technique — PortfolioTracker

**Auteur :** Adam Houri  
**Date :** Janvier 2026  
**Module :** II.1102 — Programmation Java Avancée

---

## 1. Introduction

PortfolioTracker est une application desktop JavaFX pour le suivi de portefeuilles financiers (cryptomonnaies et actions). Elle permet de visualiser, analyser et gérer ses positions en temps réel.

| Aspect       | Choix technique                      |
| ------------ | ------------------------------------ |
| Architecture | MVC + Services (Singleton)           |
| Interface    | JavaFX (FXML + CSS)                  |
| Persistance  | JSON local (Gson)                    |
| APIs         | Binance, Yahoo Finance, ExchangeRate |

---

## 2. Fonctionnalités implémentées

### 2.1 Fonctionnalités principales

| Fonctionnalité           | Description                                          | Statut |
| ------------------------ | ---------------------------------------------------- | :----: |
| Gestion multi-portfolios | Création, suppression, clonage                       |   ✅    |
| Gestion d'assets         | Ajout/suppression, transactions BUY/SELL/REWARD      |   ✅    |
| Graphiques               | Évolution valeur (1W/1M/3M/1Y), allocation pie chart |   ✅    |
| Import CSV Coinbase      | Parsing automatique des transactions                 |   ✅    |
| Multi-devises            | EUR/USD/GBP/CHF/JPY                                  |   ✅    |

### 2.2 Fonctionnalités avancées

| Fonctionnalité        | Description                         | Statut |
| --------------------- | ----------------------------------- | :----: |
| Events sur graphiques | Marqueurs crash/hack/décision       |   ✅    |
| Analysis              | Profit vs Loss days, Best/Worst day |   ✅    |
| Whale Alerts          | Transactions > $1M                  |   ✅    |
| Chiffrement local     | XOR avec passphrase                 |   ✅    |

![Vue principale](Images/capture%20presentation/exemple%20portfolio.PNG)

---

## 3. Architecture MVC

Le projet suit une architecture **MVC** avec couche Service :

![Architecture MVC](Images/capture%20presentation/Diagramme_MVC.png)

| Package      | Responsabilité                          |
| ------------ | --------------------------------------- |
| `model`      | Entités (Portfolio, Asset, Transaction) |
| `controller` | Orchestration UI                        |
| `service`    | Logique métier (Singleton)              |
| `api`        | Clients HTTP externes                   |

**Services implémentés :** PortfolioService, MarketDataService, PersistenceService, EncryptionService, CacheService, EventService, AnalysisService.

---

## 4. Modèle de données

### Portfolio
- `id`, `name`, `description`, `currency`, `createdAt`
- `assets` : List<Asset>

### Asset
- `ticker`, `name`, `type` (CRYPTO/STOCK)
- `transactions` : List<Transaction>
- Méthodes : `getTotalQuantity()`, `getAverageBuyPrice()`, `getTotalInvested()`

### Transaction
- `type` (BUY/SELL/REWARD/CONVERT)
- `quantity`, `pricePerUnit`, `date`, `fees`

---

## 5. Services clés

### 5.1 Pattern Singleton

```java
public static PortfolioService getInstance() {
    if (instance == null) {
        instance = new PortfolioService();
    }
    return instance;
}
```

### 5.2 Cache à deux niveaux (MarketDataService)

- **Mémoire** : ConcurrentHashMap, TTL 60s
- **Disque** : JSON persistant pour prix USD

### 5.3 Import CSV Coinbase

Parsing automatique des fichiers Coinbase avec création des assets manquants.

---

## 6. Interface utilisateur

| Écran     | FXML                | Description          |
| --------- | ------------------- | -------------------- |
| Main      | main.fxml           | Toolbar + navigation |
| Portfolio | portfolio-view.fxml | Liste assets + P&L   |
| Charts    | chart-view.fxml     | Courbes + allocation |
| Analysis  | analysis-view.fxml  | Whale Alerts + stats |

![Vue Charts](Images/capture%20presentation/partie%20charts%20.PNG)

![Vue Analysis](Images/capture%20presentation/partie%20Analyse.PNG)

---

## 7. Concurrence

Les appels réseau utilisent des **Tasks JavaFX** :

```java
Task<Map<String, Double>> task = new Task<>() {
    @Override
    protected Map<String, Double> call() {
        // Appels API en arrière-plan
    }
};
task.setOnSucceeded(e -> updateUI());
new Thread(task).start();
```

**Avantage** : UI non bloquée pendant les chargements.

---

## 8. Persistance & Chiffrement

### Structure

```
data/
├── portfolios/     # JSON (ou .enc si chiffré)
├── cache/          # Prix historiques
└── events/         # Événements
```

### Chiffrement XOR

```java
private byte[] codeDecode(byte[] input, byte[] secret) {
    byte[] output = new byte[input.length];
    for (int pos = 0; pos < input.length; pos++) {
        output[pos] = (byte) (input[pos] ^ secret[pos % secret.length]);
    }
    return output;
}
```

> ⚠️ XOR = implémentation pédagogique. Production nécessiterait AES-256.

---

## 9. Analysis (fonctionnalité avancée)

### Whale Alerts
- Transactions crypto > $1M des dernières 24h
- Volume total, token le plus actif

### Profit vs Loss Days
- Ratio jours profitables vs déficitaires (30 jours)
- Meilleur et pire jour du mois

![Whale Alerts](Images/capture%20presentation/Whale%20alert.PNG)

---

## 10. Tests

| Classe                | Tests | Objectif                   |
| --------------------- | :---: | -------------------------- |
| AssetTest             |   5   | Calculs financiers         |
| EncryptionServiceTest |   6   | Round-trip encrypt/decrypt |

```bash
mvn test
# Tests run: 11, Failures: 0
```

---

## 11. Limites & Évolutions

| Limite               | Évolution envisagée   |
| -------------------- | --------------------- |
| XOR non sécurisé     | Implémenter AES-256   |
| Cache USD uniquement | Étendre multi-devises |
| Pas de tests UI      | Ajouter TestFX        |

---

## 12. Conclusion

PortfolioTracker répond aux objectifs du projet :
- ✅ Gestion multi-portfolios avec visualisation graphique
- ✅ Import CSV et APIs temps réel
- ✅ Fonctionnalités avancées (Analysis, Whale Alerts, Encryption)
- ✅ Architecture MVC propre et testée
