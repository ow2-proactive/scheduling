/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAProperties.PAPropertiesType;


/**
 * PASchedulerProperties contains all ProActive Scheduler properties.
 * 
 * You must use provide methods in order to get the Scheduler properties.
 * 
 * @author The ProActiveTeam
 * @date 11 june 08
 * @since ProActive 4.0
 *
 */
@PublicAPI
public enum PASchedulerProperties {

    /* ***************************************************************** */
    /* ********************** SCHEDULER PROPERTIES ********************* */
    /* ***************************************************************** */

    /** Default scheduler node name */
    SCHEDULER_DEFAULT_NAME("pa.scheduler.core.defaultname", PAPropertiesType.STRING),

    /** Scheduler main loop time out */
    SCHEDULER_TIME_OUT("pa.scheduler.core.timeout", PAPropertiesType.INTEGER),

    /** Scheduler node ping frequency in ms. */
    SCHEDULER_NODE_PING_FREQUENCY("pa.scheduler.core.nodepingfrequency", PAPropertiesType.INTEGER),

    /** Delay to wait between getting a job result and removing the job concerned */
    SCHEDULER_REMOVED_JOB_DELAY("pa.scheduler.core.removejobdelay", PAPropertiesType.INTEGER),

    /** Default database configuration file name */
    SCHEDULER_DEFAULT_DBCONFIG_FILE("pa.scheduler.core.database.defaultconfigfile", PAPropertiesType.STRING),

    /** Login default file name */
    SCHEDULER_LOGIN_FILENAME("pa.scheduler.core.defaultloginfilename", PAPropertiesType.STRING),

    /** Group default filename */
    SCHEDULER_GROUP_FILENAME("pa.scheduler.core.defaultgroupfilename", PAPropertiesType.STRING),

    /** Cache classes definition in task class servers  */
    SCHEDULER_CLASSSERVER_USECACHE("pa.scheduler.classserver.usecache", PAPropertiesType.BOOLEAN),

    /** Scheduler default policy full name.  */
    SCHEDULER_DEFAULT_POLICY("pa.scheduler.policy", PAPropertiesType.STRING),

    /* ***************************************************************** */
    /* ************************* JOBS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Default job name */
    JOB_DEFAULT_NAME("pa.scheduler.job.defaultname", PAPropertiesType.STRING),

    /** Multiplicative factor for job id (taskId will be : this_factor*jobID+taskID) */
    JOB_FACTOR("pa.scheduler.job.factor", PAPropertiesType.INTEGER),

    /* ***************************************************************** */
    /* ************************ TASKS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Default task name */
    TASK_DEFAULT_NAME("pa.scheduler.task.defaultname", PAPropertiesType.STRING),

    /* ***************************************************************** */
    /* ************************* LOGS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Log forwarder default listening port */
    LOGS_LISTEN_PORT("pa.scheduler.logs.listenport", PAPropertiesType.INTEGER),

    /* ***************************************************************** */
    /* ************************ OTHER PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Name of the environment variable for windows home directory on the common file system. */
    WINDOWS_HOME_ENV_VAR("pa.scheduler.launcher.windowsenv", PAPropertiesType.STRING),

    /** Name of the environment variable for unix home directory on the common file system. */
    UNIX_HOME_ENV_VAR("pa.scheduler.launcher.unixenv", PAPropertiesType.STRING),

    /** Java security policy file path */
    FORKEDJAVA_SECURITY_POLICY("pa.scheduler.forkedJava.security.policy", PAPropertiesType.STRING),

    /** Scheduler home directory */
    SCHEDULER_HOME("pa.scheduler.home", PAPropertiesType.STRING),

    /* ------------------------------------
     *  SCHEDULER EXTENSIONS
     */
    /** Matlab finder under linux/unix */
    MATLAB_SCRIPT_LINUX("pa.scheduler.ext.matlab.script.linux", PAPropertiesType.STRING),

    /** Matlab finder under windows */
    MATLAB_SCRIPT_WINDOWS("pa.scheduler.ext.matlab.script.windows", PAPropertiesType.STRING),

    /** Scilab finder under linux/unix */
    SCILAB_SCRIPT_LINUX("pa.scheduler.ext.scilab.script.linux", PAPropertiesType.STRING),

    /** Scilab finder under windows */
    SCILAB_SCRIPT_WINDOWS("pa.scheduler.ext.scilab.script.windows", PAPropertiesType.STRING);

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    /** Default properties file for the scheduler configuration */
    private static final String DEFAULT_PROPERTIES_FILE = "PASchedulerProperties.ini";
    /** memory entity of the properties file. */
    private static Properties prop = null;
    /** Key of the specific instance. */
    private String key;
    /** value of the specific instance. */
    private PAPropertiesType type;

    /**
     * Create a new instance of PASchedulerProperties
     *
     * @param str the key of the instance.
     * @param type the real java type of this instance.
     */
    PASchedulerProperties(String str, PAPropertiesType type) {
        this.key = str;
        this.type = type;
    }

    /**
     * Set the user java properties to the PASchedulerProperties.<br/>
     * User properties are defined using the -Dname=value in the java command.
     */
    private static void setUserJavaProperties() {
        for (Object o : prop.keySet()) {
            String s = System.getProperty((String) o);
            if (s != null) {
                prop.setProperty((String) o, s);
            }
        }
    }

    /**
     * Get the properties map or load it if needed.
     * 
     * @return the properties map corresponding to the default property file.
     */
    private static Properties getProperties() {
        if (prop == null) {
            prop = new Properties();
            try {
                prop.load(PASchedulerProperties.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE));
                setUserJavaProperties();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return prop;
    }

    /**
     * override properties defined in the default configuration file,
     * by properties defined in another file.
     * @param filename path of file containing some properties to override
     */
    public static void updateProperties(String filename) {
        //load properties file if needed
        getProperties();
        Properties ptmp = new Properties();
        try {
            ptmp.load(new FileInputStream(filename));
            for (Object o : ptmp.keySet()) {
                prop.setProperty((String) o, (String) ptmp.get(o));
            }
            setUserJavaProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the value of this property as an integer.
     * If value is not an integer, an exception will be thrown.
     * 
     * @return the value of this property.
     */
    public int getValueAsInt() {
        String valueS = getValueAsString();
        try {
            int value = Integer.parseInt(valueS);
            return value;
        } catch (NumberFormatException e) {
            RuntimeException re = new IllegalArgumentException(key +
                " is not an integer property. getValueAsInt cannot be called on this property");
            throw re;
        }
    }

    /**
     * Returns the value of this property as a string.
     * 
     * @return the value of this property.
     */
    public String getValueAsString() {
        return getProperties().getProperty(key);
    }

    /**
     * Returns the value of this property as a boolean.
     * If value is not a boolean, an exception will be thrown.<br>
     * The behavior of this method is the same as the {@link java.lang.Boolean#parseBoolean(String s)}. 
     * 
     * @return the value of this property.
     */
    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(getValueAsString());
    }

    /**
     * Return the type of the given properties.
     *
     * @return the type of the given properties.
     */
    public PAPropertiesType getType() {
        return type;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return getValueAsString();
    }

}
