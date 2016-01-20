/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.forkenvironment;

import java.io.Serializable;
import java.util.Map;


public class ForkEnvironmentScriptResultExtractor implements Serializable {
    public static final String PRE_JAVA_CMD_KEY = "preJavaHomeCmd";

    public String[] getJavaPrefixCommand(Map<String, Object> bindingsMap) {
        // Null checks
        if (bindingsMap == null || bindingsMap.get(PRE_JAVA_CMD_KEY) == null) {
            return new String[0];
        }

        // The java prefix command is split by spaces. That can introduce issues, e.g. with paths
        // which contain spaces. That behavior is address in the user interface (studio), so when
        // this split behavior is changed communicate it in the user interface.
        if (bindingsMap.get(PRE_JAVA_CMD_KEY) instanceof CharSequence) {
            return bindingsMap.get(PRE_JAVA_CMD_KEY)
                    .toString()
                    .split(" ");
        }
        return new String[0];
    }
}
