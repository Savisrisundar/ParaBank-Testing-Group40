package au.edu.rmit.parabank.pulkit;

import au.edu.rmit.parabank.base.ParaBankTestSupport;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Support only for Pulkit's assigned classes; the team's existing base classes stay intact. */
@ExtendWith(ExecutionEvidence.class)
public abstract class AssignedTestSupport extends ParaBankTestSupport {
    protected static final String PAYEE = "Demo Utilities";
    protected static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM-dd-uuuu");
    protected record Account(String id, BigDecimal balance) { }
    protected record Transaction(String id, LocalDate date, String description,
                                 BigDecimal debit, BigDecimal credit) { }
    protected record Payment(Account source, Transaction transaction) { }

    protected Account freshAccount() {
        registerNewCustomer();
        return readAccount(firstAccountId());
    }

    /** Wait for the requested option, never silently select another customer's/account value. */
    protected void selectExact(By locator, String value) {
        wait.until(d -> {
            try {
                WebElement element = d.findElement(locator);
                if (!element.isDisplayed() || !element.isEnabled()) return false;
                Select select = new Select(element);
                boolean exists = select.getOptions().stream()
                        .anyMatch(o -> value.equals(o.getDomAttribute("value")) && o.isEnabled());
                if (!exists) return false;
                select.selectByValue(value);
                return value.equals(select.getFirstSelectedOption().getDomAttribute("value"));
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    @Override protected void selectId(String id, String value) { selectExact(By.id(id), value); }
    @Override protected void selectName(String name, String value) { selectExact(By.name(name), value); }

    protected boolean visible(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    protected String visibleText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
    }

    protected String nonBlankText(By locator) {
        return wait.until(d -> {
            WebElement element = d.findElement(locator);
            String text = element.getText().trim();
            return element.isDisplayed() && !text.isBlank() ? text : null;
        });
    }

    protected BigDecimal money(String text) {
        String normalized = text.replace("$", "").replace(",", "").trim();
        assertTrue(normalized.matches("-?\\d+(\\.\\d{1,2})?"), "Expected monetary value: " + text);
        return new BigDecimal(normalized).setScale(2);
    }

    protected void assertMoney(BigDecimal expected, BigDecimal actual, String purpose) {
        assertEquals(0, expected.compareTo(actual), purpose + ": expected " + expected + ", actual " + actual);
    }

    protected Account readAccount(String accountId) {
        clickMenu("Accounts Overview");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(accountId))).click();
        wait.until(ExpectedConditions.textToBe(By.id("accountId"), accountId));
        BigDecimal balance = money(nonBlankText(By.id("balance")));
        return new Account(accountId, balance);
    }

    protected Set<String> accountIds() {
        clickMenu("Accounts Overview");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#accountTable a[href*='activity.htm']")));
        return driver.findElements(By.cssSelector("#accountTable a[href*='activity.htm']"))
                .stream().map(e -> e.getText().trim()).collect(Collectors.toSet());
    }

    protected void openBill(String accountId) {
        clickMenu("Bill Pay");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billpayForm")));
        selectName("fromAccountId", accountId);
    }

    protected void sendBill() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#billpayForm input[value='Send Payment']"))).click();
    }

    protected void verifyBillConfirmation(String accountId, BigDecimal amount) {
        wait.until(d -> visible(By.id("billpayResult")) || visible(By.id("billpayError")));
        assertFalse(visible(By.id("billpayError")), "Technical Bill Pay error is not a business outcome");
        assertAll("Payment confirmation",
                () -> assertEquals("Bill Payment Complete", visibleText(By.cssSelector("#billpayResult h1"))),
                () -> assertEquals(PAYEE, visibleText(By.id("payeeName"))),
                () -> assertEquals(accountId, visibleText(By.id("fromAccountId"))),
                () -> assertMoney(amount, money(visibleText(By.cssSelector("#billpayResult #amount"))), "Confirmed amount"));
    }

    protected List<Transaction> rows() {
        return driver.findElements(By.cssSelector("#transactionTable tbody tr")).stream().map(row -> {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            assertEquals(4, cells.size(), "Transaction row structure");
            WebElement link = cells.get(1).findElement(By.tagName("a"));
            String href = link.getDomAttribute("href");
            assertNotNull(href);
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[?&]id=(\\d+)").matcher(href);
            assertTrue(matcher.find(), "Transaction link must contain an ID: " + href);
            String debit = cells.get(2).getText().trim();
            String credit = cells.get(3).getText().trim();
            return new Transaction(matcher.group(1), LocalDate.parse(cells.get(0).getText().trim(), DATE),
                    link.getText().trim(), debit.isBlank() ? null : money(debit), credit.isBlank() ? null : money(credit));
        }).toList();
    }

    /** Actual transaction date is captured from the UI, avoiding local/server clock assumptions. */
    protected Transaction waitForPayment(BigDecimal amount) {
        return wait.until(d -> {
            try {
                return rows().stream().filter(t -> t.description().contains(PAYEE)
                        && t.debit() != null && t.debit().compareTo(amount) == 0).findFirst().orElse(null);
            } catch (StaleElementReferenceException ex) {
                return null;
            }
        });
    }

    protected Payment pay(Account source, BigDecimal amount) {
        assertTrue(source.balance().compareTo(amount) >= 0, "Fixture requires enough funds for payment");
        openBill(source.id());
        fillBill(amount.toPlainString(), "12345678", "12345678");
        sendBill();
        verifyBillConfirmation(source.id(), amount);
        Account after = readAccount(source.id());
        assertMoney(source.balance().subtract(amount), after.balance(), "Source balance after payment");
        return new Payment(after, waitForPayment(amount));
    }

    protected void assertBillValidation(Account account, By error, String expected) {
        assertEquals(expected, visibleText(error));
        assertAll("Rejected payment",
                () -> assertTrue(visible(By.id("billpayForm"))),
                () -> assertFalse(visible(By.id("billpayResult"))),
                () -> assertFalse(visible(By.id("billpayError"))));
        assertMoney(account.balance(), readAccount(account.id()).balance(), "Rejected payment leaves balance unchanged");
    }

    protected void openFind(String accountId) {
        clickMenu("Find Transactions");
        selectId("accountId", accountId);
        assertTrue(visible(By.id("formContainer")));
    }

    /** The results container becomes visible in the completed response callback, including empty results. */
    protected List<Transaction> search(String buttonId) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id(buttonId))).click();
        wait.until(d -> visible(By.id("resultContainer")) || visible(By.id("errorContainer")));
        assertFalse(visible(By.id("errorContainer")), "Search returned a technical error");
        assertFalse(visible(By.id("formContainer")), "Search should finish before results are asserted");
        assertTrue(visible(By.id("transactionTable")), "Completed results table must be visible");
        return rows();
    }

    protected void assertContainsTransaction(List<Transaction> actual, Transaction expected) {
        Transaction found = actual.stream().filter(t -> t.id().equals(expected.id())).findFirst()
                .orElseThrow(() -> new AssertionError("Expected transaction " + expected.id() + " in " + actual));
        assertAll("Known transaction",
                () -> assertEquals(expected.date(), found.date()),
                () -> assertEquals(expected.description(), found.description()),
                () -> assertEquals(expected.debit(), found.debit()),
                () -> assertEquals(expected.credit(), found.credit()));
    }
}
