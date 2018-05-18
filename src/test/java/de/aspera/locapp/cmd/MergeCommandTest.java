package de.aspera.locapp.cmd;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import de.aspera.locapp.dao.BasicFacadeTest;
import de.aspera.locapp.dao.DatabaseException;

public class MergeCommandTest extends BasicFacadeTest {

    @Before
    public void init() throws InstantiationException, IllegalAccessException {

        CMDCTX.addArgument("init");
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        CMDCTX.addArgument("cl");
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        String[] files = new String[]{"export_all_edit.xls", "export_de_edit.xls", "export_it_edit.xls"};
        for (String file : files) {
            CMDCTX.addArgument("ei");
            String filePath = ExcelImportCommandTest.class.getClassLoader().getResource(file).getFile();
            CMDCTX.addArgument(filePath);
            CMDCTX.executeCommand(CMDCTX.nextArgument());
        }
    }

    @Test
    public void countSRCPropertiesFiles()
            throws InstantiationException, IllegalAccessException, IOException, DatabaseException {
        CMDCTX.addArgument("mp");
        CMDCTX.addArgument("xls");
        CMDCTX.executeCommand(CMDCTX.nextArgument());


    }

    @Override
    public Class<?> getLoggerClass() {
        return this.getClass();
    }
}
