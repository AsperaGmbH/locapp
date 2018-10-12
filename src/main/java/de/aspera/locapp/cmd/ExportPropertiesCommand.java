package de.aspera.locapp.cmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.Localization;
import de.aspera.locapp.dto.Localization.Status;
import de.aspera.locapp.util.HelperUtil;

/**
 * This class export all property files into a directory.
 *
 * @author Daniel.Weiss
 *
 */
public class ExportPropertiesCommand implements CommandRunnable {

    private static final Logger logger    = Logger.getLogger(ExportPropertiesCommand.class.getName());
    private LocalizationFacade  locFacade = new LocalizationFacade();
    private List<Localization>  allLocalizations;

    @Override
    public void run() {
        exportPropertiesFiles();
    }

    private void exportPropertiesFiles() {
        long start = System.currentTimeMillis();
        String exportPath = CommandContext.getInstance().nextArgument();

        if (StringUtils.isEmpty(exportPath)) {
            logger.warning("No export path found! Please define a export path.");
            return;
        }

        try {
            allLocalizations = locFacade.getLocalizations(locFacade.lastVersion(Status.XLS), Status.XLS, false, null);
            Set<String> defaultPathFiles = locFacade.getDefaultFiles(true);
            List<String> languages = locFacade.getLanguages();

            for (String defaultPathFile : defaultPathFiles) {
                for (String local : languages) {
                	if (skipPropertyFile(defaultPathFile, local)) {
                		continue; // skip unnecessary languages and files
                	}
                    String replacedFile = replaceFilePathWithLocale(defaultPathFile, local);

                    File exportPropertyFile = new File(exportPath + replacedFile);
                    Properties prop = new Properties() {
                        private static final long serialVersionUID = 7103264221960600113L;
                        // this sort the keys of a property file.
                        @Override
                        public synchronized Enumeration<Object> keys() {
                            return Collections.enumeration(new TreeSet<Object>(super.keySet()));
                        }
                    };

                    if (exportPropertyFile.exists()) {
                        InputStream input = FileUtils.openInputStream(exportPropertyFile);
                        if (input != null) {
                            prop.load(input);
                        }
                    }

                    String locFilename = null;
                    for (Localization loc : getLocalization(replacedFile)) {
                        logger.fine("save -> file: " + replacedFile + " >> key: " + loc.getKey() + " ; value: "
                                + loc.getValue());
                        if (prop.containsKey(loc.getKey())) {
                            if (!prop.get(loc.getKey()).equals(loc.getValue())) {
                                prop.setProperty(loc.getKey(), loc.getValue());
                            }
                        } else {
                            prop.put(loc.getKey(), loc.getValue());
                        }
                        locFilename = loc.getFileName();
                    }
                    OutputStream outStream = FileUtils.openOutputStream(exportPropertyFile);
                    if (prop.size() >= 1) {
                        prop.store(outStream, "SLC property file " + locFilename != null ? locFilename : "");
                    } else {
                        outStream.close();
                        FileUtils.forceDelete(exportPropertyFile);
                    }
                    outStream.close();
                }
            }
        } catch (IOException | DatabaseException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        long end = System.currentTimeMillis() - start;
        logger.log(Level.INFO, "Export properties fileset into a directory [" + exportPath + "] in ms: " + end);
    }

	private boolean skipPropertyFile(String defaultFilePath, String language) {
		for (Localization loc : allLocalizations) {
			if (!loc.getLocale().equals(language)
					&& HelperUtil.removeLanguageFromPath(loc.getFullPath()).equals(defaultFilePath) ) {
				return true; // skip unnecessary languages
			}
		}
		return false;
	}

    private String replaceFilePathWithLocale(String myfile, String local) {
        if (!local.contains(Locale.ENGLISH.toString())) {
            return HelperUtil.replaceLanguageFromPath(myfile, local);
        }
        return myfile;
    }

    private List<Localization> getLocalization(String filePath) {
        List<Localization> locs = new ArrayList<>();
        for (Localization loc : allLocalizations) {
            if (loc.getFullPath().equals(filePath)) {
                locs.add(loc);
            }
        }
        return locs;
    }
}
