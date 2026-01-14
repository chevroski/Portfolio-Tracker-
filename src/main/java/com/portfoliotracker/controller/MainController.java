package com.portfoliotracker.controller;

import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.service.DemoService;
import com.portfoliotracker.service.MarketDataService;
import com.portfoliotracker.service.PortfolioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {
    @FXML private ListView<Portfolio> portfolioListView;
    @FXML private StackPane contentArea;
    @FXML private Button newPortfolioBtn;
    @FXML private Button importCsvBtn;
    @FXML private Button chartsBtn;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> currencyCombo;

    private final PortfolioService portfolioService = PortfolioService.getInstance();
    private final MarketDataService marketDataService = MarketDataService.getInstance();
    private ObservableList<Portfolio> portfolios;
    private Portfolio currentPortfolio = null;

    @FXML
    public void initialize() {
        portfolios = FXCollections.observableArrayList(portfolioService.getAllPortfolios());
        portfolioListView.setItems(portfolios);
        
        portfolioListView.setCellFactory(lv -> new ListCell<Portfolio>() {
            @Override
            protected void updateItem(Portfolio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        
        portfolioListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentPortfolio = newVal;
                loadPortfolioView(newVal);
            }
        });
        
        currencyCombo.getItems().addAll("EUR", "USD", "GBP", "CHF", "JPY");
        currencyCombo.setValue(marketDataService.getReferenceCurrency());
        currencyCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[CURRENCY] Change triggered: " + oldVal + " -> " + newVal);
            if (newVal != null && !newVal.equals(oldVal)) {
                try {
                    System.out.println("[CURRENCY] Updating reference currency...");
                    marketDataService.setReferenceCurrency(newVal);
                    
                    // Use currentPortfolio instead of ListView selection
                    System.out.println("[CURRENCY] DEBUG - Current portfolio: " + (currentPortfolio != null ? currentPortfolio.getName() : "NULL"));
                    
                    if (currentPortfolio != null) {
                        System.out.println("[CURRENCY] Updating portfolio: " + currentPortfolio.getName());
                        currentPortfolio.setCurrency(newVal);
                        portfolioService.updatePortfolio(currentPortfolio);
                        System.out.println("[CURRENCY] Reloading portfolio view...");
                        loadPortfolioView(currentPortfolio);
                        System.out.println("[CURRENCY] Portfolio updated successfully");
                    } else {
                        System.err.println("[CURRENCY] WARNING - No portfolio currently displayed, skipping currency update");
                    }
                    setStatus("Currency changed to " + newVal);
                    System.out.println("[CURRENCY] Change completed successfully!");
                } catch (Exception e) {
                    System.err.println("[CURRENCY] ERROR during change:");
                    e.printStackTrace();
                    showAlert("Currency Error", "Failed to change currency: " + e.getMessage());
                }
            }
        });
        
        setStatus("Loaded " + portfolios.size() + " portfolios");
    }

    @FXML
    public void onNewPortfolio() {
        Dialog<Portfolio> dialog = new Dialog<>();
        dialog.setTitle("New Portfolio");
        dialog.setHeaderText("Create a new portfolio");
        
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("My Crypto Portfolio");
        TextField descField = new TextField();
        descField.setPromptText("Description (optional)");
        ComboBox<String> currencyCombo = new ComboBox<>();
        currencyCombo.getItems().addAll("EUR", "USD", "GBP", "CHF");
        currencyCombo.setValue("EUR");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Currency:"), 0, 2);
        grid.add(currencyCombo, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    name = "My Portfolio";
                }
                return portfolioService.createPortfolio(name, descField.getText().trim(), currencyCombo.getValue());
            }
            return null;
        });
        
        Optional<Portfolio> result = dialog.showAndWait();
        result.ifPresent(portfolio -> {
            portfolios.add(portfolio);
            portfolioListView.getSelectionModel().select(portfolio);
            setStatus("Created portfolio: " + portfolio.getName());
        });
    }

    @FXML
    public void onImportCSV() {
        Portfolio selected = portfolioListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Portfolio Selected", "Please select a portfolio first to import CSV data.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showOpenDialog(contentArea.getScene().getWindow());
        if (file != null) {
            portfolioService.importFromCoinbaseCSV(selected.getId(), file);
            refreshPortfolios();
            loadPortfolioView(selected);
            setStatus("Imported CSV: " + file.getName());
        }
    }

    @FXML
    public void onShowCharts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chart-view.fxml"));
            Parent chartView = loader.load();
            
            ChartController controller = loader.getController();
            controller.setPortfolios(portfolios);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(chartView);
            setStatus("Showing charts");
            showNotification("Charts", "Displaying portfolio charts");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load chart view: " + e.getMessage());
        }
    }

    @FXML
    public void onRefresh() {
        refreshPortfolios();
        Portfolio selected = portfolioListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadPortfolioView(selected);
        }
        setStatus("Refreshed portfolios");
        showNotification("Refresh", "Portfolio data refreshed");
    }

    @FXML
    public void onShowAnalysis() {
        try {
            System.out.println("[ANALYSIS] Loading analysis-view.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/analysis-view.fxml"));
            System.out.println("[ANALYSIS] FXML resource found: " + loader.getLocation());
            Parent analysisView = loader.load();
            System.out.println("[ANALYSIS] FXML loaded successfully!");
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(analysisView);
            setStatus("Showing analysis");
            showNotification("Analysis", "Displaying whale alerts");
        } catch (Exception e) {
            System.err.println("========== ANALYSIS VIEW ERROR ==========");
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            
            Throwable cause = e.getCause();
            int depth = 0;
            while (cause != null && depth < 5) {
                System.err.println("  Caused by [" + depth + "]: " + cause.getClass().getName());
                System.err.println("  Message: " + cause.getMessage());
                cause = cause.getCause();
                depth++;
            }
            
            System.err.println("Full Stack Trace:");
            e.printStackTrace();
            System.err.println("=========================================");
            
            showAlert("Error", "Could not load analysis view.\nCheck console for details.\n\nError: " + e.getMessage());
        }
    }

    @FXML
    public void onLoadDemo() {
        DemoService demoService = DemoService.getInstance();
        
        if (demoService.isDemoLoaded()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Demo Already Loaded");
            confirm.setHeaderText("Demo data already exists");
            confirm.setContentText("Do you want to reload the demo data?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }
        
        List<Portfolio> demoPortfolios = demoService.loadDemoData();
        
        // Refresh the full list to avoid duplicates
        refreshPortfolios();
        
        if (!portfolios.isEmpty()) {
            portfolioListView.getSelectionModel().select(0);
        }
        
        setStatus("Loaded " + demoPortfolios.size() + " demo portfolios");
        showNotification("Demo Loaded", "Loaded " + demoPortfolios.size() + " demo portfolios with sample data");
    }

    private void loadPortfolioView(Portfolio portfolio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/portfolio-view.fxml"));
            Parent portfolioView = loader.load();
            
            PortfolioController controller = loader.getController();
            controller.setPortfolio(portfolio);
            controller.setMainController(this);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(portfolioView);
            setStatus("Viewing: " + portfolio.getName());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load portfolio view: " + e.getMessage());
        }
    }

    public void refreshPortfolios() {
        portfolios.clear();
        portfolios.addAll(portfolioService.getAllPortfolios());
    }

    public void removePortfolioFromList(Portfolio portfolio) {
        portfolios.removeIf(p -> p.getId().equals(portfolio.getId()));
        contentArea.getChildren().clear();
        showNotification("Deleted", "Portfolio removed successfully");
    }

    public void addPortfolioToList(Portfolio portfolio) {
        portfolios.add(portfolio);
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showNotification(String title, String message) {
        try {
            org.controlsfx.control.Notifications.create()
                    .title(title)
                    .text(message)
                    .darkStyle()
                    .showInformation();
        } catch (Exception e) {
            // Fallback if notifications fail
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void onMinimize() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    public void onMaximize() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onCurrencyChange() {
        String selected = currencyCombo.getValue();
        if (selected != null) {
            marketDataService.setReferenceCurrency(selected);
            setStatus("Currency: " + selected);
            onRefresh();
        }
    }
}
