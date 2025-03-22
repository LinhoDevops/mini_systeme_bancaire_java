package com.isi.mini_systeme_bancaire_javafx_jpa.model;

import com.isi.mini_systeme_bancaire_javafx_jpa.enums.TypeCompte;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comptes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"client", "transactions", "frais", "carte"})
public class Compte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;

    @Enumerated(EnumType.STRING)
    private TypeCompte type;  // COURANT/EPARGNE

    private double solde;
    private LocalDate dateCreation;
    private String statut;  // actif/inactif/suspendu
    private double tauxInteret; // Pour les comptes épargne

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FraisBancaire> frais = new ArrayList<>();

    @OneToOne(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CarteBancaire carte;

    // Constructeur avec champs principaux
    public Compte(String numero, TypeCompte type, double solde, LocalDate dateCreation, String statut) {
        this.numero = numero;
        this.type = type;
        if (type == TypeCompte.COURANT) {
            this.solde = 10000; // Solde initial pour compte courant
            this.tauxInteret = 0;
        } else if (type == TypeCompte.EPARGNE) {
            this.solde = 5000; // Solde initial pour compte épargne
            this.tauxInteret = 3.5; // Taux d'intérêt par défaut pour compte épargne (%)
        } else {
            this.solde = solde;
        }
        this.dateCreation = dateCreation;
        this.statut = statut;
    }

    // Méthodes utilitaires
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setCompte(this);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setCompte(null);
    }

    public void addFrais(FraisBancaire frais) {
        this.frais.add(frais);
        frais.setCompte(this);
    }

    public void removeFrais(FraisBancaire frais) {
        this.frais.remove(frais);
        frais.setCompte(null);
    }

    // Méthode pour appliquer des frais bancaires
    public void appliquerFrais(String type, double montant) {
        if (this.solde >= montant) {
            this.solde -= montant;
            FraisBancaire frais = new FraisBancaire(type, montant, LocalDate.now());
            this.addFrais(frais);
        }
    }

    // Méthode pour appliquer le taux d'intérêt (pour compte épargne)
    public void appliquerInteret() {
        if (this.type == TypeCompte.EPARGNE) {
            double interets = this.solde * (this.tauxInteret / 100);
            this.solde += interets;

            // Créer une transaction pour tracer cette opération
            Transaction transaction = new Transaction(
                    "CREDIT_INTERET",
                    interets,
                    LocalDate.now().atStartOfDay(),
                    "COMPLETEE"
            );
            this.addTransaction(transaction);
        }
    }
}