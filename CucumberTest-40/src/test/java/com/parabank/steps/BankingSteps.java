
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
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("the customer is logged into ParaBank")
    public void customerIsLoggedIn() {

        String username = System.getenv("PARABANK_USERNAME");
        String password = System.getenv("PARABANK_PASSWORD");

        Assertions.assertNotNull(username,
                "Set PARABANK_USERNAME in the run configuration");
        Assertions.assertNotNull(password,
                "Set PARABANK_PASSWORD in the run configuration");

        driver.get(
                "https://parabank.parasoft.com/parabank/index.htm"
        );

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.name("username"))).sendKeys(username);

        driver.findElement(By.name("password"))
                .sendKeys(password);

        driver.findElement(
                By.cssSelector("input[value='Log In']")
        ).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.linkText("Accounts Overview")));
    }

    @Given("the customer has access to two bank accounts")
    public void customerHasTwoAccounts() {

        openAccountsOverview();

        beforeBalances.clear();
        beforeBalances.putAll(readBalances());

        Assertions.assertTrue(
                beforeBalances.size() >= 2,
                "At least two accounts are required"
        );
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

    @Then("the loan request should fail without creating an account or changing balances")
    public void verifyFailedLoanRequest() {
        // This characterizes the demo site's unsuccessful request behaviour.
        // Its generic internal error remains a validation defect, not a loan denial.
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("requestLoanError")));
        Assertions.assertTrue(error.getText().contains(
                "An internal error has occurred and has been logged."),
                "Expected the observed unsuccessful-request error");
        Assertions.assertTrue(driver.findElements(By.id("newAccountId")).stream()
                .noneMatch(WebElement::isDisplayed),
                "An unsuccessful request must not display a new loan account");
        Assertions.assertTrue(driver.findElements(By.id("loanStatus")).stream()
                .filter(WebElement::isDisplayed)
                .noneMatch(element -> element.getText().trim().equals("Approved")),
                "An unsuccessful request must not show approval");
        loadAfterBalances();
        Assertions.assertEquals(beforeBalances.keySet(), afterBalances.keySet(),
                "An unsuccessful request must not create or remove accounts");
        beforeBalances.forEach((id, balance) -> Assertions.assertEquals(
                0, balance.compareTo(afterBalances.get(id)),
                "An unsuccessful request must not change account " + id));
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

        wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Accounts Overview"))).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("accountTable")));

        System.out.println("Current URL: " + driver.getCurrentUrl());

        System.out.println(
                "Account table: " +
                        driver.findElement(By.id("accountTable")).getText()
        );

        System.out.println(
                "Account links found: " +
                        driver.findElements(
                                By.cssSelector("#accountTable td a")
                        ).size()
        );

    }

    private void loadAfterBalances() {

        if (afterBalances == null) {
            openAccountsOverview();
            afterBalances = readBalances();
        }
    }

    private Map<String, BigDecimal> readBalances() {

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

            return balances.size() >= 2 ? balances : null;
        });
    }
}
