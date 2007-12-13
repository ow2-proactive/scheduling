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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggerFactory;
import org.objectweb.proactive.core.config.PAProperties;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Feb 21, 2005
 */
public class ProActiveLoggerFactory implements LoggerFactory {

    static {
        if (System.getProperty("log4j.configuration") == null) {
            Properties p = new Properties();
            try {
                InputStream in = PAProperties.class.getResourceAsStream("proactive-log4j");
                if (in != null) {
                    p.load(in);
                    PropertyConfigurator.configure(p);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a new ProActiveLogger with Diagnostic Context information (hostname and runtime).
     * @see org.apache.log4j.spi.LoggerFactory#makeNewLoggerInstance(java.lang.String)
     */
    public Logger makeNewLoggerInstance(String name) {
        if (MDC.get("hostname") == null) {
            MDC.put("hostname", getHostName());
        }
        if (MDC.get("runtime") == null) {
            MDC.put("runtime", "unknown runtime");
        }

        return new ProActiveLogger(name);
    }

    private static String getHostName() {
        try {
            // ProActiveInet.getInstance().getLocal() cannot be used here since loggers are used in
            // ProActiveConfiguration constructor.
            // It should not be that important. AFAIK this method is only used to 
            // print a hostname in the logs
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown host";
        }
    }
}
