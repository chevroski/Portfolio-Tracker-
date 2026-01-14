package com.portfoliotracker.model;

import com.portfoliotracker.model.enums.AssetType;
import com.portfoliotracker.model.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AssetTest {
    private Asset asset;

    @BeforeEach
    void setUp() {
        asset = new Asset("BTC", "Bitcoin", AssetType.CRYPTO);
    }

    @Test
    void testGetTotalQuantity_withBuyAndSell() {
        asset.addTransaction(new Transaction(TransactionType.BUY, 2.0, 50000, LocalDateTime.now(), 10, ""));
        asset.addTransaction(new Transaction(TransactionType.BUY, 1.5, 48000, LocalDateTime.now(), 5, ""));
        asset.addTransaction(new Transaction(TransactionType.SELL, 0.5, 55000, LocalDateTime.now(), 8, ""));
        
        assertEquals(3.0, asset.getTotalQuantity(), 0.001);
    }

    @Test
    void testGetTotalQuantity_withRewards() {
        asset.addTransaction(new Transaction(TransactionType.REWARD, 0.01, 50000, LocalDateTime.now(), 0, ""));
        asset.addTransaction(new Transaction(TransactionType.BUY, 1.0, 50000, LocalDateTime.now(), 10, ""));
        
        assertEquals(1.01, asset.getTotalQuantity(), 0.001);
    }

    @Test
    void testGetAverageBuyPrice() {
        asset.addTransaction(new Transaction(TransactionType.BUY, 1.0, 40000, LocalDateTime.now(), 0, ""));
        asset.addTransaction(new Transaction(TransactionType.BUY, 1.0, 50000, LocalDateTime.now(), 0, ""));
        
        assertEquals(45000, asset.getAverageBuyPrice(), 0.001);
    }

    @Test
    void testGetTotalInvested() {
        asset.addTransaction(new Transaction(TransactionType.BUY, 1.0, 40000, LocalDateTime.now(), 100, ""));
        asset.addTransaction(new Transaction(TransactionType.BUY, 0.5, 50000, LocalDateTime.now(), 50, ""));
        
        double expected = (1.0 * 40000 + 100) + (0.5 * 50000 + 50);
        assertEquals(expected, asset.getTotalInvested(), 0.001);
    }

    @Test
    void testEmptyAsset() {
        assertEquals(0, asset.getTotalQuantity());
        assertEquals(0, asset.getAverageBuyPrice());
        assertEquals(0, asset.getTotalInvested());
    }
}
