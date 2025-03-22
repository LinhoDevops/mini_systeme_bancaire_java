package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.AnalyseOperationsServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.AnalyseOperationsService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AnalyseOperationsController implements Initializable {

    @FXML
    private DatePicker dpDateDebut;

    @FXML
    private DatePicker dpDateFin;

    @FXML
    private Button btnAnalyser;

    @FXML
    private Button btnExportPDF;

    @FXML
    private Button btnExportExcel;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabTransactions;

    @FXML
    private Tab tabRapport;

    @FXML
    private TableView<Transaction> tableTransactions;

    @FXML
    private TableColumn<Transaction, LocalDateTime> colDate;

    @FXML
    private TableColumn<Transaction, String> colType;

    @FXML
    private TableColumn<Transaction, Double> colMontant;

    @FXML
    private TableColumn<Transaction, String> colClient;

    @FXML
    private TableColumn<Transaction, String> colCompte;

    @FXML
    private TableColumn<Transaction, String> colStatut;

    @FXML
    private TextArea txtRapport;

    private AnalyseOperationsService analyseOperationsService;
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        analyseOperationsService = new AnalyseOperationsServiceImpl();

        // Initialiser les dates
        dpDateDebut.setValue(LocalDate.now().minusDays(30));
        dpDateFin.setValue(LocalDate.now());

        // Initialiser les colonnes du tableau
        colDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(formatter));
                }
            }
        });

        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        colMontant.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getMontant()).asObject());

        colClient.setCellValueFactory(cellData -> {
            Transaction transaction = cellData.getValue();
            String client = "";

            if (transaction.getCompte() != null && transaction.getCompte().getClient() != null) {
                client = transaction.getCompte().getClient().getNom() + " " + transaction.getCompte().getClient().getPrenom();
            } else if (transaction.getCompteSource() != null && transaction.getCompteSource().getClient() != null) {
                client = transaction.getCompteSource().getClient().getNom() + " " + transaction.getCompteSource().getClient().getPrenom();
            }

            return new SimpleStringProperty(client);
        });

        colCompte.setCellValueFactory(cellData -> {
            Transaction transaction = cellData.getValue();
            String compte = "";

            if (transaction.getCompte() != null) {
                compte = transaction.getCompte().getNumero();
            } else if (transaction.getCompteSource() != null) {
                compte = transaction.getCompteSource().getNumero();
            }

            return new SimpleStringProperty(compte);
        });

        colStatut.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));

        // Désactiver les boutons d'export initialement
        btnExportPDF.setDisable(true);
        btnExportExcel.setDisable(true);

        // Analyser les transactions automatiquement au démarrage
        handleAnalyser();
    }

    @FXML
    private void handleAnalyser() {
        LocalDate dateDebut = dpDateDebut.getValue();
        LocalDate dateFin = dpDateFin.getValue();

        if (dateDebut == null || dateFin == null) {
            Notification.notifWarning("Erreur", "Veuillez sélectionner des dates valides");
            return;
        }

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(LocalTime.MAX);

        // Récupérer les transactions suspectes
        List<Transaction> transactions = analyseOperationsService.detecterTransactionsSuspectes();
        transactions.removeIf(t -> t.getDate().isBefore(debut) || t.getDate().isAfter(fin));

        // Mettre à jour le tableau
        transactionList.setAll(transactions);
        tableTransactions.setItems(transactionList);

        // Générer le rapport
        String rapport = analyseOperationsService.genererRapportAnalyse(debut, fin);
        txtRapport.setText(rapport);

        // Activer les boutons d'export
        btnExportPDF.setDisable(false);
        btnExportExcel.setDisable(false);

        // Afficher un message de succès
        Notification.notifSuccess("Analyse terminée",
                "L'analyse a identifié " + transactions.size() + " transactions suspectes");
    }

    @FXML
    private void handleExportPDF() {
        LocalDate dateDebut = dpDateDebut.getValue();
        LocalDate dateFin = dpDateFin.getValue();

        if (dateDebut == null || dateFin == null) {
            Notification.notifWarning("Erreur", "Veuillez sélectionner des dates valides");
            return;
        }

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(LocalTime.MAX);

        // Ouvrir un dialogue pour choisir le fichier de destination
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Rapport_Transactions_Suspectes_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");

        Stage stage = (Stage) btnExportPDF.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            boolean success = analyseOperationsService.exporterRapportPDF(debut, fin, file.getAbsolutePath());

            if (success) {
                Notification.notifSuccess("Export réussi",
                        "Le rapport a été exporté avec succès vers " + file.getAbsolutePath());
            } else {
                Notification.notifError("Erreur d'export",
                        "Une erreur est survenue lors de l'export du rapport");
            }
        }
    }

    @FXML
    private void handleExportExcel() {
        LocalDate dateDebut = dpDateDebut.getValue();
        LocalDate dateFin = dpDateFin.getValue();

        if (dateDebut == null || dateFin == null) {
            Notification.notifWarning("Erreur", "Veuillez sélectionner des dates valides");
            return;
        }

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(LocalTime.MAX);

        // Ouvrir un dialogue pour choisir le fichier de destination
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
        fileChooser.setInitialFileName("Rapport_Transactions_Suspectes_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");

        Stage stage = (Stage) btnExportExcel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            boolean success = analyseOperationsService.exporterRapportExcel(debut, fin, file.getAbsolutePath());

            if (success) {
                Notification.notifSuccess("Export réussi",
                        "Le rapport a été exporté avec succès vers " + file.getAbsolutePath());
            } else {
                Notification.notifError("Erreur d'export",
                        "Une erreur est survenue lors de l'export du rapport");
            }
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Outils.load(event, "Administration - Tableau de bord", "/fxml/admin/dashboard.fxml");
        } catch (IOException e) {
            Notification.notifError("Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
    }
}