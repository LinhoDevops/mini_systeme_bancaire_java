package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Client;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class ClientRepository {

    public List<Client> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery("SELECT c FROM Client c ORDER BY c.nom, c.prenom", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Méthode pour charger les clients avec leurs comptes
    public List<Client> findAllWithComptes() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                    "SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.comptes ORDER BY c.nom, c.prenom", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Client> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Client client = em.find(Client.class, id);
            return Optional.ofNullable(client);
        } finally {
            em.close();
        }
    }

    // Méthode pour charger un client avec ses comptes
    public Optional<Client> findByIdWithComptes(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                    "SELECT c FROM Client c LEFT JOIN FETCH c.comptes WHERE c.id = :id", Client.class);
            query.setParameter("id", id);
            List<Client> clients = query.getResultList();
            return clients.isEmpty() ? Optional.empty() : Optional.of(clients.get(0));
        } finally {
            em.close();
        }
    }

    public Optional<Client> findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                    "SELECT c FROM Client c WHERE c.email = :email", Client.class);
            query.setParameter("email", email);
            try {
                Client client = query.getSingleResult();
                return Optional.of(client);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        } finally {
            em.close();
        }
    }

    public Optional<Client> findByTelephone(String telephone) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                    "SELECT c FROM Client c WHERE c.telephone = :telephone", Client.class);
            query.setParameter("telephone", telephone);
            try {
                Client client = query.getSingleResult();
                return Optional.of(client);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        } finally {
            em.close();
        }
    }

    public List<Client> searchClients(String searchTerm) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                    "SELECT c FROM Client c WHERE " +
                            "LOWER(c.nom) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.prenom) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.email) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.telephone) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(CONCAT(c.nom, ' ', c.prenom)) LIKE LOWER(:searchTerm)", Client.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Méthode de recherche avec chargement des comptes
    public List<Client> searchClientsWithComptes(String searchTerm) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                    "SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.comptes WHERE " +
                            "LOWER(c.nom) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.prenom) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.email) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.telephone) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(CONCAT(c.nom, ' ', c.prenom)) LIKE LOWER(:searchTerm)", Client.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Compter le nombre total de clients
    public long countAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Client c", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    // Compter le nombre de clients par statut
    public long countByStatut(String statut) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Client c WHERE c.statut = :statut", Long.class);
            query.setParameter("statut", statut);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public Client save(Client client) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (client.getId() == null) {
                em.persist(client);
            } else {
                client = em.merge(client);
            }
            em.getTransaction().commit();
            return client;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Client client) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(client)) {
                client = em.merge(client);
            }
            em.remove(client);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Client client = em.find(Client.class, id);
            if (client != null) {
                em.remove(client);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}