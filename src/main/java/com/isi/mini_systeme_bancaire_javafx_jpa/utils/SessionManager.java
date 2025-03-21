package com.isi.mini_systeme_bancaire_javafx_jpa.utils;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;

/**
 * Classe utilitaire pour gérer les sessions des utilisateurs connectés
 */
public class SessionManager {
    // Instances des utilisateurs actuellement connectés
    private static Admin currentAdmin;
    private static Client currentClient;

    /**
     * Définit l'administrateur actuellement connecté
     * @param admin L'administrateur connecté
     */
    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
    }

    /**
     * Récupère l'administrateur actuellement connecté
     * @return L'administrateur connecté ou null si aucun administrateur n'est connecté
     */
    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    /**
     * Définit le client actuellement connecté
     * @param client Le client connecté
     */
    public static void setCurrentClient(Client client) {
        currentClient = client;
    }

    /**
     * Récupère le client actuellement connecté
     * @return Le client connecté ou null si aucun client n'est connecté
     */
    public static Client getCurrentClient() {
        return currentClient;
    }

    /**
     * Vérifie si un administrateur est actuellement connecté
     * @return true si un administrateur est connecté, false sinon
     */
    public static boolean isAdminLoggedIn() {
        return currentAdmin != null;
    }

    /**
     * Vérifie si un client est actuellement connecté
     * @return true si un client est connecté, false sinon
     */
    public static boolean isClientLoggedIn() {
        return currentClient != null;
    }

    /**
     * Déconnecte l'utilisateur actuel (client ou administrateur)
     */
    public static void logout() {
        currentAdmin = null;
        currentClient = null;
    }
}