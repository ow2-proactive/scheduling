package org.ow2.proactive.scripting.helper.filetransfer.driver;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;


public interface FileTransfertDriver {

    void init(FileTransfertInitializer myInit);

    void getFile(String remoteFilePath, String localFolder) throws Exception;

    void getFiles(List<String> files, String localFolder) throws Exception;;

    void putFile(String localFilePath, String remoteFolder) throws Exception;

    void putFiles(List<String> localFilePaths, String remoteFolder) throws Exception;;

    public void getFolder(String remoteFolderPath, String localFolderPath) throws Exception;

    public void putFolder(String localFolderPath, String remoteFolderPath) throws Exception;

    ArrayList<String> list(String remoteFolder) throws Exception;

}
