# Status

**Phase:** PH-1 (spec v1.0): released as v0.1.4 (9 Nov, tag ph-1); waiting for Chris's test on the phone. Report: docs/reports/PH-1.md.

## Done

- Spec from the launcher's handoff plus Chris's screen requirements (docs/REQUIREMENTS.md, WX-6).
- AccuWeather client (WX-2): Bearer key in the Keystore-encrypted store, one allowlisted host and three paths, call counter and a 100/day stop, errors kept for settings, four daily updates with WorkManager, the next 12 hours on opening.
- Days classified by the launcher's rules (WX-3), tile provider with the launcher's key check (WX-4), stand-in `fakeweather` (WX-4.6), docs/LAUNCHER-CHANGES.md.
- Forecast and settings pages (WX-6), with Chris's rounding everywhere.
- 9 Nov (Chris): 0 stays 0 in the rounding; night hours show the Fluent moon and partly cloudy night icons; the launcher's tile uses the same rounding (wp-launcher main, not released). Real AccuWeather replies recorded as fixtures (3 calls with Chris's key).
- In the session: 38 unit and screenshot tests pass; signed release builds; permission, network and host checks pass and reject their test cases.

## Next

- 9 Nov: Chris has the signing secrets as a file (sent in chat, never committed). Q1 answered: 500 calls a day; the app stops at 450.

- Chris tests v0.1.4 on the phone (steps in PH-1.md). Secrets saved by Chris on 9 Nov; the release's key fingerprint matches docs/LAUNCHER-CHANGES.md.
- Launcher: the tile rounding change is on wp-launcher main (2c5c3e7), not released; waiting for Chris's go-ahead.
- The launcher session reads docs/LAUNCHER-CHANGES.md and builds its side.

## Open questions

- More Fluent conditions (thunderstorm, fog, sleet, showers) would need AccuWeather's own icon numbers on the app's screens (WX-3.3). Not done; asked Chris.

- Q1: 500 calls a day (Chris, 9 Nov).
- Q2–Q4: defaults used (no location, tile unchanged without alerts, hourly only when opened).

## Known issues

- Nothing has run on the phone yet. The app itself hasn't called AccuWeather yet; the session made 3 calls with curl to record fixtures.
