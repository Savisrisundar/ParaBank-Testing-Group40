package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.base.ParaBankTestSupport;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.junit.jupiter.api.Assertions.*;

/** Login: valid and invalid credentials, required fields, and logout. */
public class LoginTest extends ParaBankTestSupport {
    private void attempt(String username, String password) {
        typeName("username", username);
        typeName("password", password);
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
    }

    @Test void TC_LOG_001_validCredentials() {
        String username = registerNewCustomer();
        clickMenu("Log Out");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        attempt(username, PASSWORD);
        assertTrue(panelTextContaining("Accounts Overview").contains("Accounts Overview"));
        assertFalse(driver.findElements(By.linkText("Log Out")).isEmpty());
    }

    @Test void TC_LOG_002_wrongPassword() {
        String username = registerNewCustomer();
        clickMenu("Log Out");
        attempt(username, "Incorrect@987");
        assertTrue(panelTextContaining("Error!").contains("username and password could not be verified"));
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty());
    }

    @Test void TC_LOG_003_unknownUsername() {
        attempt("missing_" + System.nanoTime(), PASSWORD);
        assertTrue(panelTextContaining("Error!").contains("username and password could not be verified"));
    }

    @Test void TC_LOG_004_emptyCredentials() {
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
        String panel = panelTextContaining("Error!");
        assertTrue(panel.contains("Please enter a username and password"), panel);
    }

    @Test void TC_LOG_005_logoutEndsSession() {
        registerNewCustomer();
        clickMenu("Log Out");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#loginPanel"))).isDisplayed());
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty());
    }
}
