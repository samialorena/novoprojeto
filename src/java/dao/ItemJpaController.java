/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import modelo.Tipo;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import modelo.Item;

/**
 *
 * @author Samia
 */
public class ItemJpaController implements Serializable {

    public ItemJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Item item) {
        if (item.getTipos() == null) {
            item.setTipos(new ArrayList<Tipo>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Tipo> attachedTipos = new ArrayList<Tipo>();
            for (Tipo tiposTipoToAttach : item.getTipos()) {
                tiposTipoToAttach = em.getReference(tiposTipoToAttach.getClass(), tiposTipoToAttach.getId());
                attachedTipos.add(tiposTipoToAttach);
            }
            item.setTipos(attachedTipos);
            em.persist(item);
            for (Tipo tiposTipo : item.getTipos()) {
                Item oldItemOfTiposTipo = tiposTipo.getItem();
                tiposTipo.setItem(item);
                tiposTipo = em.merge(tiposTipo);
                if (oldItemOfTiposTipo != null) {
                    oldItemOfTiposTipo.getTipos().remove(tiposTipo);
                    oldItemOfTiposTipo = em.merge(oldItemOfTiposTipo);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Item item) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Item persistentItem = em.find(Item.class, item.getId());
            List<Tipo> tiposOld = persistentItem.getTipos();
            List<Tipo> tiposNew = item.getTipos();
            List<Tipo> attachedTiposNew = new ArrayList<Tipo>();
            for (Tipo tiposNewTipoToAttach : tiposNew) {
                tiposNewTipoToAttach = em.getReference(tiposNewTipoToAttach.getClass(), tiposNewTipoToAttach.getId());
                attachedTiposNew.add(tiposNewTipoToAttach);
            }
            tiposNew = attachedTiposNew;
            item.setTipos(tiposNew);
            item = em.merge(item);
            for (Tipo tiposOldTipo : tiposOld) {
                if (!tiposNew.contains(tiposOldTipo)) {
                    tiposOldTipo.setItem(null);
                    tiposOldTipo = em.merge(tiposOldTipo);
                }
            }
            for (Tipo tiposNewTipo : tiposNew) {
                if (!tiposOld.contains(tiposNewTipo)) {
                    Item oldItemOfTiposNewTipo = tiposNewTipo.getItem();
                    tiposNewTipo.setItem(item);
                    tiposNewTipo = em.merge(tiposNewTipo);
                    if (oldItemOfTiposNewTipo != null && !oldItemOfTiposNewTipo.equals(item)) {
                        oldItemOfTiposNewTipo.getTipos().remove(tiposNewTipo);
                        oldItemOfTiposNewTipo = em.merge(oldItemOfTiposNewTipo);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = item.getId();
                if (findItem(id) == null) {
                    throw new NonexistentEntityException("The item with id " + id + " no longer exists.");
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
            Item item;
            try {
                item = em.getReference(Item.class, id);
                item.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The item with id " + id + " no longer exists.", enfe);
            }
            List<Tipo> tipos = item.getTipos();
            for (Tipo tiposTipo : tipos) {
                tiposTipo.setItem(null);
                tiposTipo = em.merge(tiposTipo);
            }
            em.remove(item);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Item> findItemEntities() {
        return findItemEntities(true, -1, -1);
    }

    public List<Item> findItemEntities(int maxResults, int firstResult) {
        return findItemEntities(false, maxResults, firstResult);
    }

    private List<Item> findItemEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Item.class));
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

    public Item findItem(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Item.class, id);
        } finally {
            em.close();
        }
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Item> rt = cq.from(Item.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
