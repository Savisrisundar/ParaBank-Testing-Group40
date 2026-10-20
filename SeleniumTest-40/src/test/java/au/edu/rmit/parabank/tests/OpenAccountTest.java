package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.base.ParaBankTestSupport;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import static org.junit.jupiter.api.Assertions.*;

/** Open Account: checking, savings and account overview traceability. */
public class OpenAccountTest extends ParaBankTestSupport {
    private String openAccount(String type) {
        String fundingId = firstAccountId();
        clickMenu("Open New Account");
        new Select(wait.until(ExpectedConditions.elementToBeClickable(By.id("type"))))
                .selectByVisibleText(type);
        String selectedFundingId = selectAvailableAccount("fromAccountId", fundingId);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Open New Account']"))).click();
        panelTextContaining("Account Opened!");
        String newId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newAccountId"))).getText().trim();
        assertTrue(newId.matches("\\d+"), "New account ID must be numeric: " + newId);
        assertNotEquals(selectedFundingId, newId, "New account must differ from funding account");
        return newId;
    }

    @Test void TC_ACC_001_openSavings() {
        registerNewCustomer();
        String id = openAccount("SAVINGS");
        assertFalse(id.isBlank());
        clickMenu("Accounts Overview");
        assertTrue(wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("accountTable"), id)),
                "New account " + id + " must be listed after creation");
    }

    @Test void TC_ACC_002_openChecking() {
        registerNewCustomer();
        String id = openAccount("CHECKING");
        assertFalse(id.isBlank());
        clickMenu("Accounts Overview");
        assertTrue(wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("accountTable"), id)),
                "New account " + id + " must be listed after creation");
    }

    @Test void TC_ACC_003_newAccountsHaveUniqueNumbers() {
        registerNewCustomer();
        String first = openAccount("SAVINGS");
        String second = openAccount("CHECKING");
        assertNotEquals(first, second, "Two accounts must have different account numbers");
        clickMenu("Accounts Overview");
        // The heading may appear before account-table data finishes rendering.
        // Wait for both exact account IDs within the live table, not a heading snapshot.
        String overview;
        try {
            overview = wait.until(d -> {
            java.util.List<org.openqa.selenium.WebElement> tables = d.findElements(By.id("accountTable"));
            if (tables.isEmpty()) return null;
            try {
                String text = tables.get(0).getText();
                return text.contains(first) && text.contains(second) ? text : null;
            } catch (org.openqa.selenium.StaleElementReferenceException ex) { return null; }
        });
        } catch (org.openqa.selenium.TimeoutException ex) {
            TestOutcome.evidence(driver, "TC_ACC_003_account_persistence");
            fail("Created account numbers " + first + ", " + second
                + " not both visible in accountTable; actual panel=" + TestOutcome.panel(driver));
            return;
        }
        assertAll("Both accounts persist",
            () -> assertTrue(overview.contains(first), "First created account is missing: " + first),
            () -> assertTrue(overview.contains(second), "Second created account is missing: " + second));
    }
}
