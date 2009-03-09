package org.ow2.proactive.scripting.helper.filetransfer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class ScriptLoggerHelper {

    static String logsDirName = ".proactive_logs";

    /**
     * If filepath is absolute, we append in that file
     * If it is relative, we append in tmpDir+file.separator+filePath
     * @param filePath
     * @throws IOException 
     */
    public static void logToFile(String filePath, String message) throws IOException {
        String logsAbsoluteFilePath;
        File testF = new File(filePath);
        if (testF.isAbsolute())
            logsAbsoluteFilePath = filePath;
        else {
            String tmpDirPath = System.getProperty("java.io.tmpdir");
            if (!tmpDirPath.endsWith(File.separator))
                tmpDirPath += File.separator;

            String logsDirPath = tmpDirPath + logsDirName;
            File logsDir = new File(logsDirPath);
            if (!(logsDir.isDirectory()))
                logsDir.mkdir();
            logsAbsoluteFilePath = logsDirPath + File.separator + filePath;
        }

        File logsFile = new File(logsAbsoluteFilePath);
        if (!logsFile.exists()) {
            logsFile.createNewFile();
        }

        //System.out.println("logging to file "+logsFile+" msg: "+message);

        BufferedWriter bw = new BufferedWriter(new FileWriter(logsFile, true));
        bw.append(message + "\n");
        bw.close();
        //System.out.println("Log ok. ");

    }

}
