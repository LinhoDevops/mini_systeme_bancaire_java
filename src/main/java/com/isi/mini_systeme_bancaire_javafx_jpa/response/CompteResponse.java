package com.isi.mini_systeme_bancaire_javafx_jpa.response;

import com.isi.mini_systeme_bancaire_javafx_jpa.enums.TypeCompte;
import java.time.LocalDate;

public record CompteResponse(
        Long id,
        String numero,
        TypeCompte type,
        double solde,
        LocalDate dateCreation,
        String statut,
        double tauxInteret,
        String nomClient,
        String prenomClient,
        Long clientId,
        boolean hasCarte,
        int nombreTransactions
) {}