package com.portfoliotracker.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ExchangeRateClient {
    private static final String BASE_URL = "https://api.exchangerate-api.com/v4/latest/";
    private final HttpClient httpClient;
    private final Gson gson;

    public ExchangeRateClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public double getRate(String from, String to) {
        try {
            String url = BASE_URL + from.toUpperCase();
            String response = sendRequest(url);
            JsonObject json = gson.fromJson(response, JsonObject.class);
            JsonObject rates = json.getAsJsonObject("rates");
            
            if (rates != null && rates.has(to.toUpperCase())) {
                return rates.get(to.toUpperCase()).getAsDouble();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1.0;
    }

    public double convert(double amount, String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return amount;
        }
        double rate = getRate(from, to);
        return amount * rate;
    }

    private String sendRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
