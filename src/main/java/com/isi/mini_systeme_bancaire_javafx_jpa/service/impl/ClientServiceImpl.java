package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;

import com.isi.mini_systeme_bancaire_javafx_jpa.mapper.ClientMapper;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.repository.ClientRepository;
import com.isi.mini_systeme_bancaire_javafx_jpa.request.ClientRequest;
import com.isi.mini_systeme_bancaire_javafx_jpa.response.ClientResponse;
import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.ClientService;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl() {
        this.clientRepository = new ClientRepository();
    }

    @Override
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(ClientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClientResponse> getClientById(Long id) {
        return clientRepository.findById(id)
                .map(ClientMapper::toResponse);
    }

    @Override
    public Optional<ClientResponse> getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(ClientMapper::toResponse);
    }

    @Override
    public Optional<ClientResponse> getClientByTelephone(String telephone) {
        return clientRepository.findByTelephone(telephone)
                .map(ClientMapper::toResponse);
    }

    @Override
    public List<ClientResponse> searchClients(String searchTerm) {
        return clientRepository.searchClients(searchTerm).stream()
                .map(ClientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClientResponse createClient(ClientRequest clientRequest) {
        // Vérifier si l'email est déjà utilisé
        if (clientRepository.findByEmail(clientRequest.email()).isPresent()) {
            Notification.notifWarning("Erreur de création", "Un client avec cet email existe déjà");
            return null;
        }

        // Vérifier si le numéro de téléphone est déjà utilisé
        if (clientRepository.findByTelephone(clientRequest.telephone()).isPresent()) {
            Notification.notifWarning("Erreur de création", "Un client avec ce numéro de téléphone existe déjà");
            return null;
        }

        // Créer un nouveau client
        Client client = ClientMapper.fromRequest(clientRequest);

        // Définir la date d'inscription et le statut si non fournis
        if (client.getDateInscription() == null) {
            client.setDateInscription(LocalDate.now());
        }

        if (client.getStatut() == null || client.getStatut().isEmpty()) {
            client.setStatut("actif");
        }

        // Sauvegarder le client
        client = clientRepository.save(client);

        // Envoyer un email avec les identifiants de connexion
        try {
            String messageConnexion = "Votre compte client a été créé avec succès.\n\n" +
                    "Voici vos identifiants de connexion :\n" +
                    "Identifiant : " + client.getEmail() + " ou " + client.getTelephone() + "\n" +
                    "Mot de passe : P@sser123\n\n" +
                    "Lors de votre première connexion, vous serez invité à changer votre mot de passe.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe du Mini Système Bancaire";

            Email.sendCustomEmail(
                    client.getEmail(),
                    "Bienvenue au Mini Système Bancaire - Vos identifiants",
                    messageConnexion
            );
        } catch (Exception e) {
            Notification.notifWarning("Email", "Impossible d'envoyer l'email de bienvenue: " + e.getMessage());
        }

        return ClientMapper.toResponse(client);
    }

    @Override
    public Optional<ClientResponse> updateClient(Long id, ClientRequest clientRequest) {
        return clientRepository.findById(id)
                .map(client -> {
                    // Vérifier si l'email est modifié et s'il est déjà utilisé par un autre client
                    if (clientRequest.email() != null && !clientRequest.email().equals(client.getEmail())) {
                        Optional<Client> existingClient = clientRepository.findByEmail(clientRequest.email());
                        if (existingClient.isPresent() && !existingClient.get().getId().equals(id)) {
                            Notification.notifWarning("Erreur de modification", "Un autre client utilise déjà cet email");
                            return null;
                        }
                    }

                    // Vérifier si le téléphone est modifié et s'il est déjà utilisé par un autre client
                    if (clientRequest.telephone() != null && !clientRequest.telephone().equals(client.getTelephone())) {
                        Optional<Client> existingClient = clientRepository.findByTelephone(clientRequest.telephone());
                        if (existingClient.isPresent() && !existingClient.get().getId().equals(id)) {
                            Notification.notifWarning("Erreur de modification", "Un autre client utilise déjà ce numéro de téléphone");
                            return null;
                        }
                    }

                    // Mettre à jour les champs du client
                    ClientMapper.updateFromRequest(client, clientRequest);

                    // Sauvegarder les modifications
                    Client updatedClient = clientRepository.save(client);

                    // Notifier le client des modifications
                    try {
                        String message = "Votre profil client a été mis à jour.\n\n" +
                                "Si vous n'êtes pas à l'origine de ces modifications, veuillez contacter immédiatement notre service client.\n\n" +
                                "Cordialement,\n" +
                                "L'équipe du Mini Système Bancaire";

                        Email.sendCustomEmail(
                                updatedClient.getEmail(),
                                "Mise à jour de votre profil client",
                                message
                        );
                    } catch (Exception e) {
                        // Ignorer l'erreur d'envoi d'email pour ne pas bloquer la mise à jour
                    }

                    return ClientMapper.toResponse(updatedClient);
                });
    }

    @Override
    public boolean deleteClient(Long id) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(id);
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();

                // Vérifier si le client a des comptes actifs
                if (client.getComptes() != null && !client.getComptes().isEmpty()) {
                    Notification.notifError("Erreur de suppression",
                            "Ce client possède des comptes. Veuillez d'abord fermer tous ses comptes.");
                    return false;
                }

                // Désactiver le client au lieu de le supprimer complètement
                client.setStatut("supprimé");
                clientRepository.save(client);

                // Notifier le client
                try {
                    String message = "Votre compte client a été désactivé.\n\n" +
                            "Si vous n'êtes pas à l'origine de cette action, veuillez contacter immédiatement notre service client.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe du Mini Système Bancaire";

                    Email.sendCustomEmail(
                            client.getEmail(),
                            "Désactivation de votre compte client",
                            message
                    );
                } catch (Exception e) {
                    // Ignorer l'erreur d'envoi d'email
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            Notification.notifError("Erreur de suppression",
                    "Impossible de supprimer le client: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean activerClient(Long id) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(id);
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                client.setStatut("actif");
                clientRepository.save(client);

                // Notifier le client
                try {
                    String message = "Votre compte client a été activé.\n\n" +
                            "Vous pouvez maintenant vous connecter et accéder à tous nos services.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe du Mini Système Bancaire";

                    Email.sendCustomEmail(
                            client.getEmail(),
                            "Activation de votre compte client",
                            message
                    );
                } catch (Exception e) {
                    // Ignorer l'erreur d'envoi d'email
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            Notification.notifError("Erreur d'activation",
                    "Impossible d'activer le client: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean desactiverClient(Long id) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(id);
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                client.setStatut("inactif");
                clientRepository.save(client);

                // Notifier le client
                try {
                    String message = "Votre compte client a été désactivé temporairement.\n\n" +
                            "Si vous avez des questions, veuillez contacter notre service client.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe du Mini Système Bancaire";

                    Email.sendCustomEmail(
                            client.getEmail(),
                            "Désactivation temporaire de votre compte client",
                            message
                    );
                } catch (Exception e) {
                    // Ignorer l'erreur d'envoi d'email
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            Notification.notifError("Erreur de désactivation",
                    "Impossible de désactiver le client: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean resetPassword(Long id) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(id);
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                client.setPassword("P@sser123");
                client.setPremierConnexion(true);
                clientRepository.save(client);

                // Notifier le client
                try {
                    String message = "Votre mot de passe a été réinitialisé.\n\n" +
                            "Voici vos nouveaux identifiants de connexion :\n" +
                            "Identifiant : " + client.getEmail() + " ou " + client.getTelephone() + "\n" +
                            "Mot de passe : P@sser123\n\n" +
                            "Lors de votre prochaine connexion, vous serez invité à changer votre mot de passe.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe du Mini Système Bancaire";

                    Email.sendCustomEmail(
                            client.getEmail(),
                            "Réinitialisation de votre mot de passe",
                            message
                    );
                } catch (Exception e) {
                    // Ignorer l'erreur d'envoi d'email
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            Notification.notifError("Erreur de réinitialisation",
                    "Impossible de réinitialiser le mot de passe: " + e.getMessage());
            return false;
        }
    }

    @Override
    public long countAllClients() {
        return clientRepository.countAll();
    }

    @Override
    public long countClientsByStatut(String statut) {
        return clientRepository.countByStatut(statut);
    }

    @Override
    public Optional<Client> getClientEntityById(Long id) {
        return clientRepository.findById(id);
    }
}