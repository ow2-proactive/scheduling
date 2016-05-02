/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptContext;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.internal.JavaStandaloneExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scripting.helper.progress.ProgressFile;


/**
 * AbstractJavaExecutable : base class of Java Executables
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class JavaExecutable {

    // this value is set only on worker node side !!
    // see JavaTaskLauncher
    protected JavaStandaloneExecutableInitializer execInitializer;

    private Map<String, Serializable> propagatedVariables;

    private String inputSpace, outputSpace, globalSpace, localSpace, userSpace;

    /**
     * Initialize the executable using the given executable Initializer.
     *
     * @param execInitializer the executable Initializer used to init the executable itself
     *
     * @throws Exception an exception if something goes wrong during executable initialization.
     */
    public void internalInit(JavaStandaloneExecutableInitializer execInitializer, ScriptContext sc) throws Exception {
        this.execInitializer = execInitializer;
        // at this point, the context class loader is the TaskClassLoader
        // see JavaExecutableContainer.getExecutable()
        Map<String, Serializable> arguments = this.execInitializer.getArguments(Thread.currentThread()
                .getContextClassLoader());
        Map<String, Serializable> propagatedVariables = SerializationUtil
                .deserializeVariableMap(execInitializer.getPropagatedVariables());
        setVariables(propagatedVariables);
        // update arguments
        updateVariables(arguments, getVariables());
        init(arguments);
        initDataSpaces(sc);
    }

    /**
     * Initialization default method for a java task.<br>
     * <p>
     * By default, this method does automatic assignment between the value given in the arguments map
     * and the fields contained in your executable.<br>
     * If the field type and the argument type are different and if the argument type is String
     * (i.e. for all jobs defined with XML descriptors), then a automatic mapping is tried.
     * Managed types are byte, short, int, long, boolean and the corresponding classes, other type
     * must be handle by user by overriding this method.<br><br>
     * For example, if you set as argument the key="var", value="12" in the XML descriptor<br>
     * just add an int (or Integer, long, Long) field named "var" in your executable.
     * The default {@link #init(java.util.Map)} method will store your arguments into the integer class field.
     * </p>
     * To avoid this default behavior, just override this method to make your own initialization.
     *
     * @param args a map containing the different parameter names and values given by the user task.
     */
    public void init(Map<String, Serializable> args) throws Exception {
        if (args == null) {
            return;
        }
        Class<?> current = this.getClass();
        while (JavaExecutable.class.isAssignableFrom(current)) {
            for (Entry<String, Serializable> e : args.entrySet()) {
                try {
                    Field f = current.getDeclaredField(e.getKey());
                    // if f does not exist -> catch block
                    f.setAccessible(true);
                    Class<?> fieldClass = f.getType();
                    Class<?> valueClass = e.getValue().getClass();
                    // unbox manually as it is not done automatically
                    // ie : int is not assignable from Integer
                    if (valueClass.equals(Integer.class) || valueClass.equals(Short.class) ||
                        valueClass.equals(Long.class) || valueClass.equals(Byte.class) ||
                        valueClass.equals(Boolean.class)) {
                        e.setValue(e.getValue().toString());
                        valueClass = String.class;
                    }
                    if (String.class.equals(valueClass) && !String.class.equals(fieldClass)) {
                        String valueAsString = (String) e.getValue();
                        // parameter has been defined as string in XML
                        // try to convert it automatically
                        if (fieldClass.equals(Integer.class) || fieldClass.equals(int.class)) {
                            f.set(this, Integer.parseInt(valueAsString));
                        } else if (fieldClass.equals(Short.class) || fieldClass.equals(short.class)) {
                            f.set(this, Short.parseShort(valueAsString));
                        } else if (fieldClass.equals(Long.class) || fieldClass.equals(long.class)) {
                            f.set(this, Long.parseLong(valueAsString));
                        } else if (fieldClass.equals(Byte.class) || fieldClass.equals(byte.class)) {
                            f.set(this, Byte.parseByte(valueAsString));
                        } else if (fieldClass.equals(Boolean.class) || fieldClass.equals(boolean.class)) {
                            f.set(this, Boolean.parseBoolean(valueAsString));
                        }
                    } else if (fieldClass.isAssignableFrom(valueClass)) {
                        // no conversion for other type than String and primitive
                        f.set(this, e.getValue());
                    }
                } catch (Exception ex) {
                    // nothing to do, no automatic assignment can be done for this field
                }
            }
            current = current.getSuperclass();
        }
    }

    /**
     * Initialization of the dataSpaces.<br>
     *
     * @param sc the ScriptContext including as bindings the dataSpaces locations.
     */
    public void initDataSpaces(ScriptContext sc) {
        this.inputSpace = (String) sc.getAttribute(SchedulerConstants.DS_INPUT_BINDING_NAME);
        this.outputSpace = (String) sc.getAttribute(SchedulerConstants.DS_OUTPUT_BINDING_NAME);
        this.globalSpace = (String) sc.getAttribute(SchedulerConstants.GLOBALSPACE_NAME);
        this.userSpace = (String) sc.getAttribute(SchedulerConstants.USERSPACE_NAME);
        this.localSpace = (String) sc.getAttribute(SchedulerConstants.DS_SCRATCH_BINDING_NAME);
    }

    /**
     * Use this method for a multi-node task. It returns the list of nodes url demanded by the user
     * while describing the task.<br>
     * In a task, one node is used to start the task itself, the other are returned by this method.<br>
     * If user describe the task using the "numberOfNodes" property set to 5, then this method
     * returns a list containing 4 nodes. The first one being used by the task itself.
     *
     * @return the list of nodes demanded by the user.
     */
    public List<String> getNodesURL() {
        return execInitializer.getNodesURL();
    }

    /**
     * When iteration occurs due to a {@link org.ow2.proactive.scheduler.common.task.flow.FlowActionType#LOOP} FlowAction,
     * each new iterated instance of a task is assigned an iteration index so
     * that it can be uniquely identified.
     * <p>
     * This is a convenience method to retrieve the Iteration Index that was exported
     * as a Java Property by the TaskLauncher.
     *
     * @return the Iteration Index of this Task
     */
    public final int getIterationIndex() {
        return (Integer) getVariables().get(SchedulerVars.PA_TASK_ITERATION.toString());
    }

    /**
     * When replication occurs due to a {@link org.ow2.proactive.scheduler.common.task.flow.FlowActionType#REPLICATE} FlowAction,
     * each new replicated instance of a task is assigned a replication index so
     * that it can be uniquely identified.
     * <p>
     * This is a convenience method to retrieve the Replication Index that was exported
     * as a Java Property by the TaskLauncher.
     * 
     * @return the Replication Index of this Task
     */
    public final int getReplicationIndex() {
        return (Integer) getVariables().get(SchedulerVars.PA_TASK_REPLICATION.toString());
    }

    /**
     * Third-party credentials are specific to each Scheduler user and stored on the server side.
     * They consist of key-value pairs and are exposed in Java tasks via this method.
     *
     * @param key the credential's key whose associated value is to be returned
     * @return the credential's value associated with the key parameter
     */
    protected String getThirdPartyCredential(String key) {
        return execInitializer.getThirdPartyCredentials().get(key);
    }

    /**
     * When using non forked Java tasks, you should use this PrintStream instead of System.out.
     * @return a stream that will write to the task's output stream
     */
    protected PrintStream getOut() {
        return execInitializer.getOutputSink();
    }

    /**
     * When using non forked Java tasks, you should use this PrintStream instead of System.err.
     * @return a stream that will write to the task's error stream
     */
    protected PrintStream getErr() {
        return execInitializer.getErrorSink();
    }

    private void updateVariables(Map<String, Serializable> old, Map<String, Serializable> updated) {
        for (String k : old.keySet()) {
            if (updated.containsKey(k)) {
                old.put(k, updated.get(k));
            }
        }
    }

    /**
     * The content of this method will be executed once after being scheduled.<br>
     * This may generate a result as an {@link Object}. It can be whatever you want.<br>
     * The results list order correspond to the order in the dependence list.
     *
     * @param results the results (as a taskResult) from parent tasks.
     * @throws Throwable any exception thrown by the user's code
     * @return any serializable object from the user.
     */
    public abstract Serializable execute(TaskResult... results) throws Throwable;

    /**
     * Set the progress value for this Executable. Progress value must be ranged
     * between 0 and 100.
     * @param newValue the new progress value
     * @return the previous progress value
     * @throws IllegalArgumentException if the value is not ranged between 0 and 100.
     */
    protected final int setProgress(int newValue) throws IllegalArgumentException {
        if (newValue < 0 || newValue > 100) {
            throw new IllegalArgumentException("Progress value must be ranged between 0 and 100");
        }
        String progressFilePath = (String) getVariables().get(SchedulerVars.PA_TASK_PROGRESS_FILE.toString());
        int previousValue = ProgressFile.getProgress(progressFilePath);
        ProgressFile.setProgress(progressFilePath, newValue);
        return previousValue;
    }

    /**
     * Return the current progress value for this executable, ranged between 0 and 100.
     *
     * @return the current progress value for this executable.
     */
    public int getProgress() {
        return ProgressFile.getProgress((String) getVariables().get(
                SchedulerVars.PA_TASK_PROGRESS_FILE.toString()));
    }

    public Map<String, Serializable> getVariables() {
        return this.propagatedVariables;
    }

    public void setVariables(Map<String, Serializable> propagatedVariables) {
        this.propagatedVariables = propagatedVariables;
    }

    public String getInputSpace()
    {
        return this.inputSpace;
    }

    public String getOutputSpace()
    {
        return this.outputSpace;
    }

    public String getGlobalSpace()
    {
        return this.globalSpace;
    }

    public String getLocalSpace()
    {
        return this.localSpace;
    }

    public String getUserSpace()
    {
        return this.userSpace;
    }
}
