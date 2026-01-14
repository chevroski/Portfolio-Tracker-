# 📊 PortfolioTracker - Product Requirements Document (PRD)

> **Module**: II.1102 Algorithmics and JAVA Programming  
> **Project**: Financial Portfolio Management Application  
> **Version**: 1.0  
> **Date**: Janvier 2026

---

## 1. Executive Summary

**PortfolioTracker** est une application desktop JavaFX permettant aux utilisateurs de suivre et analyser leurs portefeuilles financiers (actions et cryptomonnaies) avec des visualisations graphiques avancées, des analyses de performance et une intégration avec les APIs financières publiques.

### Vision Produit
> *"Donner aux utilisateurs une vision claire et analytique de leurs investissements pour une prise de décision éclairée."*

---

## 2. Objectifs Produit

| Objectif | Description | Priorité |
|----------|-------------|----------|
| **Suivi Multi-Portfolio** | Gérer plusieurs portefeuilles simultanément | 🔴 Critique |
| **Visualisation Graphique** | Afficher l'évolution des valeurs via LineCharts | 🔴 Critique |
| **Import de Données** | Intégrer APIs (CoinGecko, Yahoo Finance) | 🔴 Critique |
| **Analyse Financière** | Calculs de rentabilité, P&L, allocation | 🟡 Important |
| **Gestion d'Événements** | Marquer des événements (crash, hack, etc.) | 🟢 Souhaité |
| **Persistance Locale** | Sauvegarder les données en JSON/XML | 🔴 Critique |

---

## 3. Utilisateurs Cibles

### Persona Principal: "L'Investisseur Débutant"
- **Profil**: Étudiant ou jeune actif avec quelques investissements
- **Besoins**: Suivre ses investissements, comprendre sa rentabilité
- **Pain Points**: Difficile de visualiser la performance globale

### Persona Secondaire: "Le Crypto Enthusiast"
- **Profil**: Investisseur actif en cryptomonnaies
- **Besoins**: Importer depuis Coinbase/Binance, suivre plusieurs tokens
- **Pain Points**: Fragmentation des données sur plusieurs plateformes

---

## 4. Fonctionnalités Détaillées

### 4.1 Core Features (MVP)

#### 📁 Gestion des Portefeuilles
| Feature | Description |
|---------|-------------|
| Création | Nom, description, devise de référence |
| Modification | Éditer les propriétés du portefeuille |
| Suppression | Supprimer un portefeuille avec confirmation |
| Clonage | Dupliquer un portefeuille existant |

#### 💰 Gestion des Assets
| Feature | Description |
|---------|-------------|
| Ajout d'asset | Acheter un actif (ticker, quantité, prix, date) |
| Vente d'asset | Retirer un actif avec calcul P&L |
| Types supportés | Actions (AAPL, MSFT...) et Crypto (BTC, ETH...) |
| Recherche | Autocomplete via API externe |

#### 📈 Visualisations
| Feature | Description |
|---------|-------------|
| LineChart temporel | Évolution de la valeur sur axe X (temps) / Y (valeur) |
| PieChart allocation | Répartition des actifs par catégorie |
| Multi-portfolio | Affichage superposé ou séparé |
| Sélection période | Jour, Semaine, Mois, Année, Custom |

#### 🔌 Intégration APIs
| API | Usage | Type |
|-----|-------|------|
| CoinGecko | Prix crypto temps réel + historique | REST/JSON |
| Yahoo Finance (yfinance) | Prix actions | REST/JSON |
| ExchangeRate-API | Conversion devises | REST/JSON |

### 4.2 Advanced Features (Bonus)

#### 📊 Analyses Avancées
- Calcul du ROI (Return on Investment)
- P&L réalisé vs non-réalisé
- Statistiques: périodes rentables vs déficitaires
- Estimation fiscale (plus-values)

#### 🔔 Monitoring (Optionnel)
- Suivi d'adresses blockchain publiques
- Création de portefeuilles "shadow" pour tiers

#### 🐋 Whale Hunting (Optionnel)
- Alertes sur grosses transactions blockchain
- Filtrage par blockchain ou tokens suivis

#### 🔐 Encryption (Optionnel)
- Chiffrement des données locales (AES-256)
- Demande de passphrase au démarrage

---

## 5. Exigences Non-Fonctionnelles

| Catégorie | Exigence |
|-----------|----------|
| **Performance** | Chargement UI < 2 secondes |
| **Stockage** | Cache local des prix historiques (éviter appels répétés) |
| **UX** | Interface intuitive, responsive |
| **Compatibilité** | Windows, macOS, Linux (JDK 17+) |
| **Langue** | Interface en français ou anglais |

---

## 6. Contraintes Techniques

| Contrainte | Détail |
|------------|--------|
| Langage | Java 17+ |
| Framework UI | JavaFX 21+ |
| Build Tool | Maven ou Gradle |
| APIs | REST uniquement, pas de WebSocket requis |
| Stockage | Fichiers locaux JSON/XML (pas de BDD) |

---

## 7. Critères de Succès

- [ ] L'utilisateur peut créer et gérer plusieurs portefeuilles
- [ ] Les graphiques affichent correctement l'évolution temporelle
- [ ] L'import depuis CSV (format exchange) fonctionne
- [ ] Les données sont persistées et rechargées correctement
- [ ] L'interface est fluide et réactive

---

## 8. Hors Scope (V1)

- Trading automatique
- Notifications push/email
- Application mobile
- Synchronisation cloud
- Authentification utilisateur multi-comptes

---

## 9. Glossaire

| Terme | Définition |
|-------|------------|
| **Portfolio** | Ensemble d'actifs financiers détenus |
| **Asset** | Actif individuel (action ou crypto) |
| **Ticker** | Symbole boursier (ex: AAPL, BTC) |
| **P&L** | Profit and Loss (Gains et Pertes) |
| **ROI** | Return on Investment (Rendement) |
| **Whale** | Compte détenant un grand volume de tokens |
| **Fiat** | Monnaie nationale (EUR, USD) |

---

## 10. Livrables Projet

| Date | Livrable |
|------|----------|
| 9 Janvier 2026 | Modélisation UML |
| 15 Janvier 2026 | Documentation technique + Code source |
| 16 Janvier 2026 | Soutenance orale |

---

*Document rédigé pour le module II.1102 - A.U. 2025-2026*
