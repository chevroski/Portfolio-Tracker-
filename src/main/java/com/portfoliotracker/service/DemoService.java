package com.portfoliotracker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.portfoliotracker.model.Event;
import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.util.LocalDateTimeAdapter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DemoService {
    private static DemoService instance;
    private final Gson gson;
    private final PersistenceService persistenceService;
    private final CacheService cacheService;

    private DemoService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.persistenceService = PersistenceService.getInstance();
        this.cacheService = CacheService.getInstance();
    }

    public static DemoService getInstance() {
        if (instance == null) {
            instance = new DemoService();
        }
        return instance;
    }

    public List<Portfolio> loadDemoData() {
        List<Portfolio> portfolios = loadDemoPortfolios();
        loadDemoEvents();
        loadDemoCache();
        
        for (Portfolio portfolio : portfolios) {
            persistenceService.savePortfolio(portfolio);
        }
        
        return portfolios;
    }

    private List<Portfolio> loadDemoPortfolios() {
        List<Portfolio> portfolios = new ArrayList<>();
        try {
            InputStream is = getClass().getResourceAsStream("/demo/demo-portfolios.json");
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("demo/demo-portfolios.json");
            }
            if (is == null) {
                // Fallback: read from data folder
                java.io.File file = new java.io.File("data/demo/demo-portfolios.json");
                if (file.exists()) {
                    is = new java.io.FileInputStream(file);
                }
            }
            
            if (is != null) {
                Type listType = new TypeToken<ArrayList<Portfolio>>(){}.getType();
                portfolios = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), listType);
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return portfolios;
    }

    private void loadDemoEvents() {
        try {
            InputStream is = getClass().getResourceAsStream("/demo/demo-events.json");
            if (is == null) {
                java.io.File file = new java.io.File("data/demo/demo-events.json");
                if (file.exists()) {
                    is = new java.io.FileInputStream(file);
                }
            }
            
            if (is != null) {
                Type listType = new TypeToken<ArrayList<Event>>(){}.getType();
                List<Event> events = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), listType);
                persistenceService.saveEvents(events);
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadDemoCache() {
        try {
            InputStream is = getClass().getResourceAsStream("/demo/demo-cache.json");
            if (is == null) {
                java.io.File file = new java.io.File("data/demo/demo-cache.json");
                if (file.exists()) {
                    is = new java.io.FileInputStream(file);
                }
            }
            
            if (is != null) {
                Map<String, Object> cacheData = gson.fromJson(
                        new InputStreamReader(is, StandardCharsets.UTF_8), Map.class);
                
                Map<String, Map<String, Double>> prices = (Map<String, Map<String, Double>>) cacheData.get("prices");
                LocalDate today = LocalDate.now();
                
                if (prices != null) {
                    for (Map.Entry<String, Map<String, Double>> entry : prices.entrySet()) {
                        String ticker = entry.getKey();
                        Map<String, Double> currencyPrices = entry.getValue();
                        Double eurPrice = currencyPrices.get("EUR");
                        if (eurPrice != null) {
                            cacheService.cachePrice(ticker, today, eurPrice);
                        }
                    }
                }
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDemoLoaded() {
        List<Portfolio> portfolios = persistenceService.loadAllPortfolios();
        return portfolios.stream().anyMatch(p -> p.getId().startsWith("demo-"));
    }
}
