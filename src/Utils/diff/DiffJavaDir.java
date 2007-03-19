package diff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DiffJavaDir {

    /**
     * Perform an unified diff between to directory.
     * Only Java file are compared
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            usageAndExit();
        }

        File dir1 = new File(args[0]);
        File dir2 = new File(args[1]);

        if (!directoryExist(dir1) || !directoryExist(dir2)) {
            System.exit(1);
        }

        List<File> files = exploreDirectory(dir1);
        boolean diffFound = false;

        for (File file : files) {
            String file2 = file.toString().replaceFirst(dir1.toString(), dir2.toString());
            if (!new File(file2).exists()) {
                System.err.println(file2 + " does not exist in " + dir2);
                diffFound = true;
                continue;
            }

            if (DiffPrint.printUnifiedDiff(file.toString(), file2)) {
                diffFound = true;
            }
        }

        if (diffFound) {
            System.exit(1);
        }
    }

    static private List<File> exploreDirectory(File dir) {
        List<File> files = new ArrayList<File>();

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(exploreDirectory(file));
            }

            if (!file.toString().endsWith(".java")) {
                continue;
            }

            files.add(file);
        }

        return files;
    }

    private static boolean directoryExist(File dir) {
        if (!dir.exists()) {
            return false;
        }
        if (!dir.isDirectory()) {
            return false;
        }
        if (!dir.canRead()) {
            return false;
        }

        return true;
    }

    private static void usageAndExit() {
        System.err.println("Usage:");
        System.err.println("\tcommand dir1 dir2");
        System.exit(2);
    }
}
