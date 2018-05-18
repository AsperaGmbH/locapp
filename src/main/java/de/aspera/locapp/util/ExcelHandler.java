/**
 *
 */
package de.aspera.locapp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @author Bjoern.Buchholz
 *
 */
public class ExcelHandler {

    private static final String    STYLE_HEADER       = "header";
    private static final String    STYLE_CHANGES      = "changes";
    private static final Logger    LOGGER             = Logger.getLogger(ExcelHandler.class.getName());

    private HSSFWorkbook           workbook;
    private Map<String, CellStyle> styleMap;
    private Map<String, Sheet>     sheets;
    private List<String>           markedChanges;
    private int                    rowcounter;

    public ExcelHandler() {
        workbook = new HSSFWorkbook();
        styleMap = new HashMap<>();
        sheets = new HashMap<>();
        markedChanges = new ArrayList<>();
        rowcounter = 0;

        initStyles();
    }

    public void initStyles() {
        HSSFPalette palette = workbook.getCustomPalette();
        CellStyle header = workbook.createCellStyle();
        CellStyle changes = workbook.createCellStyle();
        palette.setColorAtIndex(HSSFColor.GREY_25_PERCENT.index, (byte) 217, (byte) 217, (byte) 217);
        header.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font fontHeader = workbook.createFont();
        fontHeader.setBold(true);
        fontHeader.setColor(HSSFColor.BLACK.index);
        header.setFont(fontHeader);
        header.setAlignment(HorizontalAlignment.CENTER);
        this.styleMap.put(STYLE_HEADER, header);
        changes.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
        changes.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.styleMap.put(STYLE_CHANGES, changes);
    }

    public void markChanges(String cellName) {
        markedChanges.add(cellName);
    }

    public void reformatSheets(int cols) {
        for (String sheet : sheets.keySet()) {
            for (int i = 0; i < cols; i++) {
                sheets.get(sheet).setColumnWidth(i, 9000);
            }
        }
    }

    public void writeHeader(String sheetName, List<String> headers) {
        Sheet sheet = this.sheets.get(sheetName);
        int headercounter = 0;
        Row headerRow = sheet.createRow(rowcounter++);
        Cell cell;

        for (String header : headers) {
            cell = headerRow.createCell(headercounter++);
            cell.setCellValue(header);
            cell.setCellStyle(this.styleMap.get(STYLE_HEADER));
        }
    }

    public void writeRow(String sheetName, List<String> rowValues, String key, List<String> headers, String fullPath) {
        Sheet sheet = this.sheets.get(sheetName);
        int cellcounter = 0;
        String cellName = "";
        String[] splitHeader;
        Row row;
        Cell cell;

        row = sheet.createRow(rowcounter++);
        for (String rowValue : rowValues) {
            // Evaluate with the header row which language we got to build
            // a full unique key with property key, language and fullPath
            // this is used to check if the cellStyle needs to be changed
            splitHeader = headers.get(cellcounter).split("[\\()]");
            if (splitHeader.length == 2) {
                String language = splitHeader[1];
                cellName = key + "#" + language + "#" + HelperUtil.buildFullPath(language, fullPath);
            } else {
                cellName = key;
            }
            cell = row.createCell(cellcounter++);
            cell.setCellValue(rowValue);
            if (markedChanges.contains(cellName)) {
                cell.setCellStyle(this.styleMap.get(STYLE_CHANGES));
            }
        }
    }

    public void export(String filePath, String fileName) {
        export(filePath + fileName);
    }

    public void export(String filePathName) {
        FileOutputStream outStream = null;

        try {
            outStream = FileUtils.openOutputStream(new File(filePathName));
            workbook.write(outStream);
            outStream.close();
            workbook.close();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public Map<String, Sheet> getSheets() {
        return sheets;
    }

    public void createSheets(List<String> sheetNames) {
        Map<String, Sheet> sheetList = new HashMap<>();
        for (String sheetName : sheetNames) {
            sheetList.put(sheetName, workbook.createSheet(sheetName));
        }
        this.sheets = sheetList;
    }
}
