# Coûts d'acquisition d'utilisateurs via TikTok (France, avec comparaison UK/US) pour un développeur solo à petit budget

Research date: November 2026. Most sources are agency or vendor blogs. They often recycle the same benchmarks and should be read as orders of magnitude, not audited data. USD figures are kept in USD where the source gives no EUR figure.

## 1. TikTok Ads in France: CPM, CPC, CTR, CPI, minimum budgets, and comparison with Meta and Google

### Takeaway
I found no public benchmark of TikTok CPI for France specifically. The usable numbers are a France CPM of about €3–8 and a CPC of about €0.20–0.80 (agency blogs), plus global or channel TikTok CPI ranges of roughly $1.20–3.50 (low-quality sources). Official minimums are $50/day per campaign and $20/day per ad group, which work out to about €20/day in practice for a solo developer. For a free lifestyle app in France, a planning CPI of roughly €1–3 on Android and more on iOS is a reasonable working assumption. That range is an inference and needs to be validated with a test.

### Cited Findings
- **Official minimum budgets:** ad group daily budget > $20; campaign daily or lifetime budget > $50. For a lifetime budget, the ad group minimum is the daily minimum × the number of days (e.g. 31 days = $620). — [TikTok Ads Help: About daily budgets](https://ads.tiktok.com/help/article/about-daily-budgets); [TikTok Ads Help: Budget](https://ads.tiktok.com/resources/help/article/budget)
- Third-party guides give €50/day per campaign and €20/day per ad group, calling them "technical thresholds, not performance thresholds." I found no official TikTok page with euro amounts. One Polish guide cites 80 PLN per ad group, which suggests minimums are set per currency. — [Trendtrack](https://www.trendtrack.io/blog-post/how-much-does-a-tiktok-ads-campaign-cost); [Delante](https://delante.co/how-much-do-tiktok-ads-cost/)
- A third-party rule of thumb (not official) sets the daily ad group budget at about 10 × the target CPA so the algorithm can learn. — [Emplicit](https://emplicit.co/scaling-tiktok-ads-budget-optimization-guide/) (as summarized in search results)
- **France CPM:** €3–8 on TikTok vs €12–20 on Facebook for comparable audiences. CPC is €0.20–0.80 (agency blog, date unclear, likely 2025). — [Adintime](https://adintime.com/en/blog?id=311)
- **Europe in-feed (2025):** CPM €5–10 (average about €7.50), CPC €0.30–0.80 (average about €0.55). — [Fullmetrix, TikTok ads ecommerce guide](https://fullmetrix.com/en/blog/tiktok-ads-ecommerce-guide)
- **Global 2025 (Triple Whale data, January–December 2025, cited by Influee in July 2026):** CPM $13.26 (+16% vs 2024), CPA $32.74, CTR 1.77%, CVR 2.01%. In **Fashion / Clothing & Accessories**: CPM $4.24, CTR 0.69%, CPA $21.85. In **Lifestyle & Boutique**: CPM $4.25, CTR 0.57%. These are e-commerce figures (purchase CPA), not app installs. The source gives no CPC, no CPI, and no country breakdown. — [Influee, Benchmarks TikTok Ads 2026](https://influee.co/fr/blog/tiktok-ads-benchmarks)
- Global CPM ranges from other blogs: $6.16–9 with CPC $0.17–1.50 (2025), and $4.20–9 with CPC $0.17–1 and CPV $0.01–0.07 (2026). A Metricool study cited by Blog du Modérateur gives a top-of-funnel TikTok CPM of about $0.32, which is inconsistent with the others (probably a different metric). — [Trendtrack FR](https://www.trendtrack.io/fr/blog-post/donnees-de-reference-cpm-tiktok-vs-meta-pour-2026); [Blog du Modérateur](https://www.blogdumoderateur.com/social-ads-meta-tiktok-faire-connaitre-convertir/); [CB News / Metricool](https://www.cbnews.fr/node/103352)
- **CPI by channel (not country-specific, 2026, uncertain quality):** TikTok $1.20–3.50, Google App Campaigns $1.80–4.50, Meta $2.00–5.50, Apple Search Ads $2.50–5.00. — [Semnexus](https://semnexus.com/cpi-by-country-where-app-budget-goes-furthest-2026/)
- **CPI by region (2025):** EMEA $2–4, North America $2.50–5. France and the UK are placed in the "cheaper" group relative to the US. — [Mapendo, CPI by country 2025](https://mapendo.co/blog/cost-per-install-by-country-2025)
- TikTok CPI by vertical (unverified secondary source): about $0.70–4.50. Casual gaming is $0.50–2, while fintech and SaaS are $5–20+. — [Stackmatix](https://www.stackmatix.com/blog/tiktok-ads-for-app-installs)
- TikTok's own figures (Adjust meta-analysis, October 2025, self-published): lower CPIs than other platforms, e.g. −40% for Education and about −65% for Health & Fitness (North America, 2024 data). — [TikTok for Business blog](https://ads.tiktok.com/business/en-US/blog/how-tiktok-powers-app-installs)
- **Meta CPI in France:** the SuperAds tracker is very volatile from month to month. Its France figures run from about $44.7 in July 2025 to $2.84 in June 2026, which makes them unreliable. For the UK, it shows an average median of about 29.9 from October 2025 to September 2026, with the unit unclear. — [SuperAds France](https://www.superads.ai/facebook-ads-costs/cost-per-app-install/france); [SuperAds UK](https://www.superads.ai/facebook-ads-costs/cost-per-app-install/united-kingdom)
- TikTok vs Meta globally (2025): CPM $13.26 vs $14.19, CPA $32.74 vs $38.19. — [Influee](https://influee.co/fr/blog/tiktok-ads-benchmarks)
- AppsFlyer reports that paid iOS installs rose at least 25% in Western markets, while Android was flat or declining. — [AppsFlyer Top 5 data trends](https://www.appsflyer.com/resources/reports/top-5-data-trends-report/)

### Inferences
- If CPM is €4–8, CTR is about 0.7–1.5%, and click-to-install conversion is 20–40%, the implied France Android CPI is roughly €0.70–5. A working planning figure is **€1.5–3 on Android** and **€2.5–5 on iOS**. This is my own calculation, not a published benchmark.
- With the €20/day minimum, a 2–3 week test costs about €300–450. At €2 per install, that buys roughly 150–225 installs, enough to measure CPI but not enough for algorithmic optimization on deep in-app events.
- The "Fashion" CPMs ($4.24) are lower than the global average, which fits a young female audience saturated with fashion content. This data comes from e-commerce, though.

### Gaps
- No public benchmark gives TikTok **CPI for France** split by Android and iOS, or for the lifestyle or fashion app category specifically. AppsFlyer and Liftoff publish this in paid or gated reports, which I could not access.
- I found no reliable figure for Google App Campaigns CPI in France.
- UK and US TikTok figures are only regional (North America vs EMEA).

## 2. Spark Ads (boosting organic or creator posts)

### Takeaway
Spark Ads promote an existing organic post, either from your own account or a creator's account with an authorization code. The engagement stays on the original post. Available evidence, mostly from vendors, shows a higher CPM but a lower CPI/CPA. The Endel case study reports a CPI of $3 vs $7 without Spark.

### Cited Findings
- Endel case study (TikTok): CPI $3 with Spark Ads vs $7 with non-Spark ads, and CPA cut by 2.2×. This is published by TikTok, so it is a best case. — [TikTok Success stories: Endel](https://ads.tiktok.com/business/en-US/inspiration/endel-823); [CaseStudies.com](https://www.casestudies.com/company/tiktok-for-business/case-study/reducing-cpa-and-promoting-brand-account-with-spark-ads)
- An iOS test (Business of Apps / Mobile Marketing Magazine) found that Spark CPM and CPC were "not always cheap," but click-to-install conversion was always higher, by up to +72%, and final CPI and CPA were lower. — [Business of Apps](https://businessofapps.com/?p=84901); [Mobile Marketing Magazine](https://mobilemarketingmagazine.com/?p=116642)
- Dynamoi reports CPI up to 57% lower with Spark but a higher CPM, without citing its dataset. — [Dynamoi](https://dynamoi.com/learn/tiktok-music-promotion/tiktok-spark-ads-vs-non-spark-ads)
- RocketShip HQ reports, from practitioner experience rather than a measured benchmark, that Spark Ads tend to lower CPA for app installs where social proof matters. — [RocketShip HQ Spark Ads guide](https://www.rocketshiphq.com/tiktok-spark-ads-guide/)
- Amplification (Spark or whitelisting) rights for a creator's post add **20–50% to the content fee** (Kolsquare, July 2026). Takema puts whitelisting at 20–30% of the base fee per 30 days. — [Kolsquare](https://www.kolsquare.com/fr/blog/quels-sont-les-tarifs-dinfluenceurs-sur-tiktok); [Takema Studio](https://takema-studio.com/marques/prix-video-ugc-en-france-le-guide-complet-pour-2026)

### Inferences
- For a solo developer, the most cost-effective path is to post organically and only boost with Spark Ads the posts that are already performing well organically (good watch time and saves). Spark Ads use the same minimums ($20/day per ad group).

### Gaps
- No independent multi-advertiser 2025–2026 study isolates Spark vs non-Spark CPI.

## 3. Creator and UGC costs in France

### Takeaway
According to French grids (Kolsquare, 2026), a TikTok post costs about **€25–200 for nano** creators (1k–10k followers) and **€200–1,500 for micro** creators (10k–100k). A UGC video (content the brand posts or uses in ads) costs **about €35–320**, with a median around €80–210 depending on the platform. Kolsquare reports that fees rose 30–50% in 2025.

### Cited Findings
- Kolsquare (July 2026), per sponsored TikTok post: nano €25–200; micro €200–1,500; macro €1,500–5,000; mega €5,000+. Kolsquare says these are converted from international benchmarks (Influencer Marketing Hub 2026) and that the French market may differ. — [Kolsquare](https://www.kolsquare.com/fr/blog/quels-sont-les-tarifs-dinfluenceurs-sur-tiktok)
- Kolsquare also reports fees up 30–50% in 2025. At the same tier, TikTok is said to cost 15–40% less than Instagram (vendor blogs, per search summary). For nano and micro creators, sending products can be enough. — [Kolsquare](https://www.kolsquare.com/fr/blog/quels-sont-les-tarifs-dinfluenceurs-sur-tiktok); [Influee: TikTok influencer rates](https://influee.co/fr/blog/tiktok-influencer-rates)
- Affiliate or commission deals: commissions are typically **5–50% depending on the sector** (percentage of sales). This is poorly suited to a free app with no revenue, where a fixed **CPI or bounty per install** would be needed instead. That last point is my inference. — [Kolsquare](https://www.kolsquare.com/fr/blog/quels-sont-les-tarifs-dinfluenceurs-sur-tiktok)
- **UGC in France (2026):** €150–320 per video, with a median creator ask of about €212. Beginners charge about €75–100. Script writing or extra shots add 25–50%. — [Takema Studio](https://takema-studio.com/marques/prix-video-ugc-en-france-le-guide-complet-pour-2026)
- Influee: average of about €80 for a 30-second UGC video in France. Listed creators range from about €34 to €64 per video (marketplace prices). — [Influee FR creators](https://influee.co/fr/creators/FR)
- **TikTok One / Creator Marketplace:** a platform connecting brands and creators for creator content and ready-to-run ads. It publishes no price grid, and fees are negotiated with each creator. — [TikTok for Business: TikTok One](https://ads.tiktok.com/business/fr/blog/tiktok-one-creative-platform)

### Inferences
- A realistic starter budget is 5 nano or micro creators at €100–300 each, so **€500–1,500**. Alternatively, 5–10 UGC videos at €50–150 each (**€250–1,500**) give enough creatives to test in Spark or in-feed ads.

### Gaps
- No primary French report (Reech, Favikon) with prices segmented for the fashion niche was found or accessible.
- I found no public data on CPI- or performance-based deals with French creators for free apps.

## 4. Organic TikTok growth for apps (indie case studies)

### Takeaway
The documented indie successes come mainly from high-volume "slideshow" (photo carousel) or faceless formats, often run across several accounts. The figures are self-reported or come from vendors. I found no reliable benchmark for view-to-install conversion.

### Cited Findings
- Astra (SideShift client): 400+ TikToks in 6 weeks, 5M+ organic views, and one video brought in about 25k downloads. SideShift sells creator marketing, so this is a vendor figure. — [SideShift case studies](https://sideshift.app/casestudies)
- An indie app reached 20k downloads and $9K MRR in 78 days, mainly through TikTok views (secondary write-up). — [Shortimize](https://www.shortimize.com/blog/20k-downloads-and-9k-mrr-in-78-days-how-this-indie-app-turned-minimalism-into-millions-of-views)
- Two apps (Kyle's Cardstock and Scanemon) reportedly earn about $120K/month combined, largely from TikTok slideshows. The method is to validate ideas by posting slideshows from accounts created on your phone, then scale up. This is unverified. — [Stork.ai](https://www.stork.ai/blog/the-120kmonth-tiktok-slideshow-hack)
- About 40% of users discover apps by browsing or searching the stores, so ASO remains a major channel alongside TikTok. — [Mobile Action](https://www.mobileaction.co/blog/organic-app-growth-in-2025/)
- Paid install costs have "exploded," and relying on ads alone drains an indie budget quickly. — [Mobile Action](https://www.mobileaction.co/blog/3-tips-to-raise-organic-downloads)

### Inferences
- Implied by Astra's numbers: 25k downloads from one viral video (view count not given) in a 5M-view total, which suggests a view-to-install rate on the order of 0.1–0.5% for strong videos. This is very uncertain.
- The common cadence in these cases is 1–3 posts per day per account, sometimes across several accounts. The weather, outfit and wardrobe niche suits "outfit of the day by weather" slideshows.

### Gaps
- I found no rigorous benchmark (independent source) for view-to-install conversion. No French indie case study was found.

## 5. iOS: ATT in France, SKAdNetwork, and TikTok iOS efficiency

### Takeaway
AppsFlyer puts ATT opt-in in France at **just under 51%** (2025), versus about 35% globally according to Adjust (Q2 2025). The difference comes from methodology. That means about half of French iOS users are only measured through SKAdNetwork, which is aggregated and delayed, and this weakens TikTok's optimization on iOS.

### Cited Findings
- AppsFlyer (April 2025, four years after ATT): France average opt-in just under 51%, Germany 47%. The previous year's release had France at 53%. — [AppsFlyer newsroom](https://www.appsflyer.com/company/newsroom/pr/post-att-growth/); [Advanced Television](https://www.advanced-television.com/2025/04/24/data-50-of-app-users-now-consent-to-tracking/)
- Adjust Q2 2025: 35% global opt-in (34.5% in Q2 2024). No France figure published. — [Adjust: ATT opt-in rates 2025](https://www.adjust.com/blog/att-opt-in-rates-2025)

### Inferences
- Launching on Android first is advantageous for measurement: no ATT, and GAID is available with consent. On iOS, TikTok campaigns require SKAN configuration (conversion values) and an MMP or TikTok SDK. Expect higher CPIs and noisier data.

### Gaps
- I did not find a quantified source on TikTok iOS vs Android CPI in France, or on the precise impact of SKAN 4/AdAttributionKit on TikTok in 2025–2026.

## 6. Measurement tools: TikTok SDK vs MMPs, free tiers

### Takeaway
AppsFlyer offers **12,000 free conversions in the first year** (Welcome package). After that, the Zero plan is free but does not attribute paid campaigns, and Growth costs about **$0.07 per conversion**. Kochava and Singular also have free tiers. At very small scale, the TikTok Events SDK (free) plus Play Console statistics is enough to start.

### Cited Findings
- AppsFlyer: the Welcome package gives 12k free conversions or 1 year, whichever comes first, then switches to Zero (engagement only, no attribution, "not suited to apps with paid activity"). Growth is pay-as-you-go at $0.07 per conversion. — [AppsFlyer pricing](https://www.appsflyer.com/pricing-2025/); [AppsFlyer support: plans](https://support.appsflyer.com/hc/en-us/articles/115000136303)
- Adjust: a free Base plan with 1,500 attributions/month for 12 months, per Airbridge's comparison (unverified on Adjust's site). Branch: paid use starts at $199/month (third-party source). Kochava: free up to 10k attributed conversions/month. Singular: free up to 15k paid conversions. — [Airbridge, MMP pricing comparison](https://www.airbridge.io/tips/mobile-attribution-pricing-plans-contract-terms)

### Inferences
- Recommended order: TikTok Events SDK or TikTok's direct Play integration on Android first, then add an MMP (AppsFlyer Welcome or Singular free) before launching on iOS or using several paid channels.

### Gaps
- The free plans of Adjust and Branch could not be confirmed on their official pages. No TikTok official documentation was consulted on the free SDK and its limits.

## 7. EU and France rules: GDPR consent for ad SDKs and the 2023 influencer law

### Takeaway
The CNIL requires **prior consent** before any data processing for advertising or tracking by an SDK, and it has been inspecting mobile apps since spring 2025. Under the influencer law (n° 2023-451 of 9 June 2023), any paid partnership must carry the label **"Publicité" or "Collaboration commerciale"** throughout, with a **written contract** above a threshold. Penalties go up to 2 years in prison and a €300,000 fine.

### Cited Findings
- CNIL mobile app recommendation (final version 24 September 2024, updated 8 April 2025): consent is required for data that is not necessary for the app to function, such as ad targeting. Consent for geolocation does not cover personalized advertising. — [CNIL recommendations](https://www.cnil.fr/fr/recommandations-applications-mobiles); [Recommendation PDF](https://www.cnil.fr/sites/cnil/files/2024-09/recommandation-applications-mobiles.pdf)
- The CNIL announced a mobile app inspection campaign starting spring 2025. Commentaries say inspectors check whether SDKs actually stop sending data before consent is given. — [Clubic](https://www.clubic.com/actualite-539018-des-applications-mobiles-qui-respectent-enfin-votre-vie-privee-la-cnil-va-multiplier-les-controles-en-2025.html); [Secure Privacy](https://secureprivacy.ai/fr/blog/conformite-rgpd-des-applications-mobiles-cnil-sdk-et-consentement-2026)
- Publisher and SDK provider can be joint controllers, and the publisher cannot shift responsibility by contract (secondary commentary). — [donneespersonnelles.fr](https://www.donneespersonnelles.fr/application-mobile-rgpd)
- Influencer law: the "Publicité" or "Collaboration commerciale" label is mandatory and must stay visible for the whole promotion. Violations carry 2 years in prison and a €300,000 fine. — [vie-publique.fr](https://www.vie-publique.fr/loi/288793-loi-influenceurs-proposition-de-loi-delaporte-vojetta); [Eurojuris](https://www.eurojuris.fr/categories/e-commerce-8500/articles/decryptage-loi-influenceurs-41692.htm)
- A written contract is required (governed by French law), and the contract is void if it is not in writing. A dedicated DGCCRF unit (15 agents) enforces the law. — [CMS Law](https://cms.law/en/fra/news-information/loi-sur-les-influenceurs); [economie.gouv.fr](https://www.economie.gouv.fr/node/3220692)
- The written-contract threshold is cited as **€1,000** by one January 2026 blog. I did not confirm this on Legifrance. — (search summary; unverified)

### Inferences
- In practice: use a consent banner or CMP in the app before initializing the TikTok SDK or MMP in personalized-ads mode, and have creators sign a simple written contract even for small amounts. The audience includes minors aged 16–17, which calls for extra caution with content and data. That is my inference; the specific rules on minors were not researched.

### Gaps
- The exact contract threshold (implementing decree) was not verified. I did not research DSA or TikTok rules on targeting minors (16–17) in the EU, which likely matter for this audience.
