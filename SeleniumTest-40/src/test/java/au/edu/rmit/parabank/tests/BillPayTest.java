package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.base.ParaBankTestSupport;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.junit.jupiter.api.Assertions.*;

/** Bill Pay: valid transaction, required fields, confirmation mismatch and invalid amount. */
public class BillPayTest extends ParaBankTestSupport {
    private void openBillPay() {
        clickMenu("Bill Pay");
        panelTextContaining("Bill Payment Service");
    }

    @Test void TC_BILL_001_validPayment() {
        registerNewCustomer();
        String source = firstAccountId();
        openBillPay();
        fillBill("25.00", "12345678", "12345678");
        selectName("fromAccountId", source);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Send Payment']"))).click();
        String message = panelTextContaining("Bill Payment Complete");
        assertTrue(message.contains("$25.00") || message.contains("25.00"), message);
        clickMenu("Find Transactions");
        selectId("accountId", source);
        typeId("amount", "25.00");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("findByAmount"))).click();
        String results = wait.until(d -> {
            String t = d.findElement(By.id("rightPanel")).getText();
            return t.contains("Transaction Results") || t.contains("No transactions found") ? t : null;
        });
        assertTrue(results.contains("25.00") && !results.contains("No transactions found"),
                "Successful bill payment should create a searchable transaction: " + results);
    }

    @Test void TC_BILL_002_emptyRequiredFields() {
        registerNewCustomer();
        openBillPay();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Send Payment']"))).click();
        String panel = panelTextContaining("required");
        assertTrue(panel.contains("Payee name is required."), panel);
        assertTrue(panel.contains("Account number is required."), panel);
        assertFalse(panel.contains("Bill Payment Complete"));
    }

    @Test void TC_BILL_003_mismatchedPayeeAccountNumbers() {
        registerNewCustomer();
        openBillPay();
        fillBill("10.00", "12345678", "87654321");
        driver.findElement(By.cssSelector("input[value='Send Payment']")).click();
        // Allow the application to render either confirmation or validation feedback.
        // No feedback within the wait is also an observable failure, not an unexplained timeout.
        String panel;
        try {
            panel = wait.until(d -> {
                String text = d.findElement(By.id("rightPanel")).getText();
                return text.contains("Bill Payment Complete")
                        || text.toLowerCase().contains("do not match")
                        || text.toLowerCase().contains("does not match")
                        || text.toLowerCase().contains("error")
                        || hasNonEmptyError("validationModel.verifyAccount")
                        ? text : null;
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            abortIfBotChallenge();
            panel = driver.findElement(By.id("rightPanel")).getText();
            fail("No confirmation or account-mismatch feedback appeared. Page response: " + panel);
            return;
        }
        assertFalse(panel.contains("Bill Payment Complete"),
                "Payment must not succeed if payee account and confirmation differ: " + panel);
        assertTrue(panel.toLowerCase().contains("match") || hasNonEmptyError("validationModel.verifyAccount"),
                "Expected a clear payee-account mismatch error. Actual: " + panel);
    }

    /** Expected business rule: a payment amount must be greater than zero. */
    @Test void TC_BILL_004_negativeAmount() {
        registerNewCustomer();
        openBillPay();
        fillBill("-10.00", "12345678", "12345678");
        driver.findElement(By.cssSelector("input[value='Send Payment']")).click();
        wait.until(d -> d.findElement(By.id("rightPanel")).getText().contains("Bill Payment Complete")
                || d.findElement(By.id("rightPanel")).getText().toLowerCase().contains("error")
                || hasNonEmptyError("validationModel.amount"));
        String response = driver.findElement(By.id("rightPanel")).getText();
        assertFalse(response.contains("Bill Payment Complete"),
                "Negative bill payment must be rejected (documented expected rule). Actual: " + response);
    }

    private void assertInvalidBillAmount(String value, String caseLabel) {
        registerNewCustomer();
        openBillPay();
        fillBill(value, "12345678", "12345678");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Send Payment']"))).click();
        String text = TestOutcome.afterSubmit(driver, wait, "Bill Payment Complete",
                "validationModel.amount", caseLabel);
        assertAll(caseLabel,
                () -> assertFalse(text.contains("Bill Payment Complete"), "DEFECT: invalid payment accepted: " + text),
                () -> assertTrue(TestOutcome.fieldError(driver, "validationModel.amount")
                        || !TestOutcome.errors(driver).isBlank()
                        || text.toLowerCase(java.util.Locale.ROOT).contains("error"),
                        "Payment should display validation feedback: " + text));
    }

    @Test void TC_BILL_005_zeroAmountRejected() {
        assertInvalidBillAmount("0", "Zero-amount bill payment");
    }

    @Test void TC_BILL_006_textAmountRejected() {
        assertInvalidBillAmount("abc", "Non-numeric bill payment");
    }


    @Test void TC_BILL_007_missingPayeeNameOnly() {
        registerNewCustomer();
        openBillPay();
        fillBill("10.00", "12345678", "12345678");
        driver.findElement(By.name("payee.name")).clear();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Send Payment']"))).click();
        String response = panelTextContaining("required");
        assertAll("Payee name required", 
            () -> assertFalse(response.contains("Bill Payment Complete"), "Missing-name payment succeeded"),
            () -> assertTrue(response.toLowerCase().contains("payee name"), "Missing-name error absent: " + response));
    }
    @Test void TC_BILL_008_missingAmountOnly() {
        registerNewCustomer();
        openBillPay();
        fillBill("10.00", "12345678", "12345678");
        driver.findElement(By.name("amount")).clear();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Send Payment']"))).click();
        String response = TestOutcome.afterSubmit(driver, wait, "Bill Payment Complete",
                "validationModel.amount", "TC_BILL_008_missingAmountOnly");
        assertAll("Amount required", 
            () -> assertFalse(response.contains("Bill Payment Complete"), "Missing-amount payment succeeded"),
            () -> assertTrue(TestOutcome.fieldError(driver, "validationModel.amount") || !TestOutcome.errors(driver).isBlank() || response.toLowerCase().contains("amount"),
                "Missing amount must produce clear validation feedback: " + response));
    }

}
