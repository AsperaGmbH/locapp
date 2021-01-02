package de.aspera.locapp.dao;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.aspera.locapp.dto.IgnoredItem;

public class IgnoredItemFacade extends AbstractFacade<IgnoredItem> {
    private static final Logger logger = Logger.getLogger(IgnoredItemFacade.class.getName());

    public IgnoredItemFacade() {
        super(IgnoredItem.class);
    }
    
    public boolean isIgnored(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }

        try {
            var queryStr = "select * from IgnoredItem.name where IgnoredItem.name = :name";
            var query = getEntityManager().createQuery(queryStr);
            query.setParameter("name", name);
            
            var ignoredItem = query.getResultList();
            return ignoredItem.size() > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while executing query.", e);
            return false;
        }
    }
}
