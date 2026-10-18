package au.edu.rmit.parabank.base;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.opentest4j.TestAbortedException;
import java.time.Duration;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Reusable browser-only data setup for isolated ParaBank demo tests. */
public abstract class ParaBankTestSupport extends BaseTest {
    protected static final String PASSWORD = "Test@12345";

    /** Register a unique demo customer; failures here are fixture/setup failures, not bank-feature defects. */
    protected String registerNewCustomer() {
        String user = "g40_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register"))).click();
        } catch (TimeoutException e) {
            abortIfBotChallenge();
            throw e;
        }
        typeId("customer.firstName", "Group");
        typeId("customer.lastName", "Forty");
        typeId("customer.address.street", "123 Demo Street");
        typeId("customer.address.city", "Melbourne");
        typeId("customer.address.state", "VIC");
        typeId("customer.address.zipCode", "3000");
        typeId("customer.phoneNumber", "0399999999");
        typeId("customer.ssn", "123456789");
        typeId("customer.username", user);
        typeId("customer.password", PASSWORD);
        typeId("repeatedPassword", PASSWORD);
        driver.findElement(By.cssSelector("input[value='Register']")).click();
        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("rightPanel"),
                    "Your account was created successfully"));
        } catch (TimeoutException e) {
            abortIfBotChallenge();
            String response = driver.findElements(By.id("rightPanel")).stream()
                    .findFirst().map(WebElement::getText).orElse("Right panel not found");
            throw new IllegalStateException("Registration fixture could not be created (not a loan/login failure). "
                    + "Check ParaBank availability and registration validation. Page: "
                    + driver.getCurrentUrl() + "; response: " + response, e);
        }
        assertTrue(driver.findElement(By.id("rightPanel")).getText().contains("Welcome " + user));
        return user;
    }

    /** Do not attempt to solve or evade Cloudflare; mark environment-blocked tests as aborted. */
    protected void abortIfBotChallenge() {
        String title = driver.getTitle().toLowerCase(java.util.Locale.ROOT);
        String body = driver.findElements(By.tagName("body")).stream()
                .findFirst().map(WebElement::getText).orElse("")
                .toLowerCase(java.util.Locale.ROOT);
        String currentUrl = driver.getCurrentUrl().toLowerCase(java.util.Locale.ROOT);
        if (title.contains("just a moment") || title.contains("attention required")
                || body.contains("verify you are human")
                || body.contains("checking your browser")
                || body.contains("cloudflare") && body.contains("verification")
                || currentUrl.contains("/cdn-cgi/challenge")) {
            throw new TestAbortedException("Cloudflare human verification blocked ParaBank setup. "
                    + "Run again later, reduce request frequency, or request approved testing access.");
        }
    }

    /** Wait for account options to load, then select a real selectable value. */
    protected String selectAvailableAccount(String elementId, String preferredId) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(elementId)));
        wait.until(d -> new Select(d.findElement(By.id(elementId))).getOptions().stream()
                .anyMatch(o -> o.getAttribute("value") != null && !o.getAttribute("value").isBlank() && !o.getAttribute("value").equals("0")));
        Select select = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.id(elementId))));
        java.util.List<WebElement> options = select.getOptions();
        String selected = options.stream()
                .map(o -> o.getAttribute("value"))
                .filter(v -> v != null && !v.isBlank() && !v.equals("0"))
                .filter(v -> v.equals(preferredId))
                .findFirst()
                .orElseGet(() -> options.stream().map(o -> o.getAttribute("value"))
                        .filter(v -> v != null && !v.isBlank() && !v.equals("0"))
                        .findFirst().orElseThrow(() -> new AssertionError("No funding account options found")));
        if (!selected.equals(preferredId)) {
            System.err.println("NOTICE: Account overview " + preferredId
                    + " was absent from funding dropdown; available account selected: " + selected);
        }
        select.selectByValue(selected);
        return selected;
    }

    protected void typeId(String id, String value) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        input.clear();
        input.sendKeys(value);
    }

    protected void clickMenu(String text) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(text))).click();
    }

    protected String firstAccountId() {
        clickMenu("Accounts Overview");
        WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#accountTable a[href*='activity.htm']")));
        String id = account.getText().trim();
        assertTrue(id.matches("\\d+"), "Expected numeric account ID, got: " + id);
        return id;
    }

    protected void selectId(String id, String value) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
        new Select(element).selectByValue(value);
    }

    protected void selectName(String name, String value) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.name(name)));
        new Select(element).selectByValue(value);
    }

    protected String panelTextContaining(String expected) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("rightPanel"), expected));
        return driver.findElement(By.id("rightPanel")).getText();
    }

    protected boolean hasNonEmptyError(String id) {
        return driver.findElements(By.id(id)).stream()
                .anyMatch(e -> e.isDisplayed() && !e.getText().trim().isEmpty());
    }

    protected void fillBill(String amount, String account, String confirm) {
        typeName("payee.name", "Demo Utilities");
        typeName("payee.address.street", "456 Demo Avenue");
        typeName("payee.address.city", "Melbourne");
        typeName("payee.address.state", "VIC");
        typeName("payee.address.zipCode", "3000");
        typeName("payee.phoneNumber", "0399999999");
        typeName("payee.accountNumber", account);
        typeName("verifyAccount", confirm);
        typeName("amount", amount);
    }

    protected void typeName(String name, String value) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(name)));
        input.clear();
        input.sendKeys(value);
    }

    protected void clickButton(String value) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[value='" + value + "'],button[type='submit']")))
                .click();
    }
}
