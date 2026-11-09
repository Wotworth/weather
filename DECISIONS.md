# Decisions

| Date | Req | Decision | Why |
|---|---|---|---|
| 2026-11-08 | WX-2.2 | The key goes in an `Authorization: Bearer` header only, to `dataservice.accuweather.com`. | AccuWeather's Authentication page (developer.accuweather.com/documentation/authentication, read 8 Nov 2026): "Every request to the API must include a valid API key in the Authorization header", bearer format. |
| 2026-11-08 | WX-2.6 | No current-conditions call. The page's top block shows today's high, low and figures from the daily forecast; the hours come from the 12-hour forecast. | One call fewer per opening; the 12-hour forecast starts within the hour. |
| 2026-11-08 | WX-2.7 | The app stops at 100 calls a day (WeatherTokens). | AccuWeather's Free tier allows 500 a day and Starter 15,000 a month (≈500 a day; pricing page, 8 Nov 2026). Normal use is about 4 + one per opening. Q1 (Chris's plan) is still open. |
| 2026-11-08 | WX-2.5 | A failed daily update isn't retried until the next slot or a manual refresh, including when the app restarts. | The launcher starts the app's process each time it reads the tile; retrying on each start could spend calls with a bad key. |
| 2026-11-08 | WX-2.4 | The default town's location key is found with one city search ("Hastings-on-Hudson", matched to NY, US) on the first update, then kept. | No key was available to look it up in advance. |
| 2026-11-08 | WX-6.2 | Rounding ties go up: 72.5 → 75, 4 → 5, 8 → 9, −2 → −1. | The launcher's roundTo5 rounds halves up. |
| 2026-11-09 | WX-6.2 | 0 is a target alongside the odd numbers (Chris: "0 should equal 0"): 0 → 0, 0.4 → 0, 0.5 → 1, −0.5 → 0, 2 → 3. | Nearest of {odd numbers, 0}, ties up, as above. |
| 2026-11-08 | WX-6.2 | Settings' diagnostics (calls today, the key's last 4 characters, version, dates and times) are not rounded. | They aren't weather values, and "calls today" has to be exact to be useful against the plan's limit. Clock times aren't values either. |
| 2026-11-08 | WX-6.3 | Hours get one of the five icons by the tile's thresholds, with cloud cover standing in for sunshine (sunshine share = 1 − cloud cover); rain or snow from a 50% chance when AccuWeather expects precipitation that hour. AccuWeather's icon numbers aren't used. | Same icons and rules as the tile (WX-3.3). 9 Nov: at night, the moon and partly cloudy night icons (Chris). |
| 2026-11-08 | WX-6.1 | Classic Views (WX-1.1), not Compose like the Tennis app. The top block uses the tile's gradient for today's condition (Palette.kt, copied from the launcher). | WX-1.1 is a MUST. The gradient ties the page to the tile without copying the tile. |
| 2026-11-08 | WX-2.4 | Choosing another town clears the old town's forecast until the new one arrives. | Showing the old town's days under the new town's name would be wrong. |
| 2026-11-08 | WX-4.5 | Release key: EC P-256 (alias `weather`), made in the session; Chris pastes it into GitHub secrets. | Same as Wotworth/Tennis: short enough to paste from a tablet. |
| 2026-11-09 | WX-2.9 | Three real replies (city search, 5 days, 12 hours for Hastings-on-Hudson, key 344606) recorded in the session with Chris's key, saved as test fixtures. They contain no key. CI still never calls AccuWeather. | Checks the field names against a real reply (WX-3.2). The key is held by the session's network proxy, never seen or stored by Claude. |
