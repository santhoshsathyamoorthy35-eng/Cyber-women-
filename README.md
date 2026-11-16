# CYBER WOMEN

**Intelligent Safety · Real-Time Protection · Empowerment**

CYBER WOMEN is a native Kotlin + Jetpack Compose Android application that unifies the **CrimeMap 3D** spatial intelligence dashboard, **SafeHer** verified women & child helpline/empowerment directory, **The Shield & Shield Protocol** SOS/time-risk advisory system, and **Meena AI** bilingual (English & Tamil) safety companion.

## Key Modules

1. **Home Command Dashboard (`HomeScreen`)**
   - Live GPS lock + sector switcher (`Puducherry`, `Chennai`, `Coimbatore`, `Madurai`).
   - **The Shield SOS Hero Trigger**: Press-and-hold 1.5s or quick-tap 5s cancel countdown + direct `112` dialer.
   - **Shield Protocol Time-Window Risk Advisory**: Interactive hour-of-day slider evaluating historical incident concentration across Puducherry zones. Clearly labeled as historical aggregate patterns over synthetic baseline + user-submitted reports.
   - Nearest Safe Havens & Emergency Dial Strip (`112`, `181`, `14490`, `1091`, `1930`, `14416`, `1098`, `1094`, `15100`).

2. **CrimeMap 3D (`SafetyMapCanvas` & `SafetyMapScreen`)**
   - 60fps Jetpack Compose 3D-projected geospatial engine with CartoDB Dark Matter slippy tile cache and offline Puducherry vector grid fallback.
   - Deterministic 4,200 seeded baseline incidents + real user `citizen_reports` from Firestore.
   - Layers: Heatmap, 3D Hexagonal Risk Towers, Data Beams, Hotspot Clusters, 7-Day Predicted Risk Grid, Shield Time-Risk Zones, Emerging Surge Alerts, 24 CCTV Camera nodes + FOV Cones, 8 Police Stations, Radar Sweep, Patrol VRP routes.
   - Sub-modes: `3D Map`, `Live Watch` (10-type AI detection stream + 4-up simulated CCTV matrix modal), `Analytics` (KPIs, Canvas Donut Chart, Station Leaderboard), `Patrol VRP` optimizer, and `Reports` (Firestore-backed citizen reporting) + CSV export.

3. **The Shield & Emergency SOS (`ShieldSosScreen`)**
   - Trusted Contacts CRUD (Cloud Firestore `/users/{userId}/trusted_contacts`).
   - SOS Event log lifecycle (`created`, `queued_local`, `logged_cloud`, `cancelled`, `resolved`) synced with Room offline cache + `LocationSharingService` foreground GPS updates.
   - Full-screen Fake Call Simulator & Accelerometer Shake-to-SOS toggle.

4. **SafeHer Safety Hub (`SafetyHubScreen` & `SafetyHubDiagrams`)**
   - Verified `National`, `Puducherry`, `Tamil Nadu`, and `Other` helplines (including all 33 District DV Protection Officers & Sakhi One Stop Centres) with `tel:` dial intents.
   - Government & NGO directory, 6 interactive learning modules with custom Canvas diagrams and badges, 7-question Touch & Cyber Quiz, 8 Legal Rights, 8 Case Studies, Personal Safety Checklist, and Feedback submission.

5. **Meena AI Companion (`MeenaAiScreen`)**
   - Powered by **Firebase AI Logic** (`gemini-3.8-flash`) with Firebase App Check (`DebugAppCheckProviderFactory` in debug, `RecaptchaAppCheckProviderFactory` in release).
   - Bilingual EN / தமிழ் guidance grounded in verified helplines and automatic immediate-danger escalation banner (`Call 112` & `Shield SOS`).

## Firebase & Security Architecture

- **Authentication**: Google Sign-In exclusively via Jetpack `CredentialManager` (`GetGoogleIdOption` silent + `GetSignInWithGoogleOption` button).
- **Database**: Named Cloud Firestore instance (`R.string.firestore_database_id`) with zero-trust owner-scoped `firestore.rules` (validated by both Node `@firebase/rules-unit-testing` and Robolectric `*RuleTest`).
- **Offline Cache**: Room database (`CyberWomenDatabase`) + DataStore (`PreferencesStore`).

## Transparent Simulation Disclosures & Production Roadmap

- **CrimeMap 3D Historical Baseline**: Uses 4,200 deterministic synthetic FIR records modeled across Puducherry coordinates (`11.850..11.985`, `79.760..79.865`) separated cleanly (`isSynthetic = true`) from user-submitted `citizen_reports` (`isSynthetic = false`).
- **Shield Protocol**: Computes aggregate historical time-window concentration; it does not predict individual crimes.
- **External Gateways**: SOS events log to private cloud history and launch `112` / OS Share Sheet intents; automated carrier SMS/WhatsApp blasting and municipal CCTV feeds require external Twilio/PSIM server integrations outside client scope.
