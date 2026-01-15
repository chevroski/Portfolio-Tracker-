# Rapport Technique — PortfolioTracker

**Auteur :** [Ton Nom]  
**Date :** Janvier 2026  
**Module :** II.1102 — Programmation Java Avancée  
**Version :** 1.0

---

## 1. Introduction
PortfolioTracker est une application desktop JavaFX de suivi de portefeuilles (crypto et actions). Elle fournit un suivi de valeur, des graphiques, des analyses, un import CSV et une persistance locale.

**Objectifs de ce document**
- Décrire l’implémentation réelle (architecture, services, APIs, persistance, tests).
- Donner une vision claire, professionnelle et exploitable pour relecture technique.
- Servir de base courte (8–10 pages) pour le rendu final.

**Sources utilisées**
- Code source du projet (packages `model`, `controller`, `service`, `api`, `util`).
- Vues JavaFX (FXML/CSS).
- Diagrammes UML fournis (classes + séquences).

---

## 2. Périmètre & Fonctionnalités livrées

### 2.1 Fonctionnalités principales
- **Gestion multi-portfolios** : création, suppression, clonage, changement de devise de référence.  
- **Gestion d’assets** : ajout/suppression d’actifs, transactions BUY/SELL/REWARD/CONVERT, calculs de quantité totale, coût moyen et P&L.  
- **Graphiques** : évolution de la valeur sur périodes 1W/1M/3M/1Y, sélection d’un asset, allocation par actif.  
- **Import CSV Coinbase** : import des transactions depuis un CSV Coinbase, création d’actifs manquants, normalisation des types de transaction.  
- **Persistance locale JSON** : sauvegarde automatique des portfolios et événements (JSON chiffré ou clair).  
- **Devise de référence** : conversion et affichage des valeurs dans une devise choisie (EUR/USD/GBP/CHF/JPY).  

### 2.2 Fonctionnalités avancées
- **Events** sur les graphiques : ajout d’événements (crash, hack, décision) et affichage par marqueurs sur la courbe.  
- **Analysis** : profit vs loss days, best/worst day sur 30 jours, synthèse visuelle.  
- **Whale Alerts** : transactions crypto > $1M via API WhaleAlert (ou fallback mock).  
- **Chiffrement local** : activation au démarrage via passphrase, stockage en `.json.enc` (XOR pédagogique).  

### 2.3 Éléments hors périmètre (assumés)
- **Sécurité forte** : le chiffrement XOR est volontairement simplifié (objectif pédagogique).  
- **Historique transactionnel exact** : les courbes représentent la valeur à quantité actuelle, pas un historique “réel” des achats/ventes.  
- **Disponibilité API** : dépendance à des endpoints externes non contractuels (Yahoo, Binance, WhaleAlert).  

> **Images à insérer dans cette section :**
> - **Figure 1** — Capture écran “Main / Portfolio view” (illustrer multi-portfolios + valeurs + P&L).
> - **Figure 2** — Capture écran “Charts view” (courbe + allocation + compare all).
> - **Figure 3** — Capture écran “Analysis view” (Whale Alerts + Profit/Loss days).
> - **Figure 4** — Capture écran “Import CSV” (dialog + exemple de fichier Coinbase).

---

## 3. Architecture logicielle
Le projet suit une architecture **MVC** structurée en couches. L’objectif est de **séparer la présentation**, la **logique métier** et l’**accès aux données** afin de faciliter la maintenance et l’évolution.

**Découpage retenu**
- **Model** : entités métier simples (portfolio, asset, transaction, event).  
- **View** : fichiers FXML + CSS pour l’interface JavaFX.  
- **Controller** : logique d’écran (handlers, binding UI, navigation).  
- **Service Layer** : logique métier + orchestration (calculs, import, persistance, cache).  
- **API Layer** : clients HTTP externes (prix, taux, whale alerts).  

**Raisons du choix**
- Limiter le couplage entre UI et logique métier.  
- Réutiliser la logique (ex. calculs) sans dépendre d’une vue.  
- Préparer un éventuel changement d’UI (Web ou autre).  

**Flux principal (haut niveau)**
1. L’utilisateur interagit avec une **View** (FXML).  
2. Le **Controller** déclenche des actions (ex. ajout d’asset, import CSV).  
3. Les **Services** exécutent la logique métier, appellent la persistance ou les APIs.  
4. Le **Controller** met à jour l’UI avec les résultats.  

> **Image à insérer dans cette section :**
> - **Figure 5** — Diagramme MVC / diagramme des packages (UML fourni).

---

## 4. Modèle de données

### 4.1 Portfolio
Un portefeuille contient un nom, une devise de référence et une liste d’actifs. Il supporte le clonage (deep copy).  
- Classe : `model/Portfolio.java`

### 4.2 Asset
Un actif contient un ticker, un type (STOCK/CRYPTO) et une liste de transactions.  
- Classe : `model/Asset.java`

