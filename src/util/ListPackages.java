package util;

public class ListPackages {

  public static void main(String[] arg) throws java.io.IOException {
    java.io.File sourceDir = new java.io.File("D:\\cygwin\\home\\lmestre\\ProActive\\src");
    java.io.File targetDir = new java.io.File("org\\objectweb\\proactive");
    java.io.File destFile = new java.io.File(sourceDir, "..\\compile\\packages.txt");
    java.io.Writer out = new java.io.BufferedWriter(new java.io.PrintWriter(new java.io.FileOutputStream(destFile), false));
    listPackages(sourceDir, targetDir, out);
    out.flush();
    out.close();
  }
  
  private static void listPackages(java.io.File baseDir, java.io.File targetDir, java.io.Writer out) throws java.io.IOException {
    java.io.File dir = new java.io.File(baseDir, targetDir.toString());
    java.io.File[] listFiles = dir.listFiles();
    if (listFiles == null) return;
    int javaFilesCount = countJavaFiles(dir);
    if (javaFilesCount > 0) {
      out.write(targetDir.toString().replace('\\','.'));
      out.write("\n");
    }
    for (int i=0; i<listFiles.length; i++) {
      java.io.File fileItem = listFiles[i];
      if (fileItem.isDirectory() && acceptDirectory(fileItem.getName())) {
        java.io.File currentDir = new java.io.File(targetDir, fileItem.getName());
        listPackages(baseDir, currentDir, out);
      }
    }
  }
  
  
  private static int countJavaFiles(java.io.File targetDir) {
    java.io.File[] listFiles = targetDir.listFiles();
    if (listFiles == null) return 0;
    int count = 0;
    for (int i=0; i<listFiles.length; i++) {
      java.io.File fileItem = listFiles[i];
      if (fileItem.isFile() && fileItem.toString().endsWith(".java")) {
        count++;
      }
    }
    return count;
  }


  private static boolean acceptDirectory(String dirName) {
    if (dirName.indexOf("CVS") > -1) return false;
    if (dirName.indexOf("doc-files") > -1) return false;
    if (dirName.indexOf("images") > -1) return false;
    if (dirName.indexOf("examples") > -1) return false;
    if (dirName.indexOf("security") > -1) return false;
    if (dirName.indexOf("ic2d") > -1) return false;
    return true;
  }

}