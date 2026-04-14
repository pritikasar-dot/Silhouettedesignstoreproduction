package com.mystore.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;

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
        int implicitWait = Integer.parseInt(prop.getProperty("implicit.wait"));
        int pageLoadTimeout = Integer.parseInt(prop.getProperty("page.load.timeout"));

        switch (browser) {

            case "chrome":
            case "chrome-headless":
                WebDriverManager.chromedriver().setup();
                driver.set(new ChromeDriver(getChromeOptions(browser.contains("headless"))));
                break;

            case "firefox":
            case "firefox-headless":
                WebDriverManager.firefoxdriver().setup();
                driver.set(new FirefoxDriver(getFirefoxOptions(browser.contains("headless"))));
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                driver.set(new EdgeDriver(getEdgeOptions(false)));
                break;

            default:
                throw new RuntimeException("❌ Unsupported browser: " + browser);
        }

        WebDriver drv = getDriver();

        drv.manage().window().maximize();
        drv.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        drv.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        // Navigate
        drv.get(prop.getProperty("url"));

        // Handle cookies
        handleCookieConsent();
    }

    // ==============================
    // BROWSER OPTIONS
    // ==============================

    private ChromeOptions getChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        // Stability flags (VERY IMPORTANT)
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        return options;
    }

    private FirefoxOptions getFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("--headless");
        }

        return options;
    }

    private EdgeOptions getEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();

        if (headless) {
            options.addArguments("--headless=new");
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
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(cookieBtn));
                element.click();
                System.out.println("✅ Cookie accepted");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Cookie popup not present");
        }
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