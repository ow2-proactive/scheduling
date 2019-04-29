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
package org.ow2.proactive.scheduler.util;

import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.utils.appenders.AsynchChachedFileAppender;
import org.ow2.proactive.utils.appenders.FileAppender;


public class JobLoggerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    File logFolder;

    @BeforeClass
    public static void init() {
        BasicConfigurator.configure();
        // this line is commented, because otherwise
        // JobLoggerTest.{testLoggerAsync, testLoggerSync, testLoggerAsyncWithCache}
        // fails in some mysterious way only for Linux machine
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @AfterClass
    public static void wrapup() {
        PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_ENABLED.updateProperty("true");
        PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED.updateProperty("false");
    }

    @After
    public void clean() {
        Logger.getLogger(JobLogger.class).removeAllAppenders();
    }

    @Test
    public void testGetJobLogFilename() {
        JobId id = new JobIdImpl(1123, "readableName");
        assertThat(JobLogger.getJobLogRelativePath(id), is("1123/1123"));
    }

    @Test
    public void testLoggerAsync() throws IOException {

        PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_ENABLED.updateProperty("true");
        PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED.updateProperty("false");

        logFolder = initLoggers();

        doLogTest(logFolder, false);
    }

    @Test
    public void testLoggerAsyncWithCache() throws IOException {

        PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_ENABLED.updateProperty("true");
        PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED.updateProperty("true");

        File logFolder = initLoggers();

        doLogTestCached(logFolder, true);
    }

    private File initLoggers() throws IOException {
        Logger jobLogger = Logger.getLogger(JobLogger.class);
        FileAppender appender = ServerJobAndTaskLogs.createFileAppender();
        File logFolder = folder.newFolder("logs");
        appender.setFilesLocation(logFolder.getAbsolutePath());
        jobLogger.removeAllAppenders();
        jobLogger.addAppender(appender);
        return logFolder;
    }

    @Test
    public void testLoggerSync() throws IOException {

        PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_ENABLED.updateProperty("false");
        PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED.updateProperty("false");

        File logFolder = initLoggers();

        doLogTest(logFolder, false);
    }

    private void doLogTest(File logFolder, boolean cacheEnabled) throws IOException {
        JobId id1 = new JobIdImpl(112, "readableName");
        JobId id2 = new JobIdImpl(113, "readableName");
        JobId id3 = new JobIdImpl(114, "readableName");
        JobLogger.getInstance().info(id1, "info message");
        JobLogger.getInstance().warn(id2, "warn message");
        JobLogger.getInstance().error(id3, "error message");
        JobLogger.getInstance().close(id1);
        JobLogger.getInstance().close(id2);
        JobLogger.getInstance().close(id3);
        Assert.assertEquals(1, Collections.list(Logger.getLogger(JobLogger.class).getAllAppenders()).size());
        String log1Content = IOUtils.toString(new File(logFolder, JobLogger.getJobLogRelativePath(id1)).toURI(),
                                              defaultCharset());
        String log2Content = IOUtils.toString(new File(logFolder, JobLogger.getJobLogRelativePath(id2)).toURI(),
                                              defaultCharset());
        String log3Content = IOUtils.toString(new File(logFolder, JobLogger.getJobLogRelativePath(id3)).toURI(),
                                              defaultCharset());
        System.out.println("------------------- log1 contents -----------------------");
        System.out.println(log1Content);
        System.out.println("------------------- log2 contents -----------------------");
        System.out.println(log2Content);
        System.out.println("------------------- log3 contents -----------------------");
        System.out.println(log3Content);
        Assert.assertThat(StringUtils.countMatches(log1Content, "info message"), is(1));
        Assert.assertThat(StringUtils.countMatches(log2Content, "warn message"), is(1));
        Assert.assertThat(StringUtils.countMatches(log3Content, "debug message"), is(1));
    }

    private void doLogTestCached(File logFolder, boolean cacheEnabled) throws IOException {
        JobId id1 = new JobIdImpl(112, "readableName");
        JobId id2 = new JobIdImpl(113, "readableName");
        JobId id3 = new JobIdImpl(114, "readableName");
        final Enumeration allAppenders = Logger.getLogger(JobLogger.class).getAllAppenders();
        final ArrayList list = Collections.list(allAppenders);
        assertEquals(1, list.size());
        AsynchChachedFileAppender appender = (AsynchChachedFileAppender) list.get(0);
        assertEquals(0, appender.numberOfAppenders());
        JobLogger.getInstance().info(id1, "info message");
        assertEquals(1, appender.numberOfAppenders());
        JobLogger.getInstance().warn(id2, "warn message");
        assertEquals(2, appender.numberOfAppenders());
        JobLogger.getInstance().error(id3, "error message");
        assertEquals(3, appender.numberOfAppenders());

        JobLogger.getInstance().flush(id1);
        JobLogger.getInstance().flush(id2);
        JobLogger.getInstance().flush(id3);
        Assert.assertEquals(cacheEnabled, appender.doesCacheContain(JobLogger.getJobLogRelativePath(id1)));
        Assert.assertEquals(cacheEnabled, appender.doesCacheContain(JobLogger.getJobLogRelativePath(id2)));
        Assert.assertEquals(cacheEnabled, appender.doesCacheContain(JobLogger.getJobLogRelativePath(id3)));
        JobLogger.getInstance().close(id1);
        JobLogger.getInstance().close(id2);
        JobLogger.getInstance().close(id3);
        Assert.assertFalse(appender.doesCacheContain(JobLogger.getJobLogRelativePath(id1)));
        Assert.assertFalse(appender.doesCacheContain(JobLogger.getJobLogRelativePath(id2)));
        Assert.assertFalse(appender.doesCacheContain(JobLogger.getJobLogRelativePath(id3)));
        Assert.assertEquals(1, Collections.list(Logger.getLogger(JobLogger.class).getAllAppenders()).size());
        String log1Content = IOUtils.toString(new File(logFolder, JobLogger.getJobLogRelativePath(id1)).toURI(),
                                              defaultCharset());
        String log2Content = IOUtils.toString(new File(logFolder, JobLogger.getJobLogRelativePath(id2)).toURI(),
                                              defaultCharset());
        String log3Content = IOUtils.toString(new File(logFolder, JobLogger.getJobLogRelativePath(id3)).toURI(),
                                              defaultCharset());
        System.out.println("------------------- log1 contents -----------------------");
        System.out.println(log1Content);
        System.out.println("------------------- log2 contents -----------------------");
        System.out.println(log2Content);
        System.out.println("------------------- log3 contents -----------------------");
        System.out.println(log3Content);
        Assert.assertThat(StringUtils.countMatches(log1Content, "info message"), is(1));
        Assert.assertThat(StringUtils.countMatches(log2Content, "warn message"), is(1));
        Assert.assertThat(StringUtils.countMatches(log3Content, "debug message"), is(1));
    }

}
