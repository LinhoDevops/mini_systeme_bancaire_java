package com.isi.mini_systeme_bancaire_javafx_jpa.controller.client;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ClientDashboardController implements Initializable {

    @FXML
    private Label lblNomClient;

    @FXML
    private ComboBox<Compte> cbComptes;

    @FXML
    private Label lblSolde;

    @FXML
    private Label lblNumeroCompte;

    @FXML
    private Label lblTypeCompte;

    @FXML
    private Label lblDateCreation;

    @FXML
    private Label lblNombreTransactions;

    @FXML
    private LineChart<String, Number> chartSolde;

    @FXML
    private TableView<Transaction> tableTransactions;

    @FXML
    private TableColumn<Transaction, LocalDateTime> colDate;

    @FXML
    private TableColumn<Transaction, String> colType;

    @FXML
    private TableColumn<Transaction, Double> colMontant;

    @FXML
    private TableColumn<Transaction, String> colStatut;

    private CompteRepository compteRepository = new CompteRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    private ObservableList<Compte> comptesList = FXCollections.observableArrayList();
    private ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un client est connecté
        if (!SessionManager.isClientLoggedIn()) {
            Notification.notifError("Erreur", "Aucun client connecté");
            return;
        }

        // Récupérer le client connecté
        Client client = SessionManager.getCurrentClient();

        // Afficher le nom du client
        lblNomClient.setText(client.getNom() + " " + client.getPrenom());

        // Configurer le combobox des comptes
        cbComptes.setConverter(new javafx.util.StringConverter<Compte>() {
            @Override
            public String toString(Compte compte) {
                return compte == null ? "" : compte.getNumero() + " (" + compte.getType() + ")";
            }

            @Override
            public Compte fromString(String s) {
                return null; // Non utilisé
            }
        });

        // Configurer les colonnes du tableau
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Charger les comptes du client
        chargerComptes();

        // Ajouter un listener pour le changement de compte
        cbComptes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                afficherDetailsCompte(newValue);
            }
        });
    }

    private void chargerComptes() {
        try {
            // Récupérer tous les comptes du client connecté
            List<Compte> comptes = compteRepository.findByClientId(SessionManager.getCurrentClient().getId());

            // Mettre à jour la liste observable
            comptesList.clear();
            comptesList.addAll(comptes);

            // Mettre à jour le combobox
            cbComptes.setItems(comptesList);

            // Sélectionner le premier compte par défaut
            if (!comptes.isEmpty()) {
                cbComptes.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des comptes : " + e.getMessage());
        }
    }

    private void afficherDetailsCompte(Compte compte) {
        // Afficher les détails du compte
        lblSolde.setText(String.format("%.2f FCFA", compte.getSolde()));
        lblNumeroCompte.setText(compte.getNumero());
        lblTypeCompte.setText(compte.getType());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDateCreation.setText(compte.getDateCreation().format(formatter));

        // Charger les transactions du compte
        chargerTransactions(compte);

        // Mettre à jour le graphique
        mettreAJourGraphique(compte);
    }

    private void chargerTransactions(Compte compte) {
        try {
            // Récupérer les transactions du compte
            List<Transaction> transactions = transactionRepository.findByCompteId(compte.getId());

            // Mettre à jour le nombre de transactions
            lblNombreTransactions.setText(String.valueOf(transactions.size()));

            // Mettre à jour la liste observable
            transactionsList.clear();
            transactionsList.addAll(transactions);

            // Mettre à jour le tableau
            tableTransactions.setItems(transactionsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des transactions : " + e.getMessage());
        }
    }

    private void mettreAJourGraphique(Compte compte) {
        try {
            // Effacer les données précédentes
            chartSolde.getData().clear();

            // Créer une série pour l'évolution du solde
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Évolution du solde");

            // Récupérer les transactions du compte
            List<Transaction> transactions = transactionRepository.findByCompteId(compte.getId());

            // Si aucune transaction, afficher uniquement le solde actuel
            if (transactions.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                series.getData().add(new XYChart.Data<>(LocalDate.now().format(formatter), compte.getSolde()));
            } else {
                // Trier les transactions par date
                transactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

                double solde = 0;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

                for (Transaction transaction : transactions) {
                    if (transaction.getType().equals("DEPOT")) {
                        solde += transaction.getMontant();
                    } else if (transaction.getType().equals("RETRAIT")) {
                        solde -= transaction.getMontant();
                    } else if (transaction.getType().equals("VIREMENT")) {
                        if (transaction.getCompte().getId().equals(compte.getId())) {
                            solde -= transaction.getMontant();
                        } else {
                            solde += transaction.getMontant();
                        }
                    }

                    series.getData().add(new XYChart.Data<>(
                            transaction.getDate().toLocalDate().format(formatter),
                            solde
                    ));
                }
            }

            // Ajouter la série au graphique
            chartSolde.getData().add(series);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la mise à jour du graphique : " + e.getMessage());
        }
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        Notification.notifInfo("Export", "Fonctionnalité d'export PDF à implémenter");
    }

    @FXML
    private void handleTransaction(ActionEvent event) {
        try {
            Outils.load(event, "Transactions", "/fxml/client/client_transaction.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des transactions : " + e.getMessage());
        }
    }

    @FXML
    private void handleCarte(ActionEvent event) {
        try {
            Outils.load(event, "Gestion des cartes", "/fxml/client/client_carte.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement de la gestion des cartes : " + e.getMessage());
        }
    }

    @FXML
    private void handleCredit(ActionEvent event) {
        try {
            Outils.load(event, "Crédits", "/fxml/client/client_credit.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des crédits : " + e.getMessage());
        }
    }

    @FXML
    private void handleTicket(ActionEvent event) {
        try {
            Outils.load(event, "Support client", "/fxml/client/client_ticket.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement du support client : " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Déconnecter l'utilisateur
        SessionManager.logout();

        try {
            Outils.load(event, "Connexion", "/fxml/login.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement de la page de connexion : " + e.getMessage());
        }
    }
}