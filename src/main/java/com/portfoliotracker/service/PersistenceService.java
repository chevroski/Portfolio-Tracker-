package com.portfoliotracker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.portfoliotracker.model.Event;
import com.portfoliotracker.model.Portfolio;
import com.portfoliotracker.util.LocalDateTimeAdapter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PersistenceService {
    private static PersistenceService instance;
    private final Gson gson;
    private static final String DATA_PATH = "data";
    private static final String PORTFOLIOS_PATH = DATA_PATH + "/portfolios";
    private static final String EVENTS_PATH = DATA_PATH + "/events";

    private PersistenceService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        ensureDirectoriesExist();
    }

    public static PersistenceService getInstance() {
        if (instance == null) {
            instance = new PersistenceService();
        }
        return instance;
    }

    private void ensureDirectoriesExist() {
        try {
            Files.createDirectories(Paths.get(PORTFOLIOS_PATH));
            Files.createDirectories(Paths.get(EVENTS_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePortfolio(Portfolio portfolio) {
        String filename = PORTFOLIOS_PATH + "/" + portfolio.getId() + ".json";
        String json = gson.toJson(portfolio);
        
        EncryptionService encryption = EncryptionService.getInstance();
        if (encryption.isEncryptionEnabled()) {
            byte[] encrypted = encryption.encrypt(json.getBytes(StandardCharsets.UTF_8), encryption.getPassphrase());
            writeBytes(filename + ".enc", encrypted);
        } else {
            writeString(filename, json);
        }
    }

    public Portfolio loadPortfolio(String id) {
        String filename = PORTFOLIOS_PATH + "/" + id + ".json";
        String encFilename = filename + ".enc";
        
        EncryptionService encryption = EncryptionService.getInstance();
        
        if (Files.exists(Paths.get(encFilename)) && encryption.isEncryptionEnabled()) {
            byte[] encrypted = readBytes(encFilename);
            if (encrypted != null) {
                byte[] decrypted = encryption.decrypt(encrypted, encryption.getPassphrase());
                String json = new String(decrypted, StandardCharsets.UTF_8);
                return gson.fromJson(json, Portfolio.class);
            }
        } else if (Files.exists(Paths.get(filename))) {
            String json = readString(filename);
            if (json != null) {
                return gson.fromJson(json, Portfolio.class);
            }
        }
        return null;
    }

    public List<Portfolio> loadAllPortfolios() {
        List<Portfolio> portfolios = new ArrayList<>();
        File dir = new File(PORTFOLIOS_PATH);
        File[] files = dir.listFiles();
        
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                try {
                    if (name.endsWith(".json") && !name.endsWith(".enc")) {
                        String id = name.replace(".json", "");
                        Portfolio p = loadPortfolio(id);
                        if (p != null) {
                            portfolios.add(p);
                        }
                    } else if (name.endsWith(".json.enc")) {
                        String id = name.replace(".json.enc", "");
                        Portfolio p = loadPortfolio(id);
                        if (p != null) {
                            portfolios.add(p);
                        }
                    }
                } catch (Exception e) {
                    // Skip corrupted files silently
                    System.err.println("Skipping corrupted file: " + name);
                }
            }
        }
        return portfolios;
    }

    public void deletePortfolio(String id) {
        try {
            Files.deleteIfExists(Paths.get(PORTFOLIOS_PATH + "/" + id + ".json"));
            Files.deleteIfExists(Paths.get(PORTFOLIOS_PATH + "/" + id + ".json.enc"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveEvents(List<Event> events) {
        String filename = EVENTS_PATH + "/events.json";
        String json = gson.toJson(events);
        writeString(filename, json);
    }

    public List<Event> loadEvents() {
        String filename = EVENTS_PATH + "/events.json";
        String json = readString(filename);
        if (json != null) {
            Type listType = new TypeToken<ArrayList<Event>>(){}.getType();
            return gson.fromJson(json, listType);
        }
        return new ArrayList<>();
    }

    private void writeString(String filename, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readString(String filename) {
        Path path = Paths.get(filename);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeBytes(String filename, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readBytes(String filename) {
        try {
            return Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
