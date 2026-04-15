package com.mystore.testcases;

import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mystore.base.BaseClass;
import com.mystore.page.NewDesignsPage;
import com.mystore.utility.ProductContext;


public class FetchProductLinksTest extends BaseClass {

   @Test(description = "Fetch product links and store for email")
public void fetchProductLinks() {

    launchApp();
    getDriver().get("https://www.silhouettedesignstore.com/new.html");

    NewDesignsPage page = new NewDesignsPage();

    List<String> links = page.getAllProductLinks();

    Assert.assertTrue(links.size() > 0, "❌ No product links found!");

    // ✅ Store links for email
    ProductContext.addLinks(links);

    System.out.println("✅ Stored " + links.size() + " product links.");
}
    
}