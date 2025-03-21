package com.isi.mini_systeme_bancaire_javafx_jpa.repository;

import com.isi.mini_systeme_bancaire_javafx_jpa.model.Transaction;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class TransactionRepository {

    public List<Transaction> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery("SELECT t FROM Transaction t ORDER BY t.date DESC", Transaction.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Transaction> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Transaction transaction = em.find(Transaction.class, id);
            return Optional.ofNullable(transaction);
        } finally {
            em.close();
        }
    }

    public List<Transaction> findByCompteId(Long compteId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.compte.id = :compteId ORDER BY t.date DESC", Transaction.class);
            query.setParameter("compteId", compteId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Transaction> findTransactionsByClientId(Long clientId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.compte.client.id = :clientId ORDER BY t.date DESC",
                    Transaction.class);
            query.setParameter("clientId", clientId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Transaction save(Transaction transaction) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (transaction.getId() == null) {
                em.persist(transaction);
            } else {
                transaction = em.merge(transaction);
            }
            em.getTransaction().commit();
            return transaction;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Transaction transaction) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(transaction)) {
                transaction = em.merge(transaction);
            }
            em.remove(transaction);
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
            Transaction transaction = em.find(Transaction.class, id);
            if (transaction != null) {
                em.remove(transaction);
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