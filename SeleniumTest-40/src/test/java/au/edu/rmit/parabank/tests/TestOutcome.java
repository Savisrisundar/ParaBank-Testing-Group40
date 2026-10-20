package au.edu.rmit.parabank.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.nio.file.*;
import java.util.*;

/** Read-only snapshots. Do not interpret absence of an error as successful validation. */
final class TestOutcome {
    private TestOutcome() { }

    static String panel(WebDriver driver) {
        try {
            List<WebElement> panels = driver.findElements(By.id("rightPanel"));
            return panels.isEmpty() ? "" : panels.get(0).getText();
        } catch (StaleElementReferenceException | NoSuchWindowException ex) { return ""; }
    }

    static String errors(WebDriver driver) {
        StringJoiner messages = new StringJoiner(" | ");
        try {
            for (WebElement e : driver.findElements(By.cssSelector(".error, [aria-invalid='true'], .invalid-feedback"))) {
                try { if (e.isDisplayed() && !e.getText().isBlank()) messages.add(e.getText().trim()); }
                catch (StaleElementReferenceException ignored) { }
            }
        } catch (org.openqa.selenium.NoSuchElementException | StaleElementReferenceException ignored) { }
        return messages.toString();
    }

    static boolean fieldError(WebDriver driver, String id) {
        try {
            for (WebElement e : driver.findElements(By.id(id))) {
                try { if (e.isDisplayed() && !e.getText().isBlank()) return true; }
                catch (StaleElementReferenceException ignored) { }
            }
        } catch (org.openqa.selenium.NoSuchElementException ignored) { }
        return false;
    }

    /** Wait for an observable application response, not merely for the form to remain visible. */
    static String afterSubmit(WebDriver driver, WebDriverWait wait,
                              String successText, String errorId, String label) {
        try {
            wait.until(d -> {
                if (isSecurityChallenge(d)) {
                    evidence(d, label + "_security_challenge");
                    org.junit.jupiter.api.Assertions.fail(
                        "TEST BLOCKED: Cloudflare verification interrupted [" + label + "]. "
                        + "This is not a confirmed ParaBank defect. See target/evidence/diagnostics.");
                }
                String p = panel(d);
                boolean specificError = errorId != null && fieldError(d, errorId);
                boolean generalError = !errors(d).isBlank()
                    || p.toLowerCase(Locale.ROOT).contains("error");
                return p.contains(successText) || specificError || generalError ? true : null;
            });
        } catch (TimeoutException ex) {
            evidence(driver, label);
            if (isSecurityChallenge(driver)) {
                org.junit.jupiter.api.Assertions.fail(
                    "TEST BLOCKED: Cloudflare verification interrupted [" + label + "]. "
                    + "See target/evidence/diagnostics.");
            }
            org.junit.jupiter.api.Assertions.fail(
                "INCONCLUSIVE: no explicit success or validation outcome [" + label + "]. URL="
                + driver.getCurrentUrl() + ", panel=" + panel(driver)
                + ", errors=" + errors(driver)
                + ". See target/evidence/diagnostics.");
        }
        return panel(driver);
    }

    /** Read HTML constraint validation without changing page state or bypassing validation. */
    static String nativeValidationMessage(WebDriver driver, String fieldId) {
        try {
            List<WebElement> matches = driver.findElements(By.id(fieldId));
            if (matches.isEmpty()) return "";
            Object value = ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].checkValidity() ? '' : arguments[0].validationMessage;",
                matches.get(0));
            return value == null ? "" : value.toString();
        } catch (WebDriverException ex) { return ""; }
    }

    static void evidence(WebDriver driver, String label) {
        try {
            Path folder = Paths.get("target", "evidence", "diagnostics");
            Files.createDirectories(folder);
            String safe = label.replaceAll("[^a-zA-Z0-9_-]", "_");
            Files.write(folder.resolve(safe + ".png"), ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            Files.writeString(folder.resolve(safe + ".txt"), "URL: " + driver.getCurrentUrl()
                + "\nPanel: " + panel(driver) + "\nVisible errors: " + errors(driver)
                + "\nPage title: " + driver.getTitle());
        } catch (Exception ex) { System.err.println("Evidence capture failed: " + ex.getMessage()); }
    }


    static boolean isSecurityChallenge(WebDriver driver) {
        try {
            String title = driver.getTitle().toLowerCase(Locale.ROOT);
            String body = driver.findElements(By.tagName("body")).stream()
                .findFirst().map(WebElement::getText).orElse("")
                .toLowerCase(Locale.ROOT);
            return title.contains("just a moment")
                || body.contains("performing security verification")
                || body.contains("verify you are human");
        } catch (WebDriverException ex) {
            return false; // Let the normal test diagnostics explain browser failures.
        }
    }
}
