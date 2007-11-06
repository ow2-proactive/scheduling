/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.ext.matlab;

import java.io.PrintWriter;
import java.io.StringWriter;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.Engine;


public class MatlabEngine {
    static Engine eng = null;
    static long[] engineHandle;
    static String commandName;

    static void init() throws IllegalActionException {
        if (eng == null) {
            try {
                eng = new Engine();

                eng.setDebugging((byte) 0);

                engineHandle = eng.open(commandName +
                        " -nodisplay -nosplash -nodesktop", true);
            } catch (UnsatisfiedLinkError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the Matlab libraries");
                pw.println("PATH=" + System.getenv("PATH"));
                pw.println("LD_LIBRARY_PATH=" +
                    System.getenv("LD_LIBRARY_PATH"));
                pw.println("java.library.path=" +
                    System.getProperty("java.library.path"));

                UnsatisfiedLinkError ne = new UnsatisfiedLinkError(error_message.toString());
                ne.initCause(e);
                throw ne;
            } catch (NoClassDefFoundError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the ptolemy classes");
                pw.println("java.class.path=" +
                    System.getProperty("java.class.path"));
                NoClassDefFoundError ne = new NoClassDefFoundError(error_message.toString());
                ne.initCause(e);
                throw ne;
            }
        }
    }

    public static void setCommandName(String name) {
        commandName = name;
    }

    public static String getCommandName() {
        return commandName;
    }

    public static void evalString(String command) throws IllegalActionException {
        init();
        eng.evalString(engineHandle, command);
    }

    public static Token get(String variableName) throws IllegalActionException {
        init();
        return eng.get(engineHandle, variableName);
    }

    public static void put(String variableName, Token token)
        throws IllegalActionException {
        init();
        eng.put(engineHandle, variableName, token);
    }

    public static void close() {
        eng.close(engineHandle);
        eng = null;
    }
}
