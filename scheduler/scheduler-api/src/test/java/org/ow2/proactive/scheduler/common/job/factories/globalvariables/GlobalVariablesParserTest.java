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
package org.ow2.proactive.scheduler.common.job.factories.globalvariables;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * @author ActiveEon Team
 * @since 13/07/2021
 */
public class GlobalVariablesParserTest {

    @BeforeClass
    public static void beforeClass() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    @AfterClass
    public static void afterClass() {
        GlobalVariablesParser.setConfigurationPath(GlobalVariablesParserTest.class.getResource("global_variables_default.xml")
                                                                                  .toExternalForm());
        GlobalVariablesParser.getInstance().reloadFilters();
    }

    @Test
    public void testParseDefaultConfig() throws IOException {
        GlobalVariablesParser.setConfigurationPath(GlobalVariablesParserTest.class.getResource("global_variables_default.xml")
                                                                                  .toExternalForm());
        GlobalVariablesParser.getInstance().reloadFilters();
        List<Filter> filters = GlobalVariablesParser.getInstance().getLoadedFilters();
        Assert.assertEquals(1, filters.size());
        Assert.assertEquals(1, filters.get(0).getXpath().size());
        Assert.assertEquals(".", filters.get(0).getXpath().get(0));
        Assert.assertEquals(0, filters.get(0).getVariables().size());
        String md5First = GlobalVariablesParser.getInstance().getMD5();
        GlobalVariablesParser.getInstance().reloadFilters();
        String md5Second = GlobalVariablesParser.getInstance().getMD5();
        Assert.assertEquals("MD5 should be equals after reload", md5First, md5Second);
    }

    @Test
    public void testParseConfigTwoVars() throws IOException {
        GlobalVariablesParser.setConfigurationPath(GlobalVariablesParserTest.class.getResource("global_variables_two_vars.xml")
                                                                                  .toExternalForm());
        GlobalVariablesParser.getInstance().reloadFilters();
        List<Filter> filters = GlobalVariablesParser.getInstance().getLoadedFilters();
        Assert.assertEquals(1, filters.size());
        Assert.assertEquals(1, filters.get(0).getXpath().size());
        Assert.assertEquals(".", filters.get(0).getXpath().get(0));
        Assert.assertEquals(2, filters.get(0).getVariables().size());
        Assert.assertEquals(new JobVariable("var1", "value1", "model1"), filters.get(0).getVariables().get(0));
        Assert.assertEquals(new JobVariable("var2", "value2", "model2"), filters.get(0).getVariables().get(1));
        GlobalVariablesData globalData = GlobalVariablesParser.getInstance()
                                                              .getVariablesFor(IOUtils.toString(GlobalVariablesParserTest.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_no_variables.xml"),
                                                                                                PASchedulerProperties.FILE_ENCODING.getValueAsString()));
        ;
        Map<String, JobVariable> globalVariables = globalData.getVariables();
        Assert.assertEquals(2, globalVariables.size());
        Assert.assertEquals(new JobVariable("var1", "value1", "model1"), globalVariables.get("var1"));
        Assert.assertEquals(new JobVariable("var2", "value2", "model2"), globalVariables.get("var2"));
        Map<String, String> globalGI = globalData.getGenericInformation();
        Assert.assertEquals(2, globalGI.size());
        Assert.assertEquals("gi_value1", globalGI.get("gi1"));
        Assert.assertEquals("gi_value2", globalGI.get("gi2"));
    }

    @Test
    public void testParseConfigOneFilterOK() throws IOException {
        GlobalVariablesParser.setConfigurationPath(GlobalVariablesParserTest.class.getResource("global_variables_one_filter.xml")
                                                                                  .toExternalForm());
        GlobalVariablesParser.getInstance().reloadFilters();
        List<Filter> filters = GlobalVariablesParser.getInstance().getLoadedFilters();
        Assert.assertEquals(1, filters.size());
        Assert.assertEquals(2, filters.get(0).getXpath().size());
        Assert.assertEquals(2, filters.get(0).getVariables().size());

        GlobalVariablesData globalData = GlobalVariablesParser.getInstance()
                                                              .getVariablesFor(IOUtils.toString(GlobalVariablesParserTest.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_with_global_variables_and_gi.xml"),
                                                                                                PASchedulerProperties.FILE_ENCODING.getValueAsString()));
        Map<String, JobVariable> globalVariables = globalData.getVariables();
        // As this job matches the xpath filters, it will return the configured variables
        Assert.assertEquals(2, globalVariables.size());
        Assert.assertEquals(new JobVariable("var1", "value1", "model1"), globalVariables.get("var1"));
        Assert.assertEquals(new JobVariable("var2", "value2", "model2"), globalVariables.get("var2"));
        Map<String, String> globalGI = globalData.getGenericInformation();
        Assert.assertEquals(2, globalGI.size());
        Assert.assertEquals("gi_value1", globalGI.get("gi1"));
        Assert.assertEquals("gi_value2", globalGI.get("gi2"));
    }

    @Test
    public void testParseConfigOneFilterKO() throws IOException {
        GlobalVariablesParser.setConfigurationPath(GlobalVariablesParserTest.class.getResource("global_variables_one_filter.xml")
                                                                                  .toExternalForm());
        GlobalVariablesParser.getInstance().reloadFilters();
        List<Filter> filters = GlobalVariablesParser.getInstance().getLoadedFilters();
        Assert.assertEquals(1, filters.size());
        Assert.assertEquals(2, filters.get(0).getXpath().size());
        Assert.assertEquals(2, filters.get(0).getVariables().size());

        GlobalVariablesData globalData = GlobalVariablesParser.getInstance()
                                                              .getVariablesFor(IOUtils.toString(GlobalVariablesParserTest.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_variables_order.xml"),
                                                                                                PASchedulerProperties.FILE_ENCODING.getValueAsString()));
        Map<String, JobVariable> globalVariables = globalData.getVariables();
        // As this job does not match the xpath filters, it will return no variables
        Assert.assertEquals(0, globalVariables.size());
    }
}
