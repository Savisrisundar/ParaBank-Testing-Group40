# ParaBank Cucumber — Assignment 3, Part 3

This is the complete combined project. No patching or file replacement is needed.

## Open and run
1. Extract the ZIP into a NEW folder (keep your old project as a backup).
2. In IntelliJ, select File > Open and choose the extracted ParaBankCucumber folder containing pom.xml.
3. Load the Maven project when prompted and wait for dependency downloads.
4. Use JDK 17 or later and installed Google Chrome.
5. Select the bundled RunCucumberTest run configuration; if IntelliJ does not detect it, right-click src/test/java/com/parabank/runner/RunCucumberTest.java to create its JUnit configuration.
6. In Run > Edit Configurations, set PARABANK_USERNAME and PARABANK_PASSWORD under Environment variables to your ParaBank demo credentials. No credentials are bundled.
7. Run RunCucumberTest. Inspect all six results and target/cucumber-report.html.

## Test account
Use a dedicated demo user with at least two accounts, and a funded account with at least $25. Do not run other tests against this user simultaneously. The suite selects accounts dynamically, verifies preconditions, and does not register users or reset the shared site. ParaBank may reset its data, requiring fresh account setup.

## Six scenarios
- Successful $1 transfer: source and destination balance checks.
- Empty transfer amount: error and no transfer confirmation.
- Successful $1 bill payment: confirmation and balance check.
- Missing payee name: validation and unchanged balance.
- Successful $100 loan with $20 down payment: approval and new account.
- Missing loan amount with $1 down payment: observed error, no approval/new account, and unchanged account IDs and balances.

## Verification and limitation
The user's earlier execution passed five original scenarios; the original loan-denial scenario failed because the site approved the request. The replacement missing-amount scenario was manually verified through the live browser, including unchanged accounts and balances. Its Java step mapping and archive structure were checked, but the combined Java/Cucumber suite has NOT been compiled or run here. Run it in IntelliJ before claiming six passing tests or submitting.

The loan error is a characterization of an unsuccessful request, NOT a business-rule denial or proof of correct input validation. ParaBank displays a generic internal error instead of a helpful required-field message. Explain this potential defect in the demonstration. If your tutor specifically requires a business-rule Denied result, this scenario is not a substitute for that requirement. See VERIFICATION-NOTES.txt for investigation details.

## Submission
After verifying execution, name the submission ZIP CucumberTest-GroupNo.zip using your actual group number. Include the required camera-on demonstration (maximum 3 minutes), team contribution form and AI declaration as instructed by the brief. GitHub contribution history and recording remain separate tasks.
