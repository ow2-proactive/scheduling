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
package org.ow2.proactive.scheduler.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.tests.ProActiveTestClean;


/**
 * @author ActiveEon Team
 * @since 23/06/2017
 */
public class CheckEligibleTaskDescriptorScriptTest extends ProActiveTestClean {

    private EligibleTaskDescriptorImpl etd;

    private InternalTask it;

    private InternalScriptTask ist;

    private ScriptExecutableContainer sec;

    private ForkEnvironment fe;

    @Before
    public void init() {
        etd = mock(EligibleTaskDescriptorImpl.class);
        it = mock(InternalTask.class);
        ist = mock(InternalScriptTask.class);
        sec = mock(ScriptExecutableContainer.class);
        fe = mock(ForkEnvironment.class);

        Mockito.when(((EligibleTaskDescriptorImpl) etd).getInternal()).thenReturn(it);
        Mockito.when(ist.getExecutableContainer()).thenReturn(sec);
        Mockito.when(it.getForkEnvironment()).thenReturn(fe);
        Mockito.when(ist.getForkEnvironment()).thenReturn(fe);
    }

    @Test
    public void testAllScriptsContainAPIBinding() throws InvalidScriptException {
        Script s = scriptWithApiBindingClient();
        Script s1 = scriptWithApiBindingUser();
        Script s2 = scriptWithApiBindingGlobal();
        FlowScript fs = flowScriptWithApiBindingClient();
        Mockito.when(it.getPreScript()).thenReturn(s1);
        Mockito.when(it.getPostScript()).thenReturn(s2);
        Mockito.when(it.getCleaningScript()).thenReturn(s2);
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(fe.getEnvScript()).thenReturn(s);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertTrue(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testAllScriptsNotContainAPIBinding() throws InvalidScriptException {
        Script s = scriptWithoutApiBinding();
        FlowScript fs = flowScriptWithoutApiBinding();
        Mockito.when(it.getPreScript()).thenReturn(s);
        Mockito.when(it.getPostScript()).thenReturn(s);
        Mockito.when(it.getCleaningScript()).thenReturn(s);
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(fe.getEnvScript()).thenReturn(s);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertFalse(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testOnlyPreScriptContainsAPIBinding() throws InvalidScriptException {
        Script s = scriptWithApiBindingUser();
        Script s2 = scriptWithoutApiBinding();
        FlowScript fs = flowScriptWithoutApiBinding();
        Mockito.when(it.getPreScript()).thenReturn(s);
        Mockito.when(it.getPostScript()).thenReturn(s2);
        Mockito.when(it.getCleaningScript()).thenReturn(s2);
        Mockito.when(sec.getScript()).thenReturn(s2);
        Mockito.when(fe.getEnvScript()).thenReturn(s2);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertTrue(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testOnlyPostScriptContainsAPIBinding() throws InvalidScriptException {
        Script s = scriptWithApiBindingClient();
        Script s2 = scriptWithoutApiBinding();
        FlowScript fs = flowScriptWithoutApiBinding();
        Mockito.when(it.getPreScript()).thenReturn(s2);
        Mockito.when(it.getPostScript()).thenReturn(s);
        Mockito.when(it.getCleaningScript()).thenReturn(s2);
        Mockito.when(sec.getScript()).thenReturn(s2);
        Mockito.when(fe.getEnvScript()).thenReturn(s2);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertTrue(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testOnlyCleanScriptContainsAPIBinding() throws InvalidScriptException {
        Script s = scriptWithApiBindingClient();
        Script s2 = scriptWithoutApiBinding();
        FlowScript fs = flowScriptWithoutApiBinding();
        Mockito.when(it.getPreScript()).thenReturn(s2);
        Mockito.when(it.getPostScript()).thenReturn(s2);
        Mockito.when(it.getCleaningScript()).thenReturn(s);
        Mockito.when(sec.getScript()).thenReturn(s2);
        Mockito.when(fe.getEnvScript()).thenReturn(s2);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertTrue(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testOnlyInternalScriptContainsAPIBinding() throws InvalidScriptException {
        Script s = scriptWithApiBindingGlobal();
        Script s2 = scriptWithoutApiBinding();
        FlowScript fs = flowScriptWithoutApiBinding();
        Mockito.when(((EligibleTaskDescriptorImpl) etd).getInternal()).thenReturn(ist);
        Mockito.when(ist.getPreScript()).thenReturn(s2);
        Mockito.when(ist.getPostScript()).thenReturn(s2);
        Mockito.when(it.getCleaningScript()).thenReturn(s2);
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(fe.getEnvScript()).thenReturn(s2);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertTrue(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testOnlyEnvScriptContainsAPIBinding() throws InvalidScriptException {
        Script s = scriptWithApiBindingUser();
        FlowScript fs = flowScriptWithoutApiBinding();
        Script s2 = scriptWithoutApiBinding();
        Mockito.when(it.getPreScript()).thenReturn(s2);
        Mockito.when(it.getPostScript()).thenReturn(s2);
        Mockito.when(it.getCleaningScript()).thenReturn(s2);
        Mockito.when(sec.getScript()).thenReturn(s2);
        Mockito.when(fe.getEnvScript()).thenReturn(s);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertTrue(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testOnlyFlowScriptContainsAPIBinding() throws InvalidScriptException {
        Script s = scriptWithoutApiBinding();
        FlowScript fs = flowScriptWithApiBindingUser();
        Mockito.when(it.getPreScript()).thenReturn(s);
        Mockito.when(it.getPostScript()).thenReturn(s);
        Mockito.when(it.getCleaningScript()).thenReturn(s);
        Mockito.when(fe.getEnvScript()).thenReturn(s);
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertTrue(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testAllScriptsNull() throws InvalidScriptException {
        Script s = null;
        FlowScript fs = null;
        Mockito.when(it.getPreScript()).thenReturn(s);
        Mockito.when(it.getPostScript()).thenReturn(s);
        Mockito.when(it.getCleaningScript()).thenReturn(s);
        Mockito.when(fe.getEnvScript()).thenReturn(s);
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(it.getFlowScript()).thenReturn(fs);
        assertFalse(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testForkEnvironmentNull() throws InvalidScriptException {
        fe = null;
        Mockito.when(((EligibleTaskDescriptorImpl) etd).getInternal()).thenReturn(it);
        Mockito.when(it.getForkEnvironment()).thenReturn(fe);
        assertFalse(new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd));
    }

    @Test
    public void testScriptExecutableContainerNull() throws InvalidScriptException {
        sec = null;
        Mockito.when(((EligibleTaskDescriptorImpl) etd).getInternal()).thenReturn(ist);
        Mockito.when(ist.getExecutableContainer()).thenReturn(sec);
        Boolean result = new CheckEligibleTaskDescriptorScript().isTaskContainsAPIBinding(etd);
        assertFalse(result);
    }

    private SimpleScript scriptWithApiBindingClient() throws InvalidScriptException {
        String engineName = "engineName";
        String script = "System.out.println(\"message\")" + SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME + "";
        return new SimpleScript(script, engineName);
    }

    private SimpleScript scriptWithApiBindingUser() throws InvalidScriptException {
        String engineName = "engineName";
        String script = "sleep(5000)" + SchedulerConstants.DS_USER_API_BINDING_NAME + "System.out.println(\"message\")";
        return new SimpleScript(script, engineName);
    }

    private SimpleScript scriptWithApiBindingGlobal() throws InvalidScriptException {
        String engineName = "engineName";
        String script = "System.out.println(\"message\")" + SchedulerConstants.DS_GLOBAL_API_BINDING_NAME + "";
        return new SimpleScript(script, engineName);
    }

    private SimpleScript scriptWithoutApiBinding() throws InvalidScriptException {
        String engineName = "engineName";
        String script = "sleep(5000)";
        return new SimpleScript(script, engineName);
    }

    private FlowScript flowScriptWithApiBindingClient() throws InvalidScriptException {
        String script = "System.out.println(\"message\")" + SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME + "";
        return new FlowScript().createReplicateFlowScript(script);
    }

    private FlowScript flowScriptWithApiBindingUser() throws InvalidScriptException {
        String script = "System.out.println(\"message\")" + SchedulerConstants.DS_USER_API_BINDING_NAME + "";
        return new FlowScript().createReplicateFlowScript(script);
    }

    private FlowScript flowScriptWithApiBindingGlobal() throws InvalidScriptException {
        String script = "System.out.println(\"message\")" + SchedulerConstants.DS_GLOBAL_API_BINDING_NAME + "";
        return new FlowScript().createReplicateFlowScript(script);
    }

    private FlowScript flowScriptWithoutApiBinding() throws InvalidScriptException {
        String script = "sleep(5000)";
        return new FlowScript().createReplicateFlowScript(script);
    }

}
