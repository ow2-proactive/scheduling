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
package org.ow2.proactive.scheduler.common.job.factories;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.UpdatableProperties;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


public class JobComparator {

    // stack used to create a message in case of differences found between jobs
    private Stack<String> stack;

    /**
     * if the last 2 jobs compared are not equal, a message will be returned
     * explaining the first difference encountered. Returns an empty string if
     * the
     * 
     */
    public String getDifferenceMessage() {
        return stack.toString();
    }

    /**
     * 
     * We state that: For any jobs (TaskFlowJob) job1 and job2 such that job1
     * serialized to xml produces job1.xml job1.xml loaded in java produces job2
     * then isEqual(job1,job2) == true
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public boolean isEqualJob(TaskFlowJob job1, TaskFlowJob job2) throws IOException, ClassNotFoundException {

        stack = new Stack<>();
        stack.push("job");

        stack.push("Job attributes");
        if (!isEqualCommonAttribute(job1, job2))
            return false;

        stack.pop(); // job attributes

        if (!isEqualString(job1.getProjectName(), job2.getProjectName(), false)) {
            stack.push("ProjectName");
            return false;
        }

        if (!job1.getPriority().equals(job2.getPriority())) {
            stack.push("Priority");
            return false;
        }

        if (!job1.getName().equals(job2.getName())) {
            stack.push("Name");
            return false;
        }

        if (!isEqualString(job1.getDescription(), job2.getDescription())) {
            stack.push("Description");
            return false;
        }

        if (!isEqualString(job1.getInputSpace(), job2.getInputSpace())) {
            stack.push("Input Space");
            return false;
        }

        if (!isEqualString(job1.getOutputSpace(), job2.getOutputSpace())) {
            stack.push("Output Space");
            return false;
        }

        stack.push("stackflow");
        if (!isTaskFlowEqual(job1, job2))
            return false;
        stack.pop();

        return true;
    }

    private boolean isEqualCommonAttribute(CommonAttribute attrib1, CommonAttribute attrib2) {
        if (!isEqualUpdatableProperty(attrib1.getOnTaskErrorProperty(), attrib2
                .getOnTaskErrorProperty())) {
            stack.push(" CancelJobOnErrorProperty ");
            return false;
        }

        if (!isEqualUpdatableProperty(attrib1.getMaxNumberOfExecutionProperty(), attrib2
                .getMaxNumberOfExecutionProperty())) {
            stack.push(" maxNumberOfExecution ");
            return false;
        }

        if (!isEqualUpdatableProperty(attrib1.getRestartTaskOnErrorProperty(), attrib2
                .getRestartTaskOnErrorProperty())) {
            stack.push(" RestartTaskOnError ");
            return false;
        }

        stack.push(" genericInformations ");
        if (!isEqualMap(attrib1.getGenericInformation(), attrib2.getGenericInformation())) {

            stack.push("generic info 1= " + attrib1.getGenericInformation() + " ----- generic info 2 = " +
                attrib2.getGenericInformation());

            return false;
        }
        stack.pop(); // generic informations
        return true;
    }

    private boolean isTaskFlowEqual(TaskFlowJob job1, TaskFlowJob job2) throws IOException,
            ClassNotFoundException {
        ArrayList<Task> tasks1 = job1.getTasks();
        ArrayList<Task> tasks2 = job2.getTasks();

        if (tasks1.size() != tasks2.size()) {
            stack.push("Sizes don't match");
            return false;
        }
        // the order of the tasks may not be the same
        // the tasks have unique name inside a job
        Map<String, Task> map1 = new HashMap<>();
        Map<String, Task> map2 = new HashMap<>();

        for (int k = 0; k < tasks1.size(); k++) {
            map1.put(tasks1.get(k).getName(), tasks1.get(k));
            map2.put(tasks2.get(k).getName(), tasks2.get(k));
        }

        for (String name : map1.keySet()) {
            stack.push("Task " + map1.get(name));
            if (!isTaskEqual(map1.get(name), map2.get(name)))
                return false;
            stack.pop();
        }
        return true;
    }

    private boolean isTaskEqual(Task t1, Task t2) throws IOException, ClassNotFoundException {

        if ((t1 == null) && (t2 == null))
            return true;

        if ((t1 == null) ^ (t2 == null)) {
            stack.push("One of 2 tasks is null");
            return false;
        }

        if (!isEqualCommonAttribute(t1, t2))
            return false;

        if (!t1.getName().equals(t2.getName())) {
            stack.push("name");
            return false;
        }
        if (!isEqualString(t1.getDescription(), t2.getDescription())) {
            stack.push("description");
            return false;
        }

        if (t1.getWallTime() != t2.getWallTime()) {
            stack.push("walltime");
            return false;
        }

        // ****** task dependencies ****
        stack.push("task dependenices");
        List<Task> dep1 = t1.getDependencesList();
        List<Task> dep2 = t2.getDependencesList();

        if (dep1 == null ^ dep2 == null) {
            stack.push("one dependency list is empty");
            return false;
        }

        if (dep1 != null) {
            if (dep1.size() != dep2.size()) {
                stack.push("sizes don't match");
                return false;
            }

            // we only compare the names in the 2 dependencies lists
            int dep1Size = dep1.size();

            Set<String> names1 = new HashSet<String>(dep1Size);
            Set<String> names2 = new HashSet<String>(dep1Size);

            for (int k = 0; k < dep1Size; k++) {
                names1.add(dep1.get(k).getName());
                names2.add(dep2.get(k).getName());
            }

            if (!CollectionUtils.isEqualCollection(names1, names2)) {
                return false;
            }
        }

        stack.pop(); // task dependencies

        // **** parallel environment ****
        stack.push("parallel environment");
        if (!isEqualParallelEnvironment(t1.getParallelEnvironment(), t2.getParallelEnvironment()))
            return false;
        stack.pop(); // parallel env

        // input files
        stack.push("input files");
        if (!isEqualInputFiles(t1.getInputFilesList(), t2.getInputFilesList()))
            return false;
        stack.pop();

        stack.push("output files");
        if (!isEqualOutputFiles(t1.getOutputFilesList(), t2.getOutputFilesList()))
            return false;
        stack.pop();

        // scripts
        stack.push("pre script");
        if (!isEqualScript(t1.getPreScript(), t2.getPreScript()))
            return false;
        stack.pop();

        stack.push("post script");
        if (!isEqualScript(t1.getPostScript(), t2.getPostScript()))
            return false;
        stack.pop();

        stack.push("cleaning script");
        if (!isEqualScript(t1.getCleaningScript(), t2.getCleaningScript()))
            return false;
        stack.pop();

        stack.push("selection scripts");
        List<SelectionScript> ss1 = t1.getSelectionScripts();
        List<SelectionScript> ss2 = t2.getSelectionScripts();

        if ((ss1 == null) ^ (ss2 == null)) {
            stack.push("One of two lists of selection scripts is null");
            return false;
        }

        if (ss1 != null) {
            if (t1.getSelectionScripts().size() != t2.getSelectionScripts().size()) {
                stack.push("lists size don't match");
                return false;
            }

            for (int k = 0; k < t1.getSelectionScripts().size(); k++) {
                if (!isEqualScript(t1.getSelectionScripts().get(k), t2.getSelectionScripts().get(k))) {
                    return false;
                }
            }
        }
        stack.pop(); // select scripts

        // flow control
        if (t1.getFlowBlock() != t2.getFlowBlock()) {
            stack.push("flow block");
            return false;
        }

        stack.push("flow control");
        if (!isEqualFlowControl(t1.getFlowScript(), t2.getFlowScript()))
            return false;
        stack.pop();

        // ***** task executable *****
        if (!isEqualClass(t1.getClass(), t2.getClass())) {
            stack.push("Executable types don't match");
            return false;
        }

        if (t1 instanceof JavaTask) {
            JavaTask jt1 = (JavaTask) t1;
            JavaTask jt2 = (JavaTask) t2;
            stack.push("arguments");
            if (!isEqualMap(jt1.getArguments(), jt2.getArguments()))
                return false;
            stack.pop();

            stack.push("executable class");
            if (!isEqualString(jt1.getExecutableClassName(), jt2.getExecutableClassName()))
                return false;
            stack.pop();

            stack.push("forked environemnt");
            if (!isEqualForkedEnvironment(jt1.getForkEnvironment(), jt2.getForkEnvironment()))
                return false;
            stack.pop();
        } // insttanceof JavaTask

        if (t1 instanceof NativeTask) {
            NativeTask nt1 = (NativeTask) t1;
            NativeTask nt2 = (NativeTask) t2;
            String[] cl1 = nt1.getCommandLine();
            String[] cl2 = nt2.getCommandLine();

            if (cl1 == null ^ cl2 == null) {
                return false;

            } else if (cl1 != null) {
                if (!CollectionUtils.isEqualCollection(Arrays.asList(cl1), Arrays.asList(cl2))) {
                    return false;
                }
            }

        }

        if (t1 instanceof ScriptTask) {
            ScriptTask st1 = (ScriptTask) t1;
            ScriptTask st2 = (ScriptTask) t2;
            if (!isEqualScript(st1.getScript(), st2.getScript()))
                return false;
        }

        return true;
    }

    private boolean isEqualForkedEnvironment(ForkEnvironment fe1, ForkEnvironment fe2) {
        if ((fe1 == null) && (fe2 == null))
            return true;

        if ((fe1 == null) ^ (fe2 == null)) {
            stack.push("One null value out of 2");
            return false;
        }
        if (!CollectionUtils.isEqualCollection(fe1.getAdditionalClasspath(), fe2.getAdditionalClasspath())) {
            stack.push("AdditionalClasspath");
            return false;
        }
        if (!isEqualScript(fe1.getEnvScript(), fe2.getEnvScript())) {
            stack.push("EnvScript");
            return false;
        }

        if (!isEqualString(fe1.getJavaHome(), fe2.getJavaHome())) {
            stack.push("JavaHome");
            return false;
        }

        if (!CollectionUtils.isEqualCollection(fe1.getJVMArguments(), fe2.getJVMArguments())) {
            stack.push("JVMArguments");
            return false;
        }

        if (!isEqualMap(fe1.getSystemEnvironment(), fe2.getSystemEnvironment())) {
            stack.push("SystemEnvironment");
            return false;
        }

        if (!isEqualString(fe1.getWorkingDir(), fe2.getWorkingDir())) {
            stack.push("WorkingDir");
            return false;
        }

        // no need to check fe1.getPropertyModifiers() as they are included in
        // getSystemEnvironment()

        return true;
    }

    private boolean isEqualFlowControl(FlowScript fs1, FlowScript fs2) {
        if ((fs1 == null) && (fs2 == null))
            return true;

        if ((fs1 == null) || (fs2 == null))
            return false;

        if (!isEqualString(fs1.getActionContinuation(), fs2.getActionContinuation()))
            return false;

        if (!isEqualString(fs1.getActionTarget(), fs2.getActionTarget()))
            return false;

        if (!isEqualString(fs1.getActionTargetElse(), fs2.getActionTargetElse()))
            return false;

        if (!isEqualString(fs1.getActionType(), fs2.getActionType()))
            return false;

        if (!isEqualString(fs1.getEngineName(), fs2.getEngineName()))
            return false;

        if (!isEqualScript(fs1, fs2))
            return false;

        return true;

    }

    private boolean isEqualString(String s1, String s2) {
        if ((s1 == null) && (s2 == null))
            return true;

        if ((s1 == null) ^ (s2 == null)) {
            stack.push("One of 2 values is null " + s1 + "!=" + s2);
            return false;
        }
        if (!s1.equals(s2)) {
            stack.push(s1 + "!= " + s2);
            return false;
        }

        return true;

    }

    private boolean isEqualString(String s1, String s2, boolean caseSensitive) {
        if ((s1 == null) || (s2 == null))
            return isEqualString(s1, s2);

        if (caseSensitive)
            return isEqualString(s1, s2);
        else
            return isEqualString(s1.toLowerCase(), s2.toLowerCase());

    }

    /**
     * 
     * Rather than comparing the scripts and the parameters, we will inline the
     * parameters in the script code and then just compare the script text. This
     * is because the Job2XMLTransformer inlines the script parameters
     * 
     */
    private boolean isEqualScript(Script<?> s1, Script<?> s2) {
        if ((s1 == null) && (s2 == null))
            return true;

        if ((s1 == null) ^ (s2 == null)) {
            stack.push("One of 2 scripts is null");
            return false;
        }

        if (!s1.getEngineName().equals(s2.getEngineName())) {
            stack.push("engine name");
            return false;
        }

        String text1 = s1.getScript().trim();
        if (s1.getParameters() != null) {
            text1 = Job2XMLTransformer.inlineScriptParametersInText(text1, s1.getParameters());
        }
        String text2 = s2.getScript().trim();
        if (s2.getParameters() != null) {
            text2 = Job2XMLTransformer.inlineScriptParametersInText(text2, s2.getParameters());
        }

        return text1.equals(text2);
    }