### 4.3 Transaction
Chaque transaction garde type, quantité, prix, date et frais.  
- Classe : `model/Transaction.java`

### 4.4 Event
Un événement peut être global ou attaché à un portefeuille.  
- Classe : `model/Event.java`

### 4.5 PricePoint
Objet simple pour l’historique de prix (timestamp + prix).  
- Classe : `model/PricePoint.java`

> **Insertion recommandée :** *Diagramme UML simplifié des entités* (figure 2)

---

## 5. Services (logique métier)

### 5.1 PortfolioService
- CRUD de portfolios
- Ajout d’actifs / transactions
- Clonage
- Import CSV Coinbase

**Classe :** `service/PortfolioService.java`

### 5.2 MarketDataService
- Façade vers crypto/stocks
- Cache mémoire (TTL) + cache disque (USD seulement)
- Conversion devise

**Classe :** `service/MarketDataService.java`

### 5.3 PersistenceService
- Sérialisation JSON via Gson
- Fichiers `data/portfolios/*.json` ou `*.json.enc` si chiffré

**Classe :** `service/PersistenceService.java`

### 5.4 EncryptionService
- Chiffrement XOR (démonstration académique)
- Activé via la passphrase au lancement

**Classe :** `service/EncryptionService.java`

### 5.5 CacheService
- Cache disque des prix par ticker et date
- Stocké dans `data/cache/`

**Classe :** `service/CacheService.java`

### 5.6 EventService
- Gestion des événements pour les charts

**Classe :** `service/EventService.java`

### 5.7 AnalysisService
- ROI, PnL, allocation
- Périodes profitables/déficitaires

**Classe :** `service/AnalysisService.java`

---

## 6. APIs externes

### 6.1 Crypto (Binance via CoinGeckoClient)
Le client nommé `CoinGeckoClient` interroge **l’API Binance** pour :
- Prix actuels (`/ticker/price`)
- Historique (`/klines`)

### 6.2 Actions (Yahoo Finance)
Utilisation d’un endpoint Yahoo non-officiel :
- `query1.finance.yahoo.com/v8/finance/chart/{symbol}`

### 6.3 Devises (ExchangeRate)
Conversion entre devises par `api.exchangerate-api.com`.

### 6.4 Whale Alerts
Utilisation de `api.whale-alert.io` avec fallback sur données mockées.

---

## 7. Interface utilisateur (JavaFX)

### 7.1 Écrans principaux
- **Main** : toolbar + sidebar portfolios
- **Portfolio view** : table assets + P&L
- **Charts view** : line chart + pie chart
- **Analysis view** : whale alerts + stats
- **Passphrase dialog** : chiffrement

**Fichiers FXML** : `main.fxml`, `portfolio-view.fxml`, `chart-view.fxml`, `analysis-view.fxml`, `passphrase-dialog.fxml`.

> **Insertion recommandée :** captures d’écran des vues principales (figures 3-6)

---

## 8. Concurrence & performance
Les appels API sont exécutés dans des **Tasks JavaFX** pour ne pas bloquer l’UI :
- `PortfolioController` (chargement prix)
- `ChartController` (historique)
- `AnalysisController` (whale alerts + stats)

---

## 9. Persistance & chiffrement

- Les données sont stockées en JSON dans `data/portfolios/`.
- Si chiffrement activé, sauvegarde sous `*.json.enc`.
- Le chiffrement est un **XOR** simple (objectif pédagogique, pas sécurité production).

---

## 10. Analyse & statistiques

### 10.1 Charts
- Graphique de valeur sur 1W / 1M / 3M / 1Y
- Sélection d’un asset individuel
- Mode “Compare All” pour multi-portfolio

### 10.2 Analysis
- Whale Alerts (transactions > $1M)
- Profit vs Loss days (30 jours)
- Best/Worst day

---

## 11. Tests

### 11.1 Tests unitaires
- `AssetTest` : calculs financiers
- `EncryptionServiceTest` : chiffrement/déchiffrement

### 11.2 Exécution
```bash
mvn test
```

---

## 12. Limites connues
- **CoinGeckoClient** utilise Binance (nom trompeur).
- **Cache disque** limité à USD uniquement.
- **Encryption XOR** non sécurisé pour production.
- **API Yahoo Finance** non officielle (peut changer).

---

## 13. Pistes d’évolution
- Ajout d’un vrai chiffrement (AES-256)
- Historique multi-devises en cache
- Tests UI automatisés (TestFX)
- Export PDF/CSV

---

## Annexes

### A. Exemple d’extrait de code
> *Insérer ici un extrait court de `ChartController` (chargement historique + Task)*

### B. Diagrammes et captures
- Diagramme MVC global
- Diagramme UML simplifié des modèles
- Captures d’écran des vues (Main, Portfolio, Charts, Analysis)
