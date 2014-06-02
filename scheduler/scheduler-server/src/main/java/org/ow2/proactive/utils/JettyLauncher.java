/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;



/**
 * Deploy one or more Web Applications in a Jetty Web Server
 *
 * @deprecated Replaced by JettyStarter
 * @author mschnoor
 *
 */
public class JettyLauncher {

    private static void usage() {
        System.out.println("Usage: java " + JettyLauncher.class.getCanonicalName() +
            " [options] app1 [app2...]");
        System.out.println();
        System.out.println("Mandatory argument is at least one Web Application.");
        System.out.println("An application is either a valid .war file or a directory");
        System.out.println("such that the file /WEB-INF/web.xml exists.");
        System.out.println();
        System.out.println("Options:");
        System.out.println("-p port    HTTP server port, defaults to 8080");
        System.out.println("-l file    Log to file, defaults to stdout");
        System.out.println("-h         Print this message and exit");
    }

    private static void error(String message) {
        System.out.println(message);
        usage();
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        PrintStream sysout = System.out;

        try {
            ArrayList<File> apps = new ArrayList<File>();
            int port = 8080;
            File logFile = null;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                if (arg.equals("-p")) {
                    if (i + 1 == args.length) {
                        error("No port specified for option -p");
                    } else {
                        String p = args[i + 1];
                        try {
                            port = Integer.parseInt(p);
                        } catch (NumberFormatException n) {
                            error("Port should be an integer");
                        }
                        i++;
                    }
                } else if (arg.equals("-l")) {
                    if (i + 1 == args.length) {
                        error("No file specified for options -l");
                    } else {
                        String f = args[i + 1];
                        logFile = new File(f);
                        if (logFile.isDirectory()) {
                            error(f + " is a directory");
                        }
                        if (logFile.exists() && !logFile.delete()) {
                            error(f + ": file exists");
                        }
                        i++;
                    }
                } else if (arg.equals("-h")) {
                    usage();
                    System.exit(0);
                } else {
                    File app = new File(arg);
                    if (app.exists()) {
                        apps.add(app);
                    } else {
                        error("File " + arg + " does not exist");
                    }
                }
            }

            if (apps.size() == 0) {
                error("You need to specify at least one application as argument");
            }

            if (logFile != null) {
                PrintStream ps = new PrintStream(new FileOutputStream(logFile));
                System.setOut(ps);
                System.setErr(ps);
            }

            Server server = new Server(port);
            HandlerList handlerList = new HandlerList();
            for (File app : apps) {
                String name = app.getName();
                if (app.isFile() && app.getName().indexOf('.') >= 0) {
                    name = app.getName().substring(0, app.getName().lastIndexOf("."));
                }
                WebAppContext webapp = new WebAppContext(app.getAbsolutePath(), "/" + name);
                handlerList.addHandler(webapp);
                sysout.println("Deployed application: http://localhost:" + port + "/" + name);
            }
            server.setHandler(handlerList);

            server.start();
            server.join();

        } catch (Throwable t) {
            t.printStackTrace(sysout);
        }
    }
}
