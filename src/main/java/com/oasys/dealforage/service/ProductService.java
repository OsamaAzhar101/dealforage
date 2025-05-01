package com.oasys.dealforage.service;

import com.oasys.dealforage.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    private static final String BASE_URL = "https://dealforager.com/api/products";

    private final RestTemplate restTemplate = new RestTemplate();
    private String lastAsin;
    private String lastPrice;
    private String lastSavings;
    private String lastDealScore;
    private String lastUsedPrice;
    private String lastUpdatedAt;

    public List<Product> fetchInitialProducts() {
        String url = "https://dealforager.com/api/products";
        System.out.println("Fetching initial products with URL: " + url);

        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);
        List<Product> products = Arrays.asList(response.getBody());

        // Process the image field and map it to processedImage
        for (Product product : products) {
            if (product.getImage() != null) {

                product.setProcessedImage(getIntegerArrayToString(product.getImage()));
//                System.out.println("Processing image for product: " + product.getProcessedImage());
            }
        }

        updateCursor(products);
        return products;
    }

    public List<Product> fetchNextProducts(String asin, String priceDifference, String savingspercent,
                                           String dealScore, String usedPrice, String lastupdate,
                                           Map<String, String> filters) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://dealforager.com/api/products")
                .queryParam("a", asin)
                .queryParam("p", priceDifference)
                .queryParam("s", savingspercent)
                .queryParam("d", dealScore)
                .queryParam("u", usedPrice)
                .queryParam("l", lastupdate);


        // Add category and domain parameters
        addCategoryAndDomainParams(builder, filters);

        addSortParams(builder, filters);


        String url = builder.toUriString();


        System.out.println("Fetching next products with URL: " + url);
        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);

        List<Product> products = Arrays.asList(response.getBody());

        processProductImages(products);

        return products;
    }

    private void updateCursor(List<Product> products) {
        if (!products.isEmpty()) {
            Product lastProduct = products.get(products.size() - 1);
            lastAsin = lastProduct.getAsin();
            lastPrice = lastProduct.getNewprice();
            lastSavings = lastProduct.getSavingspercent();
            lastDealScore = lastProduct.getDealscore();
            lastUsedPrice = lastProduct.getUsedprice();
            lastUpdatedAt = lastProduct.getUpdated_at();
        }
    }


    public List<Product> fetchProductsWithFilters(Map<String, String> filters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL);

        addDefaultParameters(builder, filters);

        if (filters != null) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    if (entry.getKey().equals("minSavings")) {
                        builder.queryParam(entry.getKey(), entry.getValue().contains(".0")
                                ? entry.getValue().replaceAll(".0", "") : entry.getValue());
                    } else if (entry.getKey().equals("minDifference")) {
                        builder.queryParam(entry.getKey(), entry.getValue().contains(".0")
                                ? entry.getValue().replaceAll(".0", "") : entry.getValue());
                    } else if (entry.getKey().equals("minUsed")) {
                        builder.queryParam(entry.getKey(), entry.getValue().contains(".0")
                                ? entry.getValue().replaceAll(".0", "") : entry.getValue());
                    } else if (entry.getKey().equals("maxUsed")) {
                        builder.queryParam(entry.getKey(), entry.getValue().contains(".0")
                                ? entry.getValue().replaceAll(".0", "") : entry.getValue());
                    } else if (entry.getKey().equals("maxNew")) {
                        builder.queryParam(entry.getKey(), entry.getValue().contains(".0")
                                ? entry.getValue().replaceAll(".0", "") : entry.getValue());
                    } else if (entry.getKey().equals("minNew")) {
                        builder.queryParam(entry.getKey(), entry.getValue().contains(".0")
                                ? entry.getValue().replaceAll(".0", "") : entry.getValue());
                    } else {
                        builder.queryParam(entry.getKey(), entry.getValue());
                    }


                }
            }
        }

        String urlWithParams = builder.toUriString();
        System.out.println("Fetching products with URL: " + urlWithParams);

        ResponseEntity<Product[]> response = restTemplate.getForEntity(urlWithParams, Product[].class);

        List<Product> products = Arrays.asList(response.getBody());
        // Process the image field and map it to processedImage

        processProductImages(products);

        return products;
    }


    public String getIntegerArrayToString(byte[] imageArray) {
        String imageName = new String(imageArray, StandardCharsets.UTF_8);
        if (imageName != null && imageName.length() > 0) {
            imageName = imageName.replaceAll("SL500", "SL100");
            imageName = "https://c.media-amazon.com/images/I/" + imageName;
        }
        return imageName;
    }

    private void addDefaultParameters(UriComponentsBuilder builder, Map<String, String> filters) {
        // Add default parameters
        builder.queryParam("domain", "1");
        if ((filters != null || !filters.isEmpty())) {

            if ((filters.get("sort") == null || filters.get("sort").isEmpty())) {

                builder.queryParam("sort", "0");
            }
            if (filters.get("cat") == null || filters.get("cat").isEmpty()) {
                builder.queryParam("cat", "0000000000000000000000");
            }

        }
    }

    private void processProductImages(List<Product> products) {
        for (Product product : products) {
            if (product.getImage() != null) {
                product.setProcessedImage(getIntegerArrayToString(product.getImage()));
//                System.out.println("Processing image for product: " + product.getProcessedImage());
            }
        }
    }

    private void addSortParams(UriComponentsBuilder builder, Map<String, String> filters) {

        if (filters != null && filters.size() > 0) {
            if (filters.containsKey("minSavings") && !filters.get("minSavings").isEmpty()) {
                builder.queryParam("minSavings", filters.get("minSavings"));
            }

            if (filters.containsKey("minDifference") && !filters.get("minDifference").isEmpty()) {
                builder.queryParam("minDifference", filters.get("minDifference"));

            }

            if (filters.containsKey("minUsed") && !filters.get("minUsed").isEmpty()) {
                builder.queryParam("minUsed", filters.get("minUsed"));
            }

            if (filters.containsKey("maxUsed") && !filters.get("maxUsed").isEmpty()) {
                builder.queryParam("maxUsed", filters.get("maxUsed"));
            }

            if (filters.containsKey("minNew") && !filters.get("minNew").isEmpty()) {
                builder.queryParam("minNew", filters.get("minNew"));
            }

            if (filters.containsKey("maxNew") && !filters.get("maxNew").isEmpty()) {
                builder.queryParam("maxNew", filters.get("maxNew"));
            }

            if (filters.containsKey("search") && !filters.get("search").isEmpty()) {
                builder.queryParam("search", filters.get("search"));
            }

        }
    }


    private void addCategoryAndDomainParams(UriComponentsBuilder builder,
                                            Map<String, String> filters) {

        if (filters != null && filters.size() > 0 && (
                filters.containsKey("cat")
                        || filters.containsKey("sort")
                        || filters.containsKey("minSavings")
                        || filters.containsKey("minDifference")
                        || filters.containsKey("minUsed")
                        || filters.containsKey("maxUsed")
                        || filters.containsKey("minNew")
                        || filters.containsKey("maxNew")
                        || filters.containsKey("search")
        )) {


            if (!filters.containsKey("cat")) {
                builder.queryParam("cat", "0000000000000000000000").queryParam("domain", "1");
            } else {
                if (filters.containsKey("cat")) {
                    builder.queryParam("cat", filters.get("cat")).queryParam("domain", "1");
                }

            }

            if (!filters.containsKey("sort")) {
                builder.queryParam("sort", "0");
            } else {
                builder.queryParam("sort", filters.get("sort"));
            }


        }


    }

}
