# Status

**Phase:** PH-1 (spec v1.0): round 2 (v0.1.5) accepted by Chris on 9 Nov apart from round 3 (refresh feedback, pull to refresh, festive snow), built and pushed; round 4 (seasonal cards) pushed; waiting for his test. Report: docs/reports/PH-1.md.

## Done

- Spec from the launcher's handoff plus Chris's screen requirements (docs/REQUIREMENTS.md, WX-6).
- AccuWeather client (WX-2): Bearer key in the Keystore-encrypted store, one allowlisted host and three paths, call counter and a 100/day stop, errors kept for settings, four daily updates with WorkManager, the next 12 hours on opening.
- Days classified by the launcher's rules (WX-3), tile provider with the launcher's key check (WX-4), stand-in `fakeweather` (WX-4.6), docs/LAUNCHER-CHANGES.md.
- Forecast and settings pages (WX-6), with Chris's rounding everywhere.
- 9 Nov (Chris): 0 stays 0 in the rounding; night hours show the Fluent moon and partly cloudy night icons; the launcher's tile uses the same rounding (wp-launcher main, not released). Real AccuWeather replies recorded as fixtures (3 calls with Chris's key).
- 9 Nov, round 2 (Chris's feedback): shrink-to-fit town and temperature, capitalised names, cloud cover labelled everywhere, a day page on tap (daytime/overnight + hours), updates every 30 minutes, condition backgrounds image. Spec WX-2.5, 2.6, 6.6–6.8.
- 9 Nov, round 3: refresh icon turns, pull to refresh, festive snow block with Fluent snowflakes (WX-6.9 to 6.11).
- 9 Nov, round 4: seasonal cards, 20 designs (WX-6.12); snow festive all year (Q6).
- In the session: 55 unit and screenshot tests pass; signed release builds; permission, network and host checks pass and reject their test cases.

## Next

- 9 Nov: Chris has the signing secrets as a file (sent in chat, never committed). Q1 answered: 500 calls a day; the app stops at 450.

- Chris tests round 3 on the phone (steps in chat; screens in PH-1.md). Secrets saved by Chris on 9 Nov; the release's key fingerprint matches docs/LAUNCHER-CHANGES.md.
- Launcher v0.9.184 released 9 Nov (CI passed, from main 3bfac17): tile rounding and the Weather app's tile. https://github.com/Wotworth/wp-launcher/releases/tag/v0.9.184

## Open questions

- Q5: hourly forecasts for every day need AccuWeather's "elite" plan (Chris has "starter"). Default: no upgrade; later days show daytime and overnight.
- More Fluent conditions (thunderstorm, fog, sleet, showers) would need AccuWeather's own icon numbers on the app's screens (WX-3.3). Not done; asked Chris.

- Q1: 500 calls a day (Chris, 9 Nov).
- Q6: festive snow all year (Chris, 9 Nov).
- Q2–Q3: defaults used (no location, tile unchanged without alerts). Q4: every 30 minutes (Chris, 9 Nov).

## Known issues

- Background updates every 30 minutes can be delayed by Android while the phone sleeps (Doze); only the phone can show how often they really run.
- The session made 6 AccuWeather calls with curl: 3 fixtures (8 Nov) and 3 refused plan checks (9 Nov).
