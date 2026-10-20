package au.edu.rmit.parabank.tests;

import au.edu.rmit.parabank.base.BaseTest;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Registration: positive and negative boundary/format cases with documented assumptions.
 * REG_005 numeric-only postal rule and REG_006 alphabetic name rule are test assumptions.
 * Their assertion failures are preserved as potential defects, not suppressed.
 */
public class RegistrationTest extends BaseTest {

    // Generate a unique username for each registration
    private String generateUsername() {
        return "group40_" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12);
    }

    // Open the registration page
    private void openRegistrationPage() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Register")
        )).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("customer.firstName")
        ));
    }

    // Fill registration form with valid details
    private void fillRegistrationForm(
            String username,
            String password,
            String confirmPassword,
            String postalCode,
            String firstName) {

        driver.findElement(By.id("customer.firstName"))
                .sendKeys(firstName);

        driver.findElement(By.id("customer.lastName"))
                .sendKeys("Forty");

        driver.findElement(By.id("customer.address.street"))
                .sendKeys("123 Test Street");

        driver.findElement(By.id("customer.address.city"))
                .sendKeys("Melbourne");

        driver.findElement(By.id("customer.address.state"))
                .sendKeys("VIC");

        driver.findElement(By.id("customer.address.zipCode"))
                .sendKeys(postalCode);

        driver.findElement(By.id("customer.phoneNumber"))
                .sendKeys("0399999999");

        driver.findElement(By.id("customer.ssn"))
                .sendKeys("123456789");

        driver.findElement(By.id("customer.username"))
                .sendKeys(username);

        driver.findElement(By.id("customer.password"))
                .sendKeys(password);

        driver.findElement(By.id("repeatedPassword"))
                .sendKeys(confirmPassword);
    }

    // Submit registration form
    private void submitRegistration() {
        driver.findElement(
                By.cssSelector("input[value='Register']")
        ).click();
    }

    // Wait for registration success
    private String waitForRegistrationSuccess() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#rightPanel h1.title")
                )
        ).getText();
    }

    // Wait for either registration success or an explicit field validation error.
    // ParaBank may replace rightPanel after submission. A stale reference here is
    // a transient DOM refresh, so retry instead of marking the test as broken.
    private String waitForRegistrationOutcome(String errorElementId) {
        return TestOutcome.afterSubmit(driver, wait,
                "Your account was created successfully", errorElementId,
                "Registration_" + errorElementId);
    }

    // =====================================================
    // TC_REG_001 - Successful Registration
    // =====================================================

    @Test
    void TC_REG_001_successfulRegistration() {

        String username = generateUsername();
        String password = "Test@12345";

        openRegistrationPage();

        fillRegistrationForm(
                username,
                password,
                password,
                "3000",
                "Group"
        );

        submitRegistration();

        String welcomeMessage = waitForRegistrationSuccess();

        assertEquals(
                "Welcome " + username,
                welcomeMessage,
                "Registration must display the correct username"
        );

        assertTrue(
                driver.getPageSource().contains(
                        "Your account was created successfully"
                ),
                "Registration should succeed with valid details"
        );
    }

    // =====================================================
    // TC_REG_002 - Empty Required Fields
    // =====================================================

    @Test
    void TC_REG_002_emptyRequiredFields() {

        openRegistrationPage();

        submitRegistration();

        String firstNameError = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("customer.firstName.errors")
                )
        ).getText();

        assertTrue(
                firstNameError.toLowerCase().contains("required"),
                "First name must be required"
        );

        assertFalse(
                driver.getPageSource().contains(
                        "Your account was created successfully"
                ),
                "Empty registration must not succeed"
        );
    }

    // =====================================================
    // TC_REG_003 - Password Mismatch
    // =====================================================

    @Test
    void TC_REG_003_passwordMismatch() {

        String username = generateUsername();

        openRegistrationPage();

        fillRegistrationForm(
                username,
                "Test@12345",
                "Different@12345",
                "3000",
                "Group"
        );

        submitRegistration();

        String errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("repeatedPassword.errors")
                )
        ).getText();

        assertTrue(
                errorMessage.toLowerCase().contains("match"),
                "Mismatched passwords must be rejected"
        );

        assertFalse(
                driver.getPageSource().contains(
                        "Your account was created successfully"
                ),
                "Registration must not succeed with different passwords"
        );
    }

    // =====================================================
    // TC_REG_004 - Duplicate Username
    // =====================================================

    @Test
    void TC_REG_004_duplicateUsername() {

        String username = generateUsername();
        String password = "Test@12345";

        // First registration
        openRegistrationPage();

        fillRegistrationForm(
                username,
                password,
                password,
                "3000",
                "Group"
        );

        submitRegistration();

        assertEquals(
                "Welcome " + username,
                waitForRegistrationSuccess()
        );

        // Log out
        wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Log Out")
        )).click();

        // Second registration using same username
        openRegistrationPage();

        fillRegistrationForm(
                username,
                password,
                password,
                "3000",
                "Second"
        );

        submitRegistration();

        String errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("customer.username.errors")
                )
        ).getText();

        assertTrue(
                errorMessage.toLowerCase().contains("already exists"),
                "Duplicate usernames must be rejected. Actual: "
                        + errorMessage
        );

        assertFalse(
                driver.getPageSource().contains(
                        "Your account was created successfully"
                ),
                "Second registration must not succeed"
        );
    }

    // =====================================================
    // TC_REG_005 - Invalid Postal Code
    // =====================================================

    @Test
    void TC_REG_005_invalidPostalCode() {

        String username = generateUsername();

        openRegistrationPage();

        fillRegistrationForm(
                username,
                "Test@12345",
                "Test@12345",
                "ABCDE",
                "Group"
        );

        submitRegistration();

        String result = waitForRegistrationOutcome(
                "customer.address.zipCode.errors"
        );

        assertFalse(
                result.contains(
                        "Your account was created successfully"
                ),
                "DEFECT: Registration accepted alphabetic postal "
                        + "code ABCDE. Actual response: " + result
        );

        assertTrue(
                driver.findElements(
                        By.id("customer.address.zipCode.errors")
                ).stream().anyMatch(element ->
                        element.isDisplayed()
                                && !element.getText().isBlank()
                ),
                "Invalid postal code should display a validation error"
        );
    }

    // =====================================================
    // TC_REG_006 - Invalid Personal Information
    // =====================================================

    @Test
    void TC_REG_006_invalidPersonalInformation() {

        String username = generateUsername();

        openRegistrationPage();

        // Invalid first name: numeric characters
        fillRegistrationForm(
                username,
                "Test@12345",
                "Test@12345",
                "3000",
                "123456"
        );

        submitRegistration();

        String result = waitForRegistrationOutcome(
                "customer.firstName.errors"
        );

        assertFalse(
                result.contains(
                        "Your account was created successfully"
                ),
                "DEFECT: Registration accepted numeric-only "
                        + "first name. Actual response: " + result
        );

        assertTrue(
                driver.findElements(
                        By.id("customer.firstName.errors")
                ).stream().anyMatch(element ->
                        element.isDisplayed()
                                && !element.getText().isBlank()
                ),
                "Invalid first name should display a validation error"
        );
    }

    @Test void TC_REG_007_missingLastName() {
        openRegistrationPage();
        fillRegistrationForm(generateUsername(), "Test@12345", "Test@12345", "3000", "Group");
        driver.findElement(By.id("customer.lastName")).clear();
        submitRegistration();
        String outcome = waitForRegistrationOutcome("customer.lastName.errors");
        String error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.lastName.errors"))).getText();
        assertAll("Last name required",
                () -> assertFalse(outcome.contains("Your account was created successfully"), outcome),
                () -> assertTrue(error.toLowerCase().contains("required"), "Missing last name needs validation: " + error));
    }

    @Test void TC_REG_008_missingUsername() {
        openRegistrationPage();
        fillRegistrationForm(generateUsername(), "Test@12345", "Test@12345", "3000", "Group");
        driver.findElement(By.id("customer.username")).clear();
        submitRegistration();
        String outcome = waitForRegistrationOutcome("customer.username.errors");
        String error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.username.errors"))).getText();
        assertAll("Username required",
                () -> assertFalse(outcome.contains("Your account was created successfully"), outcome),
                () -> assertTrue(error.toLowerCase().contains("required"), "Missing username needs validation: " + error));
    }

    @Test void TC_REG_009_missingPassword() {
        openRegistrationPage();
        fillRegistrationForm(generateUsername(), "Test@12345", "Test@12345", "3000", "Group");
        driver.findElement(By.id("customer.password")).clear();
        driver.findElement(By.id("repeatedPassword")).clear();
        submitRegistration();
        String outcome = waitForRegistrationOutcome("customer.password.errors");
        String error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.password.errors"))).getText();
        assertAll("Password required",
                () -> assertFalse(outcome.contains("Your account was created successfully"), outcome),
                () -> assertTrue(error.toLowerCase().contains("required"), "Missing password needs validation: " + error));
    }


    /** Equivalence partition: each address field individually missing, other fields valid. */
    private void assertMissingRegistrationField(String id, String errorId, String description) {
        openRegistrationPage();
        String username = generateUsername();
        fillRegistrationForm(username, "Test@12345", "Test@12345", "3000", "Group");
        org.openqa.selenium.WebElement field = driver.findElement(By.id(id));
        field.clear();
        submitRegistration();
        String response = waitForRegistrationOutcome(errorId);
        assertAll(description,
            () -> assertFalse(response.contains("Your account was created successfully"),
                description + " must not allow account creation: " + response),
            () -> assertTrue(driver.findElements(By.id(errorId)).stream().anyMatch(
                e -> e.isDisplayed() && !e.getText().isBlank()),
                description + " must display a field-specific error"));
    }
    @Test void TC_REG_010_missingStreet() {
        assertMissingRegistrationField("customer.address.street", "customer.address.street.errors", "Missing street");
    }
    @Test void TC_REG_011_missingCity() {
        assertMissingRegistrationField("customer.address.city", "customer.address.city.errors", "Missing city");
    }
    @Test void TC_REG_012_missingState() {
        assertMissingRegistrationField("customer.address.state", "customer.address.state.errors", "Missing state");
    }

    @Test
    void TC_REG_013_missingPostalCode() {
        String username = generateUsername();
        openRegistrationPage();
        fillRegistrationForm(username, "Test@12345", "Test@12345", "", "Group");

        if (TestOutcome.isSecurityChallenge(driver)) {
            TestOutcome.evidence(driver, "Registration_missingPostalCode_beforeSubmit");
            fail("TEST BLOCKED: Cloudflare security verification appeared before submission.");
        }

        submitRegistration();

        if (TestOutcome.isSecurityChallenge(driver)) {
            TestOutcome.evidence(driver, "Registration_missingPostalCode_afterSubmit");
            TestOutcome.evidence(driver, "TC_REG_013_Cloudflare");

            assumeFalse(
                    TestOutcome.isSecurityChallenge(driver),
                    "TEST BLOCKED: Cloudflare security verification "
                            + "interrupted the registration test."
            );
        }

        // A native HTML constraint may block form submission without a ParaBank error span.
        String browserMessage = TestOutcome.nativeValidationMessage(
                driver, "customer.address.zipCode");
        if (!browserMessage.isBlank()) {
            assertFalse(driver.getPageSource().contains("Your account was created successfully"),
                    "Registration must not succeed without a postal code");
            return; // Browser rejected the missing field; test expectation satisfied.
        }

        String result = TestOutcome.afterSubmit(driver, wait,
                "Your account was created successfully",
                "customer.address.zipCode.errors",
                "Registration_missingPostalCode");

        boolean registrationSucceeded =
                result.contains("Your account was created successfully");
        boolean validationDisplayed = TestOutcome.fieldError(
                driver, "customer.address.zipCode.errors");

        assertAll("Missing postal code validation",
                () -> assertFalse(registrationSucceeded,
                        "DEFECT: registration accepted a missing postal code: " + result),
                () -> assertTrue(validationDisplayed,
                        "Expected a field-specific postal-code validation error: " + result));
    }

    @Test void TC_REG_014_missingSsn() {
        assertMissingRegistrationField("customer.ssn", "customer.ssn.errors", "Missing SSN");
    }

}
