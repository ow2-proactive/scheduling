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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.filessplitmerge.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.ExceptionToStringHelper;


/**
 * The singleton instance of this class is to be used in order to perform logs
 * 
 * It stores a Log4j Loggger and a list of {@link GenericLogger} The
 * {@link GenericLogger} are entities of the application (parts of the UI) that
 * might be interested in information to be logged
 * 
 * @author esalagea
 * 
 */
public class LoggerManager implements GenericLogger {

    /**
     * Log4j Logger name
     */
    public static final String EPF_LOGGERR_NAME = "proactive.epf";
    public static final String log4jFileName = "epf-log4j";

    /**
     * A list of loggers that will be notified when information to be logged is
     * received by this object i.e. : A textual UI might be one of these loggers
     */
    private List<GenericLogger> loggers;

    /**
     * a log4j loggger defined in the log4j.properties file
     */
    private static Logger epfLogger = null;

    /**
     * singleton instance
     */
    private static LoggerManager instance;

    private LoggerManager() {
        loggers = new LinkedList<GenericLogger>();
    }

    /**
     * 
     * @return tthe singleton instance
     */
    public static LoggerManager getInstane() {
        if (instance == null) {
            instance = new LoggerManager();
        }
        return instance;
    }

    private static boolean loaded = false;

    // Callers must be synchronized to avoid race conditions
    private static void load() {

        String configurationFile = System.getProperty("log4j.configuration");
        //System.out.println("LoggerManager.load() - using log4j config file: "+configurationFile);
        // If log4j.configuration is set, log4j will use this file automatically
        if (configurationFile == null) {
            //System.out.println("LoggerManager.load() -> loading default configurayion file");
            // We have to load load the log4j configuration by ourself
            Properties p = new Properties();
            // Load the default proactive-log4j file embedded in the ProActive.jar
            InputStream in = LoggerManager.class.getResourceAsStream(log4jFileName);
            try {
                p.load(in);
            } catch (IOException e1) {
                System.err.println("Failed to read the default configuration file:" + e1.getMessage());
            }

            PropertyConfigurator.configure(p);
        }
        loaded = true;
    }

    /**
     * uses the log4j defined logger
     * 
     * @param msg
     */
    public void debug(String msg) {
        this.getLogger().debug(msg);
    }

    /**
     * Adds a new generic logger
     * 
     * @param l
     */
    public void addLogger(GenericLogger l) {
        loggers.add(l);
    }

    /**
     * logs the error's message on the log4j logger sends it to each
     * GenericLogger
     */
    //@Override
    public void error(String message) {
        LoggerManager.getLogger().error("ERROR: " + message + " ");
        Iterator<GenericLogger> i = loggers.iterator();
        while (i.hasNext()) {
            GenericLogger gl = i.next();
            gl.error(message);
        }

    }

    /**
     * logs the error on the log4j logger sends it to each GenericLogger
     */

    //@Override
    public void error(String message, Exception e) {
        LoggerManager.getLogger().error("Exception is: " + ExceptionToStringHelper.getStackTrace(e));
        Iterator<GenericLogger> i = loggers.iterator();
        while (i.hasNext()) {
            GenericLogger gl = i.next();
            gl.error(message, e);
        }
    }

    /**
     * Logs the info on the log4j logger sends it to each GenericLogger
     */
    //@Override
    public void info(String msg) {
        LoggerManager.getLogger().info("USER COSOLE INFO: " + msg);

        Iterator<GenericLogger> i = loggers.iterator();
        while (i.hasNext()) {
            GenericLogger gl = i.next();
            gl.info(msg);
        }

    }

    /**
     * Logs the warning's message on the log4j logger sends it to each
     * GenericLogger
     */
    //@Override
    public void warning(String msg) {
        LoggerManager.getLogger().warn("USER COSOLE WARNING: " + msg);
        Iterator<GenericLogger> i = loggers.iterator();
        while (i.hasNext()) {
            GenericLogger gl = i.next();
            gl.warning(msg);
        }

    }

    /**
     * Logs the warning on the log4j logger sends it to each GenericLogger
     */

    //@Override
    public void warning(String message, Exception e) {
        LoggerManager.getLogger().warn(" Exception is: " + ExceptionToStringHelper.getStackTrace(e));
        Iterator<GenericLogger> i = loggers.iterator();
        while (i.hasNext()) {
            GenericLogger gl = i.next();
            gl.warning(message, e);
        }

    }

    /**
     * return the log4j defined logger This is to be used to log information
     * that should not appear on any user interfaces
     * 
     * @return
     */
    public static Logger getLogger() {
        if (!loaded) {
            load();
        }

        if (epfLogger == null) {
            epfLogger = Logger.getLogger(EPF_LOGGERR_NAME);

        }
        return epfLogger;

    }

}
