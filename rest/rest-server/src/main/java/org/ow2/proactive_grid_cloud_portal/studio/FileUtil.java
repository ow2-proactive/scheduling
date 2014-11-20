package org.ow2.proactive_grid_cloud_portal.studio;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;

public class FileUtil {

    private final static Logger logger = Logger.getLogger(FileUtil.class);

    static void writeFileContent(String fileName, String content) {
        try {
            logger.info("Writing file " + fileName);
            FileUtils.write(new File(fileName), content);
        } catch (IOException e) {
            logger.warn("Could not write file " + fileName, e);
        }
    }

    static String getFileContent(String fileName) {
        try {
            logger.info("Reading file " + fileName);
            return FileUtils.readFileToString(new File(fileName));
        } catch (Exception e) {
            logger.warn("Could not read file " + fileName, e);
        }
        return "";
    }

}
