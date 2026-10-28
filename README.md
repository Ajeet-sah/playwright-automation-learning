# Alberta Payments Kiosk - Enterprise Playwright Java Automation Framework

[![Java 17](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Playwright](https://img.shields.io/badge/Playwright-Java%201.49.0-green.svg)](https://playwright.dev/java/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-orange.svg)](https://testng.org/)
[![ExtentReports](https://img.shields.io/badge/ExtentReports-5.1.2-purple.svg)](https://extentreports.com/)
[![Log4j2](https://img.shields.io/badge/Log4j2-2.24.1-red.svg)](https://logging.apache.org/log4j/2.x/)

A scalable, maintainable, thread-safe test automation framework designed for the **Alberta Payments Kiosk Backoffice** React application ([https://menu.albertapayments.com/](https://menu.albertapayments.com/)).

---

## 🏗️ Framework Architecture

```
Alberta-Playwright-Automation/
├── src/main/java/com/albertapayments/
│   ├── base/
│   │   └── BasePage.java              # Reusable Playwright element wrappers with auto-waiting
│   ├── factory/
│   │   └── PlaywrightFactory.java     # ThreadLocal Playwright, Browser, Context, and Page management
│   ├── pages/
│   │   ├── LoginPage.java             # Login page actions and accessible locators
│   │   ├── DashboardPage.java         # Shell navigation, drawer, store selector, and logout
│   │   └── MenuPage.java              # Menu items, search, filters, and cards
│   ├── utilities/
│   │   ├── ConfigReader.java          # Thread-safe properties loader
│   │   ├── TestDataReader.java        # JSON test data provider
│   │   ├── ScreenshotUtil.java        # Failure and on-demand screenshot utility
│   │   └── WaitUtil.java              # Explicit wait utilities and assertions
│   └── reporting/
│       └── ExtentReportManager.java   # ExtentReports 5 initialization and thread-safe test nodes
│
├── src/test/java/com/albertapayments/
│   ├── base/
│   │   └── BaseTest.java              # TestNG lifecycle hooks (@BeforeMethod, @AfterMethod)
│   ├── tests/
│   │   ├── LoginTest.java             # Authentication test suite
│   │   ├── DashboardTest.java         # Shell navigation and dashboard test suite
│   │   ├── MenuTest.java              # Menu items and categories test suite
│   │   └── RegressionTest.java        # Comprehensive end-to-end regression suite
│   └── listeners/
│       └── TestListener.java          # TestNG listener for logging, reports, screenshots, and traces
│
├── src/test/resources/
│   ├── config.properties              # Centralized environment and browser configuration
│   ├── testdata/
│   │   └── testData.json              # Structured test datasets
│   ├── testng.xml                     # TestNG suite configuration with parallel threads
│   └── log4j2.xml                     # Log4j2 console and file logging setup
│
├── screenshots/                       # Captured screenshots on test failures
├── reports/                           # Interactive Extent HTML execution reports
├── test-results/                      # Playwright trace files (.zip) and recordings
├── pom.xml                            # Maven dependencies and Surefire plugin
└── README.md
```

---

## 🚀 Prerequisites

1. **Java JDK 17+** (JDK 17 or higher LTS).
2. **Apache Maven 3.8+**.
3. **Browsers:** Playwright automatically manages browser binaries (Chromium, Firefox, WebKit).

---

## 💻 Execution Commands

### 1. Run All Tests
```bash
mvn clean test
```

### 2. Run by TestNG Group
```bash
# Smoke Tests
mvn clean test -Dgroups=smoke

# Regression Tests
mvn clean test -Dgroups=regression

# Sanity Tests
mvn clean test -Dgroups=sanity

# Functional Tests
mvn clean test -Dgroups=functional
```

### 3. Cross-Browser Execution
```bash
# Run on Google Chrome / Chromium
mvn clean test -Dbrowser=chromium

# Run on Mozilla Firefox
mvn clean test -Dbrowser=firefox

# Run on WebKit (Safari engine)
mvn clean test -Dbrowser=webkit
```

### 4. Headed vs. Headless Execution
```bash
# Headed mode (browser UI visible)
mvn clean test -Dheadless=false

# Headless mode (fast background execution)
mvn clean test -Dheadless=true
```

---

## 📊 Reports and Artifacts

- **Extent Reports:** Generated in `reports/index.html` after test runs.
- **Screenshots:** Automatically saved to `screenshots/` upon test failure.
- **Playwright Traces:** Saved to `test-results/traces/` on test failure. View traces with:
  ```bash
  mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="show-trace test-results/traces/<test-trace>.zip"
  ```
