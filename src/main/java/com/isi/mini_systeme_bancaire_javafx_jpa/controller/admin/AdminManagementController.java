//package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;
//
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
//import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.AdminService;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.List;
//import java.util.Optional;
//import java.util.ResourceBundle;
//
//public class AdminManagementController implements Initializable {
//
//    @FXML
//    private TextField txtUsername;
//
//    @FXML
//    private PasswordField txtPassword;
//
//    @FXML
//    private ComboBox<String> cbRole;
//
//    @FXML
//    private TableView<Admin> tableAdmins;
//
//    @FXML
//    private TableColumn<Admin, Long> colId;
//
//    @FXML
//    private TableColumn<Admin, String> colUsername;
//
//    @FXML
//    private TableColumn<Admin, String> colRole;
//
//    @FXML
//    private Button btnSave;
//
//    @FXML
//    private Button btnUpdate;
//
//    @FXML
//    private Button btnDelete;
//
//    @FXML
//    private Button btnClear;
//
//    private Admin selectedAdmin;
//    private AdminService adminService;
//    private ObservableList<Admin> adminList = FXCollections.observableArrayList();
//
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        adminService = new AdminServiceImpl();
//
//        // Initialiser les colonnes du tableau
//        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
//        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
//        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
//
//        // Initialiser le combobox des rôles
//        cbRole.setItems(FXCollections.observableArrayList("ROLE_ADMIN", "ROLE_SUPER_ADMIN"));
//        cbRole.setValue("ROLE_ADMIN");
//
//        // Charger les administrateurs
//        loadAdmins();
//
//        // Gérer la sélection d'un administrateur
//        tableAdmins.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                selectedAdmin = newValue;
//                displayAdminDetails();
//            }
//        });
//    }
//
//    private void loadAdmins() {
//        List<Admin> admins = adminService.getAllAdmins();
//        adminList.setAll(admins);
//        tableAdmins.setItems(adminList);
//    }
//
//    private void displayAdminDetails() {
//        if (selectedAdmin != null) {
//            txtUsername.setText(selectedAdmin.getUsername());
//            txtPassword.setText(""); // Ne pas afficher le mot de passe pour des raisons de sécurité
//            cbRole.setValue(selectedAdmin.getRole());
//
//            // Désactiver la modification de l'admin par défaut
//            boolean isDefaultAdmin = "admin".equals(selectedAdmin.getUsername());
//            btnDelete.setDisable(isDefaultAdmin);
//        }
//    }
//
//    @FXML
//    private void handleSave() {
//        String username = txtUsername.getText().trim();
//        String password = txtPassword.getText().trim();
//        String role = cbRole.getValue();
//
//        // Validation des champs
//        if (username.isEmpty()) {
//            Notification.notifWarning("Erreur de saisie", "Le nom d'utilisateur est requis");
//            return;
//        }
//
//        // Créer un nouvel administrateur (avec mot de passe par défaut)
//        Admin admin = adminService.createAdmin(username, "P@sser123", role);
//
//        if (admin != null) {
//            Notification.notifSuccess("Succès", "Administrateur créé avec succès");
//            loadAdmins();
//            clearFields();
//        }
//    }
//
//    @FXML
//    private void handleUpdate() {
//        if (selectedAdmin == null) {
//            Notification.notifWarning("Erreur", "Veuillez sélectionner un administrateur");
//            return;
//        }
//
//        String username = txtUsername.getText().trim();
//        String password = txtPassword.getText().trim();
//        String role = cbRole.getValue();
//
//        // Validation des champs
//        if (username.isEmpty()) {
//            Notification.notifWarning("Erreur de saisie", "Le nom d'utilisateur est requis");
//            return;
//        }
//
//        // Mettre à jour l'administrateur
//        Optional<Admin> updatedAdmin = adminService.updateAdmin(
//                selectedAdmin.getId(),
//                username,
//                password.isEmpty() ? null : password, // Ne pas mettre à jour le mot de passe s'il est vide
//                role
//        );
//
//        if (updatedAdmin.isPresent()) {
//            Notification.notifSuccess("Succès", "Administrateur mis à jour avec succès");
//            loadAdmins();
//            clearFields();
//        }
//    }
//
//    @FXML
//    private void handleDelete() {
//        if (selectedAdmin == null) {
//            Notification.notifWarning("Erreur", "Veuillez sélectionner un administrateur");
//            return;
//        }
//
//        // Vérifier que l'admin ne se supprime pas lui-même
//        if (selectedAdmin.getId().equals(SessionManager.getCurrentAdmin().getId())) {
//            Notification.notifWarning("Erreur", "Vous ne pouvez pas supprimer votre propre compte");
//            return;
//        }
//
//        // Demander confirmation
//        boolean confirmed = Notification.confirmDelete();
//        if (!confirmed) {
//            return;
//        }
//
//        // Supprimer l'administrateur
//        boolean success = adminService.deleteAdmin(selectedAdmin.getId());
//        if (success) {
//            Notification.notifSuccess("Succès", "Administrateur supprimé avec succès");
//            loadAdmins();
//            clearFields();
//        }
//    }
//
//    @FXML
//    private void handleClear() {
//        clearFields();
//    }
//
//    private void clearFields() {
//        txtUsername.clear();
//        txtPassword.clear();
//        cbRole.setValue("ROLE_ADMIN");
//        selectedAdmin = null;
//        tableAdmins.getSelectionModel().clearSelection();
//    }
//
//    @FXML
//    private void handleRetour(ActionEvent event) {
//        try {
//            Outils.load(event, "Administration - Tableau de bord", "/fxml/admin/dashboard.fxml");
//        } catch (IOException e) {
//            Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
//        }
//    }
//}