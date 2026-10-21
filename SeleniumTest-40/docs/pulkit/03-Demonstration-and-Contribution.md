# Pulkit: demonstration and contribution

## Camera-on segment: approximately 90–110 seconds

This is one segment of the shared SeleniumRecording-40.mp4, whose total limit is five minutes. Agree timing with Rayyan and any other Selenium contributors. Keep your camera visible and show IntelliJ, source, execution and results. Rehearse and explain in your own words.

| Time | Screen/action | Explanation |
|---|---|---|
| 0–12 s | Show three test classes under src/test | Identify your Bill Pay, Loan Request and Find Transactions scope: 7 + 3 + 10 core cases. |
| 12–32 s | BILL-001 and BILL-008 with pay helper | Explain exact confirmation, persisted debit, balance change, and one-cent precision. |
| 32–49 s | LOAN-001 and assertDenied | Explain explicit approval/new account/down payment, and denial with no account or balance change. A technical error is not a denial. |
| 49–68 s | TXN-005, TXN-003 and search helper | Explain same-test transaction setup, actual captured ID/date, inclusive boundaries and waiting for completed results. |
| 68–85 s | Brief execution clip then supplied combined result/XML | Show genuine execution footage and the 20/0/0/0 result. Clearly label any cut from execution to its result; do not present the five-minute full run as live within this segment. |
| 85–110 s | Actual Spira mapped case and newly published run, once ready | Show method-to-case mapping, automatic outcome and requirement link. Coordinate this part with the integration owner. |

Do not claim Spira success until the mapping and automatic run exist. If the team member demonstrates shared integration, your segment can focus on your implementation and results while they show the mappings for both contributors.

## Practice wording

My assigned Selenium functionalities are Bill Pay, Loan Request and Find Transactions. I revised and extended the existing tests to verify exact outcomes and persisted banking state. Each test establishes fresh customer data. Bill Pay checks the correct debit and input validation, including a one-cent payment. Loan tests check an approved account and explicit denials without unintended changes. Transaction searches use records created during the same test and verify IDs, dates and amounts. Explicit waits prevent assertions against an unfinished page, and BigDecimal handles money. The combined core run passed all 20 tests with no failures, errors or skips. Three proposed-rule probes remain separately labelled exploratory. [Once verified: show and explain automatic Spira reporting here.]

## Questions to prepare for

- Why create fresh users? To reduce reliance on shared data surviving a database reset.
- Why not just check a success heading? A correct heading alone does not prove the intended amount/account was persisted.
- Why use BigDecimal? It supports exact decimal monetary comparisons.
- Why explicit waits? Account options and search results load asynchronously.
- What distinguishes failed validation from a technical error? A field/business explanation with unchanged state, rather than a generic server error.
- Why separate exploratory cases? Their financial rules are proposals that need confirmation; their execution is not included in the core acceptance claim.
- What does the screenshot callback do? Captures the final UI after the test and before teardown; it does not upload to Spira or change test outcomes.

## Contribution entry: completed work only

Name: Pulkit Sharma. Enter your real student ID, GitHub username and signature in the actual team form. Do not invent a percentage; use the team's agreed share.

Revised and extended the team's Selenium tests for Bill Pay, Loan Request and Find Transactions. Added isolated test-data setup, exact account-selection waits, monetary and persisted-transaction checks, explicit loan-decision verification, completed-result checks, and per-test execution evidence capture. Executed the 20 core tests together in IntelliJ using Maven and ChromeDriver; the supplied run passed all 20 with zero failures, errors or skips. Prepared requirement-to-test traceability, test-case specifications and supporting execution evidence for team integration.

Add GitHub commit/PR references after committing. Add Spira integration, report export and video completion only after you actually complete or participate in those activities. Describe collaboration accurately; the original team base and other members' classes remain credited to their contributors.

## Your remaining completion gates

1. Commit/push your source and documentation from your own GitHub account; merge after team review.
2. Complete actual Spira artefacts/mappings with the owner, execute with automatic reporting and verify run records.
3. Supply this execution evidence and the Spira exports for the single team Selenium PDF.
4. Record your camera-on segment and combine into the team's five-minute video.
5. Complete/sign your contribution form section with accurate responsibilities and agreed percentage.
