package de.aspera.locapp.cmd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.Localization;
import de.aspera.locapp.dto.Localization.Status;

/**
 * All known properties will be merged with their latest version to a new data set. The submitted status iterate all
 * fetched properties with versions and compare the key-value combination with a newer version and can replace it.
 *
 * @author adidweis
 *
 */
public class MergeCommand implements CommandRunnable {

    private static final Logger logger = Logger.getLogger(MergeCommand.class.getName());
    private LocalizationFacade locFacade = new LocalizationFacade();
    private Map<String, Localization> propertiesMap = new HashMap<>();

    @Override
    public void run() {
        try {
            mergeProperties(CommandContext.getInstance().allArguments());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void mergeProperties(String... options) throws DatabaseException {

        if (options == null || options.length == 0) {
            logger.log(Level.WARNING, "No command parameters was found!");
            return;
        }

        String localizationType = options[0]; // SRC / XLS
        if (StringUtils.isEmpty(localizationType)) {
            logger.log(Level.WARNING, "The property type was null! Please define it!");
            return;
        }

        List<Localization> localizations = locFacade.findAll();
        Localization transientLocalization = null;

        for (Localization loc : localizations) {
            if (!loc.getStatus().name().equalsIgnoreCase(localizationType)) {
                continue; // skip unknown status types
            }

            locFacade.detach(loc);
            loc.setId(null);

            String keyWithoutVersion = StringUtils.substringBefore(createUniqueKey(loc), "@VERSION");
            String versionOfKey = StringUtils.substringAfter(createUniqueKey(loc), "@VERSION=");

            transientLocalization = propertiesMap.get(keyWithoutVersion);
            if (transientLocalization != null && NumberUtils.toInt(versionOfKey) > transientLocalization.getVersion()) {
                propertiesMap.put(keyWithoutVersion, loc);
            }
            if (!propertiesMap.containsKey(keyWithoutVersion)) {
                propertiesMap.put(keyWithoutVersion, loc);
            }
        }

        int lastVersion = 0;
        Status status = null;
        if (Status.SRC.name().equalsIgnoreCase(localizationType)) {
            lastVersion = locFacade.lastVersion(Status.SRC);
            status = Status.SRC;
        } else if (Status.XLS.name().equalsIgnoreCase(localizationType)) {
            lastVersion = locFacade.lastVersion(Status.XLS);
            status = Status.XLS;
        }

        for (Localization loc : propertiesMap.values()) {
            loc.setVersion(lastVersion + 1);
        }

        List<Localization> mergeLocs = Arrays.asList(propertiesMap.values().toArray(new Localization[] {}));
        locFacade.saveLocalizations(mergeLocs);
        logger.log(Level.INFO, "The properties was saved into a new dataset with status {0}", new Object[]{status});
    }

    /**
     * return a unique key of a localization object.
     *
     * @param loc
     * @return
     */
    private String createUniqueKey(Localization loc) {
        StringBuilder builder = new StringBuilder();
        builder.append(loc.getFullPath()).append("@");
        builder.append(loc.getKey()).append("@");
        builder.append(loc.getStatus().name()).append("@");
        builder.append(loc.getLocale()).append("@VERSION=");
        builder.append(loc.getVersion());
        return builder.toString();
    }
}
