package de.aspera.locapp.cmd;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.Localization.Status;

public class PropertiesCounterCommand implements CommandRunnable {

    private static final Logger logger = Logger.getLogger(PropertiesCounterCommand.class.getName());
    private LocalizationFacade locFacade = new LocalizationFacade();

    @Override
    public void run() {
        try {
            countProperties(CommandContext.getInstance().allArguments());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    private void countProperties(String... options) throws DatabaseException {

        if (options == null || options.length == 0) {
            logger.log(Level.WARNING, "No command parameters was found!");
            return;
        }
        String localizationType = options[0];
        String language = null;
        boolean emptyProperties = false;

        if (options.length == 2) {
            if (options[1].equals("1")) {
                emptyProperties = true;
            } else {
                language = options[1];
            }
        } else if (options.length == 3) {
            language = options[1];
            emptyProperties = options[2].equals("1");
        }

        if (StringUtils.isEmpty(localizationType)) {
            logger.log(Level.WARNING, "The property type was null! Please define it!");
            return;
        }

        long countProperties = 0;
        int defaultAmount = 0;
        Status status = null;



        Locale selectedLanguage = null;
        if (language != null) {
            selectedLanguage = new Locale(language);
        }
        boolean found = true;
        switch (localizationType.toUpperCase()) {
        case "SRC":
            defaultAmount = locFacade.getLocalizationForIntegrityCheck(locFacade.lastVersion(Status.SRC), Status.SRC, Locale.ENGLISH, true, null).size();
            countProperties = locFacade.countOfProperties(Status.SRC, selectedLanguage, emptyProperties);
            status = Status.SRC;
            break;
        case "XLS":
            defaultAmount = locFacade.getLocalizationForIntegrityCheck(locFacade.lastVersion(Status.XLS), Status.XLS, Locale.ENGLISH, true, null).size();
            countProperties = locFacade.countOfProperties(Status.XLS, selectedLanguage, emptyProperties);
            status = Status.XLS;
            break;
        default:
            found = false;
            logger.log(Level.WARNING, "The property type {0} is unknown!", localizationType);
        }
        if (found)
            logger.log(Level.INFO, "The amount of properties type {2} is {3} [language={0}, englishDefaultAmount={1}, status={2}].",
                    new Object[] { language, defaultAmount, status, countProperties });
    }
}
