package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.pulkit.AssignedTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.math.BigDecimal;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** Loan decisions use the dedicated status and explicit approved/denied panels. */
@Tag("pulkit")
public class LoanRequestTest extends AssignedTestSupport {
    private void submitLoan(String amount, String downPayment, String source) {
        clickMenu("Request Loan");
        typeId("amount", amount);
        typeId("downPayment", downPayment);
        selectId("fromAccountId", source);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Apply Now']"))).click();
        wait.until(d -> visible(By.id("requestLoanResult")) || visible(By.id("requestLoanError")));
        assertFalse(visible(By.id("requestLoanError")), "Technical loan error must not count as a business rejection");
        assertFalse(nonBlankText(By.id("loanProviderName")).isBlank(), "Loan provider must be recorded");
        assertFalse(nonBlankText(By.id("responseDate")).isBlank(), "Decision date must be recorded");
    }

    private void assertDenied(Account account, Set<String> before) {
        assertEquals("Denied", nonBlankText(By.id("loanStatus")), "Expected a business denial");
        assertTrue(visible(By.id("loanRequestDenied")), "Denial explanation panel must be shown");
        assertFalse(visibleText(By.cssSelector("#loanRequestDenied .error")).isBlank(), "Denial needs an explanation");
        assertFalse(visible(By.id("loanRequestApproved")), "No approval panel after denial");
        assertFalse(visible(By.id("newAccountId")), "No visible loan-account link after denial");
        assertEquals(before, accountIds(), "Denied request must not create an account");
        assertMoney(account.balance(), readAccount(account.id()).balance(), "Denied request must not debit down payment");
    }

    /** Requires the documented standard demo lending configuration; never rewrites a denial as a pass. */
    @Test @DisplayName("LOAN-001: affordable request is approved and opens a loan account")
    void TC_LOAN_001_validRequestProcessed() {
        Account account = freshAccount();
        assertTrue(account.balance().compareTo(new BigDecimal("20.00")) >= 0, "Fixture needs at least $20");
        Set<String> before = accountIds();
        submitLoan("100", "20", account.id());
        assertEquals("Approved", nonBlankText(By.id("loanStatus")), "Expected approval under the documented demo preconditions");
        assertTrue(visible(By.id("loanRequestApproved")));
        assertFalse(visible(By.id("loanRequestDenied")));
        String newId = nonBlankText(By.id("newAccountId"));
        assertTrue(newId.matches("\\d+"), "New account ID must be numeric");
        assertNotEquals(account.id(), newId);
        assertFalse(before.contains(newId), "Approval must return a newly created account");
        Set<String> after = accountIds();
        assertEquals(before.size() + 1, after.size());
        assertTrue(after.containsAll(before));
        assertTrue(after.contains(newId));
        assertMoney(account.balance().subtract(new BigDecimal("20.00")), readAccount(account.id()).balance(), "Down payment debit");
        assertMoney(new BigDecimal("100.00"), readAccount(newId).balance(), "Loan principal credited to new account");
    }

    /** Proposed rule: negative principal is invalid. Kept separate from source-backed acceptance coverage. */
    @Test @Tag("exploratory") @DisplayName("LOAN-002: investigate negative loan amount")
    void TC_LOAN_002_negativeAmountRejected() {
        Account account = freshAccount();
        Set<String> before = accountIds();
        submitLoan("-100", "10", account.id());
        assertDenied(account, before);
    }

    /** Proposed rule only: down payment cannot exceed principal. */
    @Test @Tag("exploratory") @DisplayName("LOAN-003: investigate down payment exceeding principal")
    void TC_LOAN_003_downPaymentGreaterThanLoanRejected() {
        Account account = freshAccount();
        assertTrue(account.balance().compareTo(new BigDecimal("200.00")) >= 0, "Fixture needs $200 to isolate proposed rule");
        Set<String> before = accountIds();
        submitLoan("100", "200", account.id());
        assertDenied(account, before);
    }

    @Test @DisplayName("LOAN-004: unaffordable down payment is explicitly denied")
    void TC_LOAN_004_downPaymentExceedsAvailableFunds() {
        Account account = freshAccount();
        Set<String> before = accountIds();
        assertEquals(Set.of(account.id()), before, "Fixture needs exactly one account so total funds are known");
        BigDecimal down = account.balance().add(BigDecimal.ONE);
        submitLoan(down.multiply(new BigDecimal("2")).toPlainString(), down.toPlainString(), account.id());
        assertEquals("You do not have sufficient funds for the given down payment.",
                visibleText(By.cssSelector("#loanRequestDenied .error")));
        assertDenied(account, before);
    }

    @Test @DisplayName("LOAN-005: very large request with no down payment is denied")
    void TC_LOAN_005_insufficientQualification() {
        Account account = freshAccount();
        Set<String> before = accountIds();
        assertEquals(Set.of(account.id()), before, "Fixture needs exactly one account");
        assertTrue(account.balance().compareTo(BigDecimal.ZERO) >= 0, "Fixture balance must be nonnegative");
        BigDecimal principal = account.balance().max(BigDecimal.ONE).multiply(new BigDecimal("1000000"));
        submitLoan(principal.toPlainString(), "0", account.id());
        assertDenied(account, before);
    }
}
