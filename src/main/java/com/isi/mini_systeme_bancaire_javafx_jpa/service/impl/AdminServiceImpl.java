//package com.isi.mini_systeme_bancaire_javafx_jpa.service.impl;
//
//import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;
//import com.isi.mini_systeme_bancaire_javafx_jpa.repository.AdminRepository;
//import com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces.AdminService;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Email;
//import com.isi.mini_systeme_bancaire_javafx_jpa.utils.Notification;
//
//import java.util.List;
//import java.util.Optional;
//
//public class AdminServiceImpl implements AdminService {
//
//    private final AdminRepository adminRepository;
//
//    public AdminServiceImpl() {
//        this.adminRepository = new AdminRepository();
//    }
//
//    @Override
//    public List<Admin> getAllAdmins() {
//        return adminRepository.findAll();
//    }
//
//    @Override
//    public Optional<Admin> getAdminById(Long id) {
//        return adminRepository.findById(id);
//    }
//
//    @Override
//    public Optional<Admin> getAdminByUsername(String username) {
//        return adminRepository.findByUsername(username);
//    }
//
//    @Override
//    public Admin createAdmin(String username, String password, String role) {
//        // Vérifier si l'administrateur existe déjà
//        if (adminRepository.findByUsername(username).isPresent()) {
//            Notification.notifWarning("Erreur de création", "Un administrateur avec ce nom d'utilisateur existe déjà");
//            return null;
//        }
//
//        // Créer un nouvel administrateur
//        Admin admin = new Admin();
//        admin.setUsername(username);
//        admin.setPassword("P@sser123"); // Mot de passe par défaut
//        admin.setRole(role != null && !role.isEmpty() ? role : "ROLE_ADMIN");
//        admin.setFirstLogin(true); // Premier login, doit changer le mot de passe
//
//        // Sauvegarder l'administrateur
//        admin = adminRepository.save(admin);
//
//        // Envoyer un email avec les informations de connexion (si un email est fourni)
//        if (username.contains("@")) { // Vérifier si le username est un email
//            try {
//                String message = "Bonjour,\n\n" +
//                        "Votre compte administrateur a été créé dans le Mini Système Bancaire.\n\n" +
//                        "Vos informations de connexion sont les suivantes:\n" +
//                        "Nom d'utilisateur: " + username + "\n" +
//                        "Mot de passe: P@sser123\n\n" +
//                        "Lors de votre première connexion, vous serez invité à changer votre mot de passe.\n\n" +
//                        "Cordialement,\n" +
//                        "L'équipe du Mini Système Bancaire";
//
//                Email.sendCustomEmail(username, "Votre compte administrateur - Mini Système Bancaire", message);
//            } catch (Exception e) {
//                Notification.notifWarning("Email", "Impossible d'envoyer l'email de confirmation: " + e.getMessage());
//            }
//        }
//
//        return admin;
//    }
//
//    @Override
//    public Optional<Admin> updateAdmin(Long id, String username, String password, String role) {
//        return adminRepository.findById(id)
//                .map(admin -> {
//                    // Mettre à jour les champs
//                    if (username != null && !username.isEmpty()) {
//                        // Vérifier que le nouveau username n'est pas déjà utilisé par un autre admin
//                        Optional<Admin> existingAdmin = adminRepository.findByUsername(username);
//                        if (existingAdmin.isPresent() && !existingAdmin.get().getId().equals(id)) {
//                            Notification.notifWarning("Erreur de mise à jour", "Ce nom d'utilisateur est déjà utilisé");
//                            return null;
//                        }
//                        admin.setUsername(username);
//                    }
//
//                    if (password != null && !password.isEmpty()) {
//                        admin.setPassword(password);
//                        admin.setFirstLogin(false); // Si le mot de passe est mis à jour, ce n'est plus le premier login
//                    }
//
//                    if (role != null && !role.isEmpty()) {
//                        admin.setRole(role);
//                    }
//
//                    // Sauvegarder les modifications
//                    return adminRepository.save(admin);
//                });
//    }
//
//    @Override
//    public boolean deleteAdmin(Long id) {
//        try {
//            adminRepository.deleteById(id);
//            return true;
//        } catch (Exception e) {
//            Notification.notifError("Erreur de suppression",
//                    "Impossible de supprimer l'administrateur: " + e.getMessage());
//            return false;
//        }
//    }
//
//    @Override
//    public boolean authenticate(String username, String password) {
//        return adminRepository.authenticate(username, password);
//    }
//
//    @Override
//    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
//        Optional<Admin> adminOpt = adminRepository.findById(id);
//        if (adminOpt.isEmpty()) {
//            return false;
//        }
//
//        Admin admin = adminOpt.get();
//
//        // Vérifier l'ancien mot de passe
//        if (!admin.getPassword().equals(oldPassword)) {
//            Notification.notifError("Erreur", "Ancien mot de passe incorrect");
//            return false;
//        }
//
//        // Mettre à jour le mot de passe
//        admin.setPassword(newPassword);
//        admin.setFirstLogin(false);
//        adminRepository.save(admin);
//
//        return true;
//    }
//}