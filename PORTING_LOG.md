# Porting log: daspay_release → kmp-migration-ui-foundation

Source: `origin/daspay_release`, changes after merge base `db4afe0` (8 commits: b31a3aa, cc3f8b5, 1ac13af, 820d6c7, 62d16d8, 21e807b, a6a5764, 27f1a54).
Target branch: `kmp-migration-ui-foundation-port-release` (from `origin/kmp-migration-ui-foundation` @ 845e2cd).
Changes are re-implemented in the KMP structure, never merged or cherry-picked.

Paths: `cM` = `shared/src/commonMain/kotlin/com/paymentoptions/pos`, `aM` = androidMain, `iM` = iosMain.
Status: **ported** · **already present** · **skipped** (with reason) · **not ported** (secrets, by instruction).

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
| 32 | B5 | Cart: dynamic service charge and GST | 62d16d8/21e807b | ported | cM/ui/screens/_flow/foodOrderFlow/Cart.kt | HIGH (charged amount). Per-item ServiceFeeAmount × qty; tax on items+service+additional when CatalogEnabled. Deviation: load() also refreshes merchant settings and recalculates totals, so an old saved cart can't charge a stale amount. Covered by CartTotalsTest. |
| 33 | B5 | Additional charge via Cart API | 62d16d8 | ported | AdditionalChargeBottomSectionContent | |
| 34 | B5 | Item Total / Service Charge / tax rows, grandTotal | 62d16d8/21e807b | ported | FoodMenuBottomSectionContent, ReviewCartBottomSectionContent | Reuses reviewcart CurrencyRow (release defines a duplicate). FoodDetail part was done in B1. |
| 35 | B5 | Product service-fee % field + enabled checkbox; errors clear after 4s | 62d16d8/21e807b | ported | Add/EditProductSectionContent | Blank fee → 0 via toFloatOrNull (release crashes on blank in Edit). Fixed: prices ≥ 1,000 crashed on save (grouped formatter then toFloat). |
| 36 | B5 | FoodOrderFlow: merchant cart, grandTotal, shared PBL request, clear cart after link | 62d16d8/21e807b/27f1a54 | ported | FoodOrderFlow.kt, utils/PlainAmount.kt | HIGH. Fixed: amountToCharge was "1,234.00" for totals ≥ 1,000, which BigDecimal can't parse (release has the same bug); now toPlainAmountString(). Cart → charge screen is #42. |
| 37 | B5 | Geo-restriction dialog before charge | 820d6c7 | ported | FoodOrderFlow.kt | Same as ReceiveMoneyFlow; "OK" still lets the charge proceed (release behaviour). |
| 38 | B5 | Tap QR/link opens browser | 27f1a54 | ported | PaymentQrCodeImage (onClick), ReceiveMoneyFlow, FoodOrderFlow, utils/OpenPaymentUrl.kt | Compose LocalUriHandler (both platforms) instead of the Context-bound UrlOpener. |
| 39 | B5 | Remove "QR not supported" gate | 27f1a54 | ported | ReceiveMoneyFlow, FoodOrderFlow | |
| 40 | B5 | Receipt breakdown + TAX REGISTRATION NO. | 62d16d8/21e807b | ported | new _components/ReceiptBreakdown.kt; Receipt (screen + PDF), Status (card + PDF), TransactionBottom | One shared component instead of three copies; amounts with thousands separators parsed. Uses the device's current merchant settings, as release does. |
| 41 | B6 | Payment path → daspay/payment | a6a5764 | ported | cM/network/ApiEndpoints.kt | Decision: switch, following release. If an environment doesn't serve the new path, every payment there fails. |
| 42 | B6 | TTP: cart → PBL product → cartID, clear cart | 21e807b | ported | payment/TapToPaySupport.createCartProductId; TapChargeMoney (common, Android, iOS), MineSecSDK.processSale(cartID), ChargeMoney, FoodOrderFlow | If creating the product fails, the payment continues without a cartID (release behaviour). |
| 43 | B6 | Amount ≥ CVM limit → MANDATORY_SIGNATURE | 820d6c7 | ported | TapChargeMoney Android + iOS, requiresMandatorySignature() | Only when a mandatory-signature handler is passed (Receive Money). Release also runs it for food orders, which have no handler and hang on the loader; KMP food orders go to success as before. |
| 44 | B6 | Mandatory signature: close/cancel voids, "Signature is required", V2UUID | 62d16d8 | ported | TakeDigitalSignature.kt | Void-on-cancel handler already existed in ReceiveMoneyFlow. |
| 45 | B6 | gatewayNotes as MineSec SALE description | 62d16d8 | ported | TapChargeMoney Android + iOS | iOS keeps "Payment from POS" when there is no note. |
| 46 | B6 | success=false → failure screen | 820d6c7/1ac13af | ported | TapChargeMoney.android.kt | Also: a payment-setup exception shows the failure screen instead of clearing the session (release 820d6c7); after B4 that clear would have wiped the device PIN. iOS already handled this. |
| 47 | B6 | Webhook success only for APPROVED/VOIDED/ADJUSTED | 820d6c7 | ported | network/endpoints/PaymentStatus.isSuccessfulTranStatus; TapChargeMoney Android + iOS | Requires a webhook reply and an approving status. TranStatusTest. |
| 48 | B6 | Refund/void: SDK status, webhook result, hostRespMessage, response.success | 21e807b | ported | dashboard/TransactionActionScreen.kt | Fixed: a failed action used to show the failure and then still open the receipt. |
| 49 | B6 | Notes forwarded on iOS void/refund | 62d16d8 | ported | PaymentStatusMapper (gatewayNotes), TransactionActionScreen.ios.kt | Sent in the webhook acquirer payload, as Android does. |
| 50 | B6 | MineSec profile from merchant MinesecDeviceId with fallback | 1ac13af/21e807b | ported | DPStorageManager.getMerchantProfileId(); Android sale, iOS refund/void, reader MerchantConfig | Decision: device config, else the existing MineSecPlatform.PROFILE_ID. No MineSec ID value added or changed. |
| 51 | B7 | MineSec profile ID constants (PROD) | b31a3aa | not ported | | MineSec ID; only on explicit request. |
| 52 | B7 | MineSec config JSON + licence file | 1ac13af | not ported | | Secret; only on explicit request. Must pair with #54. |
| 53 | B7 | x-api-key per environment | 62d16d8 | not ported | | Secret; only on explicit request. |
| 54 | B7 | MineSec production `headless` artifact for release builds | 27f1a54/a6a5764 | not ported | | Must pair with #52; only on explicit request. |
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
| `d957298` | Items 55–56 | pass | pass | 7/7 pass | fails (pre-existing legacy tree only; no new errors) |

