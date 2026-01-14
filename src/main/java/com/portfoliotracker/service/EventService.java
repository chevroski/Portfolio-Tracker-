package com.portfoliotracker.service;

import com.portfoliotracker.model.Event;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class EventService {
    private static EventService instance;
    private List<Event> events = new ArrayList<>();
    private static final Path DATA_FILE = Paths.get("data", "events.json");
    private final Gson gson;

    private EventService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        load();
    }

    public static EventService getInstance() {
        if (instance == null) {
            instance = new EventService();
        }
        return instance;
    }

    public void addEvent(Event event) {
        events.add(event);
        save();
    }

    public void removeEvent(String eventId) {
        events.removeIf(e -> e.getId().equals(eventId));
        save();
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    public List<Event> getEventsForPortfolio(String portfolioId) {
        return events.stream()
                .filter(e -> e.isGlobal() || portfolioId.equals(e.getPortfolioId()))
                .sorted(Comparator.comparing(Event::getDate))
                .collect(Collectors.toList());
    }

    public List<Event> getGlobalEvents() {
        return events.stream()
                .filter(Event::isGlobal)
                .collect(Collectors.toList());
    }

    private void load() {
        try {
            if (Files.exists(DATA_FILE)) {
                String json = Files.readString(DATA_FILE);
                Type listType = new TypeToken<List<Event>>(){}.getType();
                List<Event> loaded = gson.fromJson(json, listType);
                if (loaded != null) {
                    events = loaded;
                }
            }
        } catch (Exception e) {
            events = new ArrayList<>();
        }
    }

    private void save() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            Files.writeString(DATA_FILE, gson.toJson(events));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type type, JsonSerializationContext ctx) {
            return new JsonPrimitive(src.format(FORMATTER));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext ctx) {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        }
    }
}
