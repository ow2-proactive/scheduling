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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Composition of selection script and its execution result.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public class ScriptWithResult {
    /** selection script */
    private SelectionScript script;
    /** script result */
    private ScriptResult<Boolean> scriptResult;

    /**
     * Create a new instance of ScriptWithResult
     * 
     * @param script a selection script
     * @param scriptResult a script result
     */
    public ScriptWithResult(SelectionScript script, ScriptResult<Boolean> scriptResult) {
        this.script = script;
        this.scriptResult = scriptResult;
    }

    /**
     * Get the selection script
     * 
     * @return the selection script
     */
    public SelectionScript getScript() {
        return script;
    }

    /**
     * Get the script result
     * 
     * @return the script result
     */
    public ScriptResult<Boolean> getScriptResult() {
        return scriptResult;
    }
}
