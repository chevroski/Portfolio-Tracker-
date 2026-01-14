package com.portfoliotracker.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Event {
    private String id;
    private String title;
    private String description;
    private LocalDateTime date;
    private String portfolioId;

    public Event() {
        this.id = UUID.randomUUID().toString();
    }

    public Event(String id, String title, String description, LocalDateTime date, String portfolioId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.portfolioId = portfolioId;
    }

    public Event(String title, String description, LocalDateTime date, String portfolioId) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.date = date;
        this.portfolioId = portfolioId;
    }

    public boolean isGlobal() {
        return portfolioId == null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }
}
