package com.albertapayments.tests;

import com.albertapayments.base.BaseTest;
import com.albertapayments.pages.LoginPage;
import com.albertapayments.pages.MenuPage;
import com.albertapayments.utilities.ConfigReader;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * MenuTest verifies the Menu Items, Menu Categories, and search/filter operations
 * within the Alberta Payments Kiosk Backoffice.
 */
public class MenuTest extends BaseTest {

    private MenuPage menuPage;
    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        menuPage = new MenuPage(getPage());
        loginPage = new LoginPage(getPage());
    }

    @Test(groups = {"smoke", "regression"}, description = "MENU_001: Verify unauthenticated navigation to menu items is securely blocked")
    public void testUnauthenticatedMenuAccessBlocked() {
        String baseUrl = ConfigReader.getProperty("baseUrl", "https://menu.albertapayments.com/");
        String menuUrl = baseUrl.endsWith("/") ? baseUrl + "kiosk/menu-items-list" : baseUrl + "/kiosk/menu-items-list";

        logger.info("Attempting unauthenticated access to: {}", menuUrl);
        getPage().navigate(menuUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        boolean isLoaded = loginPage.isLoginPageLoaded();
        Assert.assertTrue(isLoaded, "Unauthenticated user was not redirected to the login page.");
        Assert.assertFalse(getPage().url().contains("/kiosk/menu-items-list"), "Unauthenticated user was able to reach menu items list.");
    }

    @Test(groups = {"smoke", "regression"}, description = "MENU_002: Verify Menu Items page loads with search and actions")
    public void testMenuItemsPageLoad() {
        authenticateIfCredentialsAvailable();

        getPage().navigate(ConfigReader.getProperty("baseUrl") + "kiosk/menu-items-list",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        Assert.assertTrue(menuPage.isMenuPageLoaded(), "Menu items page was not loaded.");
        Assert.assertTrue(menuPage.isAddMenuItemButtonVisible(), "Add Menu Item button was not visible.");
    }

    @Test(groups = {"functional", "regression"}, description = "MENU_003: Verify menu item search input handles query entry")
    public void testMenuItemSearch() {
        authenticateIfCredentialsAvailable();

        getPage().navigate(ConfigReader.getProperty("baseUrl") + "kiosk/menu-items-list",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        menuPage.searchMenuItem("Coffee");
        menuPage.clearSearch();
    }

    @Test(groups = {"functional", "regression"}, description = "MENU_004: Verify Menu Categories page loads with category search")
    public void testMenuCategoriesSearch() {
        authenticateIfCredentialsAvailable();

        getPage().navigate(ConfigReader.getProperty("baseUrl") + "kiosk/menu-categories",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        Assert.assertTrue(menuPage.isCategoryPageLoaded(), "Menu categories page was not loaded.");
        menuPage.searchCategory("Beverages");
    }

    private void authenticateIfCredentialsAvailable() {
        String testUser = System.getenv("ALBERTA_TEST_USER");
        String testPass = System.getenv("ALBERTA_TEST_PASS");

        if (testUser == null || testUser.trim().isEmpty() || testPass == null || testPass.trim().isEmpty()) {
            testUser = ConfigReader.getProperty("testUsername");
            testPass = ConfigReader.getProperty("testPassword");
        }

        if (testUser == null || testUser.trim().isEmpty() || testPass == null || testPass.trim().isEmpty()) {
            throw new SkipException("Skipping test: Requires active credentials (set ALBERTA_TEST_USER and ALBERTA_TEST_PASS).");
        }

        loginPage.doLogin(testUser, testPass);
    }
}
