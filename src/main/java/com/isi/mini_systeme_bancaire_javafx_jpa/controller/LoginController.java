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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private ImageView imgLogo;

    private AdminRepository adminRepository = new AdminRepository();
    private ClientRepository clientRepository = new ClientRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Code d'initialisation si nécessaire
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        // Récupérer les identifiants saisis
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Vérifier que les champs ne sont pas vides
        if (username.isEmpty() || password.isEmpty()) {
            Notification.notifWarning("Connexion", "Veuillez remplir tous les champs");
            return;
        }

        // Essayer de connecter en tant qu'administrateur
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.getPassword().equals(password)) {
                // Connexion réussie en tant qu'administrateur
                SessionManager.setCurrentAdmin(admin);
                try {
                    Outils.load(event, "Administration - Tableau de bord", "/fxml/admin/dashboard.fxml");
                } catch (IOException e) {
                    Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
                }
                return;
            }
        }

        // Essayer de connecter en tant que client
        Optional<Client> clientOpt = clientRepository.findByEmail(username);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            // Dans une vraie application, le mot de passe serait hashé et vérifié de manière sécurisée
            if (client.getTelephone().equals(password)) { // Utilisation du téléphone comme mot de passe pour la démo
                // Connexion réussie en tant que client
                SessionManager.setCurrentClient(client);
                try {
                    Outils.load(event, "Client - Tableau de bord", "/fxml/client/dashboard.fxml");
                } catch (IOException e) {
                    Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
                }
                return;
            }
        }

        // Si on arrive ici, la connexion a échoué
        Notification.notifError("Connexion", "Identifiants incorrects");
    }
}