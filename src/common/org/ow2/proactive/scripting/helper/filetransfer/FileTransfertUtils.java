package org.ow2.proactive.scripting.helper.filetransfer;

public class FileTransfertUtils {

    /** Extract filename from pathfile (ex: C:\myFld\myFile.txt => myFile.txt)
     * @param pathFile the path of the file
     */
    public static String getFilenameFromPathfile(String pathFile) {
        //--Check parameters
        if (pathFile == null)
            return "";

        String filename = pathFile;
        int index = pathFile.lastIndexOf("/");
        // --Unix
        if (index != -1) {
            filename = pathFile.substring(pathFile.lastIndexOf("/") + 1, pathFile.length());
        }
        // --Windows
        else {
            filename = pathFile.substring(pathFile.lastIndexOf("\\") + 1, pathFile.length());
        }
        return filename;
    }

    /** Extract path from pathfile (ex: C:\myFld\myFile.txt => C:\myFld)
     * @param pathFile the path of the file
     */
    public static String getFolderFromPathfile(String pathFile) {
        //--Check parameters
        if (pathFile == null)
            return "";

        String path = pathFile;
        int index = pathFile.lastIndexOf("/");
        // --Unix
        if (index != -1) {
            path = pathFile.substring(0, pathFile.lastIndexOf("/"));
        }
        // --Windows
        else {
            path = pathFile.substring(0, pathFile.lastIndexOf("\\"));
        }

        return path;
    }
}
