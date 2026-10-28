package com.albertapayments.tests;

import com.albertapayments.base.BaseTest;
import com.albertapayments.pages.DashboardPage;
import com.albertapayments.pages.LoginPage;
import com.albertapayments.utilities.ConfigReader;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * DashboardTest verifies the Alberta Payments Kiosk Backoffice shell,
 * header components, store dropdown switcher, sidebar drawer, and authentication guards.
 */
public class DashboardTest extends BaseTest {

    private DashboardPage dashboardPage;
    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        dashboardPage = new DashboardPage(getPage());
        loginPage = new LoginPage(getPage());
    }

    @Test(groups = {"smoke", "regression"}, description = "DASH_001: Verify unauthenticated direct navigation to /dashboard is blocked by auth guard")
    public void testUnauthenticatedDashboardAccessBlocked() {
        String baseUrl = ConfigReader.getProperty("baseUrl", "https://menu.albertapayments.com/");
        String dashboardUrl = baseUrl.endsWith("/") ? baseUrl + "dashboard" : baseUrl + "/dashboard";

        logger.info("Attempting unauthenticated navigation to: {}", dashboardUrl);
        getPage().navigate(dashboardUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        boolean isLoaded = loginPage.isLoginPageLoaded();
        Assert.assertTrue(isLoaded, "Unauthenticated user was not redirected to the login page.");
        Assert.assertFalse(getPage().url().endsWith("/dashboard"), "Unauthenticated user should not remain on /dashboard route.");
    }

    @Test(groups = {"smoke", "regression"}, description = "DASH_002: Verify unauthenticated direct navigation to /kiosk/menu-items-list is blocked")
    public void testUnauthenticatedKioskRouteBlocked() {
        String baseUrl = ConfigReader.getProperty("baseUrl", "https://menu.albertapayments.com/");
        String kioskUrl = baseUrl.endsWith("/") ? baseUrl + "kiosk/menu-items-list" : baseUrl + "/kiosk/menu-items-list";

        logger.info("Attempting unauthenticated navigation to: {}", kioskUrl);
        getPage().navigate(kioskUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        boolean isLoaded = loginPage.isLoginPageLoaded();
        Assert.assertTrue(isLoaded, "Unauthenticated user was not redirected to login from /kiosk route.");
    }

    @Test(groups = {"smoke", "sanity"}, description = "DASH_003: Verify dashboard header and shell layout when logged in")
    public void testDashboardHeaderElements() {
        authenticateIfCredentialsAvailable();

        Assert.assertTrue(dashboardPage.isHeaderVisible(), "Header container was not visible.");
        Assert.assertTrue(dashboardPage.isLogoVisible(), "Brand logo in header was not visible.");
        Assert.assertTrue(dashboardPage.isStoreSelectorVisible(), "Store selector dropdown was not visible.");
        Assert.assertTrue(dashboardPage.isLogoutButtonVisible(), "Logout button was not visible in header.");
    }

    @Test(groups = {"functional", "regression"}, description = "DASH_004: Verify sidebar drawer expand and collapse interaction")
    public void testSidebarDrawerInteractions() {
        authenticateIfCredentialsAvailable();

        Assert.assertTrue(dashboardPage.isSidebarVisible(), "Sidebar drawer was not visible.");
        dashboardPage.toggleSidebar();
        dashboardPage.toggleSidebar();
        Assert.assertTrue(dashboardPage.isSidebarVisible(), "Sidebar drawer was not accessible after toggling.");
    }

    @Test(groups = {"regression"}, description = "DASH_005: Verify logout workflow terminates session and redirects to login")
    public void testLogoutWorkflow() {
        authenticateIfCredentialsAvailable();

        LoginPage returnedLoginPage = dashboardPage.clickLogout();
        Assert.assertTrue(returnedLoginPage.isLoginPageLoaded(), "User was not returned to login page after logout.");
        Assert.assertFalse(getPage().url().contains("/kiosk"), "Session was not terminated upon logout.");
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
