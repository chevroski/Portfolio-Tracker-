# 📹 Script Vidéo de Présentation

## Durée Totale: ~12-15 minutes
- **Partie 1: Démo App** (~6 min)
- **Partie 2: Explication Code** (~6 min)

---

# 🎬 PARTIE 1: DÉMONSTRATION DE L'APPLICATION

## INTRO (30 sec)
**[WEBCAM - Ton visage]**

- "Bonjour, je suis [Prénom] et je vais vous présenter PortfolioTracker"
- "C'est une application de suivi de portefeuilles financiers"
- "Elle permet de tracker ses cryptos, actions et ETFs en temps réel"
- "Je vais d'abord vous montrer l'application en action"

---

## ÉCRAN PRINCIPAL (45 sec)
**[ÉCRAN - App lancée, vue principale]**

- "Voici l'écran principal"
- "En haut, on a la toolbar avec les actions principales"
- "À droite, le sélecteur de portfolio"
- **[Clique sur le sélecteur]** "J'ai plusieurs portfolios déjà créés"
- **[Sélectionne 'Crypto Hodler']** "Je sélectionne mon portfolio crypto"

---

## VUE PORTFOLIO (1 min)
**[ÉCRAN - Portfolio view avec assets]**

- "On voit tous mes assets: Bitcoin, Ethereum, Solana..."
- "Les prix sont récupérés en temps réel via les APIs"
- **[Pointe la colonne Value]** "La valeur actuelle de chaque asset"
- **[Pointe la colonne P&L]** "Le profit ou la perte, en vert si positif, rouge si négatif"
- **[Pointe le total en haut]** "Et ici le total du portfolio: 66 000 euros"
- **[Pointe le P&L total]** "Avec un profit total de 36 000 euros, soit +193%"
- "Toutes les valeurs sont automatiquement converties dans la devise de référence du portfolio, ici l'Euro"

---

## VUE GRAPHIQUES (1 min 30)
**[Clique sur le bouton Charts]**

- "Maintenant les graphiques"
- **[Attend que ça charge]** "Le graphique montre l'évolution de la valeur sur le temps"
- **[Clique 1M]** "Je peux voir sur 1 mois"
- **[Clique 3M]** "3 mois"
- **[Clique 1Y]** "Ou une année complète"
- **[Pointe un flag/marqueur sur le graphique si visible]** "On peut aussi afficher des événements sur le graphique, comme un crash ou une décision de justice"
- **[Pointe le Profit Days]** "Ici le pourcentage de jours en profit: 56%"
- **[Clique sur BTC]** "Je peux aussi voir un asset individuel"
- **[Pointe le PieChart]** "La répartition des assets en camembert"
- **[Clique Compare All]** "Et ici, je peux comparer tous mes portfolios sur le même graphique"

---

## AJOUT D'ASSET (1 min)
**[Reviens sur Portfolio view, clique Add Asset]**

- "Pour ajouter un nouvel asset"
- **[Tape "BTC" dans Ticker]** "Je tape le ticker, par exemple BTC"
- **[Clique ailleurs ou sur Fetch]** "Et le prix actuel est récupéré automatiquement"
- **[Montre le champ prix rempli]** "Voilà, 94 000 euros pour 1 Bitcoin"
- **[Tape une quantité]** "Je mets la quantité que je possède"
- "Je peux aussi entrer un prix personnalisé si c'est un achat passé"
- **[Cancel]** "Je vais annuler pour l'instant"

---

## FONCTIONNALITÉS AVANCÉES (45 sec)
**[Reviens sur la vue Portfolio]**