The baseline fix and B1 were verified in one build with B1 applied on top; the baseline commit was not built on its own. Every later batch was built and tested on its own.

`:app` fails on the unused legacy tree `app/src/main/java/com/paymentoptions/pos/ui/composables/**` (~30 files referencing screens that moved to `:shared`; nothing outside it imports it). Left as-is by decision; B1 does not touch it. There are no iOS tests (`iosSimulatorArm64Test` skipped). Test task is `:shared:testAndroidHostTest` (CLAUDE.md says `androidHostTest`).

Already present before porting (no change): MANDATORY_SIGNATURE stage and void message, failure-message plumbing, gatewayNotes to charge, webhook rename, Refund/Void button, shouldShowFullReceipt, parseApiErrorMessage, UrlOpener, formatToPrecisionString/formatAmount, txn-details paymentDetails fallback, navigationBarsPadding, custom date picker, chart net amount, settlement/sign-in/bottom-nav analytics, MyDropdown close, PayNow/QRPH/Samsung logos, all APM drawables, Datadog plugin, uat flavour, MineSec bootstrap logging, crash analytics, analytics API, settlement params, config retry, Payment/SignIn error propagation, APM flags, ConfigJSON and GST/cart models, refund-initiated "View Full Receipt" hidden.
Also already present (found in the Phase 3 file re-check): SdkUnCaughtException, AnalyticsEvent/AppAnalytics (as AnalyticsHelper), SettlementList/SettlementActionScreen deviceNumber, MyBottomNavigationBar analytics, ReceiveMoneyFlowStage, ErrorUtils, TransactionSummaryUtil.shouldShowFullReceipt, PayNow/Samsung logo images, and the 8 APM drawables plus samsung.jpeg (byte-identical in composeResources).

