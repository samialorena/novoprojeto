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
import modelo.Menu;
import modelo.Garcom;
import modelo.Caixa;
import modelo.Pedido;

/**
 *
 * @author Samia
 */
public class PedidoJpaController implements Serializable {

    public PedidoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Pedido pedido) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Menu menu = pedido.getMenu();
            if (menu != null) {
                menu = em.getReference(menu.getClass(), menu.getId());
                pedido.setMenu(menu);
            }
            Garcom garcom = pedido.getGarcom();
            if (garcom != null) {
                garcom = em.getReference(garcom.getClass(), garcom.getId());
                pedido.setGarcom(garcom);
            }
            Caixa caixa = pedido.getCaixa();
            if (caixa != null) {
                caixa = em.getReference(caixa.getClass(), caixa.getId());
                pedido.setCaixa(caixa);
            }
            em.persist(pedido);
            if (menu != null) {
                Pedido oldPedidoOfMenu = menu.getPedido();
                if (oldPedidoOfMenu != null) {
                    oldPedidoOfMenu.setMenu(null);
                    oldPedidoOfMenu = em.merge(oldPedidoOfMenu);
                }
                menu.setPedido(pedido);
                menu = em.merge(menu);
            }
            if (garcom != null) {
                Pedido oldPedidoOfGarcom = garcom.getPedido();
                if (oldPedidoOfGarcom != null) {
                    oldPedidoOfGarcom.setGarcom(null);
                    oldPedidoOfGarcom = em.merge(oldPedidoOfGarcom);
                }
                garcom.setPedido(pedido);
                garcom = em.merge(garcom);
            }
            if (caixa != null) {
                Pedido oldPedidoOfCaixa = caixa.getPedido();
                if (oldPedidoOfCaixa != null) {
                    oldPedidoOfCaixa.setCaixa(null);
                    oldPedidoOfCaixa = em.merge(oldPedidoOfCaixa);
                }
                caixa.setPedido(pedido);
                caixa = em.merge(caixa);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Pedido pedido) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pedido persistentPedido = em.find(Pedido.class, pedido.getId());
            Menu menuOld = persistentPedido.getMenu();
            Menu menuNew = pedido.getMenu();
            Garcom garcomOld = persistentPedido.getGarcom();
            Garcom garcomNew = pedido.getGarcom();
            Caixa caixaOld = persistentPedido.getCaixa();
            Caixa caixaNew = pedido.getCaixa();
            if (menuNew != null) {
                menuNew = em.getReference(menuNew.getClass(), menuNew.getId());
                pedido.setMenu(menuNew);
            }
            if (garcomNew != null) {
                garcomNew = em.getReference(garcomNew.getClass(), garcomNew.getId());
                pedido.setGarcom(garcomNew);
            }
            if (caixaNew != null) {
                caixaNew = em.getReference(caixaNew.getClass(), caixaNew.getId());
                pedido.setCaixa(caixaNew);
            }
            pedido = em.merge(pedido);
            if (menuOld != null && !menuOld.equals(menuNew)) {
                menuOld.setPedido(null);
                menuOld = em.merge(menuOld);
            }
            if (menuNew != null && !menuNew.equals(menuOld)) {
                Pedido oldPedidoOfMenu = menuNew.getPedido();
                if (oldPedidoOfMenu != null) {
                    oldPedidoOfMenu.setMenu(null);
                    oldPedidoOfMenu = em.merge(oldPedidoOfMenu);
                }
                menuNew.setPedido(pedido);
                menuNew = em.merge(menuNew);
            }
            if (garcomOld != null && !garcomOld.equals(garcomNew)) {
                garcomOld.setPedido(null);
                garcomOld = em.merge(garcomOld);
            }
            if (garcomNew != null && !garcomNew.equals(garcomOld)) {
                Pedido oldPedidoOfGarcom = garcomNew.getPedido();
                if (oldPedidoOfGarcom != null) {
                    oldPedidoOfGarcom.setGarcom(null);
                    oldPedidoOfGarcom = em.merge(oldPedidoOfGarcom);
                }
                garcomNew.setPedido(pedido);
                garcomNew = em.merge(garcomNew);
            }
            if (caixaOld != null && !caixaOld.equals(caixaNew)) {
                caixaOld.setPedido(null);
                caixaOld = em.merge(caixaOld);
            }
            if (caixaNew != null && !caixaNew.equals(caixaOld)) {
                Pedido oldPedidoOfCaixa = caixaNew.getPedido();
                if (oldPedidoOfCaixa != null) {
                    oldPedidoOfCaixa.setCaixa(null);
                    oldPedidoOfCaixa = em.merge(oldPedidoOfCaixa);
                }
                caixaNew.setPedido(pedido);
                caixaNew = em.merge(caixaNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = pedido.getId();
                if (findPedido(id) == null) {
                    throw new NonexistentEntityException("The pedido with id " + id + " no longer exists.");
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
            Pedido pedido;
            try {
                pedido = em.getReference(Pedido.class, id);
                pedido.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pedido with id " + id + " no longer exists.", enfe);
            }
            Menu menu = pedido.getMenu();
            if (menu != null) {
                menu.setPedido(null);
                menu = em.merge(menu);
            }
            Garcom garcom = pedido.getGarcom();
            if (garcom != null) {
                garcom.setPedido(null);
                garcom = em.merge(garcom);
            }
            Caixa caixa = pedido.getCaixa();
            if (caixa != null) {
                caixa.setPedido(null);
                caixa = em.merge(caixa);
            }
            em.remove(pedido);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Pedido> findPedidoEntities() {
        return findPedidoEntities(true, -1, -1);
    }

    public List<Pedido> findPedidoEntities(int maxResults, int firstResult) {
        return findPedidoEntities(false, maxResults, firstResult);
    }

    private List<Pedido> findPedidoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Pedido.class));
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

    public Pedido findPedido(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pedido.class, id);
        } finally {
            em.close();
        }
    }

    public int getPedidoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Pedido> rt = cq.from(Pedido.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
