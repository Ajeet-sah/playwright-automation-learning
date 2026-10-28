package com.albertapayments.factory;

import com.albertapayments.utilities.ConfigReader;
import com.albertapayments.utilities.ScreenshotUtil;
import com.microsoft.playwright.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/**
 * PlaywrightFactory manages the lifecycle and initialization of Playwright,
 * Browser, BrowserContext, and Page instances using ThreadLocal variables
 * to guarantee thread safety during parallel TestNG test execution.
 */
public class PlaywrightFactory {

    private static final Logger logger = LogManager.getLogger(PlaywrightFactory.class);

    private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlBrowserContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();

    /**
     * @return Thread-safe Playwright instance for current test thread.
     */
    public static Playwright getPlaywright() {
        return tlPlaywright.get();
    }

    /**
     * @return Thread-safe Browser instance for current test thread.
     */
    public static Browser getBrowser() {
        return tlBrowser.get();
    }

    /**
     * @return Thread-safe BrowserContext instance for current test thread.
     */
    public static BrowserContext getBrowserContext() {
        return tlBrowserContext.get();
    }

    /**
     * @return Thread-safe Page instance for current test thread.
     */
    public static Page getPage() {
        return tlPage.get();
    }

    /**
     * Initializes the Playwright browser, isolated context, and page.
     * Respects system property overrides (-Dbrowser=firefox, -Dheadless=false).
     *
     * @param prop Configuration properties.
     * @return Initialized Page instance ready for navigation.
     */
    public Page initBrowser(Properties prop) {
        String browserName = ConfigReader.getProperty("browser", "chromium").toLowerCase().trim();
        boolean isHeadless = ConfigReader.getBooleanProperty("headless", true);
        int timeout = ConfigReader.getIntProperty("timeout", 30000);

        logger.info("Initializing Playwright with Browser: [{}], Headless: [{}], Timeout: [{}ms]",
                browserName, isHeadless, timeout);

        // Create Playwright engine instance for current thread
        Playwright playwright = Playwright.create();
        tlPlaywright.set(playwright);

        Browser browser;
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setArgs(Arrays.asList(
                        "--start-maximized",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu"
                ));

        switch (browserName) {
            case "chromium":
                browser = playwright.chromium().launch(launchOptions);
                break;
            case "chrome":
                browser = playwright.chromium().launch(launchOptions.setChannel("chrome"));
                break;
            case "msedge":
            case "edge":
                browser = playwright.chromium().launch(launchOptions.setChannel("msedge"));
                break;
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
                browser = playwright.webkit().launch(launchOptions);
                break;
            default:
                logger.warn("Unsupported browser '{}' requested. Defaulting to Chromium.", browserName);
                browser = playwright.chromium().launch(launchOptions);
                break;
        }

        tlBrowser.set(browser);

        // Create a completely isolated browser context per test to avoid sharing state
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setIgnoreHTTPSErrors(true);

        // Configure video recording if enabled
        if (ConfigReader.getBooleanProperty("videoRecording", false)) {
            contextOptions.setRecordVideoDir(Paths.get("test-results", "videos"))
                          .setRecordVideoSize(1280, 720);
            logger.info("Video recording enabled. Videos will be saved to test-results/videos");
        }

        BrowserContext context = browser.newContext(contextOptions);
        context.setDefaultTimeout(timeout);
        context.setDefaultNavigationTimeout(timeout);
        tlBrowserContext.set(context);

        // Start Playwright Tracing if enabled
        if (ConfigReader.getBooleanProperty("traceOnFailure", true)) {
            startTracing(context);
        }

        // Create isolated page instance
        Page page = context.newPage();
        tlPage.set(page);

        logger.info("Browser initialized successfully for thread: {}", Thread.currentThread().getId());
        return page;
    }

    /**
     * Starts Playwright tracing on the given context.
     *
     * @param context Active BrowserContext.
     */
    public static void startTracing(BrowserContext context) {
        if (context != null) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
            logger.debug("Playwright tracing started.");
        }
    }

    /**
     * Stops tracing and saves the trace file to the specified path.
     *
     * @param context   Active BrowserContext.
     * @param tracePath Destination file path (.zip).
     */
    public static void stopTracing(BrowserContext context, String tracePath) {
        if (context != null) {
            try {
                context.tracing().stop(new Tracing.StopOptions()
                        .setPath(Paths.get(tracePath)));
                logger.info("Playwright trace captured and saved to: {}", tracePath);
            } catch (Exception e) {
                logger.error("Failed to stop tracing: {}", e.getMessage());
            }
        }
    }

    /**
     * Discards tracing without saving to disk for successful tests.
     *
     * @param context Active BrowserContext.
     */
    public static void discardTracing(BrowserContext context) {
        if (context != null) {
            try {
                context.tracing().stop();
                logger.debug("Playwright trace discarded for successful test.");
            } catch (Exception e) {
                logger.debug("Tracing stop ignored: {}", e.getMessage());
            }
        }
    }

    /**
     * Captures a screenshot of the current page for the active thread.
     *
     * @param testName Name of the test.
     * @return Path to the captured screenshot.
     */
    public static String takeScreenshot(String testName) {
        return ScreenshotUtil.captureScreenshot(getPage(), testName);
    }

    /**
     * Safely closes the page, browser context, browser, and Playwright instances,
     * and clears ThreadLocal references to prevent memory leaks in thread pools.
     */
    public static void closeBrowser() {
        try {
            if (getPage() != null) {
                getPage().close();
            }
        } catch (Exception e) {
            logger.warn("Error closing Page: {}", e.getMessage());
        } finally {
            tlPage.remove();
        }

        try {
            if (getBrowserContext() != null) {
                getBrowserContext().close();
            }
        } catch (Exception e) {
            logger.warn("Error closing BrowserContext: {}", e.getMessage());
        } finally {
            tlBrowserContext.remove();
        }

        try {
            if (getBrowser() != null) {
                getBrowser().close();
            }
        } catch (Exception e) {
            logger.warn("Error closing Browser: {}", e.getMessage());
        } finally {
            tlBrowserContext.remove();
            tlBrowser.remove();
        }

        try {
            if (getPlaywright() != null) {
                getPlaywright().close();
            }
        } catch (Exception e) {
            logger.warn("Error closing Playwright: {}", e.getMessage());
        } finally {
            tlPlaywright.remove();
        }

        logger.info("Browser teardown complete and ThreadLocal state cleared for thread: {}",
                Thread.currentThread().getId());
    }
}
