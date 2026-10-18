package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.base.ParaBankTestSupport;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.junit.jupiter.api.Assertions.*;

/** Find Transactions: amount, missing matches, date and date range. */
public class FindTransactionsTest extends ParaBankTestSupport {
    private String makePaymentAndGetAccount() {
        registerNewCustomer();
        String id = firstAccountId();
        clickMenu("Bill Pay");
        fillBill("15.00", "12345678", "12345678");
        selectName("fromAccountId", id);
        driver.findElement(By.cssSelector("input[value='Send Payment']")).click();
        panelTextContaining("Bill Payment Complete");
        return id;
    }

    private void openFind(String accountId) {
        clickMenu("Find Transactions");
        selectId("accountId", accountId);
    }

    @Test void TC_TXN_001_findByAmount() {
        String accountId = makePaymentAndGetAccount();
        openFind(accountId);
        typeId("amount", "15.00");
        driver.findElement(By.id("findByAmount")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transactionTable")));
        String table = driver.findElement(By.id("transactionTable")).getText();
        assertTrue(table.contains("15.00"), "Expected payment amount in results: " + table);
    }

    @Test void TC_TXN_002_noMatchingAmount() {
        String accountId = makePaymentAndGetAccount();
        openFind(accountId);
        typeId("amount", "999999999.99");
        driver.findElement(By.id("findByAmount")).click();
        String panel = wait.until(d -> {
            String text = d.findElement(By.id("rightPanel")).getText();
            return text.contains("No transactions found") || text.contains("Transaction Results") ? text : null;
        });
        assertTrue(panel.contains("No transactions found") ||
                        driver.findElements(By.cssSelector("#transactionTable tbody tr")).isEmpty(),
                "No transactions should match an unused amount: " + panel);
    }

    @Test void TC_TXN_003_findByDateRange() {
        String accountId = makePaymentAndGetAccount();
        openFind(accountId);
        typeId("fromDate", "01-01-2020");
        typeId("toDate", "12-31-2030");
        driver.findElement(By.id("findByDateRange")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transactionTable")));
        String table = driver.findElement(By.id("transactionTable")).getText();
        assertTrue(table.contains("15.00"), "Expected recent payment in wide date range: " + table);
    }

    @Test void TC_TXN_004_findByDateWithoutResults() {
        String accountId = makePaymentAndGetAccount();
        openFind(accountId);
        typeId("transactionDate", "01-01-1990");
        driver.findElement(By.id("findByDate")).click();
        String panel = wait.until(d -> {
            String text = d.findElement(By.id("rightPanel")).getText();
            return text.contains("No transactions found") || text.contains("Transaction Results") ? text : null;
        });
        assertTrue(panel.contains("No transactions found") ||
                        driver.findElements(By.cssSelector("#transactionTable tbody tr")).isEmpty(),
                "No transactions should exist for 1990: " + panel);
    }
}
