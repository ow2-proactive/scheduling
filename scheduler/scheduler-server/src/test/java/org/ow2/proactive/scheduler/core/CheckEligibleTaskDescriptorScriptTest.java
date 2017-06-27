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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;


/**
 * @author ActiveEon Team
 * @since 23/06/2017
 */
public class CheckEligibleTaskDescriptorScriptTest {

    private EligibleTaskDescriptor etd;

    private InternalTask it;

    private InternalScriptTask ist;

    private ScriptExecutableContainer sec;

    @Before
    public void init() {
        etd = mock(EligibleTaskDescriptor.class);
        it = mock(InternalTask.class);
        ist = mock(InternalScriptTask.class);
        sec = mock(ScriptExecutableContainer.class);

        Mockito.when(etd.getInternal()).thenReturn(it);
        Mockito.when(ist.getExecutableContainer()).thenReturn(sec);
    }

    @Test
    public void testAllScriptsContainAPIBindingOK() throws InvalidScriptException {
        Script s = scriptWithApiBindingClient();
        Script s1 = scriptWithApiBindingUser();
        Script s2 = scriptWithApiBindingGlobal();
        Mockito.when(etd.getInternal()).thenReturn(ist);
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(ist.getPreScript()).thenReturn(s1);
        Mockito.when(ist.getPostScript()).thenReturn(s2);
        Boolean result = new CheckEligibleTaskDescriptorScript().containsAPIBinding(etd);
        assertEquals(true, result);
    }

    @Test
    public void testAllScriptsContainAPIBindingKO() throws InvalidScriptException {
        Script s = scriptWithoutApiBinding();
        Mockito.when(etd.getInternal()).thenReturn(ist);
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(ist.getPreScript()).thenReturn(s);
        Mockito.when(ist.getPostScript()).thenReturn(s);
        Boolean result = new CheckEligibleTaskDescriptorScript().containsAPIBinding(etd);
        assertEquals(false, result);
    }

    @Test
    public void testScriptContainsAPIBindingOK() throws InvalidScriptException {
        Script s = scriptWithApiBindingGlobal();
        Script s2 = scriptWithoutApiBinding();
        Mockito.when(etd.getInternal()).thenReturn(ist);
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(ist.getPreScript()).thenReturn(s2);
        Mockito.when(ist.getPostScript()).thenReturn(s2);
        Boolean result = new CheckEligibleTaskDescriptorScript().containsAPIBinding(etd);
        assertEquals(true, result);
    }

    @Test
    public void testScriptContainsAPIBindingKO() throws InvalidScriptException {
        Script s = scriptWithoutApiBinding();
        Mockito.when(sec.getScript()).thenReturn(s);
        Mockito.when(it.getPreScript()).thenReturn(s);
        Mockito.when(it.getPostScript()).thenReturn(s);
        Boolean result = new CheckEligibleTaskDescriptorScript().containsAPIBinding(etd);
        assertEquals(false, result);
    }

    @Test
    public void testPostScriptContainsAPIBinding() throws InvalidScriptException {
        Script s = scriptWithApiBindingClient();
        Script s2 = scriptWithoutApiBinding();
        Mockito.when(it.getPreScript()).thenReturn(s2);
        Mockito.when(it.getPostScript()).thenReturn(s);
        Mockito.when(sec.getScript()).thenReturn(s2);
        Boolean result = new CheckEligibleTaskDescriptorScript().containsAPIBinding(etd);
        assertEquals(true, result);
    }

    @Test
    public void testPreScriptContainsAPIBinding() throws InvalidScriptException {
        Script s = scriptWithApiBindingUser();
        Script s2 = scriptWithoutApiBinding();
        Mockito.when(it.getPreScript()).thenReturn(s);
        Mockito.when(it.getPostScript()).thenReturn(s2);
        Mockito.when(sec.getScript()).thenReturn(s2);
        Boolean result = new CheckEligibleTaskDescriptorScript().containsAPIBinding(etd);
        assertEquals(true, result);
    }

    @Test
    public void testNotContainsAPIBindingOK() throws InvalidScriptException {
        Script s = scriptWithoutApiBinding();
        Mockito.when(it.getPreScript()).thenReturn(s);
        Mockito.when(it.getPostScript()).thenReturn(s);
        Mockito.when(sec.getScript()).thenReturn(s);
        Boolean result = new CheckEligibleTaskDescriptorScript().containsAPIBinding(etd);
        assertEquals(false, result);
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

}
