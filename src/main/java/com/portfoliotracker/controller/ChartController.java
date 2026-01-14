package com.portfoliotracker.controller;

import com.portfoliotracker.model.Asset;
import com.portfoliotracker.model.Event;
import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.model.PricePoint;
import com.portfoliotracker.service.EventService;
import com.portfoliotracker.service.MarketDataService;
import com.portfoliotracker.service.PortfolioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChartController {
    @FXML private ComboBox<Portfolio> portfolioCombo;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private PieChart pieChart;
    
    @FXML private Button btn1W, btn1M, btn3M, btn1Y;
    @FXML private HBox assetSelectorBar;
    
    @FXML private Label totalValueLabel;
    @FXML private Label periodLabel;
    @FXML private Label changeLabel;
    @FXML private Label pnlLabel;
    @FXML private Label profitDaysLabel;
    @FXML private Label chartTitleLabel;
    @FXML private Label chartSubtitle;
    
    @FXML private VBox assetDetailsCard;
    @FXML private Label selectedAssetName;
    @FXML private Label selectedAssetValue;
    @FXML private Label selectedAssetRoi;
    
    @FXML private VBox tooltipBox;
    @FXML private Label tooltipDate;
    @FXML private Label tooltipValue;
    @FXML private HBox loadingBox;

    private ObservableList<Portfolio> portfolios;
    private final MarketDataService marketDataService = MarketDataService.getInstance();
    private final PortfolioService portfolioService = PortfolioService.getInstance();
    private final EventService eventService = EventService.getInstance();
    
    private int currentDays = 30;
    private String selectedAsset = null;
    private ChartData lastChartData = null;
    private Button activeAssetButton = null;
    private boolean compareMode = false;
    
    @FXML private Button compareAllBtn;

    @FXML
    public void initialize() {
        portfolioCombo.setCellFactory(lv -> new ListCell<Portfolio>() {
            @Override
            protected void updateItem(Portfolio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        portfolioCombo.setButtonCell(new ListCell<Portfolio>() {
            @Override
            protected void updateItem(Portfolio item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        
        updatePeriodButtons();
    }

    public void setPortfolios(List<Portfolio> portfolioList) {
        this.portfolios = FXCollections.observableArrayList(portfolioList);
        portfolioCombo.setItems(portfolios);
        if (!portfolios.isEmpty()) {
            portfolioCombo.getSelectionModel().select(0);
            loadChartData();
        }
    }

    @FXML public void onPeriod1W() { currentDays = 7; updatePeriodButtons(); loadChartData(); }
    @FXML public void onPeriod1M() { currentDays = 30; updatePeriodButtons(); loadChartData(); }
    @FXML public void onPeriod3M() { currentDays = 90; updatePeriodButtons(); loadChartData(); }
    @FXML public void onPeriod1Y() { currentDays = 365; updatePeriodButtons(); loadChartData(); }
    
    @FXML
    public void onPortfolioChange() {
        selectedAsset = null;
        loadChartData();
    }

    private void updatePeriodButtons() {
        btn1W.getStyleClass().remove("active");
        btn1M.getStyleClass().remove("active");
        btn3M.getStyleClass().remove("active");
        btn1Y.getStyleClass().remove("active");
        
        String periodText = "";
        switch (currentDays) {
            case 7 -> {
                btn1W.getStyleClass().add("active");
                periodText = "7D CHANGE";
            }
            case 30 -> {
                btn1M.getStyleClass().add("active");
                periodText = "30D CHANGE";
            }
            case 90 -> {
                btn3M.getStyleClass().add("active");
                periodText = "90D CHANGE";
            }
            case 365 -> {
                btn1Y.getStyleClass().add("active");
                periodText = "1Y CHANGE";
            }
        }
        
        if (periodLabel != null && !periodText.isEmpty()) {
            periodLabel.setText(periodText);
        }
    }
    
    private void buildAssetSelector(List<String> assetTickers) {
        assetSelectorBar.getChildren().removeIf(n -> n instanceof Button);
        
        Button allBtn = new Button("📊 Total");
        allBtn.getStyleClass().addAll("asset-btn", "active");
        allBtn.setOnAction(e -> selectAsset(null, allBtn));
        assetSelectorBar.getChildren().add(allBtn);
        activeAssetButton = allBtn;
        
        for (String ticker : assetTickers) {
            Button btn = new Button(ticker);
            btn.getStyleClass().add("asset-btn");
            btn.setOnAction(e -> selectAsset(ticker, btn));
            assetSelectorBar.getChildren().add(btn);
        }
    }
    
    private void selectAsset(String ticker, Button btn) {
        if (activeAssetButton != null) {
            activeAssetButton.getStyleClass().remove("active");
        }
        activeAssetButton = btn;
        btn.getStyleClass().add("active");
        
        selectedAsset = ticker;
        
        if (lastChartData != null) {
            updateLineChart(lastChartData);
            updateAssetDetails(lastChartData);
            highlightPieSlice();
        }
    }

    private void loadChartData() {
        Portfolio selected = portfolioCombo.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Portfolio portfolio = portfolioService.getPortfolio(selected.getId());
        if (portfolio == null) portfolio = selected;
        
        if (portfolio.getAssets() == null || portfolio.getAssets().isEmpty()) {
            lineChart.getData().clear();
            pieChart.getData().clear();
            return;
        }
        
        showLoading(true);
        
        final Portfolio finalPortfolio = portfolio;
        
        Task<ChartData> task = new Task<>() {
            @Override
            protected ChartData call() {
                ChartData data = new ChartData();
                
                try {
                    double totalInvested = 0;
                    String bestAsset = "";
                    double bestPnlPercent = Double.NEGATIVE_INFINITY;
                    
                    for (Asset asset : finalPortfolio.getAssets()) {
                        List<PricePoint> history = marketDataService.getPriceHistory(
                                asset.getTicker(), asset.getType(), finalPortfolio.getCurrency(), currentDays);
                        
                        if (history != null && !history.isEmpty()) {
                            data.assetHistory.put(asset.getTicker(), history);
                            data.quantities.put(asset.getTicker(), asset.getTotalQuantity());
                            data.invested.put(asset.getTicker(), asset.getTotalInvested());
                            data.avgPrices.put(asset.getTicker(), asset.getAverageBuyPrice());
                            totalInvested += asset.getTotalInvested();
                            
                            double currentPrice = history.get(history.size() - 1).getPrice();
                            double value = asset.getTotalQuantity() * currentPrice;
                            data.allocation.put(asset.getTicker(), value);
                            
                            double pnlPercent = asset.getAverageBuyPrice() > 0 ? 
                                ((currentPrice - asset.getAverageBuyPrice()) / asset.getAverageBuyPrice()) * 100 : 0;
                            data.assetRoi.put(asset.getTicker(), pnlPercent);
                            
                            if (pnlPercent > bestPnlPercent) {
                                bestPnlPercent = pnlPercent;
                                bestAsset = asset.getTicker() + " (+" + String.format("%.1f%%", pnlPercent) + ")";
                            }
                        }
                    }
                    
                    data.totalValueHistory = calculateTotalValueHistory(data.assetHistory, data.quantities);
                    data.totalInvested = totalInvested;
                    
                    if (!data.totalValueHistory.isEmpty()) {
                        data.totalValue = data.totalValueHistory.get(data.totalValueHistory.size() - 1).value;
                        data.totalPnL = data.totalValue - totalInvested;
                        
                        // Calculate change over the entire selected period
                        if (data.totalValueHistory.size() >= 2) {
                            double firstValue = data.totalValueHistory.get(0).value;
                            double lastValue = data.totalValueHistory.get(data.totalValueHistory.size() - 1).value;
                            data.change24h = firstValue > 0 ? ((lastValue - firstValue) / firstValue) * 100 : 0;
                        }
                    }
                    
                    data.bestAsset = bestAsset;
                    data.assetTickers = new ArrayList<>(data.assetHistory.keySet());
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                return data;
            }
        };
        
        task.setOnSucceeded(e -> {
            lastChartData = task.getValue();
            buildAssetSelector(lastChartData.assetTickers);
            updateUI(lastChartData);
            showLoading(false);
        });
        
        task.setOnFailed(e -> {
            showLoading(false);
            task.getException().printStackTrace();
        });
        
        new Thread(task).start();
    }
    
    private List<ValuePoint> calculateTotalValueHistory(Map<String, List<PricePoint>> assetHistory, Map<String, Double> quantities) {
        List<ValuePoint> result = new ArrayList<>();
        
        if (assetHistory.isEmpty()) return result;
        
        int minSize = assetHistory.values().stream().mapToInt(List::size).min().orElse(0);
        if (minSize == 0) return result;
        
        List<String> assets = new ArrayList<>(assetHistory.keySet());
        List<PricePoint> firstHistory = assetHistory.get(assets.get(0));
        
        for (int i = 0; i < minSize; i++) {
            double totalValue = 0;
            LocalDateTime timestamp = firstHistory.get(i).getTimestamp();
            
            for (String asset : assets) {
                List<PricePoint> history = assetHistory.get(asset);
                double price = history.get(i).getPrice();
                double qty = quantities.getOrDefault(asset, 0.0);
                totalValue += price * qty;
            }
            
            result.add(new ValuePoint(timestamp, totalValue));
        }
        
        return result;
    }
    
    private void updateUI(ChartData data) {
        totalValueLabel.setText(String.format("€%.2f", data.totalValue));
        
        changeLabel.setText(String.format("%+.2f%%", data.change24h));
        changeLabel.getStyleClass().removeAll("positive", "negative");
        changeLabel.getStyleClass().add(data.change24h >= 0 ? "positive" : "negative");
        
        pnlLabel.setText(String.format("%+.2f€", data.totalPnL));
        pnlLabel.getStyleClass().removeAll("positive", "negative");
        pnlLabel.getStyleClass().add(data.totalPnL >= 0 ? "positive" : "negative");
        
        int profitDays = 0;
        int totalDays = data.totalValueHistory.size();
        if (totalDays > 1) {
            for (int i = 1; i < totalDays; i++) {
                if (data.totalValueHistory.get(i).value > data.totalValueHistory.get(i-1).value) {
                    profitDays++;
                }
            }
        }
        double profitPercent = totalDays > 1 ? (profitDays * 100.0 / (totalDays - 1)) : 0;
        profitDaysLabel.setText(String.format("%.0f%%", profitPercent));
        profitDaysLabel.getStyleClass().removeAll("positive", "negative");
        profitDaysLabel.getStyleClass().add(profitPercent >= 50 ? "positive" : "negative");
        
        updateLineChart(data);
        updatePieChart(data);
        updateAssetDetails(data);
    }
    
    private void updateLineChart(ChartData data) {
        lineChart.getData().clear();
        
        List<ValuePoint> historyToShow;
        String title;
        
        if (selectedAsset != null && data.assetHistory.containsKey(selectedAsset)) {
            List<PricePoint> assetHistory = data.assetHistory.get(selectedAsset);
            double qty = data.quantities.getOrDefault(selectedAsset, 1.0);
            historyToShow = new ArrayList<>();
            for (PricePoint p : assetHistory) {
                historyToShow.add(new ValuePoint(p.getTimestamp(), p.getPrice() * qty));
            }
            title = selectedAsset;
            chartTitleLabel.setText(selectedAsset + " Value");
            chartSubtitle.setText("Showing individual asset");
        } else {
            historyToShow = data.totalValueHistory;
            title = "Total";
            chartTitleLabel.setText("Portfolio Value");
            chartSubtitle.setText("Combined value of all assets");
        }
        
        if (historyToShow.isEmpty()) return;
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(title);
        
        DateTimeFormatter formatter = currentDays <= 7 ? 
            DateTimeFormatter.ofPattern("dd/MM HH:mm") : 
            DateTimeFormatter.ofPattern("dd MMM");
        
        int step = Math.max(1, historyToShow.size() / 25);
        
        for (int i = 0; i < historyToShow.size(); i += step) {
            ValuePoint point = historyToShow.get(i);
            String label = point.timestamp.format(formatter);
            series.getData().add(new XYChart.Data<>(label, point.value));
        }
        
        int lastIdx = historyToShow.size() - 1;
        if (lastIdx > 0 && lastIdx % step != 0) {
            ValuePoint last = historyToShow.get(lastIdx);
            series.getData().add(new XYChart.Data<>(last.timestamp.format(formatter), last.value));
        }
        
        lineChart.getData().add(series);
        
        addEventMarkers(historyToShow, formatter);
    }
    
    private void addEventMarkers(List<ValuePoint> history, DateTimeFormatter formatter) {
        if (history.isEmpty()) return;
        
        Portfolio selected = portfolioCombo.getSelectionModel().getSelectedItem();
        String portfolioId = selected != null ? selected.getId() : null;
        List<Event> events = eventService.getEventsForPortfolio(portfolioId);
        
        if (events.isEmpty()) return;
        
        XYChart.Series<String, Number> eventSeries = new XYChart.Series<>();
        eventSeries.setName("Events");
        
        java.time.LocalDate startDate = history.get(0).timestamp.toLocalDate();
        java.time.LocalDate endDate = history.get(history.size() - 1).timestamp.toLocalDate();
        
        // Create a map of dates to values for precise positioning
        Map<String, Double> dateValueMap = new HashMap<>();
        for (ValuePoint vp : history) {
            String label = vp.timestamp.format(formatter);
            dateValueMap.put(label, vp.value);
        }
        
        for (Event event : events) {
            java.time.LocalDate eventDate = event.getDate().toLocalDate();
            if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
                String label = eventDate.atStartOfDay().format(formatter);
                
                // Find the actual value at this date or nearest value
                Double valueAtDate = dateValueMap.get(label);
                if (valueAtDate == null) {
                    // Find closest date
                    valueAtDate = history.stream()
                        .min((v1, v2) -> {
                            long diff1 = Math.abs(java.time.Duration.between(v1.timestamp, eventDate.atStartOfDay()).toMillis());
                            long diff2 = Math.abs(java.time.Duration.between(v2.timestamp, eventDate.atStartOfDay()).toMillis());
                            return Long.compare(diff1, diff2);
                        })
                        .map(v -> v.value)
                        .orElse(0.0);
                }
                
                XYChart.Data<String, Number> point = new XYChart.Data<>(label, valueAtDate);
                eventSeries.getData().add(point);
                
                // Store event for tooltip
                final Event eventRef = event;
                
                // Apply styling and tooltip when node is created
                point.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        // Larger, more visible marker (Red Dot)
                        newNode.setStyle(
                            "-fx-background-color: #ff4444, white;" +
                            "-fx-background-insets: 0, 2;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-padding: 8px;" +
                            "-fx-cursor: hand;"
                        );
                        
                        // Create detailed tooltip
                        Tooltip tooltip = new Tooltip(eventRef.getTitle());
                        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2c2c2c; -fx-text-fill: white;");
                        Tooltip.install(newNode, tooltip);
                        
                        // Hover effect
                        newNode.setOnMouseEntered(e -> {
                            newNode.setScaleX(1.2);
                            newNode.setScaleY(1.2);
                        });
                        newNode.setOnMouseExited(e -> {
                            newNode.setScaleX(1.0);
                            newNode.setScaleY(1.0);
                        });
                    }
                });
            }
        }
        
        if (!eventSeries.getData().isEmpty()) {
            lineChart.getData().add(eventSeries);
            
            // Hide the connecting line for events (fix for "tangent" bug)
            Node seriesNode = eventSeries.getNode();
            if (seriesNode != null) {
                seriesNode.setStyle("-fx-stroke: transparent;");
            } else {
                eventSeries.nodeProperty().addListener((obs, old, newNode) -> {
                    if (newNode != null) newNode.setStyle("-fx-stroke: transparent;");
                });
            }
        }
    }
    
    private void updatePieChart(ChartData data) {
        pieChart.getData().clear();
        
        double total = data.allocation.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) return;
        
        for (Map.Entry<String, Double> entry : data.allocation.entrySet()) {
            double percent = (entry.getValue() / total) * 100;
            if (percent > 0.5) {
                PieChart.Data slice = new PieChart.Data(
                        entry.getKey() + " " + String.format("%.0f%%", percent),
                        entry.getValue()
                );
                pieChart.getData().add(slice);
            }
        }
        
        for (PieChart.Data slice : pieChart.getData()) {
            String ticker = slice.getName().split(" ")[0];
            
            slice.getNode().setOnMouseEntered(e -> 
                slice.getNode().setStyle("-fx-scale-x: 1.08; -fx-scale-y: 1.08; -fx-cursor: hand;"));
            slice.getNode().setOnMouseExited(e -> {
                if (selectedAsset != null && selectedAsset.equals(ticker)) {
                    slice.getNode().setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
                } else {
                    slice.getNode().setStyle("-fx-opacity: " + (selectedAsset == null ? "1" : "0.6") + ";");
                }
            });
            
            slice.getNode().setOnMouseClicked(e -> {
                for (javafx.scene.Node child : assetSelectorBar.getChildren()) {
                    if (child instanceof Button btn && btn.getText().equals(ticker)) {
                        selectAsset(ticker, btn);
                        break;
                    }
                }
            });
        }
        
        highlightPieSlice();
    }
    
    private void highlightPieSlice() {
        for (PieChart.Data slice : pieChart.getData()) {
            String ticker = slice.getName().split(" ")[0];
            boolean isSelected = selectedAsset != null && selectedAsset.equals(ticker);
            if (isSelected) {
                slice.getNode().setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
            } else {
                slice.getNode().setStyle("-fx-opacity: " + (selectedAsset == null ? "1" : "0.6") + ";");
            }
        }
    }
    
    private void updateAssetDetails(ChartData data) {
        if (selectedAsset != null && data.allocation.containsKey(selectedAsset)) {
            assetDetailsCard.setVisible(true);
            assetDetailsCard.setManaged(true);
            
            selectedAssetName.setText(selectedAsset);
            
            double value = data.allocation.get(selectedAsset);
            selectedAssetValue.setText(String.format("€%.2f", value));
            
            double roi = data.assetRoi.getOrDefault(selectedAsset, 0.0);
            selectedAssetRoi.setText(String.format("%+.1f%%", roi));
            selectedAssetRoi.getStyleClass().removeAll("positive", "negative");
            selectedAssetRoi.getStyleClass().add(roi >= 0 ? "positive" : "negative");
        } else {
            assetDetailsCard.setVisible(false);
            assetDetailsCard.setManaged(false);
        }
    }
    
    private void showLoading(boolean show) {
        if (loadingBox != null) {
            loadingBox.setVisible(show);
            loadingBox.setManaged(show);
        }
    }
    
    private static class ValuePoint {
        LocalDateTime timestamp;
        double value;
        
        ValuePoint(LocalDateTime timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }
    
    private static class ChartData {
        double totalValue = 0;
        double totalPnL = 0;
        double totalInvested = 0;
        double change24h = 0;
        String bestAsset = "";
        List<String> assetTickers = new ArrayList<>();
        List<ValuePoint> totalValueHistory = new ArrayList<>();
        Map<String, Double> allocation = new HashMap<>();
        Map<String, List<PricePoint>> assetHistory = new HashMap<>();
        Map<String, Double> quantities = new HashMap<>();
        Map<String, Double> invested = new HashMap<>();
        Map<String, Double> avgPrices = new HashMap<>();
        Map<String, Double> assetRoi = new HashMap<>();
    }

    @FXML
    public void onAddEvent() {
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Add Event");
        dialog.setHeaderText("Add an event to the chart");
        
        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("BTC Halving");
        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(2);
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        CheckBox globalCheck = new CheckBox("Global event (all portfolios)");
        globalCheck.setSelected(true);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);
        grid.add(globalCheck, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(btn -> {
            if (btn == addType && !titleField.getText().trim().isEmpty()) {
                Portfolio selected = portfolioCombo.getSelectionModel().getSelectedItem();
                String portfolioId = globalCheck.isSelected() ? null : (selected != null ? selected.getId() : null);
                return new Event(titleField.getText().trim(), descField.getText().trim(), 
                        datePicker.getValue().atStartOfDay(), portfolioId);
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(event -> {
            eventService.addEvent(event);
            loadChartData();
        });
    }

    @FXML
    public void onCompareAll() {
        compareMode = !compareMode;
        compareAllBtn.setText(compareMode ? "Single View" : "Compare All");
        
        if (compareMode) {
            loadCompareChart();
        } else {
            loadChartData();
        }
    }

    private void loadCompareChart() {
        if (portfolios == null || portfolios.isEmpty()) return;
        
        lineChart.getData().clear();
        chartTitleLabel.setText("Portfolio Comparison");
        chartSubtitle.setText("Showing all portfolios");
        
        DateTimeFormatter formatter = currentDays <= 7 ? 
            DateTimeFormatter.ofPattern("dd/MM HH:mm") : 
            DateTimeFormatter.ofPattern("dd MMM");
        
        String currency = marketDataService.getReferenceCurrency();
        
        // Calculate real historical data for each portfolio
        for (Portfolio portfolio : portfolios) {
            // Skip empty portfolios
            if (portfolio.getAssets().isEmpty()) continue;
            
            try {
                // Calculate portfolio history using real data
                Map<String, List<PricePoint>> assetHistory = new HashMap<>();
                Map<String, Double> quantities = new HashMap<>();
                
                for (Asset asset : portfolio.getAssets()) {
                    String ticker = asset.getTicker();
                    double quantity = asset.getTotalQuantity();
                    
                    if (quantity > 0) {
                        List<PricePoint> history = marketDataService.getPriceHistory(
                            ticker, asset.getType(), portfolio.getCurrency(), currentDays
                        );
                        
                        if (!history.isEmpty()) {
                            assetHistory.put(ticker, history);
                            quantities.put(ticker, quantity);
                        }
                    }
                }
                
                // Skip if no valid data
                if (assetHistory.isEmpty()) continue;
                
                // Calculate total value history
                List<ValuePoint> valueHistory = calculateTotalValueHistory(assetHistory, quantities);
                
                if (valueHistory.isEmpty()) continue;
                
                // Create series for this portfolio
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(portfolio.getName());
                
                for (ValuePoint vp : valueHistory) {
                    String label = vp.timestamp.format(formatter);
                    series.getData().add(new XYChart.Data<>(label, vp.value));
                }
                
                lineChart.getData().add(series);
                
            } catch (Exception e) {
                System.err.println("Error loading portfolio: " + portfolio.getName());
                e.printStackTrace();
            }
        }
        
        // Update stats to show combined totals
        updateCompareStats();
    }
    
    private void updateCompareStats() {
        // Calculate combined stats for all portfolios
        double totalValue = 0;
        double totalInvested = 0;
        
        for (Portfolio portfolio : portfolios) {
            for (Asset asset : portfolio.getAssets()) {
                totalInvested += asset.getTotalInvested();
                try {
                    double price = marketDataService.getPrice(
                        asset.getTicker(), asset.getType(), portfolio.getCurrency()
                    );
                    totalValue += asset.getTotalQuantity() * price;
                } catch (Exception e) {
                    // Skip on error
                }
            }
        }
        
        double totalPnL = totalValue - totalInvested;
        
        totalValueLabel.setText(String.format("€%.2f", totalValue));
        pnlLabel.setText(String.format("%+.2f€", totalPnL));
        pnlLabel.getStyleClass().removeAll("positive", "negative");
        pnlLabel.getStyleClass().add(totalPnL >= 0 ? "positive" : "negative");
        
        // Clear change label in compare mode
        changeLabel.setText("—");
        profitDaysLabel.setText("—");
    }
}
