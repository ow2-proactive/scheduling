/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.matlab.util;

import java.io.Serializable;


public class MatlabConfiguration implements Serializable {

    /**
     * The home dir of Matlab on this machine
     */
    private String matlabHome = null;

    /**
     * The name of the arch dir to find native libraries (can be win32, glnx86, ...)
     */
    private String matlabLibDirName = null;

    /**
     * The path to matlab bin dir
     */
    private String matlabBinDir = null;

    /**
     * Version of Matlab 
     */
    private String matlabVersion;

    /**
     *  the name of the Matlab command on this machine
     */
    private String matlabCommandName = null;

    /**
     * Path to the ptolemy library dir.
     */
    private String ptolemyPath;

    private static final String nl = System.getProperty("line.separator");;

    public MatlabConfiguration(String matlabHome, String matlabVersion, String matlabLibDirName,
            String matlabBinDir, String matlabCommandName, String ptolemyPath) {
        this.matlabHome = matlabHome;
        this.matlabVersion = matlabVersion;
        this.matlabLibDirName = matlabLibDirName;
        this.matlabBinDir = matlabBinDir;
        this.matlabCommandName = matlabCommandName;
        this.ptolemyPath = ptolemyPath;
    }

    /**
     * returns the home dir of Matlab
     * @return home dir
     */
    public String getMatlabHome() {
        return matlabHome;
    }

    /**
     * returns the relative path of the lib directory on this matlab install
     * @return lib dir name
     */
    public String getMatlabLibDirName() {
        return matlabLibDirName;
    }

    public String getMatlabBinDir() {
        return matlabBinDir;
    }

    /**
     * returns the path to the ptolemy library directory
     * @return ptolemy lib dir
     */
    public String getPtolemyPath() {
        return ptolemyPath;
    }

    /**
     * Returns the current matlab version.
     * @return matlab version
     */
    public String getMatlabVersion() {
        return matlabVersion;
    }

    /**
     * Returns the name of the matlab command.
     * @return returns the String containing the name of the Matlab command
     */
    public String getMatlabCommandName() {
        return matlabCommandName;
    }

    @Override
    public String toString() {
        return "Matlab home : " + matlabHome + nl + "Matlab version : " + matlabVersion + nl +
            "Matlab lib directory name : " + matlabLibDirName + nl + "Matlab bin directory  : " +
            matlabBinDir + nl + "Matlab command name:" + matlabCommandName + nl + "Ptolemy lib dir : " +
            ptolemyPath;
    }

}
