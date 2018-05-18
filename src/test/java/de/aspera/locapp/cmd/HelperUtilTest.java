package de.aspera.locapp.cmd;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import de.aspera.locapp.dao.BasicFacadeTest;
import de.aspera.locapp.util.HelperUtil;

public class HelperUtilTest extends BasicFacadeTest {

    @Test
    public void getLocaleFromFileTestDefault() {
        String fileName = "EmailConnectionTesterConnectionSuccessPanel.properties";
        Assert.assertEquals(Locale.ENGLISH.toString(), HelperUtil.getLocaleFromPropertyFile(fileName));
    }

    @Test
    public void getLocaleFromFileTestEN() {
        String fileName = "EmailConnectionTesterConnectionSuccessPanel_en.properties";
        Assert.assertEquals(Locale.ENGLISH.toString(), HelperUtil.getLocaleFromPropertyFile(fileName));
    }

    @Test
    public void getLocaleFromFileTestDE() {
        String fileName = "EmailConnectionTesterConnectionSuccessPanel_de.properties";
        Assert.assertEquals(Locale.GERMAN.toString(), HelperUtil.getLocaleFromPropertyFile(fileName));
    }

    @Test
    public void getLocaleFromFileTestFR() {
        String fileName = "EmailConnectionTesterConnectionSuccessPanel_fr.properties";
        Assert.assertEquals(Locale.FRENCH.toString(), HelperUtil.getLocaleFromPropertyFile(fileName));
    }

    @Test
    public void removeLocaleFromFileTest() {
        String fileName = "EmailConnectionTesterConnectionSuccessPanel_fr.properties";
        Assert.assertEquals("EmailConnectionTesterConnectionSuccessPanel.properties", HelperUtil.removeLanguageFromPath(fileName));
    }


    @Override
    public Class<?> getLoggerClass() {
        return getClass();
    }

}
