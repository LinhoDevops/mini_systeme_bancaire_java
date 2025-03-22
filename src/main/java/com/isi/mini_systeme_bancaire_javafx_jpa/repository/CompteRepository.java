package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.enums.TypeCompte;
import com.isi.mini_systeme_bancaire_javafx_jpa.model.Compte;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CompteRepository {

    public List<Compte> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c LEFT JOIN FETCH c.client ORDER BY c.dateCreation DESC",
                    Compte.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Compte> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c LEFT JOIN FETCH c.client WHERE c.id = :id",
                    Compte.class);
            query.setParameter("id", id);
            try {
                Compte compte = query.getSingleResult();
                return Optional.of(compte);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        } finally {
            em.close();
        }
    }

    public Optional<Compte> findByNumero(String numero) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c LEFT JOIN FETCH c.client WHERE c.numero = :numero",
                    Compte.class);
            query.setParameter("numero", numero);
            try {
                Compte compte = query.getSingleResult();
                return Optional.of(compte);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        } finally {
            em.close();
        }
    }

    public List<Compte> findByClientId(Long clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c WHERE c.client.id = :clientId ORDER BY c.dateCreation DESC",
                    Compte.class);
            query.setParameter("clientId", clientId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Compte> findByType(TypeCompte type) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c LEFT JOIN FETCH c.client WHERE c.type = :type ORDER BY c.dateCreation DESC",
                    Compte.class);
            query.setParameter("type", type);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Compte> findByStatut(String statut) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c LEFT JOIN FETCH c.client WHERE c.statut = :statut ORDER BY c.dateCreation DESC",
                    Compte.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Compte> findByDateCreationBetween(LocalDate dateDebut, LocalDate dateFin) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c LEFT JOIN FETCH c.client WHERE c.dateCreation BETWEEN :dateDebut AND :dateFin ORDER BY c.dateCreation DESC",
                    Compte.class);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Compte> searchComptes(String searchTerm) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Compte> query = em.createQuery(
                    "SELECT c FROM Compte c LEFT JOIN FETCH c.client cl WHERE " +
                            "c.numero LIKE :searchTerm OR " +
                            "LOWER(c.type) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(c.statut) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(cl.nom) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(cl.prenom) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(cl.email) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(CONCAT(cl.nom, ' ', cl.prenom)) LIKE LOWER(:searchTerm)",
                    Compte.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Somme des soldes de tous les comptes
    public double getSommeSoldes() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT SUM(c.solde) FROM Compte c", Double.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    // Compter le nombre total de comptes
    public long countAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Compte c", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    // Compter le nombre de comptes par type
    public long countByType(TypeCompte type) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Compte c WHERE c.type = :type", Long.class);
            query.setParameter("type", type);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    // Compter le nombre de comptes par statut
    public long countByStatut(String statut) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Compte c WHERE c.statut = :statut", Long.class);
            query.setParameter("statut", statut);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public Compte save(Compte compte) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (compte.getId() == null) {
                em.persist(compte);
            } else {
                compte = em.merge(compte);
            }
            em.getTransaction().commit();
            return compte;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Compte compte) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(compte)) {
                compte = em.merge(compte);
            }
            em.remove(compte);
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
            Compte compte = em.find(Compte.class, id);
            if (compte != null) {
                em.remove(compte);
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