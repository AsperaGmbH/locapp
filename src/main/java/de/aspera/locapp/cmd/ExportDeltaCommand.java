/**
 *
 */
package de.aspera.locapp.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.SystemUtils;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.Localization;
import de.aspera.locapp.dto.Localization.Status;
import de.aspera.locapp.util.ExcelHandler;
import de.aspera.locapp.util.HelperUtil;

public class ExportDeltaCommand implements CommandRunnable {

    private static final Logger LOGGER        = Logger.getLogger(ExportDeltaCommand.class.getName());

    private List<Localization>  locaDeltaList = new ArrayList<>();
    private LocalizationFacade  locaFacade    = new LocalizationFacade();
    private ExcelHandler        excelHandler  = new ExcelHandler();
    private List<String>        languages;

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            createDeltaRecords();
            runExport();
            long end = System.currentTimeMillis() - start;
            LOGGER.log(Level.INFO, "Export Excel Delta file in ms: {0}", end);
        } catch (DatabaseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void createDeltaRecords() throws DatabaseException {
        int lastVersionSlc = 0;
        int lastVersionExcel = 0;
        Map<String, Localization> locaSlcMap;
        Map<String, Localization> locaXlsMap;

        lastVersionSlc = locaFacade.lastVersion(Status.SRC);
        lastVersionExcel = locaFacade.lastVersion(Status.XLS);

        locaSlcMap = storeListInMap(locaFacade.getLocalizations(lastVersionSlc, Status.SRC, false, null));
        locaXlsMap = storeListInMap(locaFacade.getLocalizations(lastVersionExcel, Status.XLS, false, null));

        locaDeltaList = createDeltaList(locaSlcMap, locaXlsMap);
    }

    public void runExport() throws DatabaseException {
        List<String> sheetNames = new ArrayList<>();
        List<String> headers;
        String exportPath = CommandContext.getInstance().nextArgument();
        String fileName = HelperUtil.currentTimestamp() + "-delta.xls";

        exportPath += SystemUtils.IS_OS_WINDOWS ? "\\" : "/";

        sheetNames.add("SLC Properties Delta");

        headers = createHeaders(getLanguages());
        excelHandler.createSheets(sheetNames);
        excelHandler.writeHeader(sheetNames.get(0), headers);

        for (Localization delta : locaDeltaList) {
            // A new row for the Excel sheet will be written only for the
            // english
            // property key, because in the same row the values of the other
            // languages
            // will also be written
            if (delta.getLocale().equals(Locale.ENGLISH.toString())) {
                excelHandler.writeRow(sheetNames.get(0), createRow(delta, getLanguages()), delta.getKey(), headers,
                        delta.getFullPath());
            }
        }
        excelHandler.reformatSheets(headers.size());

        excelHandler.export(exportPath, fileName);
    }

    private List<String> getLanguages() throws DatabaseException {
        if (languages == null || languages.isEmpty()) {
            languages = locaFacade.getLanguages();
        }
        return languages;
    }

    private List<String> createHeaders(List<String> languages) {
        List<String> headers = new LinkedList<>();
        headers.add("Filename");
        headers.add("Status");
        headers.add("Key");
        for (String lang : languages) {
            headers.add("Value (" + lang + ")");
        }
        headers.add("FullPath");
        return headers;
    }

    private List<String> createRow(Localization localization, List<String> languages) {
        List<String> row = new LinkedList<>();

        row.add(localization.getFileName());
        row.add(localization.getStatus().toString());
        row.add(localization.getKey());

        // Add the value for every language of a property
        for (String lang : languages) {
            if (lang.equals(Locale.ENGLISH.toString())) {
                row.add(localization.getValue());
            } else {
                row.add(getLoc(locaDeltaList, HelperUtil.replaceLanguageFromPath(localization.getFullPath(), lang),
                        localization.getKey(), localization.getStatus().toString()).getValue());
            }
        }
        row.add(localization.getFullPath());
        return row;
    }

    private Localization getLoc(List<Localization> allLocalizations, String fullPath, String key, String status) {
        for (Localization loc : allLocalizations) {
            if (loc.getFullPath().equals(fullPath) && loc.getKey() != null && loc.getKey().equals(key)
                    && loc.getStatus().toString().equals(status)) {
                return loc;
            }
        }
        Localization loc = new Localization();
        loc.setValue(EMPTY_VALUE);
        return loc;
    }

    private List<Localization> createDeltaList(Map<String, Localization> firstMap, Map<String, Localization> secondMap)
            throws DatabaseException {
        List<Localization> deltaList = new ArrayList<>();

        // The compareMaps function is used two times to check if the secondMap
        // contains
        // every key of the firstMap and the other way round
        deltaList = compareMaps(firstMap, secondMap, deltaList);
        deltaList = compareMaps(secondMap, firstMap, deltaList);

        return deltaList;
    }

    private List<Localization> compareMaps(Map<String, Localization> firstMap, Map<String, Localization> secondMap,
            List<Localization> deltaList) throws DatabaseException {
        languages = getLanguages();
        List<String> keys = new ArrayList<>();
        for (String key : firstMap.keySet()) {
            if (secondMap.containsKey(key)) {
                // If both maps got the same key then the values must be
                // checked. In case they're
                // different and the keys of both maps are not already included
                // in the deltaList
                // (to avoid dublicate entries), the Localization behind the key
                // of both maps will be add to the deltaList. In addition to
                // that the key will be marked for
                // the output and it will be remembered for filling up deltaList
                // with the unchanged keys
                // for output purpose
                if (!firstMap.get(key).getValue().equals(secondMap.get(key).getValue())
                        && !deltaList.contains(firstMap.get(key)) && !deltaList.contains(secondMap.get(key))) {
                    deltaList.add(firstMap.get(key));
                    deltaList.add(secondMap.get(key));
                    excelHandler.markChanges(key);
                    keys.add(key);
                }
            } else {
                deltaList.add(firstMap.get(key));
                excelHandler.markChanges(key);
                keys.add(key);
            }
        }
        // Fill up the deltaList with the unchaged Localizations of a specific
        // key for output purpose
        deltaList = fillWithUnchangedLocale(firstMap, secondMap, deltaList, keys, languages);

        return deltaList;
    }

    private List<Localization> fillWithUnchangedLocale(Map<String, Localization> firstMap,
            Map<String, Localization> secondMap, List<Localization> deltaList, List<String> keys,
            List<String> languages) {
        String tempKey;

        for (String key : keys) {
            for (String lang : languages) {
                // key will be split to create a new one with different language
                // and fullPath
                String[] splittedKey = key.split("#");
                // create new fullPath with the respective language suffix
                String fullPath = HelperUtil.buildFullPath(lang, splittedKey[2]);
                tempKey = splittedKey[0] + "#" + lang + "#" + fullPath;
                // Check if the new created tempKey already exists in deltaList
                // to avoid duplicate entries.
                if (!deltaList.contains(firstMap.get(tempKey)) && !deltaList.contains(secondMap.get(tempKey))
                        && !key.equals(tempKey)) {
                    if (firstMap.containsKey(tempKey)) {
                        deltaList.add(firstMap.get(tempKey));
                    }
                    if (secondMap.containsKey(tempKey)) {
                        deltaList.add(secondMap.get(tempKey));
                    }
                }
            }
        }
        return deltaList;
    }

    private Map<String, Localization> storeListInMap(List<Localization> locaList) {
        Map<String, Localization> resultMap = new HashMap<>();
        String key;

        for (Localization loca : locaList) {
            key = loca.getKey() + "#" + loca.getLocale() + "#" + loca.getFullPath();
            resultMap.put(key, loca);
        }
        return resultMap;
    }
}
