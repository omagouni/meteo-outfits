# Fiche Google Play · Météo Outfits

Textes prêts à coller dans Play Console (Présence sur le Play Store → Fiche principale).
Limites Google : titre 30 caractères, description courte 80, description longue 4 000.

## Français (fr-FR, langue par défaut)

**Titre** (≤ 30) : `Météo Outfits – Tenue du jour`

**Description courte** (≤ 80) : `Ta tenue selon la météo, avec tes vêtements. Et assortie à tes amis.`

**Description longue** :

```
Chaque matin, la même question : qu'est-ce que je mets ? Météo Outfits y répond avec tes propres vêtements et la météo du jour.

👚 TA GARDE-ROBE, TES TENUES
Ajoute tes vêtements en quelques secondes : colle le lien de la boutique et l'appli récupère le nom et la photo. L'appli compose ensuite une tenue adaptée à la température ressentie, à la pluie et au vent, en évitant ce que tu as porté hier.

👯 DES TENUES ASSORTIES AVEC TES AMIS
Un rendez-vous samedi ? Crée-le, choisis la ville et le jour : l'appli regarde la météo là-bas et propose deux tenues coordonnées en couleurs, une dans ta garde-robe et une dans celle de ton ami. Ton ami reçoit un simple lien, sans compte à créer. S'il n'a pas encore sa garde-robe, il choisit juste ses couleurs.

🌦️ LA MÉTÉO QUI COMPTE POUR S'HABILLER
Température ressentie, risque de pluie, vent, écart entre le matin et le soir, prévisions sur 5 jours.

🔔 CHAQUE MATIN, SANS OUVRIR L'APPLI
Un widget sur ton écran d'accueil et une notification le matin avec ta tenue du jour.

🔒 TES DONNÉES RESTENT CHEZ TOI
Pas de compte, pas d'inscription. Ta garde-robe est stockée sur ton téléphone.

Gratuit. Pensé pour porter ce qu'on a déjà.
```

**Catégorie** : Style de vie · **Tags** : mode, météo, garde-robe, tenue
**Coordonnées** : e-mail de contact (obligatoire) · **Politique de confidentialité** : https://omagouni.github.io/meteo-outfits/privacy.html

## English (en-US)

**Title**: `Météo Outfits – Daily outfit`

**Short description**: `Your outfit for the weather, from your own clothes. Matched with friends.`

**Full description**:

```
Every morning, the same question: what do I wear? Météo Outfits answers it with your own clothes and today's weather.

👚 YOUR WARDROBE, YOUR OUTFITS
Add clothes in seconds: paste the shop link and the app grabs the name and photo. The app then builds an outfit for the feels-like temperature, rain and wind, avoiding what you wore yesterday.

👯 MATCHING OUTFITS WITH FRIENDS
Meeting on Saturday? Create the meetup, pick the city and day: the app checks the weather there and suggests two color-coordinated outfits, one from your wardrobe and one from your friend's. Your friend gets a simple link, no account needed. No wardrobe yet? They just pick their colors.

🌦️ THE WEATHER THAT MATTERS FOR DRESSING
Feels-like temperature, rain chance, wind, morning-to-evening swing, 5-day forecast.

🔔 EVERY MORNING, WITHOUT OPENING THE APP
A home-screen widget and a morning notification with today's outfit.

🔒 YOUR DATA STAYS WITH YOU
No account, no sign-up. Your wardrobe is stored on your phone.

Free. Made for wearing what you already own.
```

## Formulaire « Sécurité des données » (réponses)

| Question | Réponse |
|---|---|
| L'appli collecte-t-elle des données utilisateur ? | **Non** : la garde-robe et les rendez-vous restent sur l'appareil, aucune donnée n'est envoyée aux serveurs du développeur |
| Données partagées avec des tiers ? | Non (le nom de ville envoyé au service météo n'est pas une donnée personnelle) |
| Données chiffrées en transit ? | Oui (HTTPS) |
| Suppression possible ? | Oui, dans l'appli et en désinstallant |

## Captures d'écran à fournir

Au moins 2 captures téléphone (16:9 ou 9:16, 320 à 3 840 px). Celles du dossier `store/screenshots/` conviennent (1080×2400). Ordre conseillé : météo + tenue, rendez-vous reçu (tenues assorties), garde-robe.
Icône 512×512 : exporter depuis `app/src/main/ic_launcher-playstore.png`.
Image de présentation 1024×500 (obligatoire) : à créer (fond dégradé bleu + logo + « Habillé pour la météo, à deux »).

## Test fermé (nouveau compte perso)

1. Play Console → Tests → Test fermé → créer une version, importer `app/build/outputs/bundle/release/app-release.aab`.
2. Ajouter une liste de testeurs (adresses Gmail) et partager le lien d'adhésion.
3. 12 testeurs inscrits pendant 14 jours en continu, puis demander l'accès à la production.
