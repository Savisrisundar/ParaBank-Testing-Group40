# ParaBank-Testing-Group40

Group 40's Software Testing Assignment 2. We tested the ParaBank demo banking site
(https://parabank.parasoft.com) with JMeter, Selenium with SpiraPlan, and Cucumber.

## What's in this repo

| Part                         | Files                                         | Done by                                                       |
| ---------------------------- | --------------------------------------------- | ------------------------------------------------------------- |
| Performance testing (JMeter) | `jmeterplan-40.JMX`, `JMeter-Analysis-40.pdf` | Savitha Sundaresan (s4193740)                                 |
| Selenium + SpiraPlan         | `SeleniumTest-40` project                     | Rayyan Sohail (s4073657)                                      |
| BDD (Cucumber)               | `CucumberTest-40` project                     | Thanekachalam Jagadeesan(S4184455), Manoj Balusamy (S4156232) |

## JMeter performance testing - s4193740

The test follows one user journey: Login, Accounts Overview, View Account Details,
Transfer Funds, View Transactions, then Logout.

It runs this journey with 1, 10, 20 and 50 users. Each group has a 10 second ramp-up,
runs for 20 seconds, and runs on its own after the previous group finishes. Every
request has an assertion, using several different types, and each group has a
View Results Tree and an Aggregate Report.

To run it:

1. Register an account on ParaBank and open a second account.
2. Open `jmeterplan-40.JMX` in Apache JMeter 5.6.3.
3. Update the `username`, `password`, `fromAccount` and `toAccount` variables in the Test Plan.
4. Click Start.

The results and analysis are in `JMeter-Analysis-40.pdf`.

## Tools used

Apache JMeter 5.6.3, Java, Maven, Selenium WebDriver (ChromeDriver), JUnit 5,
SpiraPlan, Cucumber and IntelliJ IDEA.

## Selenium functional testing - s4073657

The Selenium tests cover seven areas of ParaBank: Browser Launch, Registration,
Login, Open Account, Bill Pay, Request Loan and Find Transactions.

The tests use Java, Selenium WebDriver and JUnit 5, with Maven to run the suite.
Both positive and negative test cases are included to check valid inputs,
invalid inputs and expected application responses.

To run it:

1. Install Java, Maven and Google Chrome.
2. Open the `SeleniumTest-40` folder.
3. Run `mvn clean test`.
4. View the results in `target/surefire-reports/`.

The final run completed 26 tests: 22 passed and 4 failed. The failed tests
identified possible validation issues with registration inputs, negative bill
payments and loan down payments exceeding the requested loan amount.
