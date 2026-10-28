package com.albertapayments.base;

import com.albertapayments.factory.PlaywrightFactory;
import com.albertapayments.utilities.ConfigReader;
import com.albertapayments.utilities.ScreenshotUtil;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * BaseTest serves as the foundational test lifecycle manager for all TestNG test classes.
 * Manages suite initialization, isolated browser setup per test method, screenshot capture,
 * trace recording on failure, and clean browser teardown.
 */
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected static Properties prop;
    protected PlaywrightFactory playwrightFactory;
    protected Page page;

    @BeforeSuite(alwaysRun = true)
    public void setupSuite() {
        logger.info("================ STARTING TEST SUITE EXECUTION ================");
        prop = ConfigReader.initProperties();

        // Ensure artifact directories exist
        new File("screenshots").mkdirs();
        new File("reports").mkdirs();
        new File("test-results/traces").mkdirs();
        new File("test-results/videos").mkdirs();
    }

    @BeforeMethod(alwaysRun = true)
    public void setupTest() {
        logger.info("Initializing browser session for test thread: {}", Thread.currentThread().getId());
        playwrightFactory = new PlaywrightFactory();
        page = playwrightFactory.initBrowser(prop);

        String baseUrl = ConfigReader.getProperty("baseUrl", "https://menu.albertapayments.com/");
        logger.info("Navigating to application base URL: {}", baseUrl);
        page.navigate(baseUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownTest(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        BrowserContext context = PlaywrightFactory.getBrowserContext();
        Path videoPath = null;

        // Capture video path before page closes if video recording was enabled
        if (page != null && page.video() != null) {
            try {
                videoPath = page.video().path();
            } catch (Exception ignored) {}
        }

        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("TEST FAILED: [{}]. Capturing diagnostics...", testName);

            // 1. Capture Failure Screenshot
            if (ConfigReader.getBooleanProperty("screenshotOnFailure", true)) {
                String screenshotPath = ScreenshotUtil.captureScreenshot(page, testName);
                result.setAttribute("screenshotPath", screenshotPath);
            }

            // 2. Stop and Save Trace File on Failure
            if (ConfigReader.getBooleanProperty("traceOnFailure", true) && context != null) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String tracePath = Paths.get("test-results", "traces", testName + "_" + timestamp + "_trace.zip")
                        .toAbsolutePath().toString();
                PlaywrightFactory.stopTracing(context, tracePath);
                result.setAttribute("tracePath", tracePath);
            }

            // 3. Keep video on failure
            if (videoPath != null) {
                result.setAttribute("videoPath", videoPath.toString());
                logger.info("Failure video recording preserved at: {}", videoPath);
            }
        } else {
            // For passed or skipped tests: discard trace buffer to save disk space
            if (context != null) {
                PlaywrightFactory.discardTracing(context);
            }

            // Clean up unnecessary video files on success unless configured to keep all
            boolean keepAllVideos = ConfigReader.getBooleanProperty("keepAllVideos", false);
            if (!keepAllVideos && videoPath != null) {
                try {
                    Files.deleteIfExists(videoPath);
                } catch (Exception ignored) {}
            }
        }

        // Clean teardown of Browser, Context, Page, and Playwright
        PlaywrightFactory.closeBrowser();
        logger.info("Browser session closed for test: [{}]", testName);
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        logger.info("================ COMPLETED TEST SUITE EXECUTION ================");
    }

    /**
     * @return Active Playwright Page instance for current thread.
     */
    public Page getPage() {
        return PlaywrightFactory.getPage();
    }
}
