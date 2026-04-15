package com.mystore.utility;

import java.util.ArrayList;
import java.util.List;

public class ProductContext {

    private static final List<String> productLinks = new ArrayList<>();

    public static void addLinks(List<String> links) {
        productLinks.clear(); // always fresh run
        productLinks.addAll(links);
    }

    public static List<String> getLinks() {
        return productLinks;
    }

    public static void clear() {
        productLinks.clear();
    }
}