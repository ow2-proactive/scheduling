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
package unitTests;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.core.db.DatabaseManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.SimpleScript;

import functionaltests.SchedulerTHelper;


/**
 * This class will test every String[] that goes in database.
 * It will check that String[] of more than 255 char length are well saved in database.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class TestDatabaseStringArray {

    private static String functionalTestSchedulerProperties = SchedulerTHelper.class.getResource(
            "config/functionalTSchedulerProperties.ini").getPath();

    @Before
    public void before() throws Exception {
        PASchedulerProperties.updateProperties(functionalTestSchedulerProperties);
        //build hibernate session
        DatabaseManager.getInstance().build();
    }

    @Test
    public void run() throws Throwable {
        String URLbegin = System.getProperty("pa.scheduler.home") + "/";
        String[] sa = new String[3];
        for (int k = 0; k < sa.length; k++) {
            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < 150; i++) {
                sb.append("a");
            }
            sa[k] = sb.toString();
        }
        SimpleScript ss = new SimpleScript(new File(URLbegin + "samples/scripts/misc/set.js"), sa);
        DatabaseManager.getInstance().register(ss);
        JobEnvironment je = new JobEnvironment();
        je.setJobClasspath(sa);
        DatabaseManager.getInstance().register(je);
        GenerationScript gs = new GenerationScript(URLbegin + "samples/scripts/misc/set.js", "js");
        NativeExecutableContainer nec = new NativeExecutableContainer(sa, gs, URLbegin);
        DatabaseManager.getInstance().register(nec);
        TaskResultImpl tri = new TaskResultImpl();
        tri.setJobClasspath(sa);
        DatabaseManager.getInstance().register(tri);
        //if no exception occurs, test is OK
    }

    @After
    public void after() throws Exception {
        DatabaseManager.getInstance().close();
    }

}
