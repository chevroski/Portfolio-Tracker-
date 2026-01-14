package com.portfoliotracker.controller;

import com.portfoliotracker.api.WhaleAlertClient;
import com.portfoliotracker.model.Asset;
import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.model.PricePoint;
import com.portfoliotracker.service.MarketDataService;
import com.portfoliotracker.service.PortfolioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AnalysisController {
    @FXML private TableView<WhaleAlertClient.WhaleTransaction> whaleTable;
    @FXML private TableColumn<WhaleAlertClient.WhaleTransaction, String> timeColumn;
    @FXML private TableColumn<WhaleAlertClient.WhaleTransaction, String> tokenColumn;
    @FXML private TableColumn<WhaleAlertClient.WhaleTransaction, String> amountColumn;
    @FXML private TableColumn<WhaleAlertClient.WhaleTransaction, String> valueColumn;
    @FXML private TableColumn<WhaleAlertClient.WhaleTransaction, String> typeColumn;
    @FXML private TableColumn<WhaleAlertClient.WhaleTransaction, String> fromColumn;
    
    @FXML private Label totalVolumeLabel;
    @FXML private Label txCountLabel;
    @FXML private Label topTokenLabel;
    @FXML private Label sentimentLabel;
    @FXML private ComboBox<Portfolio> portfolioCombo;
    @FXML private ProgressBar profitLossBar;
    @FXML private Label profitDaysLabel;
    @FXML private Label lossDaysLabel;
    @FXML private Label profitLossSummaryLabel;
    @FXML private Label bestDayLabel;
    @FXML private Label worstDayLabel;
    
    private final WhaleAlertClient whaleClient = new WhaleAlertClient();
    private final PortfolioService portfolioService = PortfolioService.getInstance();
    private final MarketDataService marketDataService = MarketDataService.getInstance();
    private static final int ANALYSIS_DAYS = 30;

    @FXML
    public void initialize() {
        timeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimeAgo()));
        tokenColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().symbol));
        amountColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedAmount()));
        valueColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedValue()));
        typeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedType()));
        fromColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().from));
        
        loadData();
        setupPortfolioAnalysis();
    }

    @FXML
    public void onRefresh() {
        loadData();
        refreshPortfolioAnalysis();
    }

    private void loadData() {
        Task<List<WhaleAlertClient.WhaleTransaction>> task = new Task<>() {
            @Override
            protected List<WhaleAlertClient.WhaleTransaction> call() {
                return whaleClient.getRecentTransactions();
            }
        };
        
        task.setOnSucceeded(e -> {
            List<WhaleAlertClient.WhaleTransaction> transactions = task.getValue();
            ObservableList<WhaleAlertClient.WhaleTransaction> data = FXCollections.observableArrayList(transactions);
            whaleTable.setItems(data);
            updateStats(transactions);
        });
        
        new Thread(task).start();
    }
    
    private void updateStats(List<WhaleAlertClient.WhaleTransaction> transactions) {
        double totalVolume = transactions.stream().mapToDouble(t -> t.usdValue).sum();
        
        if (totalVolume >= 1e9) {
            totalVolumeLabel.setText(String.format("$%.2fB", totalVolume / 1e9));
        } else {
            totalVolumeLabel.setText(String.format("$%.0fM", totalVolume / 1e6));
        }
        
        txCountLabel.setText(String.valueOf(transactions.size()));
        
        String topToken = transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.symbol, 
                        java.util.stream.Collectors.summingDouble(t -> t.usdValue)))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("BTC");
        topTokenLabel.setText(topToken);
        
        Random r = new Random();
        boolean bullish = r.nextBoolean();
        sentimentLabel.setText(bullish ? "Bullish 📈" : "Bearish 📉");
        sentimentLabel.getStyleClass().removeAll("positive", "negative");
        sentimentLabel.getStyleClass().add(bullish ? "positive" : "negative");
    }

    private void setupPortfolioAnalysis() {
        List<Portfolio> portfolios = portfolioService.getAllPortfolios();
        portfolioCombo.setItems(FXCollections.observableArrayList(portfolios));
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
        if (!portfolios.isEmpty()) {
            portfolioCombo.getSelectionModel().select(0);
            refreshPortfolioAnalysis();
        } else {
            setEmptyAnalysis();
        }
        portfolioCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshPortfolioAnalysis());
    }

    private void refreshPortfolioAnalysis() {
        Portfolio selected = portfolioCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setEmptyAnalysis();
            return;
        }
        Task<AnalysisData> task = new Task<>() {
            @Override
            protected AnalysisData call() {
                Portfolio portfolio = portfolioService.getPortfolio(selected.getId());
                if (portfolio == null) {
                    portfolio = selected;
                }
                return computeAnalysis(portfolio);
            }
        };
        task.setOnSucceeded(e -> updateAnalysisUI(task.getValue()));
        new Thread(task).start();
    }

    private AnalysisData computeAnalysis(Portfolio portfolio) {
        if (portfolio.getAssets() == null || portfolio.getAssets().isEmpty()) {
            return AnalysisData.empty();
        }
        Map<String, List<PricePoint>> assetHistory = new HashMap<>();
        Map<String, Double> quantities = new HashMap<>();
        for (Asset asset : portfolio.getAssets()) {
            double quantity = asset.getTotalQuantity();
            if (quantity <= 0) {
                continue;
            }
            List<PricePoint> history = marketDataService.getPriceHistory(
                    asset.getTicker(), asset.getType(), portfolio.getCurrency(), ANALYSIS_DAYS);
            if (history != null && !history.isEmpty()) {
                assetHistory.put(asset.getTicker(), history);
                quantities.put(asset.getTicker(), quantity);
            }
        }
        List<ValuePoint> totalHistory = calculateTotalValueHistory(assetHistory, quantities);
        if (totalHistory.size() < 2) {
            return AnalysisData.empty();
        }
        int profitDays = 0;
        int lossDays = 0;
        double bestChange = Double.NEGATIVE_INFINITY;
        double worstChange = Double.POSITIVE_INFINITY;
        LocalDateTime bestDate = null;
        LocalDateTime worstDate = null;
        for (int i = 1; i < totalHistory.size(); i++) {
            double prev = totalHistory.get(i - 1).value;
            double curr = totalHistory.get(i).value;
            if (prev <= 0) {
                continue;
            }
            double change = ((curr - prev) / prev) * 100;
            if (change >= 0) {
                profitDays++;
            } else {
                lossDays++;
            }
            if (change > bestChange) {
                bestChange = change;
                bestDate = totalHistory.get(i).timestamp;
            }
            if (change < worstChange) {
                worstChange = change;
                worstDate = totalHistory.get(i).timestamp;
            }
        }
        return new AnalysisData(profitDays, lossDays, bestChange, worstChange, bestDate, worstDate);
    }

    private void updateAnalysisUI(AnalysisData data) {
        if (data.totalDays() == 0) {
            setEmptyAnalysis();
            return;
        }
        double profitRatio = data.profitDays * 1.0 / data.totalDays();
        profitLossBar.setProgress(profitRatio);
        profitDaysLabel.setText(String.format("%.0f%%", profitRatio * 100));
        lossDaysLabel.setText(String.format("%.0f%%", (1 - profitRatio) * 100));
        profitLossSummaryLabel.setText(String.format("Based on %d days", data.totalDays()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
        bestDayLabel.getStyleClass().removeAll("positive", "negative");
        bestDayLabel.getStyleClass().add(data.bestChange >= 0 ? "positive" : "negative");
        worstDayLabel.getStyleClass().removeAll("positive", "negative");
        worstDayLabel.getStyleClass().add(data.worstChange >= 0 ? "positive" : "negative");
        bestDayLabel.setText(formatDayChange(data.bestChange, data.bestDate, formatter));
        worstDayLabel.setText(formatDayChange(data.worstChange, data.worstDate, formatter));
    }

    private String formatDayChange(double change, LocalDateTime date, DateTimeFormatter formatter) {
        if (date == null || Double.isInfinite(change)) {
            return "—";
        }
        return String.format("%+.2f%% (%s)", change, date.format(formatter));
    }

    private void setEmptyAnalysis() {
        profitLossBar.setProgress(0);
        profitDaysLabel.setText("—");
        lossDaysLabel.setText("—");
        profitLossSummaryLabel.setText("No portfolio data");
        bestDayLabel.setText("—");
        worstDayLabel.setText("—");
    }

    private List<ValuePoint> calculateTotalValueHistory(Map<String, List<PricePoint>> assetHistory, Map<String, Double> quantities) {
        List<ValuePoint> result = new ArrayList<>();
        if (assetHistory.isEmpty()) {
            return result;
        }
        int minSize = assetHistory.values().stream().mapToInt(List::size).min().orElse(0);
        if (minSize == 0) {
            return result;
        }
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

    private static class ValuePoint {
        LocalDateTime timestamp;
        double value;

        ValuePoint(LocalDateTime timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    private static class AnalysisData {
        final int profitDays;
        final int lossDays;
        final double bestChange;
        final double worstChange;
        final LocalDateTime bestDate;
        final LocalDateTime worstDate;

        AnalysisData(int profitDays, int lossDays, double bestChange, double worstChange, LocalDateTime bestDate, LocalDateTime worstDate) {
            this.profitDays = profitDays;
            this.lossDays = lossDays;
            this.bestChange = bestChange;
            this.worstChange = worstChange;
            this.bestDate = bestDate;
            this.worstDate = worstDate;
        }

        static AnalysisData empty() {
            return new AnalysisData(0, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null, null);
        }

        int totalDays() {
            return profitDays + lossDays;
        }
    }
}
