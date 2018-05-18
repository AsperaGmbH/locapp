package de.aspera.locapp.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.aspera.locapp.util.ValidationHelper;

/**
 *
 * @author daniel
 * @param <T>
 */
abstract class AbstractFacade<T> {

    private final Class<T> entityClass;
    private static Logger logger;
    protected final H2DatabaseManager instance = H2DatabaseManager.getInstance();

    protected AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager() {
        return instance.getEntityManager();
    }

    public void create(T entity) throws DatabaseException {
        try {
            ValidationHelper.validateBean(entity);
            getEntityManager().getTransaction().begin();
            getEntityManager().persist(entity);
            getEntityManager().getTransaction().commit();

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().setRollbackOnly();
            }
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    protected T edit(T entity) throws DatabaseException {
        try {
            ValidationHelper.validateBean(entity);
            return getEntityManager().merge(entity);
        } catch (Exception ex) {
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().setRollbackOnly();
            }
            throw new DatabaseException(ex.getMessage(), ex);
        }
    }

    protected void remove(T entity) throws DatabaseException {
        try {
            ValidationHelper.validateBean(entity);
            getEntityManager().remove(getEntityManager().merge(entity));
        } catch (Exception ex) {
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().setRollbackOnly();
            }
            Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new DatabaseException(ex.getMessage(), ex);
        }
    }

    public void removeAll() throws DatabaseException {
        getEntityManager().getTransaction().begin();
        getEntityManager().createQuery("delete from " + entityClass.getSimpleName()).executeUpdate();
        getEntityManager().getTransaction().commit();
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        CriteriaQuery<Object> cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return (List<T>) getEntityManager().createQuery(cq).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<T> findRange(int[] range) {
        CriteriaQuery<Object> cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return (List<T>) q.getResultList();
    }

    public int count() {
        CriteriaQuery<Object> cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    protected Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }
}
