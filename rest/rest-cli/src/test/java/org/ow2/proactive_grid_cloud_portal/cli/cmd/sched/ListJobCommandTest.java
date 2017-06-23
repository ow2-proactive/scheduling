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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.JobPriority;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobPriorityData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStatusData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestMapPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;


/**
 * Created by zeinebzhioua on 13/06/2017.
 */
public class ListJobCommandTest {

    private ApplicationContext currentContextMock;

    private SchedulerRestClient schedulerRestClientMock;

    private SchedulerRestInterface schedulerRestInterfaceMock;

    private AbstractDevice deviceMock;

    @Before
    public void init() {
        currentContextMock = Mockito.mock(ApplicationContext.class);
        schedulerRestClientMock = Mockito.mock(SchedulerRestClient.class);
        schedulerRestInterfaceMock = Mockito.mock(SchedulerRestInterface.class);
        deviceMock = Mockito.mock(AbstractDevice.class);

        Mockito.when(currentContextMock.getSessionId()).thenReturn("sessionid");
        Mockito.when(currentContextMock.getDevice()).thenReturn(deviceMock);
        Mockito.when(currentContextMock.getRestClient()).thenReturn(schedulerRestClientMock);
        Mockito.when(schedulerRestClientMock.getScheduler()).thenReturn(schedulerRestInterfaceMock);
    }

    @Test
    public void testAllJobsReturned() throws Exception {

        int index = 0;
        int offset = 10;

        List<UserJobData> userJobInfoList = createUserJobInfoList(10);

        RestPage<String> restPage = new RestPage(userJobInfoList, 1);
        Map<Long, List<UserJobData>> stateMap = new HashMap<>();
        stateMap.put(1L, userJobInfoList);
        RestMapPage<Long, ArrayList<UserJobData>> page = new RestMapPage(stateMap, 1);

        Mockito.when(schedulerRestInterfaceMock.jobs("sessionid", index, offset)).thenReturn(restPage);
        Mockito.when(schedulerRestInterfaceMock.revisionAndJobsInfo("sessionid", index, 10, false, true, true, true))
               .thenReturn(page);

        new ListJobCommand("limit=" + offset, "from=" + index).execute(currentContextMock);

        Mockito.verify(schedulerRestInterfaceMock).revisionAndJobsInfo("sessionid", index, 10, false, true, true, true);
    }

    @Test
    public void testZeroJobsReturned() throws Exception {

        int index = 0;
        int offset = 1;

        List<UserJobData> userJobInfoList = createUserJobInfoList(1);

        RestPage<String> restPage = new RestPage(userJobInfoList, 1);
        Map<Long, List<UserJobData>> stateMap = new HashMap<>();
        stateMap.put(1L, userJobInfoList);
        RestMapPage<Long, ArrayList<UserJobData>> page = new RestMapPage(stateMap, 1);

        Mockito.when(schedulerRestInterfaceMock.jobs("sessionid", index, offset)).thenReturn(restPage);
        Mockito.when(schedulerRestInterfaceMock.revisionAndJobsInfo("sessionid", index, 1, false, true, true, true))
               .thenReturn(page);

        new ListJobCommand("limit=" + offset, "from=" + index).execute(currentContextMock);

        Mockito.verify(schedulerRestInterfaceMock).revisionAndJobsInfo("sessionid", index, 1, false, true, true, true);

    }

    @Test
    public void testOneJobReturned() throws Exception {

        int index = 1;
        int offset = 1;

        List<UserJobData> userJobInfoList = createUserJobInfoList(3);

        RestPage<String> restPage = new RestPage(userJobInfoList, 1);
        Map<Long, List<UserJobData>> stateMap = new HashMap<>();
        stateMap.put(1L, userJobInfoList);
        RestMapPage<Long, ArrayList<UserJobData>> page = new RestMapPage(stateMap, 1);

        Mockito.when(schedulerRestInterfaceMock.jobs("sessionid", index, offset)).thenReturn(restPage);
        Mockito.when(schedulerRestInterfaceMock.revisionAndJobsInfo("sessionid", index, 3, false, true, true, true))
               .thenReturn(page);

        new ListJobCommand("limit=" + offset, "from=" + index).execute(currentContextMock);

        Mockito.verify(schedulerRestInterfaceMock).revisionAndJobsInfo("sessionid", index, 3, false, true, true, true);
    }

    @Test
    public void testTwoJobsReturned() throws Exception {

        int index = 0;
        int offset = 2;

        List<UserJobData> userJobInfoList = createUserJobInfoList(10);

        RestPage<String> restPage = new RestPage(userJobInfoList, 1);
        Map<Long, List<UserJobData>> stateMap = new HashMap<>();
        stateMap.put(1L, userJobInfoList);
        RestMapPage<Long, ArrayList<UserJobData>> page = new RestMapPage(stateMap, 1);

        Mockito.when(schedulerRestInterfaceMock.jobs("sessionid", index, offset)).thenReturn(restPage);
        Mockito.when(schedulerRestInterfaceMock.revisionAndJobsInfo("sessionid", index, 10, false, true, true, true))
               .thenReturn(page);

        new ListJobCommand("limit=" + offset, "from=" + index).execute(currentContextMock);

        Mockito.verify(schedulerRestInterfaceMock).revisionAndJobsInfo("sessionid", index, 10, false, true, true, true);

    }

    @Test
    public void testNoArgumentsSoAllJobsReturned() throws Exception {

        int index = 0;
        int offset = -1;

        List<UserJobData> userJobInfoList = createUserJobInfoList(10);

        RestPage<String> restPage = new RestPage(userJobInfoList, 1);
        Map<Long, List<UserJobData>> stateMap = new HashMap<>();
        stateMap.put(1L, userJobInfoList);
        RestMapPage<Long, ArrayList<UserJobData>> page = new RestMapPage(stateMap, 1);

        Mockito.when(schedulerRestInterfaceMock.jobs("sessionid", index, offset)).thenReturn(restPage);
        Mockito.when(schedulerRestInterfaceMock.revisionAndJobsInfo("sessionid", index, 10, false, true, true, true))
               .thenReturn(page);

        new ListJobCommand().execute(currentContextMock);

        Mockito.verify(schedulerRestInterfaceMock).revisionAndJobsInfo("sessionid", index, 10, false, true, true, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongLatestArgument() throws Exception {
        new ListJobCommand("latest=xxx").execute(currentContextMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongFromArgument() throws Exception {
        new ListJobCommand("from=xxx").execute(currentContextMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongLimitArgument() throws Exception {
        new ListJobCommand("limit=xxx").execute(currentContextMock);
    }

    private List<UserJobData> createUserJobInfoList(int jobsNumber) {
        List<UserJobData> userJobInfoList = new ArrayList<UserJobData>(1);
        for (int i = 0; i < jobsNumber; i++) {
            JobInfoData jobInfoData = new JobInfoData();
            JobIdData jobIdData = new JobIdData();
            jobIdData.setId(i);
            jobIdData.setReadableName(i + "name");
            jobInfoData.setJobId(jobIdData);
            jobInfoData.setPriority(JobPriorityData.HIGH);
            jobInfoData.setStatus(JobStatusData.FINISHED);
            jobInfoData.setStartTime(System.currentTimeMillis() - 1000);
            jobInfoData.setFinishedTime(System.currentTimeMillis());
            jobInfoData.setJobOwner("test");
            userJobInfoList.add(new UserJobData(jobInfoData));
        }

        return userJobInfoList;
    }

}
