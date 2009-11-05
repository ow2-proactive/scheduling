/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler;

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
    //    private String fileName = ".scheduler.java.policy";

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
     */
    public Object run(Object args) throws Exception {
        //        searchJavaPolicyFile();

        Display display = PlatformUI.createDisplay();
        Location location = Platform.getInstanceLocation();
        location.release();
        location.setURL(new URL("file:" + System.getProperty("user.home") +
            "/.ProActive_Scheduler/workspace/"), true);
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
    //                System.out.println("[Scheduler] Creates a new file: " + file);
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
