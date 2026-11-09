# Weather: requirements

**Source of truth** (CLAUDE.md). Version **v1.0**, 8 Nov 2026: the wp-launcher session's handoff
(`Wotworth/wp-launcher` branch `ccr-fa86e307-85zu7m`, `docs/handoffs/weather-app.md`, copied below
unchanged in sections 1–5, 7 and 8) plus Chris's requirements for the app's own screens (section 6,
8 Nov).

## 0. How this spec works

- **MUST** is required, **SHOULD** is expected unless there's a stated reason, **COULD** is optional.
- Requirement IDs (WX-…) go in commit messages and reports.
- Every tunable value (colour, size, timing, limit) lives in `app/src/main/java/io/github/wotworth/weather/WeatherTokens.kt`.
- Decisions go in `DECISIONS.md` with the date and the requirement.
- When a requirement conflicts with Android or with another requirement: stop and ask Chris.
- Phases: section 9. Reports go in `docs/reports/` with their screenshots.

---

## 0b. About Chris, and how to work (from the handoff)

- Chris has no computer. He drives Claude from the Claude app on a tablet, reads reports on GitHub
  and installs builds from GitHub release pages. He can't run commands, use adb or open Android
  Studio.
- He's an engineer, but MATLAB is the only language he knows. Ask him about behaviour and looks,
  never about code. For a decision, give two or three options in plain language with your
  recommendation.
- Set the repository up the way Wotworth/Tile (the Alarms & Clock app) is set up, and copy its
  conventions:
  - `CLAUDE.md`, `docs/REQUIREMENTS.md`, `docs/STATUS.md`, `DECISIONS.md` and `docs/reports/`;
  - a GitHub Actions build that runs the unit and screenshot tests and publishes a signed release
    APK;
  - signing secrets stored only in GitHub secrets.
  Never commit a signing key or any password.
- Chris creates the empty GitHub repository himself (suggested name `Wotworth/Weather`). Ask him
  for it if it isn't attached to your session.
- The look everywhere is Windows 10 Mobile (Metro), matching wp-launcher, the Zune player and the
  clock app. That means:
  - no ripple effects, Material components or Material icons;
  - no rounded cards, shadows or floating buttons;
  - Selawik text;
  - the accent colour only for on-states.
- Chris will write the requirements for the app's own screens (what you see when it's open)
  separately. This document covers everything else: the data source, the API key, privacy, and
  the live tile on the launcher's Start screen.

## 1. Scope

| ID | Requirement |
|---|---|
| WX-1.1 | MUST: a standalone Android app, package `io.github.wotworth.weather`, display name **Weather**. minSdk 33, Kotlin, classic Views (as wp-launcher). |
| WX-1.2 | MUST: AccuWeather is the only weather data source. Chris pays for the API plan. |
| WX-1.3 | MUST: the app feeds a live tile on wp-launcher's Start screen that looks **exactly** like the launcher's current weather tile (section 5). The launcher draws the tile; the app supplies the forecast. |
| WX-1.4 | MUST: tapping the tile opens this app (the launcher handles that, section 7). |
| WX-1.5 | The app's own screens (hourly forecast, chance of precipitation, cloud cover, humidity and so on): requirements to come from Chris. Fetch and store hourly data so those screens can use it (WX-2.6). |

## 2. AccuWeather

