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
}
