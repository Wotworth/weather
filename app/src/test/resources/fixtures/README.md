# Test fixtures (WX-2.9)

AccuWeather replies in the shape of the Core Weather API's OpenAPI schemas on developer.accuweather.com
(Daily Forecasts 5 days, Hourly Forecasts 12 hours, City search; read 8 Nov 2026). They contain no key.

They were written from the schemas, not recorded from a live call: CI never calls AccuWeather, and no
key was available when they were made. The location keys and figures are made up. Once the app has
fetched a real forecast, a real reply can replace them (open question in docs/STATUS.md).

- `daily-5day.json`: a sunny, a partly cloudy, a cloudy, a rainy and a snowy day (WX-3.6).
- `daily-fog-missing.json`: a day with fog that clears, and a day with missing values (WX-3.6).
- `hourly-12hour.json`: 12 hours, two of them showers.
- `cities-hastings.json`, `cities-paris.json`: city search matches (Paris: a duplicate is dropped).
