package de.aspera.locapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class HelperUtil {

    public static final String      UNDERSCORE                        = "_";

    public static final String      FILE_TIMESTAMP_FORMAT             = "yyyyMMdd-HHmmss";

    private static SimpleDateFormat FILE_TIMESTAMP_SIMPLE_DATE_FORMAT = new SimpleDateFormat(
            FILE_TIMESTAMP_FORMAT);

    /**
     * Get the Locale ident of a property file.
     *
     * @param fileName
     * @return
     */
    public static String getLocaleFromPropertyFile(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("The fileName is required to proceed.");
        }
        String nameWithoutExtension = StringUtils.substringBefore(fileName, ".properties");
        String localeIdent = StringUtils.substring(nameWithoutExtension, nameWithoutExtension.length() - 3,
                nameWithoutExtension.length());
        if (localeIdent.contains(UNDERSCORE)) {
            String locale = StringUtils.substring(localeIdent, localeIdent.length() - 2, localeIdent.length());
            return new Locale(locale).toString();
        }
        return Locale.ENGLISH.toString();
    }

    /**
     * Replace the language description from properties file urls
     *
     * @param filePath
     * @param locale
     * @return
     */
    public static String replaceLanguageFromPath(String filePath, String locale) {
        return StringUtils.replace(filePath, ".properties", "_" + locale + ".properties");
    }

    /**
     * Remove the language description from properties file urls
     *
     * @param filePath
     * @param locale
     * @return
     */
    public static String removeLanguageFromPath(String filePath) {
        return StringUtils.replacePattern(filePath, "_.*{2}\\.properties", ".properties");
    }

    public static String buildFullPath(String language, String fullPath) {
        int index = fullPath.indexOf(".properties") - 3;
        if (fullPath.charAt(index) == '_') {
            if (language.equals(Locale.ENGLISH.toString())) {
                return StringUtils.replace(fullPath,
                        StringUtils.substring(fullPath, fullPath.indexOf(".properties") - 3), ".properties");
            } else {
                return StringUtils.replace(fullPath,
                        StringUtils.substring(fullPath, fullPath.indexOf(".properties") - 3),
                        "_" + language + ".properties");
            }
        } else if (!language.equals(Locale.ENGLISH.toString())) {
            return StringUtils.replace(fullPath, ".properties", "_" + language + ".properties");
        } else {
            return fullPath;
        }
    }

    /**
     * @return Timestamp - yyyyMMdd-HHmmss (e.g. 20180101-133742)
     */
    public static final String currentTimestamp() {
        return FILE_TIMESTAMP_SIMPLE_DATE_FORMAT.format(new Date());
    }

}
