package de.aspera.locapp.cmd;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.aspera.locapp.dao.ConfigFacade;
import de.aspera.locapp.dao.DatabaseException;

public class SetDefaultLanguageCommand implements CommandRunnable {
    private ConfigFacade configFacade = new ConfigFacade();
    private Logger logger = Logger.getLogger(SetDefaultLanguageCommand.class.getName());

    @Override
    public void run() {
        try {
            setDefaultLanguage();
        } catch (DatabaseException | CommandException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void setDefaultLanguage() 
        throws CommandException, DatabaseException {
        var language = CommandContext.getInstance()
            .nextArgument()
            .toLowerCase();

        var locale = new Locale(language);

		try {
			locale.getISO3Language();
		} catch (MissingResourceException e) {
			throw new CommandException("Language [" + language + "] is invalid");
		}

        configFacade.setDefaultLanguage(locale);
    }
}
