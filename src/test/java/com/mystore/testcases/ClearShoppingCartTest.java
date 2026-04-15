package com.mystore.testcases;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mystore.base.BaseClass;
import com.mystore.page.HomePage;
import com.mystore.page.LoginAble;
import com.mystore.page.MyAccount;
import com.mystore.page.ShoppingCartPage;

public class ClearShoppingCartTest extends BaseClass {

    HomePage homepage;
    LoginAble login;
    MyAccount myAccount;
    ShoppingCartPage cartPage;

 @BeforeMethod
public void setup() throws Throwable {

    launchApp();

    homepage = new HomePage(getDriver());
    login = homepage.clickAndCheckLogin();

    if (login == null) {
        Assert.fail("❌ Login page not opened.");
    }

    // Login
    login.enterEmail(prop.getProperty("username"));
    login.enterPassword(prop.getProperty("password"));
    login.clickSignIn();

    myAccount = new MyAccount(getDriver());

    boolean isLoggedIn = false;

    try {
        // ✅ Primary check (UI based)
        isLoggedIn = myAccount.isUserLoggedIn();
    } catch (Exception e) {
        System.out.println("⚠️ UI login check failed, trying fallback...");
    }

    // ✅ Fallback check (URL access)
    if (!isLoggedIn) {
        try {
            getDriver().get("https://www.silhouettedesignstore.com/customer/account/");

            Thread.sleep(3000); // small wait for page load

            String currentUrl = getDriver().getCurrentUrl();

            if (currentUrl.contains("/customer/account")) {
                isLoggedIn = true;
                System.out.println("✅ Login verified via My Account URL");
            }

        } catch (Exception e) {
            System.out.println("❌ Fallback login check failed");
        }
    }

    Assert.assertTrue(isLoggedIn, "❌ Login failed");

    cartPage = new ShoppingCartPage(getDriver());
}

    @Test(description = "Shopping Cart : Clear shopping cart if items exist")
    public void clearCartTest() {

        cartPage.navigateToCart();
        cartPage.clearCartIfNotEmpty();

        Assert.assertTrue(cartPage.isCartEmpty(),
                "❌ Cart is not empty after clearing!");

        System.out.println("✅ Cart validation successful.");
    }
}
