package com.oasys.dealforage.service;

import com.oasys.dealforage.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService_ {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Product> fetchProducts(String category, double minPrice, double maxPrice, int page, int limit) {
        String url = String.format("https://dealforager.com/api/products?page=%d&limit=%d&category=%s&price_min=%.2f&price_max=%.2f",
                page, limit, category, minPrice, maxPrice);

        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);
        return Arrays.asList(response.getBody());
    }
}
