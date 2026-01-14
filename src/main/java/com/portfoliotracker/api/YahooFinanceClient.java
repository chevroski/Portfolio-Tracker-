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
import java.util.ArrayList;
import java.util.List;

public class YahooFinanceClient {
    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private final HttpClient httpClient;
    private final Gson gson;

    public YahooFinanceClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public double getCurrentPrice(String symbol) {
        try {
            String url = BASE_URL + symbol.toUpperCase() + "?interval=1d&range=1d";
            String response = sendRequest(url);
            JsonObject json = gson.fromJson(response, JsonObject.class);
            JsonObject chart = json.getAsJsonObject("chart");
            JsonArray result = chart.getAsJsonArray("result");
            
            if (result != null && result.size() > 0) {
                JsonObject data = result.get(0).getAsJsonObject();
                JsonObject meta = data.getAsJsonObject("meta");
                return meta.get("regularMarketPrice").getAsDouble();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<PricePoint> getPriceHistory(String symbol, int days) {
        List<PricePoint> pricePoints = new ArrayList<>();
        try {
            String range = days <= 7 ? "5d" : days <= 30 ? "1mo" : days <= 90 ? "3mo" : "1y";
            String url = BASE_URL + symbol.toUpperCase() + "?interval=1d&range=" + range;
            String response = sendRequest(url);
            JsonObject json = gson.fromJson(response, JsonObject.class);
            JsonObject chart = json.getAsJsonObject("chart");
            JsonArray result = chart.getAsJsonArray("result");
            
            if (result != null && result.size() > 0) {
                JsonObject data = result.get(0).getAsJsonObject();
                JsonArray timestamps = data.getAsJsonArray("timestamp");
                JsonObject indicators = data.getAsJsonObject("indicators");
                JsonArray quote = indicators.getAsJsonArray("quote");
                JsonArray closes = quote.get(0).getAsJsonObject().getAsJsonArray("close");
                
                for (int i = 0; i < timestamps.size(); i++) {
                    long timestamp = timestamps.get(i).getAsLong();
                    if (!closes.get(i).isJsonNull()) {
                        double price = closes.get(i).getAsDouble();
                        LocalDateTime dateTime = LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
                        pricePoints.add(new PricePoint(dateTime, price));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pricePoints;
    }

    private String sendRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
