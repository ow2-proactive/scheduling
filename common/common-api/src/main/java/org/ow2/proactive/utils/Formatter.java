/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


/**
 * Formatter is a class grouping tools for formatting
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public final class Formatter {

    /**
     * Return the stack trace of the given exception in a string.
     *
     * @param e
     *            an exception or throwable
     * @return the stack trace of the given exception in a string.
     */
    public static String stackTraceToString(Throwable aThrowable) {
        Writer result = null;
        PrintWriter printWriter = null;
        try {
            result = new StringWriter();
            printWriter = new PrintWriter(result);
            aThrowable.printStackTrace(printWriter);
        } finally {
            if (printWriter != null)
                printWriter.close();
            if (result != null)
                try {
                    result.close();
                } catch (Exception e) {
                    //was not able to produce the String representing the exception
                    System.out.println("Could not get the stacktrace for the following ex: ");
                    aThrowable.printStackTrace();
                    System.out
                            .println("An exception occured while constructing the String representation of the exception above: ");
                    e.printStackTrace();
                }
        }
        return result.toString();
    }

}
