package com.albertapayments.utilities;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class providing explicit wait mechanisms and assertions
 * wrapping Playwright auto-waiting.
 */
public class WaitUtil {

    private static final Logger logger = LogManager.getLogger(WaitUtil.class);
    private static final double DEFAULT_TIMEOUT_MS = 15000;

    private WaitUtil() {
        // Prevent direct instantiation
    }

    /**
     * Waits for an element to become visible.
     *
     * @param locator Playwright Locator.
     */
    public static void waitForVisible(Locator locator) {
        waitForVisible(locator, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Waits for an element to become visible within custom timeout.
     *
     * @param locator   Playwright Locator.
     * @param timeoutMs Timeout in milliseconds.
     */
    public static void waitForVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
        } catch (Exception e) {
            logger.error("Timed out waiting for locator to become visible: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Waits for an element to be hidden or removed from the DOM.
     *
     * @param locator Playwright Locator.
     */
    public static void waitForHidden(Locator locator) {
        waitForHidden(locator, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Waits for an element to be hidden within custom timeout.
     *
     * @param locator   Playwright Locator.
     * @param timeoutMs Timeout in milliseconds.
     */
    public static void waitForHidden(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(timeoutMs));
        } catch (Exception e) {
            logger.error("Timed out waiting for locator to become hidden: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Waits for DOMContentLoaded load state.
     *
     * @param page Playwright Page.
     */
    public static void waitForPageLoad(Page page) {
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        } catch (Exception e) {
            logger.warn("Page load state wait warning: {}", e.getMessage());
        }
    }

    /**
     * Waits for network to be idle (useful after async React dispatch).
     *
     * @param page Playwright Page.
     */
    public static void waitForNetworkIdle(Page page) {
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        } catch (Exception e) {
            logger.debug("Network idle timeout reached (non-fatal): {}", e.getMessage());
        }
    }
}
