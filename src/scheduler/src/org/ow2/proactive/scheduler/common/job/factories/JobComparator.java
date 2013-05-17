/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.apache.commons.collections.CollectionUtils;


public class JobComparator {

    //stack used to create a message in case of differences found between jobs
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
     * We state that:
     * 		For any jobs (TaskFlowJob) job1 and job2 such that
     * 			   job1 serialized to xml produces job1.xml
     * 			   job1.xml loaded in java produces job2
     * 		then isEqual(job1,job2) == true
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public boolean isEqualJob(TaskFlowJob job1, TaskFlowJob job2) throws IOException, ClassNotFoundException {

        stack = new Stack<String>();
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

        stack.push("Job Environment");
        if (!isEqualJobEnvironment(job1.getEnvironment(), job2.getEnvironment()))
            return false;
        stack.pop();

        stack.push("stackflow");
        if (!isTaskFlowEqual(job1, job2))
            return false;
        stack.pop();

        return true;
    }

    private boolean isEqualCommonAttribute(CommonAttribute attrib1, CommonAttribute attrib2) {

        if (attrib1.isCancelJobOnError() != attrib2.isCancelJobOnError()) {
            stack.push(" CancelJobOnErrorProperty ");
            return false;
        }
        if (attrib1.getMaxNumberOfExecution() != attrib2.getMaxNumberOfExecution()) {
            stack.push(" maxNumberOfExecution ");
            return false;
        }
        if (!attrib1.getRestartTaskOnError().equals(attrib2.getRestartTaskOnError())) {
            stack.push(" RestartTaskOnError ");
            return false;
        }

        stack.push(" genericInformations ");
        if (!isEqualMap(attrib1.getGenericInformations(), attrib2.getGenericInformations())) {

            stack.push("generic info 1= " + attrib1.getGenericInformations() + " ----- generic info 2 = " +
                attrib2.getGenericInformations());

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
        Map<String, Task> map1 = new HashMap<String, Task>();
        Map<String, Task> map2 = new HashMap<String, Task>();

        for (int k = 0; k < tasks1.size(); k++) {
            map1.put(tasks1.get(k).getName(), tasks1.get(k));
            map2.put(tasks2.get(k).getName(), tasks2.get(k));
        }

        for (String name : map1.keySet()) {
            stack.push("Task " + map1.get(name));
            if (isTaskEqual(map1.get(name), map2.get(name)))
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

        if (t1.getName().equals(t2.getName())) {
            stack.push("name");
            return false;
        }
        if (!isEqualString(t1.getDescription(), t2.getDescription())) {
            stack.push("description");
            return false;
        }
        if (!isEqualString(t1.getResultPreview(), t2.getResultPreview())) {
            stack.push("result preview");
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

        if (dep1.size() != dep2.size()) {
            stack.push("sizes don't match");
            return false;
        }
        // we only compare the names in the 2 dependencies lists
        Set<String> names1 = new HashSet<String>();
        Set<String> names2 = new HashSet<String>();
        for (int k = 0; k < dep1.size(); k++) {
            names1.add(dep1.get(k).getName());
            names2.add(dep2.get(k).getName());
        }

        if (!CollectionUtils.isEqualCollection(names1, names2)) {
            return false;
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
        if (isEqualScript(t1.getPreScript(), t2.getPreScript()))
            return false;
        stack.pop();

        stack.push("post script");
        if (isEqualScript(t1.getPostScript(), t2.getPostScript()))
            return false;
        stack.pop();

        stack.push("cleaning script");
        if (isEqualScript(t1.getCleaningScript(), t2.getCleaningScript()))
            return false;
        stack.pop();

        stack.push("selection scripts");
        List<SelectionScript> ss1 = t1.getSelectionScripts();
        List<SelectionScript> ss2 = t1.getSelectionScripts();

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
        if (!(t1.getClass().equals(t2.getClass()))) {
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
            if (isEqualString(jt1.getExecutableClassName(), jt2.getExecutableClassName()))
                return false;
            stack.pop();

            stack.push("forked environemnt");
            if (isEqualForkedEnvironment(jt1.getForkEnvironment(), jt2.getForkEnvironment()))
                return false;
            stack.pop();
        } // insttanceof JavaTask

        if (t1 instanceof NativeTask) {
            NativeTask nt1 = (NativeTask) t1;
            NativeTask nt2 = (NativeTask) t2;
            if (!CollectionUtils.isEqualCollection(Arrays.asList(nt1.getCommandLine()), Arrays.asList(nt2
                    .getCommandLine())))
                return false;

            if (!isEqualScript(nt1.getGenerationScript(), nt2.getGenerationScript()))
                return false;
        }

        if(t1 instanceof ScriptTask) {
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
    private boolean isEqualScript(Script s1, Script s2) {
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
        String text1 = Job2XMLTransformer.inlineScriptParametersInText(s1.getScript(), s1.getParameters());
        String text2 = Job2XMLTransformer.inlineScriptParametersInText(s2.getScript(), s2.getParameters());

        return text1.equals(text2);
    }

    /**
     * Compares the element in the 2 lists in the exact order they appear in the
     * lists
     *
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
            InputSelector is2 = l2.get(2);
            if (is1.getMode().equals(is2.getMode()))
                return false;

            if (CollectionUtils.isEqualCollection(Arrays.asList(is1.getInputFiles().getIncludes()), Arrays
                    .asList(is2.getInputFiles().getIncludes()))) {
                stack.push("includes");
                return false;
            }

            if (CollectionUtils.isEqualCollection(Arrays.asList(is1.getInputFiles().getExcludes()), Arrays
                    .asList(is2.getInputFiles().getExcludes()))) {
                stack.push("excludes");
                return false;
            }

        }
        return true;
    }

    /**
     * Compares the element in the 2 lists in the exact order they appear in the
     * lists FIXME: bad object design in the data space layer provides us to
     * unify the similar code in this method and isEqualInputFiles
     *
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
            OutputSelector os2 = l2.get(2);
            if (os1.getMode().equals(os2.getMode()))
                return false;

            if (CollectionUtils.isEqualCollection(Arrays.asList(os1.getOutputFiles().getIncludes()), Arrays
                    .asList(os2.getOutputFiles().getIncludes()))) {
                stack.push("includes");
                return false;
            }

            if (CollectionUtils.isEqualCollection(Arrays.asList(os1.getOutputFiles().getExcludes()), Arrays
                    .asList(os2.getOutputFiles().getExcludes()))) {
                stack.push("excludes");
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
        if (e1.getTopologyDescriptor().getClass().equals(e2.getTopologyDescriptor().getClass())) {
            stack.push("topology descriptor type");
            return false;
        }

        if (e1.getTopologyDescriptor() instanceof ThresholdProximityDescriptor) {
            ThresholdProximityDescriptor d1 = (ThresholdProximityDescriptor) e1.getTopologyDescriptor();
            ThresholdProximityDescriptor d2 = (ThresholdProximityDescriptor) e2.getTopologyDescriptor();
            if (d1.getThreshold() != d2.getThreshold()) {
                stack.push("ThresholdProximityDescriptor.threshold");
                return false;
            }
        }

        return true;
    }

    private boolean isEqualJobEnvironment(JobEnvironment e1, JobEnvironment e2) {
        if ((e1 == null) && (e2 == null))
            return true;

        if ((e1 == null) ^ (e2 == null)) {
            stack.push("One null value out of 2");
            return false;
        }

        String[] cp1 = e1.getJobClasspath();
        String[] cp2 = e2.getJobClasspath();

        if ((cp1 == null) && (cp2 == null))
            return true;

        if ((cp1 == null) ^ (cp2 == null)) {
            stack.push("One null value out of 2");
            return false;
        }

        if (!CollectionUtils.isEqualCollection(Arrays.asList(cp1), Arrays.asList(cp2))) {
            stack.push("classpath1 = " + Arrays.asList(e1.getJobClasspath()) + " ---- " + "classpath 2 = " +
                Arrays.asList(e2.getJobClasspath()));
            return false;
        } else
            return true;

    }

    private boolean isEqualMap(Map m1, Map m2) {
        if ((m1 == null) && (m2 == null))
            return true;

        if ((m1 == null) || (m2 == null))
            return false;

        return m1.equals(m2);
    }
}
