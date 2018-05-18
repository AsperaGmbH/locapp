package de.aspera.locapp.cmd;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.aspera.locapp.dao.BasicFacadeTest;

public class ExportPropertiesCommandTest extends BasicFacadeTest {

    private static final String TEMP_DIR = FileUtils.getTempDirectoryPath();

    @Test
    public void importExcelFile() throws InstantiationException, IllegalAccessException, IOException {
        CMDCTX.addArgument("cl");
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        CMDCTX.addArgument("ei");
        String file = ExportPropertiesCommandTest.class.getClassLoader().getResource("slc_excel_export.xls").getFile();
        CMDCTX.addArgument(file);
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        CMDCTX.addArgument("ep");
        CMDCTX.addArgument(TEMP_DIR);
        CMDCTX.executeCommand(CMDCTX.nextArgument());

    }

    @Override
    public Class<?> getLoggerClass() {
        return this.getClass();
    }
}
