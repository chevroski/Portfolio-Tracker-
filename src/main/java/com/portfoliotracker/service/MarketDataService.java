package com.portfoliotracker.service;

import com.portfoliotracker.api.CoinGeckoClient;
import com.portfoliotracker.api.ExchangeRateClient;
import com.portfoliotracker.api.YahooFinanceClient;
import com.portfoliotracker.model.PricePoint;
import com.portfoliotracker.model.enums.AssetType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataService {
    private static MarketDataService instance;
    private final CoinGeckoClient coinGeckoClient;
    private final YahooFinanceClient yahooClient;
    private final ExchangeRateClient exchangeClient;
    private final CacheService cacheService;
    
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();
    private final Map<String, CachedHistory> historyCache = new ConcurrentHashMap<>();
    
    private static final long PRICE_CACHE_TTL = 60_000;
    private static final long HISTORY_CACHE_TTL = 300_000;
    
    private String referenceCurrency = "EUR";
    
    private static final Map<String, String> TICKER_TO_COINGECKO = new HashMap<>();
    
    static {
        TICKER_TO_COINGECKO.put("BTC", "bitcoin");
        TICKER_TO_COINGECKO.put("ETH", "ethereum");
        TICKER_TO_COINGECKO.put("SOL", "solana");
        TICKER_TO_COINGECKO.put("LTC", "litecoin");
        TICKER_TO_COINGECKO.put("LINK", "chainlink");
        TICKER_TO_COINGECKO.put("ADA", "cardano");
        TICKER_TO_COINGECKO.put("DOT", "polkadot");
        TICKER_TO_COINGECKO.put("XRP", "ripple");
        TICKER_TO_COINGECKO.put("DOGE", "dogecoin");
        TICKER_TO_COINGECKO.put("AVAX", "avalanche-2");
    }

    private MarketDataService() {
        this.coinGeckoClient = new CoinGeckoClient();
        this.yahooClient = new YahooFinanceClient();
        this.exchangeClient = new ExchangeRateClient();
        this.cacheService = CacheService.getInstance();
    }

    public static MarketDataService getInstance() {
        if (instance == null) {
            instance = new MarketDataService();
        }
        return instance;
    }

    public double getPrice(String ticker, AssetType type, String currency) {
        String cacheKey = ticker.toUpperCase() + "_" + currency.toUpperCase();
        
        CachedPrice cached = priceCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.price;
        }
        
        // Only use disk cache for USD prices since disk cache doesn't store currency
        LocalDate today = LocalDate.now();
        if (currency.equalsIgnoreCase("USD")) {
            Optional<Double> diskCached = cacheService.getCachedPrice(ticker, today);
            if (diskCached.isPresent()) {
                priceCache.put(cacheKey, new CachedPrice(diskCached.get()));
                return diskCached.get();
            }
        }

        double price = 0;
        if (type == AssetType.CRYPTO) {
            String coinId = TICKER_TO_COINGECKO.getOrDefault(ticker.toUpperCase(), ticker.toLowerCase());
            price = coinGeckoClient.getCurrentPrice(coinId, currency);
        } else {
            price = yahooClient.getCurrentPrice(ticker);
        }

        if (price > 0) {
            // Only cache USD prices to disk
            if (currency.equalsIgnoreCase("USD")) {
                cacheService.cachePrice(ticker, today, price);
            }
            priceCache.put(cacheKey, new CachedPrice(price));
        }
        return price;
    }

    public List<PricePoint> getPriceHistory(String ticker, AssetType type, String currency, int days) {
        String cacheKey = ticker.toUpperCase() + "_" + currency.toUpperCase() + "_" + days;
        
        CachedHistory cached = historyCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.history;
        }
        
        List<PricePoint> history;
        if (type == AssetType.CRYPTO) {
            String coinId = TICKER_TO_COINGECKO.getOrDefault(ticker.toUpperCase(), ticker.toLowerCase());
            history = coinGeckoClient.getPriceHistory(coinId, currency, days);
        } else {
            history = yahooClient.getPriceHistory(ticker, days);
        }
        
        if (history != null && !history.isEmpty()) {
            historyCache.put(cacheKey, new CachedHistory(history));
        }
        
        return history;
    }

    public double convertCurrency(double amount, String from, String to) {
        return exchangeClient.convert(amount, from, to);
    }

    public String getCoinGeckoId(String ticker) {
        return TICKER_TO_COINGECKO.getOrDefault(ticker.toUpperCase(), ticker.toLowerCase());
    }
    
    public void clearCache() {
        priceCache.clear();
        historyCache.clear();
    }
    
    private static class CachedPrice {
        final double price;
        final long timestamp;
        
        CachedPrice(double price) {
            this.price = price;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > PRICE_CACHE_TTL;
        }
    }
    
    private static class CachedHistory {
        final List<PricePoint> history;
        final long timestamp;
        
        CachedHistory(List<PricePoint> history) {
            this.history = history;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > HISTORY_CACHE_TTL;
        }
    }
    
    public String getReferenceCurrency() {
        return referenceCurrency;
    }
    
    public void setReferenceCurrency(String currency) {
        this.referenceCurrency = currency;
        priceCache.clear();
    }
}

