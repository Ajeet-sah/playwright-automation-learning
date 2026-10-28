package com.albertapayments.reporting;

import com.albertapayments.utilities.ConfigReader;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * ExtentReportManager manages ExtentReports 5 initialization and maintains
 * thread-safe ExtentTest instances using ThreadLocal for parallel test reporting.
 */
public class ExtentReportManager {

    private static final Logger logger = LogManager.getLogger(ExtentReportManager.class);
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();
    private static final String REPORT_PATH = "reports/index.html";

    private ExtentReportManager() {
        // Prevent direct instantiation
    }

    /**
     * Initializes the ExtentReports instance with Spark Reporter configuration.
     *
     * @return ExtentReports singleton instance.
     */
    public static synchronized ExtentReports initReports() {
        if (extentReports == null) {
            new File("reports").mkdirs();
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(REPORT_PATH);
            
            sparkReporter.config().setDocumentTitle("Alberta Payments - Test Execution Report");
            sparkReporter.config().setReportName("Kiosk Backoffice Automation Execution");
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);

            // System environment information
            extentReports.setSystemInfo("Application Name", "Alberta Payments Kiosk");
            extentReports.setSystemInfo("Environment", ConfigReader.getProperty("environment", "PROD"));
            extentReports.setSystemInfo("Operating System", System.getProperty("os.name"));
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("Target Browser", ConfigReader.getProperty("browser", "chromium"));

            logger.info("Extent Reports initialized successfully at: {}", REPORT_PATH);
        }
        return extentReports;
    }

    /**
     * @return Thread-safe ExtentTest instance for the active test thread.
     */
    public static ExtentTest getTest() {
        return extentTestThreadLocal.get();
    }

    /**
     * Sets the ExtentTest instance for the active test thread.
     *
     * @param test ExtentTest instance.
     */
    public static void setTest(ExtentTest test) {
        extentTestThreadLocal.set(test);
    }

    /**
     * Clears the ExtentTest ThreadLocal to prevent memory leaks.
     */
    public static void removeTest() {
        extentTestThreadLocal.remove();
    }

    /**
     * Flushes the reports and writes output to disk.
     */
    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("Extent Reports flushed and written to: {}", REPORT_PATH);
        }
    }
}
