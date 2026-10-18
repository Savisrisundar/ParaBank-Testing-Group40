package au.edu.rmit.parabank.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Shared JUnit 5 setup and teardown: a clean Chrome session for every test. */
public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final String BASE_URL =
            "https://parabank.parasoft.com/parabank";

    /** Browser is isolated per test to avoid order-dependent authentication state. */
    @BeforeEach
    protected void setUp() {
        ChromeOptions options = new ChromeOptions();

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(30));

        wait = new WebDriverWait(
                driver, Duration.ofSeconds(15)
        );

        driver.get(BASE_URL + "/index.htm");
    }

    /** Always release ChromeDriver, even when an assertion fails. */
    @AfterEach
    protected void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
