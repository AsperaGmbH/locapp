package de.aspera.locapp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author daniel
 */
public class Resources {

    private static Resources resources;
    private static Properties systemProperties;
    private static final Logger LOGGER = Logger.getLogger(Resources.class.getName());
    public final static String PROJECT_NAME = "locapp";

    /**
     * Privater leerer Standardkonstruktor.
     */
    private Resources() {
        systemProperties = new Properties();

    }

    /**
     * Liefert eine statische Instanz für die Ressourcen.
     *
     * @return die Ressourcen.
     */
    public static synchronized Resources getInstance() {
        if (resources == null) {
            initializeResources();
        }
        return resources;
    }

    /**
     * Initialisiert die System Properties. Liest die Properties aus der
     * Konfigurationsdatei. Der Name der Konfigurationsdatei ist abhängig von
     * der Anwendungsumgebung.
     */
    private static void initializeResources() {
        try {
            resources = new Resources();
            String environmentSetting = System.getProperty("de.aspera.locapp.env");

            if (environmentSetting == null || environmentSetting.length() == 0) {
                environmentSetting = "dev";
                // throw new IllegalArgumentException(
                // "You have to set the JVM property env = "
                // + "[dev|test|int|qs|prod] for your environment");
            }
            LOGGER.log(Level.FINE, "Reading system properties of environment: {0}", environmentSetting);

            String fileName = null;
            String filePath = null;
            if (SystemUtils.IS_OS_WINDOWS) {
                fileName = System.getProperty("user.home") + "\\." + PROJECT_NAME + "\\locapp_" + environmentSetting
                        + ".properties";
                filePath = System.getProperty("user.home") + "\\." + PROJECT_NAME + "";
            } else {
                fileName = System.getProperty("user.home") + "/." + PROJECT_NAME + "/locapp_" + environmentSetting
                        + ".properties";
                filePath = System.getProperty("user.home") + "/." + PROJECT_NAME;
            }

            InputStream readInConfig = null;
            try {
                readInConfig = new FileInputStream(fileName);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "The config properties could not found. So it will be created!");
            }
            if (readInConfig == null) {
                boolean mkdirs = new File(filePath).mkdirs();
                File file = new File(fileName);
                FileOutputStream fileOut = new FileOutputStream(file);
                systemProperties.put("system-env", environmentSetting);
                SecureRandom random = new SecureRandom();
                final String dbPassword = new BigInteger(130, random).toString(32);

                systemProperties.put("db-user", "locapp");
                systemProperties.put("db-password", dbPassword);
                systemProperties.put("db-name", "locapp-db");
                systemProperties.put("userPath", filePath);
                systemProperties.store(fileOut, "locapp Application Config");
                fileOut.close();
            }
            resources.systemProperties.load(new FileInputStream(fileName));
            // add logger configuration
            Enumeration propNames = resources.systemProperties.propertyNames();
            while (propNames.hasMoreElements()) {
                String propName = propNames.nextElement().toString();
                String value = resources.systemProperties.getProperty(propName);
            }
        } catch (IOException ioex) {
            resources = null;
            String msg = "Error during loading the properties";
            LOGGER.log(Level.SEVERE, ioex.getMessage(), ioex);
            throw new IllegalStateException(msg, ioex);
        }
    }

    public String getProperty(String key) {
        return systemProperties.getProperty(key);
    }

}