| ID | Requirement |
|---|---|
| WX-2.1 | MUST: Chris enters the API key in the app's settings. It is stored on the phone only, encrypted with the Android Keystore. It is never in the repository, the APK, logs, backups, crash reports or screenshots. The settings show only its last 4 characters. |
| WX-2.2 | MUST: the key is sent only to AccuWeather's API host over HTTPS. Read AccuWeather's current developer documentation to find out whether the key goes in the `apikey` query parameter or in an `Authorization: Bearer` header. Their developer portal changed in 2025; don't rely on memory. |
| WX-2.3 | MUST: the app's only network host is AccuWeather's API host. All requests go through one class that allows only that host, HTTPS and the listed paths and parameters, and doesn't follow redirects (as the launcher's [`WeatherNet.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/WeatherNet.kt) does). No analytics, ads, crash-reporting SDKs or other libraries that use the network. A CI check fails the build if any other host appears in the code or the merged manifest. |
| WX-2.4 | MUST: no location permission and no live location. The town is chosen by typing its name, using AccuWeather's city search. Only the typed text and the chosen town's location key are sent (as the launcher does now; see its privacy note at the top of [`Weather.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Weather.kt#L11-L24)). The default town is Hastings-on-Hudson, NY. Keep up to 5 recent towns. |
| WX-2.5 | MUST (Chris, 9 Nov: "Shouldn't it be at least once every 30 minutes given I get 500 calls per day?"): the 5-day daily forecast (`details=true`, `metric=true`) and the next 12 hours are fetched together every 30 minutes in the background (2 calls; 96 a day), with WorkManager and a network constraint; also when the town changes, on refresh, and on opening if the last try is over 20 minutes old. Android may delay background work while the phone sleeps. Replaces the launcher's four slots a day. |
| WX-2.6 | MUST: hourly data comes from the 12-hour endpoint with `details=true`, fetched with each update (WX-2.5). Chris's plan ("core-weather/starter", checked 9 Nov) has no 24-, 72- or 120-hour forecast; AccuWeather names its "elite" plan for those. |
| WX-2.7 | MUST: stay well under the plan's call limit. Count calls per day on the phone and show "calls today" in settings. Cache the town's location key; never look it up again for a town already chosen. Before building, ask Chris which plan he bought and its limit (open question Q1). |
| WX-2.8 | MUST: on any failure (no network, bad key, over the limit, unreadable reply), keep the last good forecast and show it, and record the error for the settings page. Never show an empty tile because one update failed. |
| WX-2.9 | MUST: CI never calls AccuWeather (it costs money and would need the key). Tests use recorded JSON replies committed as test fixtures, with no key in them. |

## 3. The forecast the tile needs

The launcher's tile shows days, not hours. Each day has the date where the town is, one of five
conditions, and a high and a low. The launcher's model is
[`Weather.Day`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Weather.kt#L138-L142),
with the five conditions in
[`Weather.Kind`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Weather.kt#L25-L32).

| ID | Requirement |
|---|---|
| WX-3.1 | MUST: up to 5 days, from today (the town's local date). High and low in °C, **exact (not rounded)**. A missing value stays missing. The launcher does the rounding (WX-5.2). |
| WX-3.2 | MUST: each day gets one condition: `SUNNY`, `PARTLY_CLOUDY`, `CLOUDY`, `RAIN` or `SNOW`. Use the launcher's own rules, so that AccuWeather days pick icons the way the tile picks them today. Port [`classifyOpenMeteo`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Weather.kt#L247-L268) and its [`Thresholds`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Weather.kt#L40-L52) unchanged, and feed it from AccuWeather's daily details for the whole day (Day plus Night):<br>• chance = the larger of the Day and Night `PrecipitationProbability`;<br>• precipitation (mm) = Day plus Night `TotalLiquid`;<br>• snow (cm) = Day plus Night `Snow`;<br>• sunshine = `HoursOfSun` × 3600 s;<br>• daylight = `Sun.Set` − `Sun.Rise` in seconds.<br>Check these field names against a recorded reply. |
| WX-3.3 | MUST: AccuWeather's own icon numbers (1–44) are **not** used for the tile. They may be used on the app's own screens only if Chris's screen requirements say so. |
| WX-3.4 | MUST: the town label in the launcher's form: "Hastings-on-Hudson, NY" in the US (the state's two-letter code), "Paris, France" elsewhere. See [`Place.label`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Weather.kt#L59-L70). AccuWeather's `LocalizedName`, `AdministrativeArea.ID` and `Country.LocalizedName` give these. |
| WX-3.5 | MUST: a °F/°C setting in the app (default °F). The launcher uses it for the tile. |
| WX-3.6 | MUST: unit tests cover the classification against recorded AccuWeather replies: a sunny, a partly cloudy, a cloudy, a rainy and a snowy day, a day with fog that clears, and a day with missing values. |

## 4. The tile interface (names are fixed; the launcher codes against them)

This works like the clock app's provider: Wotworth/Tile `docs/LAUNCHER-CHANGES.md`, and
wp-launcher [`ClockTile.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/ClockTile.kt).

| ID | Requirement |
|---|---|
| WX-4.1 | MUST: a read-only content provider with authority `io.github.wotworth.weather.tile` (insert, update and delete throw). It answers only `io.github.wotworth.wplauncher` and `io.github.wotworth.wplauncher.preview`, and only when they are signed with the launcher's release key, SHA-256 `a76a8518adf98b85d4972f180338ca8c3582c1470d7305800206899195a6741d`. Any other caller gets a SecurityException. The clock app's provider shows how to do this check. |
| WX-4.2 | MUST: `content://io.github.wotworth.weather.tile/state` returns one row with these columns:<br>• `version` (long): changes whenever anything below changes;<br>• `town` (text): the label, WX-3.4;<br>• `units` (text): `F` or `C`;<br>• `fetched_at` (long, epoch ms): 0 if no forecast yet;<br>• `source` (text): `AccuWeather`;<br>• `error` (text or null): the last update's error, if it failed. |
| WX-4.3 | MUST: `content://io.github.wotworth.weather.tile/days` returns up to 5 rows, oldest first, with the columns `date` (text, ISO `2026-11-08`, the town's local date), `kind` (text, one of the five names in WX-3.2), `high_c` (real or null) and `low_c` (real or null). Include days from yesterday on; the launcher drops past days itself. |
| WX-4.4 | MUST: call `notifyChange` on `content://io.github.wotworth.weather.tile` after every successful update, and after a change of town or units. |
| WX-4.5 | MUST: the release APK is signed with the app's own key. Put its SHA-256 in the repo's `docs/LAUNCHER-CHANGES.md`, so the launcher can accept the provider only from this app. |
| WX-4.6 | SHOULD: a debug-only stand-in (or test hooks) that serves fixed days, so the launcher's CI can test the tile without the key. Match the clock app's "fakeclock" approach and say in LAUNCHER-CHANGES.md how to build it. |

## 5. How the tile looks (identical to today, by construction)

| ID | Requirement |
|---|---|
| WX-5.1 | MUST: the app does **not** draw the tile. The launcher draws it with its existing code, unchanged, from the WX-4 data. The tile then keeps everything it has today:<br>• the gradient per condition;<br>• the five white Fluent icons, with the yellow sun;<br>• Selawik text, the layout and the spacing;<br>• 1, 3 or 5 days on small, medium or wide tiles;<br>• the optional slow gradient animation;<br>• the town at the top-left, and day names like "MON".<br>A picture drawn by the app could not animate, and would drift from the launcher's code over time. |
| WX-5.2 | MUST: temperatures are rounded by the launcher exactly as now: to the nearest 5 in the shown unit, halves up (72 → 70, 73 → 75), with a degree sign. See [`roundTo5` and `shownTemp`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Weather.kt#L272-L276). That's why WX-3.1 sends exact °C. |

## 6. The app's own screens (Chris, 8 Nov 2026)

Chris's words: "For the UI inside the app, please use something that is influenced and inspired —
but not too heavily reliant on — the metro type interface of the tennis app. Do not take this too
literally though: the emphasis should be on the forecast. Please use the same icons as used on the
live tile. All numeric values (temperature, humidity, % chance of precipitation, % cloud cover,
literally anything) should be rounded to the nearest 5. For instance, 51 should be rounded to 50,
48 to 50, so on. If the value is less than 10 but greater than −10, then round to the nearest odd
digit. Focus should be simplicity."

| ID | Requirement |
|---|---|
| WX-6.1 | MUST: Metro-inspired, after the Tennis app (black page, Selawik, small all-caps app title, large lowercase headers, flat square blocks), but the forecast is the page. Simple. |
| WX-6.2 | MUST: every number shown is rounded to the nearest 5; values between −10 and 10 (exclusive) to the nearest odd number, or 0 (Chris, 9 Nov: "0 should equal 0"). In the unit shown. Ties go up (DECISIONS.md). The launcher's tile uses the same rule (Chris, 9 Nov; replaces WX-5.2's nearest 5 for single digits). |
| WX-6.3 | MUST: the live tile's five icons (`wx_*`) for conditions, on every screen; at night (AccuWeather's IsDaylight), the Fluent moon for clear hours and the Fluent partly cloudy night icon (Chris, 9 Nov). Fluent UI System Icons, Filled, white. |
| WX-6.4 | SHOULD: the forecast page shows today (icon, high, low, phrase, chance of rain, humidity, cloud cover), the next 12 hours, and 5 days. |
| WX-6.5 | SHOULD: settings hold the key, town, units, calls today, the last error, a manual update and the crash log. |
| WX-6.6 | MUST (Chris, 9 Nov): town, day and month names are capitalised ("Hastings-on-Hudson", "Today", "Mon", "November 10"). The page shows the town's name only, with no state or "…"; the town and the top temperature shrink to fit rather than being cut off at the phone's text size. |
| WX-6.7 | MUST (Chris, 9 Nov): tapping today's block or a day opens that day: its block, its daytime and overnight halves (phrase, precipitation, cloud cover, humidity), and whichever of the next 12 hours fall on it. Hourly forecasts for later days need AccuWeather's "elite" plan (WX-2.6); open question Q5. |
| WX-6.8 | MUST (Chris, 9 Nov): cloud cover is shown wherever precipitation is (top block, each hour, each day, each half day), and every percentage is labelled ("precip 25%", "cloud 30%", "humidity 60%"). WX-6.2's rounding applies. |
| WX-6.9 | MUST (Chris, 9 Nov): pressing refresh turns the refresh icon until the update comes back (or 30 s without a network), finishing its turn, so the press visibly registered. |
| WX-6.10 | MUST (Chris, 9 Nov): pulling the forecast page down from the top and letting go refreshes, as WX-6.9. A small "PULL TO REFRESH" / "RELEASE TO REFRESH" hint shows behind the page; no Material spinner. |
| WX-6.11 | MUST (Chris, 9 Nov): the app's snow block is "a little Christmas festive", sophisticated and slight: evergreen-to-midnight gradient, a faint warm glow from one corner, a light still snowfall, and a few faint Fluent snowflakes (Chris, 9 Nov: "use snowflakes"). No cartoon shapes. Text keeps 4.5:1 contrast. The tile's snow gradient is unchanged. All year (Chris, 9 Nov, Q6). |
| WX-6.12 | MUST (Chris, 9 Nov; second pass: "lean more into the differences", "be less slight"): every card has a version per season, by the day's date (winter Dec–Feb, spring Mar–May, summer Jun–Aug, fall Sep–Nov). Winter: crisp ice blue, cool light, white snowflakes and snowfall. Spring (Easter): violet to rose, butter-yellow light, pastel eggs in five colours and clover. Summer: deep green, a strong golden sun, gold sparkles and green leaves. Fall: burnt orange to russet, amber light, many falling leaves in gold, red, brown, cream and orange. Decorations are bold and plentiful but never behind text or the condition icon. Cloud and rain mute the season; rain adds raindrops. Snow stays festive in every season (WX-6.11, Q6). Text keeps 4.5:1 contrast. |

## 7. Launcher side (the wp-launcher session does this, not you)

Write anything the launcher needs from you in your repo's `docs/LAUNCHER-CHANGES.md`: the signing
key's SHA-256, how to build the stand-in, and any change to section 4. Chris will pass it on. For
your information, the launcher will:
- make the tile for `io.github.wotworth.weather` the weather tile while the provider resolves and
  is signed with the app's key. Otherwise it is a plain app tile, and MSN Weather's tile works as
  today;
- draw it from `/state` and `/days` with the current code, and redraw it on `notifyChange`;
- stop its own Open-Meteo and NWS requests while the app's tile is in use, so the phone makes no
  duplicate weather requests;
- open the app on tap.

## 8. Open questions for Chris (ask at the start, with these defaults)

| # | Question | Default |
|---|---|---|
| Q1 | Which AccuWeather plan, and its daily or monthly call limit? | Ask; no default |
| Q2 | May the app use the phone's location to pick the town? | No: town search only, as in the launcher |
| Q3 | Should the tile also show AccuWeather's severe-weather alerts? | No: the tile stays exactly as it is |
| Q4 | Hourly data in the background (for a future notification) or only when the app is opened? | Answered 9 Nov: every 30 minutes with the daily forecast (WX-2.5) |
| Q6 | Festive snow all year, or only in the Christmas season (for example 1 Dec to 6 Jan)? | Answered 9 Nov: all year |
| Q5 | Hourly forecasts for every day need AccuWeather's "elite" plan. Upgrade, or keep daytime and overnight for later days? | Keep daytime and overnight (no upgrade) |

## 9. Phases

| Phase | Content | Status |
|---|---|---|
| PH-1 | Everything above: AccuWeather client, daily and hourly forecasts, tile provider, stand-in, forecast and settings pages, CI and signed release. | Built 8 Nov; waiting for Chris's test |

## Appendix A. Reusing the launcher's tile code in the app (for its own screens)

If Chris's screen requirements ask the app to show the same art, copy these files rather than
redrawing anything:
- a preview of the tile in settings, or a header in the same style;
- the same five icons and gradients.

Copy them and keep the Selawik and Fluent licence files with them. They are written to be
self-contained; replace `R` and `Metrics` with the app's own.

| What | Where (wp-launcher, commit a444997) |
|---|---|
| Conditions, thresholds, classification, rounding, town label, US state codes | [`Weather.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Weather.kt) (the forecast and places parts; drop the Open-Meteo and NWS code) |
| Palette (gradient stops per condition, contrast-checked), grid, spacing, text sizes, layout boxes, animation drift | [`WeatherDesign.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/WeatherDesign.kt) (pure Kotlin, no Android) |
| Drawing the tile into a bitmap, with the gradient as a moving shader | [`WeatherArt.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/WeatherArt.kt) (`render`, `Art.draw`) |
| How Start builds the cells (day names, shown temperatures, 1/3/5 days by tile size) | [`HomeActivity.kt` `weatherFor`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/HomeActivity.kt#L1940-L1988) |
| The tile view's weather drawing and animation hook | [`TileView.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/TileView.kt#L173-L195) and [line 668](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/TileView.kt#L666-L675) |
| Icon size and day gap tokens | [`Metrics.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/java/io/github/wotworth/wplauncher/Metrics.kt#L44-L49) (`WEATHER_ICON`, `WEATHER_DAY_GAP`, `LABEL_INSET`) |
| The five icons | [`res/drawable/wx_*.xml`](https://github.com/Wotworth/wp-launcher/tree/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/res/drawable) (`wx_sunny`, `wx_partly_cloudy`, `wx_cloudy`, `wx_rain`, `wx_snow`) |
| Fonts | [`res/font/selawik_*.ttf`](https://github.com/Wotworth/wp-launcher/tree/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/main/res/font) |
| Licences | [`FLUENT-ICONS-LICENSE.txt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/FLUENT-ICONS-LICENSE.txt), [`SELAWIK-OFL.txt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/SELAWIK-OFL.txt) |
| Unit tests for all of the above (classification, rounding, palette contrast, layout) | [`WeatherTest.kt`](https://github.com/Wotworth/wp-launcher/blob/a444997452257f0bfc9b9e339f77a2243a3ac81e/app/src/test/java/io/github/wotworth/wplauncher/WeatherTest.kt) |
| Why it looks the way it does | wp-launcher `DECISIONS.md` (weather sections) and `docs/design-values.md` |

