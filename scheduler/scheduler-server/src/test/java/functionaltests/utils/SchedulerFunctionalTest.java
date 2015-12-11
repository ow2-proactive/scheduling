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
package functionaltests.utils;

import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.tests.ProActiveTest;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;


/**
 * 
 * The parent class for all consecutive functional tests.
 *
 */
public class SchedulerFunctionalTest extends ProActiveTest {

    protected static final Logger logger = Logger.getLogger("SchedulerTests");

    protected SchedulerTHelper schedulerHelper;

    protected JobFactory xmlParser = JobFactory.getFactory();

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue(),
        TimeUnit.MILLISECONDS);

    @Before
    public void startSchedulerIfNeeded() throws Exception {
        schedulerHelper = new SchedulerTHelper();
    }

    @After
    public void killAllProcessesIfNeeded() throws Exception {
        schedulerHelper.removeExtraNodes();

        try {
            schedulerHelper.disconnect(); // in case user has changed during test
        } catch (NotConnectedException alreadyDisconnected) {
        }
    }

    protected Job parseXml(String workflowFile) throws JobCreationException {
        return xmlParser.createJob(getClass().getResource(workflowFile).getPath());
    }

}
