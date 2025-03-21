package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service pour l'analyse des opérations potentiellement suspectes
 */
public interface AnalyseOperationsService {

    /**
     * Détecte les transactions suspectes sur la base de différents critères
     * @return Liste des transactions suspectes
     */
    List<Transaction> detecterTransactionsSuspectes();

    /**
     * Détecte les transactions de montant inhabituellement élevé
     * @param seuil Seuil au-delà duquel une transaction est considérée comme inhabituelle
     * @return Liste des transactions suspectées
     */
    List<Transaction> detecterTransactionsMontantEleve(double seuil);

    /**
     * Détecte les transactions multiples dans un intervalle de temps court
     * @param nombreMin Nombre minimum de transactions pour déclencher une alerte
     * @param intervalleMins Intervalle de temps en minutes
     * @return Liste des transactions suspectes
     */
    List<Transaction> detecterTransactionsMultiples(int nombreMin, int intervalleMins);

    /**
     * Détecte les transactions effectuées à des heures inhabituelles
     * @param debutHeureNormale Heure de début des transactions normales (ex: 8)
     * @param finHeureNormale Heure de fin des transactions normales (ex: 18)
     * @return Liste des transactions suspectes
     */
    List<Transaction> detecterTransactionsHeuresInhabituelles(int debutHeureNormale, int finHeureNormale);

    /**
     * Détecte des motifs récurrents de petites transactions suivies d'une grosse transaction
     * @param nombrePetites Nombre minimum de petites transactions
     * @param seuilPetite Seuil maximum pour une petite transaction
     * @param seuilGrosse Seuil minimum pour une grosse transaction
     * @param intervalleMins Intervalle de temps en minutes
     * @return Liste des transactions suspectes groupées par client
     */
    Map<Long, List<Transaction>> detecterMotifSmurfing(int nombrePetites, double seuilPetite, double seuilGrosse, int intervalleMins);

    /**
     * Analyse les transactions entre la date de début et la date de fin
     * @param debut Date de début
     * @param fin Date de fin
     * @return Un rapport d'analyse contenant les transactions suspectes et les raisons
     */
    String genererRapportAnalyse(LocalDateTime debut, LocalDateTime fin);

    /**
     * Génère un rapport des transactions suspectes au format PDF
     * @param debut Date de début
     * @param fin Date de fin
     * @param filePath Chemin où sauvegarder le fichier
     * @return true si l'export a réussi, false sinon
     */
    boolean exporterRapportPDF(LocalDateTime debut, LocalDateTime fin, String filePath);

    /**
     * Génère un rapport des transactions suspectes au format Excel
     * @param debut Date de début
     * @param fin Date de fin
     * @param filePath Chemin où sauvegarder le fichier
     * @return true si l'export a réussi, false sinon
     */
    boolean exporterRapportExcel(LocalDateTime debut, LocalDateTime fin, String filePath);
}