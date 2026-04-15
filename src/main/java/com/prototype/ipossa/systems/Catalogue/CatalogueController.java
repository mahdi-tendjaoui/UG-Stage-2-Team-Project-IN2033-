package com.prototype.ipossa.systems.Catalogue;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class CatalogueController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    @FXML private TableView<CatalogueItem> catalogueTable;

    @FXML private TableColumn<CatalogueItem, String> itemIdColumn;
    @FXML private TableColumn<CatalogueItem, String> descriptionColumn;
    @FXML private TableColumn<CatalogueItem, String> packageTypeColumn;
    @FXML private TableColumn<CatalogueItem, String> unitColumn;
    @FXML private TableColumn<CatalogueItem, Integer> unitsInPackColumn;
    @FXML private TableColumn<CatalogueItem, Double> packageCostColumn;
    @FXML private TableColumn<CatalogueItem, Integer> availabilityColumn;
    @FXML private TableColumn<CatalogueItem, Integer> stockLimitColumn;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    @FXML private TextField argumentField;
    @FXML private Button submitButton;

    private final CatalogueService service = new CatalogueService();
    private String currentAction = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        packageTypeColumn.setCellValueFactory(new PropertyValueFactory<>("packageType"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitsInPackColumn.setCellValueFactory(new PropertyValueFactory<>("unitsInPack"));
        packageCostColumn.setCellValueFactory(new PropertyValueFactory<>("packageCost"));
        availabilityColumn.setCellValueFactory(new PropertyValueFactory<>("availability"));
        stockLimitColumn.setCellValueFactory(new PropertyValueFactory<>("stockLimit"));

        loadTable();

        addButton.setOnAction(e -> currentAction = "add");
        updateButton.setOnAction(e -> currentAction = "update");
        deleteButton.setOnAction(e -> currentAction = "delete");
        searchButton.setOnAction(e -> handleSearch());
        submitButton.setOnAction(e -> handleSubmit());
    }

    private void loadTable() {
        catalogueTable.getItems().setAll(service.getAllItems());
    }

    private void handleSearch() {
        String keyword = searchField.getText();

        if (keyword == null || keyword.isBlank()) {
            loadTable();
        } else {
            catalogueTable.getItems().setAll(service.searchItems(keyword));
        }
    }

    private void handleSubmit() {
        String input = argumentField.getText();

        if (input == null || input.isBlank()) {
            System.out.println("Argument field is empty.");
            return;
        }

        try {
            switch (currentAction) {
                case "add":
                    handleAdd(input);
                    break;
                case "update":
                    handleUpdate(input);
                    break;
                case "delete":
                    handleDelete(input);
                    break;
                default:
                    System.out.println("No action selected. Click Add, Update, or Delete first.");
                    return;
            }

            argumentField.clear();
            loadTable();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Invalid input format.");
        }
    }

    private void handleAdd(String input) {
        String[] parts = input.split(",");

        if (parts.length != 8) {
            System.out.println("Add format must be: itemId, description, packageType, unit, unitsInPack, packageCost, availability, stockLimit");
            return;
        }

        CatalogueItem item = new CatalogueItem(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                Integer.parseInt(parts[4].trim()),
                Double.parseDouble(parts[5].trim()),
                Integer.parseInt(parts[6].trim()),
                Integer.parseInt(parts[7].trim())
        );

        boolean success = service.addProduct(item);
        System.out.println("Add success: " + success);
    }

    private void handleUpdate(String input) {
        String[] parts = input.split(",");

        if (parts.length != 8) {
            System.out.println("Update format must be: itemId, description, packageType, unit, unitsInPack, packageCost, availability, stockLimit");
            return;
        }

        CatalogueItem item = new CatalogueItem(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                Integer.parseInt(parts[4].trim()),
                Double.parseDouble(parts[5].trim()),
                Integer.parseInt(parts[6].trim()),
                Integer.parseInt(parts[7].trim())
        );

        boolean success = service.updateItem(item);
        System.out.println("Update success: " + success);
    }

    private void handleDelete(String input) {
        String itemId = input.trim();
        boolean success = service.deleteItem(itemId);
        System.out.println("Delete success: " + success);
    }
}