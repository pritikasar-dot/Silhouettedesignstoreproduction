package com.mystore.testcases;

import org.testng.Assert;
import org.testng.annotations.*;

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

        MyAccount myAccount = new MyAccount(getDriver());

        Assert.assertTrue(myAccount.isUserLoggedIn(),
                "❌ Login failed: User not logged in.");

        System.out.println("✅ Login successful.");
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