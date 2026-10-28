package com.albertapayments.pages;

import com.albertapayments.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * LoginPage encapsulates locators and user interactions on the Alberta Payments
 * Kiosk Backoffice login page (https://menu.albertapayments.com/).
 */
public class LoginPage extends BasePage {

    // Locators defined using accessible and verified selectors
    private final Locator logo;
    private final Locator formTitle;
    private final Locator subtitle;
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator passwordToggleIcon;
    private final Locator loginButton;
    private final Locator errorTextList;
    private final Locator customerServiceInfo;
    private final Locator toastNotification;

    public LoginPage(Page page) {
        super(page);
        this.logo = page.locator("img[alt='Alberta Logo'], div.login-logo img, img[src*='alberta-logo']");
        this.formTitle = page.locator("h3.form-title");
        this.subtitle = page.locator("p.subtitle");
        this.usernameInput = page.locator("input[name='username']");
        this.passwordInput = page.locator("input[name='password']");
        this.passwordToggleIcon = page.locator("img.right-icon, img[alt='show password'], img[alt='hide password']");
        this.loginButton = page.locator("button.login-btn");
        this.errorTextList = page.locator("p.error-text");
        this.customerServiceInfo = page.locator("footer.support");
        this.toastNotification = page.locator(".Toastify__toast-body, [role='alert'], .alert-danger");
    }

    /**
     * Checks if the login page is loaded by waiting for the form title to become visible.
     *
     * @return true if form title and login button are visible.
     */
    public boolean isLoginPageLoaded() {
        try {
            waitForVisible(formTitle);
            return isVisible(formTitle) && isVisible(loginButton);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return Form title text (e.g. "Log in").
     */
    public String getFormTitle() {
        waitForVisible(formTitle);
        return getText(formTitle);
    }

    /**
     * @return Subtitle text (e.g. "Please log in to continue").
     */
    public String getSubtitle() {
        waitForVisible(subtitle);
        return getText(subtitle);
    }

    /**
     * Checks if the Alberta brand logo is visible.
     *
     * @return true if visible.
     */
    public boolean isLogoVisible() {
        try {
            waitForVisible(logo);
            return isVisible(logo);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Enters username / email into the input field.
     *
     * @param username Email or username string.
     * @return Current LoginPage instance for fluent chaining.
     */
    public LoginPage enterUsername(String username) {
        logger.info("Entering username: {}", username);
        waitForVisible(usernameInput);
        fill(usernameInput, username);
        return this;
    }

    /**
     * Enters password into the input field.
     *
     * @param password Password string.
     * @return Current LoginPage instance for fluent chaining.
     */
    public LoginPage enterPassword(String password) {
        logger.info("Entering password");
        waitForVisible(passwordInput);
        fill(passwordInput, password);
        return this;
    }

    /**
     * Clicks the Log in button.
     */
    public void clickLoginButton() {
        logger.info("Clicking Log in button");
        waitForVisible(loginButton);
        click(loginButton);
    }

    /**
     * Toggles password visibility by clicking the eye icon.
     *
     * @return Current LoginPage instance.
     */
    public LoginPage togglePasswordVisibility() {
        logger.info("Toggling password visibility");
        waitForVisible(passwordToggleIcon);
        click(passwordToggleIcon);
        return this;
    }

    /**
     * Checks if password input type is currently "text" (visible) or "password" (masked).
     *
     * @return true if visible text, false if masked.
     */
    public boolean isPasswordVisible() {
        String type = getAttribute(passwordInput, "type");
        return "text".equalsIgnoreCase(type);
    }

    /**
     * Performs complete login action.
     *
     * @param username Email or username.
     * @param password User password.
     * @return DashboardPage object representing post-login dashboard.
     */
    public DashboardPage doLogin(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        return new DashboardPage(page);
    }

    /**
     * Retrieves client-side validation error message for a specific input field.
     *
     * @return First visible validation error text, or empty string if none.
     */
    public String getFieldValidationError() {
        try {
            waitForVisible(errorTextList);
            return getText(errorTextList.first());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Retrieves server-side toast error message (e.g. "Invalid credentials").
     *
     * @return Toast message text, or empty string.
     */
    public String getToastErrorMessage() {
        try {
            waitForVisible(toastNotification);
            return getText(toastNotification);
        } catch (Exception e) {
            logger.debug("No toast error notification appeared: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Checks if customer support footer info is visible.
     *
     * @return true if visible.
     */
    public boolean isCustomerServiceInfoVisible() {
        try {
            waitForVisible(customerServiceInfo);
            return isVisible(customerServiceInfo);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return Customer support footer text.
     */
    public String getCustomerServiceText() {
        waitForVisible(customerServiceInfo);
        return getText(customerServiceInfo);
    }
}
