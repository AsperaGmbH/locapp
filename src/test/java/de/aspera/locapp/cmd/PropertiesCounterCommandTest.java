package de.aspera.locapp.cmd;

import java.io.IOException;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.aspera.locapp.dao.BasicFacadeTest;
import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.Localization.Status;

public class PropertiesCounterCommandTest extends BasicFacadeTest {

    @Before
    public void init() throws InstantiationException, IllegalAccessException {

        String testfiles = PropertiesCounterCommandTest.class.getClassLoader().getResource("testfiles").getFile();

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

    @Test
    public void countSRCPropertiesFiles()
            throws InstantiationException, IllegalAccessException, IOException, DatabaseException {
        CMDCTX.addArgument("pc");
        CMDCTX.addArgument("src");
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        CMDCTX.addArgument("pc");
        CMDCTX.addArgument("src");
        CMDCTX.addArgument("fr");
        CMDCTX.addArgument("1");
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        CMDCTX.addArgument("pc");
        CMDCTX.addArgument("src");
        CMDCTX.addArgument("fr");
        CMDCTX.executeCommand(CMDCTX.nextArgument());

        long countSRC = new LocalizationFacade().countOfProperties(Status.SRC, null, false);
        Assert.assertNotNull(countSRC);
        Assert.assertEquals(10, countSRC);

        countSRC = new LocalizationFacade().countOfProperties(Status.SRC, Locale.GERMAN, false);
        Assert.assertNotNull(countSRC);
        Assert.assertEquals(2, countSRC);

        countSRC = new LocalizationFacade().countOfProperties(Status.SRC, Locale.ENGLISH, false);
        Assert.assertNotNull(countSRC);
        Assert.assertEquals(4, countSRC);

        countSRC = new LocalizationFacade().countOfProperties(Status.SRC, Locale.FRENCH, false);
        Assert.assertNotNull(countSRC);
        Assert.assertEquals(3, countSRC);

        countSRC = new LocalizationFacade().countOfProperties(Status.SRC, Locale.FRENCH, true);
        Assert.assertNotNull(countSRC);
        Assert.assertEquals(2, countSRC);

        countSRC = new LocalizationFacade().countOfProperties(Status.SRC, Locale.ITALIAN, false);
        Assert.assertNotNull(countSRC);
        Assert.assertEquals(1, countSRC);

    }

    @Override
    public Class<?> getLoggerClass() {
        return this.getClass();
    }
}
