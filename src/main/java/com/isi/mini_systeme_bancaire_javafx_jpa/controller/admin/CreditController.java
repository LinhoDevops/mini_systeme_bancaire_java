package com.isi.mini_systeme_bancaire_javafx_jpa.controller.admin;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Credit;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Remboursement;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CreditRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.RemboursementRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CreditRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CreditResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.impl.CreditServiceImpl;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CreditService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreditController implements Initializable {

    @FXML
    private TabPane tabPane;

    // Onglet gestion des crédits
    @FXML
    private TextField txtMontant;

    @FXML
    private TextField txtTauxInteret;

    @FXML
    private TextField txtDuree;

    @FXML
    private TextField txtMensualite;

    @FXML
    private Button btnCalculerMensualite;

    @FXML
    private DatePicker dpDateDemande;

    @FXML
    private ComboBox<String> cbStatut;

    @FXML
    private ComboBox<Client> cbClient;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnClear;

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Credit> tableCredits;

    @FXML
    private TableColumn<Credit, String> colClient;

    @FXML
    private TableColumn<Credit, Double> colMontant;

    @FXML
    private TableColumn<Credit, Double> colTaux;

    @FXML
    private TableColumn<Credit, Integer> colDuree;

    @FXML
    private TableColumn<Credit, Double> colMensualite;

    @FXML
    private TableColumn<Credit, LocalDate> colDateDemande;

    @FXML
    private TableColumn<Credit, String> colStatut;

    // Onglet remboursements
    @FXML
    private ComboBox<Credit> cbCreditRemboursement;

    @FXML
    private TextField txtMontantRemboursement;

    @FXML
    private DatePicker dpDateRemboursement;

    @FXML
    private Button btnEffectuerRemboursement;

    @FXML
    private TableView<Remboursement> tableRemboursements;

    @FXML
    private TableColumn<Remboursement, Double> colMontantRemboursement;

    @FXML
    private TableColumn<Remboursement, LocalDate> colDateRemboursement;

    // Onglet simulation
    @FXML
    private TextField txtMontantSimulation;

    @FXML
    private TextField txtTauxInteretSimulation;

    @FXML
    private TextField txtDureeSimulation;

    @FXML
    private Button btnCalculerSimulation;

    @FXML
    private Label lblMensualiteSimulation;

    @FXML
    private Label lblMontantTotalSimulation;

    @FXML
    private Label lblCoutTotalSimulation;

    @FXML
    private Label lblTEGSimulation;

    private CreditService creditService = new CreditServiceImpl();
    private CreditRepository creditRepository = new CreditRepository();
    private ClientRepository clientRepository = new ClientRepository();
    private RemboursementRepository remboursementRepository = new RemboursementRepository();

    private ObservableList<Credit> creditsList = FXCollections.observableArrayList();
    private ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private ObservableList<Remboursement> remboursementsList = FXCollections.observableArrayList();
    private Credit selectedCredit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier si un administrateur est connecté
        if (!SessionManager.isAdminLoggedIn()) {
            Notification.notifError("Erreur", "Aucun administrateur connecté");
            return;
        }

        // Initialiser les statuts disponibles
        cbStatut.setItems(FXCollections.observableArrayList("En attente", "Approuvé", "Refusé", "En cours", "Remboursé"));

        // Configurer le combobox des clients
        cbClient.setConverter(new javafx.util.StringConverter<Client>() {
            @Override
            public String toString(Client client) {
                return client == null ? "" : client.getNom() + " " + client.getPrenom() + " (" + client.getEmail() + ")";
            }

            @Override
            public Client fromString(String s) {
                return null; // Non utilisé
            }
        });

        // Configurer le combobox des crédits pour les remboursements
        cbCreditRemboursement.setConverter(new javafx.util.StringConverter<Credit>() {
            @Override
            public String toString(Credit credit) {
                if (credit == null) return "";
                String clientInfo = "N/A";
                if (credit.getClient() != null) {
                    clientInfo = credit.getClient().getNom() + " " + credit.getClient().getPrenom();
                }
                return "Crédit #" + credit.getId() + " - " + clientInfo + " - " + credit.getMontant() + " FCFA";
            }

            @Override
            public Credit fromString(String s) {
                return null; // Non utilisé
            }
        });

        // Configurer les colonnes du tableau des crédits
        colClient.setCellValueFactory(cellData -> {
            if (cellData.getValue().getClient() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getClient().getNom() + " " + cellData.getValue().getClient().getPrenom()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colTaux.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeEnMois"));
        colMensualite.setCellValueFactory(new PropertyValueFactory<>("mensualite"));
        colDateDemande.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configurer les colonnes du tableau des remboursements
        colMontantRemboursement.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDateRemboursement.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Désactiver les boutons de modification et suppression au départ
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        // Ajouter un listener pour la recherche
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercherCredits(newValue);
        });

        // Ajouter un listener pour la sélection d'un crédit dans le combobox des remboursements
        cbCreditRemboursement.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chargerRemboursements(newValue);
            } else {
                remboursementsList.clear();
                tableRemboursements.setItems(remboursementsList);
            }
        });

        // Charger les clients
        chargerClients();

        // Charger les crédits
        chargerCredits();
    }

    private void chargerClients() {
        try {
            // Récupérer tous les clients
            List<Client> clients = clientRepository.findAll();

            // Mettre à jour la liste observable
            clientsList.clear();
            clientsList.addAll(clients);

            // Mettre à jour le combobox
            cbClient.setItems(clientsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des clients : " + e.getMessage());
        }
    }

    private void chargerCredits() {
        try {
            // Récupérer tous les crédits
            List<Credit> credits = creditRepository.findAll();

            // Mettre à jour la liste observable
            creditsList.clear();
            creditsList.addAll(credits);

            // Mettre à jour le tableau
            tableCredits.setItems(creditsList);

            // Mettre à jour le combobox des crédits pour les remboursements
            cbCreditRemboursement.setItems(creditsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des crédits : " + e.getMessage());
        }
    }

    private void rechercherCredits(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                chargerCredits();
                return;
            }

            // Rechercher les crédits
            List<Credit> credits = creditRepository.searchCredits(searchTerm);

            // Mettre à jour la liste observable
            creditsList.clear();
            creditsList.addAll(credits);

            // Mettre à jour le tableau
            tableCredits.setItems(creditsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la recherche des crédits : " + e.getMessage());
        }
    }

    private void chargerRemboursements(Credit credit) {
        try {
            // Récupérer tous les remboursements du crédit
            List<Remboursement> remboursements = remboursementRepository.findByCreditId(credit.getId());

            // Mettre à jour la liste observable
            remboursementsList.clear();
            remboursementsList.addAll(remboursements);

            // Mettre à jour le tableau
            tableRemboursements.setItems(remboursementsList);
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du chargement des remboursements : " + e.getMessage());
        }
    }

    @FXML
    private void handleCreditSelection() {
        // Récupérer le crédit sélectionné
        selectedCredit = tableCredits.getSelectionModel().getSelectedItem();

        if (selectedCredit != null) {
            // Remplir les champs avec les informations du crédit
            txtMontant.setText(String.valueOf(selectedCredit.getMontant()));
            txtTauxInteret.setText(String.valueOf(selectedCredit.getTauxInteret()));
            txtDuree.setText(String.valueOf(selectedCredit.getDureeEnMois()));
            txtMensualite.setText(String.valueOf(selectedCredit.getMensualite()));
            dpDateDemande.setValue(selectedCredit.getDateDemande());
            cbStatut.setValue(selectedCredit.getStatut());
            cbClient.setValue(selectedCredit.getClient());

            // Activer les boutons de modification et suppression
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
            btnSave.setDisable(true);
        }
    }

    @FXML
    private void handleCalculerMensualite(ActionEvent event) {
        try {
            // Vérifier que les champs nécessaires sont remplis
            if (txtMontant.getText().isEmpty() || txtTauxInteret.getText().isEmpty() || txtDuree.getText().isEmpty()) {
                Notification.notifWarning("Mensualité", "Veuillez remplir les champs Montant, Taux d'intérêt et Durée");
                return;
            }

            // Récupérer les valeurs
            double montant = Double.parseDouble(txtMontant.getText());
            double taux = Double.parseDouble(txtTauxInteret.getText());
            int duree = Integer.parseInt(txtDuree.getText());

            // Calculer la mensualité
            double mensualite = creditService.calculerMensualite(montant, taux, duree);

            // Afficher le résultat
            txtMensualite.setText(String.format("%.2f", mensualite));
        } catch (NumberFormatException e) {
            Notification.notifError("Erreur", "Veuillez saisir des valeurs numériques valides");
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors du calcul de la mensualité : " + e.getMessage());
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Vérifier que tous les champs obligatoires sont remplis
            if (txtMontant.getText().isEmpty() || txtTauxInteret.getText().isEmpty() ||
                    txtDuree.getText().isEmpty() || txtMensualite.getText().isEmpty() ||
                    cbClient.getValue() == null) {
                Notification.notifWarning("Crédit", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Récupérer les valeurs
            double montant = Double.parseDouble(txtMontant.getText());
            double taux = Double.parseDouble(txtTauxInteret.getText());
            int duree = Integer.parseInt(txtDuree.getText());
            double mensualite = Double.parseDouble(txtMensualite.getText());

            // Créer un crédit
            CreditRequest creditRequest = new CreditRequest(
                    montant,
                    taux,
                    duree,
                    mensualite,
                    dpDateDemande.getValue() != null ? dpDateDemande.getValue() : LocalDate.now(),
                    cbStatut.getValue() != null ? cbStatut.getValue() : "En attente",
                    cbClient.getValue().getId()
            );

            // Enregistrer le crédit
            CreditResponse creditResponse = creditService.createCredit(creditRequest);

            if (creditResponse != null) {
                Notification.notifSuccess("Crédit", "Crédit créé avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les crédits
                chargerCredits();
            }
        } catch (NumberFormatException e) {
            Notification.notifError("Erreur", "Veuillez saisir des valeurs numériques valides");
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la création du crédit : " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            // Vérifier qu'un crédit est sélectionné
            if (selectedCredit == null) {
                Notification.notifWarning("Crédit", "Veuillez sélectionner un crédit");
                return;
            }

            // Vérifier que tous les champs obligatoires sont remplis
            if (txtMontant.getText().isEmpty() || txtTauxInteret.getText().isEmpty() ||
                    txtDuree.getText().isEmpty() || txtMensualite.getText().isEmpty() ||
                    cbClient.getValue() == null) {
                Notification.notifWarning("Crédit", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Récupérer les valeurs
            double montant = Double.parseDouble(txtMontant.getText());
            double taux = Double.parseDouble(txtTauxInteret.getText());
            int duree = Integer.parseInt(txtDuree.getText());
            double mensualite = Double.parseDouble(txtMensualite.getText());

            // Créer un crédit
            CreditRequest creditRequest = new CreditRequest(
                    montant,
                    taux,
                    duree,
                    mensualite,
                    dpDateDemande.getValue() != null ? dpDateDemande.getValue() : LocalDate.now(),
                    cbStatut.getValue() != null ? cbStatut.getValue() : "En attente",
                    cbClient.getValue().getId()
            );

            // Mettre à jour le crédit
            Optional<CreditResponse> creditResponse = creditService.updateCredit(selectedCredit.getId(), creditRequest);

            if (creditResponse.isPresent()) {
                Notification.notifSuccess("Crédit", "Crédit mis à jour avec succès");

                // Réinitialiser les champs
                handleClear(event);

                // Recharger les crédits
                chargerCredits();
            }
        } catch (NumberFormatException e) {
            Notification.notifError("Erreur", "Veuillez saisir des valeurs numériques valides");
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la mise à jour du crédit : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            // Vérifier qu'un crédit est sélectionné
            if (selectedCredit == null) {
                Notification.notifWarning("Crédit", "Veuillez sélectionner un crédit");
                return;
            }

            // Demander confirmation
            boolean confirm = Notification.confirmDelete();

            if (confirm) {
                // Supprimer le crédit
                boolean success = creditService.deleteCredit(selectedCredit.getId());

                if (success) {
                    Notification.notifSuccess("Crédit", "Crédit supprimé avec succès");

                    // Réinitialiser les champs
                    handleClear(event);

                    // Recharger les crédits
                    chargerCredits();
                }
            }
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la suppression du crédit : " + e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        txtMontant.clear();
        txtTauxInteret.clear();
        txtDuree.clear();
        txtMensualite.clear();
        dpDateDemande.setValue(null);
        cbStatut.setValue(null);
        cbClient.setValue(null);

        selectedCredit = null;
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnSave.setDisable(false);

        // Désélectionner la ligne du tableau
        tableCredits.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleEffectuerRemboursement(ActionEvent event) {
        try {
            // Vérifier qu'un crédit est sélectionné
            if (cbCreditRemboursement.getValue() == null) {
                Notification.notifWarning("Remboursement", "Veuillez sélectionner un crédit");
                return;
            }

            // Vérifier que le montant est saisi
            if (txtMontantRemboursement.getText().isEmpty()) {
                Notification.notifWarning("Remboursement", "Veuillez saisir un montant");
                return;
            }

            // Récupérer les valeurs
            Credit credit = cbCreditRemboursement.getValue();
            double montant = Double.parseDouble(txtMontantRemboursement.getText());
            LocalDate date = dpDateRemboursement.getValue() != null ? dpDateRemboursement.getValue() : LocalDate.now();

            // Effectuer le remboursement
            boolean success = creditService.effectuerRemboursement(credit.getId(), montant, date);

            if (success) {
                Notification.notifSuccess("Remboursement", "Remboursement effectué avec succès");

                // Réinitialiser les champs
                txtMontantRemboursement.clear();
                dpDateRemboursement.setValue(null);

                // Recharger les remboursements
                chargerRemboursements(credit);

                // Recharger les crédits (statut peut avoir changé)
                chargerCredits();
            }
        } catch (NumberFormatException e) {
            Notification.notifError("Erreur", "Veuillez saisir un montant valide");
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de l'enregistrement du remboursement : " + e.getMessage());
        }
    }

    @FXML
    private void handleCalculerSimulation(ActionEvent event) {
        try {
            // Vérifier que les champs sont remplis
            if (txtMontantSimulation.getText().isEmpty() || txtTauxInteretSimulation.getText().isEmpty() ||
                    txtDureeSimulation.getText().isEmpty()) {
                Notification.notifWarning("Simulation", "Veuillez remplir tous les champs de simulation");
                return;
            }

            // Récupérer les valeurs
            double montant = Double.parseDouble(txtMontantSimulation.getText());
            double taux = Double.parseDouble(txtTauxInteretSimulation.getText());
            int duree = Integer.parseInt(txtDureeSimulation.getText());

            // Calculer la mensualité
            double mensualite = creditService.calculerMensualite(montant, taux, duree);

            // Calculer le montant total
            double montantTotal = creditService.calculerMontantTotal(mensualite, duree);

            // Calculer le coût total
            double coutTotal = montantTotal - montant;

            // Calculer le TEG
            double teg = creditService.calculerTauxEffectifGlobal(montant, mensualite, duree);

            // Afficher les résultats
            lblMensualiteSimulation.setText(String.format("%.2f FCFA", mensualite));
            lblMontantTotalSimulation.setText(String.format("%.2f FCFA", montantTotal));
            lblCoutTotalSimulation.setText(String.format("%.2f FCFA", coutTotal));
            lblTEGSimulation.setText(String.format("%.2f %%", teg));
        } catch (NumberFormatException e) {
            Notification.notifError("Erreur", "Veuillez saisir des valeurs numériques valides");
        } catch (Exception e) {
            Notification.notifError("Erreur", "Erreur lors de la simulation : " + e.getMessage());
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
}