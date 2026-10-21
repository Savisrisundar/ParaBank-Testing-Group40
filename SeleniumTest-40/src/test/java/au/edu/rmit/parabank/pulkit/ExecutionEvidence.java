package au.edu.rmit.parabank.pulkit;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Captures the final UI before BaseTest closes Chrome; never changes a test's result. */
public final class ExecutionEvidence implements AfterTestExecutionCallback {
    @Override public void afterTestExecution(ExtensionContext context) {
        Throwable problem = context.getExecutionException().orElse(null);
        String status = problem == null ? "PASSED" : problem instanceof TestAbortedException ? "BLOCKED" : "FAILED";
        Path directory = Path.of("target", "evidence", context.getRequiredTestClass().getSimpleName(),
                context.getRequiredTestMethod().getName());
        try {
            Files.createDirectories(directory);
            String result = "status=" + status + "\nmethod=" + context.getRequiredTestMethod().getName()
                    + "\ntime=" + java.time.Instant.now() + "\n";
            if (problem != null) result += "reason=" + problem + "\n";
            Files.writeString(directory.resolve("result.txt"), result, StandardCharsets.UTF_8);
            Object instance = context.getRequiredTestInstance();
            Class<?> type = instance.getClass();
            while (type != null) {
                try {
                    java.lang.reflect.Field field = type.getDeclaredField("driver");
                    field.setAccessible(true);
                    WebDriver driver = (WebDriver) field.get(instance);
                    if (driver instanceof TakesScreenshot screenshot) {
                        Files.write(directory.resolve("final-state.png"), screenshot.getScreenshotAs(OutputType.BYTES));
                    }
                    break;
                } catch (NoSuchFieldException ex) { type = type.getSuperclass(); }
            }
        } catch (Exception ex) {
            System.err.println("Evidence capture failed; original result remains " + status + ": " + ex);
        }
    }
}
