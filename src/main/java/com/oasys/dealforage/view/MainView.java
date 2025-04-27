package com.oasys.dealforage.view;

import com.oasys.dealforage.entity.Product;
import com.oasys.dealforage.service.ProductService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Route("")
public class MainView extends VerticalLayout {

    private static final List<String> CATEGORIES = List.of(
            "All", "Appliances", "Arts, Crafts & Sewing", "Auto Parts & Accessories",
            "Baby", "Beauty & Personal Care", "Cell Phones & Accessories",
            "Clothing, Shoes, & Jewelry", "Electronics", "Garden & Outdoor",
            "Health & Household", "Home & Kitchen", "Industrial & Scientific",
            "Musical Instruments", "Movies & TV", "Office Products",
            "Pet Supplies", "Sports & Outdoors", "Tools & Home Improvement",
            "Toys & Games", "Video Games", "Vinyl", "Other"
    );

    private final ProductService productService;
    private final Grid<Product> productGrid = new Grid<>(Product.class);
    private final List<Product> productList = new ArrayList<>();

    private final ComboBox<String> categoryFilter = new ComboBox<>("Select Category");

    private Product lastProduct; // To store the last product details for pagination

    public MainView(ProductService productService) {

        categoryFilter.setItems(CATEGORIES);
        categoryFilter.setValue("All"); // Default value

// Add a listener if you want to filter based on selected category
        categoryFilter.addValueChangeListener(event -> {
            String selectedCategory = event.getValue();
            Notification.show("Selected category: " + selectedCategory);
            // Later: apply filtering logic here if you need
        });

// Add it to the layout
        add(categoryFilter);


        this.productService = productService;
        setSizeFull();
        configureGrid();
        add(productGrid);


        // Add Next Page Button
        Button nextPageButton = new Button("Load More Data", e -> loadNextPage());


        Button exportButton = new Button("Export Selected to CSV", e -> exportSelectedProducts());

        // Center buttons in a HorizontalLayout
        HorizontalLayout buttonLayout = new HorizontalLayout(nextPageButton, exportButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(buttonLayout);



        loadInitialData();
    }

    private void configureGrid() {
        productGrid.setSizeFull();
        productGrid.setColumns("asin", "title", "newprice", "usedprice", "savingspercent");
        productGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    private void loadInitialData() {
        List<Product> products = productService.fetchInitialProducts();

        System.out.println( "Initial products loaded: " + products.size());
        System.out.println( "Last product: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getAsin()));
        System.out.println( "Last product price: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getNewprice()));
        System.out.println( "Last product savings: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getSavingspercent()));
        System.out.println( "Last product deal score: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getDealscore()));
        System.out.println( "Last product used price: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getUsedprice()));
        System.out.println( "Last product updated at: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getUpdated_at()));
        System.out.println( "Last product last change: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getLastchange()));
        System.out.println( "Last product last update: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getLastupdate()));
        System.out.println( "Last product source: " + (products.isEmpty() ? "None" : products.get(products.size() -1).getSource()));

        if (!products.isEmpty()) {
            productList.addAll(products);
            lastProduct = products.get(products.size() - 1); // Store the last product
        }
        productGrid.setItems(productList);
    }

    private void loadNextPage() {
        if (lastProduct == null) {
            Notification.show("No more products to load.");
            return;
        }

        // Fetch the next set of products using the last product's details
        List<Product> products = productService.fetchNextProducts(
                lastProduct.getAsin(),
                lastProduct.getPricedifference(),
                lastProduct.getSavingspercent(),
                lastProduct.getDealscore(),
                lastProduct.getUsedprice(),
                lastProduct.getLastchange()
        );

        if (!products.isEmpty()) {
            productList.addAll(products);
            lastProduct = products.get(products.size() - 1); // Update the last product
            productGrid.getDataProvider().refreshAll();
        } else {
            Notification.show("No more products to load.");
        }
    }



    private void exportSelectedProducts() {
        Set<Product> selectedProducts = productGrid.getSelectedItems();
        if (selectedProducts.isEmpty()) {
            Notification.show("No products selected for export.");
            return;
        }

        try {
            StringWriter writer = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("ASIN", "Title", "New Price", "Used Price", "Savings Percent", "Deal Score", "Last Updated"));

            for (Product product : selectedProducts) {
                csvPrinter.printRecord(
                        product.getAsin(),
                        product.getTitle(),
                        product.getNewprice(),
                        product.getUsedprice(),
                        product.getSavingspercent(),
                        product.getDealscore(),
                        product.getUpdated_at()
                );
            }

            csvPrinter.flush();
            byte[] csvData = writer.toString().getBytes(StandardCharsets.UTF_8);
            StreamResource resource = new StreamResource("selected_products.csv", () -> new ByteArrayInputStream(csvData));

            // Remove any existing download link
            getChildren()
                    .filter(component -> component instanceof Anchor)
                    .forEach(this::remove);

            // Add the new download link
            Anchor downloadLink = new Anchor(resource, "Download CSV");
            downloadLink.getElement().setAttribute("download", true);

            // Add JavaScript to notify when the file is downloaded
            downloadLink.getElement().executeJs(
                    "this.addEventListener('click', () => $0.$server.notifyDownloadComplete());",
                    getElement()
            );


            // Center the download link
            HorizontalLayout downloadLayout = new HorizontalLayout(downloadLink);
            downloadLayout.setWidthFull();
            downloadLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            add(downloadLayout);


        } catch (IOException e) {
            Notification.show("Error generating CSV: " + e.getMessage());
        }
    }

    @ClientCallable
    public void notifyDownloadComplete() {
        Notification notification = Notification.show("File download started.");
        notification.setPosition(Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}