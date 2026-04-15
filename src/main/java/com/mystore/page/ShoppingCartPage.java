package com.mystore.page;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ShoppingCartPage {

    WebDriver driver;
    WebDriverWait wait;

    // Locators
    private By clearCartBtn = By.xpath("//span[normalize-space()='Clear cart']");
    private By emptyCartMsg1 = By.cssSelector("div.cart-empty p:nth-child(1)");
    private By emptyCartMsg2 = By.xpath("//p[normalize-space()='You have no items in your shopping cart.']");

    public ShoppingCartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void navigateToCart() {
driver.navigate().to("https://www.silhouettedesignstore.com/checkout/cart/");

    new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(driver -> ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").equals("complete"));    }

    public boolean isCartEmpty() {
        return driver.findElements(emptyCartMsg1).size() > 0 ||
               driver.findElements(emptyCartMsg2).size() > 0;
    }

    public void clearCartIfNotEmpty() {
    try {
        if (!isCartEmpty()) {

            System.out.println("🛒 Cart has items. Clearing cart...");

            WebElement clearBtn = wait.until(ExpectedConditions.presenceOfElementLocated(clearCartBtn));

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView(true);", clearBtn);

            wait.until(ExpectedConditions.elementToBeClickable(clearBtn)).click();

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(emptyCartMsg1),
                    ExpectedConditions.visibilityOfElementLocated(emptyCartMsg2)
            ));

            System.out.println("✅ Cart cleared successfully.");
        } else {
            System.out.println("ℹ️ Cart already empty.");
        }

    } catch (Exception e) {
        System.out.println("⚠️ Failed to clear cart: " + e.getMessage());
        throw e;
    }
}
}