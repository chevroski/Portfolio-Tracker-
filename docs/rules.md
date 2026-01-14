# Règles de Développement Java/JavaFX - PortfolioTracker

## 1. Code Simple et Accessible
- Écrire du code lisible et compréhensible par tous les membres de l'équipe
- Éviter les constructions complexes inutiles
- Privilégier la clarté à la concision excessive
- **PAS DE COMMENTAIRES EXPLICATIFS DANS LE CODE** - le code doit être auto-explicatif

## 2. Architecture MVC Stricte
- **Controllers** : AUCUNE logique métier, uniquement gestion des événements UI
- **Services** : contiennent TOUTE la logique métier
- **Models** : POJO simples avec getters/setters, pas de logique

## 3. Séparation Vue/Logique
- Utiliser FXML pour TOUTES les vues (pas de création de composants en Java)
- Un Controller par fichier FXML
- Les Controllers sont liés aux FXML via `fx:controller`

## 4. Pattern Singleton pour les Services
- Chaque Service est un Singleton accessible via `getInstance()`
- Les Services ne dépendent JAMAIS des Controllers
- Injection des Services dans les Controllers via `initialize()`

## 5. Threading et Appels Asynchrones
- TOUS les appels réseau sur un thread séparé (`Task`, `Platform.runLater`)
- Ne JAMAIS bloquer le JavaFX Application Thread
- Utiliser `CompletableFuture` pour les opérations asynchrones

## 6. Gestion du Cache
- Toujours vérifier le cache AVANT un appel API
- Prix historiques immuables : une fois cachés, ne plus re-télécharger
- Format de cache : JSON avec timestamp

## 7. Persistance JSON
- Utiliser Gson avec `GsonBuilder().setPrettyPrinting().create()`
- Sauvegarder après CHAQUE modification de données
- Gérer les exceptions avec des valeurs par défaut

## 8. Conventions de Nommage
- Classes : `PascalCase` (ex: `PortfolioService`)
- Méthodes/Variables : `camelCase` (ex: `calculateTotalValue`)
- Constantes : `UPPER_SNAKE_CASE` (ex: `API_BASE_URL`)
- Fichiers FXML : `kebab-case` (ex: `portfolio-view.fxml`)

## 9. Gestion des Erreurs
- Afficher les erreurs utilisateur via `Alert` JavaFX
- Logger les erreurs techniques (pas de `printStackTrace()`)
- Toujours fournir un message d'erreur clair

## 10. Style CSS
- Un seul fichier `styles.css`
- Utiliser des classes CSS, pas de styles inline
- Variables CSS pour couleurs et polices réutilisables

## 11. Pas de Documentation Inline
- JavaDoc UNIQUEMENT sur classes publiques et méthodes de Service
- Pas de commentaires évidents dans le code
- Le nom des variables et méthodes doit être suffisamment descriptif
