package com.portfoliotracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = EncryptionService.getInstance();
    }

    @Test
    void testEncryptDecrypt_roundTrip() {
        String originalText = "Hello Portfolio Tracker!";
        String passphrase = "secretKey123";
        
        byte[] encrypted = encryptionService.encrypt(originalText.getBytes(), passphrase);
        byte[] decrypted = encryptionService.decrypt(encrypted, passphrase);
        
        assertEquals(originalText, new String(decrypted));
    }

    @Test
    void testEncryptDecrypt_withSpecialCharacters() {
        String originalText = "Prix: 1234.56€ - Quantité: 0.5 BTC";
        String passphrase = "myP@ssw0rd!";
        
        byte[] encrypted = encryptionService.encrypt(originalText.getBytes(), passphrase);
        byte[] decrypted = encryptionService.decrypt(encrypted, passphrase);
        
        assertEquals(originalText, new String(decrypted));
    }

    @Test
    void testEncryption_changesCiphertext() {
        String originalText = "Test data";
        String passphrase = "key";
        
        byte[] encrypted = encryptionService.encrypt(originalText.getBytes(), passphrase);
        
        assertFalse(originalText.equals(new String(encrypted)));
    }

    @Test
    void testSetPassphrase_enablesEncryption() {
        encryptionService.setPassphrase("testPass");
        assertTrue(encryptionService.isEncryptionEnabled());
        assertEquals("testPass", encryptionService.getPassphrase());
    }

    @Test
    void testEmptyPassphrase_disablesEncryption() {
        encryptionService.setPassphrase("");
        assertFalse(encryptionService.isEncryptionEnabled());
    }

    @Test
    void testEncrypt_withEmptyKey_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            encryptionService.encrypt("data".getBytes(), "");
        });
    }
}
