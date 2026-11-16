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
- [x] Suggestions enrichies par un LLM (Claude)

### Styliste IA (Claude)
Interrupteur **"✨ AI stylist"** sur la carte "From your wardrobe" (visible seulement si une clé est configurée).
Quand il est activé :
1. la tenue calculée par les règles s'affiche tout de suite ;
2. la météo du jour + ta garde-robe (nom, catégorie, chaleur, imperméable, couleur, marque, historique de port)
   sont envoyées à Claude (`claude-opus-5-5`, sortie JSON structurée, effort bas pour la rapidité) ;
3. la tenue de Claude remplace celle des règles, avec une raison par pièce et un conseil de style.

En cas d'erreur (pas de réseau, clé invalide, refus), l'app garde la tenue des règles et l'indique.
Les réponses sont mises en cache par jour + météo + garde-robe pour ne pas repayer le même appel.
Le repli serveur (`fallbacks`) est activé : si un filtre de sécurité refuse la requête, l'API la rejoue sur un autre modèle.

Code : `domain/ClaudeOutfitPlanner.kt` (appel SDK) et `domain/ClaudeOutfitPrompt.kt` (prompt, schéma, validation de la réponse).

**⚠️ Clé API** : la clé est compilée dans l'APK, donc **extractable par toute personne qui a l'APK**.
C'est acceptable pour un usage perso ou du développement, pas pour une app publiée. Pour publier,
fais passer les appels par ton propre serveur (qui détient la clé) et remplace le client dans `ClaudeOutfitPlanner`.

## Installation
1. Cloner le dépôt et l'ouvrir dans Android Studio.
2. Créer une clé gratuite sur https://openweathermap.org/appid
3. Ajouter dans `local.properties` (fichier non versionné) :
   ```properties
   WEATHER_API_KEY=ta_cle_ici
   # optionnel, pour le styliste IA (clé sur https://platform.claude.com)
   ANTHROPIC_API_KEY=ta_cle_claude
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
Kotlin · Jetpack Compose · Material 3 · Retrofit · Coil · MVVM (ViewModel) · Anthropic Java SDK (Claude)
