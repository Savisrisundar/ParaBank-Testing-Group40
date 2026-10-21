# Pulkit: verified Selenium execution

Scope: Bill Pay, Loan Request and Find Transactions only. Source: Pulkit's uploaded SeleniumTest-40.zip, including original Maven Surefire XML and per-method result/screenshot files. These are reviewed records of the user's execution, not a new execution by the reviewer.

| Class | Tests | Failures | Errors | Skipped | Surefire class time, seconds |
|---|---:|---:|---:|---:|---:|
| BillPayTest | 7 | 0 | 0 | 0 | 110.394 |
| FindTransactionsTest | 10 | 0 | 0 | 0 | 160.434 |
| LoanRequestTest | 3 | 0 | 0 | 0 | 50.003 |

The supplied combined Maven screenshot shows BUILD SUCCESS, 20 tests, zero failures/errors/skips, total Maven duration 5:24, finishing 21 October 2026 at 01:24:22 AEDT. Per-method UTC timestamps correspond to the same run. All 20 method names in the XML match the core-case catalogue. All 20 per-method result files report PASSED. Core execution does not prove the whole team's Selenium suite passed.

The source contains three additional exploratory probes: BILL-004, LOAN-002 and LOAN-003. Maven excludes the exploratory tag by default. These proposed-rule investigations are not part of the 20-test claim and have no passed execution claim here. Retain and explain their purpose; do not describe exclusions as fixes to application defects.

## Report paragraph

Pulkit's assigned Selenium classes were executed together in IntelliJ IDEA using Java, Maven, Selenium ChromeDriver and JUnit 5 on 21 October 2026. The selected core suite executed 20 tests: seven Bill Pay, three Loan Request and ten Find Transactions tests. The supplied Surefire reports record zero failures, errors or skipped tests. Assertions cover exact payment confirmations, persisted transaction details, monetary balance changes, explicit loan approval/denial, completed transaction searches and field validation. Each test establishes fresh customer state through the UI. Three proposed-rule exploratory tests remain separate from this acceptance result. Spira publication and the full team suite require separate execution evidence.

## Evidence retention

Original XML/text reports and per-method evidence are in execution-evidence-2026-10-21 beside repo-overlay. Keep that folder outside target. The final-state images capture the UI at test completion: some show Accounts Overview or Account Details because the final assertion checks persistence. They do not individually display every intermediate interaction. Supplement with the camera-on demonstration.

## Technical points to explain

- BaseTest uses JUnit @BeforeEach/@AfterEach for a fresh Chrome session and quit. AssignedTestSupport inherits that lifecycle and adds the evidence callback.
- Fixtures register unique customers and create payments within the test, reducing dependency on previous sessions and shared database state.
- WebDriverWait polls for the exact account option and completed result containers. Empty rows alone do not establish a completed empty search.
- BigDecimal comparisons verify cents without floating-point rounding assumptions.
- Assertions include equality, inequality, null checks, boolean conditions, collections and grouped assertions. Locators include ID, name, CSS and link text across the inherited helper/test flow.
- A generic server error does not count as successful validation or a lending-policy denial in these core tests.
- The loan approval/denial tests rely on the demonstrated demo lending configuration. Changed external configuration needs a documented investigation, not a weakened assertion.
- CDP warnings appear in this successful run; they are warnings, not the test summary.
