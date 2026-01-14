package com.portfoliotracker.model;

import com.portfoliotracker.model.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private String id;
    private TransactionType type;
    private double quantity;
    private double pricePerUnit;
    private LocalDateTime date;
    private double fees;
    private String notes;

    public Transaction() {
        this.id = UUID.randomUUID().toString();
    }

    public Transaction(String id, TransactionType type, double quantity, double pricePerUnit, 
                       LocalDateTime date, double fees, String notes) {
        this.id = id;
        this.type = type;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.date = date;
        this.fees = fees;
        this.notes = notes;
    }

    public Transaction(TransactionType type, double quantity, double pricePerUnit, 
                       LocalDateTime date, double fees, String notes) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.date = date;
        this.fees = fees;
        this.notes = notes;
    }

    public double getTotalCost() {
        return (quantity * pricePerUnit) + fees;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
