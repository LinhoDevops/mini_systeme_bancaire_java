package com.isi.mini_systeme_bancaire_javafx_jpa.mapper;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.ClientRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.ClientResponse;

import java.time.LocalDate;

public class ClientMapper {

    public static Client fromRequest(ClientRequest request) {
        if (request == null) {
            return null;
        }

        Client client = new Client();
        client.setNom(request.nom());
        client.setPrenom(request.prenom());
        client.setEmail(request.email());
        client.setTelephone(request.telephone());
        client.setAdresse(request.adresse());
        client.setDateInscription(request.dateInscription() != null ? request.dateInscription() : LocalDate.now());
        client.setStatut(request.statut() != null ? request.statut() : "actif");
        client.setPassword("P@sser123"); // Mot de passe par défaut
        client.setPremierConnexion(true);

        return client;
    }

    public static ClientResponse toResponse(Client client) {
        if (client == null) {
            return null;
        }

        return new ClientResponse(
                client.getId(),
                client.getNom(),
                client.getPrenom(),
                client.getEmail(),
                client.getTelephone(),
                client.getAdresse(),
                client.getDateInscription(),
                client.getStatut(),
                client.getComptes() != null ? client.getComptes().size() : 0,
                client.isPremierConnexion()
        );
    }

    public static void updateFromRequest(Client client, ClientRequest request) {
        if (client == null || request == null) {
            return;
        }

        if (request.nom() != null && !request.nom().isEmpty()) {
            client.setNom(request.nom());
        }

        if (request.prenom() != null && !request.prenom().isEmpty()) {
            client.setPrenom(request.prenom());
        }

        if (request.email() != null && !request.email().isEmpty()) {
            client.setEmail(request.email());
        }

        if (request.telephone() != null && !request.telephone().isEmpty()) {
            client.setTelephone(request.telephone());
        }

        if (request.adresse() != null && !request.adresse().isEmpty()) {
            client.setAdresse(request.adresse());
        }

        if (request.statut() != null && !request.statut().isEmpty()) {
            client.setStatut(request.statut());
        }
    }
}