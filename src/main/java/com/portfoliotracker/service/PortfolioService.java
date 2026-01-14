package com.portfoliotracker.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.portfoliotracker.model.Asset;
import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.model.enums.AssetType;
import com.portfoliotracker.model.enums.TransactionType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PortfolioService {
    private static PortfolioService instance;
    private final PersistenceService persistenceService;
    private final MarketDataService marketDataService;

    private PortfolioService() {
        this.persistenceService = PersistenceService.getInstance();
        this.marketDataService = MarketDataService.getInstance();
    }

    public static PortfolioService getInstance() {
        if (instance == null) {
            instance = new PortfolioService();
        }
        return instance;
    }

    public Portfolio createPortfolio(String name, String description, String currency) {
        Portfolio portfolio = new Portfolio(name, description, currency);
        persistenceService.savePortfolio(portfolio);
        return portfolio;
    }

    public Portfolio getPortfolio(String id) {
        return persistenceService.loadPortfolio(id);
    }

    public List<Portfolio> getAllPortfolios() {
        return persistenceService.loadAllPortfolios();
    }

    public void updatePortfolio(Portfolio portfolio) {
        persistenceService.savePortfolio(portfolio);
    }

    public void deletePortfolio(String id) {
        persistenceService.deletePortfolio(id);
    }

    public Portfolio clonePortfolio(String id) {
        Portfolio original = getPortfolio(id);
        if (original != null) {
            Portfolio cloned = original.clone();
            persistenceService.savePortfolio(cloned);
            return cloned;
        }
        return null;
    }

    public void addAssetToPortfolio(String portfolioId, Asset asset) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio != null) {
            portfolio.addAsset(asset);
            updatePortfolio(portfolio);
        }
    }

    public void removeAssetFromPortfolio(String portfolioId, String assetId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio != null) {
            portfolio.removeAsset(assetId);
            updatePortfolio(portfolio);
        }
    }

    public void addTransaction(String portfolioId, String assetId, Transaction transaction) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio != null) {
            Asset asset = portfolio.getAssetById(assetId);
            if (asset != null) {
                asset.addTransaction(transaction);
                updatePortfolio(portfolio);
            }
        }
    }

    public double calculatePortfolioValue(String portfolioId, String currency) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) return 0;

        double totalValue = 0;
        for (Asset asset : portfolio.getAssets()) {
            double price = marketDataService.getPrice(asset.getTicker(), asset.getType(), currency);
            totalValue += asset.getTotalQuantity() * price;
        }
        return totalValue;
    }

    public double calculatePortfolioValue(Portfolio portfolio) {
        if (portfolio == null) return 0;

        double totalValue = 0;
        for (Asset asset : portfolio.getAssets()) {
            double price = marketDataService.getPrice(asset.getTicker(), asset.getType(), portfolio.getCurrency());
            totalValue += asset.getTotalQuantity() * price;
        }
        return totalValue;
    }

    public void importFromCoinbaseCSV(String portfolioId, File csvFile) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) return;

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> lines = reader.readAll();
            
            int dataStartLine = 8;
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            
            for (int i = dataStartLine; i < lines.size(); i++) {
                String[] row = lines.get(i);
                if (row.length < 10 || row[0].isEmpty()) continue;
                
                try {
                    String timestamp = row[0];
                    String txType = row[1];
                    String ticker = row[2];
                    double quantity = parseDouble(row[3]);
                    double spotPrice = parseDouble(row[5]);
                    double fees = parseDouble(row[8]);
                    String notes = row.length > 9 ? row[9] : "";
                    
                    LocalDateTime date = LocalDateTime.parse(timestamp, formatter);
                    TransactionType type = parseTransactionType(txType);
                    
                    Asset asset = portfolio.getAssetByTicker(ticker);
                    if (asset == null) {
                        asset = new Asset(ticker, ticker, AssetType.CRYPTO);
                        portfolio.addAsset(asset);
                    }
                    
                    Transaction transaction = new Transaction(type, quantity, spotPrice, date, fees, notes);
                    asset.addTransaction(transaction);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            updatePortfolio(portfolio);
            
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0;
        value = value.trim().replaceAll("[^0-9,.-]", "");
        if (value.contains(",") && value.contains(".")) {
            value = value.replace(",", "");
        } else if (value.contains(",")) {
            value = value.replace(",", ".");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private TransactionType parseTransactionType(String type) {
        if (type == null) return TransactionType.BUY;
        switch (type.toLowerCase()) {
            case "sell": return TransactionType.SELL;
            case "convert": return TransactionType.CONVERT;
            case "reward": 
            case "coinbase earn":
            case "rewards income": return TransactionType.REWARD;
            default: return TransactionType.BUY;
        }
    }
}
