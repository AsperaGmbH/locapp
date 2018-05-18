package de.aspera.locapp.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;

public class ClearLocalizationCommand implements CommandRunnable {

    private static final Logger LOGGER = Logger.getLogger(ClearLocalizationCommand.class.getName());

    @Override
    public void run() {
        try {
            new LocalizationFacade().removeAll();
            LOGGER.log(Level.INFO, "All Localization entries were deleted!");
        } catch (DatabaseException e) {
            LOGGER.severe(e.getMessage());
        }
    }
}
