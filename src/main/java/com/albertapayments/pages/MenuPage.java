package com.albertapayments.pages;

import com.albertapayments.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

/**
 * MenuPage encapsulates locators and actions for the Menu Items and Menu Categories
 * modules within the Alberta Payments Kiosk Backoffice.
 */
public class MenuPage extends BasePage {

    // Menu Items locators
    private final Locator menuItemsHeading;
    private final Locator menuSearchInput;
    private final Locator addMenuItemButton;
    private final Locator departmentFilter;
    private final Locator categoryFilter;
    private final Locator statusFilter;
    private final Locator menuItemCards;
    private final Locator itemTitles;
    private final Locator itemPrices;
    private final Locator emptyStateMessage;

    // Menu Categories locators
    private final Locator menuCategoriesHeading;
    private final Locator categorySearchInput;
    private final Locator addCategoryButton;

    public MenuPage(Page page) {
        super(page);
        this.menuItemsHeading = page.locator("span:has-text('MENU ITEMS'), h4:has-text('MENU ITEMS'), h3:has-text('MENU ITEMS')");
        this.menuSearchInput = page.getByPlaceholder("Search menu items...");
        this.addMenuItemButton = page.locator("a[href*='/kiosk/add-menu-items'], button:has-text('Add Menu Item')");
        this.departmentFilter = page.locator("select[name='department_id'], div[id*='department']");
        this.categoryFilter = page.locator("select[name='category_id'], div[id*='category']");
        this.statusFilter = page.locator("select[name='status_id'], div[id*='status']");
        this.menuItemCards = page.locator(".menu-item, .draggable-card-wrapper");
        this.itemTitles = page.locator(".menu-title");
        this.itemPrices = page.locator(".menu-price");
        this.emptyStateMessage = page.locator("text='There are no menu items available yet.', text='No data available.'");

        this.menuCategoriesHeading = page.locator("span:has-text('Menu Categories'), h4:has-text('Menu Categories')");
        this.categorySearchInput = page.getByPlaceholder("Search menu categories...");
        this.addCategoryButton = page.locator("a[href*='/kiosk/add-category'], button:has-text('Add Category')");
    }

    /**
     * Checks if the Menu Items page is loaded.
     *
     * @return true if heading or search input is visible.
     */
    public boolean isMenuPageLoaded() {
        return isVisible(menuItemsHeading) || isVisible(menuSearchInput) || page.url().contains("/kiosk/menu-items");
    }

    /**
     * Checks if the Menu Categories page is loaded.
     *
     * @return true if categories heading or search input is visible.
     */
    public boolean isCategoryPageLoaded() {
        return isVisible(menuCategoriesHeading) || isVisible(categorySearchInput) || page.url().contains("/kiosk/menu-categories");
    }

    /**
     * Searches for menu items by typing into the search input.
     *
     * @param query Search query text.
     * @return Current MenuPage instance.
     */
    public MenuPage searchMenuItem(String query) {
        logger.info("Searching for menu item: '{}'", query);
        fill(menuSearchInput, query);
        pressKey(menuSearchInput, "Enter");
        return this;
    }

    /**
     * Clears the menu item search input.
     *
     * @return Current MenuPage instance.
     */
    public MenuPage clearSearch() {
        logger.info("Clearing search input");
        clearAndFill(menuSearchInput, "");
        return this;
    }

    /**
     * Searches for categories on the Menu Categories page.
     *
     * @param query Category name to search.
     * @return Current MenuPage instance.
     */
    public MenuPage searchCategory(String query) {
        logger.info("Searching for category: '{}'", query);
        fill(categorySearchInput, query);
        pressKey(categorySearchInput, "Enter");
        return this;
    }

    /**
     * @return Number of visible menu item cards displayed on the grid.
     */
    public int getMenuItemCount() {
        int count = menuItemCards.count();
        logger.debug("Visible menu item count: {}", count);
        return count;
    }

    /**
     * @return List of all visible menu item titles.
     */
    public List<String> getAllItemTitles() {
        return itemTitles.allInnerTexts();
    }

    /**
     * Checks if the "Add Menu Item" button is visible.
     *
     * @return true if visible.
     */
    public boolean isAddMenuItemButtonVisible() {
        return isVisible(addMenuItemButton);
    }

    /**
     * Clicks the "Add Menu Item" button to open creation form.
     */
    public void clickAddMenuItem() {
        logger.info("Clicking Add Menu Item button");
        click(addMenuItemButton);
    }

    /**
     * Checks if empty state message is visible when no items match search or store is empty.
     *
     * @return true if empty state is visible.
     */
    public boolean isEmptyStateVisible() {
        return isVisible(emptyStateMessage);
    }

    /**
     * @return Empty state text message.
     */
    public String getEmptyStateText() {
        return getText(emptyStateMessage);
    }
}
