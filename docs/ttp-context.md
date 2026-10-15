# Tap to Pay on iPhone — working rules
- App: DASPay, Kotlin Multiplatform (shared commonMain/iosMain + androidApp + iosApp Swift). PSP: MineSec Headless SDK. Market: Singapore. Distribution: Unlisted App.
- Goal: meet Apple's Tap to Pay on iPhone requirements v1.7. Status is tracked in docs/ttp-apple-audit.md.
- Use only real MineSec APIs (Headless.swiftinterface and existing usage). If a needed API doesn't exist, STOP and ask.
- Never persist or cache whether Tap to Pay T&C are accepted; always read it from the SDK.
- User-facing name on iOS: "Tap to Pay on iPhone" (short form "Tap to Pay" only where space is too tight). Android must never say "iPhone". Use platform-specific strings (expect/actual).
- Don't touch the signature/CVM threshold logic (pending a MineSec decision).
- Match the existing architecture and navigation; keep changes minimal and focused on the step.
- After each step: build Android and iOS, fix errors, list changed files with reasons, give a device test checklist, and update docs/ttp-apple-audit.md (mark items Done with file references).
