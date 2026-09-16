package com.brownevents.app.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Base class for Playwright Java E2E test suites.
 * Handles browser lifecycle and captures full-page screenshot on test failure.
 */
public abstract class BaseE2ETest {

    protected static final String BASE_URL = System.getProperty("frontend.url",
            System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:3000");

    private static final String SCREENSHOTS_DIR = "target/e2e-screenshots";

    protected static Playwright playwright;
    protected static Browser browser;
    protected Page page;

    @RegisterExtension
    TestWatcher screenshotOnFailureWatcher = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            if (page != null && !page.isClosed()) {
                try {
                    File dir = new File(SCREENSHOTS_DIR);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    String testName = context.getRequiredTestClass().getSimpleName() + "_" +
                            context.getRequiredTestMethod().getName();
                    Path screenshotPath = Paths.get(SCREENSHOTS_DIR, testName + ".png");
                    page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath).setFullPage(true));
                    System.err.println("[E2E Failure Screenshot Saved] -> " + screenshotPath.toAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Could not capture screenshot on test failure: " + e.getMessage());
                }
            }
        }
    };

    @BeforeAll
    public static void setUpAll() {
        playwright = Playwright.create();
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @AfterAll
    public static void tearDownAll() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    public void setUp() {
        page = browser.newPage();
    }

    @AfterEach
    public void tearDown() {
        if (page != null) {
            page.close();
        }
    }
}
