package com.oasys.dealforage.view;

import com.oasys.dealforage.entity.Product;
import com.oasys.dealforage.service.ProductService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Route("")
public class MainView extends VerticalLayout {

    private static final Map<String, Integer> CATEGORY_INDEX = new HashMap<>();
    private static final Map<String, Integer> SORT_BY_INDEX = new LinkedHashMap<>();


    static {
        CATEGORY_INDEX.put("All", 0);
        CATEGORY_INDEX.put("Appliances", 1);
        CATEGORY_INDEX.put("Arts, Crafts & Sewing", 2);
        CATEGORY_INDEX.put("Auto Parts & Accessories", 3);
        CATEGORY_INDEX.put("Baby", 4);
        CATEGORY_INDEX.put("Beauty & Personal Care", 5);
        CATEGORY_INDEX.put("Cell Phones & Accessories", 6);
        CATEGORY_INDEX.put("Clothing, Shoes, & Jewelry", 7);
        CATEGORY_INDEX.put("Electronics", 8);
        CATEGORY_INDEX.put("Garden & Outdoor", 9);
        CATEGORY_INDEX.put("Health & Household", 10);
        CATEGORY_INDEX.put("Home & Kitchen", 11);
        CATEGORY_INDEX.put("Industrial & Scientific", 12);
        CATEGORY_INDEX.put("Musical Instruments", 13);
        CATEGORY_INDEX.put("Movies & TV", 14);
        CATEGORY_INDEX.put("Office Products", 15);
        CATEGORY_INDEX.put("Pet Supplies", 16);
        CATEGORY_INDEX.put("Sports & Outdoors", 17);
        CATEGORY_INDEX.put("Tools & Home Improvement", 18);
        CATEGORY_INDEX.put("Toys & Games", 19);
        CATEGORY_INDEX.put("Video Games", 20);
        CATEGORY_INDEX.put("Vinyl", 21);
        CATEGORY_INDEX.put("Other", 22);

        SORT_BY_INDEX.put("Deal Rating", 0);
        SORT_BY_INDEX.put("Estimated Savings", 1);
        SORT_BY_INDEX.put("Estimated Price Difference", 2);
        SORT_BY_INDEX.put("Price", 3);
        SORT_BY_INDEX.put("Most Recent Change", 4);
    }

    private final ProductService productService;
    private final Grid<Product> productGrid = new Grid<>(Product.class);
    private final List<Product> productList = new ArrayList<>();
    private final MultiSelectComboBox<String> categoryFilter = new MultiSelectComboBox<>("Select Categories");

    private final com.vaadin.flow.component.combobox.ComboBox<String> sortByFilter = new com.vaadin.flow.component.combobox.ComboBox<>("Sort By");

    private Product lastProduct;

    public MainView(ProductService productService) {
        this.productService = productService;
        setSizeFull();
        configureGrid();
        add(createFilterLayout(), productGrid, createButtonLayout());
        loadInitialData();
    }

    private HorizontalLayout createFilterLayout() {
        categoryFilter.setItems(CATEGORY_INDEX.keySet());

        categoryFilter.addClassNames(LumoUtility.Background.BASE, LumoUtility.TextColor.PRIMARY);

        sortByFilter.setItems(SORT_BY_INDEX.keySet());
        sortByFilter.setPlaceholder("Select Sort By");
        sortByFilter.addClassNames(LumoUtility.Background.BASE, LumoUtility.TextColor.PRIMARY);


        Button searchButton = new Button("Search", e -> searchProducts());

        HorizontalLayout filterLayout = new HorizontalLayout(categoryFilter, sortByFilter, searchButton);
        filterLayout.setAlignItems(Alignment.END);
        return filterLayout;
    }

    private HorizontalLayout createButtonLayout() {
        Button nextPageButton = new Button("Load More Data", e -> loadNextPage());
        Button exportButton = new Button("Export Selected to CSV", e -> exportSelectedProducts());

        HorizontalLayout buttonLayout = new HorizontalLayout(nextPageButton, exportButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        return buttonLayout;
    }

    private void configureGrid() {
        productGrid.setSizeFull();
        productGrid.setColumns("asin", "title", "newprice", "usedprice", "savingspercent");
        productGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    private void loadInitialData() {
        List<Product> products = productService.fetchInitialProducts();

        if (!products.isEmpty()) {
            productList.addAll(products);
            lastProduct = products.get(products.size() - 1);
        }
        productGrid.setItems(productList);
    }

    private void loadNextPage() {
        if (lastProduct == null) {
            Notification.show("No more products to load.");
            return;
        }

        // Check if filters are applied
        Set<String> selectedCategories = categoryFilter.getValue();
        String categoriesBinary = null;

        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            categoriesBinary = convertCategoriesToBackendFormat(selectedCategories).substring(1);
        }


        String SelectedSortBy = SORT_BY_INDEX.containsKey(sortByFilter.getValue())
                ? SORT_BY_INDEX.get(sortByFilter.getValue()).toString()
                : null;


//        String SelectedSortBy = sortByFilter.getItems().stream().skip(index).findFirst().orElse(null);
        // Fetch the next set of products with filters (if any)
        List<Product> products = productService.fetchNextProducts(
                lastProduct.getAsin(),
                lastProduct.getPricedifference(),
                lastProduct.getSavingspercent(),
                lastProduct.getDealscore(),
                lastProduct.getUsedprice(),
                lastProduct.getLastchange(),
                categoriesBinary, // Pass the filter,
                SelectedSortBy
        );

        if (!products.isEmpty()) {
            productList.addAll(products);
            lastProduct = products.get(products.size() - 1);
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

            getChildren()
                    .filter(component -> component instanceof Anchor)
                    .forEach(this::remove);

            Anchor downloadLink = new Anchor(resource, "Download CSV");
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.getElement().executeJs(
                    "this.addEventListener('click', () => $0.$server.notifyDownloadComplete());",
                    getElement()
            );

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

    private void searchProducts() {
        Map<String, String> filters = new HashMap<>();
        String selectedSortBy = sortByFilter.getValue();
        Set<String> selectedCategories = categoryFilter.getValue();

        if ((selectedCategories == null || selectedCategories.isEmpty())
                && (selectedSortBy == null || selectedSortBy.isEmpty())) {
            Notification.show("Please select at least one category or sort option.");
            return;
        }


        if (selectedSortBy != null) {
            Integer sortIndex = SORT_BY_INDEX.get(selectedSortBy);
            filters.put("sort", sortIndex.toString());
        }

        if ((selectedCategories != null || !selectedCategories.isEmpty())) {
            String categoriesBinary = convertCategoriesToBackendFormat(selectedCategories);
            categoriesBinary = categoriesBinary.substring(1);

            filters.put("cat", categoriesBinary);
        }


        List<Product> filteredProducts = productService.fetchProductsWithFilters(filters);

        productList.clear();
        productList.addAll(filteredProducts);
        productGrid.setItems(productList);

        if (!filteredProducts.isEmpty()) {
            lastProduct = filteredProducts.get(filteredProducts.size() - 1);
        } else {
            lastProduct = null;
        }
    }


    private String convertCategoriesToBackendFormat(Set<String> selectedCategories) {
        StringBuilder sb = new StringBuilder("00000000000000000000000");

        if (selectedCategories.contains("All")) {
            for (int i = 0; i < sb.length(); i++) {
                sb.setCharAt(i, '1');
            }
            return sb.toString();
        }

        for (String category : selectedCategories) {
            Integer index = CATEGORY_INDEX.get(category);
            if (index != null) {
                sb.setCharAt(index, '1');
            }
        }

        return sb.toString();
    }
}
