# 📘 Manuel Utilisateur - PortfolioTracker

> **Application**: Financial Portfolio Manager  
> **Version**: 1.0  
> **Langage UI**: Anglais  
> **Thème**: Dark Mode

---

## 🚀 Démarrage de l'Application

### Prérequis
- Java 17+ installé
- Connexion internet (pour les prix en temps réel)

### Lancement
```bash
cd progjava
mvn compile
mvn javafx:run
```

---

## 🔐 Écran de Démarrage - Encryption

Au lancement, un dialog de passphrase apparaît:

| Bouton | Action |
|--------|--------|
| **Unlock** | Active le chiffrement XOR pour vos données |
| **Skip** | Mode sans chiffrement (recommandé pour tester) |

> ⚠️ **Note**: Si vous activez l'encryption, mémorisez votre passphrase !

---

## 🏠 Écran Principal

### Structure de l'Interface

```
┌─────────────────────────────────────────────────────────┐
│  [New Portfolio] [Import CSV] [Charts]      Status Bar  │  ← Toolbar
├──────────────┬──────────────────────────────────────────┤
│              │                                          │
│  Portfolios  │           Zone de Contenu               │
│  ──────────  │                                          │
│  ● My Crypto │    (Portfolio sélectionné ici)          │
│  ● Actions   │                                          │
│              │                                          │
└──────────────┴──────────────────────────────────────────┘
     Sidebar                    Content Area
```

---

## 📂 Créer un Portfolio

1. Cliquez sur **New Portfolio**
2. Remplissez le formulaire:
   - **Name**: Nom du portfolio (ex: "Crypto 2024")
   - **Description**: Description optionnelle
   - **Currency**: EUR, USD, GBP, ou CHF
3. Cliquez **Create**

→ Le portfolio apparaît dans la sidebar à gauche

---

## 💰 Ajouter un Asset

1. Sélectionnez un portfolio dans la sidebar
2. Cliquez **Add Asset**
3. Remplissez le formulaire:
   - **Ticker**: BTC, ETH, SOL, AAPL, MSFT...
   - **Name**: Bitcoin, Ethereum, Solana...
   - **Type**: CRYPTO ou STOCK
   - **Quantity**: Quantité achetée (ex: 0.5)
   - **Price per unit**: Prix unitaire à l'achat
   - **Date**: Date de l'achat
   - **Fees**: Frais de transaction
4. Cliquez **Save**

---

## 📥 Import CSV Coinbase

1. Sélectionnez un portfolio
2. Cliquez **Import CSV**
3. Sélectionnez votre fichier `Coinbase.csv`
4. L'application importe automatiquement:
   - Toutes les transactions (BUY, SELL, CONVERT, REWARD)
   - Les prix, quantités, et frais

### Format CSV Supporté

```
Lignes 1-7: En-têtes Coinbase (ignorées)
Ligne 8: Colonnes
Ligne 9+: Données
```

Colonnes utilisées:
- Timestamp, Transaction Type, Asset
- Quantity Transacted, Spot Price at Transaction
- Fees, Notes

---

## 📊 Vue Portfolio

Après avoir sélectionné un portfolio:

### En-tête
- **Nom du portfolio**
- **Valeur totale** en temps réel
- **P&L** (Profit & Loss) avec pourcentage ROI

### Tableau des Assets

| Colonne | Description |
|---------|-------------|
| Ticker | Symbole de l'asset |
| Name | Nom complet |
| Type | CRYPTO ou STOCK |
| Quantity | Quantité totale possédée |
| Avg Price | Prix moyen d'achat pondéré |
| Current Price | Prix actuel (API) |
| Value | Valeur actuelle |
| P&L | Profit ou perte |

### Actions

| Bouton | Action |
|--------|--------|
| **Add Asset** | Ajouter un nouvel asset |
| **Remove Asset** | Supprimer l'asset sélectionné |
| **Clone** | Dupliquer le portfolio |
| **Delete** | Supprimer le portfolio |

---

## 📈 Graphiques (Charts)

Cliquez sur **Charts** dans la toolbar:

### LineChart - Évolution de la Valeur
- Affiche l'évolution de chaque asset
- Périodes: 1W, 1M, 3M, 1Y
- Sélectionnez le portfolio dans le dropdown

### PieChart - Allocation
- Répartition par asset (en %)
- Affichage visuel de la diversification

---

## 🔧 Fonctionnalités Techniques

### APIs Utilisées

| API | Usage | Limite |
|-----|-------|--------|
| CoinGecko | Prix crypto | ~30 req/min |
| Yahoo Finance | Prix actions | Non officiel |
| ExchangeRate | Conversion devises | 1500/mois |

### Cryptos Supportées

BTC, ETH, SOL, LTC, LINK, ADA, DOT, XRP, DOGE, AVAX

### Stockage des Données

```
data/
├── portfolios/      ← Fichiers JSON des portfolios
├── cache/           ← Cache des prix
├── events/          ← Événements timeline
└── config/          ← Configuration
```

---

## 🎯 Cas d'Usage Type

### Scénario: Tracker mes cryptos

1. **Créer** → "My Crypto Portfolio" (EUR)
2. **Import** → Fichier Coinbase CSV
3. **Voir** → Tableau avec BTC, ETH, SOL...
4. **Analyser** → Charts pour voir l'évolution
5. **P&L** → Rouge/Vert selon profit/perte

### Scénario: Ajout manuel

1. **Créer** → "Manual Trades" (USD)
2. **Add Asset** → BTC, 0.1, $35,000
3. **Add Asset** → ETH, 2.5, $2,500
4. **Voir** → Valeur totale calculée

---

## ⌨️ Raccourcis

| Action | Comment |
|--------|---------|
| Nouveau portfolio | Bouton "New Portfolio" |
| Importer CSV | Bouton "Import CSV" |
| Voir graphiques | Bouton "Charts" |
| Sélectionner portfolio | Clic dans la sidebar |

---

## ❓ Troubleshooting

| Problème | Solution |
|----------|----------|
| Prix à 0 | Vérifiez votre connexion internet |
| CSV non importé | Vérifiez le format Coinbase |
| App freeze | Rate limit API, attendez 1 min |
| Données perdues | Oubli de passphrase → données chiffrées |

---

## 📞 Support

Projet académique - JavaFX Portfolio Tracker  
Stack: Java 17 + JavaFX 21 + Maven + Gson

---

*Manuel Utilisateur v1.0 - Janvier 2026*
