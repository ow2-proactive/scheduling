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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.utils.OperatingSystem;


/**
* MatSciFinder
*
* @author The ProActive Team
*/
public abstract class MatSciFinder {

    // the OS where this JVM is running
    private static OperatingSystem os = OperatingSystem.getOperatingSystem();

    private static MatSciFinder instance = null;

    public abstract MatSciEngineConfig findMatSci(String version_pref, HashSet<String> versionsRej,
            String versionMin, String versionMax) throws Exception;

    protected MatSciEngineConfig chooseMatSciConfig(ArrayList<MatSciEngineConfig> configs,
            String version_pref, Set<String> versionsRej, String versionMin, String versionMax) {
        List<MatSciEngineConfig> selected = new ArrayList<MatSciEngineConfig>();
        System.out.println("Choosing config with version_pref=" + version_pref + ", versionRej=" +
            versionsRej + ", versionMin=" + versionMin + ", versionMax=" + versionMax);
        for (MatSciEngineConfig conf : configs) {
            String version = conf.getVersion();
            System.out.println("Version : " + version);
            if (versionsRej != null && !versionsRej.isEmpty() && versionsRej.contains(version)) {
                System.out.println("... rejected");
                continue;
            }
            if (versionMin != null && MatSciEngineConfigBase.infStrictVersion(version, versionMin)) {
                System.out.println("... too low");
                continue;
            }
            if (versionMax != null && MatSciEngineConfigBase.infStrictVersion(versionMax, version)) {
                System.out.println("... too high");
                continue;
            }
            if (version_pref != null && version_pref.equals(version)) {
                System.out.println("... preferred");
                return conf;
            }
            System.out.println("... accepted");
            selected.add(conf);
        }
        if (selected.size() > 0) {
            return selected.get(0);
        }
        return null;
    }

    protected HashSet<String> parseVersionRej(String vrej) {
        HashSet<String> vrejSet = new HashSet<String>();
        if ((vrej != null) && (vrej.length() > 0)) {
            vrej = vrej.trim();
            String[] vRejArr = vrej.split("[ ,;]+");

            for (String rej : vRejArr) {
                if (rej != null) {
                    vrejSet.add(rej);
                }
            }
        }
        return vrejSet;
    }
}
