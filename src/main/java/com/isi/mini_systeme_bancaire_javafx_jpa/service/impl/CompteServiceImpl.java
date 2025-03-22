package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;

import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.CompteMapper;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.TransactionRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CompteRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CompteResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.ClientService;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.CompteService;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.FraisBancaireService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Outils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompteServiceImpl implements CompteService {

    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;
    private final ClientService clientService;
    private final FraisBancaireService fraisBancaireService;

    public CompteServiceImpl() {
        this.compteRepository = new CompteRepository();
        this.transactionRepository = new TransactionRepository();
        this.clientService = new ClientServiceImpl();
        this.fraisBancaireService = new FraisBancaireServiceImpl();
    }

    @Override
    public List<CompteResponse> getAllComptes() {
        return compteRepository.findAll().stream()
                .map(CompteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CompteResponse> getCompteById(Long id) {
        return compteRepository.findById(id)
                .map(CompteMapper::toResponse);
    }

    @Override
    public Optional<CompteResponse> getCompteByNumero(String numero) {
        return compteRepository.findByNumero(numero)
                .map(CompteMapper::toResponse);
    }

    @Override
    public List<CompteResponse> getComptesByClientId(Long clientId) {
        return compteRepository.findByClientId(clientId).stream()
                .map(CompteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompteResponse> searchComptes(String searchTerm) {
        return compteRepository.searchComptes(searchTerm).stream()
                .map(CompteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CompteResponse createCompte(CompteRequest compteRequest) {
        // Vérifier si le client existe
        Optional<Client> clientOpt = clientService.getClientEntityById(compteRequest.clientId());
        if (clientOpt.isEmpty()) {
            Notification.notifWarning("Erreur de création", "Le client spécifié n'existe pas");
            return null;
        }

        Client client = clientOpt.get();

        // Générer un numéro de compte unique si non fourni
        String numero = compteRequest.numero();
        if (numero == null || numero.isEmpty()) {
            numero = Outils.generateAccountNumber();
        } else if (compteRepository.findByNumero(numero).isPresent()) {
            Notification.notifWarning("Erreur de création", "Un compte avec ce numéro existe déjà");
            return null;
        }

        // Déterminer le type de compte et définir le solde initial en conséquence
        String type = compteRequest.type();
        double solde;

        if ("EPARGNE".equalsIgnoreCase(type) || "épargne".equalsIgnoreCase(type)) {
            solde = 5000.0; // Solde par défaut pour compte épargne
            type = "EPARGNE"; // Normaliser le type
        } else {
            solde = 10000.0; // Solde par défaut pour compte courant
            type = "COURANT"; // Normaliser le type
        }

        // Créer un nouveau compte
        Compte compte = new Compte();
        compte.setNumero(numero);
        compte.setType(type);
        compte.setSolde(solde);
        compte.setDateCreation(compteRequest.dateCreation() != null ? compteRequest.dateCreation() : LocalDate.now());
        compte.setStatut(compteRequest.statut() != null ? compteRequest.statut() : "actif");
        compte.setClient(client);

        // Sauvegarder le compte
        compte = compteRepository.save(compte);

        // Créer une transaction de dépôt initial
        Transaction transaction = new Transaction();
        transaction.setType("DEPOT");
        transaction.setMontant(solde);
        transaction.setDate(LocalDateTime.now());
        transaction.setStatut("COMPLETEE");
        transaction.setCompte(compte);
        transactionRepository.save(transaction);

        // Envoyer un email de confirmation
        try {
            String message = "Bonjour " + client.getNom() + " " + client.getPrenom() + ",\n\n" +
                    "Nous vous informons qu'un nouveau compte a été créé pour vous:\n" +
                    "Numéro de compte: " + compte.getNumero() + "\n" +
                    "Type de compte: " + compte.getType() + "\n" +
                    "Solde initial: " + compte.getSolde() + " FCFA\n\n" +
                    "Cordialement,\n" +
                    "L'équipe du Mini Système Bancaire";

            Email.sendCustomEmail(client.getEmail(), "Création de compte - Mini Système Bancaire", message);
        } catch (Exception e) {
            Notification.notifWarning("Email", "Impossible d'envoyer l'email de confirmation: " + e.getMessage());
        }

        return CompteMapper.toResponse(compte);
    }

    @Override
    public Optional<CompteResponse> updateCompte(Long id, CompteRequest compteRequest) {
        return compteRepository.findById(id)
                .map(compte -> {
                    // Mettre à jour les champs du compte
                    if (compteRequest.type() != null) {
                        compte.setType(compteRequest.type());
                    }

                    if (compteRequest.statut() != null) {
                        compte.setStatut(compteRequest.statut());
                    }

                    // Sauvegarder les modifications
                    Compte updatedCompte = compteRepository.save(compte);
                    return CompteMapper.toResponse(updatedCompte);
                });
    }

    @Override
    public boolean deleteCompte(Long id) {
        try {
            compteRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de suppression",
                    "Impossible de supprimer le compte: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean effectuerDepot(Long compteId, double montant) {
        if (montant <= 0) {
            Notification.notifError("Erreur de dépôt", "Le montant doit être supérieur à zéro");
            return false;
        }

        Optional<Compte> compteOpt = compteRepository.findById(compteId);
        if (compteOpt.isEmpty()) {
            Notification.notifError("Erreur de dépôt", "Compte introuvable");
            return false;
        }

        Compte compte = compteOpt.get();

        try {
            // Augmenter le solde du compte
            compte.setSolde(compte.getSolde() + montant);
            compteRepository.save(compte);

            // Créer une transaction
            Transaction transaction = new Transaction(
                    "DEPOT",
                    montant,
                    LocalDateTime.now(),
                    "COMPLETEE"
            );
            transaction.setCompte(compte);
            transactionRepository.save(transaction);

            // Appliquer des frais bancaires pour le dépôt
            if (montant >= 100000) { // Exemple: frais pour les gros dépôts
                fraisBancaireService.appliquerFraisTransaction(compteId, "DEPOT", montant);
            }

            // Envoyer un email de notification
            Client client = compte.getClient();
            if (client != null) {
                Email.sendNotificationTransaction(
                        client.getEmail(),
                        client.getNom() + " " + client.getPrenom(),
                        "dépôt",
                        montant,
                        compte.getNumero()
                );
            }

            Notification.notifSuccess("Dépôt réussi",
                    "Dépôt de " + montant + " FCFA effectué sur le compte " + compte.getNumero());
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de dépôt", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean effectuerRetrait(Long compteId, double montant) {
        if (montant <= 0) {
            Notification.notifError("Erreur de retrait", "Le montant doit être supérieur à zéro");
            return false;
        }

        Optional<Compte> compteOpt = compteRepository.findById(compteId);
        if (compteOpt.isEmpty()) {
            Notification.notifError("Erreur de retrait", "Compte introuvable");
            return false;
        }

        Compte compte = compteOpt.get();

        if (compte.getSolde() < montant) {
            Notification.notifError("Erreur de retrait", "Solde insuffisant");
            return false;
        }

        try {
            // Calculer les frais de retrait
            double fraisRetrait = calculerFraisRetrait(montant, compte.getType());
            double montantTotal = montant + fraisRetrait;

            if (compte.getSolde() < montantTotal) {
                Notification.notifError("Erreur de retrait",
                        "Solde insuffisant pour couvrir le montant et les frais de " + fraisRetrait + " FCFA");
                return false;
            }

            // Diminuer le solde du compte
            compte.setSolde(compte.getSolde() - montantTotal);
            compteRepository.save(compte);

            // Créer une transaction pour le retrait
            Transaction transaction = new Transaction(
                    "RETRAIT",
                    montant,
                    LocalDateTime.now(),
                    "COMPLETEE"
            );
            transaction.setCompte(compte);
            transactionRepository.save(transaction);

            // Appliquer les frais bancaires
            if (fraisRetrait > 0) {
                fraisBancaireService.appliquerFraisTransaction(compteId, "RETRAIT", montant);
            }

            // Envoyer un email de notification
            Client client = compte.getClient();
            if (client != null) {
                String message = "Bonjour " + client.getNom() + " " + client.getPrenom() + ",\n\n" +
                        "Un retrait de " + montant + " FCFA a été effectué sur votre compte N° " + compte.getNumero() + ".\n" +
                        "Frais de retrait: " + fraisRetrait + " FCFA\n" +
                        "Montant total débité: " + montantTotal + " FCFA\n" +
                        "Nouveau solde: " + compte.getSolde() + " FCFA\n\n" +
                        "Cordialement,\n" +
                        "L'équipe du Mini Système Bancaire";

                Email.sendCustomEmail(client.getEmail(), "Notification de retrait - Mini Système Bancaire", message);
            }

            Notification.notifSuccess("Retrait réussi",
                    "Retrait de " + montant + " FCFA (+ frais: " + fraisRetrait + " FCFA) effectué sur le compte " + compte.getNumero());
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de retrait", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean effectuerVirement(Long compteSourceId, Long compteDestinationId, double montant) {
        if (montant <= 0) {
            Notification.notifError("Erreur de virement", "Le montant doit être supérieur à zéro");
            return false;
        }

        if (compteSourceId.equals(compteDestinationId)) {
            Notification.notifError("Erreur de virement", "Impossible de faire un virement vers le même compte");
            return false;
        }

        Optional<Compte> compteSourceOpt = compteRepository.findById(compteSourceId);
        if (compteSourceOpt.isEmpty()) {
            Notification.notifError("Erreur de virement", "Compte source introuvable");
            return false;
        }

        Optional<Compte> compteDestOpt = compteRepository.findById(compteDestinationId);
        if (compteDestOpt.isEmpty()) {
            Notification.notifError("Erreur de virement", "Compte destination introuvable");
            return false;
        }

        Compte compteSource = compteSourceOpt.get();
        Compte compteDest = compteDestOpt.get();

        // Calculer les frais de virement
        double fraisVirement = calculerFraisVirement(montant, compteSource.getType(), compteDest.getType());
        double montantTotal = montant + fraisVirement;

        if (compteSource.getSolde() < montantTotal) {
            Notification.notifError("Erreur de virement",
                    "Solde insuffisant pour couvrir le montant et les frais de " + fraisVirement + " FCFA");
            return false;
        }

        try {
            // Diminuer le solde du compte source
            compteSource.setSolde(compteSource.getSolde() - montantTotal);
            compteRepository.save(compteSource);

            // Augmenter le solde du compte destination
            compteDest.setSolde(compteDest.getSolde() + montant);
            compteRepository.save(compteDest);

            // Créer une transaction
            Transaction transaction = new Transaction(
                    "VIREMENT",
                    montant,
                    LocalDateTime.now(),
                    "COMPLETEE"
            );
            transaction.setCompteSource(compteSource);
            transaction.setCompteDestination(compteDest);
            transactionRepository.save(transaction);

            // Appliquer les frais bancaires
            if (fraisVirement > 0) {
                fraisBancaireService.appliquerFraisTransaction(compteSourceId, "VIREMENT", montant);
            }

            // Envoyer des emails de notification
            Client clientSource = compteSource.getClient();
            if (clientSource != null) {
                String message = "Bonjour " + clientSource.getNom() + " " + clientSource.getPrenom() + ",\n\n" +
                        "Un virement de " + montant + " FCFA a été effectué depuis votre compte N° " + compteSource.getNumero() + ".\n" +
                        "Frais de virement: " + fraisVirement + " FCFA\n" +
                        "Montant total débité: " + montantTotal + " FCFA\n" +
                        "Nouveau solde: " + compteSource.getSolde() + " FCFA\n\n" +
                        "Cordialement,\n" +
                        "L'équipe du Mini Système Bancaire";

                Email.sendCustomEmail(clientSource.getEmail(), "Notification de virement sortant - Mini Système Bancaire", message);
            }

            Client clientDest = compteDest.getClient();
            if (clientDest != null) {
                String message = "Bonjour " + clientDest.getNom() + " " + clientDest.getPrenom() + ",\n\n" +
                        "Un virement de " + montant + " FCFA a été reçu sur votre compte N° " + compteDest.getNumero() + ".\n" +
                        "Nouveau solde: " + compteDest.getSolde() + " FCFA\n\n" +
                        "Cordialement,\n" +
                        "L'équipe du Mini Système Bancaire";

                Email.sendCustomEmail(clientDest.getEmail(), "Notification de virement entrant - Mini Système Bancaire", message);
            }

            Notification.notifSuccess("Virement réussi",
                    "Virement de " + montant + " FCFA (+ frais: " + fraisVirement + " FCFA) effectué du compte " +
                            compteSource.getNumero() + " vers le compte " + compteDest.getNumero());
            return true;
        } catch (Exception e) {
            Notification.notifError("Erreur de virement", e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Compte> getCompteEntityById(Long id) {
        return compteRepository.findById(id);
    }

    // Méthode pour calculer les frais de retrait
    private double calculerFraisRetrait(double montant, String typeCompte) {
        if ("EPARGNE".equalsIgnoreCase(typeCompte)) {
            return montant * 0.01; // 1% pour comptes épargne
        } else {
            return montant * 0.005; // 0.5% pour comptes courants
        }
    }

    // Méthode pour calculer les frais de virement
    private double calculerFraisVirement(double montant, String typeCompteSource, String typeCompteDest) {
        boolean isInterne = typeCompteSource.equalsIgnoreCase(typeCompteDest);

        if (isInterne) {
            return montant * 0.002; // 0.2% pour virements internes
        } else {
            return montant * 0.01; // 1% pour virements externes
        }
    }
}