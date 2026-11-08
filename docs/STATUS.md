# Status

**Phase:** PH-1 (spec v1.0, 8 Nov): built; waiting for the signing secrets, then Chris's test on the phone. Report: docs/reports/PH-1.md.

## Done

- Spec from the launcher's handoff plus Chris's screen requirements (docs/REQUIREMENTS.md, WX-6).
- AccuWeather client (WX-2): Bearer key in the Keystore-encrypted store, one allowlisted host and three paths, call counter and a 100/day stop, errors kept for settings, four daily updates with WorkManager, the next 12 hours on opening.
- Days classified by the launcher's rules (WX-3), tile provider with the launcher's key check (WX-4), stand-in `fakeweather` (WX-4.6), docs/LAUNCHER-CHANGES.md.
- Forecast and settings pages (WX-6), with Chris's rounding everywhere.
- In the session: 35 unit and screenshot tests pass; signed release builds; permission, network and host checks pass and reject their test cases.

## Next

- Chris saves the three signing secrets and runs the workflow (steps in PH-1.md), then tests on the phone.
- The launcher session reads docs/LAUNCHER-CHANGES.md and builds its side.

## Open questions

- Q1: Which AccuWeather plan, and its limit? The app stops at 100 calls a day meanwhile.
- Rounding: 0% shows as 1% (ties go up). Keep, or show 0?
- Rounding: the tile (launcher, WX-5.2) rounds to the nearest 5 only, so 7° shows 7° in the app and 5° on the tile. Should the launcher use the odd-number rule too?
- Clear nights show the sun in the hourly strip (only the tile's five icons). OK?
- The test replies were written from AccuWeather's schemas, not recorded. Replace with a real reply once the app has one.
- Q2–Q4: defaults used (no location, tile unchanged without alerts, hourly only when opened).

## Known issues

- Nothing has run on the phone yet. No real AccuWeather call has been made (no key in the session).
