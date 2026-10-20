package com.parabank.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.util.LinkedHashMap;
import java.util.Set;

public class BankingSteps {

    private WebDriver driver;
    private WebDriverWait wait;

    private final Map<String, BigDecimal> beforeBalances =
            new HashMap<>();

    private Map<String, BigDecimal> afterBalances;

    private String sourceAccount;
    private String destinationAccount;

    @Before
    public void setUp() {
        beforeBalances.clear();
        afterBalances = null;
        sourceAccount = null;
        destinationAccount = null;
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @After
    public void tearDown(Scenario scenario) {
        if (driver != null && scenario.isFailed()) {
            try {
                scenario.attach(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES),
                        "image/png", "Failure screenshot");
                scenario.attach(driver.findElement(By.tagName("body")).getText(),
                        "text/plain", "Page text on failure");
            } catch (Exception ignored) { /* Do not hide the original failure. */ }
        }
        if (driver != null) {
            driver.quit();
        }
    }

    // A new user is created for every scenario to avoid depending on old demo data.
    // Registration and account opening are preconditions, not tested features.
    @Given("the customer is logged into ParaBank")
    public void customerIsLoggedIn() {
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        String username = "rmitbdd" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String password = "DemoTest123!";

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));
        } catch (TimeoutException e) {
            throw new AssertionError("TEST SETUP: registration form unavailable; check ParaBank availability or verification page. "
                    + "Current page: " + driver.getTitle(), e);
        }

        fill(By.id("customer.firstName"), "RMIT");
        fill(By.id("customer.lastName"), "BDDTest");
        fill(By.id("customer.address.street"), "10 Test Street");
        fill(By.id("customer.address.city"), "Melbourne");
        fill(By.id("customer.address.state"), "VIC");
        fill(By.id("customer.address.zipCode"), "3000");
        fill(By.id("customer.phoneNumber"), "0400000000");
        fill(By.id("customer.ssn"), "123456789");
        fill(By.id("customer.username"), username);
        fill(By.id("customer.password"), password);
        fill(By.id("repeatedPassword"), password);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[value='Register']"))).click();

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Accounts Overview")));
            wait.until(d -> {
                String page = d.findElement(By.id("rightPanel")).getText();
                if (page.contains("Username already exists") || page.contains("error")) {
                    throw new AssertionError("TEST SETUP: registration failed: " + page);
                }
                return page.contains("Welcome") || page.contains("Your account was created");
            });
        } catch (TimeoutException e) {
            throw new AssertionError("TEST SETUP: new customer registration was not confirmed. "
                    + "Check ParaBank page and registration response.", e);
        }
        System.out.println("Created fresh ParaBank test customer: " + username);
    }

    @Given("the customer has access to two bank accounts")
    public void customerHasTwoAccounts() {
        openAccountsOverview();
        Map<String, BigDecimal> available = readBalances(1);

        if (available.size() < 2) {
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));
            Select accountType = new Select(driver.findElement(By.id("type")));
            accountType.selectByVisibleText("SAVINGS");
            wait.until(d -> !new Select(d.findElement(By.id("fromAccountId"))).getOptions().isEmpty());
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[value='Open New Account'], button[type='submit']"))).click();
            try {
                wait.until(d -> {
                    WebElement panel = d.findElement(By.id("rightPanel"));
                    return panel.getText().contains("Account Opened!")
                            && !d.findElement(By.id("newAccountId")).getText().isBlank();
                });
            } catch (TimeoutException e) {
                throw new AssertionError("TEST SETUP: savings account creation not confirmed", e);
            }
            openAccountsOverview();
        }

        beforeBalances.clear();
        beforeBalances.putAll(readBalances(2));
        Assertions.assertTrue(beforeBalances.size() >= 2,
                "TEST SETUP: two bank accounts must be available");
        System.out.println("Verified test accounts: " + beforeBalances.size());
        beforeBalances.forEach((id, balance) ->
                System.out.println("Account " + id + " | opening balance: $" + balance));
    }

    @Given("the customer is on the Transfer Funds page")
    public void customerIsOnTransferPage() {

        wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Transfer Funds"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("amount")));
    }

    @Given("account {string} has sufficient funds")
    public void accountHasSufficientFunds(String accountId) {

        BigDecimal balance = beforeBalances.get(accountId);

        Assertions.assertNotNull(balance,
                "Account not found: " + accountId);

        Assertions.assertTrue(
                balance.compareTo(BigDecimal.ONE) >= 0,
                "Insufficient balance for a $1 transfer"
        );
    }

    @When("the customer selects an account with sufficient funds")
    public void selectFundedSourceAccount() {
        String enteredAmount = driver.findElement(By.id("amount")).getAttribute("value");
        BigDecimal required = enteredAmount == null || enteredAmount.isBlank()
                ? BigDecimal.ONE : new BigDecimal(enteredAmount);
        sourceAccount = beforeBalances.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(required) >= 0)
                .map(Map.Entry::getKey)
                .sorted()
                .findFirst()
                .orElseThrow(() -> new AssertionError("No account has sufficient funds for " + required));
        selectAccount("fromAccountId", sourceAccount);
    }

    @When("the customer selects a different destination account")
    public void selectDifferentDestinationAccount() {
        Assertions.assertNotNull(sourceAccount, "Select a source account first");
        destinationAccount = beforeBalances.keySet().stream()
                .filter(id -> !id.equals(sourceAccount))
                .sorted()
                .findFirst()
                .orElseThrow(() -> new AssertionError("A different destination account is required"));
        selectAccount("toAccountId", destinationAccount);
    }

    @When("the customer enters the amount {string}")
    public void customerEntersAmount(String amount) {

        WebElement amountField =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.id("amount")));

        amountField.clear();
        amountField.sendKeys(amount);
    }

    @When("the customer leaves the transfer amount empty")
    public void customerLeavesAmountEmpty() {

        WebElement amountField =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.id("amount")));

        amountField.clear();
    }

    @When("selects account {string} as the source account")
    public void selectSourceAccount(String accountId) {

        sourceAccount = accountId;
        selectAccount("fromAccountId", accountId);
    }

    @When("selects account {string} as the destination account")
    public void selectDestinationAccount(String accountId) {

        destinationAccount = accountId;
        selectAccount("toAccountId", accountId);
    }

    @When("clicks the Transfer button")
    public void clickTransferButton() {

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath(
                        "//input[@value='Transfer'] | " +
                                "//button[normalize-space()='Transfer']"
                )
        )).click();
    }

    @Then("the message {string} should be displayed")
    public void verifyMessage(String expectedMessage) {

        boolean displayed = wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.tagName("body"),
                        expectedMessage
                )
        );

        Assertions.assertTrue(displayed,
                "Expected message not displayed: " + expectedMessage);
    }

    @Then("the source account balance should decrease by {string}")
    public void verifySourceBalance(String amount) {

        loadAfterBalances();

        BigDecimal before = beforeBalances.get(sourceAccount);
        BigDecimal after = afterBalances.get(sourceAccount);

        Assertions.assertNotNull(before);
        Assertions.assertNotNull(after);

        BigDecimal expected =
                before.subtract(new BigDecimal(amount));

        Assertions.assertEquals(
                0,
                expected.compareTo(after),
                "Source account balance is incorrect"
        );
    }

    @Then("the destination account balance should increase by {string}")
    public void verifyDestinationBalance(String amount) {

        loadAfterBalances();

        BigDecimal before =
                beforeBalances.get(destinationAccount);

        BigDecimal after =
                afterBalances.get(destinationAccount);

        Assertions.assertNotNull(before);
        Assertions.assertNotNull(after);

        BigDecimal expected =
                before.add(new BigDecimal(amount));

        Assertions.assertEquals(
                0,
                expected.compareTo(after),
                "Destination account balance is incorrect"
        );
    }

    @Then("the transfer confirmation should not be displayed")
    public void transferConfirmationNotDisplayed() {

        String pageText =
                driver.findElement(By.tagName("body")).getText();

        Assertions.assertFalse(
                pageText.contains("Transfer Complete!"),
                "Unexpected transfer confirmation appeared"
        );
    }

    @Given("the customer is on the Bill Pay page")
    public void openBillPay() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("payee.name")));
    }

    @When("the customer submits a bill payment of {string} with payee name {string}")
    public void submitBillPayment(String amount, String payeeName) {
        fill(By.name("payee.name"), payeeName);
        fill(By.name("payee.address.street"), "10 Test Street");
        fill(By.name("payee.address.city"), "Melbourne");
        fill(By.name("payee.address.state"), "VIC");
        fill(By.name("payee.address.zipCode"), "3000");
        fill(By.name("payee.phoneNumber"), "0400000000");
        fill(By.name("payee.accountNumber"), "123456");
        fill(By.name("verifyAccount"), "123456");
        fill(By.name("amount"), amount);
        BigDecimal required = new BigDecimal(amount);
        sourceAccount = beforeBalances.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(required) >= 0)
                .map(Map.Entry::getKey).sorted().findFirst()
                .orElseThrow(() -> new AssertionError("No account has sufficient funds for bill payment"));
        selectAccount("fromAccountId", sourceAccount);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Send Payment']"))).click();
    }

    @Then("the bill payment should be confirmed for {string} and {string}")
    public void verifyBillPayment(String payee, String amount) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("rightPanel"), "Bill Payment Complete"));
        Assertions.assertAll(
                () -> Assertions.assertEquals(payee, driver.findElement(By.id("payeeName")).getText().trim()),
                () -> Assertions.assertEquals(0, money(driver.findElement(By.id("amount")).getText())
                        .compareTo(new BigDecimal(amount))),
                () -> Assertions.assertEquals(sourceAccount, driver.findElement(By.id("fromAccountId")).getText().trim())
        );
    }

    @Then("the missing payee name should be reported")
    public void verifyMissingPayee() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("validationModel-name")));
        String error = driver.findElement(By.id("validationModel-name")).getText();
        Assertions.assertTrue(error.contains("Payee name is required"), "Unexpected validation: " + error);
        Assertions.assertFalse(driver.findElement(By.id("rightPanel")).getText().contains("Bill Payment Complete"));
    }

    @Then("the source account balance should remain unchanged")
    public void verifyUnchangedSourceBalance() {
        loadAfterBalances();
        Assertions.assertNotNull(afterBalances.get(sourceAccount));
        Assertions.assertEquals(0, beforeBalances.get(sourceAccount).compareTo(afterBalances.get(sourceAccount)),
                "An unsuccessful payment must not debit the source account");
    }

    @Given("the customer is on the Request Loan page")
    public void openLoanPage() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
    }

    @When("the customer requests a loan of {string} with down payment {string}")
    public void requestLoan(String amount, String downPayment) {
        fill(By.id("amount"), amount);
        fill(By.id("downPayment"), downPayment);
        sourceAccount = beforeBalances.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(new BigDecimal(downPayment)) >= 0)
                .map(Map.Entry::getKey).sorted().findFirst()
                .orElseThrow(() -> new AssertionError("No account can fund the down payment"));
        selectAccount("fromAccountId", sourceAccount);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Apply Now']"))).click();
    }

    @Then("the loan decision should be {string}")
    public void verifyLoanDecision(String decision) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("loanStatus"), decision));
        Assertions.assertEquals(decision, driver.findElement(By.id("loanStatus")).getText().trim());
        if ("Approved".equals(decision)) {
            WebElement newAccount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newAccountId")));
            Assertions.assertAll(
                    () -> Assertions.assertFalse(newAccount.getText().isBlank(), "Approved loan must create an account"),
                    () -> Assertions.assertFalse(beforeBalances.containsKey(newAccount.getText().trim()),
                            "Loan account must be new")
            );
        } else {
            Assertions.assertTrue(driver.findElements(By.id("newAccountId")).stream()
                    .noneMatch(WebElement::isDisplayed), "Denied loan must not show a new account");
        }
    }

    // Expanded acceptance scenarios. All browser actions remain limited to the three required features.
    private String submittedAmount;
    private String submittedPayee;
    private String expectedLoanAccount;

    @When("the customer transfers {string} between two different accounts")
    public void submitTransferValue(String value) {
        customerIsOnTransferPage();
        customerEntersAmount(value);
        submittedAmount = value;
        chooseSourceForInput(value);
        selectDifferentDestinationAccount();
        clickTransferButton();
    }

    private void chooseSourceForInput(String value) {
        BigDecimal threshold = BigDecimal.ZERO;
        try {
            threshold = new BigDecimal(value);
        } catch (NumberFormatException ignored) { /* Invalid input is intentional. */ }
        final BigDecimal minimum = threshold.signum() > 0 ? threshold : BigDecimal.ZERO;
        sourceAccount = beforeBalances.entrySet().stream()
                .filter(e -> e.getValue().compareTo(minimum) >= 0)
                .map(Map.Entry::getKey).sorted().findFirst()
                .orElseThrow(() -> new AssertionError("TEST DATA: no account funds transfer " + value));
        selectAccount("fromAccountId", sourceAccount);
    }

    @When("the customer transfers the entire available balance to another account")
    public void transferFullBalance() {
        customerIsOnTransferPage();
        sourceAccount = beforeBalances.entrySet().stream()
                .filter(e -> e.getValue().signum() > 0)
                .map(Map.Entry::getKey).sorted().findFirst()
                .orElseThrow(() -> new AssertionError("No funded account"));
        submittedAmount = beforeBalances.get(sourceAccount).toPlainString();
        customerEntersAmount(submittedAmount);
        selectAccount("fromAccountId", sourceAccount);
        selectDifferentDestinationAccount();
        clickTransferButton();
    }

    @When("the customer attempts to transfer {string} from an account with insufficient available balance")
    public void transferInsufficient(String extra) {
        customerIsOnTransferPage();
        sourceAccount = beforeBalances.keySet().stream().sorted().findFirst().orElseThrow();
        submittedAmount = beforeBalances.get(sourceAccount).add(new BigDecimal(extra)).toPlainString();
        customerEntersAmount(submittedAmount);
        selectAccount("fromAccountId", sourceAccount);
        selectDifferentDestinationAccount();
        clickTransferButton();
    }

    @Then("the transfer is confirmed and both account balances change by the exact amount")
    public void verifyTransferFinancialState() {
        verifyMessage("Transfer Complete!");
        BigDecimal amount = new BigDecimal(submittedAmount);
        Map<String, BigDecimal> balances = readBalancesEventually(expectedTransferBalances(amount));
        Assertions.assertEquals(0, beforeBalances.get(sourceAccount).subtract(amount)
                .compareTo(balances.get(sourceAccount)), "Source debit mismatch");
        Assertions.assertEquals(0, beforeBalances.get(destinationAccount).add(amount)
                .compareTo(balances.get(destinationAccount)), "Destination credit mismatch");
    }

    private Map<String, BigDecimal> expectedTransferBalances(BigDecimal amount) {
        Map<String, BigDecimal> expected = new HashMap<>(beforeBalances);
        expected.put(sourceAccount, expected.get(sourceAccount).subtract(amount));
        expected.put(destinationAccount, expected.get(destinationAccount).add(amount));
        return expected;
    }

    @Then("the transfer should be rejected without modifying any account balance")
    public void verifyTransferRejected() {

        String outcome;

        try {
            outcome = wait.until(d -> {
                String text = d.findElement(By.id("rightPanel")).getText();
                String lower = text.toLowerCase();

                if (lower.contains("transfer complete")
                        || lower.contains("error")
                        || lower.contains("insufficient")) {
                    return text;
                }

                boolean invalidInput = d.findElements(
                                By.cssSelector("input:invalid"))
                        .stream()
                        .anyMatch(WebElement::isDisplayed);

                if (invalidInput) {
                    return "BROWSER_VALIDATION";
                }

                return null;
            });

        } catch (TimeoutException e) {
            throw new AssertionError(
                    "Transfer produced no observable result or validation", e
            );
        }

        String lower = outcome.toLowerCase();

        assertRejectedTransactionState(outcome, "Transfer Complete!");

        Assertions.assertFalse(
                lower.contains("internal error"),
                "APPLICATION DEFECT: Transfer caused a server error: " + outcome
        );

        Assertions.assertTrue(
                outcome.equals("BROWSER_VALIDATION")
                        || lower.contains("error")
                        || lower.contains("insufficient"),
                "No appropriate transfer rejection was displayed: " + outcome
        );

        System.out.println("Transfer rejected and account balances unchanged.");
    }

    @When("the customer submits bill details with {string} set to {string}")
    public void billFieldInput(String field, String value) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("payee name", "payee.name");
        fields.put("street address", "payee.address.street");
        fields.put("city", "payee.address.city");
        fields.put("state", "payee.address.state");
        fields.put("postal code", "payee.address.zipCode");
        fields.put("phone number", "payee.phoneNumber");
        fields.put("payee account", "payee.accountNumber");
        fields.put("verify account", "verifyAccount");
        fields.put("amount", "amount");
        Assertions.assertTrue(fields.containsKey(field), "Unknown field: " + field);
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String defaultValue = switch (entry.getKey()) {
                case "payee name" -> "Electricity Company";
                case "street address" -> "10 Test Street";
                case "city" -> "Melbourne";
                case "state" -> "VIC";
                case "postal code" -> "3000";
                case "phone number" -> "0400000000";
                case "payee account", "verify account" -> "123456";
                default -> "1.00";
            };
            fill(By.name(entry.getValue()), entry.getKey().equals(field) ? value : defaultValue);
        }
        submittedAmount = field.equals("amount") ? value : "1.00";
        submittedPayee = field.equals("payee name") ? value : "Electricity Company";
        sourceAccount = beforeBalances.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ONE) >= 0)
                .map(Map.Entry::getKey).sorted().findFirst().orElseThrow();
        selectAccount("fromAccountId", sourceAccount);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[value='Send Payment']"))).click();
    }


    @Then("the bill payment should be rejected without debiting the customer")
    public void verifyBillRejected() {

        String outcome;

        try {
            outcome = wait.until(d -> {
                String panel = d.findElement(
                        By.id("rightPanel")).getText();

                String lower = panel.toLowerCase();

                if (lower.contains("bill payment complete")
                        || lower.contains("internal error")
                        || lower.contains("error!")) {
                    return panel;
                }

                boolean validation = d.findElements(
                        By.cssSelector(
                                ".error, [id^='validationModel-']"
                        )
                ).stream().anyMatch(e ->
                        e.isDisplayed()
                                && !e.getText().trim().isEmpty()
                );

                if (validation) {
                    return panel;
                }

                boolean invalidInput = d.findElements(
                                By.cssSelector("input:invalid"))
                        .stream()
                        .anyMatch(WebElement::isDisplayed);

                if (invalidInput) {
                    return "BROWSER_VALIDATION";
                }

                return null;
            });

        } catch (TimeoutException e) {
            throw new AssertionError(
                    "Bill Pay produced no observable result or validation",
                    e
            );
        }

        String lower = outcome.toLowerCase();

        boolean explicitValidation = outcome.equals("BROWSER_VALIDATION")
                || driver.findElements(By.cssSelector(".error, [id^='validationModel-']"))
                .stream()
                .anyMatch(e -> e.isDisplayed() && !e.getText().trim().isEmpty());

        assertRejectedTransactionState(outcome, "Bill Payment Complete");

        Assertions.assertFalse(
                lower.contains("internal error"),
                "APPLICATION DEFECT: Bill Pay produced a server error: "
                        + outcome
        );

        Assertions.assertTrue(explicitValidation,
                "No visible validation reason for rejected bill payment: " + outcome);

        System.out.println(
                "Bill payment rejection verified; account balances unchanged."
        );
    }


    @When("the customer pays {string} to {string} with valid payee details")
    public void payValidBill(String amount, String payee) {
        submittedAmount = amount;
        submittedPayee = payee;
        submitBillPayment(amount, payee);
    }

    @Then("the bill should be confirmed and the source balance debited by the exact amount")
    public void verifyBillFinancialState() {
        verifyBillPayment(submittedPayee, submittedAmount);
        BigDecimal expected = beforeBalances.get(sourceAccount).subtract(new BigDecimal(submittedAmount));
        Map<String, BigDecimal> target = new HashMap<>(beforeBalances);
        target.put(sourceAccount, expected);
        Map<String, BigDecimal> actual = readBalancesEventually(target);
        Assertions.assertEquals(0, expected.compareTo(actual.get(sourceAccount)), "Bill payment debit mismatch");
    }

    @When("the customer requests a loan of {string} with a down payment of {string}")
    public void requestLoanVariants(String amount, String downPayment) {
        // Keep invalid strings as submitted: this is a validation test, not a Java parsing test.
        fill(By.id("amount"), amount);
        fill(By.id("downPayment"), downPayment);
        sourceAccount = beforeBalances.entrySet().stream().filter(e -> e.getValue().signum() >= 0)
                .map(Map.Entry::getKey).sorted().findFirst().orElseThrow();
        selectAccount("fromAccountId", sourceAccount);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Apply Now']"))).click();
        System.out.println("Current URL: " + driver.getCurrentUrl());

        System.out.println(
                "Loan result panel: " +
                        driver.findElement(By.id("rightPanel")).getText()
        );

        System.out.println(
                "Invalid fields: " +
                        driver.findElements(By.cssSelector("input:invalid")).size()
        );
    }

    @Then("the loan application should be approved and a new account should exist")
    public void verifyApprovedLoanInAccounts() {

        verifyLoanDecision("Approved");

        expectedLoanAccount = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("newAccountId")
                )
        ).getText().trim();

        Assertions.assertFalse(
                expectedLoanAccount.isBlank(),
                "Approved loan must provide a new account number"
        );

        Assertions.assertFalse(
                beforeBalances.containsKey(expectedLoanAccount),
                "Loan account already existed before the request"
        );

        // Navigate once, not repeatedly inside WebDriverWait.
        openAccountsOverview();

        // Wait for the newly created account to appear.
        wait.until(driver -> {
            List<WebElement> accountLinks =
                    driver.findElements(By.cssSelector("#accountTable td a"));

            for (WebElement account : accountLinks) {
                try {
                    if (expectedLoanAccount.equals(account.getText().trim())) {
                        return true;
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                    return false;
                }
            }

            return false;
        });

        System.out.println(
                "Verified approved loan account: " + expectedLoanAccount
        );
    }


    @Then("the loan application should not create an account or change existing balances")
    public void verifyLoanNoMutation() {

        String outcome;

        try {
            outcome = wait.until(d -> {

                String panel = d.findElement(
                        By.id("rightPanel")).getText();

                String lower = panel.toLowerCase();

                // A real loan decision or server response
                if (lower.contains("approved")
                        || lower.contains("denied")
                        || lower.contains("internal error")) {
                    return panel;
                }

                // Check visible validation messages
                boolean validation = d.findElements(
                        By.cssSelector(
                                ".error, [id^='validationModel-']"
                        )
                ).stream().anyMatch(e ->
                        e.isDisplayed()
                                && !e.getText().trim().isEmpty()
                );

                if (validation) {
                    return panel;
                }

                // Browser-side HTML validation
                boolean invalidInput = d.findElements(
                        By.cssSelector(
                                "input:invalid"
                        )
                ).stream().anyMatch(WebElement::isDisplayed);

                if (invalidInput) {
                    return "BROWSER_VALIDATION";
                }

                return null;
            });

        } catch (TimeoutException e) {
            throw new AssertionError(
                    "Loan request produced no observable "
                            + "approval, denial, validation, "
                            + "or browser constraint violation.",
                    e
            );
        }

        String lower = outcome.toLowerCase();

        Assertions.assertFalse(
                lower.contains("approved"),
                "Invalid loan was unexpectedly approved: " + outcome
        );

        Assertions.assertFalse(
                lower.contains("internal error"),
                "APPLICATION DEFECT: Loan request produced "
                        + "an internal server error instead of "
                        + "appropriate validation: " + outcome
        );

        Assertions.assertTrue(
                lower.contains("denied")
                        || outcome.equals("BROWSER_VALIDATION")
                        || lower.contains("required")
                        || lower.contains("invalid")
                        || lower.contains("error"),
                "Loan rejection was not adequately explained: "
                        + outcome
        );

        assertUnchangedAccounts();

        System.out.println(
                "Loan rejection verified with unchanged account balances."
        );
    }


    private void assertNoSuccessfulOutcome(String successText) {
        String text = driver.findElement(By.tagName("body")).getText();
        Assertions.assertFalse(text.contains(successText), "Unexpected success: " + successText);
        String lower = text.toLowerCase();
        Assertions.assertFalse(lower.contains("internal error") || lower.contains("stack trace")
                || lower.contains("exception"), "Server error is not valid business rejection: " + text);
        boolean validation = driver.findElements(By.cssSelector(".error, [id^='validationModel-'], #requestLoanError"))
                .stream().filter(WebElement::isDisplayed)
                .anyMatch(e -> !e.getText().trim().isEmpty());
        Assertions.assertTrue(validation || lower.contains("denied") || lower.contains("error!"),
                "No visible explanation for rejected operation: " + text);
    }

    private void assertUnchangedAccounts() {
        openAccountsOverview();
        Map<String, BigDecimal> observed = readBalances(2);
        Assertions.assertEquals(beforeBalances.keySet(), observed.keySet(), "Account set changed after rejection");
        for (String id : beforeBalances.keySet()) {
            Assertions.assertEquals(0, beforeBalances.get(id).compareTo(observed.get(id)),
                    "Unexpected balance change in account " + id);
        }
    }

    private Map<String, BigDecimal> readBalancesEventually(Map<String, BigDecimal> expected) {
        openAccountsOverview();
        return wait.until(d -> {
            try {
                Map<String, BigDecimal> actual = snapshotBalances(d);
                return expected.equals(actual) ? actual : null;
            } catch (StaleElementReferenceException e) { return null; }
        });
    }

    private Map<String, BigDecimal> snapshotBalances(WebDriver d) {
        Map<String, BigDecimal> balances = new HashMap<>();
        for (WebElement row : d.findElements(By.cssSelector("#accountTable tr"))) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() < 2) continue;
            List<WebElement> links = cells.get(0).findElements(By.tagName("a"));
            if (links.isEmpty()) continue;
            String id = links.get(0).getText().trim();
            String moneyText = cells.get(1).getText().replaceAll("[^0-9.\\-]", "");
            if (!id.isEmpty() && !moneyText.isEmpty()) balances.put(id, new BigDecimal(moneyText));
        }


        return balances;
    }

    private Map<String, BigDecimal> inspectAccountBalances() {
        openAccountsOverview();
        return readBalances(beforeBalances.size());
    }

    private void assertRejectedTransactionState(
            String outcome,
            String successMessage) {

        boolean unexpectedlySuccessful =
                outcome.toLowerCase().contains(
                        successMessage.toLowerCase());

        Map<String, BigDecimal> actualBalances =
                inspectAccountBalances();

        StringBuilder differences = new StringBuilder();

        for (Map.Entry<String, BigDecimal> entry
                : beforeBalances.entrySet()) {

            String account = entry.getKey();
            BigDecimal before = entry.getValue();
            BigDecimal after = actualBalances.get(account);

            if (after == null || before.compareTo(after) != 0) {
                differences.append(
                        "\nAccount " + account
                                + ": before=" + before
                                + ", after=" + after
                );
            }
        }

        Assertions.assertEquals(
                beforeBalances.keySet(),
                actualBalances.keySet(),
                "Transaction changed the account set"
        );

        Assertions.assertFalse(
                unexpectedlySuccessful,
                "Invalid transaction was accepted.\n"
                        + "Response: " + outcome
                        + "\nBalance changes: "
                        + (differences.length() == 0
                        ? "None detected"
                        : differences)
        );

        Assertions.assertEquals(
                0,
                differences.length(),
                "Rejected transaction changed balances: "
                        + differences
        );
    }

    private void fill(By locator, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(value);
    }

    private BigDecimal money(String text) {
        return new BigDecimal(text.replaceAll("[^0-9.\\-]", ""));
    }

    private void selectAccount(String elementId, String accountId) {

        wait.until(d -> {
            Select dropdown = new Select(
                    d.findElement(By.cssSelector("select#" + elementId + ", select[name='" + elementId + "']"))
            );

            return dropdown.getOptions().stream()
                    .anyMatch(option ->
                            accountId.equals(option.getAttribute("value")));
        });

        new Select(driver.findElement(By.cssSelector("select#" + elementId + ", select[name='" + elementId + "']")))
                .selectByValue(accountId);
    }


    private void openAccountsOverview() {

        By accountTable = By.id("accountTable");

        for (int attempt = 1; attempt <= 2; attempt++) {

            wait.until(ExpectedConditions.elementToBeClickable(
                    By.linkText("Accounts Overview")
            )).click();

            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        accountTable
                ));
                return;

            } catch (TimeoutException e) {

                System.out.println(
                        "Accounts Overview did not load, attempt "
                                + attempt
                                + ". URL: " + driver.getCurrentUrl()
                );

                if (attempt == 2) {
                    throw new AssertionError(
                            "TEST SETUP: Accounts Overview unavailable. URL: "
                                    + driver.getCurrentUrl()
                                    + ", page title: " + driver.getTitle(),
                            e
                    );
                }
            }
        }
    }


    private void loadAfterBalances() {

        if (afterBalances == null) {
            openAccountsOverview();
            afterBalances = readBalances(2);
        }
    }

    private Map<String, BigDecimal> readBalances(int minimumCount) {

        return wait.until(d -> {

            Map<String, BigDecimal> balances = new HashMap<>();

            List<WebElement> rows =
                    d.findElements(By.cssSelector("#accountTable tr"));

            for (WebElement row : rows) {

                List<WebElement> cells =
                        row.findElements(By.tagName("td"));

                if (cells.size() < 2) {
                    continue;
                }

                List<WebElement> links =
                        cells.get(0).findElements(By.tagName("a"));

                if (links.isEmpty()) {
                    continue;
                }

                String accountId = links.get(0).getText().trim();

                String balanceText = cells.get(1).getText()
                        .replaceAll("[^0-9.\\-]", "");

                if (!balanceText.isEmpty()) {
                    balances.put(
                            accountId,
                            new BigDecimal(balanceText)
                    );
                }
            }

            return balances.size() >= minimumCount ? balances : null;
        });
    }
}
