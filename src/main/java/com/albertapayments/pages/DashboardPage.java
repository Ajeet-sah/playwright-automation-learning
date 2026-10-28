package com.albertapayments.pages;

import com.albertapayments.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * DashboardPage encapsulates locators and actions for the Alberta Payments
 * shell navigation, header, sidebar drawer, store switcher, and logout workflows.
 */
public class DashboardPage extends BasePage {

    // Header elements
    private final Locator headerContainer;
    private final Locator brandLogo;
    private final Locator storeSelector;
    private final Locator logoutButton;
    private final Locator kioskTopNavLink;

    // Sidebar drawer elements
    private final Locator sidebarDrawer;
    private final Locator drawerToggleButton;
    private final Locator menuDepartmentsLink;
    private final Locator menuCategoriesLink;
    private final Locator menuItemsLink;
    private final Locator modifierGroupsLink;
    private final Locator modifierItemsLink;
    private final Locator settingsLink;

    public DashboardPage(Page page) {
        super(page);
        this.headerContainer = page.locator("header.nav-container");
        this.brandLogo = page.locator("header.nav-container img.logo");
        this.storeSelector = page.locator("div[class*='MuiSelect-select']");
        this.logoutButton = page.locator("button[aria-label='Logout']");
        this.kioskTopNavLink = page.locator("nav.navbar-background a:has-text('KIOSK')");

        this.sidebarDrawer = page.locator(".MuiDrawer-root");
        this.drawerToggleButton = page.locator(".MuiDrawer-root button");
        this.menuDepartmentsLink = page.locator("a[href*='/kiosk/menu-departments']");
        this.menuCategoriesLink = page.locator("a[href*='/kiosk/menu-categories']");
        this.menuItemsLink = page.locator("a[href*='/kiosk/menu-items-list']");
        this.modifierGroupsLink = page.locator("a[href*='/kiosk/modifier-groups']");
        this.modifierItemsLink = page.locator("a[href*='/kiosk/modifier-items']");
        this.settingsLink = page.locator("div:has-text('Settings')").last();
    }

    /**
     * Verifies if dashboard shell is loaded.
     *
     * @return true if header or sidebar is visible.
     */
    public boolean isDashboardLoaded() {
        return isVisible(headerContainer) || isVisible(sidebarDrawer) || getPageUrl().contains("/kiosk");
    }

    /**
     * Checks if the header container is visible.
     *
     * @return true if visible.
     */
    public boolean isHeaderVisible() {
        return isVisible(headerContainer);
    }

    /**
     * Checks if the brand logo in the header is visible.
     *
     * @return true if visible.
     */
    public boolean isLogoVisible() {
        return isVisible(brandLogo);
    }

    /**
     * Checks if the store selector dropdown is visible.
     *
     * @return true if visible.
     */
    public boolean isStoreSelectorVisible() {
        return isVisible(storeSelector);
    }

    /**
     * Checks if the logout icon button is visible in header.
     *
     * @return true if visible.
     */
    public boolean isLogoutButtonVisible() {
        return isVisible(logoutButton);
    }

    /**
     * Checks if the sidebar drawer is visible.
     *
     * @return true if visible.
     */
    public boolean isSidebarVisible() {
        return isVisible(sidebarDrawer);
    }

    /**
     * Toggles sidebar drawer expand/collapse.
     */
    public void toggleSidebar() {
        logger.info("Toggling sidebar drawer");
        click(drawerToggleButton);
    }

    /**
     * Navigates to Menu Items module via sidebar link.
     *
     * @return MenuPage instance.
     */
    public MenuPage navigateToMenuItems() {
        logger.info("Navigating to Menu Items page");
        if (isVisible(menuItemsLink)) {
            click(menuItemsLink);
        } else {
            navigateTo(page.url().replaceAll("/kiosk/.*", "/kiosk/menu-items-list"));
        }
        return new MenuPage(page);
    }

    /**
     * Navigates to Menu Categories module via sidebar link.
     *
     * @return MenuPage instance.
     */
    public MenuPage navigateToMenuCategories() {
        logger.info("Navigating to Menu Categories page");
        if (isVisible(menuCategoriesLink)) {
            click(menuCategoriesLink);
        } else {
            navigateTo(page.url().replaceAll("/kiosk/.*", "/kiosk/menu-categories"));
        }
        return new MenuPage(page);
    }

    /**
     * Navigates to Menu Departments module via sidebar link.
     */
    public void navigateToMenuDepartments() {
        logger.info("Navigating to Menu Departments page");
        if (isVisible(menuDepartmentsLink)) {
            click(menuDepartmentsLink);
        } else {
            navigateTo(page.url().replaceAll("/kiosk/.*", "/kiosk/menu-departments"));
        }
    }

    /**
     * Performs logout by clicking the header logout button, confirming the dialog,
     * and returning to the LoginPage.
     *
     * @return LoginPage instance.
     */
    public LoginPage clickLogout() {
        logger.info("Performing logout");
        click(logoutButton);
        
        // Handle confirmation modal if present
        Locator confirmButton = page.locator("button:has-text('Yes'), button:has-text('Confirm'), button:has-text('Logout')");
        if (isVisible(confirmButton)) {
            click(confirmButton);
        }
        return new LoginPage(page);
    }
}
