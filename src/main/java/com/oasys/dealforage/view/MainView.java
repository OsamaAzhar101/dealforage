package com.oasys.dealforage.view;

import com.oasys.dealforage.entity.Product;
import com.oasys.dealforage.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("")
public class MainView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<Product> productGrid = new Grid<>(Product.class);
    private final List<Product> selectedProducts = new ArrayList<>();
    private int currentPage = 1;
    private final int pageSize = 20;

    public MainView(ProductService productService) {
        this.productService = productService;

        // Filters
        TextField categoryField = new TextField("Category");
        NumberField minPriceField = new NumberField("Min Price");
        NumberField maxPriceField = new NumberField("Max Price");
        Button filterButton = new Button("Apply Filters", e -> applyFilters(categoryField.getValue(),
                minPriceField.getValue(), maxPriceField.getValue()));

        HorizontalLayout filters = new HorizontalLayout(categoryField, minPriceField, maxPriceField, filterButton);
        add(filters);

        // Product Grid
        productGrid.setColumns("title", "price", "resalePrice", "category");
        productGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        productGrid.addSelectionListener(event -> {
            selectedProducts.clear();
            selectedProducts.addAll(event.getAllSelectedItems());
        });

        // Infinite Scrolling
        productGrid.getElement().addEventListener("scroll", e -> loadMoreProducts())
                .debounce(300);

        add(productGrid);

        // Export Button
        Button exportButton = new Button("Export Selected", e -> exportSelectedProducts());
        add(exportButton);

        // Initial Load
        loadMoreProducts();
    }

    private void applyFilters(String category, Double minPrice, Double maxPrice) {
        currentPage = 1;
        List<Product> products = productService.fetchProducts(category, minPrice != null ? minPrice : 0,
                maxPrice != null ? maxPrice : Double.MAX_VALUE, currentPage, pageSize);
        productGrid.setItems(products);
    }

    private void loadMoreProducts() {
        List<Product> existingItems = productGrid.getListDataView().getItems().collect(Collectors.toList());
        List<Product> newItems = productService.fetchProducts("electronics", 0, Double.MAX_VALUE, currentPage, pageSize);
        List<Product> combinedItems = Stream.concat(existingItems.stream(), newItems.stream())
                .collect(Collectors.toList());
        productGrid.setItems(combinedItems);
        currentPage++;
    }

   /* private void exportSelectedProducts() {
        try {
            StringWriter writer = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("ID", "Title", "Price", "Resale Price", "Category"));

            for (Product product : selectedProducts) {
                csvPrinter.printRecord(product.getId(), product.getTitle(), product.getPrice(),
                        product.getResalePrice(), product.getCategory());
            }

            csvPrinter.flush();
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(writer.toString().getBytes()));
            StreamResource streamResource = new StreamResource("selected_products.csv", () -> {
                try {
                    return resource.getInputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Anchor downloadLink = new Anchor(streamResource, "Download CSV");
            downloadLink.getElement().setAttribute("download", true);
            add(downloadLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

*/
    private void exportSelectedProducts() {
        try {
            StringWriter writer = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("ID", "Title", "Price", "Resale Price", "Category"));

            for (Product product : selectedProducts) {
                csvPrinter.printRecord(product.getId(), product.getTitle(), product.getPrice(),
                        product.getResalePrice(), product.getCategory());
            }

            csvPrinter.flush();

            // Write the CSV content to a file in the "dealforage" directory
            java.nio.file.Path outputPath = java.nio.file.Paths.get("dealforage", "selected_products.csv");
            java.nio.file.Files.createDirectories(outputPath.getParent()); // Ensure the directory exists
            java.nio.file.Files.write(outputPath, writer.toString().getBytes());

            System.out.println("CSV file created at: " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
