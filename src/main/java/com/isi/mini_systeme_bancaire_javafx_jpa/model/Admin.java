package com.isi.mini_systeme_bancaire_javafx_jpa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"tickets"})
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role; // ROLE_ADMIN, ROLE_SUPER_ADMIN
    private String email; // Pour les notifications
    private String nom;
    private String prenom;
    private String telephone;
    private boolean premierConnexion = true; // Pour forcer le changement de mot de passe

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketSupport> tickets = new ArrayList<>();

    // Constructeur avec champs principaux
    public Admin(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.premierConnexion = true;
    }

    // Constructeur complet
    public Admin(String username, String password, String role, String email, String nom, String prenom, String telephone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.premierConnexion = true;
    }

    public String getNomComplet() {
        return this.nom + " " + this.prenom;
    }
}