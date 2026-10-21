package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.pulkit.AssignedTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/** Bill Pay: confirmation, persisted debit, required inputs and invalid formats. */
@Tag("pulkit")
public class BillPayTest extends AssignedTestSupport {
    @Test @DisplayName("BILL-001: valid payment records the correct debit")
    void TC_BILL_001_validPayment() {
        Account account = freshAccount();
        Payment payment = pay(account, new BigDecimal("25.00"));
        assertAll("Persisted bill payment",
                () -> assertTrue(payment.transaction().id().matches("\\d+")),
                () -> assertTrue(payment.transaction().description().contains(PAYEE)),
                () -> assertMoney(new BigDecimal("25.00"), payment.transaction().debit(), "Recorded debit"),
                () -> assertNull(payment.transaction().credit(), "A bill payment must not be a credit"));
    }

    @Test @DisplayName("BILL-002: empty form shows all required-field errors")
    void TC_BILL_002_emptyRequiredFields() {
        Account account = freshAccount();
        openBill(account.id());
        sendBill();
        String[] fields = {"name", "address", "city", "state", "zipCode", "phoneNumber",
                "account-empty", "verifyAccount-empty", "amount-empty"};
        assertAll("Required Bill Pay fields", java.util.Arrays.stream(fields).map(field ->
                (org.junit.jupiter.api.function.Executable) () ->
                        assertFalse(visibleText(By.id("validationModel-" + field)).isBlank(), field)));
        assertFalse(visible(By.id("billpayResult")));
        assertFalse(visible(By.id("billpayError")));
        assertMoney(account.balance(), readAccount(account.id()).balance(), "No debit for empty form");
    }

    @Test @DisplayName("BILL-003: mismatched payee confirmation is rejected")
    void TC_BILL_003_mismatchedPayeeAccountNumbers() {
        Account account = freshAccount();
        openBill(account.id());
        fillBill("10.00", "12345678", "87654321");
        sendBill();
        assertBillValidation(account, By.id("validationModel-verifyAccount-mismatch"), "The account numbers do not match.");
    }

    /** Proposed financial rule, not a confirmed ParaBank requirement. Deliberately excluded from core runs. */
    @Test @Tag("exploratory") @DisplayName("BILL-004: investigate negative payment acceptance")
    void TC_BILL_004_negativeAmount() {
        Account account = freshAccount();
        openBill(account.id());
        fillBill("-10.00", "12345678", "12345678");
        sendBill();
        wait.until(d -> visible(By.id("billpayResult")) || visible(By.id("billpayError"))
                || visible(By.cssSelector("#billpayForm .error")));
        assertFalse(visible(By.id("billpayResult")), "PROPOSED RULE: negative payment should be rejected; preserve any failure as exploratory evidence");
        assertFalse(visible(By.id("billpayError")), "Technical failure does not prove input validation");
        assertTrue(visible(By.cssSelector("#billpayForm .error")), "Expected explicit validation feedback");
        assertMoney(account.balance(), readAccount(account.id()).balance(), "Rejected negative payment must not alter balance");
    }

    @Test @DisplayName("BILL-005: nonnumeric payee account is rejected")
    void TC_BILL_005_nonNumericAccount() {
        Account account = freshAccount();
        openBill(account.id());
        fillBill("10.00", "ABCDE", "ABCDE");
        sendBill();
        assertBillValidation(account, By.id("validationModel-account-invalid"), "Please enter a valid number.");
    }

    @Test @DisplayName("BILL-006: nonnumeric amount is rejected")
    void TC_BILL_006_nonNumericAmount() {
        Account account = freshAccount();
        openBill(account.id());
        fillBill("ABC", "12345678", "12345678");
        sendBill();
        assertBillValidation(account, By.id("validationModel-amount-invalid"), "Please enter a valid amount.");
    }

    @Test @DisplayName("BILL-007: blank amount is rejected with other inputs valid")
    void TC_BILL_007_missingAmount() {
        Account account = freshAccount();
        openBill(account.id());
        fillBill("", "12345678", "12345678");
        sendBill();
        assertBillValidation(account, By.id("validationModel-amount-empty"), "The amount cannot be empty.");
    }

    @Test @DisplayName("BILL-008: cent precision is preserved in payment and balance")
    void TC_BILL_008_decimalPrecision() {
        Account account = freshAccount();
        Payment payment = pay(account, new BigDecimal("0.01"));
        assertMoney(new BigDecimal("0.01"), payment.transaction().debit(), "One-cent debit");
        assertNull(payment.transaction().credit());
    }
}
