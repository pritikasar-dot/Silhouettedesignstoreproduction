package com.mystore.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;
import java.io.File;
import io.github.bonigarcia.wdm.WebDriverManager;

public class BaseClass {

    public static Properties prop;
    public static ThreadLocal<RemoteWebDriver> driver = new ThreadLocal<>();

    // ==============================
    // SUITE SETUP
    // ==============================
    @BeforeSuite(alwaysRun = true)
    public void setupSuite() {
        DOMConfigurator.configure("log4j.xml");
        loadConfig();
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    // ==============================
    // LOAD CONFIG
    // ==============================
    public void loadConfig() {
        try {
            if (prop == null) {
                prop = new Properties();
                FileInputStream ip = new FileInputStream(
                        System.getProperty("user.dir") + "/Configuration/Config.properties");
                prop.load(ip);
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load config file", e);
        }
    }

    // ==============================
    // LAUNCH APPLICATION
    // ==============================
    public void launchApp() {

        String browser = prop.getProperty("browser").toLowerCase();
        int pageLoadTimeout = Integer.parseInt(prop.getProperty("page.load.timeout"));

        boolean isHeadless = browser.contains("headless");

        switch (browser) {

            case "chrome":
            case "chrome-headless":
                WebDriverManager.chromedriver().setup();
                driver.set(new ChromeDriver(getChromeOptions(isHeadless)));
                break;

            case "firefox":
            case "firefox-headless":
                WebDriverManager.firefoxdriver().setup();
                driver.set(new FirefoxDriver(getFirefoxOptions(isHeadless)));
                break;

            case "edge":
            case "edge-headless":
                WebDriverManager.edgedriver().setup();
                driver.set(new EdgeDriver(getEdgeOptions(isHeadless)));
                break;

            default:
                throw new RuntimeException("❌ Unsupported browser: " + browser);
        }

        WebDriver drv = getDriver();

        // ===== 🔥 CRITICAL: FORCE DESKTOP VIEW =====
drv.manage().window().setPosition(new Point(0, 0));
drv.manage().window().setSize(new Dimension(1920, 1080));        // ===== Timeouts (NO implicit wait - avoids flakiness) =====
        drv.manage().timeouts().implicitlyWait(Duration.ZERO);
        drv.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        // ===== Navigate (with retry) =====
        String url = prop.getProperty("url");
        loadUrlWithRetry(drv, url);

        // ===== Handle cookies =====
        handleCookieConsent();
    }

    // ==============================
    // RETRY NAVIGATION (VERY IMPORTANT)
    // ==============================
    private void loadUrlWithRetry(WebDriver driver, String url) {
        try {
            driver.get(url);
        } catch (Exception e) {
            System.out.println("⚠️ Retry loading URL...");
            driver.get(url);
        }
    }

    // ==============================
    // CHROME OPTIONS (FINAL)
    // ==============================
    private ChromeOptions getChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        if (headless) {
            options.addArguments("--headless=new");

            // 🔥 CRITICAL FIX (mobile issue)
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--force-device-scale-factor=1");

            // 🔥 Force desktop user-agent
            options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
            );
        }

        return options;
    }

    // ==============================
    private FirefoxOptions getFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("--headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
        }

        return options;
    }

    // ==============================
    private EdgeOptions getEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        return options;
    }

    // ==============================
    // COOKIE HANDLER
    // ==============================
    private void handleCookieConsent() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(5));
            By cookieBtn = By.xpath("//button[normalize-space()='I agree']");

            if (!getDriver().findElements(cookieBtn).isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(cookieBtn)).click();
                System.out.println("✅ Cookie accepted");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ No cookie popup");
        }
    }

    // ==============================
    // SCREENSHOT (DEBUG)
    // ==============================
    public void takeScreenshot(String name) {
        try {
            File src = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
            java.nio.file.Files.copy(src.toPath(),
                    java.nio.file.Paths.get("screenshots/" + name + ".png"));
        } catch (Exception ignored) {}
    }

    // ==============================
    // TEARDOWN
    // ==============================
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
        }
    }
}