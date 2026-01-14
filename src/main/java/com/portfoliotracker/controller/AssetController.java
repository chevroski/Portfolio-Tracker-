package com.portfoliotracker.controller;

import com.portfoliotracker.model.Asset;
import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.model.enums.AssetType;
import com.portfoliotracker.model.enums.TransactionType;
import com.portfoliotracker.service.MarketDataService;
import com.portfoliotracker.service.PortfolioService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AssetController {
    @FXML private TextField tickerField;
    @FXML private TextField nameField;
    @FXML private ComboBox<AssetType> typeCombo;
    @FXML private TextField quantityField;
    @FXML private TextField priceField;
    @FXML private DatePicker datePicker;
    @FXML private TextField feesField;
    @FXML private Button fetchPriceBtn;

    private Portfolio currentPortfolio;
    private Asset currentAsset;
    private boolean editMode = false;
    private final PortfolioService portfolioService = PortfolioService.getInstance();
    private final MarketDataService marketDataService = MarketDataService.getInstance();

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList(AssetType.values()));
        typeCombo.getSelectionModel().select(AssetType.CRYPTO);
        datePicker.setValue(LocalDate.now());
        feesField.setText("0");
        
        tickerField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !tickerField.getText().trim().isEmpty()) {
                fetchCurrentPrice();
            }
        });
    }

    public void setPortfolio(Portfolio portfolio) {
        this.currentPortfolio = portfolio;
    }

    public void setEditMode(Asset asset) {
        this.currentAsset = asset;
        this.editMode = true;
        
        tickerField.setText(asset.getTicker());
        tickerField.setDisable(true);
        nameField.setText(asset.getName());
        typeCombo.getSelectionModel().select(asset.getType());
        typeCombo.setDisable(true);
    }

    @FXML
    public void onFetchPrice() {
        fetchCurrentPrice();
    }
    
    private void fetchCurrentPrice() {
        String ticker = tickerField.getText().trim().toUpperCase();
        if (ticker.isEmpty()) return;
        
        AssetType type = typeCombo.getSelectionModel().getSelectedItem();
        String currency = currentPortfolio != null ? currentPortfolio.getCurrency() : "EUR";
        
        if (fetchPriceBtn != null) {
            fetchPriceBtn.setDisable(true);
            fetchPriceBtn.setText("Loading...");
        }
        
        Task<Double> task = new Task<>() {
            @Override
            protected Double call() {
                return marketDataService.getPrice(ticker, type, currency);
            }
        };
        
        task.setOnSucceeded(e -> {
            double price = task.getValue();
            if (price > 0) {
                priceField.setText(String.format("%.2f", price));
            }
            if (fetchPriceBtn != null) {
                fetchPriceBtn.setDisable(false);
                fetchPriceBtn.setText("Fetch Price");
            }
        });
        
        task.setOnFailed(e -> {
            if (fetchPriceBtn != null) {
                fetchPriceBtn.setDisable(false);
                fetchPriceBtn.setText("Fetch Price");
            }
        });
        
        new Thread(task).start();
    }

    @FXML
    public void onSave() {
        if (!validateInputs()) {
            return;
        }

        String ticker = tickerField.getText().trim().toUpperCase();
        String name = nameField.getText().trim();
        AssetType type = typeCombo.getSelectionModel().getSelectedItem();
        double quantity = parseDouble(quantityField.getText());
        double price = parseDouble(priceField.getText());
        double fees = parseDouble(feesField.getText());
        LocalDateTime date = datePicker.getValue().atStartOfDay();

        if (editMode && currentAsset != null) {
            Transaction transaction = new Transaction(TransactionType.BUY, quantity, price, date, fees, "");
            currentAsset.addTransaction(transaction);
            portfolioService.updatePortfolio(currentPortfolio);
        } else {
            Asset existingAsset = currentPortfolio.getAssetByTicker(ticker);
            if (existingAsset != null) {
                Transaction transaction = new Transaction(TransactionType.BUY, quantity, price, date, fees, "");
                existingAsset.addTransaction(transaction);
            } else {
                Asset newAsset = new Asset(ticker, name.isEmpty() ? ticker : name, type);
                Transaction transaction = new Transaction(TransactionType.BUY, quantity, price, date, fees, "");
                newAsset.addTransaction(transaction);
                currentPortfolio.addAsset(newAsset);
            }
            portfolioService.updatePortfolio(currentPortfolio);
        }

        closeDialog();
    }

    @FXML
    public void onCancel() {
        closeDialog();
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (tickerField.getText().trim().isEmpty()) {
            errors.append("- Ticker is required\n");
        }
        if (typeCombo.getSelectionModel().getSelectedItem() == null) {
            errors.append("- Type is required\n");
        }
        if (quantityField.getText().trim().isEmpty() || parseDouble(quantityField.getText()) <= 0) {
            errors.append("- Quantity must be greater than 0\n");
        }
        if (priceField.getText().trim().isEmpty() || parseDouble(priceField.getText()) <= 0) {
            errors.append("- Price must be greater than 0\n");
        }
        if (datePicker.getValue() == null) {
            errors.append("- Date is required\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please fix the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0;
        value = value.replace(",", ".").replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) tickerField.getScene().getWindow();
        stage.close();
    }
}
