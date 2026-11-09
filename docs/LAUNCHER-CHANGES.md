# For the launcher (wp-launcher session)

Everything the launcher needs from the Weather app (handoff section 7). Chris passes this on.

## The app

- Package `io.github.wotworth.weather`, display name **Weather**. Tapping the tile opens its launcher activity (`MainActivity`, the normal MAIN/LAUNCHER intent).
- **Release signing key SHA-256** (accept the provider only from a package signed with this):
  `8D:C2:E7:04:D7:92:E3:5C:38:EE:D9:3D:37:39:DA:53:0E:15:54:ED:6E:32:BD:21:6E:96:9B:B2:E1:AB:EF:76`
  (hex, no colons: `8dc2e704d792e35c38eed93d3739da530e1554ed6e32bd216e969bb2e1abef76`). Each release's notes print it too.

## Provider (WX-4, unchanged from the handoff)

- Authority `io.github.wotworth.weather.tile`; read-only (insert, update, delete throw).
- Answers only `io.github.wotworth.wplauncher` and `io.github.wotworth.wplauncher.preview`, signed with the launcher's release key `a76a8518…6741d`. Anyone else: SecurityException.
- `content://io.github.wotworth.weather.tile/state`: one row: `version` (long), `town` (text), `units` (`F`/`C`), `fetched_at` (long, epoch ms, 0 before the first forecast), `source` (`AccuWeather`), `error` (text or null).
- `content://io.github.wotworth.weather.tile/days`: up to 5 rows, oldest first: `date` (ISO, town's local date), `kind` (`SUNNY`, `PARTLY_CLOUDY`, `CLOUDY`, `RAIN`, `SNOW`), `high_c`, `low_c` (real, exact °C, or null). Drop past days yourself.
- `notifyChange` on `content://io.github.wotworth.weather.tile` after every successful daily update, a town change and a units change. (A failed update also changes `/state`'s `error` and `version`; it notifies too.)
- `version` is a CRC-32 of town, units, fetched_at, error and the days: it changes whenever any of them does.

## Rounding (Chris, 9 Nov)

The tile rounds like the app (WX-6.2): nearest 5, except between −10 and 10, where it is the nearest odd number or 0, ties up. Done in wp-launcher main as `Weather.roundShown` (used by `shownTemp`), 9 Nov. The provider still sends exact °C.

## Stand-in for the launcher's CI (WX-4.6)

`fakeweather/` in this repo is a tiny app with the same package and authority, serving fixed days (one of each condition from today; Hastings-on-Hudson, NY; °F; one missing low) and no caller check, so it works signed with CI's debug key. Build it with:

```
./gradlew :fakeweather:assembleDebug
# → fakeweather/build/outputs/apk/debug/fakeweather-debug.apk
```

As with fakeclock, the launcher will need a CI-only second accepted key (its debug key) for the stand-in. Copying `fakeweather/` into the launcher repo (like `fakeclock/`) is the simplest: it is one file and a manifest.
