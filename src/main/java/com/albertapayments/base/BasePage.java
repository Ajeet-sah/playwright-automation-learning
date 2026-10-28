package com.albertapayments.base;

import com.albertapayments.utilities.WaitUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * BasePage encapsulates common Playwright actions with built-in auto-waiting,
 * logging, and robust error handling for all Page Object classes.
 */
public abstract class BasePage {

    protected final Logger logger = LogManager.getLogger(getClass());
    protected final Page page;

    /**
     * Constructor initializing the BasePage with active Playwright Page.
     *
     * @param page Playwright Page instance.
     */
    public BasePage(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("Page instance cannot be null when initializing " + getClass().getSimpleName());
        }
        this.page = page;
    }

    /**
     * @return Underlying Playwright Page instance.
     */
    public Page getPage() {
        return page;
    }

    /**
     * Navigates to the specified URL and waits for DOM readiness.
     *
     * @param url Application URL to navigate to.
     */
    public void navigateTo(String url) {
        logger.info("Navigating to URL: {}", url);
        page.navigate(url);
        WaitUtil.waitForPageLoad(page);
    }

    /**
     * @return Current page title.
     */
    public String getPageTitle() {
        String title = page.title();
        logger.debug("Current page title: '{}'", title);
        return title;
    }

    /**
     * @return Current URL of the browser page.
     */
    public String getPageUrl() {
        String url = page.url();
        logger.debug("Current page URL: '{}'", url);
        return url;
    }

    /**
     * Clicks an element identified by Playwright Locator.
     * Auto-waits for visibility, stability, and enabled state.
     *
     * @param locator Element locator.
     */
    public void click(Locator locator) {
        logger.info("Clicking on locator: {}", locator);
        locator.click();
    }

    /**
     * Fills an input field with text using Playwright auto-waiting.
     * Clears existing content before filling.
     *
     * @param locator Element locator.
     * @param text    Text to fill.
     */
    public void fill(Locator locator, String text) {
        logger.info("Filling text '{}' into locator: {}", text != null ? text : "", locator);
        locator.fill(text != null ? text : "");
    }

    /**
     * Clears existing text and types new text into the input field.
     *
     * @param locator Element locator.
     * @param text    Text to type.
     */
    public void clearAndFill(Locator locator, String text) {
        locator.clear();
        locator.fill(text != null ? text : "");
    }

    /**
     * Retrieves visible text content of an element.
     *
     * @param locator Element locator.
     * @return Trimmed inner text of the element.
     */
    public String getText(Locator locator) {
        WaitUtil.waitForVisible(locator);
        String text = locator.innerText().trim();
        logger.debug("Retrieved text '{}' from locator: {}", text, locator);
        return text;
    }

    /**
     * Checks if an element is currently visible on the page.
     *
     * @param locator Element locator.
     * @return true if visible, false otherwise.
     */
    public boolean isVisible(Locator locator) {
        boolean visible = locator.isVisible();
        logger.debug("Locator visibility check [{}]: {}", locator, visible);
        return visible;
    }

    /**
     * Selects an option in a HTML dropdown element by value.
     *
     * @param locator Dropdown select element.
     * @param value   Option value or label to select.
     */
    public void selectOption(Locator locator, String value) {
        logger.info("Selecting option '{}' in dropdown: {}", value, locator);
        locator.selectOption(new SelectOption().setValue(value));
    }

    /**
     * Waits for an element to become visible.
     *
     * @param locator Element locator.
     */
    public void waitForVisible(Locator locator) {
        WaitUtil.waitForVisible(locator);
    }

    /**
     * Waits for an element to become hidden.
     *
     * @param locator Element locator.
     */
    public void waitForHidden(Locator locator) {
        WaitUtil.waitForHidden(locator);
    }

    /**
     * Presses a keyboard key on the specified locator (e.g. "Enter", "Tab").
     *
     * @param locator Target locator.
     * @param key     Key name.
     */
    public void pressKey(Locator locator, String key) {
        logger.info("Pressing key '{}' on locator: {}", key, locator);
        locator.press(key);
    }

    /**
     * Retrieves the specified attribute value from the locator.
     *
     * @param locator       Target locator.
     * @param attributeName HTML attribute name.
     * @return Attribute value.
     */
    public String getAttribute(Locator locator, String attributeName) {
        return locator.getAttribute(attributeName);
    }
}
