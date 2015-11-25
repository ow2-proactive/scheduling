package org.ow2.proactive.scheduler.core.db;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.db.TransactionHelper.SessionWork;


public class TaskDBUtilsTest {

    private Session sessionMock;
    private Criteria criteriaMock;
    private List<TaskData> expectedTaskDataList;

    /* Default values from the REST interface */
    private String tag = null;
    private long from = 0L;
    private long to = 0L;
    private int offset = 0;
    private int limit = 50;
    private String user = null;
    private boolean pending = true;
    private boolean running = true;
    private boolean finished = true;

    @Before
    public void init() {
        int nbItems = 10;
        expectedTaskDataList = new ArrayList<TaskData>(nbItems);
        for (int i = 0; i < nbItems; i++) {
            TaskData taskDataMock = Mockito.mock(TaskData.class);
            Mockito.when(taskDataMock.toTaskInfo()).thenReturn(Mockito.mock(TaskInfo.class));
            Mockito.when(taskDataMock.toTaskState()).thenReturn(Mockito.mock(TaskState.class));
            expectedTaskDataList.add(taskDataMock);
        }
        sessionMock = Mockito.mock(Session.class);
        criteriaMock = Mockito.mock(Criteria.class);
        Mockito.when(sessionMock.createCriteria(TaskData.class)).thenReturn(criteriaMock);
        Mockito.when(criteriaMock.createAlias("jobData", "job")).thenReturn(criteriaMock);
        Mockito.when(criteriaMock.list()).thenReturn(expectedTaskDataList);
    }

    @Test
    public void testTaskStateSessionWorkDefaultValues() {
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskState>> sessionWork = TaskDBUtils.taskStateSessionWork(params);
        List<TaskState> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskStates(actual);
    }

    @Test
    public void testTaskStateSessionWorkWithUserName() {
        user = "toto";
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskState>> sessionWork = TaskDBUtils.taskStateSessionWork(params);
        List<TaskState> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskStates(actual);
    }

    @Test
    public void testTaskStateSessionWorkWithDates() {
        from = 1L;
        to = 2L;
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskState>> sessionWork = TaskDBUtils.taskStateSessionWork(params);
        List<TaskState> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskStates(actual);
    }

    @Test
    public void testTaskStateSessionWorkWithTag() {
        tag = "TAG-TEST";
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskState>> sessionWork = TaskDBUtils.taskStateSessionWork(params);
        List<TaskState> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskStates(actual);
    }

    @Test
    public void testTaskStateSessionWorkNoStatuses() {
        pending = false;
        running = false;
        finished = false;
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskState>> sessionWork = TaskDBUtils.taskStateSessionWork(params);
        List<TaskState> actual = sessionWork.executeWork(sessionMock);
        assertThat(actual.size(), is(0));
    }

    @Test
    public void testTaskStateSessionWorkWithPagination() {
        offset = 10;
        limit = 1000;
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskState>> sessionWork = TaskDBUtils.taskStateSessionWork(params);
        List<TaskState> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskStates(actual);
    }

    @Test
    public void testTaskInfoSessionWorkDefaultValues() {
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskInfo>> sessionWork = TaskDBUtils.taskInfoSessionWork(params);
        List<TaskInfo> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskInfos(actual);
    }

    @Test
    public void testTaskInfoSessionWorkWithUserName() {
        user = "toto";
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskInfo>> sessionWork = TaskDBUtils.taskInfoSessionWork(params);
        List<TaskInfo> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskInfos(actual);
    }

    @Test
    public void testTaskInfoSessionWorkWithDates() {
        from = 1L;
        to = 2L;
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskInfo>> sessionWork = TaskDBUtils.taskInfoSessionWork(params);
        List<TaskInfo> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskInfos(actual);
    }

    @Test
    public void testTaskInfoSessionWorkWithTag() {
        tag = "TASKINFO-TAG-TEST";
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskInfo>> sessionWork = TaskDBUtils.taskInfoSessionWork(params);
        List<TaskInfo> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskInfos(actual);
    }

