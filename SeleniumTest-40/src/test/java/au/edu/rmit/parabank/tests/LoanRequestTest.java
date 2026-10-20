package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.base.ParaBankTestSupport;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.junit.jupiter.api.Assertions.*;

/** Loan decisions and invalid monetary inputs; approval is not assumed for every valid application. */
public class LoanRequestTest extends ParaBankTestSupport {
    private void submitLoan(String amount, String downPayment, String source) {
        clickMenu("Request Loan");
        typeId("amount", amount);
        typeId("downPayment", downPayment);
        selectId("fromAccountId", source);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Apply Now']"))).click();
    }

    /**
     * A valid, small request with an affordable down payment should be approved.
     * Unlike merely checking for a decision, this verifies the expected decision.
     * This is a documented test assumption for the public ParaBank demo.
     */
    @Test void TC_LOAN_001_validRequestProcessed() {
        registerNewCustomer();
        String source = firstAccountId();
        submitLoan("100", "20", source);
        String text = panelTextContaining("Loan Request Processed");
        assertAll("Valid loan request",
                () -> assertTrue(text.contains("Loan Provider:"),
                        "Loan provider should appear in the result: " + text),
                () -> assertTrue(text.contains("Status: Approved"),
                        "Expected approval for a valid loan request, but got: " + text),
                () -> assertTrue(text.contains("Congratulations, your loan has been approved"),
                        "Approved request should show confirmation: " + text)
        );
    }

    @Test void TC_LOAN_002_negativeAmountRejected() {
        registerNewCustomer();
        String source = firstAccountId();
        submitLoan("-100", "10", source);
        String text = wait.until(d -> {
            String content = d.findElement(By.id("rightPanel")).getText();
            return content.contains("Loan Request Processed") || content.toLowerCase().contains("error")
                    ? content : null;
        });
        assertFalse(text.contains("Status: Approved"),
                "Negative loan amounts must never be approved: " + text);
    }

    /** Assumed rule: down payment cannot exceed requested loan principal. */
    @Test void TC_LOAN_003_downPaymentGreaterThanLoanRejected() {
        registerNewCustomer();
        String source = firstAccountId();
        submitLoan("100", "200", source);
        String text = wait.until(d -> {
            String content = d.findElement(By.id("rightPanel")).getText();
            return content.contains("Loan Request Processed") || content.toLowerCase().contains("error")
                    ? content : null;
        });
        assertFalse(text.contains("Status: Approved"),
                "Down payment exceeding loan amount must not be approved: " + text);
    }

    private void assertInvalidLoan(String amount, String downPayment, String rule) {
        registerNewCustomer();
        String source = firstAccountId();
        submitLoan(amount, downPayment, source);
        String response = wait.until(d -> {
            String p = d.findElement(By.id("rightPanel")).getText();
            return p.contains("Loan Request Processed") || p.toLowerCase().contains("error") ? p : null;
        });
        assertFalse(response.contains("Status: Approved"), rule + " must not be approved: " + response);
        assertTrue(response.toLowerCase().contains("denied") || response.toLowerCase().contains("error")
                        || !response.contains("Loan Request Processed"),
                "Expected rejection or input-validation feedback for " + rule + ": " + response);
    }

    @Test void TC_LOAN_004_zeroAmountRejected() {
        assertInvalidLoan("0", "0", "Zero loan principal");
    }

    @Test void TC_LOAN_005_negativeDownPaymentRejected() {
        assertInvalidLoan("100", "-10", "Negative down payment");
    }


    @Test void TC_LOAN_006_textLoanAmountRejected() {
        assertInvalidLoan("abc", "10", "Non-numeric loan amount");
    }
    @Test void TC_LOAN_007_textDownPaymentRejected() {
        assertInvalidLoan("100", "abc", "Non-numeric down payment");
    }

}
