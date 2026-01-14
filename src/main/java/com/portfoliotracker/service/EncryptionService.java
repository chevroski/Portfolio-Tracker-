package com.portfoliotracker.service;

public class EncryptionService {
    private static EncryptionService instance;
    private String passphrase;
    private boolean enabled;

    private EncryptionService() {
        this.enabled = false;
    }

    public static EncryptionService getInstance() {
        if (instance == null) {
            instance = new EncryptionService();
        }
        return instance;
    }

    public byte[] encrypt(byte[] data, String passphrase) {
        return codeDecode(data, passphrase.getBytes());
    }

    public byte[] decrypt(byte[] data, String passphrase) {
        return codeDecode(data, passphrase.getBytes());
    }

    private byte[] codeDecode(byte[] input, byte[] secret) {
        byte[] output = new byte[input.length];
        if (secret.length == 0) {
            throw new IllegalArgumentException("Empty security key");
        }
        int spos = 0;
        for (int pos = 0; pos < input.length; pos++) {
            output[pos] = (byte) (input[pos] ^ secret[spos]);
            spos++;
            if (spos >= secret.length) {
                spos = 0;
            }
        }
        return output;
    }

    public boolean isEncryptionEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        this.enabled = passphrase != null && !passphrase.isEmpty();
    }
}
