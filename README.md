# Météo & Outfits

Application Android (Kotlin + Jetpack Compose) qui récupère la météo via l'API OpenWeatherMap et suggère une tenue adaptée.

## Fonctionnalités
- Météo actuelle par ville (OpenWeatherMap)
- Suggestion de tenue selon la température et les conditions

## Prochaines étapes
- [ ] Garde-robe : ajouter ses propres vêtements (avec références/liens trouvés sur Internet)
- [ ] Tri et sélection automatique de la tenue du jour à partir de la garde-robe
- [ ] Suggestions enrichies par un LLM

## Installation
1. Cloner le dépôt et l'ouvrir dans Android Studio.
2. Créer une clé gratuite sur https://openweathermap.org/appid
3. Ajouter dans `local.properties` (fichier non versionné) :
   ```properties
   WEATHER_API_KEY=ta_cle_ici
   ```
4. Synchroniser Gradle puis lancer l'app.

## Stack
Kotlin · Jetpack Compose · Material 3 · Retrofit · Coil · MVVM (ViewModel)
