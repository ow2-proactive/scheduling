package util;


/**
 * Utilities class
 * The purpose of this class is to take all html files containing in the targeted directory
 * and to delete the line staring by ~~~
 * The resulting file is outputed as
 *     _<originalFilename.html>
 * Typically it is used to removed unwanted content from html file. In the original file we will
 * have
 * <!--
 * ~~~ -->
 * <section to remove by this tool>
 * <!-- -->
 */
public class FilterDocFiles {
    static boolean changeImagePath = true;
    String name;

    public static void main(String[] args) throws java.io.IOException {
        if (args.length == 0) {
            System.out.println("Missing target directory");
            System.exit(-1);
        }
        if (System.getProperty("changeImages") != null) {
            if (System.getProperty("changeImages").equals("false")) {
                changeImagePath = false;
            }
        }
        java.io.File sourceDir = new java.io.File(args[0]);
        filter(sourceDir);
    }

    private static void filterFile(java.io.File file)
        throws java.io.IOException {
        String name = file.getName();
        if (!name.endsWith(".html")) {
            return;
        }
        if (name.startsWith("_")) {
            return;
        }
        System.out.println("Processing " + file);
        byte[] b = getBytesFromInputStream(new java.io.FileInputStream(file));
        String html = new String(b);
        html = removeMarkedLine(html);

        
        if (changeImagePath) {
            html = changeImagesPath(html);
        }
        if (changeImagePath) {
            name = "_" + file.getName();
        } else {
            name = "__" + file.getName();
        }
		b = html.getBytes();
        java.io.File newFile = new java.io.File(file.getParentFile(), name);
        newFile.delete();
        java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(
                    newFile));
        out.write(b, 0, b.length);
        out.flush();
        out.close();
    }

    private static String removeMarkedLine(String html) {
        StringBuffer newHtml = new StringBuffer(html.length());
        int currentIndex = 0;
        while (true) {
            int markerIndex = html.indexOf("~~~", currentIndex);
            if (markerIndex == -1) {
                break;
            }
            int endIndex = html.indexOf("-->", markerIndex);
            if (endIndex == -1) {
                break;
            }
            newHtml.append(html.substring(currentIndex, markerIndex));
            currentIndex = endIndex + 3;
        }
        if (currentIndex < html.length()) {
            newHtml.append(html.substring(currentIndex, html.length()));
        }
        return newHtml.toString();
    }

    /**
     * change img src="... .gif" to img src="..._pdf.gif"
     */
    private static String changeImagesPath(String html) {
        StringBuffer newHtml = new StringBuffer(html.length());
        int currentIndex = 0;
        while (true) {
            int gifIndex = html.indexOf(".gif\"", currentIndex);
            if (gifIndex == -1) {
                break;
                
            }
            newHtml.append(html.substring(currentIndex, gifIndex));
            newHtml.append("_pdf.gif");
            currentIndex = gifIndex + 4;
        }
        if (currentIndex < html.length()) {
            newHtml.append(html.substring(currentIndex, html.length()));
        }
        return newHtml.toString();
    }

    private static void filter(java.io.File file) throws java.io.IOException {
        java.io.File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return;
        }
        for (int i = 0; i < listFiles.length; i++) {
            java.io.File fileItem = listFiles[i];
            if (fileItem.isFile()) {
                filterFile(fileItem);
            }
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in)
        throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[in.available()];
        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }
        return bytecodes;
    }
}
