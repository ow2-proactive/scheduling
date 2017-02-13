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
package org.ow2.proactive.scheduler.task.java;

import static com.google.common.base.Throwables.getStackTraceAsString;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.executable.internal.JavaStandaloneExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.task.exceptions.TaskException;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scripting.Script;


public class JavaClassScriptEngine extends AbstractScriptEngine {

    @Override
    public Object eval(String userExecutableClassName, ScriptContext context) throws ScriptException {

        try {
            JavaExecutable javaExecutable = getExecutable(userExecutableClassName);

            JavaStandaloneExecutableInitializer execInitializer = new JavaStandaloneExecutableInitializer();
            PrintStream output = new PrintStream(new WriterOutputStream(context.getWriter()), true);
            execInitializer.setOutputSink(output);
            PrintStream error = new PrintStream(new WriterOutputStream(context.getErrorWriter()), true);
            execInitializer.setErrorSink(error);

            Map<String, byte[]> propagatedVariables = null;
            if (context.getAttribute(SchedulerConstants.VARIABLES_BINDING_NAME) != null) {
                propagatedVariables = SerializationUtil.serializeVariableMap(((VariablesMap) context.getAttribute(SchedulerConstants.VARIABLES_BINDING_NAME)).getPropagatedVariables());
                execInitializer.setPropagatedVariables(propagatedVariables);
            } else {
                execInitializer.setPropagatedVariables(Collections.<String, byte[]> emptyMap());
            }

            if (context.getAttribute(Script.ARGUMENTS_NAME) != null) {
                execInitializer.setSerializedArguments((Map<String, byte[]>) ((Serializable[]) context.getAttribute(Script.ARGUMENTS_NAME))[0]);
            } else {
                execInitializer.setSerializedArguments(Collections.<String, byte[]> emptyMap());
            }

            if (context.getAttribute(SchedulerConstants.CREDENTIALS_VARIABLE) != null) {
                execInitializer.setThirdPartyCredentials((Map<String, String>) context.getAttribute(SchedulerConstants.CREDENTIALS_VARIABLE));
            } else {
                execInitializer.setThirdPartyCredentials(Collections.<String, String> emptyMap());
            }

            if (context.getAttribute(SchedulerConstants.MULTI_NODE_TASK_NODESURL_BINDING_NAME) != null) {
                List<String> nodesURLs = (List<String>) context.getAttribute(SchedulerConstants.MULTI_NODE_TASK_NODESURL_BINDING_NAME);
                execInitializer.setNodesURL(nodesURLs);
            } else {
                execInitializer.setNodesURL(Collections.<String> emptyList());
            }

            javaExecutable.internalInit(execInitializer, context);

            Serializable execute = javaExecutable.execute((TaskResult[]) context.getAttribute(SchedulerConstants.RESULTS_VARIABLE));

            if (propagatedVariables != null) {
                ((Map<String, Serializable>) context.getAttribute(SchedulerConstants.VARIABLES_BINDING_NAME)).putAll(javaExecutable.getVariables());
            }

            output.close();
            error.close();
            return execute;

        } catch (Throwable e) {
            throw new ScriptException(new TaskException(getStackTraceAsString(e), e));
        }
    }

    public JavaExecutable getExecutable(String userExecutableClassName) throws ExecutableCreationException {
        try {
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            Class<?> userExecutableClass = tcl.loadClass(userExecutableClassName);
            return (JavaExecutable) userExecutableClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ExecutableCreationException("Unable to instantiate JavaExecutable. " + userExecutableClassName +
                                                  " class cannot be found", e);
        } catch (InstantiationException e) {
            throw new ExecutableCreationException("Unable to instantiate JavaExecutable. " + userExecutableClassName +
                                                  " might not define no-args constructor", e);
        } catch (ClassCastException e) {
            throw new ExecutableCreationException("Unable to instantiate JavaExecutable. " + userExecutableClassName +
                                                  " might not inherit from org.ow2.proactive.scheduler.common.task.executable.JavaExecutable",
                                                  e);
        } catch (Throwable e) {
            throw new ExecutableCreationException("Unable to instantiate JavaExecutable", e);
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        try {
            return eval(IOUtils.toString(reader), context);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new JavaClassScriptEngineFactory();
    }
}
