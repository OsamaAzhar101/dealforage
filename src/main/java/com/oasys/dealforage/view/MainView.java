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
import com.vaadin.flow.component.textfield.NumberField;
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
    private final NumberField estimatedSavingsField = new NumberField("Estimated Savings");

    private final NumberField estimatedPriceDifferenceField = new NumberField("Estimated Price Difference");
    private final com.vaadin.flow.component.combobox.ComboBox<String> sortByFilter = new com.vaadin.flow.component.combobox.ComboBox<>("Sort By");

    private final NumberField usedPriceMin = new NumberField("Min Used Price");
    private final NumberField usedPriceMax = new NumberField("Max Used Price");
    private final NumberField newPriceMin = new NumberField("Min New Price");
    private final NumberField newPriceMax = new NumberField("Max New Price");

    private Product lastProduct;


    // Add this method to create the estimated savings filter
    private HorizontalLayout createEstimatedSavingsFilter() {
        // Create a NumberField for estimated savings

        estimatedSavingsField.setPlaceholder("Enter value");
        estimatedSavingsField.setStep(1); // Increment/Decrement step
        estimatedSavingsField.setMin(-100);// Minimum value
        estimatedSavingsField.setMax(100); // Maximum value
        estimatedSavingsField.setWidth("150px");

        // Add a mouse wheel listener for scrolling effect
        estimatedSavingsField.getElement().addEventListener("wheel", event -> {
            event.getEventData().put("deltaY", "event.deltaY");
            String deltaYString = event.getEventData().getString("deltaY");

            try {
                double deltaY = Double.parseDouble(deltaYString); // Convert to double
                Double currentValue = estimatedSavingsField.getValue() != null ? estimatedSavingsField.getValue() : 0.0;

                if (deltaY > 0) {
                    // Scroll down: decrement value
                    estimatedSavingsField.setValue(Math.max(currentValue - estimatedSavingsField.getStep(), estimatedSavingsField.getMin()));
                } else {
                    // Scroll up: increment value
                    estimatedSavingsField.setValue(Math.min(currentValue + estimatedSavingsField.getStep(), estimatedSavingsField.getMax()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid deltaY value: " + deltaYString);
            }

        }).addEventData("event.deltaY");

        // Add the NumberField and label to a layout
        HorizontalLayout savingsFilterLayout = new HorizontalLayout(estimatedSavingsField);
        savingsFilterLayout.setAlignItems(Alignment.BASELINE);

        // Add a listener to handle value changes
        estimatedSavingsField.addValueChangeListener(event -> {
            Double value = event.getValue();
            if (value != null) {
                System.out.println("Estimated Savings: " + value + "%");
                // Add logic to apply the filter
            }
        });

        return savingsFilterLayout;
    }


    private HorizontalLayout createEstimatedPriceDifferenceFilter() {

        estimatedPriceDifferenceField.setPlaceholder("Enter value");
        estimatedPriceDifferenceField.setStep(1); // Increment/Decrement step
        estimatedPriceDifferenceField.setMin(0);// Minimum value
        estimatedPriceDifferenceField.setMax(100); // Maximum value
        estimatedPriceDifferenceField.setWidth("150px");

       

        // Add a mouse wheel listener for scrolling effect
        estimatedPriceDifferenceField.getElement().addEventListener("wheel", event -> {
            event.getEventData().put("deltaY", "event.deltaY");
            String deltaYString = event.getEventData().getString("deltaY");

            try {
                double deltaY = Double.parseDouble(deltaYString); // Convert to double
                Double currentValue = estimatedPriceDifferenceField.getValue() != null ? estimatedPriceDifferenceField.getValue() : 0.0;

                if (deltaY > 0) {
                    // Scroll down: decrement value
                    estimatedSavingsField.setValue(Math.max(currentValue - estimatedPriceDifferenceField.getStep(), estimatedPriceDifferenceField.getMin()));
                } else {
                    // Scroll up: increment value
                    estimatedSavingsField.setValue(Math.min(currentValue + estimatedPriceDifferenceField.getStep(), estimatedPriceDifferenceField.getMax()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid deltaY value: " + deltaYString);
            }

        }).addEventData("event.deltaY");

        // Add the NumberField and label to a layout
        HorizontalLayout priceDifferenceFilterLayout = new HorizontalLayout(estimatedPriceDifferenceField);
        priceDifferenceFilterLayout.setAlignItems(Alignment.BASELINE);

        // Add a listener to handle value changes
        estimatedPriceDifferenceField.addValueChangeListener(event -> {
            Double value = event.getValue();
            if (value != null) {
                System.out.println("Estimated Price Difference: " + value + "$");

            }
        });

        return priceDifferenceFilterLayout;
    }


    private HorizontalLayout createNewPriceMinFilter() {
        // Create a NumberField for estimated savings

        newPriceMin.setPlaceholder("Enter value");
        newPriceMin.setStep(1); // Increment/Decrement step
        newPriceMin.setMin(-100);// Minimum value
        newPriceMin.setMax(100); // Maximum value
        newPriceMin.setWidth("150px");

        // Add a '%' label

        // Add a mouse wheel listener for scrolling effect
        newPriceMin.getElement().addEventListener("wheel", event -> {
            event.getEventData().put("deltaY", "event.deltaY");
            String deltaYString = event.getEventData().getString("deltaY");

            try {
                double deltaY = Double.parseDouble(deltaYString); // Convert to double
                Double currentValue = newPriceMin.getValue() != null ? newPriceMin.getValue() : 0.0;

                if (deltaY > 0) {
                    newPriceMin.setValue(Math.max(currentValue - newPriceMin.getStep(), newPriceMin.getMin()));
                } else {
                    newPriceMin.setValue(Math.min(currentValue + newPriceMin.getStep(), newPriceMin.getMax()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid deltaY value: " + deltaYString);
            }

        }).addEventData("event.deltaY");

        // Add the NumberField and label to a layout
        HorizontalLayout savingsFilterLayout = new HorizontalLayout(newPriceMin);
        savingsFilterLayout.setAlignItems(Alignment.BASELINE);

        // Add a listener to handle value changes
        newPriceMin.addValueChangeListener(event -> {
            Double value = event.getValue();
            if (value != null) {
                System.out.println("newPriceMin : " + value + "%");
                // Add logic to apply the filter
            }
        });

        return savingsFilterLayout;
    }


    private HorizontalLayout createNewPriceMaxFilter() {
        // Create a NumberField for estimated savings

        newPriceMax.setPlaceholder("Enter value");
        newPriceMax.setStep(1); // Increment/Decrement step
        newPriceMax.setMin(-100);// Minimum value
        newPriceMax.setMax(100); // Maximum value
        newPriceMax.setWidth("130px");


        // Add a mouse wheel listener for scrolling effect
        newPriceMax.getElement().addEventListener("wheel", event -> {
            event.getEventData().put("deltaY", "event.deltaY");
            String deltaYString = event.getEventData().getString("deltaY");

            try {
                double deltaY = Double.parseDouble(deltaYString); // Convert to double
                Double currentValue = newPriceMax.getValue() != null ? newPriceMax.getValue() : 0.0;

                if (deltaY > 0) {
                    newPriceMax.setValue(Math.max(currentValue - newPriceMax.getStep(), newPriceMax.getMin()));
                } else {
                    newPriceMax.setValue(Math.min(currentValue + newPriceMax.getStep(), newPriceMax.getMax()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid deltaY value: " + deltaYString);
            }

        }).addEventData("event.deltaY");

        // Add the NumberField and label to a layout
        HorizontalLayout savingsFilterLayout = new HorizontalLayout(newPriceMax);
        savingsFilterLayout.setAlignItems(Alignment.BASELINE);

        // Add a listener to handle value changes
        newPriceMax.addValueChangeListener(event -> {
            Double value = event.getValue();
            if (value != null) {
                System.out.println("newPriceMax : " + value + "%");
                // Add logic to apply the filter
            }
        });

        return savingsFilterLayout;
    }


    private HorizontalLayout createUsedPriceMinFilter() {
        // Create a NumberField for estimated savings

        usedPriceMin.setPlaceholder("Enter value");
        usedPriceMin.setStep(1); // Increment/Decrement step
        usedPriceMin.setMin(-100);// Minimum value
        usedPriceMin.setMax(100); // Maximum value
        usedPriceMin.setWidth("130px");

      
        // Add a mouse wheel listener for scrolling effect
        usedPriceMin.getElement().addEventListener("wheel", event -> {
            event.getEventData().put("deltaY", "event.deltaY");
            String deltaYString = event.getEventData().getString("deltaY");

            try {
                double deltaY = Double.parseDouble(deltaYString); // Convert to double
                Double currentValue = usedPriceMin.getValue() != null ? usedPriceMin.getValue() : 0.0;

                if (deltaY > 0) {
                    usedPriceMin.setValue(Math.max(currentValue - usedPriceMin.getStep(), usedPriceMin.getMin()));
                } else {
                    usedPriceMin.setValue(Math.min(currentValue + usedPriceMin.getStep(), usedPriceMin.getMax()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid deltaY value: " + deltaYString);
            }

        }).addEventData("event.deltaY");

        // Add the NumberField and label to a layout
        HorizontalLayout savingsFilterLayout = new HorizontalLayout(usedPriceMin);
        savingsFilterLayout.setAlignItems(Alignment.BASELINE);

/*
        // Add a listener to handle value changes
        usedPriceMin.addValueChangeListener(event -> {
            Double value = event.getValue();
            if (value != null) {
                System.out.println("newPriceMax : " + value + "%");
                // Add logic to apply the filter
            }
        });
*/

        return savingsFilterLayout;
    }


    private HorizontalLayout createUsedPriceMaxFilter() {
        // Create a NumberField for estimated savings

        usedPriceMax.setPlaceholder("Enter value");
        usedPriceMax.setStep(1); // Increment/Decrement step
        usedPriceMax.setMin(-100);// Minimum value
        usedPriceMax.setMax(100); // Maximum value
        usedPriceMax.setWidth("130px");


        // Add a mouse wheel listener for scrolling effect
        usedPriceMax.getElement().addEventListener("wheel", event -> {
            event.getEventData().put("deltaY", "event.deltaY");
            String deltaYString = event.getEventData().getString("deltaY");

            try {
                double deltaY = Double.parseDouble(deltaYString); // Convert to double
                Double currentValue = usedPriceMax.getValue() != null ? usedPriceMax.getValue() : 0.0;

                if (deltaY > 0) {
                    usedPriceMax.setValue(Math.max(currentValue - usedPriceMax.getStep(), usedPriceMax.getMin()));
                } else {
                    usedPriceMax.setValue(Math.min(currentValue + usedPriceMax.getStep(), usedPriceMax.getMax()));
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid deltaY value: " + deltaYString);
            }

        }).addEventData("event.deltaY");

        // Add the NumberField and label to a layout
        HorizontalLayout savingsFilterLayout = new HorizontalLayout(usedPriceMax);
        savingsFilterLayout.setAlignItems(Alignment.BASELINE);

        // Add a listener to handle value changes
        usedPriceMax.addValueChangeListener(event -> {
            Double value = event.getValue();
            if (value != null) {
                System.out.println("newPriceMax : " + value + "%");
                // Add logic to apply the filter
            }
        });

        return savingsFilterLayout;
    }

    public MainView(ProductService productService) {
        this.productService = productService;
        setSizeFull();
        getElement().getStyle().set("zoom", "80%"); // Set zoom level to 80%
        configureGrid();
        add(createFilterLayout(), productGrid, createButtonLayout());
        loadInitialData();
    }


private HorizontalLayout createFilterLayout() {
    categoryFilter.setItems(CATEGORY_INDEX.keySet());
    categoryFilter.addClassNames(LumoUtility.Background.BASE, LumoUtility.TextColor.PRIMARY);
    categoryFilter.setWidth("150px");

    sortByFilter.setItems(SORT_BY_INDEX.keySet());
    sortByFilter.setPlaceholder("Select Sort By");
    sortByFilter.setWidth("130px");
    sortByFilter.addClassNames(LumoUtility.Background.BASE, LumoUtility.TextColor.PRIMARY);

    // Add the estimated savings filter
    HorizontalLayout savingsFilter = createEstimatedSavingsFilter();
    HorizontalLayout priceDifferenceFilter = createEstimatedPriceDifferenceFilter();
    HorizontalLayout newPriceMin = createNewPriceMinFilter();
    HorizontalLayout newPriceMax = createNewPriceMaxFilter();
    HorizontalLayout usedPriceMax = createUsedPriceMaxFilter();
    HorizontalLayout usedPriceMin = createUsedPriceMinFilter();

    // Create a layout for filters and center align them
    HorizontalLayout filterLayout = new HorizontalLayout(categoryFilter, sortByFilter,
            savingsFilter, priceDifferenceFilter, newPriceMin, newPriceMax, usedPriceMin, usedPriceMax);
    filterLayout.setAlignItems(Alignment.CENTER);


    filterLayout.setJustifyContentMode(JustifyContentMode.CENTER);
    filterLayout.setWidthFull();



    // Create a separate layout for the search button and center align it
    Button searchButton = new Button("Search", e -> searchProducts());
    HorizontalLayout searchButtonLayout = new HorizontalLayout(searchButton);
    searchButtonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
    searchButtonLayout.setWidthFull();

    // Combine both layouts
    VerticalLayout combinedLayout = new VerticalLayout(filterLayout, searchButtonLayout);
    combinedLayout.setAlignItems(Alignment.CENTER);
    combinedLayout.setWidthFull();
    combinedLayout.getStyle().set("margin-left", "100px");

    return new HorizontalLayout(combinedLayout);
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

        // Set predefined columns first
        productGrid.setColumns("asin", "title", "newprice", "usedprice", "savingspercent");
        // Add a custom column for the product image
        productGrid.addComponentColumn(product -> {
            if (product.getProcessedImage() != null) {
                com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(product.getProcessedImage(), "Product Image");
                image.setWidth("100px");
                image.setHeight("100px");
                return image;
            } else {
                return new com.vaadin.flow.component.html.Span("No Image");
            }
        }).setHeader("Image").setAutoWidth(true);


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

        Map<String, String> filters = buildFilters();

        if (filters != null
                && !filters.isEmpty()) {

            String estimatedSavings = String.valueOf(((estimatedSavingsField.getValue() != null))
                    ? estimatedSavingsField.getValue() : "");

            String estimatedPriceDifference = String.valueOf(((estimatedPriceDifferenceField.getValue() != null))
                    ? estimatedPriceDifferenceField.getValue() : "");

            // Check if filters are applied
            Set<String> selectedCategories = categoryFilter.getValue();
            String categoriesBinary = null;

            if (selectedCategories != null && !selectedCategories.isEmpty()) {
                categoriesBinary = convertCategoriesToBackendFormat(selectedCategories).substring(1);
            }

            String SelectedSortBy = SORT_BY_INDEX.containsKey(sortByFilter.getValue())
                    ? SORT_BY_INDEX.get(sortByFilter.getValue()).toString()
                    : null;


        }


//        String SelectedSortBy = sortByFilter.getItems().stream().skip(index).findFirst().orElse(null);
        // Fetch the next set of products with filters (if any)
        List<Product> products = productService.fetchNextProducts(
                lastProduct.getAsin(),
                lastProduct.getPricedifference(),
                lastProduct.getSavingspercent(),
                lastProduct.getDealscore(),
                lastProduct.getUsedprice(),
                lastProduct.getLastchange(),
                filters

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
        Map<String, String> filters = buildFilters();

        if (filters.isEmpty()) {
            Notification.show("Please select at least one category or sort option.");
            return;
        }

        List<Product> filteredProducts = productService.fetchProductsWithFilters(filters);

        updateProductList(filteredProducts);
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


    private Map<String, String> buildFilters() {
        Map<String, String> filters = new HashMap<>();
        addSortFilter(filters);
        addCategoryFilter(filters);
        addEstimatedSavingsFilter(filters);
        addEstimatedPriceDifferenceFilter(filters);
        addNewPriceMin(filters);
        addNewPriceMax(filters);
        addUsedPriceMax(filters);
        addUsedPriceMin(filters);

        return filters;
    }

    private void addNewPriceMin(Map<String, String> filters) {

        Double newPriceMinVaue = newPriceMin.getValue();
        if (newPriceMinVaue != null) {
            filters.put("minNew", newPriceMin.toString());
        }
    }

    private void addNewPriceMax(Map<String, String> filters) {

        Double newPriceMaxValue = newPriceMax.getValue();
        if (newPriceMaxValue != null) {
            filters.put("maxNew", newPriceMaxValue.toString());
        }
    }

    private void addUsedPriceMin(Map<String, String> filters) {

        Double usedPriceMinValue = usedPriceMin.getValue();
        if (usedPriceMinValue != null) {
            filters.put("minUsed", usedPriceMinValue.toString());
        }
    }

    private void addUsedPriceMax(Map<String, String> filters) {

        Double usedMaxValue = usedPriceMax.getValue();
        if (usedMaxValue != null) {
            filters.put("maxUsed", usedMaxValue.toString());
        }
    }

    private void addEstimatedSavingsFilter(Map<String, String> filters) {
        // Assuming you have a method to get the estimated savings value

        Double estimatedSavingsValue = estimatedSavingsField.getValue();

        if (estimatedSavingsValue != null) {
            filters.put("minSavings", estimatedSavingsValue.toString());
        }
    }

    private void addEstimatedPriceDifferenceFilter(Map<String, String> filters) {
        // Assuming you have a method to get the estimated price difference value
        Double estimatedPriceDifferenceValue = estimatedPriceDifferenceField.getValue();

        if (estimatedPriceDifferenceValue != null) {
            filters.put("minDifference", estimatedPriceDifferenceValue.toString());
        }
    }


    private void addSortFilter(Map<String, String> filters) {
        String selectedSortBy = sortByFilter.getValue();
        if (selectedSortBy != null) {
            Integer sortIndex = SORT_BY_INDEX.get(selectedSortBy);
            filters.put("sort", sortIndex.toString());
        }
    }

    private void addCategoryFilter(Map<String, String> filters) {
        Set<String> selectedCategories = categoryFilter.getValue();
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            String categoriesBinary = convertCategoriesToBackendFormat(selectedCategories).substring(1);
            filters.put("cat", categoriesBinary);
        }
    }


    private void updateProductList(List<Product> filteredProducts) {
        productList.clear();
        productList.addAll(filteredProducts);
        productGrid.setItems(productList);

        lastProduct = filteredProducts.isEmpty() ? null : filteredProducts.get(filteredProducts.size() - 1);
    }


}
