# Pulkit: requirements, test cases and traceability

Proposed Spira artefacts ready for entry. Local keys below are planning identifiers, not real Spira IDs. Create these within the dedicated Selenium Assignment scope; link each case to its listed requirement. Use the available User Story requirement type and an appropriate Functional test-case type, with automation configured as required by the selected integration. Confirm actual type names in the project.

## Requirements / user stories

### BILL-01
As a logged-in customer, I want to pay a bill from my selected account so that the payee receives the intended amount and my account records the correct debit.

### BILL-02
As a logged-in customer, I want incomplete, mismatched or malformed payment details to be rejected so that an invalid payment does not debit my account.

### LOAN-01
As an eligible logged-in customer, I want a qualifying loan request to create a funded loan account so that I can access the approved principal.

### LOAN-02
As a logged-in customer, I want an unaffordable or nonqualifying loan request to receive an explicit denial so that my account and funds remain unchanged.

### TXN-01
As a logged-in customer, I want to find my transactions by ID, amount, date or date range so that I can identify the correct banking records.

### TXN-02
As a logged-in customer, I want a completed search with no matching records to show empty results so that I can distinguish no matches from a failed or unfinished search.

### TXN-03
As a logged-in customer, I want malformed search criteria to receive field validation so that I can correct the criteria before searching.

## Folder mapping

Selenium Assignment → BillPayTest / LoanRequestTest / FindTransactionsTest. The folder names map directly to Java classes. Requirements may be grouped under Bill Pay, Loan Request and Find Transactions in the corresponding requirement hierarchy.

## Shared preconditions

Chrome installed; Java 17 or compatible configured in IntelliJ; Maven dependencies resolved; ParaBank reachable. Each test opens a fresh browser and registers a unique customer. Payment/loan tests verify funding/account preconditions. Search tests create their own payment where needed and capture the displayed date/ID. Run sequentially; avoid simultaneous load testing. If the public demo resets or blocks access, retain the actual failed/blocked outcome and investigate the environment.

## Core cases

