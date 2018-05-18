package de.aspera.locapp.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The class reads all full filepaths on a directory. You will get a list of all
 * founded filename for further processing.
 *
 * @author Daniel.Weiss
 *
 */
public class PropertyFileReader {
    private List<File> resultFileNames = new ArrayList<>();

    /**
     * The walking tree action starts immediately on create a instance of this
     * class. You can get the result by the getter method getResultFileNames().
     *
     * @param path
     * @param searchSuffix
     * @param excludedElements
     */
    public PropertyFileReader(String path, String searchSuffix, String... excludedElements) {
        walkDownTree(path, searchSuffix, excludedElements);
    }

    /**
     * private - don't use me.
     */
    @SuppressWarnings("unused")
    private PropertyFileReader() {
    }

    /**
     * The method walks a directory structure down and collect all filePath into
     * a Collection.
     *
     * @param path
     * @param searchSuffix
     * @param excludedElements
     */
    private void walkDownTree(String path, String searchSuffix, String... excludedElements) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null)
            return;
        for (File fileObj : list) {
            if (fileObj.isDirectory()) {
                walkDownTree(fileObj.getAbsolutePath(), searchSuffix, excludedElements);
            } else {
                String fileName = fileObj.getAbsoluteFile().toString();
                if (isValid(fileName, excludedElements) && fileName.endsWith(searchSuffix)) {
                    resultFileNames.add(fileObj);
                }
            }
        }
    }

    /**
     * Checks if excluded identifier on the fileName exist. If true, so the
     * fileName will be invalid to use it.
     *
     * @param fileName
     * @param excludedElements
     * @return
     */
    private boolean isValid(String fileName, String... excludedElements) {
        boolean valid = true;
        for (String excluded : excludedElements) {
            if (fileName.contains(excluded) && !fileName.contains("test-classes")) {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * return the list of founded files on the requested directory.
     *
     * @return
     */
    public List<File> getResultFileNames() {
        return resultFileNames;
    }
}
