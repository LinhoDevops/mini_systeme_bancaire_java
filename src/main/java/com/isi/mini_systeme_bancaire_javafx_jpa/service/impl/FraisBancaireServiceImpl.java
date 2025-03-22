//package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;
//
//import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.FraisBancaireMapper;
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.FraisBancaire;
//import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CompteRepository;
//import com.isi.mini_systeme_bancaire_javafx_jpa.repository.FraisBancaireRepository;
//import com.isi.mini_systeme_bancaire_javafx_jpa.request.FraisBancaireRequest;
//import com.isi.mini_systeme_bancaire_javafx_jpa.response.FraisBancaireResponse;
//import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.FraisBancaireService;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public class FraisBancaireServiceImpl implements FraisBancaireService {
//
//    private final FraisBancaireRepository fraisRepository;
//    private final CompteRepository compteRepository;
//
//    // Constantes pour les différents types de frais
//    private static final double TAUX_TENUE_COMPTE_COURANT = 500.0; // FCFA par mois
//    private static final double TAUX_TENUE_COMPTE_EPARGNE = 250.0; // FCFA par mois
//    private static final double TAUX_TRANSACTION_DEPOT = 0.001; // 0.1% pour les dépôts
//    private static final double TAUX_TRANSACTION_RETRAIT_COURANT = 0.005; // 0.5% pour les retraits (compte courant)
//    private static final double TAUX_TRANSACTION_RETRAIT_EPARGNE = 0.01; // 1% pour les retraits (compte épargne)
//    private static final double TAUX_TRANSACTION_VIREMENT_INTERNE = 0.002; // 0.2% pour les virements internes
//    private static final double TAUX_TRANSACTION_VIREMENT_EXTERNE = 0.01; // 1% pour les virements externes
//    private static final double FRAIS_CARTE_STANDARD = 1000.0; // FCFA
//    private static final double FRAIS_CARTE_PREMIUM = 2500.0; // FCFA
//
//    public FraisBancaireServiceImpl() {
//        this.fraisRepository = new FraisBancaireRepository();
//        this.compteRepository = new CompteRepository();
//    }
//
//    @Override
//    public List<FraisBancaireResponse> getAllFrais() {
//        return fraisRepository.findAll().stream()
//                .map(FraisBancaireMapper::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Optional<FraisBancaireResponse> getFraisById(Long id) {
//        return fraisRepository.findById(id)
//                .map(FraisBancaireMapper::toResponse);
//    }
//
//    @Override
//    public List<FraisBancaireResponse> getFraisByCompteId(Long compteId) {
//        return fraisRepository.findByCompteId(compteId).stream()
//                .map(FraisBancaireMapper::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<FraisBancaireResponse> getFraisByClientId(Long clientId) {
//        return fraisRepository.findByClientId(clientId).stream()
//                .map(FraisBancaireMapper::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<FraisBancaireResponse> getFraisByType(String type) {
//        return fraisRepository.findByType(type).stream()
//                .map(FraisBancaireMapper::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<FraisBancaireResponse> getFraisByPeriode(LocalDate debut, LocalDate fin) {
//        return fraisRepository.findByPeriode(debut, fin).stream()
//                .map(FraisBancaireMapper::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public double getTotalFraisByCompteId(Long compteId) {
//        return fraisRepository.getTotalFraisByCompteId(compteId);
//    }
//
//    @Override
//    public double getTotalFraisByClientId(Long clientId) {
//        return fraisRepository.getTotalFraisByClientId(clientId);
//    }
//
//    @Override
//    public FraisBancaireResponse createFrais(FraisBancaireRequest fraisRequest) {
//        // Vérifier si le compte existe
//        Optional<Compte> compteOpt = compteRepository.findById(fraisRequest.compteId());
//        if (compteOpt.isEmpty()) {
//            Notification.notifWarning("Erreur de création", "Le compte spécifié n'existe pas");
//            return null;
//        }
//
//        Compte compte = compteOpt.get();
//
//        // Vérifier que le solde est suffisant pour appliquer les frais
//        if (compte.getSolde() < fraisRequest.montant()) {
//            Notification.notifWarning("Erreur de création", "Solde insuffisant pour appliquer les frais");
//            return null;
//        }
//
//        // Créer les frais bancaires
//        FraisBancaire frais = FraisBancaireMapper.fromRequest(fraisRequest, compte);
//
//        // Définir la date d'application si non fournie
//        if (frais.getDateApplication() == null) {
//            frais.setDateApplication(LocalDate.now());
//        }
//
//        // Appliquer les frais en déduisant du solde du compte
//        compte.setSolde(compte.getSolde() - frais.getMontant());
//        compteRepository.save(compte);
//
//        // Sauvegarder les frais
//        frais = fraisRepository.save(frais);
//
//        // Envoyer notification au client
//        notifierClient(frais);
//
//        return FraisBancaireMapper.toResponse(frais);
//    }
//
//    @Override
//    public Optional<FraisBancaireResponse> updateFrais(Long id, FraisBancaireRequest fraisRequest) {
//        return fraisRepository.findById(id)
//                .map(frais -> {
//                    // Récupérer l'ancien montant pour ajuster le solde du compte
//                    double ancienMontant = frais.getMontant();
//
//                    // Mettre à jour les champs des frais
//                    FraisBancaireMapper.updateFromRequest(frais, fraisRequest);
//
//                    // Ajuster le solde du compte si le montant a changé
//                    if (frais.getMontant() != ancienMontant && frais.getCompte() != null) {
//                        Compte compte = frais.getCompte();
//                        compte.setSolde(compte.getSolde() + ancienMontant - frais.getMontant());
//                        compteRepository.save(compte);
//                    }
//
//                    // Sauvegarder les modifications
//                    FraisBancaire updatedFrais = fraisRepository.save(frais);
//
//                    return FraisBancaireMapper.toResponse(updatedFrais);
//                });
//    }
//
//    @Override
//    public boolean deleteFrais(Long id) {
//        try {
//            // Récupérer les frais
//            Optional<FraisBancaire> fraisOpt = fraisRepository.findById(id);
//            if (fraisOpt.isPresent()) {
//                FraisBancaire frais = fraisOpt.get();
//
//                // Rembourser le montant au compte si nécessaire
//                if (frais.getCompte() != null) {
//                    Compte compte = frais.getCompte();
//                    compte.setSolde(compte.getSolde() + frais.getMontant());
//                    compteRepository.save(compte);
//                }
//
//                // Supprimer les frais
//                fraisRepository.deleteById(id);
//            } else {
//                return false;
//            }
//
//            return true;
//        } catch (Exception e) {
//            Notification.notifError("Erreur de suppression",
//                    "Impossible de supprimer les frais: " + e.getMessage());
//            return false;
//        }
//    }
//
//    @Override
//    public FraisBancaireResponse appliquerFraisTenueCompte(Long compteId) {
//        // Vérifier si le compte existe
//        Optional<Compte> compteOpt = compteRepository.findById(compteId);
//        if (compteOpt.isEmpty()) {
//            Notification.notifWarning("Erreur", "Le compte spécifié n'existe pas");
//            return null;
//        }
//
//        Compte compte = compteOpt.get();
//
//        // Déterminer le montant des frais selon le type de compte
//        double montantFrais = "EPARGNE".equalsIgnoreCase(compte.getType()) ?
//                TAUX_TENUE_COMPTE_EPARGNE : TAUX_TENUE_COMPTE_COURANT;
//
//        // Créer les frais de tenue de compte
//        FraisBancaireRequest request = new FraisBancaireRequest(
//                "Tenue de compte",
//                montantFrais,
//                LocalDate.now(),
//                compteId
//        );
//
//        return createFrais(request);
//    }
//
//    @Override
//    public FraisBancaireResponse appliquerFraisTransaction(Long compteId, String typeTransaction, double montantTransaction) {
//        // Vérifier si le compte existe
//        Optional<Compte> compteOpt = compteRepository.findById(compteId);
//        if (compteOpt.isEmpty()) {
//            Notification.notifWarning("Erreur", "Le compte spécifié n'existe pas");
//            return null;
//        }
//
//        Compte compte = compteOpt.get();
//
//        // Déterminer le taux selon le type de transaction et le type de compte
//        double taux;
//
//        switch (typeTransaction.toUpperCase()) {
//            case "DEPOT":
//                taux = TAUX_TRANSACTION_DEPOT;
//                break;
//            case "RETRAIT":
//                taux = "EPARGNE".equalsIgnoreCase(compte.getClientProperty) ?
//                        TAUX_TRANSACTION_RETRAIT_EPARGNE : TAUX_TRANSACTION_RETRAIT_COURANT;
//                break;
//            case "VIREMENT":
//                taux = TAUX_TRANSACTION_VIREMENT_INTERNE; // Par défaut, considéré comme interne
//                break;
//            default:
//                taux = 0.005; // Taux par défaut (0.5%)
//        }
//
//        // Calculer le montant des frais
//        double montantFrais = montantTransaction * taux;
//
//        // Arrondir à deux décimales
//        montantFrais = Math.round(montantFrais * 100.0) / 100.0;
//
//        // Créer les frais de transaction
//        FraisBancaireRequest request = new FraisBancaireRequest(
//                "Frais " + typeTransaction,
//                montantFrais,
//                LocalDate.now(),
//                compteId
//        );
//
//        return createFrais(request);
//    }
//
//    @Override
//    public FraisBancaireResponse appliquerFraisService(Long compteId, String typeService, double montant) {
//        // Vérifier si le compte existe
//        Optional<Compte> compteOpt = compteRepository.findById(compteId);
//        if (compteOpt.isEmpty()) {
//            Notification.notifWarning("Erreur", "Le compte spécifié n'existe pas");
//            return null;
//        }
//
//        Compte compte = compteOpt.get();
//
//        // Ajuster le montant en fonction du type de service si nécessaire
//        double montantFrais = montant;
//
//        if ("Carte Bancaire".equals(typeService)) {
//            montantFrais = "EPARGNE".equalsIgnoreCase(compte.getType()) ?
//                    FRAIS_CARTE_PREMIUM : FRAIS_CARTE_STANDARD;
//        }
//
//        // Créer les frais de service
//        FraisBancaireRequest request = new FraisBancaireRequest(
//                typeService,
//                montantFrais,
//                LocalDate.now(),
//                compteId
//        );
//
//        return createFrais(request);
//    }
//
//    /**
//     * Envoie une notification au client concernant les frais appliqués
//     */
//    private void notifierClient(FraisBancaire frais) {
//        if (frais.getCompte() != null && frais.getCompte().getClient() != null) {
//            try {
//                Email.sendFraisBancairesNotification(
//                        frais.getCompte().getClient().getEmail(),
//                        frais.getCompte().getClient().getNom() + " " + frais.getCompte().getClient().getPrenom(),
//                        frais.getType(),
//                        frais.getMontant(),
//                        frais.getCompte().getNumero()
//                );
//            } catch (Exception e) {
//                Notification.notifWarning("Email", "Impossible d'envoyer l'email de notification: " + e.getMessage());
//            }
//        }
//    }
//}