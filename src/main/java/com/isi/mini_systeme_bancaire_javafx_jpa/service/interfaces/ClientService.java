package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.ClientRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.ClientResponse;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    List<ClientResponse> getAllClients();

    Optional<ClientResponse> getClientById(Long id);

    Optional<ClientResponse> getClientByEmail(String email);

    Optional<ClientResponse> getClientByTelephone(String telephone);

    List<ClientResponse> searchClients(String searchTerm);

    ClientResponse createClient(ClientRequest clientRequest);

    Optional<ClientResponse> updateClient(Long id, ClientRequest clientRequest);

    boolean deleteClient(Long id);

    boolean activerClient(Long id);

    boolean desactiverClient(Long id);

    boolean resetPassword(Long id);

    long countAllClients();

    long countClientsByStatut(String statut);

    // Méthode utilitaire pour accéder directement au modèle Client (pour les autres services)
    Optional<Client> getClientEntityById(Long id);
}