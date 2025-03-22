package com.isi.mini_systeme_bancaire_javafx_jpa.service.interfaces;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminService {

    List<Admin> getAllAdmins();

    Optional<Admin> getAdminById(Long id);

    Optional<Admin> getAdminByUsername(String username);

    Admin createAdmin(String username, String password, String role);

    Optional<Admin> updateAdmin(Long id, String username, String password, String role);

    boolean deleteAdmin(Long id);

    boolean authenticate(String username, String password);

    boolean updatePassword(Long id, String oldPassword, String newPassword);
}