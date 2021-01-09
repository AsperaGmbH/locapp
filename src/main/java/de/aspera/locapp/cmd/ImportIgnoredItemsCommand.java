package de.aspera.locapp.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.IgnoredItemFacade;
import de.aspera.locapp.dto.IgnoredItem;

public class ImportIgnoredItemsCommand implements CommandRunnable {
	private static final Logger logger = Logger.getLogger(ImportIgnoredItemsCommand.class.getName());

	private IgnoredItemFacade ignoredItemFacade = new IgnoredItemFacade();

	@Override
	public void run() {
		var ctx = CommandContext.getInstance();
		var ignoredItemsListPath = ctx.nextArgument();
		var ignoredItems = readIgnoredItemsFile(ignoredItemsListPath);
		try {
			for (var ignoredItem : ignoredItems) {
				ignoredItemFacade.create(ignoredItem);
				System.out.println(" >> Add ignore entry: " + ignoredItem.getFileName() + " to be ignored on export. <<");
			}
			if (ignoredItems != null && ignoredItems.size() >= 0)
				logger.log(Level.INFO, "Marked " + ignoredItems.size() + " files to be ignored.");
		} catch (DatabaseException e) {
			logger.log(Level.SEVERE, "Error while saving IgnoredItem entity.", e);
			return;
		}
	}

	private List<IgnoredItem> readIgnoredItemsFile(String filePath) {
		List<IgnoredItem> ignoredItems = new ArrayList<>();
		final Set<String> linesOfFile;
		try {
			linesOfFile = new HashSet<>(FileUtils.readLines(new File(filePath)));
		} catch (Exception e) {
			logger.log(Level.WARNING, "File " + filePath + " could not found or was invalid!.");
			return ignoredItems;
		}
		
		Set<String> savedIgnoreFiles = getSavedIgnoreFiles();
		for (Iterator<String> iterator = linesOfFile.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if (savedIgnoreFiles.contains(string))
				continue;
			var item = new IgnoredItem();
			item.setFileName(string);
			ignoredItems.add(item);
		}
		return ignoredItems;
	}

	private Set<String> getSavedIgnoreFiles() {
		List<IgnoredItem> ignored = ignoredItemFacade.findAll();
		Set<String> knownIgnoreFiles = new HashSet<>();
		for (IgnoredItem ignoreItem : ignored) {
			knownIgnoreFiles.add(ignoreItem.getFileName());
		}
		return knownIgnoreFiles;
	}
}
