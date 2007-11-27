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

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


public class SimpleScript extends Script {

    /**  */
    private static final long serialVersionUID = -263217514047549052L;

    /**  */
    private String id = null;

    /** Directly create a script with a string. */
    public SimpleScript(String script, String engineName)
        throws InvalidScriptException {
        super(script, engineName);
    }

    /** Create a script from a file. */
    public SimpleScript(File file, String[] parameters)
        throws InvalidScriptException {
        super(file, parameters);
    }

    /** Create a script from an URL. */
    public SimpleScript(URL url, String[] parameters)
        throws InvalidScriptException {
        super(url, parameters);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    protected ScriptEngine getEngine() {
        return new ScriptEngineManager().getEngineByName(scriptEngine);
    }

    @Override
    protected Reader getReader() {
        return new StringReader(script);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scripting.Script#getResult(javax.script.Bindings)
     */
    @Override
    protected ScriptResult<Object> getResult(Bindings bindings) {
        return null;
    }

    /**
     * There is no parameter to give to the selection script.
     */
    @Override
    protected void prepareSpecialBindings(Bindings bindings) {
    }
}
