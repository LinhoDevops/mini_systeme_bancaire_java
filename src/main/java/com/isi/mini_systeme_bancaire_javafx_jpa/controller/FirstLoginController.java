package com.isi.mini_systeme_bancaire_javafx_jpa.controller;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.AdminRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FirstLoginController implements Initializable {

    @FXML
    private Label lblMessage;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private Button btnChange;

    private AdminRepository adminRepository = new AdminRepository();
    private ClientRepository clientRepository = new ClientRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String message = "C'est votre première connexion. Pour des raisons de sécurité, veuillez changer votre mot de passe.";

        if (SessionManager.isAdminLoggedIn()) {
            Admin admin = SessionManager.getCurrentAdmin();
            message = "Bonjour " + admin.getNomComplet() + ". " + message;
        } else if (SessionManager.isClientLoggedIn()) {
            Client client = SessionManager.getCurrentClient();
            message = "Bonjour " + client.getNomComplet() + ". " + message;
        }

        lblMessage.setText(message);
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // Vérifier que les champs ne sont pas vides
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Notification.notifWarning("Changement de mot de passe", "Veuillez remplir tous les champs");
            return;
        }

        // Vérifier que les deux mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            Notification.notifWarning("Changement de mot de passe", "Les mots de passe ne correspondent pas");
            return;
        }

        // Vérifier la complexité du mot de passe (exemple simple)
        if (newPassword.length() < 8) {
            Notification.notifWarning("Changement de mot de passe", "Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        try {
            if (SessionManager.isAdminLoggedIn()) {
                // Changer le mot de passe de l'administrateur
                Admin admin = SessionManager.getCurrentAdmin();
                admin.setPassword(newPassword);
                admin.setPremierConnexion(false);
                adminRepository.save(admin);

                // Rediriger vers le tableau de bord administrateur
                Outils.load(event, "Administration - Tableau de bord", "/fxml/admin/dashboard.fxml");

                Notification.notifSuccess("Changement de mot de passe", "Votre mot de passe a été changé avec succès");
            } else if (SessionManager.isClientLoggedIn()) {
                // Changer le mot de passe du client
                Client client = SessionManager.getCurrentClient();
                client.setPassword(newPassword);
                client.setPremierConnexion(false);
                clientRepository.save(client);

                // Rediriger vers le tableau de bord client
                Outils.load(event, "Client - Tableau de bord", "/fxml/client/dashboard.fxml");

                Notification.notifSuccess("Changement de mot de passe", "Votre mot de passe a été changé avec succès");
            } else {
                // Personne n'est connecté, rediriger vers la page de connexion
                Outils.load(event, "Mini Système Bancaire - Connexion", "/fxml/Login.fxml");
            }
        } catch (IOException e) {
            Notification.notifError("Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Déconnecter l'utilisateur et rediriger vers la page de connexion
        SessionManager.logout();
        try {
            Outils.load(event, "Mini Système Bancaire - Connexion", "/fxml/Login.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }
}