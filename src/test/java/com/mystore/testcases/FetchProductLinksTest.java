package com.mystore.testcases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.testng.annotations.Test;

import com.mystore.base.BaseClass;
import com.mystore.page.NewDesignsPage;
import com.mystore.utility.ExcelUtility;

public class FetchProductLinksTest extends BaseClass {

    private static final String EXCEL_PATH = System.getProperty("user.dir") + "/TestData/TestData.xlsx";
    private static final String SHEET_NAME = "ProductList";

    @Test(description = "Product listing page (New): Fetch product links and append in the excelsheet")
    public void fetchAndWriteProductLinks() throws Exception {
        // 1️⃣ Launch site (URL from Config.properties)
        launchApp();

        // 2️⃣ Navigate to New Designs Page
        getDriver().get("https://www.silhouettedesignstore.com/new.html");
        NewDesignsPage newDesigns = new NewDesignsPage();

        // 3️⃣ Fetch product links
        List<String> links = newDesigns.getAllProductLinks();

        // Print them in console
        System.out.println("🟩 Found " + links.size() + " product links:");
        for (String link : links) {
            System.out.println(link);
        }

        // 4️⃣ Convert to format suitable for ExcelUtility
        List<String[]> dataToWrite = new ArrayList<>();
        dataToWrite.add(new String[] {"Product Links"}); // Header
        for (String link : links) {
            dataToWrite.add(new String[] {link});
        }

        // 6️⃣ Write new product links
        ExcelUtility.writeProductData(EXCEL_PATH, SHEET_NAME, dataToWrite);
    }
}