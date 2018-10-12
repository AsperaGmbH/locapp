package de.aspera.locapp.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.Localization;
import de.aspera.locapp.dto.Localization.Status;

public class CheckIntegrityCommand implements CommandRunnable {

    private static final Logger logger = Logger.getLogger(CheckIntegrityCommand.class.getName());
    private LocalizationFacade locFacade = new LocalizationFacade();

    @Override
    public void run() {
        try {
            checkIntegrityProperties(CommandContext.getInstance().allArguments());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void checkIntegrityProperties(String... options) throws DatabaseException {

        List<Localization> srcLocalizations = locFacade.getLocalizationForIntegrityCheck(locFacade.lastVersion(Status.SRC), Status.SRC,
                Locale.ENGLISH, true, null);

        List<String> languages = new ArrayList<>();
        if (options != null && options.length > 0) {
            languages.add(options[0].toLowerCase());
        } else {
            languages = locFacade.getLanguages(false);
        }

        boolean successfull = true;
        for (String language : languages) {
            Locale locale = new Locale(language);
            logger.info("Check the integrity for language " + locale.getLanguage() + " and with src vs xls ...");

            List<Localization> xlsLocalizations = locFacade.getLocalizationForIntegrityCheck(locFacade.lastVersion(Status.XLS),
                    Status.XLS, locale, false, null);

            if (srcLocalizations.size() == xlsLocalizations.size()) {
                logger.log(Level.INFO,
                        "SUCCESS: LANGUAGE[{2}] The amount of src({0}) vs xls({1}) is equal. All src properties are provided by xls",
                        new Object[] { srcLocalizations.size(), xlsLocalizations.size(), locale.getLanguage() });
            } else {
                logger.log(Level.INFO,
                        "FAIL: LANGUAGE[{2}] The amount of src({0}) vs xls({1}) is not equal. All src properties are NOT provided by xls!",
                        new Object[] { srcLocalizations.size(), xlsLocalizations.size(), locale.getLanguage() });
                successfull = false;
            }
        }
        if (successfull) {
            System.out.println("\n*** SUCCESS! The localization properties are complete! ***");
        } else {
            System.out.println("\n*** ERROR! The localization properties are incomplete! ***");
        }
    }
}
