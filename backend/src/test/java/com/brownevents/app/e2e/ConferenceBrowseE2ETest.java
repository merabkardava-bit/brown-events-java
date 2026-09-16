package com.brownevents.app.e2e;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BEVJ-205 - Flow 1: Browse conferences → open detail → view sessions
 * Covers happy path and failure scenario (non-existent conference ID).
 */
@Tag("e2e")
public class ConferenceBrowseE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("Flow 1 Success: Browse conferences list, open conference detail, view and select sessions")
    public void browseConferences_openDetail_viewSessions() {
        // 1. Browse conferences
        page.navigate(BASE_URL + "/");
        page.waitForSelector(".page__title");
        Locator pageTitle = page.locator(".page__title");
        assertTrue(pageTitle.innerText().contains("Conferences"), "Home page should display Conferences heading");

        // Wait for conference cards to render
        Locator cards = page.locator(".grid .card");
        cards.first().waitFor();
        assertTrue(cards.count() > 0, "Conference list should contain conference cards");

        Locator firstCard = cards.first();
        String conferenceTitle = firstCard.locator(".card__title").innerText().trim();
        assertFalse(conferenceTitle.isEmpty(), "Conference card should have a non-empty title");

        // 2. Open conference detail
        firstCard.click();
        page.waitForSelector(".detail-header__title");
        Locator detailTitle = page.locator(".detail-header__title");
        assertTrue(detailTitle.isVisible(), "Conference detail title should be visible");
        assertTrue(detailTitle.innerText().contains(conferenceTitle),
                "Detail page title should match clicked conference title");

        // 3. View sessions list
        Locator sessionsSection = page.locator(".detail-section:has-text('Sessions')");
        assertTrue(sessionsSection.isVisible(), "Sessions section should be visible on detail page");

        Locator sessionCards = sessionsSection.locator(".session-card");
        sessionCards.first().waitFor();
        assertTrue(sessionCards.count() > 0, "Sessions section should contain session cards");

        Locator firstSession = sessionCards.first();
        String sessionTitle = firstSession.locator(".session-card__title").innerText().trim();
        assertFalse(sessionTitle.isEmpty(), "Session title should not be empty");

        // Click session card to view selected session details panel
        firstSession.click();
        Locator selectedSessionPanel = page.locator(".detail-section:has-text('Selected Session')");
        assertTrue(selectedSessionPanel.isVisible(), "Selected session details panel should be visible after click");
    }

    @Test
    @DisplayName("Flow 1 Failure: Navigating to non-existent conference ID displays error container gracefully")
    public void nonExistentConference_shouldDisplayGracefulErrorState() {
        // Given invalid conference ID
        page.navigate(BASE_URL + "/conferences/999999");

        // Then error container is shown instead of blank or broken page
        page.waitForSelector(".error-container");
        Locator errorContainer = page.locator(".error-container");
        assertTrue(errorContainer.isVisible(), "Error container should be visible for non-existent conference");
        assertTrue(errorContainer.innerText().contains("Failed to load conference"),
                "Error container should display failed to load message");

        // And breadcrumb allows navigating back to conferences
        Locator breadcrumbConferencesLink = page.locator(".breadcrumb a:has-text('Conferences')");
        assertTrue(breadcrumbConferencesLink.isVisible(), "Breadcrumb link to Conferences should be visible");

        breadcrumbConferencesLink.click();
        page.waitForSelector(".page__title");
        assertTrue(page.locator(".page__title").innerText().contains("Conferences"),
                "Navigating via breadcrumb should return user to Conferences list");
    }
}
