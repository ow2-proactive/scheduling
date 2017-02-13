/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scripting;

import java.io.File;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;

import javax.script.Bindings;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A simple script implementation
 *
 * @author ProActive team
 *
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleScript extends Script<Object> {

    /** Directly create a script with a string.
     * @param script a String containing script code
     * @param engineName script's engine execution name.
     * @throws InvalidScriptException if the creation fails.
     */
    public SimpleScript(String script, String engineName) throws InvalidScriptException {
        super(script, engineName);
    }

    /** Create a script from a file.
     * @param file a file containing script code.
     * @param parameters execution parameters
     * @throws InvalidScriptException if creation fails.
     */
    public SimpleScript(File file, String[] parameters) throws InvalidScriptException {
        super(file, parameters);
    }

    /** Create a script from an URL.
     * @param url an URL containing script code.
     * @param parameters execution parameters
     * @throws InvalidScriptException if the creation fails.
     */
    public SimpleScript(URL url, String[] parameters) throws InvalidScriptException {
        super(url, parameters);
    }

    /** Directly create a script with a string.
     * @param script a String containing script code
     * @param engine script's engine execution name.
     * @param parameters execution parameters
     * @throws InvalidScriptException if the creation fails.
     */
    public SimpleScript(String script, String engine, Serializable[] parameters) throws InvalidScriptException {
        super(script, engine, parameters);
    }

    @Override
    protected String getDefaultScriptName() {
        return "SimpleScript";
    }

    /**
     * Copy constructor
     * 
     * @param original script to copy
     * @throws InvalidScriptException 
     */
    public SimpleScript(Script<?> original) throws InvalidScriptException {
        super(original);
    }

    /**
     * @see org.ow2.proactive.scripting.Script#getId()
     */
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    protected Reader getReader() {
        return new StringReader(script);
    }

    /**
     * @see org.ow2.proactive.scripting.Script#getResult(Object, Bindings)
     */
    @Override
    protected ScriptResult<Object> getResult(Object evalResult, Bindings bindings) {
        // no significant result can be returned
        return new ScriptResult<>();
    }

    /**
     * There is no parameter to give to the selection script.
     */
    @Override
    protected void prepareSpecialBindings(Bindings bindings) {
    }
}
