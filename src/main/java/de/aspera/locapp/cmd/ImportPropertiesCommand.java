package de.aspera.locapp.cmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.FileInfoFacade;
import de.aspera.locapp.dao.LocalizationFacade;
import de.aspera.locapp.dto.FileInfo;
import de.aspera.locapp.dto.Localization;
import de.aspera.locapp.dto.Localization.Status;
import de.aspera.locapp.util.HelperUtil;

/**
 * This class reads the all file informations about saved properties files
 * {@link FileInfo}. The command load all entries of a property file and save it
 * into the database with a self increment version info.
 *
 * @author Daniel.Weiss
 *
 */
public class ImportPropertiesCommand implements CommandRunnable {

	private static final Logger logger = Logger.getLogger(ImportPropertiesCommand.class.getName());

	@Override
	public void run() throws CommandException {
		importPropertiesFiles();
	}

	private void importPropertiesFiles() throws CommandException {
		long start = System.currentTimeMillis();
		InputStream inStream = null;

		try {
			FileInfoFacade fileFacade = new FileInfoFacade();
			List<FileInfo> files = fileFacade.findAll();

			if (files == null || files.size() == 0) {
				logger.info("Nothing to do here! Please use 'files [DIR]' to find the properties files.");
				return;
			}

			LocalizationFacade locFacade = new LocalizationFacade();
			// increase the version of a loc data row
			int lastVersion = locFacade.lastVersion(Status.SRC) + 1;
			for (FileInfo myfile : files) {

				List<Localization> locs = new ArrayList<>();
				String locale = HelperUtil.getLocaleFromPropertyFile(myfile.getFileName());
				inStream = FileUtils.openInputStream(new File(myfile.getFullPath()));
				Properties prop = new Properties();
				prop.load(inStream);
				Enumeration<?> enums = prop.propertyNames();

				while (enums.hasMoreElements()) {
					Localization loc = new Localization();
					loc.setCreationDate(new Date());
					loc.setFileName(myfile.getFileName());
					loc.setFullPath(myfile.getRelativePath());
					String key = (String) enums.nextElement();
					String value = prop.getProperty(key);
					loc.setKey(key);
					loc.setValue(value);
					loc.setVersion(lastVersion);
					loc.setLocale(locale);
					loc.setStatus(Localization.Status.SRC);
					locs.add(loc);
				}

				inStream.close();
				locFacade.saveLocalizations(locs);
			}
		} catch (IOException | DatabaseException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {

			try {
				if (inStream != null)
					inStream.close();
			} catch (IOException e) {
				throw new CommandException(e.getMessage(), e);
			}
		}

		long end = System.currentTimeMillis() - start;
		logger.log(Level.INFO, "Iterate properties fileset and save into database in ms: " + end);
	}
}
