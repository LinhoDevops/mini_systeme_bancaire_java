package com.isi.mini_systeme_bancaire_javafx_jpa.controller.client;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.CompteServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CompteService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ClientTransactionController implements Initializable {

    @FXML
    private Label lblNomClient;

    @FXML
    private ComboBox<Compte> cbCompteSource;

    @FXML
    private ComboBox<Compte> cbCompteDestination;

    @FXML
    private TextField txtMontant;

    @FXML
    private TextArea txtDescription;

    @FXML
    private Label lblSoldeSource;

    @FXML
    private Label lblSoldeDestination;

    @FXML
    private RadioButton rbDepot;

    @FXML
    private RadioButton rbRetrait;

    @FXML
    private RadioButton rbVirement;

    @FXML
    private VBox panelVirement;

    @FXML
    private Button btnValider;

    private CompteService compteService;
    private CompteRepository compteRepository;
    private Long clientId = 1L; // ID client en dur pour la démonstration

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les services et repositories
        compteService = new CompteServiceImpl();
        compteRepository = new CompteRepository();

        // Groupe de boutons radio pour le type de transaction
        ToggleGroup toggleGroup = new ToggleGroup();
        rbDepot.setToggleGroup(toggleGroup);
        rbRetrait.setToggleGroup(toggleGroup);
        rbVirement.setToggleGroup(toggleGroup);

        // Par défaut, on choisit le dépôt
        rbDepot.setSelected(true);
        panelVirement.setVisible(false);

        // Ajouter un écouteur sur le groupe de boutons
        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbVirement) {
                panelVirement.setVisible(true);
            } else {
                panelVirement.setVisible(false);
            }
        });

        // Mettre à jour le nom du client (simulé)
        lblNomClient.setText("Jean Dupont");

        // Charger des comptes simulés
        loadMockComptes();

        // Écouteurs pour mettre à jour les soldes
        cbCompteSource.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                afficherSoldeCompte(lblSoldeSource, newVal);
            }
        });

        cbCompteDestination.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                afficherSoldeCompte(lblSoldeDestination, newVal);
            }
        });
    }

    private void loadMockComptes() {
        // Créer des comptes factices pour la démonstration
        List<Compte> comptes = new ArrayList<>();

        Compte compte1 = new Compte();
        compte1.setId(1L);
        compte1.setNumero("BQ0001");
        compte1.setType("COURANT");
        compte1.setSolde(100000.0);

        Compte compte2 = new Compte();
        compte2.setId(2L);
        compte2.setNumero("BQ0002");
        compte2.setType("EPARGNE");
        compte2.setSolde(200000.0);

        comptes.add(compte1);
        comptes.add(compte2);

        // Configuration des ComboBox
        cbCompteSource.setItems(FXCollections.observableArrayList(comptes));
        cbCompteSource.setCellFactory(param -> new ListCell<Compte>() {
            @Override
            protected void updateItem(Compte item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNumero() + " - " + item.getType() + " - " +
                            NumberFormat.getCurrencyInstance(Locale.FRANCE).format(item.getSolde()));
                }
            }
        });

        cbCompteDestination.setItems(FXCollections.observableArrayList(comptes));
        cbCompteDestination.setCellFactory(param -> new ListCell<Compte>() {
            @Override
            protected void updateItem(Compte item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNumero() + " - " + item.getType() + " - " +
                            NumberFormat.getCurrencyInstance(Locale.FRANCE).format(item.getSolde()));
                }
            }
        });

        // Sélectionner le premier compte par défaut
        cbCompteSource.getSelectionModel().selectFirst();
        afficherSoldeCompte(lblSoldeSource, cbCompteSource.getValue());
    }

    private void afficherSoldeCompte(Label label, Compte compte) {
        if (compte != null) {
            label.setText(NumberFormat.getCurrencyInstance(Locale.FRANCE).format(compte.getSolde()));
        } else {
            label.setText("0 FCFA");
        }
    }

    @FXML
    private void handleValider(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(txtMontant.getText().trim());
        } catch (NumberFormatException e) {
            Notification.notifWarning("Erreur", "Montant invalide");
            return;
        }

        boolean success = false;

        // Simuler les opérations au lieu d'appeler les services réels
        if (rbDepot.isSelected()) {
            // Simuler un dépôt
            Compte compte = cbCompteSource.getValue();
            compte.setSolde(compte.getSolde() + montant);
            success = true;
        } else if (rbRetrait.isSelected()) {
            // Simuler un retrait
            Compte compte = cbCompteSource.getValue();
            if (compte.getSolde() >= montant) {
                compte.setSolde(compte.getSolde() - montant);
                success = true;
            } else {
                Notification.notifWarning("Erreur", "Solde insuffisant");
                return;
            }
        } else if (rbVirement.isSelected()) {
            // Simuler un virement
            Compte compteSource = cbCompteSource.getValue();
            Compte compteDestination = cbCompteDestination.getValue();

            if (compteSource.getSolde() >= montant) {
                compteSource.setSolde(compteSource.getSolde() - montant);
                compteDestination.setSolde(compteDestination.getSolde() + montant);
                success = true;
            } else {
                Notification.notifWarning("Erreur", "Solde insuffisant");
                return;
            }
        }

        if (success) {
            // Mettre à jour les affichages
            afficherSoldeCompte(lblSoldeSource, cbCompteSource.getValue());
            if (cbCompteDestination.getValue() != null) {
                afficherSoldeCompte(lblSoldeDestination, cbCompteDestination.getValue());
            }

            // Notification de succès
            Notification.notifSuccess("Succès", "Transaction effectuée avec succès");

            // Réinitialiser le formulaire
            txtMontant.clear();
            txtDescription.clear();
        }
    }

    private boolean validateInputs() {
        if (cbCompteSource.getValue() == null) {
            Notification.notifWarning("Erreur", "Veuillez sélectionner un compte source");
            return false;
        }

        if (txtMontant.getText().trim().isEmpty()) {
            Notification.notifWarning("Erreur", "Veuillez saisir un montant");
            return false;
        }

        try {
            double montant = Double.parseDouble(txtMontant.getText().trim());
            if (montant <= 0) {
                Notification.notifWarning("Erreur", "Le montant doit être supérieur à zéro");
                return false;
            }

            if (rbRetrait.isSelected() || rbVirement.isSelected()) {
                if (montant > cbCompteSource.getValue().getSolde()) {
                    Notification.notifWarning("Erreur", "Solde insuffisant");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            Notification.notifWarning("Erreur", "Montant invalide");
            return false;
        }

        if (rbVirement.isSelected() && cbCompteDestination.getValue() == null) {
            Notification.notifWarning("Erreur", "Veuillez sélectionner un compte destinataire");
            return false;
        }

        if (rbVirement.isSelected() && cbCompteSource.getValue().equals(cbCompteDestination.getValue())) {
            Notification.notifWarning("Erreur", "Les comptes source et destination sont identiques");
            return false;
        }

        return true;
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        try {
            Outils.load(event, "Tableau de Bord", "/fxml/client/ClientDashboardView.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de charger le tableau de bord: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCarte(ActionEvent event) {
        try {
            Outils.load(event, "Gestion de Carte Bancaire", "/fxml/client/ClientCarteView.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de charger la page: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCredit(ActionEvent event) {
        try {
            Outils.load(event, "Crédits et Simulations", "/fxml/client/ClientCreditView.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de charger la page: " + ex.getMessage());
        }
    }

    @FXML
    private void handleTicket(ActionEvent event) {
        try {
            Outils.load(event, "Support Client", "/fxml/client/ClientTicketView.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de charger la page: " + ex.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Outils.load(event, "Mini Système Bancaire - Connexion", "/fxml/Login.fxml");
        } catch (IOException ex) {
            Notification.notifError("Erreur", "Impossible de se déconnecter: " + ex.getMessage());
        }
    }
}