## Phase 3 summary

Checked on 2026-11-14 against `origin/daspay_release` @ `27f1a54` and `origin/kmp-migration-ui-foundation` @ `845e2cd` (neither moved during the port). All 8 release commits after `db4afe0` are covered: every changed release file maps to a row above or to the already-present list.

| Result | Items |
|---|---|
| Ported | 52 (1–50, 55, 56) plus NC-1, NC-2 |
| Skipped by design | 57, 58 |
| Not ported (secrets / MineSec config, only on explicit request) | 51–54 |

Branch `kmp-migration-ui-foundation-port-release` (not pushed): `7e213f1` baseline fix, then `7897a09` B1, `2bec448` B2, `b2989e7` B3, `866c7cc` B4, `8837c92` B5, `e4077a8` B6, `d957298` B8. Final state: `:shared` compiles for Android and iOS, the iOS framework links, 7/7 host tests pass; `:app` fails only on the 265 pre-existing errors in the unused legacy tree.

Decisions taken during the port: #23 saved config with flavour fallback; #28 follow release (`appenv`, bootstrap URL, bearer token); #41 switch to `daspay/payment`; #50 device-config profile with the existing built-in fallback; #55 port on both platforms; #56 remove the server signature section.

Where KMP now deliberately differs from release:
- Amounts of 1,000+ are sent and parsed without a thousands separator (release breaks food-order Tap to Pay and product save at 1,000+).
- Food orders at or above the CVM limit don't enter the mandatory-signature step (release hangs on the loader there).
- A saved cart is recalculated on load with current merchant settings.
- Failed refunds/voids stay on the failure screen (KMP used to open the receipt anyway).
- API-error analytics never include response bodies.
- Pay-by-link and base URL fall back per flavour, not to production.

