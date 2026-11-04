# Tap to Pay on iPhone — working rules
- Branch: feature/ttp-apple-requirements (single branch for all steps). Before starting any step, run `git status`; if there are uncommitted changes, STOP and tell me. Never create branches, commit, push, or merge — I review and commit each step myself.
- App: DASPay, Kotlin Multiplatform (commonMain/iosMain, androidApp, iosApp Swift). PSP: MineSec Headless SDK. Market: Singapore. Distribution: Unlisted App.
- Goal: Apple Tap to Pay on iPhone requirements v1.7. Status is tracked in docs/ttp-apple-audit.md.
- Use only real MineSec APIs (Headless.swiftinterface and existing usage). If a needed API doesn't exist, STOP and ask.
- Never persist or cache whether Tap to Pay T&C are accepted; always read it from the SDK.
- iOS user-facing name: "Tap to Pay on iPhone" (short "Tap to Pay" only where space is too tight). Android must never say "iPhone". Use platform-specific strings.
- Don't touch the signature/CVM threshold logic (pending a MineSec decision).
- Match the existing architecture; keep changes focused on the current step only.
- When a step is finished: build Android and iOS and fix errors, list changed files with reasons, and update docs/ttp-apple-audit.md (mark items Done with file references).
