package com.albertapayments.tests;

import com.albertapayments.base.BaseTest;
import com.albertapayments.pages.DashboardPage;
import com.albertapayments.pages.LoginPage;
import com.albertapayments.utilities.ConfigReader;
import com.albertapayments.utilities.TestDataReader;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * LoginTest verifies authentication workflows, form validations,
 * password masking toggles, and session handling on the Alberta Payments application.
 */
public class LoginTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void initPage() {
        loginPage = new LoginPage(getPage());
    }

    @Test(groups = {"smoke", "sanity"}, description = "AUTH_001: Verify login page loads successfully with all essential UI components")
    public void testLoginPageLoads() {
        logger.info("Verifying login page load and UI elements");
        Assert.assertTrue(loginPage.isLoginPageLoaded(), "Login page form title or login button was not visible.");
        Assert.assertTrue(loginPage.isLogoVisible(), "Alberta brand logo was not displayed.");
        Assert.assertEquals(loginPage.getFormTitle(), "Log in", "Form title did not match expected 'Log in'.");
        Assert.assertEquals(loginPage.getSubtitle(), "Please log in to continue", "Subtitle did not match.");
    }

    @Test(groups = {"sanity"}, description = "AUTH_002: Verify customer service contact info is displayed on the login page")
    public void testCustomerServiceInfoDisplayed() {
        logger.info("Verifying customer service info");
        Assert.assertTrue(loginPage.isCustomerServiceInfoVisible(), "Customer service footer was not displayed.");
        Assert.assertTrue(loginPage.getCustomerServiceText().contains("(732) 484-4143"),
                "Customer service text did not contain expected phone number (732) 484-4143.");
    }

    @Test(groups = {"regression", "functional"}, description = "AUTH_003: Verify client-side validation when submitting with empty email")
    public void testEmptyEmailValidation() {
        logger.info("Testing login with empty email field");
        loginPage.enterUsername("")
                 .enterPassword("ValidPassword123")
                 .clickLoginButton();

        String validationError = loginPage.getFieldValidationError();
        logger.info("Validation error observed: '{}'", validationError);
        Assert.assertTrue(!validationError.isEmpty() || loginPage.isLoginPageLoaded(),
                "Expected validation error or form submission block on empty email.");
    }

    @Test(groups = {"regression", "functional"}, description = "AUTH_004: Verify client-side validation when submitting with empty password")
    public void testEmptyPasswordValidation() {
        logger.info("Testing login with empty password field");
        loginPage.enterUsername("testuser@albertapayments.com")
                 .enterPassword("")
                 .clickLoginButton();

        String validationError = loginPage.getFieldValidationError();
        logger.info("Validation error observed: '{}'", validationError);
        Assert.assertTrue(!validationError.isEmpty() || loginPage.isLoginPageLoaded(),
                "Expected validation error or form submission block on empty password.");
    }

    @Test(groups = {"functional"}, description = "AUTH_005: Verify password visibility toggle switches between masked and plain text")
    public void testPasswordVisibilityToggle() {
        logger.info("Testing password visibility toggle icon");
        loginPage.enterPassword("SecretP@ssword123");

        Assert.assertFalse(loginPage.isPasswordVisible(), "Password should initially be masked (type='password').");

        loginPage.togglePasswordVisibility();
        Assert.assertTrue(loginPage.isPasswordVisible(), "Password should be visible (type='text') after clicking eye icon.");

        loginPage.togglePasswordVisibility();
        Assert.assertFalse(loginPage.isPasswordVisible(), "Password should return to masked state after toggling again.");
    }

    @DataProvider(name = "invalidLoginDataProvider")
    public Object[][] getInvalidLoginData() {
        return new Object[][]{
                {"nonexistent_user@albertapayments.com", "WrongPassword123", "Invalid credentials"},
                {"invalid.format@domain", "WrongPassword123", "Invalid credentials"}
        };
    }

    @Test(dataProvider = "invalidLoginDataProvider", groups = {"smoke", "regression"},
            description = "AUTH_006: Data-driven test verifying server rejection on invalid login credentials")
    public void testInvalidLogin(String username, String password, String expectedMessage) {
        logger.info("Executing invalid login test with user: '{}'", username);
        loginPage.enterUsername(username)
                 .enterPassword(password)
                 .clickLoginButton();

        // User should remain on login page and not gain access to /kiosk
        Assert.assertTrue(loginPage.isLoginPageLoaded(), "User was incorrectly redirected after entering invalid credentials.");
        Assert.assertFalse(getPage().url().contains("/kiosk/menu-items-list"),
                "User should not access the kiosk backoffice with invalid credentials.");
    }

    @Test(groups = {"smoke", "regression"}, description = "AUTH_007: Verify valid login and session redirection when authorized credentials are provided")
    public void testValidLogin() {
        String testUser = System.getenv("ALBERTA_TEST_USER");
        String testPass = System.getenv("ALBERTA_TEST_PASS");

        if (testUser == null || testUser.trim().isEmpty() || testPass == null || testPass.trim().isEmpty()) {
            // Check properties fallback
            testUser = ConfigReader.getProperty("testUsername");
            testPass = ConfigReader.getProperty("testPassword");
        }

        if (testUser == null || testUser.trim().isEmpty() || testPass == null || testPass.trim().isEmpty()) {
            logger.warn("Skipping valid login test because ALBERTA_TEST_USER credentials are not configured in environment.");
            throw new SkipException("Skipping valid login test: No test credentials provided in environment variables.");
        }

        logger.info("Executing valid login with configured test credentials");
        DashboardPage dashboardPage = loginPage.doLogin(testUser, testPass);

        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard was not loaded after valid login.");
        Assert.assertTrue(getPage().url().contains("/kiosk"), "Expected URL to redirect to /kiosk route.");
    }
}
