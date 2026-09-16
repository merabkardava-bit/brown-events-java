package com.brownevents.app.e2e;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BEVJ-205 - Flow 2: Open session detail → register attendee → verify registration appears
 * Covers happy path (active conference registration + verification + cleanup)
 * and failure scenarios (missing required fields validation + inactive conference rejection).
 */
@Tag("e2e")
public class SessionRegistrationE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("Flow 2 Success: Open session detail, register attendee, verify registration in attendee list, and cleanup")
    public void openSessionDetail_registerAttendee_verifyRegistrationAppears() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "e2e_attendee_" + uniqueId + "@example.com";
        String firstName = "JavaE2E";
        String lastName = "User" + uniqueId;

        // 1. Navigate to conference detail with sessions (e.g. conference 2 Java Developer Days - ACTIVE)
        page.navigate(BASE_URL + "/conferences/2");
        page.waitForSelector(".detail-header__title");

        // Locate first session card and click 'View Details' link
        Locator sessionCard = page.locator(".session-card").first();
        sessionCard.waitFor();
        Locator viewDetailsLink = sessionCard.locator("a:has-text('View Details')");
        assertTrue(viewDetailsLink.isVisible(), "'View Details' link should be visible on session card");
        viewDetailsLink.click();

        // Verify session detail page loads
        page.waitForSelector(".detail-header__title");
        Locator sessionHeader = page.locator(".detail-header__title");
        assertTrue(sessionHeader.isVisible(), "Session detail header should be visible");

        Locator sessionDetailSection = page.locator(".detail-section:has-text('Session Details')");
        assertTrue(sessionDetailSection.isVisible(), "Session details section should be displayed");

        // 2. Return to conference detail page via back link to complete registration
        Locator backLink = page.locator("a:has-text('Back to Conference')");
        assertTrue(backLink.isVisible(), "Back to Conference link should be present");
        backLink.click();
        page.waitForSelector(".detail-header__title");

        // Open registration modal
        Locator registerButton = page.locator("button:has-text('Register Now')");
        assertTrue(registerButton.isVisible(), "Register Now button should be visible for active conference");
        registerButton.click();
        page.waitForSelector(".modal form");

        // Fill attendee registration form
        page.locator("#reg-firstName").fill(firstName);
        page.locator("#reg-lastName").fill(lastName);
        page.locator("#reg-email").fill(testEmail);
        page.locator("#reg-company").fill("Test Corp");

        // Submit registration form
        page.locator(".modal button[type='submit']").click();

        // Verify success message box
        page.waitForSelector(".success-box");
        Locator successBox = page.locator(".success-box");
        assertTrue(successBox.isVisible(), "Success box should appear after successful registration");
        assertTrue(successBox.innerText().contains("Registration Successful!"),
                "Success box should state Registration Successful!");

        // Close modal
        page.locator(".success-box button:has-text('Close')").click();

        // 3. Verify newly registered attendee appears in the registrations list
        Locator attendeeRow = page.locator(".attendee-row:has-text('" + testEmail + "')");
        try {
            attendeeRow.waitFor(new Locator.WaitForOptions().setTimeout(3000));
        } catch (Exception e) {
            page.reload();
            attendeeRow.waitFor();
        }
        assertTrue(attendeeRow.isVisible(), "Newly registered attendee should appear in attendee list");
        assertTrue(attendeeRow.innerText().contains(firstName), "Attendee row should display attendee first name");

        // Cleanup: remove registration to leave database clean
        Locator removeButton = attendeeRow.locator("button:has-text('Remove')");
        removeButton.click();
        attendeeRow.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertFalse(page.locator(".attendee-row:has-text('" + testEmail + "')").isVisible(),
                "Attendee row should be removed after cleanup");
    }

    @Test
    @DisplayName("Flow 2 Failure: Required fields validation on registration & registration closed on completed conference")
    public void registrationFailure_requiredFieldsAndClosedConference() {
        // Sub-case 1: Form validation failure when required email is omitted
        page.navigate(BASE_URL + "/conferences/2");
        page.waitForSelector(".detail-header__title");

        Locator registerButton = page.locator("button:has-text('Register Now')");
        registerButton.click();
        page.waitForSelector(".modal form");

        page.locator("#reg-firstName").fill("Jane");
        page.locator("#reg-lastName").fill("Doe");
        page.locator("#reg-email").fill("");

        // Check HTML5 validity of required email input
        Object isValid = page.locator("#reg-email").evaluate("el => el.checkValidity()");
        assertFalse(Boolean.TRUE.equals(isValid), "Email input without value should fail HTML5 validity check");

        page.locator(".modal__close").click();

        // Sub-case 2: Inactive/completed conference blocks registration
        page.navigate(BASE_URL + "/conferences/3");
        page.waitForSelector(".detail-header__title");

        Locator closedRegisterButton = page.locator("button:has-text('Register Now')");
        assertFalse(closedRegisterButton.isVisible(), "Register Now button must not be visible on completed conference");

        Locator notice = page.locator("text=This conference has ended. Registration is closed.");
        assertTrue(notice.isVisible(), "Closed notice should be visible for completed conference");

        // Direct navigation to registration page on completed conference
        page.navigate(BASE_URL + "/conferences/3/register");
        page.waitForSelector("form");
        Locator submitButton = page.locator("button[type='submit']");
        assertTrue(submitButton.isDisabled(), "Submit button should be disabled for completed conference registration");
    }
}
