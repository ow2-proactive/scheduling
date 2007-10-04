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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;


public class SimpleScript extends Script {

    /**  */
    private static final long serialVersionUID = -263217514047549052L;

    /**  */
    private String scriptEngine = null;
    private String script = null;
    private String id = null;

    /** Directly create a script with a string. */
    public SimpleScript(String script, String engineName)
        throws InvalidScriptException {
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
    public SimpleScript(File file) throws InvalidScriptException {
        getEngineName(file.getPath());
        try {
            storeScript(file);
        } catch (IOException e) {
            throw new InvalidScriptException("Unable to read script : ", e);
        }
        this.id = file.getPath();
    }

    /** Create a script from an URL. */
    public SimpleScript(URL url) throws InvalidScriptException {
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
    protected ScriptResult<Object> getResult(Bindings bindings) {
        if (bindings.containsKey("script_result")) {
            return new ScriptResult<Object>(bindings.get("script_result"));
        } else {
            return new ScriptResult<Object>(0);
        }
    }

    /**
     * There is no parameter to give to the verifying script.
     */
    @Override
    protected void prepareBindings(Bindings bindings) {
    }
}
