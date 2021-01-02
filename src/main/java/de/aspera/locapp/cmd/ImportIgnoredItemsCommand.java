package de.aspera.locapp.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

            }
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, "Error while saving IgnoredItem entity.", e);
        }
    }

    private List<IgnoredItem> readIgnoredItemsFile(String filePath) {
        List<IgnoredItem> ignoredItems = new ArrayList<>();
        File file;
        FileInputStream inputStream;

        try {
            file = new File(filePath);
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File " + filePath + " not found.", e);
            return ignoredItems;
        }
         
        var streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        var fileAsText = new BufferedReader(streamReader)
            .lines()
            .collect(Collectors.joining("\n"));

        try {
            streamReader.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while closing stream reader.", e);
            return ignoredItems;
        }
        
        StringTokenizer tokenizer = new StringTokenizer(fileAsText);
        
        while (tokenizer.hasMoreTokens()) {
            var token = tokenizer.nextToken();
            var item = new IgnoredItem();
            item.setFileName(token);
        }

        return ignoredItems;
    }
}
