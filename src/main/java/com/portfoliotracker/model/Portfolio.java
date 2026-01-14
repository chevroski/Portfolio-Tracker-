package com.portfoliotracker.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Portfolio {
    private String id;
    private String name;
    private String description;
    private String currency;
    private LocalDateTime createdAt;
    private List<Asset> assets;

    public Portfolio() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.assets = new ArrayList<>();
    }

    public Portfolio(String id, String name, String description, String currency) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
        this.assets = new ArrayList<>();
    }

    public Portfolio(String name, String description, String currency) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
        this.assets = new ArrayList<>();
    }

    public void addAsset(Asset asset) {
        assets.add(asset);
    }

    public void removeAsset(String assetId) {
        assets.removeIf(a -> a.getId().equals(assetId));
    }

    public Asset getAssetByTicker(String ticker) {
        for (Asset a : assets) {
            if (a.getTicker().equalsIgnoreCase(ticker)) {
                return a;
            }
        }
        return null;
    }

    public Asset getAssetById(String id) {
        for (Asset a : assets) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public Portfolio clone() {
        Portfolio cloned = new Portfolio();
        cloned.setName(this.name + " (Copy)");
        cloned.setDescription(this.description);
        cloned.setCurrency(this.currency);
        
        for (Asset asset : this.assets) {
            Asset clonedAsset = new Asset(asset.getTicker(), asset.getName(), asset.getType());
            for (Transaction t : asset.getTransactions()) {
                Transaction clonedTransaction = new Transaction(
                    t.getType(), t.getQuantity(), t.getPricePerUnit(),
                    t.getDate(), t.getFees(), t.getNotes()
                );
                clonedAsset.addTransaction(clonedTransaction);
            }
            cloned.addAsset(clonedAsset);
        }
        return cloned;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }
}
