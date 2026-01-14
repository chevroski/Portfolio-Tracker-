package com.portfoliotracker.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.portfoliotracker.model.PricePoint;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoinGeckoClient {
    private static final String BINANCE_URL = "https://api.binance.com/api/v3";
    private final HttpClient httpClient;
    private final Gson gson;
    
    private final Map<String, Double> priceCache = new ConcurrentHashMap<>();
    private long lastPriceFetch = 0;
    private static final long PRICE_CACHE_TTL = 30_000;
    
    private static final Map<String, String> NAME_TO_SYMBOL = new HashMap<>();
    private static final Set<String> SUPPORTED_SYMBOLS = new HashSet<>();
    
    static {
        NAME_TO_SYMBOL.put("BTC", "BTCUSDT");
        NAME_TO_SYMBOL.put("ETH", "ETHUSDT");
        NAME_TO_SYMBOL.put("SOL", "SOLUSDT");
        NAME_TO_SYMBOL.put("LTC", "LTCUSDT");
        NAME_TO_SYMBOL.put("LINK", "LINKUSDT");
        NAME_TO_SYMBOL.put("ADA", "ADAUSDT");
        NAME_TO_SYMBOL.put("DOT", "DOTUSDT");
        NAME_TO_SYMBOL.put("XRP", "XRPUSDT");
        NAME_TO_SYMBOL.put("DOGE", "DOGEUSDT");
        NAME_TO_SYMBOL.put("AVAX", "AVAXUSDT");
        NAME_TO_SYMBOL.put("MATIC", "MATICUSDT");
        NAME_TO_SYMBOL.put("ATOM", "ATOMUSDT");
        NAME_TO_SYMBOL.put("UNI", "UNIUSDT");
        NAME_TO_SYMBOL.put("BNB", "BNBUSDT");
        NAME_TO_SYMBOL.put("SHIB", "SHIBUSDT");
        
        NAME_TO_SYMBOL.put("BITCOIN", "BTCUSDT");
        NAME_TO_SYMBOL.put("ETHEREUM", "ETHUSDT");
        NAME_TO_SYMBOL.put("SOLANA", "SOLUSDT");
        NAME_TO_SYMBOL.put("LITECOIN", "LTCUSDT");
        NAME_TO_SYMBOL.put("CHAINLINK", "LINKUSDT");
        NAME_TO_SYMBOL.put("CARDANO", "ADAUSDT");
        NAME_TO_SYMBOL.put("POLKADOT", "DOTUSDT");
        NAME_TO_SYMBOL.put("RIPPLE", "XRPUSDT");
        NAME_TO_SYMBOL.put("DOGECOIN", "DOGEUSDT");
        NAME_TO_SYMBOL.put("AVALANCHE", "AVAXUSDT");
        NAME_TO_SYMBOL.put("POLYGON", "MATICUSDT");
        NAME_TO_SYMBOL.put("COSMOS", "ATOMUSDT");
        NAME_TO_SYMBOL.put("UNISWAP", "UNIUSDT");
        
        SUPPORTED_SYMBOLS.add("BTCUSDT");
        SUPPORTED_SYMBOLS.add("ETHUSDT");
        SUPPORTED_SYMBOLS.add("SOLUSDT");
        SUPPORTED_SYMBOLS.add("LTCUSDT");
        SUPPORTED_SYMBOLS.add("LINKUSDT");
        SUPPORTED_SYMBOLS.add("ADAUSDT");
        SUPPORTED_SYMBOLS.add("DOTUSDT");
        SUPPORTED_SYMBOLS.add("XRPUSDT");
        SUPPORTED_SYMBOLS.add("DOGEUSDT");
        SUPPORTED_SYMBOLS.add("AVAXUSDT");
        SUPPORTED_SYMBOLS.add("MATICUSDT");
        SUPPORTED_SYMBOLS.add("ATOMUSDT");
        SUPPORTED_SYMBOLS.add("UNIUSDT");
        SUPPORTED_SYMBOLS.add("BNBUSDT");
        SUPPORTED_SYMBOLS.add("SHIBUSDT");
    }

    public CoinGeckoClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }
    
    private String toSymbol(String coinId) {
        String upper = coinId.toUpperCase().replace("-", "").replace("_", "");
        if (NAME_TO_SYMBOL.containsKey(upper)) {
            return NAME_TO_SYMBOL.get(upper);
        }
        return upper + "USDT";
    }
    
    private void loadAllPrices() {
        if (System.currentTimeMillis() - lastPriceFetch < PRICE_CACHE_TTL && !priceCache.isEmpty()) {
            return;
        }
        
        try {
            String url = BINANCE_URL + "/ticker/price";
            String response = sendRequest(url);
            
            if (response != null) {
                JsonArray prices = gson.fromJson(response, JsonArray.class);
                int count = 0;
                for (int i = 0; i < prices.size(); i++) {
                    JsonObject item = prices.get(i).getAsJsonObject();
                    String symbol = item.get("symbol").getAsString();
                    if (SUPPORTED_SYMBOLS.contains(symbol)) {
                        double price = Double.parseDouble(item.get("price").getAsString());
                        priceCache.put(symbol, price);
                        count++;
                    }
                }
                lastPriceFetch = System.currentTimeMillis();
                System.out.println("[BINANCE] Loaded " + count + " prices in 1 request");
            }
        } catch (Exception e) {
            System.err.println("Price fetch error: " + e.getMessage());
        }
    }

    public double getCurrentPrice(String coinId, String currency) {
        String symbol = toSymbol(coinId);
        
        loadAllPrices();
        
        double priceUsd = 0;
        
        if (priceCache.containsKey(symbol)) {
            priceUsd = priceCache.get(symbol);
        } else {
            try {
                String url = BINANCE_URL + "/ticker/price?symbol=" + symbol;
                String response = sendRequest(url);
                if (response != null) {
                    JsonObject json = gson.fromJson(response, JsonObject.class);
                    if (json.has("price")) {
                        priceUsd = Double.parseDouble(json.get("price").getAsString());
                        priceCache.put(symbol, priceUsd);
                    }
                }
            } catch (Exception e) {
                System.err.println("Binance error for " + coinId + ": " + e.getMessage());
                return 0;
            }
        }
        
        if (priceUsd == 0) return 0;
        
        if (currency.equalsIgnoreCase("USD")) {
            return priceUsd;
        }
        
        ExchangeRateClient exchangeClient = new ExchangeRateClient();
        return exchangeClient.convert(priceUsd, "USD", currency);
    }

    public List<PricePoint> getPriceHistory(String coinId, String currency, int days) {
        List<PricePoint> pricePoints = new ArrayList<>();
        try {
            String symbol = toSymbol(coinId);
            
            String interval;
            int limit;
            if (days <= 1) {
                interval = "15m";
                limit = 96;
            } else if (days <= 7) {
                interval = "1h";
                limit = days * 24;
            } else if (days <= 30) {
                interval = "4h";
                limit = days * 6;
            } else if (days <= 90) {
                interval = "1d";
                limit = days;
            } else {
                interval = "1d";
                limit = Math.min(days, 365);
            }
            
            String url = BINANCE_URL + "/klines?symbol=" + symbol + "&interval=" + interval + "&limit=" + limit;
            
            String response = sendRequest(url);
            if (response != null) {
                JsonArray data = gson.fromJson(response, JsonArray.class);
                
                double conversionRate = 1.0;
                if (!currency.equalsIgnoreCase("USD")) {
                    ExchangeRateClient exchangeClient = new ExchangeRateClient();
                    conversionRate = exchangeClient.getRate("USD", currency);
                }
                
                for (int i = 0; i < data.size(); i++) {
                    JsonArray candle = data.get(i).getAsJsonArray();
                    long timestamp = candle.get(0).getAsLong();
                    double closePrice = Double.parseDouble(candle.get(4).getAsString());
                    
                    LocalDateTime dateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                    pricePoints.add(new PricePoint(dateTime, closePrice * conversionRate));
                }
                
                System.out.println("[BINANCE] Got " + pricePoints.size() + " candles for " + symbol);
            }
        } catch (Exception e) {
            System.err.println("Binance history error for " + coinId + ": " + e.getMessage());
        }
        return pricePoints;
    }

    public List<String> searchCoin(String query) {
        List<String> results = new ArrayList<>();
        String queryUpper = query.toUpperCase();
        for (String name : NAME_TO_SYMBOL.keySet()) {
            if (name.contains(queryUpper)) {
                results.add(name);
            }
        }
        return results;
    }

    private String sendRequest(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("Binance returned: " + response.statusCode() + " for " + url);
            }
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
        }
        return null;
    }
}
