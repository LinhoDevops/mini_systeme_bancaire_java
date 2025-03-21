package com.isi.mini_systeme_bancaire_javafx_jpa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "placements_financiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"client"})
public class PlacementFinancier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // Type de placement (Épargne, Actions, Obligations...)

    private double montant; // Montant investi

    private double tauxInteret; // Taux d'intérêt annuel en pourcentage

    private int dureeEnMois; // Durée du placement en mois

    private LocalDate dateDebut; // Date du début du placement

    private LocalDate dateEcheance; // Date d'échéance prévue

    private String statut; // Actif, Clôturé, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id")
    private Compte compte; // Compte lié au placement

    // Méthodes utilitaires

    /**
     * Calcule les intérêts accumulés à ce jour
     * @return Le montant des intérêts
     */
    public double calculerInteretsAccumules() {
        // Si le placement est terminé, on utilise la date d'échéance, sinon la date actuelle
        LocalDate dateFin = "Clôturé".equals(statut) ? dateEcheance : LocalDate.now();

        // Nombre de mois écoulés
        long moisEcoules = java.time.temporal.ChronoUnit.MONTHS.between(dateDebut, dateFin);

        // Limiter au nombre de mois de la durée
        moisEcoules = Math.min(moisEcoules, dureeEnMois);

        // Calcul simple des intérêts pour la période écoulée
        return montant * (tauxInteret / 100) * (moisEcoules / 12.0);
    }

    /**
     * Calcule la valeur totale prévisionnelle à l'échéance
     * @return Le montant total prévu (capital + intérêts)
     */
    public double calculerValeurFinale() {
        double interetsAnnuels = montant * (tauxInteret / 100);
        double interet = interetsAnnuels * (dureeEnMois / 12.0);
        return montant + interet;
    }

    /**
     * Clôture le placement avant la date d'échéance
     * @return Le montant à rembourser
     */
    public double cloturer() {
        this.statut = "Clôturé";
        this.dateEcheance = LocalDate.now();

        // Calcule le montant à rembourser (capital + intérêts accumulés)
        return montant + calculerInteretsAccumules();
    }
}
