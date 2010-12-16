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
package org.ow2.proactive.scheduler.ext.matlab.worker.util;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;


public class MatlabEngineConfig extends MatSciEngineConfigBase {

    protected transient static Process selectionScriptProcess = null;
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     * The home dir of Matlab on this machine
     */
    private String matlabHome = null;

    /**
     * The name of the arch dir to find native libraries (can be win32, glnx86, ...)
     */
    private String matlabLibDirName = null;

    /**
     * The path to matlab external libraries
     */
    private String matlabExtDir = null;

    /**
     * The path to matlab bin dir
     */
    private String matlabBinDir = null;

    /**
     * Version of Matlab
     */
    private String matlabVersion;

    /**
     * the name of the Matlab command on this machine
     */
    private String matlabCommandName = null;

    private boolean hasManyConfigs;

    /**
     * Path to the ptolemy library dir.
     */
    private String ptolemyPath;

    private static final String nl = System.getProperty("line.separator");

    public MatlabEngineConfig(String matlabHome, String matlabVersion, String matlabLibDirName,
            String matlabBinDir, String matlabExtDirName, String matlabCommandName, boolean hasManyConfigs) {
        this.matlabHome = matlabHome.replaceAll("" + '\u0000', "");
        this.matlabVersion = matlabVersion.replaceAll("" + '\u0000', "");
        this.matlabLibDirName = matlabLibDirName.replaceAll("" + '\u0000', "");
        this.matlabBinDir = matlabBinDir.replaceAll("" + '\u0000', "");
        if (matlabExtDirName != null) {
            this.matlabExtDir = matlabExtDirName.replaceAll("" + '\u0000', "");
        }
        this.matlabCommandName = matlabCommandName.replaceAll("" + '\u0000', "");
        this.hasManyConfigs = hasManyConfigs;
    }

    public MatlabEngineConfig(String matlabHome, String matlabVersion, String matlabLibDirName,
            String matlabBinDir, String matlabExtDirName, String matlabCommandName, boolean hasManyConfigs,
            String ptolemyPath) {
        this(matlabHome, matlabVersion, matlabLibDirName, matlabBinDir, matlabExtDirName, matlabCommandName,
                hasManyConfigs);
        this.ptolemyPath = ptolemyPath.replaceAll("" + '\u0000', "");
    }

    public static void setSelectionScriptProcess(Process p) {
        selectionScriptProcess = p;
    }

    public static Process getSelectionScriptProcess() {
        return selectionScriptProcess;
    }

    public void setPtolemyPath(String ptolemyPath) {
        this.ptolemyPath = ptolemyPath.replaceAll("" + '\u0000', "");
    }

    /**
     * returns the home dir of Matlab
     *
     * @return home dir
     */
    public String getMatlabHome() {
        return matlabHome;
    }

    /**
     * returns the relative path of the lib directory on this matlab install
     *
     * @return lib dir name
     */
    public String getMatlabLibDirName() {
        return matlabLibDirName;
    }

    public String getMatlabBinDir() {
        return matlabBinDir;
    }

    public String getMatlabExtDir() {
        return matlabExtDir;
    }

    /**
     * returns the path to the ptolemy library directory
     *
     * @return ptolemy lib dir
     */
    public String getPtolemyPath() {
        return ptolemyPath;
    }

    public boolean hasManyConfig() {
        return hasManyConfigs;
    }

    /**
     * Returns the current matlab version.
     *
     * @return matlab version
     */
    public String getVersion() {
        return matlabVersion;
    }

    public String getFullCommand() {
        if (os.equals(OperatingSystem.windows)) {
            return matlabHome + os.fileSeparator() + matlabBinDir + os.fileSeparator() + matlabCommandName;
        } else {
            return matlabCommandName;
        }
    }

    /**
     * Returns the name of the matlab command.
     *
     * @return returns the String containing the name of the Matlab command
     */
    public String getMatlabCommandName() {
        return matlabCommandName;
    }

    @Override
    public String toString() {
        return "Matlab home : " + matlabHome + nl + "Matlab version : " + matlabVersion + nl +
            "Matlab lib directory name : " + matlabLibDirName + nl + "Matlab bin directory  : " +
            matlabBinDir + nl + "Matlab command name : " + matlabCommandName + nl + "Ptolemy lib dir : " +
            ptolemyPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MatlabEngineConfig))
            return false;

        MatlabEngineConfig that = (MatlabEngineConfig) o;

        if (matlabBinDir != null ? !matlabBinDir.equals(that.matlabBinDir) : that.matlabBinDir != null)
            return false;
        if (matlabCommandName != null ? !matlabCommandName.equals(that.matlabCommandName)
                : that.matlabCommandName != null)
            return false;
        if (matlabHome != null ? !matlabHome.equals(that.matlabHome) : that.matlabHome != null)
            return false;
        if (matlabLibDirName != null ? !matlabLibDirName.equals(that.matlabLibDirName)
                : that.matlabLibDirName != null)
            return false;
        if (matlabVersion != null ? !matlabVersion.equals(that.matlabVersion) : that.matlabVersion != null)
            return false;
        if (ptolemyPath != null ? !ptolemyPath.equals(that.ptolemyPath) : that.ptolemyPath != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = matlabHome != null ? matlabHome.hashCode() : 0;
        result = 31 * result + (matlabLibDirName != null ? matlabLibDirName.hashCode() : 0);
        result = 31 * result + (matlabBinDir != null ? matlabBinDir.hashCode() : 0);
        result = 31 * result + (matlabVersion != null ? matlabVersion.hashCode() : 0);
        result = 31 * result + (matlabCommandName != null ? matlabCommandName.hashCode() : 0);
        result = 31 * result + (ptolemyPath != null ? ptolemyPath.hashCode() : 0);
        return result;
    }
}
