#!/usr/bin/env bash
# WX-2.3: no host but dataservice.accuweather.com and api.weather.gov (forecast discussion, Chris 9 Nov) may appear in the app's code, resources or merged
# manifest. Licence/namespace URLs that are never fetched are listed below.
# Usage: check-hosts.sh <dir or file>...
set -euo pipefail
# weather.gov appears only as a name on screen and in comments; requests can go only to
# api.weather.gov (NwsNet and the network security config).
allowed='^(dataservice\.accuweather\.com|api\.weather\.gov|weather\.gov|schemas\.android\.com|www\.w3\.org|developer\.accuweather\.com|developer\.android\.com)$'
found=$(grep -rhoE '[a-zA-Z][a-zA-Z0-9.-]*\.(com|org|net|io|gov|dev|app|co|uk|de|fr)\b' "$@" --include='*.kt' --include='*.xml' --include='*.java' 2>/dev/null \
  | grep -vE '^(vnd\.|io\.github|android\.|androidx\.|java\.|javax\.|kotlin\.|org\.json|org\.junit)' | sort -u || true)
bad=0
for h in $found; do
  if [[ "$h" =~ $allowed ]]; then echo "ok   $h"; else echo "FAIL $h"; bad=1; fi
done
[ "$bad" = 0 ] && echo "PASS: no other host in $*"
exit $bad
