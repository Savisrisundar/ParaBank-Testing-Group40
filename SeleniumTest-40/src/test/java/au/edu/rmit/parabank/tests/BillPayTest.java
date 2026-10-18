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
}
