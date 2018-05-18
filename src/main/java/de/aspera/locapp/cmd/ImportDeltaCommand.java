/**
 *
 */
package de.aspera.locapp.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.Localization;
import de.aspera.locapp.dto.Localization.Status;

/**
 * @author Bjoern.Buchholz
 *
 */
public class ImportDeltaCommand implements CommandRunnable {
    private static final int COL_KEY = 2;
    private static final Logger LOGGER = Logger.getLogger(ExportDeltaCommand.class.getName());

    private LocalizationFacade locaFacade = new LocalizationFacade();
    private Map<String, Integer> languagePositonMap = new HashMap<>();

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            doImport();
            long end = System.currentTimeMillis() - start;
            LOGGER.log(Level.INFO, "Import Excel Delta file in ms: " + end);
        } catch (DatabaseException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void doImport() throws DatabaseException, IOException {
        String importPath = CommandContext.getInstance().nextArgument();
        String lastVersionStr = CommandContext.getInstance().nextArgument();
        int lastVersion = 0;

        if (!invalidPath(importPath)) {
            lastVersion = determineLastVersion(lastVersionStr);
            if (!invalidVersion(lastVersion)) {
                List<Localization> importLocs = readImportFile(importPath, lastVersion);
                if (solvedConflicts(importLocs)) {
                    List<Localization> mergedLocs = mergeLocalizations(importLocs, lastVersion);
                    locaFacade.saveLocalizations(mergedLocs);
                }
            }
        }
    }

    private boolean invalidPath(String importPath) {
        if (StringUtils.isEmpty(importPath) || !StringUtils.endsWith(importPath, ".xls")) {
            LOGGER.severe("No excel file found to import! Please define the full path to excel import file.");
            return true;
        }

        return false;
    }

    private boolean invalidVersion(int lastVersion) {
        if (lastVersion == -1) {
            LOGGER.severe("Invalid version! Please enter a numeric value only");
            return true;
        }

        return false;
    }

    private List<Localization> readImportFile(String importPath, int lastVersion)
            throws IOException, DatabaseException {
        List<Localization> importLocs = new ArrayList<>();
        FileInputStream excelFile = new FileInputStream(new File(importPath));
        Workbook workbook = new HSSFWorkbook(excelFile);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = datatypeSheet.iterator();

        while (iterator.hasNext()) {
            Row row = iterator.next();

            if (row != null) {
                if (StringUtils.isEmpty(getStringValue(row.getCell(0)))) {
                    continue;
                } else if (row.getCell(0).getStringCellValue().toUpperCase().contains("FILENAME")) {
                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        buildLanguagePosMap(row.getCell(i).getStringCellValue(), i);
                    }
                    continue;
                }
                for (String language : languagePositonMap.keySet()) {
                    Localization loc = new Localization();
                    loc.setCreationDate(new Date());
                    String fileName = row.getCell(0).getStringCellValue();
                    loc.setFileName(language.equals(Locale.ENGLISH.toString()) ? fileName
                            : replaceFullPath(fileName, language));
                    loc.setKey(row.getCell(COL_KEY).getStringCellValue());
                    Cell cellByLanguage = row.getCell(languagePositonMap.get(language));
                    String cellValueByLanguage = getStringValue(cellByLanguage);
                    loc.setValue(cellValueByLanguage != null ? cellValueByLanguage : EMPTY_VALUE);
                    loc.setLocale(language);
                    String fullPath = row.getCell(row.getLastCellNum() - 1).getStringCellValue();
                    loc.setFullPath(language.equals(Locale.ENGLISH.toString()) ? fullPath
                            : replaceFullPath(fullPath, language));
                    loc.setVersion(lastVersion + 1);
                    loc.setStatus(Localization.Status.XLS);
                    if (!loc.getValue().equals(EMPTY_VALUE)) {
                        importLocs.add(loc);
                    }
                }
            }
        }
        workbook.close();

        return importLocs;
    }

    /**
     * Uses a HashSet to check for duplicate rows in Excel sheet. The add()
     * function of a HashSet returns false if it already contains the Object.
     * Therefore the equals() method of the respective Object is used.
     *
     * @param importLocs
     * @return
     */
    private boolean solvedConflicts(List<Localization> importLocs) {
        Set<Localization> readElements = new HashSet<>();
        boolean result = false;
        for (Localization loc : importLocs) {
            if (readElements.add(loc)) {
                result = true;
            } else {
                LOGGER.log(Level.WARNING, "Existing conflicts detected! Only one row per key is allowed.");
                return false;
            }
        }
        return result;
    }

    /**
     * This method uses a HashSet to avoid duplicate entries. The add() method
     * only adds a new entry if the HashSet doesn't contain it. First the most
     * recent entries of the excel sheet will be add and then the other entries
     * from the database will be depending on the given version.
     *
     * @param importLocs
     * @param lastVersion
     * @return
     * @throws DatabaseException
     */
    private List<Localization> mergeLocalizations(List<Localization> importLocs, int lastVersion)
            throws DatabaseException {
        List<Localization> databaseLocs = locaFacade.getLocalizationsWithLastVersion(lastVersion);
        Set<Localization> updateLocs = new HashSet<>();
        List<Localization> mergedLocs = new ArrayList<>();

        updateLocs.addAll(importLocs);
        for (Localization loc : databaseLocs) {
            locaFacade.detach(loc);
            loc.setId(null);
            loc.setCreationDate(new Date());
            loc.setStatus(Status.XLS);
            loc.setVersion(lastVersion + 1);
            updateLocs.add(loc);
        }

        mergedLocs.addAll(updateLocs);
        return mergedLocs;
    }

    private int determineLastVersion(String lastVersionStr) throws DatabaseException {
        if (StringUtils.isEmpty(lastVersionStr)) {
            return locaFacade.lastVersion(null);
        } else {
            return StringUtils.isNumeric(lastVersionStr) ? Integer.parseInt(lastVersionStr) : -1;
        }
    }

    private String replaceFullPath(String fullPath, String locale) {
        return StringUtils.replace(fullPath, ".properties", "_" + locale + ".properties");
    }

    private Map<String, Integer> buildLanguagePosMap(String cellValue, int cellPos) {
        String language = Locale.ENGLISH.toString();
        if (cellValue.trim().toLowerCase().contains("value")) {
            language = StringUtils.substringBetween(cellValue.trim(), "(", ")").toLowerCase();
            if (cellValue.trim().toLowerCase().contains(language)) {
                languagePositonMap.put(language, cellPos);
            }
        }
        return languagePositonMap;
    }

    /**
     * @param aDouble
     * @return
     */
    private String getStringFrom(double aDouble) {
        return Double.toString(aDouble);
    }

    private String getStringValue(Cell cell) {
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "true" : "false";
            case NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell) && cell.getDateCellValue() != null) {
                    return Long.toString(cell.getDateCellValue().getTime());
                }
                return getStringFrom(cell.getNumericCellValue());
            case STRING:
                return cell.getStringCellValue();
            case BLANK:
                return "";
            case ERROR:
                cell.getErrorCellValue();
                return "";
            // CELL_TYPE_FORMULA will never occur
            case FORMULA:
                return "";
            case _NONE:
                break;
            }
        }
        return null;
    }

}
