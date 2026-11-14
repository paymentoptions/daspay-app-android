# Porting log: daspay_release → kmp-migration-ui-foundation

Source: `origin/daspay_release`, changes after merge base `db4afe0` (8 commits: b31a3aa, cc3f8b5, 1ac13af, 820d6c7, 62d16d8, 21e807b, a6a5764, 27f1a54).
Target branch: `kmp-migration-ui-foundation-port-release` (from `origin/kmp-migration-ui-foundation` @ 845e2cd).
Changes are re-implemented in the KMP structure, never merged or cherry-picked.

Paths: `cM` = `shared/src/commonMain/kotlin/com/paymentoptions/pos`, `aM` = androidMain, `iM` = iosMain.
Status: **ported** · **already present** · **skipped** (with reason) · **pending** (later batch).

| # | Batch | Item | Source | Status | KMP files changed | Note |
|---|---|---|---|---|---|---|
| 1 | B1 | "N/A" instead of "null" on receipt/status/failed/success screens + PDF | a6a5764 | ported | cM/ui/screens/_flow/receiveMoneyFlow/receipt/ReceiptBottomSectionContent.kt, .../transactionfailed/TransactionFailedBottomSectionContent.kt, .../transactionsuccessful/TransactionSuccessfulBottomSectionContent.kt, cM/ui/screens/transactiondetails/StatusBottomSectionContent.kt | Same field set as release; Date and CurrencyCode left as-is (release did too). |
| 2 | B1 | Receipt header in brand colour, inside the card | a6a5764 | ported | cM/ui/composables/_components/ReceiptHeaderView.kt, ReceiptBottomSectionContent.kt | Goes with #8. |
| 3 | B1 | PayNow flag showed the QRPH logo | 27f1a54 | ported | cM/ui/composables/_components/paymentimagerow/PaymentApmsRow.kt | KMP-only bug, fixed to match release. |
| 4 | B1 | GCash uses `gcash_apms`; `gscash_apms` removed | 27f1a54 | ported | cM/.../images/apms/GsCashPayImage.kt; deleted composeResources/drawable/gscash_apms.xml and app/src/main/res/drawable/gscash_apms.xml | No other references. |
| 5 | B1 | Token screen: "Enter Device PIN" copy, "Continue" button, error clears after 4 s | 62d16d8/1ac13af | ported | cM/ui/screens/token/BottomSectionContent.kt | |
| 6 | B1 | Splash: remove 1 s delay before navigating when signed in | 21e807b/a6a5764 | ported | cM/ui/screens/splash/SplashScreen.kt | 4 s → 3 s was already present. Config gating is #30. |
| 7 | B1 | Logo spacer 20 → 8 dp | a6a5764 | ported | cM/ui/composables/layout/sectioned/SectionedLayout.kt | navigationBarsPadding was already present. |
| 8 | B1 | Receipt layout: 0.9 height, blurred top, no zig-zag | a6a5764 | ported | cM/ui/screens/transactiondetails/TransactionDetailsScreen.kt, TransactionStatusScreen.kt, cM/ui/screens/_flow/receiveMoneyFlow/ReceiveMoneyFlow.kt, cM/ui/screens/_flow/foodOrderFlow/FoodOrderFlow.kt | |
| 9 | B1 | Tapping a chart bar no longer navigates | 820d6c7 | ported | aM/.../transactionshistory/TransactionsGroupedBarChart.android.kt, iM/.../TransactionsGroupedBarChart.ios.kt | Both actuals. |
| 10 | B1 | FoodDetail: service-fee % via formatAmount, "+fee" amount | 62d16d8/21e807b | ported | cM/ui/screens/_flow/foodOrderFlow/foodmenu/FoodDetail.kt | ProductDesc null fallback not needed (non-null in KMP model). |
| 11 | B1 | RectangleCheckbox component | 62d16d8 | ported | cM/ui/composables/_components/RectangleCheckbox.kt (new) | Used by #35 (B5). Interaction source is now `remember`ed. |
| 12 | B1 | DashedDivider | 21e807b | ported | cM/utils/DashedDivider.kt (new) | Used by #40 (B5). |
| 13 | B1 | Date patterns `YYYY` → `yyyy` (week-year bug) | 820d6c7 | ported | cM/ui/screens/transactionshistory/BottomSectionContent.kt | All 9 patterns in the file, including the API filter dates. Same bug remains in FoodOrderFlow/ReceiveMoneyFlow ExpiryDate and TakeDigitalSignature (not changed on release; follow-up). |
| 14 | B2 | Screen-view analytics in shared Navigator | b31a3aa | ported | cM/ui/navigation/Navigator.kt | Collects currentBackStackEntryFlow → trackScreenNavigation(from, to). Also covers the auth redirects, so release's extra explicit calls there aren't duplicated. |
| 15 | B2 | Logout / dashboard / payment-method analytics | b31a3aa | ported | cM/ui/screens/settings/BottomSectionContent.kt, cM/ui/screens/dashboard/DashboardScreen.kt, cM/ui/screens/_flow/receiveMoneyFlow/ReceiveMoneyFlow.kt, .../chargemoney/ChargeMoneyBottomSectionContent.kt | Mapped to KMP AnalyticsHelper (same pattern as bottom-nav logout): click events, trackLogout on success, trackApiError on failure; trackDashboardNavigation("Dashboard") on enter; trackMerchantSelection(dasmid, method) on method change; select_payment_method_* click. Release "logout_initiated" has no KMP event (click covers it). TTP image/start clicks already tracked as trackTapToPayInitiated. |
| 16 | B2 | Global API-error tracking (Ktor) | cc3f8b5 | ported | cM/network/KtorClient.kt | HttpResponseValidator: non-2xx → trackApiError("METHOD /path", status, description); request exceptions → trackApiError with throwable. Unlike release, the error body is not sent (avoids consuming it and sending customer data). Existing ad-hoc trackApiError calls can now double-report. |
| 17 | B2 | Build info in sent logs; provideUi log | 1ac13af | ported | cM/Platform.kt (expect buildInfoSummary), aM/Platform.android.kt, iM/Platform.ios.kt, iM/shared/MainViewController.kt, app/.../ClientApp.kt, cM/ui/screens/logger/SendLogsScreen.kt, aM/ClientHeadlessImpl.kt, app/.../ClientHeadlessImpl.kt | Android fills it from BuildConfig in ClientApp; iOS uses NSBundle version/build plus the environment and base URL given to MainViewController (no flavor on iOS). |
| 18 | B3 | getGatewayNotes with Referenceremark fallback | 21e807b | ported | utils/AcquirerResponseUtils.kt; Receipt (x2), Failed, Successful, Status, TransactionBottom | Notes row now falls back to Referenceremark. Notes text is not logged (release logged it). |
| 19 | B3 | parseAcquirerResponse, AcquirerResponse as JsonElement | 820d6c7/21e807b | ported | DataModels AcquirerResponse: List<JsonElement>?; parser accepts object or JSON-string entries | kotlinx instead of Gson; lenient Json (ignoreUnknownKeys, coerceInputValues). jsonObjectToMap not ported (unused). |
| 20 | B3 | PaymentDetails fields nullable | 820d6c7 | ported | DataModels PaymentDetailsResponseData: 17 fields `String? = null` | All consumers already had fallbacks. Kept KMP types for Amount (Double) and TransactionID (Long). |
| 21 | B3 | Amount JSON missing `}` in TransactionMapper | 820d6c7 | ported | androidMain TransactionMapper.android.kt | HIGH: payment-status payload. iOS has no equivalent mapper. |
| 22 | B4 | Pay-by-link URL from config | a6a5764 | ported | DPStorageManager.getPayByLinkUrl(); ReceiveMoneyFlow (3), FoodOrderFlow (3) | Saved ConfigJSON.PayByLinkURL, else a per-flavour default (release falls back to prod for every flavour). |
| 23 | B4 | Base / transaction-details URL from stored config | a6a5764 | ported | Android + iOS DPStorageManager actuals; StatusBottomSectionContent | Decision: saved config, else the flavour's bootstrap URL. Transaction-details default is known only for DEV; UAT/PROD rely on downloaded config. |
| 24 | B4 | storeAppConfig skips blanks | a6a5764 | ported | Android + iOS dpStoreAppConfig, DPStorageManager.storeAppConfig | |
| 25 | B4 | Persist CvmLimit | 820d6c7 | ported | DPStorageManager.saveCvmLimit / getCvmLimit (default 200) | As on release, nothing saves it from config yet (release has that line commented out), so the limit stays 200. |
| 26 | B4 | getMerchantSettings | 62d16d8 | ported | DPStorageManager.getMerchantSettings() | Used by the B5 cart work. |
| 27 | B4 | Logout clears token/device config/biometrics | 62d16d8/21e807b | ported | DPStorageManager.clearSharedPreferences() | HIGH: every caller now also clears the device PIN status, device config and biometrics, including the payment-start error path in TapChargeMoney (release does the same). |
| 28 | B4 | Config fetch from bootstrap URL with auth + blank check | 21e807b/a6a5764 | ported | endpoints/AppConfig.kt, ConfigurationManager, KtorClient | Decision: follow release. `appenv`, bootstrap URL, bearer token (config removed from Ktor's public list), blank BaseAPIURL/TransactionDetailsURL rejected; skipped when not signed in. buildTimeBaseUrl is no longer overwritten. |
| 29 | B4 | Config init after sign-in | a6a5764 | ported | TokenAutoRefresher.onUserSignedIn(), called from manual and fingerprint sign-in | onUserSignedIn was never called on KMP; it now also schedules proactive token refresh, as on release. |
| 30 | B4 | Splash downloads config only when signed in | a6a5764 | ported | SplashScreen | |
| 31 | B4 | Refresh 401/403 clears session | 27f1a54 | ported | TokenRefreshService.refresh() → TokenRefreshResult; KtorClient, TokenRepository | HIGH (auth). 401/403 or ERR_AUTH_0003 → full session clear + re-auth; other failures unchanged. |
| 32–40 | B5 | Dynamic GST / service charge, cart, products, PBL request, receipt breakdown, geo-restriction, QR click, QR gate | 62d16d8/21e807b/820d6c7/27f1a54 | pending | | |
| 41–50 | B6 | Payment path, cartID, CVM signature, mandatory signature void, MineSec description, failure handling, webhook rules, refund/void checks, profile id | various | pending | | High risk. |
| 51–54 | B7 | MineSec profile constant, MineSec config/licence, x-api-key, headless prod artifact | various | pending | | Secrets; only on explicit request. |
| 55 | B8 | StartTapToPayButton + "Ready to receive payment" | a6a5764 | ported | new receiveMoneyFlow/StartTapToPayButton.kt; ReceiveMoneyFlow, FoodOrderFlow | Decision: port on both. Label is "Start $tapToPayDisplayName" ("Tap to Pay on iPhone" on iOS). Review the NFC wave icon and copy against Apple's Tap to Pay on iPhone guidelines before an iOS release. Icons were already in composeResources. |
| 56 | B8 | Remove server signature section from success screen | 820d6c7 | ported | TransactionSuccessfulBottomSectionContent | Decision: remove, like release. The just-captured signature still shows; the server-fetched "Customer Signature" (getSignature) is gone from this screen. |
| 57 | — | Datadog credentials in encrypted prefs | b31a3aa | skipped | | KMP reads BuildConfig directly (different design). |
| 58 | — | EncryptedPrefs caching/logging | a6a5764 | skipped | | KMP uses multiplatform-settings. |
| NC-1 | B1 | Xcode build phase: find Homebrew openjdk@17 before java_home | a6a5764 | ported | iosApp/iosApp.xcodeproj/project.pbxproj | |
| NC-2 | B1 | security-crypto in version catalog, 1.1.0-alpha06 → 1.1.0 | b31a3aa/a6a5764 | ported | gradle/libs.versions.toml, shared/build.gradle.kts | |

## Build status

| Commit | What | :shared Android compile | :shared iOS compile + framework link | :shared host tests | :app |
|---|---|---|---|---|---|
| 7e213f1 | Baseline fix (not a release item): missing ttp_placeholder.xml, stray KtorClient.reset(), dead iosMain/network/AppStorage.ios.kt, NSRoundingMode import | pass | pass | 1/1 pass | fails (pre-existing) |
| 7897a09 | B1: items 1–13, NC-1, NC-2 | pass | pass | 1/1 pass | fails (pre-existing) |
| `2bec448` | Items 14–17 | pass | pass | 1/1 pass | fails (pre-existing legacy tree only; no new errors) |
| `b2989e7` | Items 18–21 | pass | pass | 1/1 pass | fails (pre-existing legacy tree only; no new errors) |
| `866c7cc` | Items 22–31 | pass | pass | 1/1 pass | fails (pre-existing legacy tree only; no new errors) |
| `8837c92` | Items 32–40 | pass | pass | 6/6 pass (+5 CartTotalsTest) | fails (pre-existing legacy tree only; no new errors) |
| `e4077a8` | Items 41–50 | pass | pass | 7/7 pass (+1 TranStatusTest) | fails (pre-existing legacy tree only; no new errors) |
| B8 commit | Items 55–56 | pass | pass | 7/7 pass | fails (pre-existing legacy tree only; no new errors) |

Both rows were verified in one build with B1 applied on top of the baseline fix; the baseline commit was not built on its own.

`:app` fails on the unused legacy tree `app/src/main/java/com/paymentoptions/pos/ui/composables/**` (~30 files referencing screens that moved to `:shared`; nothing outside it imports it). Left as-is by decision; B1 does not touch it. There are no iOS tests (`iosSimulatorArm64Test` skipped). Test task is `:shared:testAndroidHostTest` (CLAUDE.md says `androidHostTest`).

Follow-ups noticed (not release items): iOS `storage/AppStorage.ios.kt` keeps tokens in NSUserDefaults (the deleted file was the only Keychain-backed one); `YYYY` week-year patterns remain in FoodOrderFlow/ReceiveMoneyFlow ExpiryDate and TakeDigitalSignature.

Already present before porting (no change): MANDATORY_SIGNATURE stage and void message, failure-message plumbing, gatewayNotes to charge, webhook rename, Refund/Void button, shouldShowFullReceipt, parseApiErrorMessage, UrlOpener, formatToPrecisionString/formatAmount, txn-details paymentDetails fallback, navigationBarsPadding, custom date picker, chart net amount, settlement/sign-in/bottom-nav analytics, MyDropdown close, PayNow/QRPH/Samsung logos, all APM drawables, Datadog plugin, uat flavour, MineSec bootstrap logging, crash analytics, analytics API, settlement params, config retry, Payment/SignIn error propagation, APM flags, ConfigJSON and GST/cart models, refund-initiated "View Full Receipt" hidden.
