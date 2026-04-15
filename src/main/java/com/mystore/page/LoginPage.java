package com.mystore.page;

import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.support.*;
import org.openqa.selenium.support.ui.*;

import com.mystore.base.BaseClass;

public class LoginPage extends BaseClass implements LoginAble {

    WebDriverWait wait;

    public LoginPage() {
        PageFactory.initElements(getDriver(), this);
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(15));
    }

    @FindBy(id = "email")
    private WebElement emailField;

    @FindBy(id = "pass")
    private WebElement passwordField;

    @FindBy(id = "send2")
    private WebElement signInButton;

    @FindBy(xpath = "//strong[@id='block-customer-login-heading']")
    private WebElement accountLink;

    private By loginErrorMessageBy = By.xpath("//div[@data-ui-id='message-error']");

    // ===== UTIL =====
    private void waitAndSendKeys(WebElement element, String value) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(value);
    }

   private void waitAndClick(WebElement element) {
    wait.until(ExpectedConditions.elementToBeClickable(element));

    ((JavascriptExecutor) getDriver())
            .executeScript("arguments[0].scrollIntoView(true);", element);

    try {
        element.click();
    } catch (Exception e) {
        // Fallback for headless
        ((JavascriptExecutor) getDriver())
                .executeScript("arguments[0].click();", element);
    }
}

    // ===== ACTIONS =====
    @Override
    public void enterEmail(String email) {
        waitAndSendKeys(emailField, email);
    }

    @Override
    public void enterPassword(String pwd) {
        waitAndSendKeys(passwordField, pwd);
    }

    @Override
    public void clickSignIn() {
        waitAndClick(signInButton);
    }

    // ===== VALIDATIONS =====
    @Override
    public boolean isUserLoggedIn() {
        try {
            wait.until(ExpectedConditions.visibilityOf(accountLink));
            return accountLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isSignInButtonDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(signInButton)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isErrorDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(loginErrorMessageBy));
            return getDriver().findElement(loginErrorMessageBy).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getErrorMessageText() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(loginErrorMessageBy))
                    .getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean waitForErrorDisplayed(int timeoutSeconds) {
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(loginErrorMessageBy));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}