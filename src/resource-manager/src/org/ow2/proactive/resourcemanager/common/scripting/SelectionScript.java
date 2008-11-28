/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.common.scripting;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A selection Script : return true if the resource tested is correct.
 * 
 * There are 2 type of selection scripts :<br>
 * -static scripts, aimed to test test static property of a resource (node), OS type
 * RAM total space, dynamic libraries present....
 * -dynamic script, aimed to test dynamic properties if a resource, free disk space...
 *  
 *  A static script is executed once on a node and result of script's execution is memorized 
 *  for a next script execution request, so that we avoid a second execution of a static script.
 *  A dynamic script is always executed, because we suppose that script tests dynamic properties
 *  able to change. by default a script is dynamic
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class SelectionScript extends Script<Boolean> {

    /**
     * The variable name which must be set after the evaluation
     * of a verifying script.
     */
    public static final String RESULT_VARIABLE = "selected";

    /** If true, script result is not cached */
    private boolean dynamic = false;

    /**
     * Hash digest of the script
     */
    protected byte[] id;

    /** ProActive needed constructor */
    public SelectionScript() {
    }

    /** Directly create a script with a String.
     * @param script String representing a script code
     * @param engineName String a script execution engine.
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(String script, String engineName) throws InvalidScriptException {
        super(script, engineName);
        buildSelectionScriptId();
    }

    /** Directly create a script with a string.
     * @param script String representing a script code
     * @param engineName String a script execution engine.
     * @param dynamic tell if the script is dynamic or static
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(String script, String engineName, boolean dynamic) throws InvalidScriptException {
        super(script, engineName);
        this.dynamic = dynamic;
        buildSelectionScriptId();
    }

    /** Create a selection script from a file.
     * @param file a file containing the script
     * @param parameters script execution arguments.
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(File file, String[] parameters) throws InvalidScriptException {
        super(file, parameters);
        buildSelectionScriptId();
    }

    /** Create a selection script from a file.
     * @param file a file containing script code
     * @param parameters script execution arguments.
     * @param dynamic tell if script is dynamic or static
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(File file, String[] parameters, boolean dynamic) throws InvalidScriptException {
        super(file, parameters);
        this.dynamic = dynamic;
        buildSelectionScriptId();
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @param parameters script execution argument.
     * @throws InvalidScriptException if the creation fails
     */
    public SelectionScript(URL url, String[] parameters) throws InvalidScriptException {
        super(url, parameters);
        buildSelectionScriptId();
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @param parameters execution arguments
     * @param dynamic true if the script is dynamic
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(URL url, String[] parameters, boolean dynamic) throws InvalidScriptException {
        super(url, parameters);
        this.dynamic = dynamic;
        buildSelectionScriptId();
    }

    /** Create a selection script from another selection script
     * @param script selection script source
     * @param dynamic true if the script is dynamic
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(Script<?> script, boolean dynamic) throws InvalidScriptException {
        super(script);
        this.dynamic = dynamic;
        buildSelectionScriptId();
    }

    /**
     * Build script ID. ID is a way to compare a script with another.
     * ID is a String made of script_code+script_parameters+type
     *   
     */
    public void buildSelectionScriptId() {
        //get code of script
        String stringId = this.script;

        //concatenate the script type (dynamic or static)
        stringId += this.dynamic;

        //concatenate parameters if any
        if (this.parameters != null) {
            for (String param : this.parameters) {
                stringId += param;
            }
        }

        try {
            this.id = MessageDigest.getInstance("SHA-1").digest(stringId.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            this.id = stringId.getBytes();
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.common.scripting.Script#getId()
     */
    @Override
    public String getId() {
        return this.id.toString();
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
     * SelectionScript must give its result in the 'result_script' variable.
     *
     * @see org.ow2.proactive.scheduler.common.scripting.Script#getResult(javax.script.Bindings)
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
                return new ScriptResult<Boolean>(!(((String) result).equals("false") || ((String) result)
                        .equals("False")));
            } else {
                return new ScriptResult<Boolean>(new Exception(
                    "Bad result format : awaited Boolean (or Integer when not existing), found " +
                        result.getClass().getName()));
            }
        } else {
            return new ScriptResult<Boolean>(new Exception("No binding for key : " + RESULT_VARIABLE +
                "\na Selection script must define a variable named '" + RESULT_VARIABLE +
                "' set to true or false"));
        }
    }

    /** Say if the script is static or dynamic
     * @return true if the script is dynamic, false otherwise
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * There is no parameter to give to the selection script.
     */
    @Override
    protected void prepareSpecialBindings(Bindings bindings) {
    }

    /**
     * @see org.ow2.proactive.resourcemanager.common.scripting.Script#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof SelectionScript) {
            return compareByteArray(this.id, ((SelectionScript) o).id);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new String(id).hashCode();
    }

    /** Compare two arrays of bytes
     * @param array1 first array to compare
     * @param array2 second array to compare
     * @return true is the two contains the same bytes values, false otherwise
     */
    public boolean compareByteArray(byte[] array1, byte[] array2) {
        if (array1.length != array2.length)
            return false;
        else {
            for (int i = 0; i < array1.length; i++) {
                if (array1[i] != array2[i])
                    return false;
            }
            return true;
        }
    }
}
