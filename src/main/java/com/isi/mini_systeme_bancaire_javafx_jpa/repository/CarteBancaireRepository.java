package com.isi.mini_systeme_bancaire_javafx_jpa.repository;



import com.isi.mini_systeme_bancaire_javafx_jpa.model.CarteBancaire;
import com.isi.mini_systeme_bancaire_javafx_jpa.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class CarteBancaireRepository {

    public List<CarteBancaire> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<CarteBancaire> query = em.createQuery("SELECT c FROM CarteBancaire c", CarteBancaire.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<CarteBancaire> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            CarteBancaire carteBancaire = em.find(CarteBancaire.class, id);
            return Optional.ofNullable(carteBancaire);
        } finally {
            em.close();
        }
    }

    public Optional<CarteBancaire> findByNumero(String numero) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<CarteBancaire> query = em.createQuery(
                    "SELECT c FROM CarteBancaire c WHERE c.numero = :numero", CarteBancaire.class);
            query.setParameter("numero", numero);
            List<CarteBancaire> cartes = query.getResultList();
            return cartes.isEmpty() ? Optional.empty() : Optional.of(cartes.get(0));
        } finally {
            em.close();
        }
    }

    public List<CarteBancaire> findByCompteId(Long compteId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<CarteBancaire> query = em.createQuery(
                    "SELECT c FROM CarteBancaire c WHERE c.compte.id = :compteId", CarteBancaire.class);
            query.setParameter("compteId", compteId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public CarteBancaire save(CarteBancaire carteBancaire) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (carteBancaire.getId() == null) {
                em.persist(carteBancaire);
            } else {
                carteBancaire = em.merge(carteBancaire);
            }
            em.getTransaction().commit();
            return carteBancaire;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(CarteBancaire carteBancaire) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(carteBancaire)) {
                carteBancaire = em.merge(carteBancaire);
            }
            em.remove(carteBancaire);
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
            CarteBancaire carteBancaire = em.find(CarteBancaire.class, id);
            if (carteBancaire != null) {
                em.remove(carteBancaire);
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