### TC_BILL_001_validPayment: Valid payment
- Class: `BillPayTest`; source: `src/test/java/au/edu/rmit/parabank/tests/BillPayTest.java`.
- Requirement: **BILL-01**.
- Purpose: Valid payment.
- Steps/data: Pay 25.00 with complete payee details.
- Expected result: Exact confirmation payee/account/amount; source balance decreases by 25.00; persisted transaction has numeric ID, expected description and debit, and no credit.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_BILL_002_emptyRequiredFields: Empty payment form
- Class: `BillPayTest`; source: `src/test/java/au/edu/rmit/parabank/tests/BillPayTest.java`.
- Requirement: **BILL-02**.
- Purpose: Empty payment form.
- Steps/data: Submit the payment form with all required fields blank.
- Expected result: All nine required-field errors appear; neither success nor technical error is displayed; balance unchanged.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_BILL_003_mismatchedPayeeAccountNumbers: Payee account mismatch
- Class: `BillPayTest`; source: `src/test/java/au/edu/rmit/parabank/tests/BillPayTest.java`.
- Requirement: **BILL-02**.
- Purpose: Payee account mismatch.
- Steps/data: Submit 10.00 with account 12345678 and confirmation 87654321.
- Expected result: Exact account-mismatch validation; payment form remains visible; no success/technical error; balance unchanged.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_BILL_005_nonNumericAccount: Nonnumeric payee account
- Class: `BillPayTest`; source: `src/test/java/au/edu/rmit/parabank/tests/BillPayTest.java`.
- Requirement: **BILL-02**.
- Purpose: Nonnumeric payee account.
- Steps/data: Submit 10.00 using ABCDE in both payee account fields.
- Expected result: Exact invalid-number validation; no success/technical error; balance unchanged.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_BILL_006_nonNumericAmount: Nonnumeric payment amount
- Class: `BillPayTest`; source: `src/test/java/au/edu/rmit/parabank/tests/BillPayTest.java`.
- Requirement: **BILL-02**.
- Purpose: Nonnumeric payment amount.
- Steps/data: Submit complete payee details with amount ABC.
- Expected result: Exact invalid-amount validation; no success/technical error; balance unchanged.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_BILL_007_missingAmount: Missing amount only
- Class: `BillPayTest`; source: `src/test/java/au/edu/rmit/parabank/tests/BillPayTest.java`.
- Requirement: **BILL-02**.
- Purpose: Missing amount only.
- Steps/data: Submit complete payee details with amount blank.
- Expected result: Exact empty-amount validation; no success/technical error; balance unchanged.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_BILL_008_decimalPrecision: One-cent payment
- Class: `BillPayTest`; source: `src/test/java/au/edu/rmit/parabank/tests/BillPayTest.java`.
- Requirement: **BILL-01**.
- Purpose: One-cent payment.
- Steps/data: Pay 0.01 with complete details.
- Expected result: Exact confirmation; source balance decreases by 0.01; persisted debit equals 0.01 and credit is absent.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_LOAN_001_validRequestProcessed: Approved modest loan
- Class: `LoanRequestTest`; source: `src/test/java/au/edu/rmit/parabank/tests/LoanRequestTest.java`.
- Requirement: **LOAN-01**.
- Purpose: Approved modest loan.
- Steps/data: With at least 20.00 available, request 100.00 with down payment 20.00.
- Expected result: Approved status and approval panel; provider/date present; one new numeric account; source debited 20.00; new account credited 100.00.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_LOAN_004_downPaymentExceedsAvailableFunds: Unaffordable down payment
- Class: `LoanRequestTest`; source: `src/test/java/au/edu/rmit/parabank/tests/LoanRequestTest.java`.
- Requirement: **LOAN-02**.
- Purpose: Unaffordable down payment.
- Steps/data: With one account of balance B, request principal 2*(B+1), down payment B+1.
- Expected result: Exact insufficient-down-payment explanation; explicit Denied status/panel; no technical error, new account or balance change.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_LOAN_005_insufficientQualification: Large request without down payment
- Class: `LoanRequestTest`; source: `src/test/java/au/edu/rmit/parabank/tests/LoanRequestTest.java`.
- Requirement: **LOAN-02**.
- Purpose: Large request without down payment.
- Steps/data: With one nonnegative-balance account, request max(balance,1)*1000000 and down payment 0.
- Expected result: Explicit Denied status and explanation; provider/date present; no technical error, new account or balance change.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_001_findByAmount: Search by amount
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-01**.
- Purpose: Search by amount.
- Steps/data: Create payments of 15.00 and 23.00; search the source account for 15.00.
- Expected result: Exact known 15.00 record returned; 23.00 record excluded; every result has amount 15.00.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_002_noMatchingAmount: Unmatched amount
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-02**.
- Purpose: Unmatched amount.
- Steps/data: Create a 15.00 payment; search for 999999999.99.
- Expected result: Completed results container/table with zero transactions and no technical error.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_003_findByDateRange: Inclusive one-day range
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-01**.
- Purpose: Inclusive one-day range.
- Steps/data: Create a payment; use its actual displayed date for both range boundaries.
- Expected result: Known record returned; every result is on the boundary date; results fully loaded.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_004_findByDateWithoutResults: Unmatched single date
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-02**.
- Purpose: Unmatched single date.
- Steps/data: Create a payment; search a date one year before its displayed date.
- Expected result: Completed empty results with no technical error.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_005_findById: Exact transaction ID
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-01**.
- Purpose: Exact transaction ID.
- Steps/data: Create a payment; search its captured transaction ID.
- Expected result: Exactly one record; exact ID, date, description, debit and credit match.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_006_findByDate: Search actual transaction date
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-01**.
- Purpose: Search actual transaction date.
- Steps/data: Create a payment; search its actual displayed date.
- Expected result: Known record returned; all result dates match the selected date.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_007_invalidTransactionId: Invalid transaction ID
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-03**.
- Purpose: Invalid transaction ID.
- Steps/data: Search transaction ID ABC.
- Expected result: Exact Invalid transaction ID validation; form remains; no results or technical error.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_008_invalidDateFormat: Malformed date
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-03**.
- Purpose: Malformed date.
- Steps/data: Search date not-a-date.
- Expected result: Exact Invalid date format validation; form remains; no results or technical error.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_009_invalidAmount: Invalid search amount
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-03**.
- Purpose: Invalid search amount.
- Steps/data: Search amount ABC.
- Expected result: Exact Invalid amount validation; form remains; no results or technical error.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

### TC_TXN_010_excludingDateRange: Range excluding payment
- Class: `FindTransactionsTest`; source: `src/test/java/au/edu/rmit/parabank/tests/FindTransactionsTest.java`.
- Requirement: **TXN-02**.
- Purpose: Range excluding payment.
- Steps/data: Create a payment; search a three-day range ending a year before its displayed date.
- Expected result: Completed empty results; known transaction excluded; no technical error.
- Verified supplied run: **Passed**. Spira case ID / run ID: **pending**.

## Exploratory probes

BILL-004: investigate negative payment; LOAN-002: investigate negative principal; LOAN-003: investigate down payment exceeding principal. These are explicitly proposed-rule investigations, excluded from core runs. If retained in Spira, label them exploratory and distinguish their results from the 20 acceptance cases.

## Automatic reporting acceptance check

1. Obtain the assignment project URL/ID, selected username, actual requirement/case IDs and approved reporting extension/sample from the integration owner. Keep secrets out of source, videos and reports.
2. Replace planning keys with real case IDs and configure/register the actual JUnit reporter. spira-case-map.properties by itself does not publish anything.
3. Check all 20 method-to-case mappings, especially LOAN-004/005 and BILL-008. Do not reuse a case ID simply because a number resembles a Java case name.
4. Execute with the reporter enabled, then verify newly created automatic run records in the intended project/cases, including timestamps and outcomes. Do not manually mark cases passed as a substitute.
5. Check requirement links and export test cases and associated automated runs to PDF. Merge with the team material into SeleniumReport-40.pdf.
6. Record the chosen Spira username and projectId in the contribution form.
