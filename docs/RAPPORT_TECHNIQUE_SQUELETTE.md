# RAPPORT TECHNIQUE : PortfolioTracker

**Auteur :** [Ton Nom]
**Date :** Janvier 2026
**Cours :** Programmation Java Avancée

---

## 1. Introduction
Ce document détaille la conception et l'implémentation de **PortfolioTracker**, une application de bureau permettant le suivi en temps réel d'actifs financiers (cryptomonnaies, actions).
L'objectif était de créer une solution performante, sécurisée et ergonomique, surpassant les tableurs traditionnels.

---

## 2. Spécifications Fonctionnelles
L'application couvre l'ensemble des besoins essentiels d'un investisseur particulier.

| Catégorie | Fonctionnalité | Description Technique | État |
| :--- | :--- | :--- | :---: |
| **Core** | Portfolio Management | Création/Édition de multiples portefeuilles | ✅ |
| **Core** | Asset Tracking | Support Crypto (CoinGecko) & Stocks (Yahoo) | ✅ |
| **Analysis** | Performance Metrics | Calcul P&L (Profit & Loss), ROI global | ✅ |
| **UI/UX** | Dynamic Charts | Graphiques interactifs (1M, 3M, 1Y) | ✅ |
| **Data** | Auto-Price Fetch | Récupération automatique du prix à l'ajout | ✅ |
| **Security** | Local Encryption | Chiffrement des données sensibles (XOR) | ✅ |
| **System** | Offline Mode | Support partiel via cache local | ✅ |

---

## 3. Architecture Logicielle
Le projet respecte scrupuleusement le pattern architectural **MVC (Model-View-Controller)** pour garantir la maintenabilité et la testabilité.

### 3.1 Structure MVC
*   **Model (`com.portfoliotracker.model`)** : POJOs anémiques (`Asset`, `Portfolio`) représentant les données métier. Aucune dépendance à JavaFX.
*   **View (`resources/fxml`)** : Interface utilisateur déclarative en FXML. Séparation totale du design et du code.
*   **Controller (`com.portfoliotracker.controller`)** : Orchestration des interactions utilisateur et déléguation aux Services.

*(Insérer ici un diagramme de classe UML simplifié)*

### 3.2 Service Layer & Singleton
La logique métier complexe est encapsulée dans des **Services** (`PortfolioService`, `MarketDataService`).
*   **Design Pattern :** Singleton.
*   **Objectif :** Garantir une instance unique des données en mémoire et un accès thread-safe.

---

## 4. Détails d'Implémentation Avancés

### 4.1 Concurrence & Asynchronisme
Pour éviter le gel de l'interface (UI Freeze) lors des appels API réseaux, l'application utilise le modèle de concurrence JavaFX.
*   **Technologie :** `javafx.concurrent.Task<T>`
*   **Stratégie :** Les requêtes HTTP (I/O blocking) sont exécutées dans un *background thread*. Le callback `setOnSucceeded` met à jour l'UI sur le *JavaFX Application Thread*.

### 4.2 Stratégie de Caching (Optimisation)
Afin de réduire la latence et respecter les limites d'API (Rate Limits) :
1.  **Write-Through :** Chaque donnée historique téléchargée est immédiatement sérialisée en JSON localement.
2.  **Read Strategy :** L'application vérifie toujours la présence du cache disque (O(1)) avant de tenter un appel réseau.

---

## 5. Sécurité et Persistance

### 5.1 Stockage de Données
*   Format : JSON (bibliothèque GSON).
*   Avantage : Portabilité et lisibilité humaine (en mode clair).

### 5.2 Chiffrement (EncryptionService)
L'application intègre un module de sécurité permettant de chiffrer les fichiers de portefeuille.
*   **Algorithme :** XOR Cipher (démonstration académique).
*   **Implémentation :** Manipulation de byte-tream pour obfusquer le contenu JSON.

---

## 6. Qualité et Tests
Une stratégie de tests unitaires a été mise en place avec **JUnit 5**.

### 6.1 Couverture
Les composants critiques sont couverts par des tests automatisés :
*   `AssetTest` : Validation des calculs financiers (valeur totale, coût moyen).
*   `EncryptionServiceTest` : Vérification que `Decrypt(Encrypt(data)) == data`.

---

## 7. Conclusion
PortfolioTracker démontre une maîtrise des concepts avancés de Java : **Programmation Orientée Objet**, **Programmation Fonctionnelle (Streams)**, **Concurrence** et **Architecture Multicouche**.
Le projet est livrable, documenté et prêt pour une évolution future (ex: portage Mobile).

---

## Annexes
*   *Annexe A : Manuel Utilisateur (Extraits)*
*   *Annexe B : Extrait de code (ChartController)*
