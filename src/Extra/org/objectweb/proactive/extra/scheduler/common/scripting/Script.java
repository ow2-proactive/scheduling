/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.common.scripting;

import java.io.Reader;
import java.io.Serializable;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;


/**
 * A simple script to evaluate using java 6 scripting API.
 *
 * @author ProActive Team
 * @version 1.0, Jun 4, 2007
 * @since ProActive 3.2
 */
public abstract class Script<E> implements Serializable {

    /**
     * Execute the script and return the ScriptResult corresponding.
     *
     * @return
     */
    public ScriptResult<E> execute() {
        ScriptEngine engine = getEngine();
        if (engine == null) {
            return new ScriptResult<E>(new Exception("No Script Engine Found"));
        }
        try {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            prepareBindings(bindings);
            engine.eval(getReader());
            return getResult(bindings);
        } catch (Throwable e) {
            return new ScriptResult<E>(new Exception(
                    "An exception occured while executing the script ", e));
        }
    }

    /** String identifying the script **/
    public abstract String getId();

    /** The reader used to read the script. */
    protected abstract Reader getReader();

    /** The Script Engine used to evaluate the script. */
    protected abstract ScriptEngine getEngine();

    protected abstract void prepareBindings(Bindings bindings);

    protected abstract ScriptResult<E> getResult(Bindings bindings);

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (o instanceof Script) {
            Script<E> new_name = (Script<E>) o;
            return this.getId().equals(new_name.getId());
        }
        return false;
    }
}
