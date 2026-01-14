package com.portfoliotracker.model;

import java.time.LocalDateTime;

public class PricePoint {
    private LocalDateTime timestamp;
    private double price;

    public PricePoint() {
    }

    public PricePoint(LocalDateTime timestamp, double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
