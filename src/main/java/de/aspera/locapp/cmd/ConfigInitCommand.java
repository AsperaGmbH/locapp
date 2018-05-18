package de.aspera.locapp.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.aspera.locapp.dao.ConfigFacade;
import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dto.Config;

public class ConfigInitCommand implements CommandRunnable {

    private static final Logger LOGGER = Logger.getLogger(ConfigInitCommand.class.getName());

    public static final String EXCLUDED_KEY = "Excluded_Paths";
    public static final String[] EXCLUDED_VALUES = { "target" };

    @Override
    public void run() {
        this.init();

    }

    private void init() {
        try {
            List<Config> configs = new ArrayList<>();
            ConfigFacade configFacade = new ConfigFacade();
            Config config = new Config();

            config.setKey(EXCLUDED_KEY);
            config.setValue(EXCLUDED_VALUES);
            configs.add(config);

            // if no entry found, so add the default entries
            if (configFacade.findAll().isEmpty()) {
                configFacade.saveConfig(configs);
            }
        } catch (DatabaseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
