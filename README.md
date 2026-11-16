# Météo Outfits

Application Android (Kotlin + Jetpack Compose) qui compose ta tenue du jour avec **tes propres vêtements** selon la météo, et des **tenues assorties avec tes amis** pour vos rendez-vous, selon la météo là où vous vous retrouvez.

Identifiant : `com.meteooutfits.app` · Interface en français et en anglais.

## Fonctionnalités

### Météo et tenue du jour
- Météo actuelle et prévisions sur 5 jours par ville (OpenWeatherMap, descriptions dans la langue du téléphone)
- Tenue basée sur la température **ressentie**, la probabilité de pluie, le vent, l'écart matin/soir
- **Widget** d'écran d'accueil et **notification du matin** (7 h 30) avec la tenue du jour

### Garde-robe
- Vêtements avec nom, catégorie, marque, référence, couleur, chaleur (1–5), imperméable / coupe-vent, notes
- **Import depuis un lien produit** (nom, photo, marque via les balises Open Graph), bouton « Trouver en ligne »
- Recherche, filtre par catégorie, tris ; « Je porte ça aujourd'hui » enregistre ce que tu portes
- Tenue du jour choisie dans ta garde-robe, qui évite ce que tu as porté la veille

### Rendez-vous (tenues assorties) — ce qui n'existe nulle part ailleurs
- Choisis un ami, une ville et un jour : l'appli prend la météo du lieu et compose ta tenue
- **Lien d'invitation sans compte ni serveur** : l'ami ouvre le lien dans l'appli (ou le colle) et obtient sa tenue, coordonnée en couleurs avec la tienne (couleur commune, couleurs voisines, pas de couleurs qui jurent)
- Sans garde-robe, l'ami choisit juste ses couleurs et obtient quand même sa tenue
- Tout est calculé sur le téléphone ; les rendez-vous sont stockés localement

### Styliste IA (optionnel, versions de développement)
Activé seulement si `ANTHROPIC_API_KEY` est renseignée. Voir `domain/ClaudeOutfitPlanner.kt`. Absent de la version publiée.

## Installation (développement)
1. Cloner le dépôt et l'ouvrir dans Android Studio.
2. Créer une clé gratuite sur https://openweathermap.org/appid
3. Dans `local.properties` (non versionné) :
   ```properties
   WEATHER_API_KEY=ta_cle_ici
   # optionnel : styliste IA
   ANTHROPIC_API_KEY=ta_cle_claude
   ```
4. Synchroniser Gradle puis lancer l'app.

## Publication
- **Signature** : le build release lit `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` dans `local.properties`. Le fichier `keystore/upload-keystore.jks` n'est pas versionné : **sauvegarde-le ailleurs** (sans lui, impossible de mettre à jour l'appli sur Google Play).
- **Bundle** : `./gradlew :app:bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`
- **Fiche store** : textes FR/EN, réponses « Sécurité des données » et étapes du test fermé dans [store/play-listing.md](store/play-listing.md)
- **Politique de confidentialité et page d'invitation** : dossier [docs/](docs/), à publier avec GitHub Pages (Settings → Pages → branche `main`, dossier `/docs`). Les liens d'invitation pointent vers `https://omagouni.github.io/meteo-outfits/m/`.
- **Plan de contenu TikTok** : [marketing/tiktok-plan.md](marketing/tiktok-plan.md)
- **Étude de marché** : [reports/](reports/)

## Tests
```bash
./gradlew :app:testDebugUnitTest
```

## Architecture
```
data/
  model/     ClothingItem, DayWeather, Meetup (+ lien d'invitation), réponses API
  local/     JsonFileStore, WardrobeRepository, MeetupRepository, OutfitSnapshotStore
  remote/    Retrofit OpenWeatherMap, WeatherRepository, LinkPreviewFetcher
domain/      ForecastAggregator, OutfitRecommender, WardrobeOutfitPlanner,
             ColorFamily, OutfitHarmony, MatchyPlanner (tenues coordonnées)
viewmodel/   WeatherViewModel, WardrobeViewModel, MeetupViewModel
ui/          WeatherScreen, wardrobe/, meetup/, components/, UiText (textes traduits)
widget/      OutfitWidget (Glance), MorningOutfitWorker (WorkManager)
```

## Stack
Kotlin · Jetpack Compose · Material 3 · Retrofit · Coil · Glance · WorkManager · MVVM
