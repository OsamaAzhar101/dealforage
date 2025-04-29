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
                                           String categoriesBinary, String SelectedSortBy,
                                           String estimatedSavings) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://dealforager.com/api/products")
                .queryParam("a", asin)
                .queryParam("p", priceDifference)
                .queryParam("s", savingspercent)
                .queryParam("d", dealScore)
                .queryParam("u", usedPrice)
                .queryParam("l", lastupdate);


        // Add category and domain parameters
        addCategoryAndDomainParams(builder, categoriesBinary, SelectedSortBy, estimatedSavings);

        addSortParams(builder, categoriesBinary, SelectedSortBy, estimatedSavings);


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

    private void addSortParams(UriComponentsBuilder builder, String categoriesBinary, String selectedSortBy, String estimatedSavings) {


        if(estimatedSavings != null && !estimatedSavings.isEmpty()){
            builder.queryParam("minSavings", estimatedSavings.contains(".0")
                    ? estimatedSavings.replaceAll(".0", "") : estimatedSavings);
        }


    }


    private void addCategoryAndDomainParams(UriComponentsBuilder builder,
                                            String categoriesBinary,
                                            String SelectedSortBy,
                                            String estimatedSavings) {

        if (categoriesBinary != null
                || SelectedSortBy != null
                || estimatedSavings != null) {


            if (categoriesBinary == null) {
                builder.queryParam("cat", "0000000000000000000000").queryParam("domain", "1");
            } else {
                if (categoriesBinary != null) {
                    builder.queryParam("cat", categoriesBinary).queryParam("domain", "1");
                }

            }

            if(SelectedSortBy == null) {
                builder.queryParam("sort", "0");
            }
            else {
             builder.queryParam("sort", SelectedSortBy);
            }


        }


    }

}
