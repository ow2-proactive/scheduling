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

import java.io.*;
import java.net.URL;

import javax.script.*;


/**
 * A Verifying Script : return true if the resource tested is correct.
 *
 *
 * @author ProActive Team
 * @version 1.0, Jun 8, 2007
 * @since ProActive 3.2
 */
public class VerifyingScript extends Script<Boolean> {

    /**
     * The variable name which must be set after the evaluation
     * of a verifiying script.
     */
    public static final String RESULT_VARIABLE = "script_result";

    /**  */
    private static final long serialVersionUID = 120693403230482727L;
    private boolean dynamic;
    private String scriptEngine = null;
    private String script = null;
    private String id = null;

    /** ProActive needed constructor */
    public VerifyingScript() {
    }

    public VerifyingScript(String script, String engineName)
        throws InvalidScriptException {
        this(script, engineName, true);
    }

    /** Directly create a script with a string. */
    public VerifyingScript(String script, String engineName, boolean dynamic)
        throws InvalidScriptException {
        this.dynamic = dynamic;
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(engineName);
        if (engine == null) {
            throw new InvalidScriptException("The engine '" + engineName +
                "' is not valid");
        } else {
            scriptEngine = engine.getFactory().getNames().get(0);
        }
        this.script = script;
        this.id = script;
    }

    /** Create a script from a file. */
    public VerifyingScript(File file) throws InvalidScriptException {
        this(file, true);
    }

    /** Create a script from a file. */
    public VerifyingScript(File file, boolean dynamic)
        throws InvalidScriptException {
        this.dynamic = dynamic;
        getEngineName(file.getPath());
        try {
            storeScript(file);
        } catch (IOException e) {
            throw new InvalidScriptException("Unable to read script : ", e);
        }
        this.id = file.getPath();
    }

    /** Create a script from an URL. */
    public VerifyingScript(URL url) throws InvalidScriptException {
        this(url, true);
    }

    /** Create a script from an URL. */
    public VerifyingScript(URL url, boolean dynamic)
        throws InvalidScriptException {
        this.dynamic = dynamic;
        getEngineName(url.getFile());
        try {
            storeScript(url);
        } catch (IOException e) {
            throw new InvalidScriptException("Unable to read script : ", e);
        }
        this.id = url.toExternalForm();
    }

    private void storeScript(URL url) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(
                    url.openStream()));
        StringBuilder builder = new StringBuilder();
        String tmp = null;
        while ((tmp = buf.readLine()) != null) {
            builder.append(tmp + "\n");
        }
        script = builder.toString();
    }

    private void storeScript(File file) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)));
        StringBuilder builder = new StringBuilder();
        String tmp = null;
        while ((tmp = buf.readLine()) != null) {
            builder.append(tmp + "\n");
        }
        script = builder.toString();
    }

    private void getEngineName(String filepath) throws InvalidScriptException {
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
     * VerifyingScript must give its result in the 'result_script' variable.
     *
     * @see org.objectweb.proactive.extra.scheduler.common.scripting.Script#getResult(javax.script.Bindings)
     */
    @Override
    protected ScriptResult<Boolean> getResult(Bindings bindings) {
        if (bindings.containsKey(RESULT_VARIABLE)) {
            Object result = bindings.get(RESULT_VARIABLE);
            if (result instanceof Boolean) {
                return new ScriptResult<Boolean>((Boolean) result);
            } else if (result instanceof Integer) {
                return new ScriptResult<Boolean>((Integer) result != 0);
            } else if (result instanceof String) {
                return new ScriptResult<Boolean>(!(((String) result).equals(
                        "false") || ((String) result).equals("False")));
            } else {
                return new ScriptResult<Boolean>(new Exception(
                        "Bad result format : awaited Boolean (or Integer when not existing), found " +
                        result.getClass().getName()));
            }
        } else {
            return new ScriptResult<Boolean>(new Exception(
                    "No binding for key " + RESULT_VARIABLE));
        }
    }

    /** Say if the script is static or dynamic **/
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * There is no parameter to give to the verifying script.
     */
    @Override
    protected void prepareBindings(Bindings bindings) {
        bindings.put("script_result", new Boolean(true));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof VerifyingScript) {
            return ((VerifyingScript) o).getId().equals(getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
