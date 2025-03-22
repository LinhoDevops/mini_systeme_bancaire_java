package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Remboursement;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.RemboursementResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RemboursementService {

    List<RemboursementResponse> getAllRemboursements();

    Optional<RemboursementResponse> getRemboursementById(Long id);

    List<RemboursementResponse> getRemboursementsByCreditId(Long creditId);

    RemboursementResponse createRemboursement(Long creditId, double montant, LocalDate date);

    boolean deleteRemboursement(Long id);

    double getTotalRemboursementsByCreditId(Long creditId);

    // Méthode pour générer un échéancier de remboursement prévisionnel
    List<Remboursement> genererEcheancierRemboursement(Long creditId);

    // Méthode pour vérifier l'état des remboursements d'un crédit
    boolean verifierRemboursementsComplets(Long creditId);
}