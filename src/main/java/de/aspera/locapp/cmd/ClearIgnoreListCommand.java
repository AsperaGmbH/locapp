package de.aspera.locapp.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.IgnoredItemFacade;

public class ClearIgnoreListCommand implements CommandRunnable {
    private static final Logger logger = Logger.getLogger(ClearIgnoreListCommand.class.getName());

    @Override
    public void run() {
        try {
            new IgnoredItemFacade().removeAll();
            logger.log(Level.INFO, "All IgnoredItem entries were deleted!");
        } catch (DatabaseException e) {
            logger.severe(e.getMessage());
        }
    }
}
