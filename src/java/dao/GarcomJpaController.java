/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import modelo.Garcom;
import modelo.Pedido;

/**
 *
 * @author Samia
 */
public class GarcomJpaController implements Serializable {

    public GarcomJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Garcom garcom) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido pedido = garcom.getPedido();
            if (pedido != null) {
                pedido = em.getReference(pedido.getClass(), pedido.getId());
                garcom.setPedido(pedido);
            }
            em.persist(garcom);
            if (pedido != null) {
                Garcom oldGarcomOfPedido = pedido.getGarcom();
                if (oldGarcomOfPedido != null) {
                    oldGarcomOfPedido.setPedido(null);
                    oldGarcomOfPedido = em.merge(oldGarcomOfPedido);
                }
                pedido.setGarcom(garcom);
                pedido = em.merge(pedido);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Garcom garcom) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Garcom persistentGarcom = em.find(Garcom.class, garcom.getId());
            Pedido pedidoOld = persistentGarcom.getPedido();
            Pedido pedidoNew = garcom.getPedido();
            if (pedidoNew != null) {
                pedidoNew = em.getReference(pedidoNew.getClass(), pedidoNew.getId());
                garcom.setPedido(pedidoNew);
            }
            garcom = em.merge(garcom);
            if (pedidoOld != null && !pedidoOld.equals(pedidoNew)) {
                pedidoOld.setGarcom(null);
                pedidoOld = em.merge(pedidoOld);
            }
            if (pedidoNew != null && !pedidoNew.equals(pedidoOld)) {
                Garcom oldGarcomOfPedido = pedidoNew.getGarcom();
                if (oldGarcomOfPedido != null) {
                    oldGarcomOfPedido.setPedido(null);
                    oldGarcomOfPedido = em.merge(oldGarcomOfPedido);
                }
                pedidoNew.setGarcom(garcom);
                pedidoNew = em.merge(pedidoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = garcom.getId();
                if (findGarcom(id) == null) {
                    throw new NonexistentEntityException("The garcom with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Garcom garcom;
            try {
                garcom = em.getReference(Garcom.class, id);
                garcom.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The garcom with id " + id + " no longer exists.", enfe);
            }
            Pedido pedido = garcom.getPedido();
            if (pedido != null) {
                pedido.setGarcom(null);
                pedido = em.merge(pedido);
            }
            em.remove(garcom);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Garcom> findGarcomEntities() {
        return findGarcomEntities(true, -1, -1);
    }

    public List<Garcom> findGarcomEntities(int maxResults, int firstResult) {
        return findGarcomEntities(false, maxResults, firstResult);
    }

    private List<Garcom> findGarcomEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Garcom.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Garcom findGarcom(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Garcom.class, id);
        } finally {
            em.close();
        }
    }

    public int getGarcomCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Garcom> rt = cq.from(Garcom.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