What is still missing for a production release:
- B7 (#51–54): production MineSec profile/config/licence, per-environment x-api-key, and the MineSec production `headless` artifact. Without them the KMP build keeps its current MineSec profile, config, licence and the stage SDK artifact.
- Backend confirmation that `daspay/payment` (#41) and the authenticated `appenv` config request (#28) work on UAT and production.
- Apple review of the Start Tap to Pay button icon and copy (#55) for Tap to Pay on iPhone.

## Manual test checklist

Run on a release-signed Play internal-testing build (Android) and a TestFlight build (iOS), against UAT first. Payments and MineSec first; stop and report on the first failure in sections 1–2.

**1. Tap to Pay payments (Receive Money)**
- [ ] Sale below the CVM limit (e.g. 10.00) with a note → approved → success screen; backend transaction shows the note; MineSec receipt shows the note as description.
- [ ] Sale at/above the CVM limit (200.00) → mandatory signature screen → sign → Confirm → success; signature on the receipt.
- [ ] Same, but close or Cancel on the signature screen → payment is voided → failure screen says it was voided; backend shows the void.
- [ ] Same, Confirm without signing → "Signature is required", stays on the screen.
- [ ] Declined card (or cancel on the reader) → failure screen, never success.
- [ ] Payment API error (e.g. airplane mode after tapping Start) → failure screen; still signed in; device PIN not asked again.
- [ ] Amount of 1,234.56 → charged exactly 1,234.56.
- [ ] Start button reads "Start Tap to Pay" (Android) / "Start Tap to Pay on iPhone" (iOS); NFC-off and developer-options dialogs still appear.
- [ ] Payment requests go to `daspay/payment` (check the backend or proxy logs) on UAT and production.

**2. MineSec setup, refund and void**
- [ ] Fresh install → sign in → device PIN → Tap to Pay set-up completes using the merchant's MinesecDeviceId profile (check logs); merchant without MinesecDeviceId still works with the built-in profile.
- [ ] Refund a Tap to Pay sale with a note → success → receipt opens; backend webhook payload has the note (iOS and Android).
- [ ] Void a sale with a note → same.
- [ ] Refund/void that the host declines → failure screen with the host's message; returns to the transaction, does not open a receipt.
- [ ] Refund of a QR / pay-by-link transaction (no MineSec) → success; a declined one shows the gateway message.

**3. Food order (cart, service charge, GST)**
- [ ] Merchant with catalog enabled and a tax rate: menu shows Item Total and Service Charge; review shows "<TaxName> (<rate>%)"; grand total = items + service + additional + tax.
- [ ] Merchant with catalog disabled: no tax row, no tax charged.
- [ ] Product with service fee enabled vs disabled: only enabled items add their fee × quantity.
- [ ] Additional charge with a note → included in totals and in tax.
- [ ] Food-order Tap to Pay → charged the grand total; backend payment has a cartID whose lines match the cart; cart is empty afterwards.
- [ ] Food-order total of 1,000+ by Tap to Pay, QR and link → all succeed.
- [ ] Food order above the CVM limit → goes to success (no signature step).
- [ ] QR and pay-by-link: link uses the configured pay-by-link URL for the flavour; tapping the QR or link opens it in the browser; cart is cleared after the link is created.
- [ ] Geo-restricted location → "Device Restricted" dialog before charging (Receive Money and Food Order).
- [ ] Add product priced 1,500.00 with service fee 5 → saves; edit it → price shows 1500.00, saves; blank fee saves as 0.

**4. Config, session and storage**
- [ ] UAT build talks to the UAT API (not dev) before and after sign-in; production build to production.
- [ ] Fresh install: no config request before sign-in; config downloads right after sign-in (manual and fingerprint); restart → splash downloads config.
- [ ] Transaction-details link on receipt/status opens the configured URL.
- [ ] Logout → next launch asks for sign-in and device PIN; biometrics off.
- [ ] Let the refresh token expire (or revoke it on the backend) → session cleared, re-authentication; a plain network drop during refresh does not wipe the device PIN.

**5. Receipts and history**
- [ ] Receipt, status and transaction details show "N/A" (not "null") for missing fields; Notes row shows gateway notes, else the reference remark.
- [ ] Food-order receipt shows the items / service / additional / tax breakdown and TAX REGISTRATION NO. (catalog enabled only), on screen and in the PDF.
- [ ] Success screen shows the just-captured signature; no "Customer Signature" section.
- [ ] Transaction history dates around 1 January show the correct year; tapping a chart bar does nothing.

**6. Analytics and logs (Android; iOS analytics is a no-op today)**
- [ ] Datadog RUM shows screen views, logout, merchant selection, payment-method clicks and API errors (method + path + status only, no bodies).
- [ ] Send Logs includes the build info line (version, build type, flavour, environment, base URL).

## Follow-ups (not release items)

- iOS `storage/AppStorage.ios.kt` keeps tokens in NSUserDefaults; the deleted dead file was the only Keychain-backed one.
- Week-year `YYYY` date patterns remain in about 12 places (receipt, status, success/failed, refund, dashboard summary, ReceiveMoneyFlow ExpiryDate, TakeDigitalSignature); release only fixed transaction history.
- Transaction-details URL has no UAT/production default before the first config download.
- iOS has no analytics backend (AnalyticsHelper is a no-op there).
- `utils/UrlUtils.kt` file name contains an invisible U+200E character; `payment_taptopay.png` is 1.4 MB; `ClientHeadlessImpl` is duplicated in `:app` and `:shared`.
- `:app` legacy tree `ui/composables/**` (265 compile errors) can be deleted.
