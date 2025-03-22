//package com.isi.mini_systeme_bancaire_javafx_jpa.controller;
//
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
//import com.isi.mini_systeme_bancaire_javafx_jpa.repository.AdminRepository;
//import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.PasswordField;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.ResourceBundle;
//
//public class ChangePasswordController implements Initializable {
//
//    @FXML
//    private Label lblWelcome;
//
//    @FXML
//    private PasswordField txtNewPassword;
//
//    @FXML
//    private PasswordField txtConfirmPassword;
//
//    @FXML
//    private Button btnValider;
//
//    private AdminRepository adminRepository = new AdminRepository();
//    private ClientRepository clientRepository = new ClientRepository();
//
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        String welcomeMessage = "Bienvenue! Comme il s'agit de votre première connexion, " +
//                "veuillez changer votre mot de passe.";
//        lblWelcome.setText(welcomeMessage);
//    }
//
//    @FXML
//    private void handleValider(ActionEvent event) {
//        String newPassword = txtNewPassword.getText();
//        String confirmPassword = txtConfirmPassword.getText();
//
//        // Vérifier que les champs ne sont pas vides
//        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
//            Notification.notifWarning("Changement de mot de passe", "Veuillez remplir tous les champs");
//            return;
//        }
//
//        // Vérifier que les mots de passe correspondent
//        if (!newPassword.equals(confirmPassword)) {
//            Notification.notifError("Changement de mot de passe", "Les mots de passe ne correspondent pas");
//            return;
//        }
//
//        // Vérifier la complexité du mot de passe (minimum 8 caractères, au moins une majuscule, un chiffre et un caractère spécial)
//        if (newPassword.length() < 8 ||
//                !newPassword.matches(".*[A-Z].*") ||
//                !newPassword.matches(".*[0-9].*") ||
//                !newPassword.matches(".*[^A-Za-z0-9].*")) {
//            Notification.notifWarning("Changement de mot de passe",
//                    "Le mot de passe doit contenir au moins 8 caractères, " +
//                            "une lettre majuscule, un chiffre et un caractère spécial");
//            return;
//        }
//
//        // Enregistrer le nouveau mot de passe
//        if (SessionManager.isAdminLoggedIn()) {
//            Admin admin = SessionManager.getCurrentAdmin();
//            admin.setPassword(newPassword);
//            admin.setFirstLogin(false);
//            adminRepository.save(admin);
//            Notification.notifSuccess("Changement de mot de passe", "Votre mot de passe a été modifié avec succès");
//
//            try {
//                // Rediriger vers le tableau de bord administrateur
//                Outils.load(event, "Administration - Tableau de bord", "/fxml/admin/dashboard.fxml");
//            } catch (IOException e) {
//                Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
//            }
//        } else if (SessionManager.isClientLoggedIn()) {
//            Client client = SessionManager.getCurrentClient();
//            client.setPassword(newPassword); // Ajouter ce champ au modèle Client
//            client.setFirstLogin(false);
//            clientRepository.save(client);
//            Notification.notifSuccess("Changement de mot de passe", "Votre mot de passe a été modifié avec succès");
//
//            try {
//                // Rediriger vers le tableau de bord client
//                Outils.load(event, "Client - Tableau de bord", "/fxml/client/dashboard.fxml");
//            } catch (IOException e) {
//                Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
//            }
//        } else {
//            Notification.notifError("Erreur", "Aucun utilisateur connecté");
//        }
//    }
//}