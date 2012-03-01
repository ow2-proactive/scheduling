/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.scilab.worker.util;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;


public class ScilabEngineConfig extends MatSciEngineConfigBase {

    // the Home Dir of Scilab on this machine
    private String scilabHome = null;
    private String scilabBinDir = null;
    private String scilabCommandName = null;
    private String version = null;

    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    private static final String nl = System.getProperty("line.separator");

    /**
     * Current Scilab configuration
     */
    protected static ScilabEngineConfig currentConf = null;

    /**
     * last Scilab configuration
     */
    protected static ScilabEngineConfig lastConf = null;

    public String getScilabBinDir() {
        return scilabBinDir;
    }

    public String getScilabCommandName() {
        return scilabCommandName;
    }

    public ScilabEngineConfig(String scilabHome, String version, String scilabBinDir,
            String scilabCommandName, boolean hasManyConfigs) {
        this.scilabHome = scilabHome;
        this.version = version;
        this.scilabBinDir = scilabBinDir;
        this.scilabCommandName = scilabCommandName;

    }

    public static ScilabEngineConfig getCurrentConfiguration() {
        return currentConf;
    }

    public static void setCurrentConfiguration(ScilabEngineConfig conf) {
        lastConf = currentConf;
        currentConf = conf;
    }

    public static boolean hasChangedConf() {
        return (lastConf != null) && (!lastConf.equals(currentConf));
    }

    /**
     * The path where Scilab is installed
     *
     * @return scilab path
     */
    public String getScilabHome() {
        return scilabHome;
    }

    public String getVersion() {
        return version;
    }

    public String getFullCommand() {
        return scilabHome + os.fileSeparator() + scilabBinDir + os.fileSeparator() + scilabCommandName;
    }

    public boolean hasManyConfig() {
        return false;
    }


    public String toString() {
        return "Scilab Home : " + scilabHome + nl + "Scilab Version : " + version + nl + "Scilab binDir : " +
            scilabBinDir + nl + "Scilab command : " + scilabCommandName;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ScilabEngineConfig))
            return false;

        ScilabEngineConfig that = (ScilabEngineConfig) o;

        if (scilabBinDir != null ? !scilabBinDir.equals(that.scilabBinDir) : that.scilabBinDir != null)
            return false;
        if (scilabCommandName != null ? !scilabCommandName.equals(that.scilabCommandName)
                : that.scilabCommandName != null)
            return false;
        if (scilabHome != null ? !scilabHome.equals(that.scilabHome) : that.scilabHome != null)
            return false;
        if (version != null ? !version.equals(that.version) : that.version != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = scilabHome != null ? scilabHome.hashCode() : 0;
        result = 31 * result + (scilabBinDir != null ? scilabBinDir.hashCode() : 0);
        result = 31 * result + (scilabCommandName != null ? scilabCommandName.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
