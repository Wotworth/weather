# Test fixtures (WX-2.9)

AccuWeather replies in the shape of the Core Weather API's OpenAPI schemas on developer.accuweather.com
(Daily Forecasts 5 days, Hourly Forecasts 12 hours, City search; read 8 Nov 2026). They contain no key.

The `real-*` files were recorded from AccuWeather on 9 Nov 2026 (Hastings-on-Hudson, location key
344606, `details=true&metric=true`), and confirm the field names. The others were written from the
schemas to cover cases a single real day can't (rain, snow, fog, missing values); their location keys
and figures are made up. CI never calls AccuWeather.

- `daily-5day.json`: a sunny, a partly cloudy, a cloudy, a rainy and a snowy day (WX-3.6).
- `daily-fog-missing.json`: a day with fog that clears, and a day with missing values (WX-3.6).
- `hourly-12hour.json`: 12 hours, two of them showers.
- `real-cities-hastings.json`, `real-daily-5day.json`, `real-hourly-12hour.json`: recorded replies.
- `cities-hastings.json`, `cities-paris.json`: city search matches (Paris: a duplicate is dropped).
