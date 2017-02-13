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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.script.Bindings;

import org.apache.log4j.Logger;
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
 *  able to change. By default a script is dynamic.
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class SelectionScript extends Script<Boolean> {
    /** Loggers */
    public static final Logger logger = Logger.getLogger(SelectionScript.class);

    /**
     * The variable name which must be set after the evaluation
     * of a verifying script.
     */
    public static final String RESULT_VARIABLE = "selected";

    /** If true, script result is not cached */
    private boolean dynamic = true;

    /**
     * Hash digest of the script
     */
    protected byte[] id_;

    /** ProActive needed constructor */
    public SelectionScript() {
    }

    @Override
    protected String getDefaultScriptName() {
        return "SelectionScript";
    }

    /** Directly create a script with a String.
     * @param script String representing a script code
     * @param engineName String a script execution engine.
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(String script, String engineName) throws InvalidScriptException {
        super(script, engineName, "SelectionScript");
        buildSelectionScriptId();
    }

    /** Directly create a script with a string.
     * @param script String representing a script code
     * @param engineName String a script execution engine.
     * @param dynamic tell if the script is dynamic or static
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(String script, String engineName, boolean dynamic) throws InvalidScriptException {
        super(script, engineName, "SelectionScript");
        this.dynamic = dynamic;
        buildSelectionScriptId();
    }

    /** Directly create a script with a string.
     * @param script String representing a script code
     * @param parameters script execution arguments.
     * @param engineName String a script execution engine.
     * @param dynamic tell if the script is dynamic or static
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(String script, String engineName, Serializable[] parameters, boolean dynamic)
            throws InvalidScriptException {
        super(script, engineName, parameters, "SelectionScript");
        this.dynamic = dynamic;
        buildSelectionScriptId();
    }

    /** Create a selection script from a file.
     * @param file a file containing the script
     * @param parameters script execution arguments.
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(File file, Serializable[] parameters) throws InvalidScriptException {
        super(file, parameters);
        buildSelectionScriptId();
    }

    /** Create a selection script from a file.
     * @param file a file containing script code
     * @param parameters script execution arguments.
     * @param dynamic tell if script is dynamic or static
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(File file, Serializable[] parameters, boolean dynamic) throws InvalidScriptException {
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
            for (Serializable param : this.parameters) {
                stringId += param;
            }
        }

        try {
            this.id_ = MessageDigest.getInstance("SHA-1").digest(stringId.getBytes());
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
            this.id_ = stringId.getBytes();
        }
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
     * SelectionScript must give its result in the 'result_script' variable.
     *
     * @see org.ow2.proactive.scripting.Script#getResult(Object, Bindings)
     */
    @Override
    protected ScriptResult<Boolean> getResult(Object evalResult, Bindings bindings) {
        if (bindings.containsKey(RESULT_VARIABLE)) {
            Object result = bindings.get(RESULT_VARIABLE);

            if (result instanceof Boolean) {
                return new ScriptResult<>((Boolean) result);
            } else if (result instanceof Integer) {
                return new ScriptResult<>((Integer) result != 0);
            } else if (result instanceof CharSequence) {
                return new ScriptResult<>(!(result.equals("false") || result.equals("False")));
            } else {
                return new ScriptResult<>(new Exception("Bad result format : awaited Boolean (or Integer when not existing), found " +
                                                        result.getClass().getName()));
            }
        } else {
            String msg = "No binding for key : " + RESULT_VARIABLE +
                         "\na Selection script must define a variable named '" + RESULT_VARIABLE +
                         "' set to true or false";
            logger.error(msg);
            return new ScriptResult<>(new Exception(msg));
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
     * @see org.ow2.proactive.scripting.Script#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof SelectionScript) {
            return compareByteArray(this.id_, ((SelectionScript) o).id_);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new String(id_).hashCode();
    }

    /**
     * Get MD5 hash value
     * 
     * @return MD5 hash value
     * @throws NoSuchAlgorithmException
     */
    public byte[] digest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(id_);
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

    /**
     * Util method to get an hashCode of a list of selection script. Can be used to compare two lists of selectionScript
     * and see if it is the same.
     * If the list is empty or null, then the return value will be 0, meaning no selection scripts are provided.
     *
     * @param selScriptsList a list of selection scripts
     * @return 0 if the list is null or empty, otherwise, an hashCode of the list content
     */
    public static int hashCodeFromList(List<SelectionScript> selScriptsList) {
        if (selScriptsList == null || selScriptsList.size() == 0) {
            return 0;
        }
        int toReturn = 0;
        for (SelectionScript ss : selScriptsList) {
            toReturn += ss.hashCode();
        }
        return toReturn;
    }

    /**
     * String representation
     */
    public String toString() {
        return hashCode() + "\n" + script;
    }

}
