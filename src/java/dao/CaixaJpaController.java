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
import modelo.Caixa;
import modelo.Pedido;

/**
 *
 * @author Samia
 */
public class CaixaJpaController implements Serializable {

    public CaixaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Caixa caixa) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido pedido = caixa.getPedido();
            if (pedido != null) {
                pedido = em.getReference(pedido.getClass(), pedido.getId());
                caixa.setPedido(pedido);
            }
            em.persist(caixa);
            if (pedido != null) {
                Caixa oldCaixaOfPedido = pedido.getCaixa();
                if (oldCaixaOfPedido != null) {
                    oldCaixaOfPedido.setPedido(null);
                    oldCaixaOfPedido = em.merge(oldCaixaOfPedido);
                }
                pedido.setCaixa(caixa);
                pedido = em.merge(pedido);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Caixa caixa) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Caixa persistentCaixa = em.find(Caixa.class, caixa.getId());
            Pedido pedidoOld = persistentCaixa.getPedido();
            Pedido pedidoNew = caixa.getPedido();
            if (pedidoNew != null) {
                pedidoNew = em.getReference(pedidoNew.getClass(), pedidoNew.getId());
                caixa.setPedido(pedidoNew);
            }
            caixa = em.merge(caixa);
            if (pedidoOld != null && !pedidoOld.equals(pedidoNew)) {
                pedidoOld.setCaixa(null);
                pedidoOld = em.merge(pedidoOld);
            }
            if (pedidoNew != null && !pedidoNew.equals(pedidoOld)) {
                Caixa oldCaixaOfPedido = pedidoNew.getCaixa();
                if (oldCaixaOfPedido != null) {
                    oldCaixaOfPedido.setPedido(null);
                    oldCaixaOfPedido = em.merge(oldCaixaOfPedido);
                }
                pedidoNew.setCaixa(caixa);
                pedidoNew = em.merge(pedidoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = caixa.getId();
                if (findCaixa(id) == null) {
                    throw new NonexistentEntityException("The caixa with id " + id + " no longer exists.");
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
            Caixa caixa;
            try {
                caixa = em.getReference(Caixa.class, id);
                caixa.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The caixa with id " + id + " no longer exists.", enfe);
            }
            Pedido pedido = caixa.getPedido();
            if (pedido != null) {
                pedido.setCaixa(null);
                pedido = em.merge(pedido);
            }
            em.remove(caixa);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Caixa> findCaixaEntities() {
        return findCaixaEntities(true, -1, -1);
    }

    public List<Caixa> findCaixaEntities(int maxResults, int firstResult) {
        return findCaixaEntities(false, maxResults, firstResult);
    }

    private List<Caixa> findCaixaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Caixa.class));
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

    public Caixa findCaixa(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Caixa.class, id);
        } finally {
            em.close();
        }
    }

    public int getCaixaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Caixa> rt = cq.from(Caixa.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
