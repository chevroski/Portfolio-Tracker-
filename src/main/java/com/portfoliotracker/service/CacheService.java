package com.portfoliotracker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CacheService {
    private static CacheService instance;
    private final Gson gson;
    private static final String CACHE_PATH = "data/cache";
    private final Map<String, Map<LocalDate, Double>> memoryCache;

    private CacheService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.memoryCache = new HashMap<>();
        ensureDirectoryExists();
    }

    public static CacheService getInstance() {
        if (instance == null) {
            instance = new CacheService();
        }
        return instance;
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(CACHE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cachePrice(String ticker, LocalDate date, double price) {
        memoryCache.computeIfAbsent(ticker, k -> new HashMap<>()).put(date, price);
        saveCacheToFile(ticker);
    }

    public Optional<Double> getCachedPrice(String ticker, LocalDate date) {
        if (!memoryCache.containsKey(ticker)) {
            loadCacheFromFile(ticker);
        }
        Map<LocalDate, Double> tickerCache = memoryCache.get(ticker);
        if (tickerCache != null && tickerCache.containsKey(date)) {
            return Optional.of(tickerCache.get(date));
        }
        return Optional.empty();
    }

    public boolean isCached(String ticker, LocalDate date) {
        return getCachedPrice(ticker, date).isPresent();
    }

    public void clearCache() {
        memoryCache.clear();
    }

    private void saveCacheToFile(String ticker) {
        String filename = CACHE_PATH + "/" + ticker.toLowerCase() + "_cache.json";
        Map<LocalDate, Double> tickerCache = memoryCache.get(ticker);
        if (tickerCache != null) {
            Map<String, Double> stringKeyMap = new HashMap<>();
            for (Map.Entry<LocalDate, Double> entry : tickerCache.entrySet()) {
                stringKeyMap.put(entry.getKey().toString(), entry.getValue());
            }
            String json = gson.toJson(stringKeyMap);
            try {
                Files.writeString(Paths.get(filename), json, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadCacheFromFile(String ticker) {
        String filename = CACHE_PATH + "/" + ticker.toLowerCase() + "_cache.json";
        Path path = Paths.get(filename);
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                Type mapType = new TypeToken<Map<String, Double>>(){}.getType();
                Map<String, Double> stringKeyMap = gson.fromJson(json, mapType);
                
                Map<LocalDate, Double> tickerCache = new HashMap<>();
                if (stringKeyMap != null) {
                    for (Map.Entry<String, Double> entry : stringKeyMap.entrySet()) {
                        tickerCache.put(LocalDate.parse(entry.getKey()), entry.getValue());
                    }
                }
                memoryCache.put(ticker, tickerCache);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
