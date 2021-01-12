package de.aspera.locapp.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import de.aspera.locapp.dao.ConfigFacade;
import de.aspera.locapp.dao.DatabaseException;
import de.aspera.locapp.dao.FileInfoFacade;
import de.aspera.locapp.dto.FileInfo;

public class FilesCommand implements CommandRunnable {

    private static final Logger logger = Logger.getLogger(FilesCommand.class.getName());

    @Override
    public void run() {
        listFiles(CommandContext.getInstance().nextArgument());
    }

    private void listFiles(String path) {
        if (StringUtils.isEmpty(path) || !new File(path).exists()) {
            logger.warning("No path found for command: (f)iles");
            return;
        }

        long start = System.currentTimeMillis();
        ConfigFacade configFacade = new ConfigFacade();
        
        String[] excludedPaths = new String[] { "" };
        try {
            excludedPaths = configFacade.getValue("Excluded_Paths");
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        PropertyFileReader propertyFileReader = new PropertyFileReader(path + (SystemUtils.IS_OS_WINDOWS ? "\\" : "/"),
                ".properties", excludedPaths);

        FileInfoFacade fileFacade = new FileInfoFacade();
        List<FileInfo> files = new ArrayList<>();
        try {
            fileFacade.removeAll();
            for (File file : propertyFileReader.getResultFileNames()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFileName(file.getName());
                fileInfo.setFullPath(file.getAbsolutePath());
                fileInfo.setRelativePath(
                        file.getAbsolutePath().replace(path, SystemUtils.IS_OS_WINDOWS ? ".\\" : "./"));
                fileInfo.setSearchPath(path);

                files.add(fileInfo);
            }
            fileFacade.saveFileInfos(files);
        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        long end = System.currentTimeMillis() - start;
        logger.log(Level.INFO, "List files and save into database in ms: {0}", end);
    }
}
