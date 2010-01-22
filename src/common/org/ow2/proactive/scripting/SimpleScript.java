/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A simple script implementation
 *
 * @author ProActive team
 *
 */
@PublicAPI
@Entity
@Table(name = "SIMPLE_SCRIPT")
@AccessType("field")
@Proxy(lazy = false)
public class SimpleScript extends Script {
    /**  */
    private static final long serialVersionUID = 200;
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hId;

    /** Hibernate default constructor*/
    @SuppressWarnings("unused")
    private SimpleScript() {
    }

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

    /**
     * @see org.ow2.proactive.scripting.Script#getId()
     */
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
     * @see org.ow2.proactive.scheduler.common.scripting.Script#getResult(javax.script.Bindings)
     */
    @Override
    protected ScriptResult<Object> getResult(Bindings bindings) {
        // no significant result can be returned
        return new ScriptResult<Object>();
    }

    /**
     * There is no parameter to give to the selection script.
     */
    @Override
    protected void prepareSpecialBindings(Bindings bindings) {
    }
}
