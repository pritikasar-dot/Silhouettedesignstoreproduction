package com.mystore.page;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import com.mystore.base.BaseClass;

public class MyAccount extends BaseClass{
	  WebDriver driver;

	    // User info container
	    @FindBy(css = "div#user-logged-in span.user-name.bs")
	    private WebElement loggedInUserName;

	    @FindBy(css = "div#user-logged-in a.user-link-dropdown.lm")
	    private WebElement accountLink;

	    // Constructor
	    public MyAccount(WebDriver driver) {
	        this.driver = driver;
	        PageFactory.initElements(driver, this);
	    }

	    // Actions
	   public boolean isUserLoggedIn() {
    WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(15));

    try {
        // ✅ Wait for login to complete (page load or redirect)
        wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));

        // ✅ Try accessing account page (REAL validation)
        getDriver().navigate().to(
                "https://www.silhouettedesignstore.com/customer/account/"
        );

        // ✅ If still on account page → logged in
        return wait.until(ExpectedConditions.urlContains("/customer/account/"));

    } catch (Exception e) {
        return false;
    }
}

	    public String getLoggedInUserName() {
	        return loggedInUserName.getText();
	    }

	    public boolean isAccountLinkDisplayed() {
	        return accountLink.isDisplayed();
	    }
	}

