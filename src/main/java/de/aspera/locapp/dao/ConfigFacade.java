package de.aspera.locapp.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Query;

import de.aspera.locapp.dto.Config;
import de.aspera.locapp.util.ValidationHelper;

/**
 *
 * @author Bjoern.Buchholz
 *
 */
public class ConfigFacade extends AbstractFacade<Config> {

    private static final Logger LOGGER = Logger.getLogger(ConfigFacade.class.getName());

    public ConfigFacade() {
        super(Config.class);
    }

    public String[] getValue(String key) throws DatabaseException {
        getEntityManager().getTransaction().begin();
        String queryStr = "select target.value from " + Config.class.getSimpleName() + " target where target.key =:key";
        Query query = getEntityManager().createQuery(queryStr);
        query.setParameter("key", key);
        String value = (String) query.getSingleResult();
        String[] values = value.split(",");
        getEntityManager().getTransaction().commit();
        return values;
    }

    public void saveConfig(List<Config> configs) throws DatabaseException {
        try {
            getEntityManager().getTransaction().begin();
            for (Config config : configs) {
                ValidationHelper.validateBean(config);
                getEntityManager().persist(config);
            }
            getEntityManager().getTransaction().commit();

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().setRollbackOnly();
            }
            throw new DatabaseException(e.getMessage(), e);
        }
    }
}
