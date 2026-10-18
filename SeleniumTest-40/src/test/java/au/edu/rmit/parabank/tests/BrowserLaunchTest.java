package au.edu.rmit.parabank.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

/** Simple smoke test to verify Chrome can open the expected ParaBank homepage. */
public class BrowserLaunchTest {

    private WebDriver driver;

    /** Give each test a fresh Chrome browser session. */
    @BeforeEach
    void setUp() {
        driver = new ChromeDriver(new ChromeOptions());
        driver.manage().window().maximize();
    }

    @Test
    void verifyParaBankHomePage() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        String title = driver.getTitle();
        String currentUrl = driver.getCurrentUrl();

        // Assert a meaningful title and that navigation reached the expected page.
        assertAll("ParaBank homepage smoke test",
                () -> assertFalse(title == null || title.isBlank(),
                        "Homepage title should not be blank"),
                () -> assertTrue(title != null && title.contains("ParaBank"),
                        "Homepage title should identify ParaBank; actual: " + title),
                () -> assertTrue(currentUrl.contains("/parabank/index.htm"),
                        "Expected ParaBank homepage URL; actual: " + currentUrl)
        );
    }

    /** Close Chrome even if an assertion fails. */
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
