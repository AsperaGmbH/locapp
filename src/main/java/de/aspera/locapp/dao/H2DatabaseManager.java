/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.aspera.locapp.dao;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import de.aspera.locapp.util.Resources;

/**
 * Der H2DatabaseManager ist ein Singleton zur Verwendung der lokalen JPA
 * Datenbankverbindung.
 *
 * @author daniel
 */
public class H2DatabaseManager {

    private static H2DatabaseManager instance;
    private static EntityManager theManager;
    private static final Map<String, String> databaseProperties = new HashMap<>();

    private H2DatabaseManager() {
    }

    public static H2DatabaseManager getInstance() {
        if (instance == null) {
            instance = new H2DatabaseManager();
            instance.init();
        }
        return instance;
    }

    /**
     * Voreinstellung für die Datenbank.
     */
    private void init() {

        Object dbname = System.getProperties().get("DBNAME");
        Object dbaction = System.getProperties().get("DBACTION");

        if (dbaction == null || "".equals(dbaction)) {
            dbaction = "create"; // "drop-and-create";
        }

        // AUTO_SERVER=TRUE ermöglicht den Zugriff für mehrere Apps auf die
        // gleich Datenbank!
        databaseProperties.put("javax.persistence.jdbc.url",
                "jdbc:h2:~/." + Resources.PROJECT_NAME + "/"
                        + (dbname != null ? dbname.toString() : Resources.getInstance().getProperty("db-name"))
                        + ";AUTO_SERVER=TRUE");
        databaseProperties.put("javax.persistence.jdbc.user", Resources.getInstance().getProperty("db-user"));
        databaseProperties.put("javax.persistence.jdbc.password", Resources.getInstance().getProperty("db-password"));

        databaseProperties.put("javax.persistence.jdbc.driver", "org.h2.Driver");
        databaseProperties.put("javax.persistence.schema-generation.database.action", dbaction.toString());
        // databaseProperties.put("eclipselink.logging.level", "WARNING");
        // databaseProperties.put("eclipselink.logging.parameters", "true");

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("h2locapp", databaseProperties);
        theManager = factory.createEntityManager();
    }

    public EntityManager getEntityManager() {
        return H2DatabaseManager.theManager;
    }

}
