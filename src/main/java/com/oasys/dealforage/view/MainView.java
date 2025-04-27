package com.oasys.dealforage.view;

import com.oasys.dealforage.entity.Product;
import com.oasys.dealforage.service.ProductService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
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

    private final ProductService productService;
    private final Grid<Product> productGrid = new Grid<>(Product.class);
    private final List<Product> productList = new ArrayList<>();

    public MainView(ProductService productService) {
        this.productService = productService;
        setSizeFull();
        configureGrid();
        add(productGrid);

        // Add Export Button
        Button exportButton = new Button("Export Selected to CSV", e -> exportSelectedProducts());
        add(exportButton);

        loadInitialData();
    }

    private void configureGrid() {
        productGrid.setSizeFull();
        productGrid.setColumns("asin", "title", "newprice", "usedprice", "savingspercent");
        productGrid.addAttachListener(e -> productGrid.getElement().executeJs(
                "this.$.table.addEventListener('scroll', function() {" +
                        "  if (this.scrollTop + this.clientHeight >= this.scrollHeight) {" +
                        "    $0.$server.loadMore();" +
                        "  }" +
                        "}.bind(this));", getElement()));
        productGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    private void loadInitialData() {
        List<Product> products = productService.fetchInitialProducts();
        productList.addAll(products);
        productGrid.setItems(productList);
    }

    @ClientCallable
    public void loadMore() {
        List<Product> products = productService.fetchNextProducts();
        productList.addAll(products);
        productGrid.getDataProvider().refreshAll();
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
            Anchor downloadLink = new Anchor(resource, "Download CSV");
            downloadLink.getElement().setAttribute("download", true);
            add(downloadLink);
        } catch (IOException e) {
            Notification.show("Error generating CSV: " + e.getMessage());
        }
    }
}
