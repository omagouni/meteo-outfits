# Monétisation et rétention : benchmarks pour une appli mobile mode/lifestyle (tenues météo + garde-robe + amis)

Notes de recherche, 16 novembre 2026. Chaque chiffre a sa source ; la plupart des benchmarks sont **mondiaux** sauf mention « France ». Beaucoup de sources sont des blogs d'éditeurs (vendor blogs) ; les sources primaires (RevenueCat, Fevad, Pinterest, Vinted) sont signalées comme telles.

## 1. Retention benchmarks (D1 / D7 / D30) for lifestyle, shopping and social apps

### Takeaway
Ordre de grandeur réaliste pour une appli lifestyle/shopping : environ 24 % à J1, 10 % à J7, 5 % à J30 ; le social fait un peu mieux à J1 (environ 32 %) mais retombe vers 6 % à J30. Je n'ai trouvé aucune donnée fiable 2025–2026 propre à la France ni de séparation iOS/Android par catégorie.

### Cited Findings
- Médiane Adjust, toutes catégories : J1 26 %, J7 13 %, J30 7 % (cité via un blog, année du jeu de données Adjust non précisée) — [getstream.io, 2026 Guide to App Retention](https://getstream.io/blog/app-retention-guide/)
- Tableau MWM, États-Unis, T3 2025 : **Lifestyle & wellbeing 23,6 % / 9,6 % / 4,8 %** ; **Social & communication 31,9 % / 12,3 % / 5,9 %** (J1/J7/J30) — [MWM glossary, Retention](https://mwm.ai/glossary/retention)
- Shopping/e-commerce : environ 24 % J1, 10–11 % J7, 5–6 % J30 ; marketplaces et petites annonces environ 24 % / 14 % / 8 % — [Prooflytics, D7 and D30 retention benchmarks by category](https://prooflytics.io/blog/d7-d30-retention-benchmarks-by-app-category); [Enable3, App retention benchmarks 2026](https://enable3.io/blog/app-retention-benchmarks-2025)
- Valeurs contradictoires : un blog donne pour le social et la messagerie 55 % / 35 % / 22 %, bien au-dessus de MWM. L'écart est trop grand pour venir seulement des définitions, donc ce chiffre n'est pas fiable — [Enable3](https://enable3.io/blog/app-retention-benchmarks-2025) vs [MWM](https://mwm.ai/glossary/retention)
- Fitness/wellness sur abonnement : J30 de 15 à 22 % (catégorie différente, utilisateurs plus engagés) — [Enable3](https://enable3.io/blog/app-retention-benchmarks-2025)
- Rétention des abonnés : environ 72 % des abonnés annuels résilient au cours de la 1re année en 2026, contre environ 56 % en 2025 ; 35 % des résiliations annuelles ont lieu au mois 1 — [RevenueCat, SOSA 2026 in 10 minutes](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)
- Rétention à 12 mois des abonnés annuels : freemium 28 %, hard paywall 27 % — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)
- Échecs de paiement (churn involontaire), en part des résiliations : Google Play 31 % (28,2 % en 2025), App Store 14 % (15,1 % en 2025) — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)

### Inferences
- Sur 1 000 installations, il faut compter environ 50 utilisateurs encore actifs à J30 dans une appli lifestyle standard. Les fonctions sociales (amis, tenues coordonnées) et l'usage quotidien lié à la météo sont les leviers qui peuvent pousser vers le haut de la fourchette.
- Le churn involontaire beaucoup plus fort sur Android (31 % contre 14 %) justifie d'activer le délai de grâce et la relance de paiement (dunning) de Google Play dès le lancement.

### Gaps
- Pas de benchmark de rétention 2025–2026 propre à la France ou à l'Europe pour le lifestyle. Les rapports AppsFlyer et Adjust ont des filtres par région, mais je n'ai pas pu en extraire les données.
- Pas de séparation iOS/Android par catégorie.
- Pas de donnée chiffrée fiable sur l'effet des publicités sur la rétention.

## 2. Subscription benchmarks (conversion, trial, pricing, churn, RPI/LTV, store commission)

### Takeaway
Pour une appli **freemium**, la médiane de conversion téléchargement → payant est de **2,1 %** et le revenu par installation à J60 de **0,38 $** (RevenueCat 2026, toutes catégories, monde). Un hard paywall fait environ 5 fois mieux en conversion et 8 fois mieux en revenu. La commission des stores est de 15 % pour un petit développeur.

### Cited Findings
- RevenueCat State of Subscription Apps 2026 : plus de 115 000 applis, plus de 16 Md$ de revenus (source primaire) — [RevenueCat blog](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)
- Conversion médiane à J35 (le graphique la nomme « download-to-paid ») : **hard paywall 10,7 % (12,1 % en 2025), freemium 2,1 % (stable)** — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)
- **Revenu par installation à J60 : hard paywall 3,09 $, freemium 0,38 $** — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)
- Conversion médiane essai → payant : 42,5 % pour un essai de 17 à 32 jours, 25,5 % pour moins de 4 jours. 46,5 % des applis utilisent des essais de moins de 4 jours (42,1 % en 2025) — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)
- Essais de 3 jours : 55,4 % des annulations ont lieu à J0 et 84 % entre J0 et J1 — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)
- Social & Lifestyle : catégorie avec la plus forte part d'applis sans essai (43,6 %), conversion tardive après essai d'environ 24–25 %, et 39,4 % des applis qui combinent abonnement et achats uniques ou à vie (extraits fragmentés du rapport) — [RevenueCat SOSA](https://www.revenuecat.com/state-of-subscription-apps); [SQ Magazine](https://sqmagazine.co.uk/revenuecat-statistics/)
- LTV réalisée médiane à 1 an : applis IA 30,16 $, applis non IA 21,37 $ (+41 %). Les abonnements mensuels des applis IA retiennent toutefois nettement moins bien — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)
- Concentration : le top 25 % des applis voit son MRR croître de plus de 80 % par an, le bas 25 % le voit baisser de plus de 33 % ; le top 5 % gagne environ 400 fois le bas 25 % — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026); [Subscription Insider](https://www.subscriptioninsider.com/blog/revenuecat-data-shows-subscription-app-growth-concentrating-at-the-top-news)
- Prix d'applis comparables (garde-robe) :
  - **Acloset** : gratuit jusqu'à 100 vêtements ; Basic 3,99 $/mois ou 28,49 $/an, Premium 9,99 $/mois (US) — [AppGoblin / App Store via recherche](https://apptail.io/app/acloset-ai-fashion-assistant-VeG/united-kingdom)
  - Au Royaume-Uni, Acloset affiche aussi Expert 24,99 £/mois et Premium 59,99 £/an — [AppTail](https://apptail.io/app/acloset-ai-fashion-assistant-VeG/united-kingdom)
  - **Indyx** : gratuit, abonnement « Insider » avec essai de 30 jours ; parmi les fonctions premium, des amis peuvent vous envoyer des tenues — [Indyx blog](https://www.myindyx.com/blog/why-we-are-launching-insider)
- Commissions des stores :
  - Apple Small Business Program : 15 % si le développeur a gagné moins de 1 M$ l'année précédente ; l'inscription se fait sur demande — [RevenueCat, Small Business Program](https://www.revenuecat.com/blog/engineering/small-business-program)
  - Abonnements Apple : 15 % après la 1re année payante, même hors programme — [RevenueCat](https://www.revenuecat.com/blog/engineering/small-business-program)
  - Google Play : 15 % sur les abonnements dès le 1er jour — [Appbot](https://appbot.co/blog/app-developers-apple-google-small-business-programs/)
  - UE : Apple a des conditions spécifiques au DMA (26 % hors programme, 15 % pour les petits développeurs) — [FunnelFox, App Store fees 2026](https://blog.funnelfox.com/apple-app-store-fees-2026-eu-dma/)

### Inferences
- Exemple de calcul pour un abonnement à 2,99 € TTC en France : TVA à 20 % retirée par le store (environ 2,49 € HT), puis 15 % de commission, soit environ **2,12 € net par mois**.
- Avec 2,1 % des installations converties : 2,12 € × 0,021 ≈ 0,045 € par installation et par mois d'abonnement. C'est un ordre de grandeur cohérent avec les 0,38 $ de revenu par installation à J60 de RevenueCat, qui concerne des applis souvent plus chères.
- Les applis garde-robe comparables facturent 3,99 à 9,99 $ par mois, avec des formules annuelles autour de 28 à 60. Un prix de 2,99 €/mois est donc bas ; une formule annuelle (par exemple 19,99 à 24,99 €) avec essai long est cohérente avec les données RevenueCat. Ce prix annuel est une suggestion, non sourcée.

### Gaps
- Pas de médiane de conversion propre à la catégorie Lifestyle, ni de chiffre Europe ou France ; le rapport complet n'a pas pu être lu.
- Pas d'écart de prix médian mensuel/annuel par catégorie.
- Pas de chiffre officiel sur les abonnés ou le revenu d'Indyx, Whering ou Acloset. AppGoblin estime Indyx à moins de 10 000 $ de revenu mensuel, environ 75 000 installations et environ 5 000 utilisateurs actifs par mois, mais ce sont des estimations tierces — [AppGoblin](https://appgoblin.info/apps/1599179405)
- Je n'ai pas confirmé dans la documentation officielle d'Apple et Google si la commission se calcule sur le prix HT ou TTC. La pratique courante est sur le prix hors TVA, à vérifier.

## 3. Fashion affiliate economics in France

### Takeaway
Dans la mode, les commissions vont généralement de **5 à 10 %** du panier, avec des cookies de 15 à 30 jours chez les marques et de **24 h chez Amazon**. Avec un panier moyen e-commerce français de 62 € (Fevad 2025, tous secteurs) et une conversion mode de 2 à 3 %, un clic sortant rapporte de l'ordre de 0,10 €.

### Cited Findings
- Awin, catégorie « Mode, luxe et accessoires » : commission moyenne de 7 % et panier moyen de 77 € (comparatif tiers, date non précisée) — [lecomparatifinternet.fr](https://lecomparatifinternet.fr/comparatifs/comparatif-plateformes-affiliation)
- Amazon Partenaires mode : taux annoncés entre 7 et 12 % selon les sources (10 % selon un guide, 7 % dans un comparatif 2026), cookie de 24 h. Sources contradictoires ; la grille officielle exige une connexion — [formalive.fr](https://www.formalive.fr/affiliation-amazon-guide/); [mon-revenu.fr](https://mon-revenu.fr/blog/meilleurs-programmes-affiliation-france/); [webandseo.fr](https://www.webandseo.fr/amazon-vs-awin/)
- Zalando : jusqu'à 8 % ; Privé by Zalando via Awin : 5 %, cookie de 30 jours — [mon-revenu.fr](https://mon-revenu.fr/blog/meilleurs-programmes-affiliation-france/); [freudix.studio](https://freudix.studio/programmes-affiliation/categorie/mode)
- Sézane : programmes Awin par pays (FR, UK, ES, NL, DE, IT), fenêtre d'attribution de 15 jours, taux non affiché publiquement — [Awin, Sézane FR terms](https://ui.awin.com/merchant-profile-terms/30303)
- Shein : 10 à 20 %, cookie de 30 jours (source tierce, non vérifiée sur la page officielle) — [creator-hero.com](https://www.creator-hero.com/fr/blog/shein-affiliate-program-in-depth-review-pros-and-cons); [makeinfluence](https://www.makeinfluence.com/en/academy/temu-and-sheins-affiliate-and-creator-programs-what-ultra-fast-fashion-marketplaces-actually-pay)
- Kwanko : principale plateforme française, fondée en 2003, plus de 180 000 sites affiliés ; taux non publiés — [digitiz.fr](https://digitiz.fr/plateformes-affiliation/)
- **Panier moyen e-commerce France 2025 (Fevad, source primaire, tous secteurs) : 62 €, en baisse de 3 %** ; 196,4 Md€ de ventes en ligne (+7 %), 3,2 milliards de transactions (+10 %) — [ecommerce-nation.fr, bilan Fevad 2025](https://www.ecommerce-nation.fr/?p=125750). Au S1 2025, le panier moyen était de 67 € — [ecommerce-nation.fr](https://www.ecommerce-nation.fr/e-commerce-en-france-croissance-au-1er-semestre-2025/)
- Taux de conversion e-commerce mode (monde) :
  - 3,01 % (2024), médiane de 2,4 % sur 500 marques en 2025 — [Smart Insights](https://www.smartinsights.com/ecommerce/ecommerce-analytics/ecommerce-conversion-rates/.); [ontapgroup](https://www.ontapgroup.com/blog/conversion-rate-fashion-ecommerce)
  - IRP : 1,39 % en juillet 2025 — [sqmagazine](https://sqmagazine.co.uk/e-commerce-conversion-rate-statistics)
  - Dynamic Yield : 2,7 à 3,1 % d'avril à octobre 2025 ; environ 1,9 % sur mobile web — [inmotionhosting](https://www.inmotionhosting.com/blog/ecommerce-conversion-rates-by-industry.md); [Branvas](https://branvas.com/blogs/news/mobile-vs-desktop-conversion-rate-gap)
- Liens affiliés dans une appli : avec Awin, le suivi passe par des Mobile Measurement Partners (AppsFlyer, Adjust). Ce dispositif sert d'abord aux annonceurs qui ont leur propre appli, et le lien doit contenir l'identifiant de site Awin — [Awin help, AppsFlyer](https://help.awin.com/developers/docs/appsflyer); [wecantrack, In-app affiliate marketing](https://wecantrack.com/insights/in-app-affiliate-marketing/)

### Inferences
- Exemple de calcul (non sourcé) : 1 clic sortant × 2,5 % de conversion × 62 à 77 € × 7 % ≈ **0,11 à 0,13 € par clic**. Si 10 % des utilisateurs actifs mensuels cliquent une fois par mois, cela fait environ 0,01 € par utilisateur actif et par mois : un complément, pas un modèle économique.
- Un lien ouvert dans le navigateur ou l'appli du marchand peut faire perdre l'attribution (cookie 24 h chez Amazon, pas de cookie partagé entre applis). Il faut ouvrir les liens dans le navigateur système plutôt que dans une WebView.
- Les liens collés par les utilisateurs doivent être réécrits en liens affiliés, par exemple via Awin ou un outil de type Skimlinks. Les règles de chaque programme sur la réécriture de liens générés par les utilisateurs sont à vérifier.

### Gaps
- Pas de grille officielle 2026 Amazon.fr consultable sans connexion.
- Taux non trouvés pour ASOS, H&M, Effiliation et Vinted ; Vinted ne semble pas avoir de programme d'affiliation public (non confirmé).
- Pas de taux de conversion propre à la France pour la mode.
- Pas de règle Apple ou Google explicite trouvée sur les liens affiliés dans une appli. Ils sont généralement autorisés pour des biens physiques hors IAP, mais je n'ai pas de citation.

## 4. In-app advertising (AdMob eCPM France) and impact on retention

### Takeaway
Les seules données France trouvées datent de **2023** : rewarded 9,82 $ sur iOS et 4,69 $ sur Android ; interstitiel 5,55 $ sur iOS et 3,14 $ sur Android. Les bannières font environ 0,50–1,50 $ dans les pays « tier 1 » en 2025. Les revenus publicitaires d'une petite appli lifestyle sont faibles.

### Cited Findings
- France, juin 2023 : eCPM rewarded iOS 9,82 $ et Android 4,69 $ ; interstitiel iOS 5,55 $ et Android 3,14 $ — [Statista](https://statista.com/statistics/1400838/ecpm-ad-type-ios-android-france)
- Bannières, 2025 : eCPM de 0,50 à 1,50 $ aux États-Unis, au Royaume-Uni, au Canada, en Australie, en Allemagne, en France et au Japon (fourchette groupée) — [Playwire, sept. 2025](https://www.playwire.com/blog/admob-ecpm-benchmarks-what-publishers-should-expect)
- Android, S1 2025 : eCPM rewarded −7 % et interstitiel −11 % sur un an (jeux, monde) — [TopOn H1 2025](https://mores.toponad.com/reports/TopOn%20Global%20Mobile%20Games%20Monetization%20Report%20_%202025%20H1.pdf)
- iOS rapporte plus qu'Android, en particulier en Amérique du Nord et en Europe — [Appodeal Q4 2024](https://appodeal.com/the-mobile-ecpm-report-updated-q4-2024-view)

### Inferences
- Exemple de calcul (non sourcé) : 3 bannières par jour et par utilisateur actif à 1 $ d'eCPM donnent environ 0,09 $ par mois. Les interstitiels et le rewarded rapportent plus mais nuisent à l'expérience d'une appli de style. La publicité est surtout pertinente comme « rewarded » (débloquer une suggestion IA, par exemple).

### Gaps
- Pas d'eCPM France 2025–2026 par format ; les données Statista datent de 2023.
- Pas d'étude chiffrée fiable sur l'effet des publicités sur la rétention d'applis lifestyle.

## 5. ARPU / revenue of comparable apps

### Takeaway
Les références sont très éloignées de l'échelle d'un développeur solo. Pinterest fait environ 1,55 $ par utilisateur actif européen et par trimestre grâce à la publicité ; Vinted 1,1 Md€ de revenus en 2025. Une appli garde-robe de niche comme Indyx est estimée sous 10 000 $ de revenu mensuel.

### Cited Findings
- Pinterest, T4 2025 (source primaire) : ARPU mondial 2,16 $ (2,12 $ un an plus tôt) ; Europe 245 M$ de revenus (+25 %) et 158 M d'utilisateurs actifs mensuels. Cela fait environ 1,55 $ par utilisateur et par trimestre en Europe (mon calcul sur les utilisateurs de fin de période). Les grands annonceurs retail européens ont réduit leurs dépenses — [Pinterest Q4 2025 press release](https://www.businesswire.com/news/home/20260212059914/en/Pinterest-Announces-Fourth-Quarter-and-Full-Year-2025-Results-Delivers-14-Revenue-Growth-and-Record-Users); [Zacks](https://www.zacks.com/stock/news/2868864/pinterest-q4-earnings-beat-estimates-on-healthy-revenue-growth)
- Vinted 2025 (publié le 9 avril 2026) : 1,1 Md€ de revenus (+38 %), volume d'affaires (GMV) de 10,8 Md€ (+47 %), résultat net 62 M€ (77 M€ en 2024), EBITDA ajusté 151 M€ ; 26 pays, lancement aux États-Unis en janvier 2026 — [FashionUnited](https://fashionunited.uk/news/business/vinted-reaches-1-1-billion-euros-in-revenue-for-2025-accepting-a-strategic-drop-in-profits/2026040987347); [Dealroom](https://dealroom.co/news/127284-vinted-hits-1-1b-revenue-and-sets-sights-on-the-us/)
- Indyx : environ 75 000 installations, environ 5 000 utilisateurs actifs mensuels, moins de 10 000 $ par mois (estimation tierce) — [AppGoblin](https://appgoblin.info/apps/1599179405)

### Inferences
- Pinterest monétise par la publicité avec des centaines de millions d'utilisateurs : son ARPU n'est pas transposable à une petite appli. Il montre surtout la limite haute d'une monétisation « inspiration mode » par la publicité.

### Gaps
- Pas d'ARPU Vinted (nombre d'utilisateurs non publié) ni de revenus de Lemon8.
- Pas de chiffres Whering ou Acloset.

## 6. When is paid acquisition profitable (LTV vs CPI, payback)

### Takeaway
La règle courante est un rapport LTV/CPI d'au moins **3** et un retour sur investissement en **6 à 12 mois**. Le coût par installation en Europe de l'Ouest est d'environ 1,70 $ (Android) à 3,60 $ (iOS). Avec un revenu freemium de 0,38 $ par installation à J60, l'acquisition payante n'est pas rentable sans un modèle de paywall plus agressif.

### Cited Findings
- Seuil de durabilité LTV/CPI ≥ 3, ou 2 acceptable s'il existe d'autres revenus ; horizon de 6 à 12 mois — [MWM, LTV](https://mwm.ai/glossary/ltv)
- LTV/CAC d'environ 3:1 et retour sur investissement en moins de 12 mois ; 6–12 mois pour les abonnements, 5–7 mois pour les meilleurs — [Adapty](https://adapty.io/blog/customer-acquisition-cost/); [Admiral Media](https://admiral.media/cac-payback-period-mobile-apps/)
- Autre règle : coût par installation ≤ 30 % de la LTV à J90, retour en 6 à 9 mois — [Adplus](https://adplus.com/tools/ad-budget-calculator/mobile-app)
- Coût par installation 2025 : moyenne Europe 2,25 $ (Amérique du Nord 2,49 $) — [Promodo](https://www.promodo.com/blog/mobile-marketing-benchmarks). Europe de l'Ouest : iOS 3,60 $, Android 1,70 $ — [AppFillip CPI report 2025](https://appfillip.com/resources__trashed/cpi-benchmark-report-2/). Les méthodes diffèrent.
- Revenu par installation à J60 (abonnements, monde) : freemium 0,38 $, hard paywall 3,09 $ — [RevenueCat 2026](https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026)

### Inferences
- Freemium : 0,38 $ de revenu à J60 contre 1,70 à 3,60 $ par installation, soit un rapport de 0,1 à 0,2. On est très loin du seuil de 3 : il faut une acquisition organique (TikTok/Instagram, effet réseau via les amis, App Store Optimization).
- Même un hard paywall (3,09 $ à J60) arrive à peine au niveau du coût par installation iOS. La viralité des fonctions « amis et tenues coordonnées », c'est-à-dire un coût par installation effectif réduit par les invitations, est le levier principal.

### Gaps
- Pas de coût par installation 2025–2026 propre à la France et à la catégorie lifestyle, ni ciblé sur les femmes de 16 à 30 ans.

## 7. Legal/administrative costs for a solo founder in France (micro-entreprise, VAT, switch to company)

### Takeaway
Plafonds de la micro-entreprise pour 2026–2028 : **83 600 €** (services/BNC) et **203 100 €** (ventes). Cotisations 2026 : **25,6 %** en BNC, 21,2 % en prestations BIC. Franchise de TVA : **37 500 €** pour les services. L'ACRE est réduite à 25 % pour les créations à partir du 1er juillet 2026. La TVA sur les abonnements est collectée par les stores.

### Cited Findings
- Plafonds de chiffre d'affaires, fixés pour 2026, 2027 et 2028 : 203 100 € pour la vente et l'hébergement, 83 600 € pour les services et BNC. L'ancien plafond services était de 77 700 € — [mesindependants.fr](https://www.mesindependants.fr/cotisations-urssaf); [astrolabe-conseil.fr](https://www.astrolabe-conseil.fr/micro-entreprise-taux-2026/)
- Cotisations 2026 : vente 12,3 % ; prestations de services BIC 21,2 % ; BNC régime général 25,6 % (24,6 % en 2025) ; BNC Cipav 23,2 % — [salaireclair.fr](https://salaireclair.fr/blog/charges-auto-entrepreneur-2026); [portail-autoentrepreneur.fr](https://www.portail-autoentrepreneur.fr/academie/statut-auto-entrepreneur/cotisations). swim.legal indique 23,2 % pour le BNC hors Cipav, en contradiction avec les autres sources — [swim.legal](https://www.swim.legal/blog/charges-micro-entreprise-2026-taux-urssaf-calcul-obligations-d9294)
- Versement libératoire de l'impôt sur le revenu : +2,2 % en BNC, soit environ 27,8 % au total — [astrolabe-conseil.fr](https://www.astrolabe-conseil.fr/micro-entreprise-taux-2026/)
- ACRE : exonération réduite à 25 % (au lieu de 50 %) pour les micro-entreprises créées à partir du 1er juillet 2026 (décret de février 2026) — [mesindependants.fr](https://www.mesindependants.fr/cotisations-urssaf)
- Franchise en base de TVA 2026 : 85 000 € pour la vente et 37 500 € pour les services ; seuils majorés de 93 500 € et 41 250 €. Le seuil unique de 25 000 € n'a pas été appliqué — [benmazue.com](https://www.benmazue.com/finance/facturation-micro-entreprise-tva/); [CMA Nouvelle-Aquitaine](https://cma-nouvelleaquitaine.fr/ressources/seuil-micro-entreprise)

### Inferences
- Les revenus d'abonnement versés par Apple et Google sont des commissions de vente via un commissionnaire, et les stores collectent et reversent la TVA aux consommateurs. Les revenus d'affiliation (Awin, Amazon) sont des prestations de services facturées à des entreprises. Ils comptent probablement dans le seuil de franchise de TVA de 37 500 €, et en cas d'autoliquidation intra-UE la mention adéquate est requise. Tout cela est à confirmer avec un expert-comptable.
- La qualification BIC (21,2 %) ou BNC (25,6 %) d'une activité d'édition d'appli et d'affiliation n'est pas tranchée par mes sources. L'écart de 4,4 points sur le chiffre d'affaires est significatif.
- Passer en SAS ou EURL se justifie quand le chiffre d'affaires approche le plafond de 83 600 €, quand les charges réelles deviennent importantes (serveurs, API IA, publicité, car la micro-entreprise ne permet pas de les déduire), ou pour lever des fonds. Ce point vient de règles générales, sans source spécifique ici.

### Gaps
- Pas de source officielle (URSSAF, service-public.fr, impots.gouv.fr) consultée directement ; les taux viennent de sites spécialisés concordants, à confirmer sur urssaf.fr.
- Pas de source sur le traitement comptable exact des versements Apple et Google (chiffre d'affaires brut ou net de commission) en micro-entreprise.
- Coûts de création et de gestion d'une SAS ou EURL (comptable, greffe) non recherchés.
