package org.objectweb.proactive.resourcemanager;

import java.net.URL;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;


/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IPlatformRunnable {
    //    private String fileName = ".resource.manager.java.policy";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
     */
    public Object run(Object args) throws Exception {
        //        searchJavaPolicyFile();

        Display display = PlatformUI.createDisplay();
        Location location = Platform.getInstanceLocation();
        location.release();
        location.setURL(new URL("file:" + System.getProperty("user.home") +
            "/.ProActive_ResourceManager/workspace/"), true);
        try {
            int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return IPlatformRunnable.EXIT_RESTART;
            }
            return IPlatformRunnable.EXIT_OK;
        } finally {
            display.dispose();
        }
    }
    //
    //    /**
    //     * Searches the '.ic2d.java.policy' file,
    //     * if this one doesn't exist then a new file is created.
    //     */
    //    private void searchJavaPolicyFile() {
    //        String pathName = System.getProperty("user.home");
    //        String separator = System.getProperty("file.separator");
    //        String file = pathName + separator + fileName;
    //
    //        try {
    //            // Seaches the '.ic2d.java.policy'
    //            new FileReader(file);
    //        }
    //        // If it doesn't exist
    //        catch (FileNotFoundException e) {
    //            BufferedWriter bw = null;
    //            try {
    //                // Creates an '.ic2d.java.policy' file
    //                bw = new BufferedWriter(new FileWriter(file, false));
    //                PrintWriter pw = new PrintWriter(bw, true);
    //                pw.println("grant {");
    //                pw.println("permission java.security.AllPermission;");
    //                pw.println("};");
    //                System.out.println("[ResourceManager] Creates a new file: " + file);
    //            } catch (IOException eio) {
    //                eio.printStackTrace();
    //            } finally {
    //                try {
    //                    bw.close();
    //                } catch (IOException eio) {
    //                    eio.printStackTrace();
    //                }
    //            }
    //        }
    //    }
}