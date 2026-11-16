# Météo & Outfits

Application Android (Kotlin + Jetpack Compose) qui récupère la météo via l'API OpenWeatherMap et suggère une tenue adaptée.

## Fonctionnalités
- Météo actuelle par ville (OpenWeatherMap)
- Prévisions sur 5 jours : choisis le jour (Now / Today / Tomorrow / …) pour voir la tenue adaptée
- Suggestion de tenue basée sur la température **ressentie**, la probabilité de pluie, le vent
- Conseil "couche amovible" quand l'écart de température dans la journée est important
- Dernière ville recherchée mémorisée

### Garde-robe
- Ajoute tes vêtements : nom, catégorie, marque, **référence**, couleur, chaleur (1–5), imperméable / coupe-vent, notes
- **Import depuis un lien produit** : colle l'URL d'une boutique, l'app récupère le nom, la photo et la marque (balises Open Graph)
- Bouton **"Find it online"** pour retrouver un vêtement à partir de sa marque + référence
- Recherche, filtre par catégorie et tri (récents, nom, chaleur, moins portés, portés récemment…)
- **Tenue du jour depuis ta garde-robe** : pour le jour sélectionné, l'app choisit manteau / haut / bas / chaussures / accessoires selon le ressenti, la pluie et le vent, évite ce que tu as porté hier, et signale ce qui manque
- "I'm wearing this today" enregistre ce que tu portes (compteur + date)

## Prochaines étapes
- [x] Garde-robe : ajouter ses propres vêtements (avec références/liens trouvés sur Internet)
- [x] Tri et sélection automatique de la tenue du jour à partir de la garde-robe
- [ ] Suggestions enrichies par un LLM

### Brancher un LLM
Le choix de tenue passe par l'interface `OutfitPlanner` (`domain/WardrobeOutfitPlanner.kt`).
L'implémentation actuelle, `RuleBasedOutfitPlanner`, est à base de règles. Un planner LLM peut
implémenter la même interface (en envoyant la météo du jour + la garde-robe au modèle) et
retomber sur les règles en cas d'erreur. Il suffit ensuite de changer `planner` dans `WeatherViewModel`.

## Installation
1. Cloner le dépôt et l'ouvrir dans Android Studio.
2. Créer une clé gratuite sur https://openweathermap.org/appid
3. Ajouter dans `local.properties` (fichier non versionné) :
   ```properties
   WEATHER_API_KEY=ta_cle_ici
   ```
4. Synchroniser Gradle puis lancer l'app.

## Tests
```bash
./gradlew :app:testDebugUnitTest
```

## Architecture
```
data/
  model/     ClothingItem, DayWeather, réponses API
  local/     WardrobeRepository (JSON dans le stockage interne)
  remote/    Retrofit OpenWeatherMap, LinkPreviewFetcher
domain/      ForecastAggregator, OutfitRecommender, WardrobeOutfitPlanner
viewmodel/   WeatherViewModel, WardrobeViewModel
ui/          WeatherScreen, wardrobe/, components/
```

## Stack
Kotlin · Jetpack Compose · Material 3 · Retrofit · Coil · MVVM (ViewModel)
