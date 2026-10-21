# GitHub integration: Pulkit's assigned work

Repository: https://github.com/Savisrisundar/ParaBank-Testing-Group40

Compared against main commit 5c820dbd39034afd271dd1c1b1d3358d0311af8c on 21 October 2026. The repository's Selenium subtree is still the older version. Apart from trailing whitespace/checkout line endings, the uploaded App, base classes, browser smoke test, Registration, Login and Open Account match that snapshot. This overlay changes only Pulkit's three classes and their Maven/support configuration. It does not replace Cucumber or JMeter.

## IntelliJ steps

1. Keep the uploaded working project and its execution evidence as a backup.
2. In IntelliJ, use Git → Clone (or Get from Version Control), select the team repository URL, and choose a NEW local folder. Authenticate with your own GitHub account. Open the SeleniumTest-40 Maven project in that checkout.
3. Update from main before editing. Create a branch named feature/pulkit-selenium-core. If it already exists, inspect it rather than overwriting it.
4. Copy the CONTENTS of this handoff's repo-overlay into the repository ROOT. SeleniumTest-40 should merge into the existing SeleniumTest-40 folder; do not create a nested duplicate project. Confirm replacement of the three assigned classes and pom.xml after reviewing their differences. If the repository has gained a Spira dependency/configuration since this comparison, merge the Maven edits rather than overwriting that newer work.
5. Reload Maven in IntelliJ. Review the Commit window: modified pom.xml plus the three classes; new two helper classes, mapping template, project .gitignore and docs/pulkit. Do not commit target, raw generated reports or API keys. Existing teammate classes should not be changed.
6. The supplied run already verifies this uploaded source. If merging introduces code/configuration changes, run the affected checks. For a fresh combined core run use Maven Execute Goal (without mvn):

   test -Dtest=BillPayTest,LoanRequestTest,FindTransactionsTest -DexcludedGroups=exploratory

7. Commit with a meaningful message, for example: test: extend Bill Pay, Loan Request and transaction search coverage. Push your branch under your own account and open a PR to main.
8. Ask the team to review/merge it. A successful local commit is not a pushed/merged contribution. Retain the final commit and PR links for the contribution form.

## Suggested PR description

Expands Bill Pay, Loan Request and Find Transactions with precise persisted-state assertions and fresh UI-created fixtures. Adds exact account-option waits, completed-search checks and final-state evidence capture. Preserves the existing shared base and other contributors' classes. Three proposed-rule investigations remain explicitly tagged exploratory and excluded from core runs.

Validation: supplied IntelliJ/Maven execution on 21 October 2026 records 20 selected core tests, zero failures, errors or skips. This is not a claim that the full team suite or Spira publication has passed. spira-case-map.properties remains an inactive planning template pending real IDs and the reporting extension.

## README coordination

The root README still identifies Assignment 2, describes an older 26-test result and credits Selenium only to Rayyan. Coordinate a truthful update: preserve historical results with their scope/date and list both contributors' actual responsibilities. Do not replace the whole-team result with Pulkit's subset result. The source overlay leaves the shared root README for that coordinated edit.

## Spira and submission

Keep the selected Maven exclusions and merge the actual reporting adapter once available. Final Canvas SeleniumTest-40.Zip must contain the complete team project. This handoff ZIP is a working integration package, not a replacement for the final Canvas deliverables. Final report and video still require team integration.
