package com.portfoliotracker.controller;

import com.portfoliotracker.service.EncryptionService;
import com.portfoliotracker.service.PersistenceService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class PassphraseController {
    @FXML private PasswordField passphraseField;
    @FXML private Label errorLabel;
    @FXML private CheckBox enableEncryptionCheck;

    private boolean unlocked = false;
    private final EncryptionService encryptionService = EncryptionService.getInstance();

    @FXML
    public void initialize() {
        passphraseField.setOnAction(e -> onUnlock());
        errorLabel.setText("");
    }

    @FXML
    public void onUnlock() {
        String passphrase = passphraseField.getText();
        
        if (passphrase == null || passphrase.trim().isEmpty()) {
            errorLabel.setText("Please enter a passphrase");
            return;
        }
        
        if (passphrase.length() < 4) {
            errorLabel.setText("Passphrase must be at least 4 characters");
            return;
        }
        
        encryptionService.setPassphrase(passphrase);
        encryptionService.setEnabled(enableEncryptionCheck.isSelected());
        
        unlocked = true;
        closeDialog();
    }

    @FXML
    public void onSkip() {
        encryptionService.setEnabled(false);
        encryptionService.setPassphrase(null);
        unlocked = true;
        closeDialog();
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    private void closeDialog() {
        Stage stage = (Stage) passphraseField.getScene().getWindow();
        stage.close();
    }
}
