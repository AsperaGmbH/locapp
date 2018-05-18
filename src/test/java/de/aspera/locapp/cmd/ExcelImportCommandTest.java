package de.aspera.locapp.cmd;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.aspera.locapp.dao.BasicFacadeTest;
import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.Localization.Status;

public class ExcelImportCommandTest extends BasicFacadeTest {
    @Test
    public void importExcelFile()
            throws InstantiationException, IllegalAccessException, IOException, DatabaseException {
        CMDCTX.addArgument("cl");
        CMDCTX.executeCommand(CMDCTX.nextArgument());
        CMDCTX.addArgument("ei");
        String file = ExcelImportCommandTest.class.getClassLoader().getResource("slc_excel_export.xls").getFile();
        CMDCTX.addArgument(file);
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        // test the count of imported properties from a excel file.
        CMDCTX.addArgument("pc");
        CMDCTX.addArgument("xls");
        CMDCTX.executeCommand(CMDCTX.nextArgument());
        long countSRC = new LocalizationFacade().countOfProperties(Status.XLS, null, false);
        Assert.assertNotNull(countSRC);
        Assert.assertEquals(countSRC, 8);

    }

    @Override
    public Class<?> getLoggerClass() {
        return this.getClass();
    }

}
