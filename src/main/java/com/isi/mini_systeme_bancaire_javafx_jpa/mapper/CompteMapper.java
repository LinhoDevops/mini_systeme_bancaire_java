package com.isi.mini_systeme_bancaire_javafx_jpa.mapper;

import com.isi.mini_systeme_bancaire_javafx_jpa.enums.TypeCompte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CompteRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CompteResponse;

import java.time.LocalDate;

public class CompteMapper {

    public static Compte fromRequest(CompteRequest request, Client client) {
        if (request == null) {
            return null;
        }

        Compte compte = new Compte();
        compte.setNumero(request.numero());
        compte.setType(request.type());

        // Définir le solde selon le type de compte
        if (request.type() == TypeCompte.COURANT) {
            compte.setSolde(10000); // Solde initial pour compte courant
            compte.setTauxInteret(0);
        } else if (request.type() == TypeCompte.EPARGNE) {
            compte.setSolde(5000); // Solde initial pour compte épargne
            compte.setTauxInteret(request.tauxInteret() > 0 ? request.tauxInteret() : 3.5); // Taux d'intérêt par défaut pour compte épargne (%)
        } else {
            compte.setSolde(request.solde());
            compte.setTauxInteret(request.tauxInteret());
        }

        compte.setDateCreation(request.dateCreation() != null ? request.dateCreation() : LocalDate.now());
        compte.setStatut(request.statut() != null ? request.statut() : "actif");
        compte.setClient(client);

        return compte;
    }

    public static CompteResponse toResponse(Compte compte) {
        if (compte == null) {
            return null;
        }

        return new CompteResponse(
                compte.getId(),
                compte.getNumero(),
                compte.getType(),
                compte.getSolde(),
                compte.getDateCreation(),
                compte.getStatut(),
                compte.getTauxInteret(),
                compte.getClient() != null ? compte.getClient().getNom() : "",
                compte.getClient() != null ? compte.getClient().getPrenom() : "",
                compte.getClient() != null ? compte.getClient().getId() : null,
                compte.getCarte() != null,
                compte.getTransactions() != null ? compte.getTransactions().size() : 0
        );
    }

    public static void updateFromRequest(Compte compte, CompteRequest request) {
        if (compte == null || request == null) {
            return;
        }

        if (request.type() != null) {
            compte.setType(request.type());

            // Mettre à jour le taux d'intérêt si le type de compte change
            if (request.type() == TypeCompte.COURANT) {
                compte.setTauxInteret(0);
            } else if (request.type() == TypeCompte.EPARGNE && compte.getTauxInteret() == 0) {
                compte.setTauxInteret(request.tauxInteret() > 0 ? request.tauxInteret() : 3.5);
            }
        }

        if (request.statut() != null) {
            compte.setStatut(request.statut());
        }

        if (request.tauxInteret() > 0 && compte.getType() == TypeCompte.EPARGNE) {
            compte.setTauxInteret(request.tauxInteret());
        }
    }
}