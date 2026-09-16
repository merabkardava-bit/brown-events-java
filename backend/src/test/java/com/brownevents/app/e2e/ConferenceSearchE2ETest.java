package com.brownevents.app.e2e;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BEVJ-205 - Flow 3: Open conference search → apply keyword filter → verify results update
 * Covers happy path (search by keyword, verify filtered results, restore on clear)
 * and failure scenarios (no matching results empty state, invalid date range error).
 */
@Tag("e2e")
public class ConferenceSearchE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("Flow 3 Success: Search conferences by keyword, verify results update dynamically, and restore")
    public void searchConferences_applyKeywordFilter_verifyResultsUpdate() {
        page.navigate(BASE_URL + "/");
        page.waitForSelector(".page__title");

        Locator initialCards = page.locator(".grid .card");
        initialCards.first().waitFor();
        int initialCount = initialCards.count();
        assertTrue(initialCount > 0, "Initial conference list should contain conferences");

        // Apply search keyword 'Spring'
        Locator searchInput = page.locator("input[placeholder='Search conferences...']");
        assertTrue(searchInput.isVisible(), "Search input should be visible");
        searchInput.fill("Spring");

        // Wait for debounced search request (300ms debounce in frontend)
        page.waitForTimeout(600);

        Locator filteredCards = page.locator(".grid .card");
        filteredCards.first().waitFor();
        int filteredCount = filteredCards.count();
        assertTrue(filteredCount > 0, "Filtered conferences should have at least 1 match for 'Spring'");

        for (int i = 0; i < filteredCount; i++) {
            String cardText = filteredCards.nth(i).innerText().toLowerCase();
            assertTrue(cardText.contains("spring"), "Each filtered conference card should contain keyword 'Spring'");
        }

        // Clear search input
        searchInput.fill("");
        page.waitForTimeout(600);

        Locator restoredCards = page.locator(".grid .card");
        restoredCards.first().waitFor();
        assertEquals(initialCount, restoredCards.count(), "Clearing search input should restore initial conference count");
    }

    @Test
    @DisplayName("Flow 3 Failure: Keyword with no matches displays empty state & invalid date range displays inline error")
    public void searchFailure_noMatchesEmptyState_andInvalidDateRange() {
        page.navigate(BASE_URL + "/");
        page.waitForSelector(".page__title");

        Locator searchInput = page.locator("input[placeholder='Search conferences...']");
        assertTrue(searchInput.isVisible(), "Search input should be visible");

        // Sub-case 1: Search query with no matches
        searchInput.fill("NO_MATCHING_CONFERENCE_RANDOM_QUERY_XYZ_123");
        page.waitForTimeout(600);

        Locator emptyMessage = page.locator("text=No conferences match your search.");
        assertTrue(emptyMessage.isVisible(), "Empty state message should be displayed when no matches are found");
        assertEquals(0, page.locator(".grid .card").count(), "Card list should be empty when no search results match");

        // Sub-case 2: Invalid date range (From date after To date)
        searchInput.fill("");

        // Wait for debounce and reload to complete so filter inputs are re-mounted
        Locator fromInput = page.locator("input[title='From date']");
        fromInput.waitFor();
        Locator toInput = page.locator("input[title='To date']");
        toInput.waitFor();
        assertTrue(fromInput.isVisible() && toInput.isVisible(), "Date range inputs should be visible");

        fromInput.fill("2026-12-01");
        toInput.fill("2026-01-01");

        Locator dateRangeError = page.locator("text=\"From\" date must not be after \"To\" date.");
        assertTrue(dateRangeError.isVisible(), "Validation error should appear when From date is after To date");
    }
}
