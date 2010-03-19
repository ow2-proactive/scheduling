/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package scalabilityTests.framework;

import java.io.File;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * This is an Actor with an active-object-like behaviour.
 * 
 * @author fabratu
 *
 */
@ActiveObject
public class ActiveActor<T, V> extends Actor<T, V> implements InitActive {

    public ActiveActor() {
        super();
    }

    public ActiveActor(Action<T, V> action) {
        super(action);
    }

    public ActiveActor(Action<T, V> action, T parameter) {
        super(action, parameter);
    }

    public void initActivity(Body arg0) {
        // log4j (re)configuration
        rebaseLogFiles();
    }

    /**
     * Log4j configuration tweaking
     * This method will modify the file name of every FileAppender
     * 	by adding the hostname of the machine onto which it executes.
     * Because it cannot be done from the log4j-configuration file
     * 	we hard-code this configuration issue
     * @param logger2 
     */
    private void rebaseLogFiles() {
        Enumeration<Logger> curLoggers = LogManager.getCurrentLoggers();
        while (curLoggers.hasMoreElements()) {
            Logger someLogger = curLoggers.nextElement();
            Enumeration<Appender> appenders = someLogger.getAllAppenders();
            while (appenders.hasMoreElements()) {
                Appender app = appenders.nextElement();
                if (app instanceof FileAppender) {
                    FileAppender fileApp = (FileAppender) app;
                    System.out.println("File appender, output file " + fileApp.getFile());
                    addHostToFilename(fileApp);
                }
            }
        }
    }

    private void addHostToFilename(FileAppender fileApp) {
        String hostname = ProActiveInet.getInstance().getHostname();
        String fileName = fileApp.getFile();
        File filePath = new File(fileName);
        String hostFileName;
        String name = filePath.getName();
        String pathToFile = filePath.getParent();
        int point = name.indexOf('.');
        if (point == -1) {
            hostFileName = fileName + "-" + hostname;
        } else {
            String extension = name.substring(point + 1);
            String nameNoExtension = name.substring(0, point);
            hostFileName = (pathToFile != null ? pathToFile : "") + File.separator + nameNoExtension + "-" +
                hostname + "." + extension;
        }
        System.out.println("New output file:" + hostFileName);
        fileApp.setFile(hostFileName);
        fileApp.activateOptions();
    }

    public void cleanup() {
        // destroy this active object
        PAActiveObject.terminateActiveObject(true);
    }

}
