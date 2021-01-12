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
        String selectedLanguage = CommandContext.getInstance()
			    .nextArgument();
		var language = selectedLanguage != null ? selectedLanguage.toLowerCase()
				: ConfigFacade.DEFAULT_LANGUAGE.toLowerCase();
		
		Locale locale;
		try {
			locale = new Locale(language);
			locale.getISO3Language();
		} catch (MissingResourceException e) {
			logger.log(Level.SEVERE, "Language [" + language + "] is invalid. [" + ConfigFacade.DEFAULT_LANGUAGE.toLowerCase() + "] was selected as default language!");
			locale = new Locale(ConfigFacade.DEFAULT_LANGUAGE.toLowerCase());
		}
        configFacade.setDefaultLanguage(locale);
        logger.log(Level.INFO, "The configured default language is [" + locale.getLanguage() + "]");
    }
}
