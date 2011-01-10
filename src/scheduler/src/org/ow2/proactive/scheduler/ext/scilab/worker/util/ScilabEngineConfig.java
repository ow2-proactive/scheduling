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

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;
	// the Home Dir of Scilab on this machine
    private String scilabHome = null;
    private String scilabLibdir = null;
    private String scilabScidir = null;
    private String scilabBinDir = null;
    private String scilabCommandName = null;
    private String version = null;

    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    private String thirdPartyDir = null;

    private static final String nl = System.getProperty("line.separator");

    protected static ScilabEngineConfig currentConf = null;

    public String getScilabBinDir() {
        return scilabBinDir;
    }

    public String getScilabCommandName() {
        return scilabCommandName;
    }

    public ScilabEngineConfig(String scilabHome, String version, String scilabLibDir, String scilabSciDir,
            String thirdPartyDir, String scilabBinDir, String scilabCommandName, boolean hasManyConfigs) {
        this.scilabHome = scilabHome;
        this.scilabLibdir = scilabLibDir;
        this.scilabScidir = scilabSciDir;
        this.version = version;
        this.thirdPartyDir = thirdPartyDir;
        this.scilabBinDir = scilabBinDir;
        this.scilabCommandName = scilabCommandName;

    }

    /**
     * The path where Scilab is installed
     *
     * @return scilab path
     */
    public String getScilabHome() {
        return scilabHome;
    }

    /**
     * The relative path (from Scilab home) where the javasci library can be found
     *
     * @return scilab path
     */
    public String getScilabLibDir() {
        return scilabLibdir;
    }

    /**
     * The absolute path where the scilab SCI dir is supposed to be
     *
     * @return scilab path
     */
    public String getScilabSCIDir() {
        return scilabScidir;
    }

    public String getThirdPartyDir() {
        return thirdPartyDir;
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

    @Override
    public String toString() {
        return "Scilab Home : " + scilabHome + nl + "Scilab Version : " + version + nl + "Scilab libDir : " +
            scilabLibdir + nl + "Scilab SCIDir : " + scilabScidir + nl + "Scilab ThirdPartyDir : " +
            thirdPartyDir + nl + "Scilab binDir : " + scilabBinDir + nl + "Scilab command : " +
            scilabCommandName;
    }

    @Override
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
        if (scilabLibdir != null ? !scilabLibdir.equals(that.scilabLibdir) : that.scilabLibdir != null)
            return false;
        if (scilabScidir != null ? !scilabScidir.equals(that.scilabScidir) : that.scilabScidir != null)
            return false;
        if (thirdPartyDir != null ? !thirdPartyDir.equals(that.thirdPartyDir) : that.thirdPartyDir != null)
            return false;
        if (version != null ? !version.equals(that.version) : that.version != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = scilabHome != null ? scilabHome.hashCode() : 0;
        result = 31 * result + (scilabLibdir != null ? scilabLibdir.hashCode() : 0);
        result = 31 * result + (scilabScidir != null ? scilabScidir.hashCode() : 0);
        result = 31 * result + (scilabBinDir != null ? scilabBinDir.hashCode() : 0);
        result = 31 * result + (scilabCommandName != null ? scilabCommandName.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (thirdPartyDir != null ? thirdPartyDir.hashCode() : 0);
        return result;
    }
}
