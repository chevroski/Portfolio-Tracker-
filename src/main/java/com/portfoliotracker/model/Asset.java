package com.portfoliotracker.model;

import com.portfoliotracker.model.enums.AssetType;
import com.portfoliotracker.model.enums.TransactionType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Asset {
    private String id;
    private String ticker;
    private String name;
    private AssetType type;
    private List<Transaction> transactions;

    public Asset() {
        this.id = UUID.randomUUID().toString();
        this.transactions = new ArrayList<>();
    }

    public Asset(String id, String ticker, String name, AssetType type) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
        this.type = type;
        this.transactions = new ArrayList<>();
    }

    public Asset(String ticker, String name, AssetType type) {
        this.id = UUID.randomUUID().toString();
        this.ticker = ticker;
        this.name = name;
        this.type = type;
        this.transactions = new ArrayList<>();
    }

    public double getTotalQuantity() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.BUY || t.getType() == TransactionType.REWARD) {
                total += t.getQuantity();
            } else if (t.getType() == TransactionType.SELL) {
                total -= t.getQuantity();
            }
        }
        return total;
    }

    public double getAverageBuyPrice() {
        double totalCost = 0;
        double totalQuantity = 0;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.BUY) {
                totalCost += t.getQuantity() * t.getPricePerUnit();
                totalQuantity += t.getQuantity();
            }
        }
        if (totalQuantity == 0) return 0;
        return totalCost / totalQuantity;
    }

    public double getTotalInvested() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.BUY) {
                total += t.getTotalCost();
            }
        }
        return total;
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public void removeTransaction(String transactionId) {
        transactions.removeIf(t -> t.getId().equals(transactionId));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getType() {
        return type;
    }

    public void setType(AssetType type) {
        this.type = type;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
