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
package org.ow2.proactive.resourcemanager.common.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A simple script to evaluate using java 6 scripting API.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 *
 *
 * @param <E> Template class's type of the result.
 */
@PublicAPI
public abstract class Script<E> implements Serializable {

    /** Variable name for script arguments */
    public static final String ARGUMENTS_NAME = "args";

    /** Name of the script engine */
    protected String scriptEngine = null;

    /** The script to evaluate */
    protected String script = null;

    /** Id of this script */
    protected String id = null;

    /** The parameters of the script */
    protected String[] parameters = null;

    /** ProActive needed constructor */
    public Script() {
    }

    /** Directly create a script with a string.
     * @param script String representing the script's source code
     * @param engineName String representing the execution engine
     * @param parameters script's execution arguments.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(String script, String engineName, String[] parameters) throws InvalidScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(engineName);

        if (engine == null) {
            throw new InvalidScriptException("The engine '" + engineName + "' is not valid");
        } else {
            scriptEngine = engine.getFactory().getNames().get(0);
        }

        this.script = script;
        this.id = script;
        this.parameters = parameters;
    }

    /** Directly create a script with a string.
     * @param script String representing the script's source code
     * @param engineName String representing the execution engine
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(String script, String engineName) throws InvalidScriptException {
        this(script, engineName, null);
    }

    /** Create a script from a file.
     * @param file a file containing the script's source code.
     * @param parameters script's execution arguments.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(File file, String[] parameters) throws InvalidScriptException {
        getEngineName(file.getPath());

        try {
            storeScript(file);
        } catch (IOException e) {
            throw new InvalidScriptException("Unable to read script : ", e);
        }

        this.id = file.getPath();
        this.parameters = parameters;
    }

    /** Create a script from a file.
     * @param file a file containing a script's source code.
     * @throws InvalidScriptException
     */
    public Script(File file) throws InvalidScriptException {
        this(file, null);
    }

    /** Create a script from an URL.
     * @param url representing a script source code.
     * @param parameters execution arguments.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(URL url, String[] parameters) throws InvalidScriptException {
        getEngineName(url.getFile());

        try {
            storeScript(url);
        } catch (IOException e) {
            throw new InvalidScriptException("Unable to read script : ", e);
        }

        this.id = url.toExternalForm();
        this.parameters = parameters;
    }

    /** Create a script from an URL.
     * @param url representing a script source code.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(URL url) throws InvalidScriptException {
        this(url, null);
    }

    /** Create a script from another script object
     * @param script2 script object source
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(Script<?> script2) throws InvalidScriptException {
        this(script2.script, script2.scriptEngine, script2.parameters);
    }

    /**
     * Execute the script and return the ScriptResult corresponding.
     * @return a ScriptResult object.
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
            e.printStackTrace();
            return new ScriptResult<E>(new Exception("An exception occured while executing the script ", e));

        }
    }

    /** String identifying the script.
     * @return a String identifying the script.
     */
    public abstract String getId();

    /** The reader used to read the script. */
    protected abstract Reader getReader();

    /** The Script Engine used to evaluate the script. */
    protected abstract ScriptEngine getEngine();

    /** Specify the variable awaited from the script execution */
    protected abstract void prepareSpecialBindings(Bindings bindings);

    /** Return the variable awaited from the script execution */
    protected abstract ScriptResult<E> getResult(Bindings bindings);

    /** Set parameters in bindings if any */
    protected final void prepareBindings(Bindings bindings) {
        //add parameters
        if (this.parameters != null) {
            bindings.put(Script.ARGUMENTS_NAME, this.parameters);
        }

        // add special bindings
        this.prepareSpecialBindings(bindings);
    }

    /** Create string script from url */
    protected void storeScript(URL url) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder builder = new StringBuilder();
        String tmp = null;

        while ((tmp = buf.readLine()) != null) {
            builder.append(tmp + "\n");
        }

        script = builder.toString();
    }

    /** Create string script from file */
    protected void storeScript(File file) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuilder builder = new StringBuilder();
        String tmp = null;

        while ((tmp = buf.readLine()) != null) {
            builder.append(tmp + "\n");
        }

        script = builder.toString();
    }

    /** Set the scriptEngine from filepath */
    protected void getEngineName(String filepath) throws InvalidScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();

        for (ScriptEngineFactory sef : manager.getEngineFactories())
            for (String ext : sef.getExtensions())
                if (filepath.endsWith(ext)) {
                    scriptEngine = sef.getNames().get(0);

                    break;
                }

        if (scriptEngine == null) {
            throw new InvalidScriptException("No script engine corresponding");
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
