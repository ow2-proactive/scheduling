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

import io.github.pixee.security.BoundedLineReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.core.properties.PASharedProperties;
import org.ow2.proactive.http.CommonHttpResourceDownloader;
import org.ow2.proactive.utils.BoundedStringWriter;
import org.ow2.proactive.utils.FileUtils;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


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
    public static final int DEFAULT_OUTPUT_MAX_SIZE = 1024 * 1024; // 1 million characters ~ 2 Mb

    /** Loggers */
    public static final Logger logger = Logger.getLogger(Script.class);

    /** Variable name for script arguments */
    public static final String ARGUMENTS_NAME = "args";

    public static final String MD5 = "MD5";

    /** Name of the script engine or file path to script file (extension will be used to lookup) */
    protected String scriptEngineLookupName;

    /** The script to evaluate */
    protected String script;

    /** url used to define this script, if aaplicable **/
    protected URL url;

    /** Id of this script */
    protected String id;

    /** The parameters of the script */
    protected Serializable[] parameters;

    /** Name of the script **/
    private String scriptName;

    private static Boolean lazyFetch = null;

    private String sessionid = null;

    private String owner = null;

    // a cache which stores temporarily script contents, to avoid eager usage of the catalog service
    private static Cache<String, Script.ScriptContentAndEngineName> scriptCache = CacheBuilder.newBuilder()
                                                                                              .expireAfterWrite(Integer.parseInt(System.getProperty("pa.script.cache.expiration",
                                                                                                                                                    "60")),
                                                                                                                TimeUnit.SECONDS)
                                                                                              .build();

    /** ProActive needed constructor */
    public Script() {
    }

    /** Directly create a script with a string.
     * @param script String representing the script's source code
     * @param engineName String representing the execution engine
     * @param parameters script's execution arguments.
     */
    public Script(String script, String engineName, Serializable[] parameters) {
        this.scriptEngineLookupName = engineName;
        this.script = script;
        this.id = script;
        this.parameters = parameters;
        this.scriptName = getDefaultScriptName();
    }

    protected abstract String getDefaultScriptName();

    /** Directly create a script with a string.
     * @param script String representing the script's source code
     * @param engineName String representing the execution engine
     * @param parameters script's execution arguments.
     * @param scriptName name of the script
     */
    public Script(String script, String engineName, Serializable[] parameters, String scriptName) {
        this.scriptEngineLookupName = engineName;
        this.script = script;
        this.id = script;
        this.parameters = parameters;
        this.scriptName = scriptName;
    }

    /** Directly create a script with a string.
     * @param script String representing the script's source code
     * @param engineName String representing the execution engine
     */
    public Script(String script, String engineName) {
        this(script, engineName, (Serializable[]) null);
    }

    /** Directly create a script with a string.
     * @param script String representing the script's source code
     * @param engineName String representing the execution engine
     * @param scriptName name of the script
     */
    public Script(String script, String engineName, String scriptName) {
        this(script, engineName, null, scriptName);
    }

    /** Create a script from a file.
     *
     * @param file a file containing the script's source code.
     * @param parameters script's execution arguments.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(File file, Serializable[] parameters) throws InvalidScriptException {
        this.scriptEngineLookupName = FileUtils.getExtension(file.getPath());

        try {
            script = readFile(file);
        } catch (IOException e) {
            throw new InvalidScriptException("Unable to read script : " + file.getAbsolutePath(), e);
        }
        this.id = file.getPath();
        this.parameters = parameters;
        this.scriptName = file.getName();
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
     * @param fetchImmediately true if the script at the given url must be fetched when the job is parsed by the server, false if the script must be fetch only at execution time.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(URL url, Serializable[] parameters, boolean fetchImmediately) throws InvalidScriptException {
        this(url, null, parameters, fetchImmediately);

    }

    /** Create a script from an URL.
     * @param url representing a script source code.
     * @param engineName String representing the execution engine
     * @param parameters execution arguments.
     * @param fetchImmediately true if the script at the given url must be fetched when the job is parsed by the server, false if the script must be fetch only at execution time.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(URL url, String engineName, Serializable[] parameters, boolean fetchImmediately)
            throws InvalidScriptException {
        this.url = url;
        this.id = url.toExternalForm();
        this.scriptEngineLookupName = engineName;
        this.parameters = parameters;
        this.scriptName = url.getPath();
        if (fetchImmediately) {
            try {
                fetchUrlIfNeeded();
            } catch (IOException e) {
                throw new InvalidScriptException(e);
            }
        }
    }

    /** Create a script from an URL.
     * @param url representing a script source code.
     * @param fetchImmediately true if the script at the given url must be fetched when the job is parsed by the server, false if the script must be fetch only at execution time.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(URL url, boolean fetchImmediately) throws InvalidScriptException {
        this(url, (String) null, fetchImmediately);
    }

    /** Create a script from an URL.
     * @param url representing a script source code.
     * @param engineName String representing the execution engine
     * @param fetchImmediately true if the script at the given url must be fetched when the job is parsed by the server, false if the script must be fetch only at execution time.
     * @throws InvalidScriptException if the creation fails.
     */
    public Script(URL url, String engineName, boolean fetchImmediately) throws InvalidScriptException {
        this(url, engineName, null, fetchImmediately);
    }

    /** Create a script from another script object
     * @param script2 script object source
     */
    public Script(Script<?> script2) {
        this(script2.getScript(), script2.scriptEngineLookupName, script2.getParameters(), script2.getScriptName());
        this.url = script2.url;
        this.id = script2.id;
    }

    /** Create a script from another script object
     * @param script2 script object source
     */
    public Script(Script<?> script2, String scriptName) {
        this(script2.script, script2.scriptEngineLookupName, script2.parameters, scriptName);
        this.url = script2.url;
        this.id = script2.id;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    protected static boolean isLazyFetch() {
        if (lazyFetch == null) {
            try {
                lazyFetch = PASharedProperties.LAZY_FETCH_SCRIPT.getValueAsBoolean();
            } catch (Exception e) {
                logger.warn("Incorrect value of " + PASharedProperties.LAZY_FETCH_SCRIPT.getKey() +
                            ", default value will be used.", e);
                lazyFetch = true;
            }
        }
        return lazyFetch;
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
     * If the script is defined by an url, in fetchImmediately=false mode, retrieve and return the script content from the url.
     *
     * The script internal definition will not be modified, thus allowing fetching again the url prior to execution.
     *
     * Otherwise, return the inline script definition
     *
     * @return the script, or null if the script could not be downloaded from the provided url.
     */
    public String fetchScript() {
        if (script == null && url != null) {
            ScriptContentAndEngineName fetchedInformation = null;
            try {
                fetchedInformation = getScriptContentAndEngineName();
                return fetchedInformation.getScriptContent();
            } catch (Exception e) {
                logger.trace("Could not fetch script at " + url, e);
                return null;
            }
        }
        return script;
    }

    /**
     * If the script is defined by an url, in fetchImmediately=false mode, retrieve and return the script content from the url.
     *
     * The script internal definition will not be modified, thus allowing fetching again the url prior to execution.
     *
     * Otherwise, return the inline script definition
     *
     * @return the script.
     * @throws IOException when the script cannot be accessed
     */
    public String fetchScriptWithExceptionHandling() throws IOException {
        if (script == null && url != null) {
            ScriptContentAndEngineName fetchedInformation = null;
            fetchedInformation = getScriptContentAndEngineName();
            return fetchedInformation.getScriptContent();

        }
        return script;
    }

    /**
     * Get the script name.
     *
     * @return the script name.
     */
    public String getScriptName() {
        return scriptName;
    }

    /**
     * Get the script url, if applicable
     * @return the script url
     */
    public URL getScriptUrl() {
        return url;
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
    public Serializable[] getParameters() {
        return parameters;
    }

    /**
     * Execute the script and return the ScriptResult corresponding.
     * Will use {@link java.lang.System#out} for output.
     *
     * @return a ScriptResult object.
     */
    public ScriptResult<E> execute() {
        return execute(null, System.out, System.err);
    }

    /**
     * Execute the script and return the ScriptResult corresponding.
     * This method can add an additional user bindings if needed.
     *
     * @param aBindings the additional user bindings to add if needed. Can be null or empty.
     * @param outputSink where the script output is printed to.
     * @param errorSink where the script error stream is printed to.
     * @return a ScriptResult object.
     */
    public ScriptResult<E> execute(Map<String, Object> aBindings, PrintStream outputSink, PrintStream errorSink) {
        try {
            fetchUrlIfNeeded();
        } catch (Throwable t) {
            logger.error("Script could not be fetched", t);
            String stack = Throwables.getStackTraceAsString(t);
            if (t.getMessage() != null) {
                stack = t.getMessage() + System.lineSeparator() + stack;
            }
            return new ScriptResult<>(new Exception(stack));
        }
        ScriptEngine engine = createScriptEngine();

        if (engine == null)
            return new ScriptResult<>(new Exception("No Script Engine Found for name or extension " +
                                                    scriptEngineLookupName));

        // SCHEDULING-1532: redirect script output to a buffer (keep the latest DEFAULT_OUTPUT_MAX_SIZE)
        BoundedStringWriter outputBoundedWriter = new BoundedStringWriter(outputSink, DEFAULT_OUTPUT_MAX_SIZE);
        BoundedStringWriter errorBoundedWriter = new BoundedStringWriter(errorSink, DEFAULT_OUTPUT_MAX_SIZE);
        StringBuilder outputBuffer = new StringBuilder();
        outputBoundedWriter.setContentBuffer(outputBuffer);
        errorBoundedWriter.setContentBuffer(outputBuffer);
        engine.getContext().setWriter(new PrintWriter(outputBoundedWriter, true));
        engine.getContext().setErrorWriter(new PrintWriter(errorBoundedWriter, true));
        Reader closedInput = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("closed");
            }

            @Override
            public void close() throws IOException {

            }
        };
        engine.getContext().setReader(closedInput);
        engine.getContext().setAttribute(ScriptEngine.FILENAME, scriptName, ScriptContext.ENGINE_SCOPE);

        try {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            //add additional bindings
            if (aBindings != null) {
                for (Entry<String, Object> e : aBindings.entrySet()) {
                    bindings.put(e.getKey(), e.getValue());
                }
            }
            prepareBindings(bindings);
            Object evalResult = engine.eval(getReader());

            // Add output to the script result
            ScriptResult<E> result = this.getResult(evalResult, bindings);
            captureOutput(engine, outputBuffer, result);

            return result;
        } catch (javax.script.ScriptException e) {
            // wrap exception cause as it might not be serializable
            String stack = Throwables.getStackTraceAsString(e);
            if (e.getMessage() != null) {
                stack = e.getMessage() + System.lineSeparator() + stack;
            }
            ScriptResult<E> result = new ScriptResult<>(new ScriptException(stack));
            captureOutput(engine, outputBuffer, result);
            return result;
        } catch (Throwable t) {
            String stack = Throwables.getStackTraceAsString(t);
            if (t.getMessage() != null) {
                stack = t.getMessage() + System.lineSeparator() + stack;
            }
            ScriptResult<E> result = new ScriptResult<>(new ScriptException(stack));
            captureOutput(engine, outputBuffer, result);
            return result;
        }
    }

    private void captureOutput(ScriptEngine engine, StringBuilder outputBuffer, ScriptResult<E> result) {
        try {
            engine.getContext().getErrorWriter().flush();
            engine.getContext().getWriter().flush();
        } catch (IOException e) {
            logger.warn("Could not flush the end of the script execution output");
        }
        result.setOutput(outputBuffer.toString());
    }

    public void fetchUrlIfNeeded() throws IOException {
        if (script == null && url != null) {
            ScriptContentAndEngineName fetchedInformation = getScriptContentAndEngineName();
            script = fetchedInformation.getScriptContent();
            scriptEngineLookupName = fetchedInformation.getEngineName();
            if (scriptEngineLookupName == null) {
                throw new IllegalStateException("Could not determine script engine for script " + url);
            }
        }
    }

    private Script.ScriptContentAndEngineName getScriptContentAndEngineName() throws IOException {
        Script.ScriptContentAndEngineName fetchedInformation = null;
        String key = null;
        // cache key contains the owner and the script url, as different owners may have different read access rights
        if (owner != null) {
            key = owner + "_" + url.toExternalForm();
        } else {
            key = url.toExternalForm();
        }
        fetchedInformation = scriptCache.getIfPresent(key);
        if (fetchedInformation != null) {
            return fetchedInformation;
        }
        if (url.getProtocol().equals("http") || url.getProtocol().equals("https")) {
            fetchedInformation = fetchScriptUsingHttpDownloader(url);
        } else {
            fetchedInformation = fetchScriptUsingOpenStream(url);
        }
        scriptCache.put(key, fetchedInformation);
        return fetchedInformation;
    }

    private ScriptContentAndEngineName fetchScriptUsingHttpDownloader(URL url) throws IOException {
        CommonHttpResourceDownloader.UrlContent content = CommonHttpResourceDownloader.getInstance()
                                                                                      .getResourceContent(sessionid,
                                                                                                          url.toExternalForm(),
                                                                                                          true);
        String scriptContent = content.getContent();
        String engineName = scriptEngineLookupName;
        if (content.getFileName() != null) {
            if (scriptEngineLookupName == null) {
                engineName = FileUtils.getExtension(content.getFileName());
            }
        }
        return new ScriptContentAndEngineName(scriptContent, engineName);
    }

    /** Create string script from url */
    private ScriptContentAndEngineName fetchScriptUsingOpenStream(URL url) throws IOException {
        String scriptContent = null;
        String engineName = scriptEngineLookupName;
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringBuilder builder = new StringBuilder();
            String tmp;

            while ((tmp = BoundedLineReader.readLine(buf, 5_000_000)) != null) {
                builder.append(tmp).append("\n");
            }

            scriptContent = builder.toString();
        }

        if (scriptEngineLookupName == null) {
            engineName = FileUtils.getExtension(FileUtils.getFileNameWithExtension(url));
        }
        return new ScriptContentAndEngineName(scriptContent, engineName);
    }

    /** String identifying the script.
     * @return a String identifying the script.
     */
    public String getId() {
        return this.id;
    }

    /** The reader used to read the script. */
    protected Reader getReader() {
        return new StringReader(this.script);
    }

    /** The Script Engine used to evaluate the script. */
    protected ScriptEngine createScriptEngine() {

        Map<ScriptEngine, Integer> scriptEngineCandidates;
        final boolean findByName = true;
        scriptEngineCandidates = findScriptEngineCandidates(findByName);

        if (scriptEngineCandidates.isEmpty()) {
            scriptEngineCandidates = findScriptEngineCandidates(!findByName);
        }

        return findBestScriptEngine(scriptEngineCandidates);
    }

    private Map<ScriptEngine, Integer> findScriptEngineCandidates(boolean findByName) {
        Map<ScriptEngine, Integer> matchPositionPerScriptEngineCandidate = new HashMap<>();
        int matchPosition;
        List<String> lookupCriteria;

        for (ScriptEngineFactory factory : new ScriptEngineManager().getEngineFactories()) {
            matchPosition = 0;
            if (findByName) {
                lookupCriteria = factory.getNames();
            } else {
                lookupCriteria = factory.getExtensions();
            }

            for (String criteria : lookupCriteria) {
                if (criteria.equalsIgnoreCase(scriptEngineLookupName)) {
                    matchPositionPerScriptEngineCandidate.put(factory.getScriptEngine(), matchPosition);
                }
                matchPosition++;
            }
        }

        return matchPositionPerScriptEngineCandidate;
    }

    private ScriptEngine findBestScriptEngine(Map<ScriptEngine, Integer> scriptEngineCandidates) {
        int minimumMatchingIndex = Integer.MAX_VALUE;
        ScriptEngine bestScriptEngine = null;

        for (Entry<ScriptEngine, Integer> candidate : scriptEngineCandidates.entrySet()) {
            if (candidate.getValue() < minimumMatchingIndex) {
                minimumMatchingIndex = candidate.getValue();
                bestScriptEngine = candidate.getKey();
            }
        }

        return bestScriptEngine;
    }

    /**
     * Specify the variable awaited from the script execution
     * @param bindings
     **/
    protected abstract void prepareSpecialBindings(Bindings bindings);

    /** Return the variable awaited from the script execution */
    protected abstract ScriptResult<E> getResult(Object evalResult, Bindings bindings);

    /** Set parameters in bindings if any */
    protected final void prepareBindings(Bindings bindings) {
        //add parameters
        if (this.parameters != null) {
            bindings.put(Script.ARGUMENTS_NAME, this.parameters);
        }

        // add special bindings
        this.prepareSpecialBindings(bindings);
    }

    /** Create string script from file */
    public static String readFile(File file) throws IOException {
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            StringBuilder builder = new StringBuilder();
            String tmp;

            while ((tmp = BoundedLineReader.readLine(buf, 5_000_000)) != null) {
                builder.append(tmp).append("\n");
            }

            return builder.toString();
        }
    }

    public String getEngineName() {
        return scriptEngineLookupName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Script))
            return false;
        Script<E> other = (Script<E>) obj;
        if (this.getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!this.getId().equals(other.getId()))
            return false;
        return true;
    }

    /**
     * Get MD5 hash value of the script without parameters
     */
    public static String digest(String script) {
        try {
            return new String(MessageDigest.getInstance(MD5).digest(script.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            logger.error("No algorithm found, digest will use the script content", e);
            return script;
        }
    }

    @Override
    public String toString() {
        return getScriptName();
    }

    public String display() {
        String nl = System.lineSeparator();
        return " { " + nl + "Script '" + getScriptName() + '\'' + nl + "\tscriptEngineLookupName = '" +
               scriptEngineLookupName + '\'' + nl + "\tscript = " + nl + script + nl + "\tid = " + nl + id + nl +
               "\tparameters = " + Arrays.toString(parameters) + nl + '}';
    }

    public void overrideDefaultScriptName(String defaultScriptName) {
        if (getScriptName().equals(getDefaultScriptName())) {
            scriptName = defaultScriptName;
        }
    }

    private static class ScriptContentAndEngineName {
        private final String scriptContent;

        private final String engineName;

        public ScriptContentAndEngineName(String scriptContent, String engineName) {
            this.scriptContent = scriptContent;
            this.engineName = engineName;
        }

        public String getScriptContent() {
            return scriptContent;
        }

        public String getEngineName() {
            return engineName;
        }
    }
}
