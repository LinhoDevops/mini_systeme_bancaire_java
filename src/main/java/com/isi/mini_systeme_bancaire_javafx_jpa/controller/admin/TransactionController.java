package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.CompteServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CompteService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionController implements Initializable {

    @FXML
    private TabPane tabPane;

    // Onglet dépôt
    @FXML
    private ComboBox<Compte> cbCompteDepot;

    @FXML
    private TextField txtMontantDepot;

    @FXML
    private TableView<Transaction> tableDepots;

    @FXML
    private TableColumn<Transaction, String> colNumeroDepot;

    @FXML
    private TableColumn<Transaction, String> colClientDepot;

    @FXML
    private TableColumn<Transaction, Double> colMontantDepot;

    @FXML
    private TableColumn<Transaction, LocalDateTime> colDateDepot;

    @FXML
    private TableColumn<Transaction, String> colStatutDepot;

    // Onglet retrait
    @FXML
    private ComboBox<Compte> cbCompteRetrait;

    @FXML
    private TextField txtMontantRetrait;

    @FXML
    private TableView<Transaction> tableRetraits;

    @FXML
    private TableColumn<Transaction, String> colNumeroRetrait;

    @FXML
    private TableColumn<Transaction, String> colClientRetrait;

    @FXML
    private TableColumn<Transaction, Double> colMontantRetrait;

    @FXML
    private TableColumn<Transaction, LocalDateTime> colDateRetrait;

    @FXML
    private TableColumn<Transaction, String> colStatutRetrait;

    // Onglet virement
    @FXML
    private ComboBox<Compte> cbCompteSource;

    @FXML
    private ComboBox<Compte> cbCompteDestination;

    @FXML
    private TextField txtMontantVirement;

    @FXML
    private TableView<Transaction> tableVirements;

    @FXML
    private TableColumn<Transaction, String> colCompteSource;

    @FXML
    private TableColumn<Transaction, String> colCompteDestination;

    @FXML
    private TableColumn<Transaction, Double> colMontantVirement;

    @FXML
    private TableColumn<Transaction, LocalDateTime> colDateVirement;

    @FXML
    private TableColumn<Transaction, String> colStatutVirement;

    private CompteService compteService = new CompteServiceImpl();
    private CompteRepository compteRepository = new CompteRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    private ObservableList<Compte> comptesList = FXCollections.observableArrayList();
    private ObservableList<Transaction> depotsList = FXCollections.observableArrayList();
    private ObservableList<Transaction> retraitsList = FXCollections.observableArrayList();
    private ObservableList<Transaction> virementsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
            Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Configurer les combobox des comptes
        configureCompteComboBox(cbCompteDepot);
        configureCompteComboBox(cbCompteRetrait);
        configureCompteComboBox(cbCompteSource);
        configureCompteComboBox(cbCompteDestination);

        // Configurer les colonnes du tableau des dépôts
        configureColonnesTableauDepots();

        // Configurer les colonnes du tableau des retraits
        configureColonnesTableauRetraits();

        // Configurer les colonnes du tableau des virements
        configureColonnesTableauVirements();

        // Charger les comptes
        chargerComptes();

        // Charger les transactions
        chargerTransactions();
    }

    private void configureCompteComboBox(ComboBox<Compte> comboBox) {
        comboBox.setConverter(new javafx.util.StringConverter<Compte>() {
            @Override
            public String toString(Compte compte) {
                if (compte == null) return "";
                String clientInfo = "N/A";
                if (compte.getClient() != null) {
                    clientInfo = compte.getClient().getNom() + " " + compte.getClient().getPrenom();
                }
                return compte.getNumero() + " - " + clientInfo + " - " + compte.getSolde() + " FCFA";
            }

            @Override
            public Compte fromString(String s) {
                return null; // Non utilisé
            }
        });
    }

    private void configureColonnesTableauDepots() {
        colNumeroDepot.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCompte() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getCompte().getNumero()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        colClientDepot.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCompte() != null && cellData.getValue().getCompte().getClient() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getCompte().getClient().getNom() + " " +
                                cellData.getValue().getCompte().getClient().getPrenom()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        colMontantDepot.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateDepot.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatutDepot.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void configureColonnesTableauRetraits() {
        colNumeroRetrait.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCompte() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getCompte().getNumero()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        colClientRetrait.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCompte() != null && cellData.getValue().getCompte().getClient() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getCompte().getClient().getNom() + " " +
                                cellData.getValue().getCompte().getClient().getPrenom()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        colMontantRetrait.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateRetrait.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatutRetrait.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void configureColonnesTableauVirements() {
        colCompteSource.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCompteSource() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getCompteSource().getNumero()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        colCompteDestination.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCompteDestination() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getCompteDestination().getNumero()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });

        colMontantVirement.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateVirement.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatutVirement.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void chargerComptes() {
        try {
            // Récupérer tous les comptes
            List<Compte> comptes = compteRepository.findAll();

            // Mettre à jour la liste observable
            comptesList.clear();
            comptesList.addAll(comptes);

            // Mettre à jour les combobox
            cbCompteDepot.setItems(comptesList);
            cbCompteRetrait.setItems(comptesList);
            cbCompteSource.setItems(comptesList);
            cbCompteDestination.setItems(comptesList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des comptes : " + e.getMessage());
        }
    }

    private void chargerTransactions() {
        try {
            // Récupérer toutes les transactions
            List<Transaction> transactions = transactionRepository.findAll();

            // Filtrer les dépôts
            List<Transaction> depots = transactions.stream()
                    .filter(t -> "DEPOT".equals(t.getType()))
                    .toList();

            // Filtrer les retraits
            List<Transaction> retraits = transactions.stream()
                    .filter(t -> "RETRAIT".equals(t.getType()))
                    .toList();

            // Filtrer les virements
            List<Transaction> virements = transactions.stream()
                    .filter(t -> "VIREMENT".equals(t.getType()))
                    .toList();

            // Mettre à jour les listes observables
            depotsList.clear();
            depotsList.addAll(depots);

            retraitsList.clear();
            retraitsList.addAll(retraits);

            virementsList.clear();
            virementsList.addAll(virements);

            // Mettre à jour les tableaux
            tableDepots.setItems(depotsList);
            tableRetraits.setItems(retraitsList);
            tableVirements.setItems(virementsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des transactions : " + e.getMessage());
        }
    }

    @FXML
    private void handleDepot(ActionEvent event) {
        try {
            // Vérifier qu'un compte est sélectionné
            if (cbCompteDepot.getValue() == null) {
                Notification.notifWarning("Dépôt", "Veuillez sélectionner un compte");
                return;
            }

            // Vérifier que le montant est saisi
            if (txtMontantDepot.getText().isEmpty()) {
                Notification.notifWarning("Dépôt", "Veuillez saisir un montant");
                return;
            }

            // Récupérer les valeurs
            Compte compte = cbCompteDepot.getValue();
            double montant;

            try {
                montant = Double.parseDouble(txtMontantDepot.getText());
            } catch (NumberFormatException e) {
                Notification.notifWarning("Dépôt", "Veuillez saisir un montant valide");
                return;
            }

            // Vérifier que le montant est positif
            if (montant <= 0) {
                Notification.notifWarning("Dépôt", "Le montant doit être supérieur à zéro");
                return;
            }

            // Effectuer le dépôt
            boolean success = compteService.effectuerDepot(compte.getId(), montant);

            if (success) {
                Notification.notifSuccess("Dépôt", "Dépôt effectué avec succès");

                // Réinitialiser les champs
                txtMontantDepot.clear();

                // Recharger les comptes et les transactions
                chargerComptes();
                chargerTransactions();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du dépôt : " + e.getMessage());
        }
    }

    @FXML
    private void handleRetrait(ActionEvent event) {
        try {
            // Vérifier qu'un compte est sélectionné
            if (cbCompteRetrait.getValue() == null) {
                Notification.notifWarning("Retrait", "Veuillez sélectionner un compte");
                return;
            }

            // Vérifier que le montant est saisi
            if (txtMontantRetrait.getText().isEmpty()) {
                Notification.notifWarning("Retrait", "Veuillez saisir un montant");
                return;
            }

            // Récupérer les valeurs
            Compte compte = cbCompteRetrait.getValue();
            double montant;

            try {
                montant = Double.parseDouble(txtMontantRetrait.getText());
            } catch (NumberFormatException e) {
                Notification.notifWarning("Retrait", "Veuillez saisir un montant valide");
                return;
            }

            // Vérifier que le montant est positif
            if (montant <= 0) {
                Notification.notifWarning("Retrait", "Le montant doit être supérieur à zéro");
                return;
            }

            // Vérifier que le solde est suffisant
            if (compte.getSolde() < montant) {
                Notification.notifWarning("Retrait", "Solde insuffisant");
                return;
            }

            // Effectuer le retrait
            boolean success = compteService.effectuerRetrait(compte.getId(), montant);

            if (success) {
                Notification.notifSuccess("Retrait", "Retrait effectué avec succès");

                // Réinitialiser les champs
                txtMontantRetrait.clear();

                // Recharger les comptes et les transactions
                chargerComptes();
                chargerTransactions();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du retrait : " + e.getMessage());
        }
    }

    @FXML
    private void handleVirement(ActionEvent event) {
        try {
            // Vérifier que les comptes sont sélectionnés
            if (cbCompteSource.getValue() == null) {
                Notification.notifWarning("Virement", "Veuillez sélectionner un compte source");
                return;
            }

            if (cbCompteDestination.getValue() == null) {
                Notification.notifWarning("Virement", "Veuillez sélectionner un compte destination");
                return;
            }

            // Vérifier que les comptes sont différents
            if (cbCompteSource.getValue().getId().equals(cbCompteDestination.getValue().getId())) {
                Notification.notifWarning("Virement", "Les comptes source et destination doivent être différents");
                return;
            }

            // Vérifier que le montant est saisi
            if (txtMontantVirement.getText().isEmpty()) {
                Notification.notifWarning("Virement", "Veuillez saisir un montant");
                return;
            }

            // Récupérer les valeurs
            Compte compteSource = cbCompteSource.getValue();
            Compte compteDestination = cbCompteDestination.getValue();
            double montant;

            try {
                montant = Double.parseDouble(txtMontantVirement.getText());
            } catch (NumberFormatException e) {
                Notification.notifWarning("Virement", "Veuillez saisir un montant valide");
                return;
            }

            // Vérifier que le montant est positif
            if (montant <= 0) {
                Notification.notifWarning("Virement", "Le montant doit être supérieur à zéro");
                return;
            }

            // Vérifier que le solde est suffisant
            if (compteSource.getSolde() < montant) {
                Notification.notifWarning("Virement", "Solde insuffisant");
                return;
            }

            // Effectuer le virement
            boolean success = compteService.effectuerVirement(compteSource.getId(), compteDestination.getId(), montant);

            if (success) {
                Notification.notifSuccess("Virement", "Virement effectué avec succès");

                // Réinitialiser les champs
                txtMontantVirement.clear();

                // Recharger les comptes et les transactions
                chargerComptes();
                chargerTransactions();
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du virement : " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Outils.load(event, "Tableau de bord", "/fxml/admin/dashboard.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
    }

    @FXML
    private void handleGestionComptes(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des comptes", "/fxml/admin/compte.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des comptes : " + e.getMessage());
        }
    }
}