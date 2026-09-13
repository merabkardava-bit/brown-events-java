package com.brownevents.app.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests using Playwright Java.
 * Verifies registration eligibility, button visibility, and notices for
 * active vs closed (completed/cancelled) conferences.
 *
 * Requirements:
 * - Frontend running at FRONTEND_URL (default http://localhost:3000)
 * - Backend running at BACKEND_URL (default http://localhost:8080)
 */
@Tag("e2e")
public class ConferenceRegistrationE2ETest {

    private static final String BASE_URL = System.getProperty("frontend.url", "http://localhost:3000");

    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @BeforeAll
    public static void setUpAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
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

    @Test
    @DisplayName("Active conference should display 'Register Now' button and open registration modal")
    public void activeConference_shouldShowRegisterNowButtonAndOpenModal() {
        // Given conference 2 (Java Developer Days - ACTIVE)
        page.navigate(BASE_URL + "/conferences/2");
        page.waitForSelector(".detail-header__title");

        // Then Register Now button must be visible in DOM
        Locator registerButton = page.locator("button:has-text('Register Now')");
        assertTrue(registerButton.isVisible(), "Register Now button should be visible for ACTIVE conference");

        // When user clicks Register Now
        registerButton.click();

        // Then modal should appear with registration form
        Locator modal = page.locator(".modal");
        assertTrue(modal.isVisible(), "Registration modal should open when clicking Register Now");
        assertTrue(page.locator("#reg-email").isVisible(), "Email input in modal should be visible");
    }

    @Test
    @DisplayName("Completed conference should NOT show 'Register Now' button and should show closed notice")
    public void completedConference_shouldNotShowRegisterButtonAndShowNotice() {
        // Given conference 3 (Cloud & DevOps World - COMPLETED)
        page.navigate(BASE_URL + "/conferences/3");
        page.waitForSelector(".detail-header__title");

        // Then Register Now button must NOT be present in DOM
        Locator registerButton = page.locator("button:has-text('Register Now')");
        assertFalse(registerButton.isVisible(), "Register Now button must not be visible for COMPLETED conference");

        // And notice indicating registration closed must be visible
        Locator notice = page.locator("text=This conference has ended. Registration is closed.");
        assertTrue(notice.isVisible(), "Closed notice should be visible for COMPLETED conference");
    }

    @Test
    @DisplayName("Standalone registration page for completed conference should disable submit button")
    public void standaloneRegistrationPage_completedConference_shouldDisableSubmit() {
        // Given standalone registration page for completed conference (3)
        page.navigate(BASE_URL + "/conferences/3/register");
        page.waitForSelector("form");

        // Then submit button should be disabled
        Locator submitButton = page.locator("button[type='submit']");
        assertTrue(submitButton.isDisabled(), "Submit button should be disabled for closed conference");

        // And closed notice should be displayed
        Locator notice = page.locator("text=This conference has ended. Registration is closed.");
        assertTrue(notice.isVisible(), "Closed notice should be visible on standalone registration page");
    }

    @Test
    @DisplayName("Active conference successful registration and subsequent cleanup")
    public void activeConference_successfulRegistration_andCleanup() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "e2e_attendee_" + uniqueId + "@example.com";
        String firstName = "Playwright";
        String lastName = "User" + uniqueId;

        // Given conference 2 (Java Developer Days - ACTIVE)
        page.navigate(BASE_URL + "/conferences/2");
        page.waitForSelector(".detail-header__title");

        // When opening registration modal
        Locator registerButton = page.locator("button:has-text('Register Now')");
        registerButton.click();
        page.waitForSelector(".modal form");

        // And filling attendee registration form
        page.locator("#reg-firstName").fill(firstName);
        page.locator("#reg-lastName").fill(lastName);
        page.locator("#reg-email").fill(testEmail);

        // And submitting form
        page.locator(".modal button[type='submit']").click();

        // Then success message is shown
        Locator successBox = page.locator(".success-box");
        page.waitForSelector(".success-box");
        assertTrue(successBox.isVisible(), "Success box should appear after successful registration");

        // When closing the modal
        page.locator(".success-box button:has-text('Close')").click();

        // Then newly registered attendee appears in registrations list
        Locator attendeeRow = page.locator(".attendee-row:has-text('" + testEmail + "')");
        page.reload();
        attendeeRow.waitFor();
        assertTrue(attendeeRow.isVisible(), "Newly registered attendee should appear in attendee list");

        // Cleanup: remove the registration to leave the DB in clean state
        Locator removeButton = attendeeRow.locator("button:has-text('Remove')");
        removeButton.click();

        // Verify row is removed
        attendeeRow.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertFalse(page.locator(".attendee-row:has-text('" + testEmail + "')").isVisible(),
                "Registered attendee should be removed after cleanup");
    }
}
