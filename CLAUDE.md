# Weather

## About me

- I have no computer. I drive you from the Claude app on a tablet, read reports on GitHub, and install builds on my phone from GitHub release pages. I can't run commands, use adb or open Android Studio.
- I'm an engineer, but MATLAB is the only language I know. Ask me about behavior and looks, never about code. When you need a decision, give me two or three options in plain language, with your recommendation.
- I pay for compute and for AccuWeather calls. Keep each session to its task, and don't redo work that already passes.

## Rules

- docs/REQUIREMENTS.md is the source of truth, and its section 0 applies: MUST/SHOULD/COULD, requirement IDs (WX-…) in commits and reports, every tunable value in WeatherTokens.kt, decisions logged in DECISIONS.md, and stop and ask me when a requirement conflicts with Android or with another requirement. If this file and the spec disagree, ask me.
- Start each session by reading docs/STATUS.md, then only the spec sections the task needs. Update STATUS.md each time you push and before you stop to wait for me.
- One phase at a time (section 9). Don't start a phase until I've accepted the previous one on my phone.
- Work on main: commit and push there (you have my permission; I don't review pull requests). CI on main builds what I install. Push at checkpoints. Always push before you stop to wait for me.
- Before pushing, compile and run the unit and screenshot tests in the session (see docs/cloud-setup.md). After pushing, check the workflow run. Never call a build ready until CI has passed and the release exists.
- If GitHub refuses a push that touches .github/workflows/, push everything else, then give me the workflow file in a code block and numbered steps to create it in GitHub's web editor.
- Never claim something works on the phone. Say what CI proved and give me numbered steps for what only the phone can show.
- The look is Metro (WX-6.1): no ripple effects, Material components or icons, rounded cards, shadows or floating buttons. Condition icons are only the live tile's five (WX-6.3). Every number shown follows WX-6.2's rounding.
- Never commit the signing key, its password or the AccuWeather key. CI never calls AccuWeather (WX-2.9). The only network host is dataservice.accuweather.com (WX-2.3).
- Code copied from Wotworth/wp-launcher keeps a header naming the file and commit.
- The tile interface (WX-4, docs/LAUNCHER-CHANGES.md) is fixed: the launcher codes against it. Tell me before changing it.
- Bug reports: I'll paste the crash log from settings, or attach screenshots. Reproduce the bug in a test where you can, then fix it.
