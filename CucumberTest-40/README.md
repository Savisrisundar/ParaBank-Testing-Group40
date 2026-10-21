# ParaBank Cucumber — Assignment 3, Part 3

## Open and run

1. Extract the ZIP into a NEW folder (keep your old project as a backup).
2. In IntelliJ, select File > Open and choose the extracted ParaBankCucumber folder containing pom.xml.
3. Load the Maven project when prompted and wait for dependency downloads.
4. Use JDK 17 or later and installed Google Chrome.
5. Select the bundled RunCucumberTest run configuration; if IntelliJ does not detect it, right-click src/test/java/com/parabank/runner/RunCucumberTest.java to create its JUnit configuration.
6. In Run > Edit Configurations, set PARABANK\_USERNAME and PARABANK\_PASSWORD under Environment variables to your ParaBank demo credentials. No credentials are bundled.
7. Run RunCucumberTest. Inspect all six results and target/cucumber-report.html.

## Test account

Use a dedicated demo user with at least two accounts, and a funded account with at least $25. Do not run other tests against this user simultaneously. The suite selects accounts dynamically, verifies preconditions, and does not register users or reset the shared site. ParaBank may reset its data, requiring fresh account setup.

## Six scenarios

* Successful $1 transfer: source and destination balance checks.
* Empty transfer amount: error and no transfer confirmation.
* Successful $1 bill payment: confirmation and balance check.
* Missing payee name: validation and unchanged balance.
* Successful $100 loan with $20 down payment: approval and new account.
* Missing loan amount with $1 down payment: observed error, no approval/new account, and unchanged account IDs and balances.

## Verification and limitation



The Cucumber test suite was executed successfully in IntelliJ IDEA on 19 October 2026.



\*\*Execution results:\*\*

\- Total scenarios: 6

\- Passed: 6

\- Failed: 0

\- Functionalities tested: Transfer Funds, Bill Pay and Request Loan



Both successful and unsuccessful scenarios were covered for each functionality.



\*\*Known limitation:\*\* ParaBank is a public demonstration application and its database may reset periodically. The negative Request Loan scenario verifies the application's response to a missing loan amount. The application displays a generic internal error instead of a field-level validation message. This represents an unsuccessful request, not a loan denied under business lending rules.



## submission



The Cucumber project is maintained in the Group 40 GitHub repository.



The required submission deliverables are:



\- `CucumberTest-40.zip` — Cucumber Maven project

\- `CucumberRecording-40.mp4` — maximum 3-minute camera-on demonstration

\- Group Contribution Form

\- Limited Surface AI Declaration



The recording demonstrates the Gherkin scenarios, Selenium step definitions, test execution and results.

