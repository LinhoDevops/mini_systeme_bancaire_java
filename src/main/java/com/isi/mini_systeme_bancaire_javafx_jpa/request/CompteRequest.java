package com.isi.mini_systeme_bancaire_javafx_jpa.request;

import com.isi.mini_systeme_bancaire_javafx_jpa.enums.TypeCompte;
import java.time.LocalDate;

public record CompteRequest(
        String numero,
        TypeCompte type,
        double solde,
        LocalDate dateCreation,
        String statut,
        double tauxInteret,
        Long clientId
) {}