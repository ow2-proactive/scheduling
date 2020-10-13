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
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.script.Bindings;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.core.properties.PASharedProperties;


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
    protected byte[] hash = null;

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
    }

    /** Create a selection script from a file.
     * @param file a file containing the script
     * @param parameters script execution arguments.
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(File file, Serializable[] parameters) throws InvalidScriptException {
        super(file, parameters);
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
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @throws InvalidScriptException if the creation fails
     */
    public SelectionScript(URL url) throws InvalidScriptException {
        this(url, true);
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @throws InvalidScriptException if the creation fails
     */
    public SelectionScript(URL url, boolean dynamic) throws InvalidScriptException {
        this(url, (String) null, dynamic);
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @param engineName script's engine execution name.
     * @throws InvalidScriptException if the creation fails
     */
    public SelectionScript(URL url, String engineName) throws InvalidScriptException {
        this(url, engineName, true);
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @param engineName script's engine execution name.
     * @param dynamic true if the script is dynamic
     * @throws InvalidScriptException if the creation fails
     */
    public SelectionScript(URL url, String engineName, boolean dynamic) throws InvalidScriptException {
        this(url, engineName, null, dynamic);
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @param parameters script execution argument.
     * @throws InvalidScriptException if the creation fails
     */
    public SelectionScript(URL url, Serializable[] parameters) throws InvalidScriptException {
        this(url, parameters, true);
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @param parameters execution arguments
     * @param dynamic true if the script is dynamic
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(URL url, Serializable[] parameters, boolean dynamic) throws InvalidScriptException {
        this(url, null, parameters, dynamic);
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @param engineName script's engine execution name.
     * @param parameters script execution argument.
     * @throws InvalidScriptException if the creation fails
     */
    public SelectionScript(URL url, String engineName, Serializable[] parameters) throws InvalidScriptException {
        this(url, engineName, parameters, true);
    }

    /** Create a selection script from an URL.
     * @param url an URL representing a script code
     * @param engineName script's engine execution name.
     * @param parameters execution arguments
     * @param dynamic true if the script is dynamic
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(URL url, String engineName, Serializable[] parameters, boolean dynamic)
            throws InvalidScriptException {
        super(url, engineName, parameters, !isLazyFetch());
        this.dynamic = dynamic;
    }

    /** Create a selection script from another selection script
     * @param script selection script source
     * @param dynamic true if the script is dynamic
     * @throws InvalidScriptException if the creation fails.
     */
    public SelectionScript(Script<?> script, boolean dynamic) throws InvalidScriptException {
        super(script);
        this.dynamic = dynamic;
    }

    public static SelectionScript resolvedSelectionScript(SelectionScript originalScript) {
        if (originalScript.script == null && originalScript.url != null) {
            try {
                SelectionScript newScript = new SelectionScript(originalScript.url,
                                                                originalScript.parameters,
                                                                originalScript.dynamic);
                newScript.fetchUrlIfNeeded();
                return newScript;
            } catch (InvalidScriptException | IOException e) {
                logger.debug("Could not fetch the selection script", e);
                return originalScript;
            }
        } else {
            return originalScript;
        }
    }

    /**
     * Build script ID. ID is a way to compare a script with another.
     * ID is a String made of script_code+script_parameters+type
     *
     */
    private void buildSelectionScriptHash() {
        String stringId;
        try {
            //get code of script
            stringId = fetchScriptWithExceptionHandling();
        } catch (IOException e) {
            logger.debug("Error when loading the script content", e);
            if (url != null) {
                stringId = url.toExternalForm();
            } else {
                // this should never happen
                throw new IllegalStateException("A script should always have a script content or url");
            }
        }

        //concatenate the script type (dynamic or static)
        stringId += this.dynamic;

        //concatenate parameters if any
        if (this.parameters != null) {
            for (Serializable param : this.parameters) {
                stringId += param;
            }
        }

        try {
            this.hash = MessageDigest.getInstance("SHA-1").digest(stringId.getBytes());
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
            this.hash = stringId.getBytes();
        }
    }

    private byte[] getHash() {
        if (hash == null) {
            buildSelectionScriptHash();
        }
        return hash;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof SelectionScript) {
            return compareByteArray(getHash(), ((SelectionScript) obj).getHash());
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new String(getHash()).hashCode();
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
     * Get MD5 hash value
     * 
     * @return MD5 hash value
     * @throws NoSuchAlgorithmException
     */
    public byte[] digest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(getHash());
    }

    @Override
    public String toString() {
        return "" + new String(getHash()) + "\n" + (script != null ? script : url);
    }
}
