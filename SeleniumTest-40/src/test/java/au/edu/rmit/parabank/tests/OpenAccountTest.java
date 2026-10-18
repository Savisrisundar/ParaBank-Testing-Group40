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
    }

    @Test void TC_ACC_002_openChecking() {
        registerNewCustomer();
        String id = openAccount("CHECKING");
        assertFalse(id.isBlank());
    }

    @Test void TC_ACC_003_newAccountListed() {
        registerNewCustomer();
        String id = openAccount("SAVINGS");
        clickMenu("Accounts Overview");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("accountTable"), id));
        assertTrue(driver.findElement(By.id("accountTable")).getText().contains(id),
                "Created account should appear in overview");
    }
}
