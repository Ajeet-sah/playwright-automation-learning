package com.albertapayments.utilities;

import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing test execution screenshots.
 */
public class ScreenshotUtil {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_DIR = "screenshots";

    private ScreenshotUtil() {
        // Prevent direct instantiation
    }

    /**
     * Captures a full-page screenshot and saves it to the screenshots directory.
     *
     * @param page     Active Playwright Page instance.
     * @param testName Name of the running test method.
     * @return Absolute file path of the saved screenshot.
     */
    public static String captureScreenshot(Page page, String testName) {
        if (page == null) {
            logger.warn("Cannot capture screenshot because Page instance is null.");
            return "";
        }

        try {
            File directory = new File(SCREENSHOT_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9_.-]", "_");
            String fileName = sanitizedTestName + "_" + timestamp + ".png";
            String fullPath = Paths.get(SCREENSHOT_DIR, fileName).toAbsolutePath().toString();

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(fullPath))
                    .setFullPage(true));

            logger.info("Screenshot successfully captured for test '{}' at: {}", testName, fullPath);
            return fullPath;
        } catch (Exception e) {
            logger.error("Failed to capture screenshot for test '{}': {}", testName, e.getMessage());
            return "";
        }
    }

    /**
     * Captures screenshot as base64 string for direct embedding in Extent Reports.
     *
     * @param page Active Playwright Page instance.
     * @return Base64 encoded screenshot string.
     */
    public static String captureBase64Screenshot(Page page) {
        if (page == null) {
            return "";
        }
        try {
            byte[] buffer = page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
            return java.util.Base64.getEncoder().encodeToString(buffer);
        } catch (Exception e) {
            logger.error("Failed to capture base64 screenshot: {}", e.getMessage());
            return "";
        }
    }
}
