//package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;
//
//import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.RemboursementMapper;
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.Credit;
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.Remboursement;
//import com.isi.mini_systeme_bancaire_javafx_jpa.repository.CreditRepository;
//import com.isi.mini_systeme_bancaire_javafx_jpa.repository.RemboursementRepository;
//import com.isi.mini_systeme_bancaire_javafx_jpa.response.RemboursementResponse;
//import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.RemboursementService;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public class RemboursementServiceImpl implements RemboursementService {
//
//    private final RemboursementRepository remboursementRepository;
//    private final CreditRepository creditRepository;
//
//    public RemboursementServiceImpl() {
//        this.remboursementRepository = new RemboursementRepository();
//        this.creditRepository = new CreditRepository();
//    }
//
//    @Override
//    public List<RemboursementResponse> getAllRemboursements() {
//        return remboursementRepository.findAll().stream()
//                .map(RemboursementMapper::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Optional<RemboursementResponse> getRemboursementById(Long id) {
//        return remboursementRepository.findById(id)
//                .map(RemboursementMapper::toResponse);
//    }
//
//    @Override
//    public List<RemboursementResponse> getRemboursementsByCreditId(Long creditId) {
//        return remboursementRepository.findByCreditId(creditId).stream()
//                .map(RemboursementMapper::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public RemboursementResponse createRemboursement(Long creditId, double montant, LocalDate date) {
//        // Vérifier si le crédit existe
//        Optional<Credit> creditOpt = creditRepository.findById(creditId);
//        if (creditOpt.isEmpty()) {
//            Notification.notifWarning("Erreur", "Le crédit spécifié n'existe pas");
//            return null;
//        }
//
//        Credit credit = creditOpt.get();
//
//        // Vérifier que le crédit est en cours
//        if (!"Approuvé".equals(credit.getStatut()) && !"En cours".equals(credit.getStatut())) {
//            Notification.notifWarning("Erreur", "Le crédit n'est pas en cours de remboursement");
//            return null;
//        }
//
//        // Créer le remboursement
//        Remboursement remboursement = new Remboursement();
//        remboursement.setMontant(montant);
//        remboursement.setDate(date != null ? date : LocalDate.now());
//        remboursement.setCredit(credit);
//
//        // Sauvegarder le remboursement
//        remboursement = remboursementRepository.save(remboursement);
//
//        // Mettre à jour le statut du crédit si nécessaire
//        updateCreditStatusAfterRemboursement(credit);
//
//        // Envoyer une notification par email
//        if (credit.getClient() != null && credit.getClient().getEmail() != null) {
//            try {
//                double montantTotal = credit.getMensualite() * credit.getDureeEnMois();
//                double montantRembourse = getTotalRemboursementsByCreditId(creditId);
//                double montantRestant = montantTotal - montantRembourse;
//
//                String message = "Bonjour " + credit.getClient().getNom() + " " + credit.getClient().getPrenom() + ",\n\n" +
//                        "Un remboursement de " + montant + " FCFA a été enregistré sur votre crédit.\n" +
//                        "Montant restant à rembourser: " + montantRestant + " FCFA\n\n" +
//                        "Cordialement,\n" +
//                        "L'équipe du Mini Système Bancaire";
//
//                Email.sendCustomEmail(
//                        credit.getClient().getEmail(),
//                        "Confirmation de remboursement - Mini Système Bancaire",
//                        message
//                );
//            } catch (Exception e) {
//                Notification.notifWarning("Email", "Impossible d'envoyer l'email de confirmation: " + e.getMessage());
//            }
//        }
//
//        return RemboursementMapper.toResponse(remboursement);
//    }
//
//    @Override
//    public boolean deleteRemboursement(Long id) {
//        try {
//            // Récupérer le remboursement
//            Optional<Remboursement> remboursementOpt = remboursementRepository.findById(id);
//            if (remboursementOpt.isEmpty()) {
//                return false;
//            }
//
//            Remboursement remboursement = remboursementOpt.get();
//            Credit credit = remboursement.getCredit();
//
//            // Supprimer le remboursement
//            remboursementRepository.deleteById(id);
//
//            // Mettre à jour le statut du crédit
//            if (credit != null) {
//                updateCreditStatusAfterRemboursement(credit);
//            }
//
//            return true;
//        } catch (Exception e) {
//            Notification.notifError("Erreur", "Impossible de supprimer le remboursement: " + e.getMessage());
//            return false;
//        }
//    }
//
//    @Override
//    public double getTotalRemboursementsByCreditId(Long creditId) {
//        List<Remboursement> remboursements = remboursementRepository.findByCreditId(creditId);
//        return remboursements.stream()
//                .mapToDouble(Remboursement::getMontant)
//                .sum();
//    }
//
//    @Override
//    public List<Remboursement> genererEcheancierRemboursement(Long creditId) {
//        Optional<Credit> creditOpt = creditRepository.findById(creditId);
//        if (creditOpt.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        Credit credit = creditOpt.get();
//        List<Remboursement> echeancier = new ArrayList<>();
//
//        // Date de départ (soit la date actuelle, soit la date de demande + 1 mois)
//        LocalDate dateDepart = LocalDate.now().isAfter(credit.getDateDemande().plusMonths(1)) ?
//                LocalDate.now() : credit.getDateDemande().plusMonths(1);
//
//        // Créer les échéances pour chaque mois
//        for (int i = 0; i < credit.getDureeEnMois(); i++) {
//            Remboursement echeance = new Remboursement();
//            echeance.setMontant(credit.getMensualite());
//            echeance.setDate(dateDepart.plusMonths(i));
//            echeance.setCredit(credit);
//            echeancier.add(echeance);
//        }
//
//        return echeancier;
//    }
//
//    @Override
//    public boolean verifierRemboursementsComplets(Long creditId) {
//        Optional<Credit> creditOpt = creditRepository.findById(creditId);
//        if (creditOpt.isEmpty()) {
//            return false;
//        }
//
//        Credit credit = creditOpt.get();
//        double montantTotal = credit.getMensualite() * credit.getDureeEnMois();
//        double montantRembourse = getTotalRemboursementsByCreditId(creditId);
//
//        // On considère le crédit comme remboursé si au moins 99% du montant a été payé
//        // (pour tenir compte des erreurs d'arrondi)
//        return montantRembourse >= (montantTotal * 0.99);
//    }
//
//    // Méthode privée pour mettre à jour le statut du crédit après un remboursement
//    private void updateCreditStatusAfterRemboursement(Credit credit) {
//        if (credit == null) {
//            return;
//        }
//
//        // Calculer le montant total à rembourser
//        double montantTotal = credit.getMensualite() * credit.getDureeEnMois();
//
//        // Calculer le montant déjà remboursé
//        double montantRembourse = getTotalRemboursementsByCreditId(credit.getId());
//
//        // Si tout est remboursé (ou presque), mettre à jour le statut
//        if (montantRembourse >= (montantTotal * 0.99)) { // 99% pour tenir compte des erreurs d'arrondi
//            credit.setStatut("Remboursé");
//            creditRepository.save(credit);
//
//            // Envoyer un email de confirmation
//            if (credit.getClient() != null && credit.getClient().getEmail() != null) {
//                try {
//                    String message = "Félicitations " + credit.getClient().getNom() + " " + credit.getClient().getPrenom() + " !\n\n" +
//                            "Votre crédit a été entièrement remboursé.\n" +
//                            "Montant initial: " + credit.getMontant() + " FCFA\n" +
//                            "Montant total remboursé: " + montantRembourse + " FCFA\n\n" +
//                            "Merci pour votre confiance.\n\n" +
//                            "Cordialement,\n" +
//                            "L'équipe du Mini Système Bancaire";
//
//                    Email.sendCustomEmail(
//                            credit.getClient().getEmail(),
//                            "Crédit entièrement remboursé - Mini Système Bancaire",
//                            message
//                    );
//                } catch (Exception e) {
//                    // Ignorer l'erreur d'envoi d'email
//                }
//            }
//        } else if (!"En cours".equals(credit.getStatut()) && "Approuvé".equals(credit.getStatut())) {
//            credit.setStatut("En cours");
//            creditRepository.save(credit);
//        }
//    }
//}