    /**
     * Compares the element in the 2 lists in the exact order they appear in the
     * lists
     */
    private boolean isEqualInputFiles(List<InputSelector> l1, List<InputSelector> l2) {
        if ((l1 == null) && (l2 == null))
            return true;

        if ((l1 == null) ^ (l2 == null)) {
            stack.push("One of 2 values is null");
            return false;
        }
        if (l1.size() != l2.size()) {
            stack.push("sizes don't match");
            return false;
        }

        for (InputSelector is1 : l1) {
            boolean found = false;
            for (int i = 0; i < l2.size(); i++) {
                InputSelector is2 = l2.get(i);
                if (!is1.getMode().equals(is2.getMode())) {
                    continue;
                }

                Set<String> i1 = is1.getInputFiles().getIncludes();
                Set<String> i2 = is2.getInputFiles().getIncludes();
                if (!i1.equals(i2)) {
                    continue;
                }

                Set<String> e1 = is1.getInputFiles().getExcludes();
                Set<String> e2 = is2.getInputFiles().getExcludes();
                if (!e1.equals(e2)) {
                    continue;
                }

                found = true;
                break;
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the element in the 2 lists in the exact order they appear in the
     * lists FIXME: bad object design in the data space layer provides us to
     * unify the similar code in this method and isEqualInputFiles
     */
    private boolean isEqualOutputFiles(List<OutputSelector> l1, List<OutputSelector> l2) {
        if ((l1 == null) && (l2 == null))
            return true;

        if ((l1 == null) ^ (l2 == null)) {
            stack.push("One of 2 values is null");
            return false;
        }

        if (l1.size() != l2.size())
            return false;

        for (OutputSelector os1 : l1) {
            boolean found = false;
            for (int i = 0; i < l2.size(); i++) {
                OutputSelector os2 = l2.get(i);
                if (!os1.getMode().equals(os2.getMode())) {
                    continue;
                }

                Set<String> i1 = os1.getOutputFiles().getIncludes();
                Set<String> i2 = os2.getOutputFiles().getIncludes();
                if (!i1.equals(i2)) {
                    continue;
                }

                Set<String> e1 = os1.getOutputFiles().getExcludes();
                Set<String> e2 = os2.getOutputFiles().getExcludes();
                if (!e1.equals(e2)) {
                    continue;
                }

                found = true;
                break;
            }

            if (!found) {
                return false;
            }
        }
        return true;
    }

    private boolean isEqualParallelEnvironment(ParallelEnvironment e1, ParallelEnvironment e2) {
        if ((e1 == null) && (e2 == null))
            return true;

        if ((e1 == null) ^ (e2 == null)) {
            stack.push("One value out of 2 is null");
            return false;
        }
        if (e1.getNodesNumber() != e2.getNodesNumber()) {
            stack.push("nodes number");
            return false;
        }
        // check same instance of topology decsriptor
        TopologyDescriptor topologyDescriptor1 = e1.getTopologyDescriptor();
        TopologyDescriptor topologyDescriptor2 = e2.getTopologyDescriptor();

        if (topologyDescriptor1 == null && topologyDescriptor2 == null) {
            return true;
        }

        if (topologyDescriptor1 == null ^ topologyDescriptor2 == null) {
            return isEqualClass(TopologyDescriptor.ARBITRARY.getClass(),
                    (topologyDescriptor1 == null ? topologyDescriptor2.getClass() : topologyDescriptor1
                            .getClass()));
        }

        if (!isEqualClass(topologyDescriptor1.getClass(), topologyDescriptor2.getClass())) {
            stack.push("topology descriptor type");
            return false;
        }

        if (topologyDescriptor1 instanceof ThresholdProximityDescriptor) {
            if (!(topologyDescriptor2 instanceof ThresholdProximityDescriptor)) {
                stack.push("Only one is ThresholdProximityDescriptor type.");
                return false;
            }

            if (((ThresholdProximityDescriptor) topologyDescriptor1).getThreshold() != ((ThresholdProximityDescriptor) topologyDescriptor2)
                    .getThreshold()) {
                stack.push("ThresholdProximityDescriptor.threshold");
                return false;
            }
        }
        return true;
    }

    private boolean isEqualMap(Map<?, ?> m1, Map<?, ?> m2) {
        if ((m1 == null) && (m2 == null))
            return true;

        if ((m1 == null) || (m2 == null))
            return false;

        return m1.equals(m2);
    }

    private static <T> boolean isEqualUpdatableProperty(UpdatableProperties<T> property1,
            UpdatableProperties<T> property2) {
        return (property1.isSet() ^ property2.isSet()) ? false : ((property1.isSet()) ? property1.getValue()
                .equals(property2.getValue()) : true);
    }

    private boolean isEqualClass(Class<?> clazz1, Class<?> clazz2) {
        return clazz1.getName().equals(clazz2.getName());

    }

}
