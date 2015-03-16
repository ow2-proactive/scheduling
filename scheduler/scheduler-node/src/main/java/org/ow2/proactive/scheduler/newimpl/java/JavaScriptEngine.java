/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.newimpl.java;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.AbstractJavaExecutable;
import org.ow2.proactive.scheduler.common.task.executable.internal.JavaExecutableInitializerImpl;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.task.TaskLauncherBak;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.TaskScript;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;

import static java.util.Collections.emptyList;


public class JavaScriptEngine extends AbstractScriptEngine {

    @Override
    public Object eval(String userExecutableClassName, ScriptContext context) throws ScriptException {

        try {
            AbstractJavaExecutable javaExecutable = getExecutable(userExecutableClassName);

            JavaExecutableInitializerImpl execInitializer = new JavaExecutableInitializerImpl();
            PrintStream output = new PrintStream(new WriterOutputStream(context.getWriter()), true);
            execInitializer.setOutputSink(output);
            PrintStream error = new PrintStream(new WriterOutputStream(context.getErrorWriter()), true);
            execInitializer.setErrorSink(error);

            Map<String, byte[]> propagatedVariables = null;
            if (context.getAttribute(TaskLauncherBak.VARIABLES_BINDING_NAME) != null) {
                propagatedVariables = SerializationUtil.serializeVariableMap(
                  (Map<String, Serializable>) context.getAttribute(TaskLauncherBak.VARIABLES_BINDING_NAME));
                execInitializer.setPropagatedVariables(propagatedVariables
                ); // TODO how to modify those? for propagation
            } else {
                execInitializer.setPropagatedVariables(Collections.<String, byte[]> emptyMap());
            }

            if (context.getAttribute(Script.ARGUMENTS_NAME) != null) {
                execInitializer.setSerializedArguments(
                  (Map<String, byte[]>) ((Serializable[]) context.getAttribute(Script.ARGUMENTS_NAME))[0]);
            } else {
                execInitializer.setSerializedArguments(Collections.<String, byte[]>emptyMap());
            }

            if (context.getAttribute(TaskScript.CREDENTIALS_VARIABLE) != null) {
                execInitializer.setThirdPartyCredentials(
                  (Map<String, String>) context.getAttribute(TaskScript.CREDENTIALS_VARIABLE));
            } else {
                execInitializer.setThirdPartyCredentials(Collections.<String, String> emptyMap());
            }

            if (context.getAttribute(TaskLauncherBak.MULTI_NODE_TASK_NODESURL_BINDING_NAME) != null) {
                Set<String> nodesURLs = (Set<String>) context.getAttribute(TaskLauncherBak.MULTI_NODE_TASK_NODESURL_BINDING_NAME);
                execInitializer.setNodesURL(new ArrayList<String>(nodesURLs));
            } else {
                execInitializer.setNodesURL(emptyList());
            }

            javaExecutable.internalInit(execInitializer);

            Serializable execute = javaExecutable.execute((TaskResult[]) context
                    .getAttribute(TaskScript.RESULTS_VARIABLE));

            context.setAttribute(TaskScript.RESULT_VARIABLE, execute, ScriptContext.ENGINE_SCOPE);
            if (propagatedVariables != null) {
                context.setAttribute(TaskLauncherBak.VARIABLES_BINDING_NAME,
                        SerializationUtil.deserializeVariableMap(propagatedVariables),
                        ScriptContext.ENGINE_SCOPE);
            }

            output.close();
            error.close();
            return execute;

        } catch (Throwable e) {
            throw new ScriptException(new Exception("Failed to execute Java executable", e));
        }
    }

    private AbstractJavaExecutable getExecutable(String userExecutableClassName)
            throws ExecutableCreationException {
        try {
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            Class<?> userExecutableClass = tcl.loadClass(userExecutableClassName);
            return (AbstractJavaExecutable) userExecutableClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ExecutableCreationException("Unable to instanciate JavaExecutable. " +
                userExecutableClassName + " class cannot be found", e);
        } catch (InstantiationException e) {
            throw new ExecutableCreationException("Unable to instanciate JavaExecutable. " +
                userExecutableClassName + " might not define no-args constructor", e);
        } catch (ClassCastException e) {
            throw new ExecutableCreationException("Unable to instanciate JavaExecutable. " +
                userExecutableClassName +
                " might not inherit from org.ow2.proactive.scheduler.common.task.executable.JavaExecutable",
                e);
        } catch (Throwable e) {
            throw new ExecutableCreationException("Unable to instanciate JavaExecutable", e);
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
        return new JavaScriptEngineFactory();
    }
}