    @Test
    public void testTaskInfoSessionWorkNoStatuses() {
        pending = false;
        running = false;
        finished = false;
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskInfo>> sessionWork = TaskDBUtils.taskInfoSessionWork(params);
        List<TaskInfo> actual = sessionWork.executeWork(sessionMock);
        assertThat(actual.size(), is(0));
    }

    @Test
    public void testTaskInfoSessionWorkWithPagination() {
        offset = 50;
        limit = 1000;
        DBTaskDataParameters params = new DBTaskDataParameters(tag, from, to, offset, limit, user, pending,
            running, finished, null);
        SessionWork<List<TaskInfo>> sessionWork = TaskDBUtils.taskInfoSessionWork(params);
        List<TaskInfo> actual = sessionWork.executeWork(sessionMock);
        verifyParameters(params);
        assertListTaskInfos(actual);
    }

    private void verifyParameters(DBTaskDataParameters params) {

        int createAliasCounter = 1;
        Mockito.verify(sessionMock, Mockito.times(1)).createCriteria(TaskData.class);
        Mockito.verify(criteriaMock, Mockito.times(1)).add(match(Restrictions.eq("job.removedTime", -1L)));

        if (params.getOffset() >= 0)
            Mockito.verify(criteriaMock, Mockito.times(1)).setFirstResult(params.getOffset());

        if (params.getLimit() > 0)
            Mockito.verify(criteriaMock, Mockito.times(1)).setMaxResults(params.getLimit());

        if (params.getUser() != null && "".compareTo(params.getUser()) != 0) {
            createAliasCounter++;
            Mockito.verify(criteriaMock, Mockito.times(1))
                    .add(match(Restrictions.eq("job.owner", params.getUser())));
        }

        if (params.getFrom() > 0)
            Mockito.verify(criteriaMock, Mockito.times(1))
                    .add(match(Restrictions.ge("startTime", params.getFrom())));

        if (params.getTo() > 0)
            Mockito.verify(criteriaMock, Mockito.times(1))
                    .add(match(Restrictions.le("finishedTime", params.getTo())));

        if (params.getTag() != null && "".compareTo(params.getTag()) != 0)
            Mockito.verify(criteriaMock, Mockito.times(1))
                    .add(match(Restrictions.eq("tag", params.getTag())));

        Mockito.verify(criteriaMock, Mockito.times(createAliasCounter)).createAlias("jobData", "job");
    }

    private void assertListTaskStates(List<TaskState> actualList) {
        assertThat(actualList.size(), is(expectedTaskDataList.size()));
        for (int i = 0; i < expectedTaskDataList.size(); i++) {
            TaskState expectedTaskState = expectedTaskDataList.get(i).toTaskState();
            assertThat(actualList.get(i), is(expectedTaskState));
        }
    }

    private void assertListTaskInfos(List<TaskInfo> actualList) {
        assertThat(actualList.size(), is(expectedTaskDataList.size()));
        for (int i = 0; i < expectedTaskDataList.size(); i++) {
            TaskInfo expectedTaskInfo = expectedTaskDataList.get(i).toTaskInfo();
            assertThat(actualList.get(i), is(expectedTaskInfo));
        }
    }

    private SimpleExpression match(SimpleExpression simpleExpression) {
        return Mockito.argThat(new SimpleExpressionMatcher(simpleExpression));
    }

    private class SimpleExpressionMatcher extends ArgumentMatcher<SimpleExpression> {
        private SimpleExpression simpleExpression;

        public SimpleExpressionMatcher(SimpleExpression simpleExpression) {
            this.simpleExpression = simpleExpression;
        }

        @Override
        public boolean matches(Object argument) {
            SimpleExpression otherSimpleExpression = null;
            try {
                otherSimpleExpression = (SimpleExpression) argument;
            } catch (Exception e) {
                return false;
            }
            return this.simpleExpression.toString().equals(otherSimpleExpression.toString());
        }
    }
}
