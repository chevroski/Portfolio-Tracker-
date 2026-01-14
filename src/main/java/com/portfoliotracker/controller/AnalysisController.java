package com.portfoliotracker.controller;

import com.portfoliotracker.api.WhaleAlertClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
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
    
    private final WhaleAlertClient whaleClient = new WhaleAlertClient();

    @FXML
    public void initialize() {
        timeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimeAgo()));
        tokenColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().symbol));
        amountColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedAmount()));
        valueColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedValue()));
        typeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedType()));
        fromColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().from));
        
        loadData();
    }

    @FXML
    public void onRefresh() {
        loadData();
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
}
