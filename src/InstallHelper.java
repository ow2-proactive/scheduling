/**
 * This class is a very simple utility program that prints out the two
 * directories the user needs to add to his/her CLASSPATH environment
 * variable in order to get ProActive to run. It is important not to run this
 * class with a security manager installed, since some of the properties we
 * read here (namely, user.dir and user.home) are not part of the default
 * properties and would require granting explicit privileges to the 
 * application by the user through the user policy file
 */

public class InstallHelper {

  public static void main(String[] args) {
    // Test the version of Java used to run this program
    String version = System.getProperty("java.version");
    System.out.println("JVM version: " + version);
  
    // Locates the root directory for ProActive classes
    java.io.File currentDirectory = new java.io.File(System.getProperty("user.dir"));
    java.io.File paDirectory = currentDirectory.getParentFile();
    java.io.File paJAR = new java.io.File(paDirectory, "ProActive.jar");
    
    // Locates the root directory for stubs
    java.io.File homeDirectory = new java.io.File(System.getProperty("user.home"));
    java.io.File stubsDirectory = new java.io.File(homeDirectory, "proactive-tmp");
  
    // If this directory does not exist, create it
    if (!stubsDirectory.exists()) {
      stubsDirectory.mkdirs();
      System.out.println("Creating directory " + stubsDirectory);
    }
  
    // Summary message
    System.out.println("\nThe two entries you need to add to your CLASSPATH are:\n");
    System.out.println(paJAR);
    System.out.println(stubsDirectory);
    System.out.println();
  }
}
