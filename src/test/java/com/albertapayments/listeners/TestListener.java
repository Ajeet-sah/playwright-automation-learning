package com.albertapayments.listeners;

import com.albertapayments.factory.PlaywrightFactory;
import com.albertapayments.reporting.ExtentReportManager;
import com.albertapayments.utilities.ConfigReader;
import com.albertapayments.utilities.ScreenshotUtil;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;

/**
 * TestListener implements TestNG's ITestListener and ISuiteListener to capture
 * execution metrics, generate interactive ExtentReports, maintain execution logs via Log4j2,
 * and attach failure screenshots and browser context metadata.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);
    private static ExtentReports extentReports;

    @Override
    public void onStart(ISuite suite) {
        logger.info("===============================================================");
        logger.info("STARTING TEST SUITE: [{}]", suite.getName());
        logger.info("===============================================================");
        extentReports = ExtentReportManager.initReports();
    }

    @Override
    public void onFinish(ISuite suite) {
        logger.info("===============================================================");
        logger.info("FINISHED TEST SUITE: [{}]", suite.getName());
        logger.info("===============================================================");
        ExtentReportManager.flushReports();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        String[] groups = result.getMethod().getGroups();
        String browser = ConfigReader.getProperty("browser", "chromium");
        boolean isHeadless = ConfigReader.getBooleanProperty("headless", true);

        logger.info(">>> TEST STARTED: [{}] | Browser: [{}] | Headless: [{}] | Thread: [{}] <<<",
                testName, browser, isHeadless, Thread.currentThread().getId());

        if (extentReports != null) {
            ExtentTest test = extentReports.createTest(testName, description);
            
            // Assign categories based on TestNG groups
            for (String group : groups) {
                test.assignCategory(group);
            }
            
            // Log test context details
            test.assignDevice(browser.toUpperCase() + (isHeadless ? " (Headless)" : " (Headed)"));
            test.log(Status.INFO, "Execution Details: Browser = " + browser + " | Base URL = " + ConfigReader.getProperty("baseUrl"));
            
            ExtentReportManager.setTest(test);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.info(">>> TEST PASSED: [{}] (Duration: {}ms) <<<", testName, duration);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, MarkupHelper.createLabel(testName + " PASSED in " + duration + "ms", ExtentColor.GREEN));
            ExtentReportManager.removeTest();
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        Throwable throwable = result.getThrowable();
        String errorMessage = throwable != null ? throwable.getMessage() : "Unknown failure";

        logger.error(">>> TEST FAILED: [{}] (Duration: {}ms) - Cause: {} <<<", testName, duration, errorMessage);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, MarkupHelper.createLabel(testName + " FAILED in " + duration + "ms", ExtentColor.RED));
            if (throwable != null) {
                test.fail(throwable);
            }

            // Capture and embed Base64 screenshot into Extent Report
            Page page = PlaywrightFactory.getPage();
            if (page != null) {
                try {
                    String base64Screenshot = ScreenshotUtil.captureBase64Screenshot(page);
                    if (base64Screenshot != null && !base64Screenshot.isEmpty()) {
                        test.fail("Failure Screenshot View:",
                                MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
                        logger.info("Base64 failure screenshot successfully embedded into Extent Report for: {}", testName);
                    }
                } catch (Exception e) {
                    logger.error("Failed to attach Base64 screenshot: {}", e.getMessage());
                }
            }
            ExtentReportManager.removeTest();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable cause = result.getThrowable();
        String reason = cause != null ? cause.getMessage() : "Prerequisite or dependency skipped";

        logger.warn(">>> TEST SKIPPED: [{}] - Reason: {} <<<", testName, reason);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, MarkupHelper.createLabel(testName + " SKIPPED: " + reason, ExtentColor.ORANGE));
            ExtentReportManager.removeTest();
        }
    }
}
