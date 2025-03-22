package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.enums.TypeCompte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.CompteRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.CompteResponse;

import java.util.List;
import java.util.Optional;

public interface CompteService {

    List<CompteResponse> getAllComptes();

    Optional<CompteResponse> getCompteById(Long id);

    Optional<CompteResponse> getCompteByNumero(String numero);

    List<CompteResponse> getComptesByClientId(Long clientId);

    List<CompteResponse> getComptesByType(TypeCompte type);

    List<CompteResponse> getComptesByStatut(String statut);

    List<CompteResponse> searchComptes(String searchTerm);

    CompteResponse createCompte(CompteRequest compteRequest);

    Optional<CompteResponse> updateCompte(Long id, CompteRequest compteRequest);

    boolean deleteCompte(Long id);

    boolean activerCompte(Long id);

    boolean suspendreCompte(Long id);

    boolean fermerCompte(Long id);

    // Méthodes de transaction
    boolean effectuerDepot(Long compteId, double montant);

    boolean effectuerRetrait(Long compteId, double montant);

    boolean effectuerVirement(Long compteSourceId, Long compteDestinationId, double montant);

    // Méthodes pour la gestion des frais et des intérêts
    boolean appliquerFraisMensuels(Long compteId);

    boolean appliquerInteret(Long compteId);

    // Méthodes statistiques
    double getSommeTotaleSoldes();

    long countAllComptes();

    long countComptesByType(TypeCompte type);

    long countComptesByStatut(String statut);

    // Méthode utilitaire pour accéder directement au modèle Compte (pour les autres services)
    Optional<Compte> getCompteEntityById(Long id);
}