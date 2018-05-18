package de.aspera.locapp.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

public class ExcelImportCommand implements CommandRunnable {
    private static final int COL_KEY = 1;
    private static final Logger logger = Logger.getLogger(ExcelExportCommand.class.getName());
    private LocalizationFacade locFacade = new LocalizationFacade();
    private Map<String, Integer> languagePositonMap = new HashMap<>();

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            importExcel();
            long end = System.currentTimeMillis() - start;
            logger.log(Level.INFO, "Import Excel file in ms: " + end);
        } catch (DatabaseException | IOException exp) {
            logger.log(Level.SEVERE, exp.getMessage(), exp);
        }
    }

    private void importExcel() throws DatabaseException, IOException {
        String importPath = CommandContext.getInstance().nextArgument();
        if (StringUtils.isEmpty(importPath) || !importPath.endsWith(".xls")) {
            logger.severe("No excel file found to import! Please define the full path to excel import file.");
            return;
        }

        FileInputStream excelFile = new FileInputStream(new File(importPath));
        Workbook workbook = new HSSFWorkbook(excelFile);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = datatypeSheet.iterator();
        List<Localization> importLocs = new ArrayList<>();
        int lastVersion = locFacade.lastVersion(Status.XLS) + 1;

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
                    loc.setVersion(lastVersion);
                    loc.setStatus(Localization.Status.XLS);
                    if (!loc.getValue().equals(EMPTY_VALUE)) {
                        importLocs.add(loc);
                    }
                }
            }
        }
        locFacade.saveLocalizations(importLocs);
        workbook.close();
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
