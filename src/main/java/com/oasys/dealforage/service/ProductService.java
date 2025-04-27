package com.oasys.dealforage.service;

import com.oasys.dealforage.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    private final RestTemplate restTemplate = new RestTemplate();
    private String lastAsin;
    private long lastPrice;
    private long lastSavings;
    private long lastDealScore;
    private long lastUsedPrice;
    private String lastUpdatedAt;

    public List<Product> fetchInitialProducts() {
        String url = "https://dealforager.com/api/products";
        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);
        List<Product> products = Arrays.asList(response.getBody());
        updateCursor(products);
        return products;
    }

    public List<Product> fetchNextProducts() {
        String url = String.format("https://dealforager.com/api/products?a=%s&p=%d&s=%d&d=%d&u=%d&l=%s",
                lastAsin, lastPrice, lastSavings, lastDealScore, lastUsedPrice, lastUpdatedAt);
        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);
        List<Product> products = Arrays.asList(response.getBody());
        updateCursor(products);
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
}
