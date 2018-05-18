package de.aspera.locapp.dao;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import de.aspera.locapp.dto.Config;
import de.aspera.locapp.dto.FileInfo;
import de.aspera.locapp.dto.Localization;

public class DatabaseFacadeTest extends BasicFacadeTest {

    @Test
    public void testLocalization() throws DatabaseException {
        Localization loc = new Localization();
        loc.setKey("a.key");
        loc.setValue("foobar");
        loc.setCreationDate(new Date());
        loc.setFileName("foo.properties");
        loc.setFullPath("d:\\foo.properties");
        loc.setVersion(1);
        new LocalizationFacade().create(loc);
    }

    @Test
    public void testFileInfo() throws DatabaseException {
        FileInfo loc = new FileInfo();
        loc.setFileName("foo.properties");
        loc.setFullPath("d:\\foo.properties");
        new FileInfoFacade().create(loc);
        new FileInfoFacade().removeAll();
    }

    @Test
    public void testConfig() throws DatabaseException {
        Config config = new Config();
        config.setKey("aKey");
        String[] valueArray = new String[] { "a", "b", "c" };
        config.setValue(valueArray);
        ConfigFacade configFacade = new ConfigFacade();
        configFacade.create(config);

        // get it back
        String[] returnValues = configFacade.getValue("aKey");
        Assert.assertTrue(returnValues.length == valueArray.length);
        Assert.assertArrayEquals(returnValues, valueArray);

    }

    @Override
    public Class<?> getLoggerClass() {
        return this.getClass();
    }
}
