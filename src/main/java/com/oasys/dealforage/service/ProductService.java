package com.oasys.dealforage.service;

import com.oasys.dealforage.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


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
        updateCursor(products);
        return products;
    }

    public List<Product> fetchNextProducts(String asin, String priceDifference, String savingspercent,
                                           String dealScore, String usedPrice, String lastupdate,
                                           String categoriesBinary, String SelectedSortBy) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://dealforager.com/api/products")
                .queryParam("a", asin)
                .queryParam("p", priceDifference)
                .queryParam("s", savingspercent)
                .queryParam("d", dealScore)
                .queryParam("u", usedPrice)
                .queryParam("l", lastupdate);

        if (categoriesBinary != null) {
            builder.queryParam("cat", categoriesBinary)
                    .queryParam("domain", "1");

        }

        if (SelectedSortBy != null) {
            builder.queryParam("sort", SelectedSortBy);
        }
        else {
            builder.queryParam("sort", "0");
        }

        String url = builder.toUriString();


        System.out.println("Fetching next products with URL: " + url);
        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);
        return Arrays.asList(response.getBody());
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

    public List<Product> fetchProducts(int offset, int limit) {
        String url = String.format("https://dealforager.com/api/products?offset=%d&limit=%d", offset, limit);
        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);
        return Arrays.asList(response.getBody());
    }


    public List<Product> fetchProductsWithFilters(Map<String, String> filters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL);

        // Add default parameters
        builder.queryParam("domain", "1");
        if ((filters != null || !filters.isEmpty())
                && (filters.get("sort") == null || filters.get("sort").isEmpty())) {
            builder.queryParam("sort", "0");
        }

        if (filters != null) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    builder.queryParam(entry.getKey(), entry.getValue());
                }
            }
        }

        String urlWithParams = builder.toUriString();
        System.out.println("Fetching products with URL: " + urlWithParams);

        ResponseEntity<Product[]> response = restTemplate.getForEntity(urlWithParams, Product[].class);
        return Arrays.asList(response.getBody());
    }


}
