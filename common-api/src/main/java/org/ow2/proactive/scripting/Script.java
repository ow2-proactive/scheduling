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
package org.ow2.proactive.scripting;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.utils.BoundedStringWriter;
import org.ow2.proactive.utils.FileUtils;
import org.apache.log4j.Logger;


/**
 * A simple script to evaluate using java 6 scripting API.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 *
 * @param <E> Template class's type of the result.
 */
@PublicAPI
public abstract class Script<E> implements Serializable {

    // default output size in chars
    public static final int DEFAULT_OUTPUT_MAX_SIZE = 1 * 1024 * 1024 * 1024; // 1 million characters ~ 2 Mb

    /** Loggers */
    public static final Logger logger = Logger.getLogger(Script.class);

    /** Variable name for script arguments */
    public static final String ARGUMENTS_NAME = "args";

    /** Name of the script engine or file path to script file (extension will be used to lookup) */
    protected String scriptEngineLookup;

    /** The script to evaluate */
    protected String script;

    /** Id of this script */
    protected String id;

    /** The parameters of the script */
    protected String[] parameters;

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
        this.scriptEngineLookup = engineName;
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
     *
     * @param file a file containing the script's source code.
     * @param parameters script's execution arguments.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(File file, String[] parameters) throws InvalidScriptException {
        this.scriptEngineLookup = FileUtils.getExtension(file.getPath());

        try {
            script = readFile(file);
        } catch (IOException e) {
            throw new InvalidScriptException("Unable to read script : " + file.getAbsolutePath(), e);
        }
        this.id = file.getPath();
        this.parameters = parameters;
    }

    /** Create a script from a file.
     * @param file a file containing a script's source code.
     * @throws InvalidScriptException if Constructor fails.
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
        this.scriptEngineLookup = FileUtils.getExtension(url.getFile());

        try {
            storeScript(url);
        } catch (IOException e) {
            throw new InvalidScriptException("Unable to read script : " + url.getPath(), e);
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
        this(script2.script, script2.scriptEngineLookup, script2.parameters);
    }

    /**
     * Get the script.
     *
     * @return the script.
     */
    public String getScript() {
        return script;
    }

    /**
     * Set the script content, ie the executed code
     * 
     * @param script the new script content
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Get the parameters.
     *
     * @return the parameters.
     */
    public String[] getParameters() {
        return parameters;
    }

    /**
     * Add a binding to the script that will be handle by this handler.
     *
     * @param name the name of the variable
     * @param value the value of the variable
     */
    public void addBinding(String name, Object value) {

    }

    /**
     * Execute the script and return the ScriptResult corresponding.
     *
     * @return a ScriptResult object.
     */
    public ScriptResult<E> execute() {
        return execute(null);
    }

    /**
     * Execute the script and return the ScriptResult corresponding.
     * This method can add an additional user bindings if needed.
     *
     * @param aBindings the additional user bindings to add if needed. Can be null or empty.
     * @return a ScriptResult object.
     */
    public ScriptResult<E> execute(Map<String, Object> aBindings) {
        ScriptEngine engine = createScriptEngine();

        if (engine == null) {
            return new ScriptResult<E>(new Exception("No Script Engine Found for name or extension " +
                scriptEngineLookup));
        }

        // SCHEDULING-1532: redirect script output to a buffer (keep the latest DEFAULT_OUTPUT_MAX_SIZE)
        // the output is still printed to stdout
        BoundedStringWriter sw = new BoundedStringWriter(DEFAULT_OUTPUT_MAX_SIZE);
        PrintWriter pw = new PrintWriter(sw);
        engine.getContext().setWriter(pw);

        try {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            //add additional bindings
            if (aBindings != null) {
                for (Entry<String, Object> e : aBindings.entrySet()) {
                    bindings.put(e.getKey(), e.getValue());
                }
            }
            prepareBindings(bindings);
            engine.eval(getReader());

            // Add output to the script result
            ScriptResult<E> result = this.getResult(bindings);
            result.setOutput(sw.toString());

            return result;
        } catch (Throwable e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            e.printStackTrace(ps);
            ps.flush();
            String stack = baos.toString();
            if (e.getMessage() != null) {
                stack = e.getMessage() + System.getProperty("line.separator") + stack;
            }
            logger.error(e.getMessage(), e);
            return new ScriptResult<E>(new Exception(stack));
        }
    }

    /** String identifying the script.
     * @return a String identifying the script.
     */
    public abstract String getId();

    /** The reader used to read the script. */
    protected abstract Reader getReader();

    /** The Script Engine used to evaluate the script. */
    protected ScriptEngine createScriptEngine() {
        for (ScriptEngineFactory factory : new ScriptEngineManager().getEngineFactories()) {
            for (String name : factory.getNames()) {
                if (name.equalsIgnoreCase(scriptEngineLookup)) {
                    return factory.getScriptEngine();
                }
            }
            for (String ext : factory.getExtensions()) {
                String scriptEngineLookupLowercase = scriptEngineLookup.toLowerCase();
                if (scriptEngineLookupLowercase.endsWith(ext.toLowerCase())) {
                    return factory.getScriptEngine();
                }
            }
        }
        return null;
    }

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
        String tmp;

        while ((tmp = buf.readLine()) != null) {
            builder.append(tmp).append("\n");
        }

        script = builder.toString();
    }

    /** Create string script from file */
    public static String readFile(File file) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuilder builder = new StringBuilder();
        String tmp;

        while ((tmp = buf.readLine()) != null) {
            builder.append(tmp).append("\n");
        }

        return builder.toString();
    }

    public String getEngineName() {
        return scriptEngineLookup;
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