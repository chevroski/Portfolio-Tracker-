package com.portfoliotracker.controller;

import com.portfoliotracker.model.Asset;
import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.service.AnalysisService;
import com.portfoliotracker.service.MarketDataService;
import com.portfoliotracker.service.PortfolioService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PortfolioController {
    @FXML private Label portfolioNameLabel;
    @FXML private Label portfolioDescLabel;
    @FXML private Label portfolioValueLabel;
    @FXML private Label portfolioPnLLabel;
    @FXML private TableView<Asset> assetTableView;
    @FXML private TableColumn<Asset, String> tickerColumn;
    @FXML private TableColumn<Asset, String> nameColumn;
    @FXML private TableColumn<Asset, String> typeColumn;
    @FXML private TableColumn<Asset, String> quantityColumn;
    @FXML private TableColumn<Asset, String> avgPriceColumn;
    @FXML private TableColumn<Asset, String> currentPriceColumn;
    @FXML private TableColumn<Asset, String> valueColumn;
    @FXML private TableColumn<Asset, String> pnlColumn;

    private Portfolio currentPortfolio;
    private MainController mainController;
    private final PortfolioService portfolioService = PortfolioService.getInstance();
    private final MarketDataService marketDataService = MarketDataService.getInstance();
    private final AnalysisService analysisService = AnalysisService.getInstance();
    private ObservableList<Asset> assets;
    
    private final Map<String, Double> priceCache = new HashMap<>();

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        tickerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTicker()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        quantityColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(String.format("%.6f", data.getValue().getTotalQuantity())));
        avgPriceColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(formatCurrency(data.getValue().getAverageBuyPrice())));
        currentPriceColumn.setCellValueFactory(data -> {
            Asset asset = data.getValue();
            Double price = priceCache.getOrDefault(asset.getTicker(), 0.0);
            return new SimpleStringProperty(formatCurrency(price));
        });
        valueColumn.setCellValueFactory(data -> {
            Asset asset = data.getValue();
            Double price = priceCache.getOrDefault(asset.getTicker(), 0.0);
            return new SimpleStringProperty(formatCurrency(asset.getTotalQuantity() * price));
        });
        pnlColumn.setCellValueFactory(data -> {
            Asset asset = data.getValue();
            Double price = priceCache.getOrDefault(asset.getTicker(), 0.0);
            double pnl = (price - asset.getAverageBuyPrice()) * asset.getTotalQuantity();
            return new SimpleStringProperty(formatPnL(pnl));
        });
    }

    public void setPortfolio(Portfolio portfolio) {
        // Use the portfolio parameter directly to preserve in-memory changes (e.g., currency)
        this.currentPortfolio = portfolio;
        
        portfolioNameLabel.setText(currentPortfolio.getName());
        portfolioDescLabel.setText(currentPortfolio.getDescription().isEmpty() ? 
                "Currency: " + currentPortfolio.getCurrency() : currentPortfolio.getDescription());
        
        loadPricesAsync();
    }

    private void loadPricesAsync() {
        Task<Map<String, Double>> task = new Task<>() {
            @Override
            protected Map<String, Double> call() {
                Map<String, Double> prices = new HashMap<>();
                for (Asset asset : currentPortfolio.getAssets()) {
                    try {
                        double price = marketDataService.getPrice(
                                asset.getTicker(), asset.getType(), currentPortfolio.getCurrency());
                        prices.put(asset.getTicker(), price);
                    } catch (Exception e) {
                        prices.put(asset.getTicker(), 0.0);
                    }
                }
                return prices;
            }
        };
        
        task.setOnSucceeded(e -> {
            priceCache.clear();
            priceCache.putAll(task.getValue());
            refreshTable();
            updateSummary();
        });
        
        task.setOnFailed(e -> {
            refreshTable();
            updateSummary();
        });
        
        new Thread(task).start();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void onAddAsset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asset-form.fxml"));
            Parent root = loader.load();
            
            AssetController controller = loader.getController();
            controller.setPortfolio(currentPortfolio);
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Asset");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            
            setPortfolio(currentPortfolio);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onRemoveAsset() {
        Asset selected = assetTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an asset to remove.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove " + selected.getTicker() + "?");
        confirm.setContentText("This will remove the asset and all its transactions.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            portfolioService.removeAssetFromPortfolio(currentPortfolio.getId(), selected.getId());
            setPortfolio(currentPortfolio);
        }
    }

    @FXML
    public void onClonePortfolio() {
        Portfolio cloned = portfolioService.clonePortfolio(currentPortfolio.getId());
        if (cloned != null && mainController != null) {
            mainController.addPortfolioToList(cloned);
            showInfo("Portfolio Cloned", "Created: " + cloned.getName());
        }
    }

    @FXML
    public void onDeletePortfolio() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + currentPortfolio.getName() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            portfolioService.deletePortfolio(currentPortfolio.getId());
            if (mainController != null) {
                mainController.removePortfolioFromList(currentPortfolio);
            }
        }
    }

    public void refreshTable() {
        if (currentPortfolio != null) {
            assets = FXCollections.observableArrayList(currentPortfolio.getAssets());
            assetTableView.setItems(assets);
        }
    }

    private void updateSummary() {
        double totalValue = 0;
        double totalInvested = 0;
        
        for (Asset asset : currentPortfolio.getAssets()) {
            Double price = priceCache.getOrDefault(asset.getTicker(), 0.0);
            totalValue += asset.getTotalQuantity() * price;
            totalInvested += asset.getTotalInvested();
        }
        
        double pnl = totalValue - totalInvested;
        double roi = totalInvested > 0 ? ((totalValue - totalInvested) / totalInvested) * 100 : 0;
        
        String currency = currentPortfolio.getCurrency();
        portfolioValueLabel.setText(formatCurrency(totalValue) + " " + currency);
        portfolioPnLLabel.setText(formatPnL(pnl) + " (" + String.format("%.2f%%", roi) + ")");
        
        if (pnl >= 0) {
            portfolioPnLLabel.getStyleClass().removeAll("portfolio-pnl-negative");
            portfolioPnLLabel.getStyleClass().add("portfolio-pnl-positive");
        } else {
            portfolioPnLLabel.getStyleClass().removeAll("portfolio-pnl-positive");
            portfolioPnLLabel.getStyleClass().add("portfolio-pnl-negative");
        }
    }

    private String formatCurrency(double value) {
        return String.format("%.2f", value);
    }

    private String formatPnL(double value) {
        return value >= 0 ? "+" + formatCurrency(value) : formatCurrency(value);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
