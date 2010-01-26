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
package org.ow2.proactive.scheduler.ext.scilab.util;

import java.io.Serializable;


public class ScilabConfiguration implements Serializable {

    /**  */
    private static final long serialVersionUID = 200;
    // the Home Dir of Scilab on this machine
    private String scilabHome = null;
    private String scilabLibdir = null;
    private String scilabScidir = null;

    private static final String nl = System.getProperty("line.separator");

    public ScilabConfiguration(String scilabHome, String scilabLibDir, String scilabSciDir) {
        this.scilabHome = scilabHome;
        this.scilabLibdir = scilabLibDir;
        this.scilabScidir = scilabSciDir;
    }

    /**
     * The path where Scilab is installed
     * @return scilab path
     */
    public String getScilabHome() {
        return scilabHome;
    }

    /**
     * The relative path (from Scilab home) where the javasci library can be found
     * @return scilab path
     */
    public String getScilabLibDir() {
        return scilabLibdir;
    }

    /**
     * The absolute path where the scilab SCI dir is supposed to be
     * @return scilab path
     */
    public String getScilabSCIDir() {
        return scilabScidir;
    }

    @Override
    public String toString() {
        return "Scilab Home : " + scilabHome + nl + "Scilab libDir : " + scilabLibdir + nl +
            "Scilab SCIDir : " + scilabScidir;
    }

}
