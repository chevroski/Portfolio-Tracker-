package com.portfoliotracker.service;

import com.portfoliotracker.model.Asset;
import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.model.PricePoint;
import com.portfoliotracker.model.Transaction;
import com.portfoliotracker.model.enums.AssetType;
import com.portfoliotracker.model.enums.TransactionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisService {
    private static AnalysisService instance;
    private final MarketDataService marketDataService;

    private AnalysisService() {
        this.marketDataService = MarketDataService.getInstance();
    }

    public static AnalysisService getInstance() {
        if (instance == null) {
            instance = new AnalysisService();
        }
        return instance;
    }

    public double calculateROI(Portfolio portfolio) {
        if (portfolio == null) return 0;

        double totalInvested = 0;
        double currentValue = 0;

        for (Asset asset : portfolio.getAssets()) {
            totalInvested += asset.getTotalInvested();
            double price = marketDataService.getPrice(asset.getTicker(), asset.getType(), portfolio.getCurrency());
            currentValue += asset.getTotalQuantity() * price;
        }

        if (totalInvested == 0) return 0;
        return ((currentValue - totalInvested) / totalInvested) * 100;
    }

    public double calculatePnL(Portfolio portfolio) {
        if (portfolio == null) return 0;

        double totalInvested = 0;
        double currentValue = 0;

        for (Asset asset : portfolio.getAssets()) {
            totalInvested += asset.getTotalInvested();
            double price = marketDataService.getPrice(asset.getTicker(), asset.getType(), portfolio.getCurrency());
            currentValue += asset.getTotalQuantity() * price;
        }

        return currentValue - totalInvested;
    }

    public double calculateRealizedPnL(Portfolio portfolio) {
        if (portfolio == null) return 0;

        double realizedPnL = 0;
        for (Asset asset : portfolio.getAssets()) {
            double avgBuyPrice = asset.getAverageBuyPrice();
            for (Transaction t : asset.getTransactions()) {
                if (t.getType() == TransactionType.SELL) {
                    realizedPnL += (t.getPricePerUnit() - avgBuyPrice) * t.getQuantity() - t.getFees();
                }
            }
        }
        return realizedPnL;
    }

    public double calculateUnrealizedPnL(Portfolio portfolio) {
        if (portfolio == null) return 0;

        double unrealizedPnL = 0;
        for (Asset asset : portfolio.getAssets()) {
            double avgBuyPrice = asset.getAverageBuyPrice();
            double currentPrice = marketDataService.getPrice(asset.getTicker(), asset.getType(), portfolio.getCurrency());
            unrealizedPnL += (currentPrice - avgBuyPrice) * asset.getTotalQuantity();
        }
        return unrealizedPnL;
    }

    public Map<AssetType, Double> getAllocation(Portfolio portfolio) {
        Map<AssetType, Double> allocation = new HashMap<>();
        allocation.put(AssetType.STOCK, 0.0);
        allocation.put(AssetType.CRYPTO, 0.0);

        if (portfolio == null) return allocation;

        double totalValue = 0;
        Map<AssetType, Double> typeValues = new HashMap<>();
        typeValues.put(AssetType.STOCK, 0.0);
        typeValues.put(AssetType.CRYPTO, 0.0);

        for (Asset asset : portfolio.getAssets()) {
            double price = marketDataService.getPrice(asset.getTicker(), asset.getType(), portfolio.getCurrency());
            double value = asset.getTotalQuantity() * price;
            totalValue += value;
            typeValues.put(asset.getType(), typeValues.get(asset.getType()) + value);
        }

        if (totalValue > 0) {
            allocation.put(AssetType.STOCK, (typeValues.get(AssetType.STOCK) / totalValue) * 100);
            allocation.put(AssetType.CRYPTO, (typeValues.get(AssetType.CRYPTO) / totalValue) * 100);
        }

        return allocation;
    }

    public Map<String, Double> getAssetAllocation(Portfolio portfolio) {
        Map<String, Double> allocation = new HashMap<>();

        if (portfolio == null) return allocation;

        double totalValue = 0;
        Map<String, Double> assetValues = new HashMap<>();

        for (Asset asset : portfolio.getAssets()) {
            double price = marketDataService.getPrice(asset.getTicker(), asset.getType(), portfolio.getCurrency());
            double value = asset.getTotalQuantity() * price;
            totalValue += value;
            assetValues.put(asset.getTicker(), value);
        }

        if (totalValue > 0) {
            for (Map.Entry<String, Double> entry : assetValues.entrySet()) {
                allocation.put(entry.getKey(), (entry.getValue() / totalValue) * 100);
            }
        }

        return allocation;
    }

    public int getProfitablePeriods(Portfolio portfolio, int days) {
        if (portfolio == null || portfolio.getAssets().isEmpty()) return 0;

        int profitableDays = 0;
        Asset firstAsset = portfolio.getAssets().get(0);
        List<PricePoint> history = marketDataService.getPriceHistory(
                firstAsset.getTicker(), firstAsset.getType(), portfolio.getCurrency(), days);

        for (int i = 1; i < history.size(); i++) {
            if (history.get(i).getPrice() > history.get(i - 1).getPrice()) {
                profitableDays++;
            }
        }
        return profitableDays;
    }

    public int getDeficitPeriods(Portfolio portfolio, int days) {
        if (portfolio == null || portfolio.getAssets().isEmpty()) return 0;

        int deficitDays = 0;
        Asset firstAsset = portfolio.getAssets().get(0);
        List<PricePoint> history = marketDataService.getPriceHistory(
                firstAsset.getTicker(), firstAsset.getType(), portfolio.getCurrency(), days);

        for (int i = 1; i < history.size(); i++) {
            if (history.get(i).getPrice() < history.get(i - 1).getPrice()) {
                deficitDays++;
            }
        }
        return deficitDays;
    }

    public boolean isProfitable(Portfolio portfolio) {
        return calculatePnL(portfolio) > 0;
    }
}
