package com.albertapayments.tests;

import com.albertapayments.base.BaseTest;
import com.albertapayments.pages.LoginPage;
import com.albertapayments.utilities.ConfigReader;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * RegressionTest provides an end-to-end regression verification suite
 * ensuring core business rules and security boundaries remain stable.
 */
public class RegressionTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void initPage() {
        loginPage = new LoginPage(getPage());
    }

    @Test(groups = {"regression"}, description = "REG_001: End-to-end regression validation of login page structure and branding")
    public void testFullLoginPageStructure() {
        logger.info("Executing comprehensive login page regression check");
        Assert.assertTrue(loginPage.isLoginPageLoaded(), "Login form card not visible.");
        Assert.assertTrue(loginPage.isLogoVisible(), "Alberta Logo not visible.");
        Assert.assertEquals(loginPage.getFormTitle(), "Log in");
        Assert.assertEquals(loginPage.getSubtitle(), "Please log in to continue");
        Assert.assertTrue(loginPage.isCustomerServiceInfoVisible(), "Support contact footer not visible.");
    }

    @Test(groups = {"regression"}, description = "REG_002: Regression check for boundary validation on credentials")
    public void testCredentialInputBoundaries() {
        logger.info("Testing input field boundary behavior");
        loginPage.enterUsername("   ")
                 .enterPassword("   ")
                 .clickLoginButton();

        Assert.assertTrue(loginPage.isLoginPageLoaded(), "Whitespace credentials unexpectedly bypassed login validation.");
    }

    @Test(groups = {"regression"}, description = "REG_003: Regression validation of security boundaries on deep kiosk routes")
    public void testDeepRouteSecurityGuards() {
        String baseUrl = ConfigReader.getProperty("baseUrl", "https://menu.albertapayments.com/");
        String[] protectedRoutes = {
                "kiosk/menu-departments",
                "kiosk/menu-categories",
                "kiosk/menu-items-list",
                "kiosk/modifier-groups",
                "kiosk/settings"
        };

        for (String route : protectedRoutes) {
            String target = baseUrl.endsWith("/") ? baseUrl + route : baseUrl + "/" + route;
            logger.info("Checking auth guard on route: {}", target);
            getPage().navigate(target, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            boolean isLoaded = loginPage.isLoginPageLoaded();
            Assert.assertTrue(isLoaded, "Protected route '" + route + "' failed to redirect unauthenticated user.");
        }
    }
}
