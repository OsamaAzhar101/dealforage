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
    private String lastPrice;
    private String lastSavings;
    private String lastDealScore;
    private String lastUsedPrice;
    private String lastUpdatedAt;

    public List<Product> fetchInitialProducts() {
        String url = "https://dealforager.com/api/products";
        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);
        List<Product> products = Arrays.asList(response.getBody());
        updateCursor(products);
        return products;
    }

    public List<Product> fetchNextProducts(String asin, String priceDifference,String savingspercent,
                                           String dealScore, String usedPrice,
                                             String lastupdate) {

        System.out.println("Fetching products with ASIN: " + asin);
        System.out.println("Price Difference: " + priceDifference);
        System.out.println("Savings Percent: " + savingspercent);
        System.out.println("Deal Score: " + dealScore);
        System.out.println("Used Price: " + usedPrice);
        System.out.println("LastChange At: " + lastupdate);

        String url = String.format("https://dealforager.com/api/products?a=%s&p=%s&s=%s&d=%s&u=%s&l=%s",
                asin, priceDifference, savingspercent, dealScore, usedPrice, lastupdate);

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

}
