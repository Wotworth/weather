# Status

**Phase:** PH-1 (spec v1.0, 8 Nov): built; waiting for the signing secrets, then Chris's test on the phone. Report: docs/reports/PH-1.md.

## Done

- Spec from the launcher's handoff plus Chris's screen requirements (docs/REQUIREMENTS.md, WX-6).
- AccuWeather client (WX-2): Bearer key in the Keystore-encrypted store, one allowlisted host and three paths, call counter and a 100/day stop, errors kept for settings, four daily updates with WorkManager, the next 12 hours on opening.
- Days classified by the launcher's rules (WX-3), tile provider with the launcher's key check (WX-4), stand-in `fakeweather` (WX-4.6), docs/LAUNCHER-CHANGES.md.
- Forecast and settings pages (WX-6), with Chris's rounding everywhere.
- 9 Nov (Chris): 0 stays 0 in the rounding; night hours show the Fluent moon and partly cloudy night icons; the launcher's tile uses the same rounding (wp-launcher main, not released). Real AccuWeather replies recorded as fixtures (3 calls with Chris's key).
- In the session: 38 unit and screenshot tests pass; signed release builds; permission, network and host checks pass and reject their test cases.

## Next

- Chris saves the three signing secrets and runs the workflow (steps in PH-1.md), then tests on the phone.
- The launcher session reads docs/LAUNCHER-CHANGES.md and builds its side.

## Open questions

- The signing secrets: Claude was stopped from showing the key in chat (credential safety check). Chris to choose how to get it.
- More Fluent conditions (thunderstorm, fog, sleet, showers) would need AccuWeather's own icon numbers on the app's screens (WX-3.3). Not done; asked Chris.

- Q1: Which AccuWeather plan, and its limit? The app stops at 100 calls a day meanwhile.
- Q2–Q4: defaults used (no location, tile unchanged without alerts, hourly only when opened).

## Known issues

- Nothing has run on the phone yet. The app itself hasn't called AccuWeather yet; the session made 3 calls with curl to record fixtures.
