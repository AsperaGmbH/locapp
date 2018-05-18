package de.aspera.locapp.cmd;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import de.aspera.locapp.dao.BasicFacadeTest;

public class ExcelExportCommandTest extends BasicFacadeTest {

    @Before
    public void init() throws InstantiationException, IllegalAccessException {

        String testfiles = ExcelExportCommandTest.class.getClassLoader().getResource("testfiles").getFile();

        CMDCTX.addArgument("init");
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        CMDCTX.addArgument("files");
        CMDCTX.addArgument(testfiles);
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        CMDCTX.addArgument("cl");
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        CMDCTX.addArgument("ip");
        CMDCTX.executeCommand(CMDCTX.nextArgument());
    }

    private static final String TEMP_DIR = FileUtils.getTempDirectoryPath();

    @Test
    public void exportExcelFile() throws InstantiationException, IllegalAccessException, IOException {
        CMDCTX.addArgument("ee");
        CMDCTX.addArgument(TEMP_DIR);
        CMDCTX.addArgument("fr");
        CMDCTX.addArgument("1");
        CMDCTX.executeCommand(CMDCTX.nextArgument());
        logger.info("saved in: " + TEMP_DIR);
    }

    @Override
    public Class<?> getLoggerClass() {
        return this.getClass();
    }
}