- **[Clic droit sur le portfolio actuel ou bouton Clone]** "Je peux aussi cloner un portfolio existant"
- "C'est utile pour faire des simulations sans modifier l'original"
- **[Clique sur Import ou menu File -> Import]** "Et surtout, je peux importer mon historique depuis Coinbase"
- **[Montre le fichier CSV ou la fenêtre d'import]** "Via un fichier CSV exporté depuis la plateforme"
- "Ça évite de ressaisir manuellement des centaines de transactions"

---

## CHIFFREMENT (30 sec)
**[Clique sur le cadenas ou Settings]**

- "L'application propose aussi le chiffrement des données"
- "Je peux définir une passphrase"
- "Mes portfolios seront alors chiffrés sur le disque"
- "Personne ne peut les lire sans le mot de passe"

---

## WHALE ALERTS (30 sec)
**[Clique sur Analysis]**

- "Dernière fonctionnalité: les Whale Alerts"
- "Ce sont les grosses transactions crypto, plus d'un million de dollars"
- "Récupérées via l'API Whale Alert"
- **[Pointe les stats]** "On voit le volume des dernières 24h et le top token"

## ANALYSE DU PORTFOLIO (30 sec)
**[Toujours dans Analysis]**

- "Juste en dessous, on a l'analyse du portfolio"
- **[Pointe le graphique barre/ratio]** "Ici, le ratio de jours en profit vs en perte sur 30 jours"
- **[Pointe les cartes Best/Worst Day]** "Et là, le meilleur et le pire jour du mois"

---

## TRANSITION (15 sec)
**[WEBCAM - Ton visage]**

- "Voilà pour la démonstration de l'application"
- "Maintenant, regardons comment c'est construit"
- "Je vais vous montrer 3 points clés du code"

---

# 🎬 PARTIE 2: ENGINEERING & ARCHITECTURE (6-7 min)

> **Note:** Ce script adopte un ton "Ingénieur Senior". Il met en avant les choix d'architecture, les design patterns et la gestion de la complexité.

---

## INTRO ARCHITECTURE (1 min)
**[ÉCRAN - IDE Vue globale, tous les packages réduits sauf la racine]**

Dis:
> "Passons à l'ingénierie sous-jacente. Pour ce projet, mon objectif était de concevoir une architecture **robuste, maintenable et scalable**."
>
> "J'ai opté pour une architecture **MVC stricte** afin de garantir une séparation claire des responsabilités (Separation of Concerns)."

**[ACTION: Déploie les packages `model`, `view`, `controller` un par un]**

Dis:
> "Cette structure découple la logique métier de l'interface utilisateur. Cela permet non seulement de faciliter les tests unitaires, mais aussi d'envisager une migration future de la vue (par exemple vers le Web) sans réécrire le cœur logique."

---

## DEEP DIVE 1: DESIGN PATTERNS & SERVICES (2 min)

Dis:
> "Au niveau de la couche Service, j'ai implémenté le **Pattern Singleton**."
> "Pour garantir un point d'accès centralisé et thread-safe."
> "C'est pratique ici car chaque service orchestre une responsabilité unique."
> "Par exemple `PortfolioService` centralise le CRUD, la persistance et l'accès aux prix."
> "Du coup, tous les contrôleurs réutilisent la même logique métier."

**[CODE À MONTRER: `src/main/java/com/portfoliotracker/service/PortfolioService.java` lignes 18-33]**
```java
public class PortfolioService {
    private static PortfolioService instance;
    private final PersistenceService persistenceService;
    private final MarketDataService marketDataService;

    private PortfolioService() {
        this.persistenceService = PersistenceService.getInstance();
        this.marketDataService = MarketDataService.getInstance();
    }

    public static PortfolioService getInstance() {
        if (instance == null) {
            instance = new PortfolioService();
        }
        return instance;
    }
}
```

Dis:
> "J'utilise aussi l'API **Stream** de Java pour manipuler les données."
> "Ça réduit le bruit et rend les agrégations lisibles."
> "Ici, on calcule en 3 étapes: somme du volume, regroupement par token, puis sélection du top."

**[CODE À MONTRER: `src/main/java/com/portfoliotracker/controller/AnalysisController.java` lignes 87-105]**
```java
private void updateStats(List<WhaleAlertClient.WhaleTransaction> transactions) {
    double totalVolume = transactions.stream().mapToDouble(t -> t.usdValue).sum();

    String topToken = transactions.stream()
            .collect(java.util.stream.Collectors.groupingBy(t -> t.symbol, 
                    java.util.stream.Collectors.summingDouble(t -> t.usdValue)))
            .entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey)
            .orElse("BTC");
    topTokenLabel.setText(topToken);
}
```

---

## DEEP DIVE 1B: MODÈLE & CALCULS FINANCIERS (1 min 30)

Dis:
> "Le cœur du projet, c'est le modèle de données."
> "Chaque `Asset` contient l'historique des transactions, et on en déduit les métriques."
> "Ces calculs sont concentrés dans le modèle pour éviter toute duplication côté UI."

**[CODE À MONTRER: `src/main/java/com/portfoliotracker/model/Asset.java` lignes 37-70]**
```java
public double getTotalQuantity() {
    double total = 0;
    for (Transaction t : transactions) {
        if (t.getType() == TransactionType.BUY || t.getType() == TransactionType.REWARD) {
            total += t.getQuantity();
        } else if (t.getType() == TransactionType.SELL) {
            total -= t.getQuantity();
        }
    }
    return total;
}

public double getAverageBuyPrice() {
    double totalCost = 0;
    double totalQuantity = 0;
    for (Transaction t : transactions) {
        if (t.getType() == TransactionType.BUY) {
            totalCost += t.getQuantity() * t.getPricePerUnit();
            totalQuantity += t.getQuantity();
        }
    }
    if (totalQuantity == 0) return 0;
    return totalCost / totalQuantity;
}

public double getTotalInvested() {
    double total = 0;
    for (Transaction t : transactions) {
        if (t.getType() == TransactionType.BUY) {
            total += t.getTotalCost();
        }
    }
    return total;
}
```

Dis:
> "L'avantage, c'est que chaque écran réutilise la même source de vérité."
> "Ça garantit des chiffres cohérents entre le portfolio, les charts et l'analyse."

---

## DEEP DIVE 2: CONCURRENCE & MULTITHREADING (2 min)

Dis:
> "Le défi d'une UI réactive, c'est de ne jamais bloquer le thread principal."
> "Voici la solution technique avec une `Task` JavaFX."
> "Je récupère les prix en arrière-plan et je mets à jour l'UI uniquement à la fin."
> "En cas d'erreur réseau, je sécurise avec une valeur 0 et l'app reste fluide."

**[CODE À MONTRER: `src/main/java/com/portfoliotracker/controller/PortfolioController.java` lignes 92-123]**
```java
private void loadPricesAsync() {
    Task<Map<String, Double>> task = new Task<>() {
        @Override
        protected Map<String, Double> call() {
            Map<String, Double> prices = new HashMap<>();
            for (Asset asset : currentPortfolio.getAssets()) {
                double price = marketDataService.getPrice(
                        asset.getTicker(), asset.getType(), currentPortfolio.getCurrency());
                prices.put(asset.getTicker(), price);
            }
            return prices;
        }
    };

    task.setOnSucceeded(e -> {
        priceCache.clear();
        priceCache.putAll(task.getValue());
        refreshTable();
        updateSummary();
    });

    new Thread(task).start();
}
```

Dis:
> "La méthode `call` est en arrière-plan. `setOnSucceeded` met à jour l'interface."
> "C'est ce qui évite les freezes et garantit une UX agréable."

---

## DEEP DIVE 3: OPTIMISATION & CACHE (2 min)

Dis:
> "Pour l'optimisation, j'utilise une stratégie de cache fichier."
> "Complexité O(1) si le fichier existe."
> "Il y a un cache mémoire + un cache disque JSON."
> "Le cache disque évite de redemander les prix historiques, qui ne changent pas."

**[CODE À MONTRER: `src/main/java/com/portfoliotracker/service/CacheService.java` lignes 45-58]**
```java
public void cachePrice(String ticker, LocalDate date, double price) {
    memoryCache.computeIfAbsent(ticker, k -> new HashMap<>()).put(date, price);
    saveCacheToFile(ticker);
}

public Optional<Double> getCachedPrice(String ticker, LocalDate date) {
    if (!memoryCache.containsKey(ticker)) {
        loadCacheFromFile(ticker);
    }
    Map<LocalDate, Double> tickerCache = memoryCache.get(ticker);
    if (tickerCache != null && tickerCache.containsKey(date)) {
        return Optional.of(tickerCache.get(date));
    }
    return Optional.empty();
}
```

Dis:
> "C'est ce qui permet à l'application de démarrer instantanément."
> "En plus, le `MarketDataService` garde un cache court terme pour éviter les appels répétés."

**[CODE À MONTRER: `src/main/java/com/portfoliotracker/service/MarketDataService.java` lignes 60-97]**
```java
public double getPrice(String ticker, AssetType type, String currency) {
    String cacheKey = ticker.toUpperCase() + "_" + currency.toUpperCase();

    CachedPrice cached = priceCache.get(cacheKey);
    if (cached != null && !cached.isExpired()) {
        return cached.price;
    }

    LocalDate today = LocalDate.now();
    if (currency.equalsIgnoreCase("USD")) {
        Optional<Double> diskCached = cacheService.getCachedPrice(ticker, today);
        if (diskCached.isPresent()) {
            priceCache.put(cacheKey, new CachedPrice(diskCached.get()));
            return diskCached.get();
        }
    }

    double price = 0;
    if (type == AssetType.CRYPTO) {
        String coinId = TICKER_TO_COINGECKO.getOrDefault(ticker.toUpperCase(), ticker.toLowerCase());
        price = coinGeckoClient.getCurrentPrice(coinId, currency);
    } else {
        price = yahooClient.getCurrentPrice(ticker);
        if (price > 0 && !currency.equalsIgnoreCase("USD")) {
            price = exchangeClient.convert(price, "USD", currency);
        }
    }

    if (price > 0) {
        if (currency.equalsIgnoreCase("USD")) {
            cacheService.cachePrice(ticker, today, price);
        }
        priceCache.put(cacheKey, new CachedPrice(price));
    }
    return price;
}
```

Dis:
> "On a donc un cache court terme en mémoire et un cache long terme sur disque."
> "C'est ce qui garantit performance et stabilité."

---

## DEEP DIVE 4: STRATÉGIE DE QUALITÉ (1 min)
**[ÉCRAN - Ouvre `AssetTest.java` ou l'onglet de résultats des tests]**

Dis:
> "Évidemment, une architecture robuste ne vaut rien sans une stratégie de qualité."
>
> "J'ai intégré **JUnit 5** pour garantir la fiabilité des composants critiques, notamment le moteur de calcul financier (`Asset`) et le module de sécurité (`EncryptionService`)."

**[ACTION: Lance les tests (clic droit sur dossier test -> Run 'All Tests')]**

Dis:
> "L'architecture découplée que j'ai présentée permet de tester la logique métier en isolation, sans dépendre de l'interface graphique. C'est ce qu'on appelle du code **Testable by Design**. Cela me permet de garantir la non-régression sur les calculs sensibles de P&L et de chiffrement."

---

## CONCLUSION TECHNIQUE (30 sec)
**[WEBCAM - Ton visage, regard direct et confiant]**

Dis:
> "En conclusion, PortfolioTracker n'est pas juste une interface graphique. C'est une démonstration d'architecture logicielle rigoureuse :"
> "1. Une application stricte des principes SOLID via le MVC."
> "2. Une maîtrise de la concurrence pour une fluidité native."
> "3. Une optimisation des ressources via un caching local."
> "4. Une fiabilité garantie par des tests unitaires critiques."
>
> "Je suis prêt pour vos questions."

---

# ✅ CHECKLIST AVANT DE FILMER

## Préparation App
- [ ] App lancée avec démo data chargée
- [ ] Internet connecté
- [ ] Fenêtre bien dimensionnée
- [ ] Pas de notifications système

## Préparation IDE
- [ ] Fichiers prêts à montrer:
  - [ ] Arborescence projet
  - [ ] `Asset.java`
  - [ ] `Portfolio.java`
  - [ ] `ChartController.java` (ligne ~94)
  - [ ] `CacheService.java`
  - [ ] Dossier `data/cache/`

## Enregistrement
- [ ] Micro testé
- [ ] OBS/Logiciel d'enregistrement configuré
- [ ] Webcam positionnée

---

# 💡 CONSEILS

| ❌ Évite | ✅ Préfère |
|----------|-----------|
| "Euh..." "Donc..." | Pause silencieuse |
| Parler trop vite | Prendre son temps |
| Tout expliquer | Montrer les points importants |
| Lire mot à mot | Reformuler naturellement |
| Se répéter | Avancer |
