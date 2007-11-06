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
package org.objectweb.proactive.core.util.log;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.config.PAProperties;


/**
 * @author Arnaud Contes
 *
 *  This class stores all logger used in proactive. It provides an easy way
 *  to create and to retrieve a logger.
 */
public class ProActiveLogger extends Logger {

    static {
        if (System.getProperty("log4j.configuration") == null) {
            try {
                InputStream in = PAProperties.class.getResourceAsStream(
                        "proactive-log4j");

                // testing the availability of the file
                Properties p = new Properties();
                p.load(in);
                PropertyConfigurator.configure(p);
            } catch (Exception e) {
                URL u = PAProperties.class.getResource("proactive-log4j");
                System.err.println("the default log4j configuration file (" +
                    u + ") is not accessible, logging is disabled");
            }
        }
    }

    private static ProActiveLoggerFactory myFactory = new ProActiveLoggerFactory();

    /**
       Just calls the parent constuctor.
     */
    protected ProActiveLogger(String name) {
        super(name);
    }

    /**
       This method overrides {@link Logger#getLogger} by supplying
       its own factory type as a parameter.
     */
    public static Logger getLogger(String name) {
        return Logger.getLogger(name, myFactory);
    }

    /**
     * Get corresponding stack trace as string
     * @param e A Throwable
     * @return The output of printStackTrace is returned as a String
     */
    public static String getStackTraceAsString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
