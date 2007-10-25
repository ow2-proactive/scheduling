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


/**
 * This script can return the command that have to be executed as Task.
 * @see ExecutableNativeTask
 * @author cdelbe
 * @since 3.9
 */
public class PreScript extends Script<String> {

    /**
     * The variable name which must be set after the evaluation
     * of a verifiying script.
     */
    public static final String RESULT_VARIABLE = "command";

    /**
     * Default value for PreScript return value
     */
    public static final String DEFAULT_COMMAND_VALUE = "NO COMMAND";

    /** ProActive needed constructor */
    public PreScript() {
    }

    /** Directly create a script with a string. */
    public PreScript(String script, String engineName)
        throws InvalidScriptException {
        super(script, engineName);
    }

    /** Create a script from a file. */
    public PreScript(File file, String[] parameters)
        throws InvalidScriptException {
        super(file, parameters);
    }

    /** Create a script from an URL. */
    public PreScript(URL url, String[] parameters)
        throws InvalidScriptException {
        super(url, parameters);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.scheduler.common.scripting.Script#getEngine()
     */
    @Override
    protected ScriptEngine getEngine() {
        return new ScriptEngineManager().getEngineByName(scriptEngine);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.scheduler.common.scripting.Script#getId()
     */
    @Override
    public String getId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.scheduler.common.scripting.Script#getReader()
     */
    @Override
    protected Reader getReader() {
        return new StringReader(script);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.scheduler.common.scripting.Script#getResult(javax.script.Bindings)
     */
    @Override
    protected ScriptResult<String> getResult(Bindings bindings) {
        if (bindings.containsKey(RESULT_VARIABLE)) {
            Object result = bindings.get(RESULT_VARIABLE);
            if (result instanceof String) {
                return new ScriptResult<String>((String) result);
            } else {
                return new ScriptResult<String>(new Exception(
                        "Bad result format : awaited String, found " +
                        result.getClass().getName()));
            }
        } else {
            return new ScriptResult<String>(new Exception("No binding for key " +
                    RESULT_VARIABLE));
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.scheduler.common.scripting.Script#prepareBindings(javax.script.Bindings)
     */
    @Override
    protected void prepareSpecialBindings(Bindings bindings) {
        bindings.put(RESULT_VARIABLE, DEFAULT_COMMAND_VALUE);
    }
}
