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
package org.ow2.proactive.scheduler.task.data;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.tests.ProActiveTestClean;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;


public class TaskProActiveDataspacesTest extends ProActiveTestClean {

    @Test(expected = Exception.class)
    public void testCopyEmptyListFromInputDataToScratchThrown() throws Exception {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);

        TaskProActiveDataspaces dataspaces = new TaskProActiveDataspaces(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"),
                                                                                                 "job",
                                                                                                 1000L),
                                                                         null,
                                                                         false);

        dataspaces.copyInputDataToScratch(Collections.<InputSelector> emptyList());
    }

    @Test
    public void testCreateFolderHierarchySequentially() throws Exception {
        CreateFolderHierarchySequentiallyFixture fixture = new CreateFolderHierarchySequentiallyFixture(10);

        fixture.test();

        verify(fixture.taskProActiveDataspaces,
               times(fixture.nbFiles)).createFolderHierarchy(Mockito.anyBoolean(),
                                                             Mockito.<DataSpacesFileObject> anyObject(),
                                                             Mockito.<DataSpacesFileObject> anyObject());

        int halfNbFiles = fixture.nbFiles / 2;
        verify(fixture.spaceFileWithFolderType, times(halfNbFiles)).getType();
        verify(fixture.spaceFileWithFolderType, times(halfNbFiles)).getType();
        verify(fixture.target, times(halfNbFiles)).getParent();
        verify(fixture.target, times(fixture.nbFiles)).createFolder();

        assertThat(fixture.filesToCopy).hasSize(1);
        verify(fixture.filesToCopy, times(fixture.nbFiles)).put(anyString(),
                                                                Mockito.<DataSpacesFileObject> anyObject());
    }

    @Test
    public void testCreateFolderHierarchySequentiallyDisabled() throws Exception {
        System.setProperty(TaskProActiveDataspaces.PA_NODE_DATASPACE_CREATE_FOLDER_HIERARCHY_SEQUENTIALLY, "fAlSe");

        CreateFolderHierarchySequentiallyFixture fixture = new CreateFolderHierarchySequentiallyFixture(10);

        fixture.test();

        verify(fixture.spaceFileWithFolderType, never()).getType();
        verify(fixture.spaceFileWithFolderType, never()).getType();
        verify(fixture.target, never()).getParent();

        verify(fixture.taskProActiveDataspaces,
               Mockito.never()).createFolderHierarchy(Mockito.anyBoolean(),
                                                      Mockito.<DataSpacesFileObject> anyObject(),
                                                      Mockito.<DataSpacesFileObject> anyObject());

        assertThat(fixture.filesToCopy).hasSize(1);
        verify(fixture.filesToCopy, times(fixture.nbFiles)).put(anyString(),
                                                                Mockito.<DataSpacesFileObject> anyObject());
    }

    private static final class CreateFolderHierarchySequentiallyFixture {

        DataSpacesFileObject target;

        DataSpacesFileObject destination;

        String spaceUri = "mySpace";

        List<DataSpacesFileObject> spaceFiles;

        DataSpacesFileObject spaceFileWithFolderType;

        DataSpacesFileObject spaceFileWithFileType;

        Map<String, DataSpacesFileObject> filesToCopy;

        TaskProActiveDataspaces taskProActiveDataspaces;

        int nbFiles;

        public CreateFolderHierarchySequentiallyFixture(int nbFiles) throws FileSystemException {
            target = Mockito.mock(DataSpacesFileObject.class);
            doReturn(target).when(target).getParent();

            destination = Mockito.mock(DataSpacesFileObject.class);
            doReturn(target).when(destination).resolveFile(Mockito.anyString());

            spaceFiles = new ArrayList<>(nbFiles);
            filesToCopy = Mockito.spy(new HashMap<String, DataSpacesFileObject>());

            spaceFileWithFolderType = Mockito.mock(DataSpacesFileObject.class);
            doReturn(org.objectweb.proactive.extensions.dataspaces.api.FileType.FOLDER).when(spaceFileWithFolderType)
                                                                                       .getType();

            spaceFileWithFileType = Mockito.mock(DataSpacesFileObject.class);
            doReturn(org.objectweb.proactive.extensions.dataspaces.api.FileType.FILE).when(spaceFileWithFileType)
                                                                                     .getType();

            for (int i = 0; i < nbFiles; i++) {
                if (i % 2 == 0) {
                    spaceFiles.add(spaceFileWithFolderType);
                } else {
                    spaceFiles.add(spaceFileWithFileType);
                }
            }

            taskProActiveDataspaces = Mockito.spy(new TaskProActiveDataspaces());

            doReturn("relativizedValue").when(taskProActiveDataspaces)
                                        .relativize(Mockito.anyString(), Mockito.<DataSpacesFileObject> anyObject());

            this.nbFiles = nbFiles;
        }

        public void test() throws FileSystemException {
            taskProActiveDataspaces.createFolderHierarchySequentially(destination, spaceUri, spaceFiles, filesToCopy);
        }

    }

    @Test
    public void testGetFileTransferThreadPoolSizeDefaultValue() {
        int fileTransferThreadPoolSize = testGetFileTransferThreadPoolSize(Optional.<String> absent());

        assertThat(fileTransferThreadPoolSize).isEqualTo(Runtime.getRuntime().availableProcessors() * 5);
    }

    @Test
    public void testGetFileTransferThreadPoolSizeInvalidPropertyValue() {
        int fileTransferThreadPoolSize = testGetFileTransferThreadPoolSize(Optional.of("oops"));

        assertThat(fileTransferThreadPoolSize).isEqualTo(Runtime.getRuntime().availableProcessors() * 5);
    }

    @Test
    public void testGetFileTransferThreadPoolSizeValidPropertyValue() {
        int fileTransferThreadPoolSize = testGetFileTransferThreadPoolSize(Optional.of("42"));

        assertThat(fileTransferThreadPoolSize).isEqualTo(42);
    }

    private int testGetFileTransferThreadPoolSize(Optional<String> propertyValue) {
        if (propertyValue.isPresent()) {
            System.setProperty(TaskProActiveDataspaces.PA_NODE_DATASPACE_FILE_TRANSFER_THREAD_POOL_SIZE,
                               propertyValue.get());
        }

        TaskProActiveDataspaces taskProActiveDataspaces = new TaskProActiveDataspaces();
        return taskProActiveDataspaces.getFileTransferThreadPoolSize();
    }

    @Test
    public void testHandleResults() throws FileSystemException, ExecutionException, InterruptedException {
        Future<Boolean> mock = Mockito.mock(Future.class);

        int nbFutures = 10;
        ImmutableList.Builder<Future<Boolean>> builder = ImmutableList.builder();

        for (int i = 0; i < nbFutures; i++) {
            builder.add(mock);
        }

        TaskProActiveDataspaces taskProActiveDataspaces = new TaskProActiveDataspaces();
        taskProActiveDataspaces.handleResultsWhileTransferringFile(builder.build(), "SPACE", 0);

        verify(mock, times(nbFutures)).get();
    }

    @Test
    public void testIsCreateFolderHierarchySequentiallyEnabled1() {
        boolean isEnabled = testIsCreateFolderHierarchySequentiallyEnabled(Optional.<String> absent());

        assertThat(isEnabled).isTrue();
    }

    @Test
    public void testIsCreateFolderHierarchySequentiallyEnabled2() {
        boolean isEnabled = testIsCreateFolderHierarchySequentiallyEnabled(Optional.of("true"));

        assertThat(isEnabled).isTrue();
    }

    @Test
    public void testIsCreateFolderHierarchySequentiallyEnabled3() {
        boolean isEnabled = testIsCreateFolderHierarchySequentiallyEnabled(Optional.of("TrUe"));

        assertThat(isEnabled).isTrue();
    }

    @Test
    public void testIsCreateFolderHierarchySequentiallyEnabled4() {
        boolean isEnabled = testIsCreateFolderHierarchySequentiallyEnabled(Optional.of("false"));

        assertThat(isEnabled).isFalse();
    }

    @Test
    public void testIsCreateFolderHierarchySequentiallyEnabled5() {
        boolean isEnabled = testIsCreateFolderHierarchySequentiallyEnabled(Optional.of("FaLsE"));

        assertThat(isEnabled).isFalse();
    }

    private boolean testIsCreateFolderHierarchySequentiallyEnabled(Optional<String> propertyValue) {
        if (propertyValue.isPresent()) {
            System.setProperty(TaskProActiveDataspaces.PA_NODE_DATASPACE_CREATE_FOLDER_HIERARCHY_SEQUENTIALLY,
                               propertyValue.get());
        }

        TaskProActiveDataspaces taskProActiveDataspaces = new TaskProActiveDataspaces();

        return taskProActiveDataspaces.isCreateFolderHierarchySequentiallyEnabled();
    }

}
