package com.mystore.testcases;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.time.Duration;
import com.mystore.base.BaseClass;
import com.mystore.page.HomePage;
import com.mystore.page.LoginAble;
import com.mystore.page.MyAccount;

@Listeners(listeners.ExtentTestListener.class)
public class Login extends BaseClass {

    private HomePage homePage;
    private LoginAble login;

    @BeforeMethod
    public void setUp() throws Throwable {
        launchApp();
        homePage = new HomePage(getDriver());

        login = homePage.clickAndCheckLogin();

        if (login == null) {
            Assert.fail("❌ Neither login popup nor login page opened.");
        }
        System.out.println("Driver initialized: " + getDriver());
System.out.println("Login object: " + login);
    }

    @Test(description = "Verify SignIn button presence", priority = 1)
    public void verifySignInButtonPresence() {
        Assert.assertTrue(login.isSignInButtonDisplayed(),
                "❌ Sign In button should be displayed.");
    }
@Test(description = "Login with valid credentials", priority = 2)
public void loginWithValidCredentials() {

    String username = prop.getProperty("username");
    String password = prop.getProperty("password");

    login.enterEmail(username);
    login.enterPassword(password);
    login.clickSignIn();

    WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(15));

    // ✅ Step 1: Give time for login to complete
    wait.until(driver -> ((JavascriptExecutor) driver)
            .executeScript("return document.readyState").equals("complete"));

    // ✅ Step 2: Manually navigate to My Account page
    String accountUrl = "https://www.silhouettedesignstore.com/customer/account/";
    getDriver().navigate().to(accountUrl);

    // ✅ Step 3: Verify user is allowed to access it
    boolean isAccessible = false;

    try {
        isAccessible = wait.until(
                ExpectedConditions.urlContains("/customer/account/")
        );
    } catch (Exception e) {
        System.out.println("❌ Could not access My Account page");
    }

    System.out.println("Final URL: " + getDriver().getCurrentUrl());

    Assert.assertTrue(isAccessible,
            "❌ Login failed: User cannot access My Account page.");

    System.out.println("✅ Login successful: User can access My Account page.");
}

    @Test(description = "Login with invalid credentials", priority = 3)
    public void loginWithInvalidCredentials() {

        String username = prop.getProperty("fakeusername");
        String password = prop.getProperty("fakepassword");

        login.enterEmail(username);
        login.enterPassword(password);
        login.clickSignIn();

        Assert.assertTrue(login.waitForErrorDisplayed(10),
                "❌ Error message not displayed.");

        Assert.assertEquals(
                login.getErrorMessageText(),
                "This email is not registered with us. Please create an account to continue shopping.",
                "❌ Error message mismatch."
        );

        System.out.println("✅ Invalid login error verified.");
    }

    @AfterMethod
    public void tearDown() {
        getDriver().quit();
    }
}