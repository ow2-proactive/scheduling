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
package functionaltests.dataspaces;

import static com.google.common.truth.Truth.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.core.DataSpaceServiceStarter;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.TaskDataSpaceApplication;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.data.TaskProActiveDataspaces;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;


/**
 * Test the integration of {@link TaskProActiveDataspaces} with
 * {@link DataSpaceServiceStarter} and {@link TaskDataSpaceApplication}.
 * <p>
 * {@link TaskProActiveDataspaces} is usually used on node side to manage
 * files from the scratch space per Task whereas {@link DataSpaceServiceStarter}
 * and {@link TaskDataSpaceApplication} generally depict the dataspace parts
 * instantiated on server side to respectively create default spaces and to
 * manage the transfer of files per Task from default spaces to nodes.
 * <p>
 * All the classes mentioned previously assume they are used from a ProActive
 * virtual node. It explains why server and node side of the dataspaces are
 * represented in this test by Active Objects. Besides, the server side
 * is deployed in a new JVM to prevent ProActive programming to make some
 * optimizations that could hide problems.
 *
 * @author ActiveEon Team
 */
public class TaskProActiveDataspacesIntegrationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public Timeout globalTimeout = Timeout.seconds(600);

    private Process schedulerDataspaceProcess;

    private SchedulerDataspace schedulerDataspace;

    private Process nodeDataspaceProcess;

    private NodeDataspace nodeDataspace;

    private File globalSpace;

    private File userSpace;

    private File inputSpace;

    private File outputSpace;

    private File scratchSpace;

    @Before
    public void setUp() throws Exception {
        String rootPath = folder.getRoot().getAbsolutePath();
        System.out.println("Scheduler folder for dataspaces is " + rootPath);

        String owner = "bobot";
        JobId jobId = JobIdImpl.makeJobId("42");
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "taskId", 0);

        schedulerDataspaceProcess = spawnNewJvm(SchedulerDataspace.class, rootPath);
        String schedulerDataspaceUrl = readLine(schedulerDataspaceProcess.getInputStream());

        schedulerDataspace = PAActiveObject.lookupActive(SchedulerDataspace.class, schedulerDataspaceUrl);
        schedulerDataspace.init(jobId, taskId, owner);

        String namingServiceUrl = schedulerDataspace.getNamingServiceUrl();

        nodeDataspaceProcess = spawnNewJvm(NodeDataspace.class);
        String nodeDataspaceUrl = readLine(nodeDataspaceProcess.getInputStream());

        nodeDataspace = PAActiveObject.lookupActive(NodeDataspace.class, nodeDataspaceUrl);
        nodeDataspace.init(taskId, namingServiceUrl);

        // prints the path to the scratch folder of the node
        System.out.println(readLine(nodeDataspaceProcess.getInputStream()));

        globalSpace = nodeDataspace.getGlobalSpace();
        userSpace = nodeDataspace.getUserSpace();
        inputSpace = nodeDataspace.getInputSpace();
        outputSpace = nodeDataspace.getOutputSpace();

        scratchSpace = nodeDataspace.getScratchFolder();
    }

    private Process spawnNewJvm(Class<?> clazz, String... params) throws Exception {
        String classpath = System.getProperty("java.class.path");
        String className = clazz.getCanonicalName();

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String javaPolicy = getSystemProperty("java.security.policy");

        String paRmHome = getSystemProperty("pa.rm.home");
        String paSchedulerHome = getSystemProperty("pa.scheduler.home");

        ProcessBuilder builder = new ProcessBuilder(concat(new String[] { javaBin, "-cp", classpath,
                                                                          "-Djava.security.policy=" + javaPolicy,
                                                                          "-Dpa.rm.home=" + paRmHome,
                                                                          "-Dpa.scheduler.home=" + paSchedulerHome,
                                                                          className },
                                                           params));

        return builder.start();
    }

    private String readLine(InputStream iStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(iStream));
        return bufferedReader.readLine();
    }

    private String getSystemProperty(String name) {
        String property = System.getProperty(name);

        if (property == null) {
            throw new IllegalArgumentException("Missing system property: " + name);
        }

        return property;
    }

    private String[] concat(String[] a, String[] b) {
        int l1 = a.length;
        int l2 = b.length;

        String[] result = new String[l1 + l2];

        System.arraycopy(a, 0, result, 0, l1);
        System.arraycopy(b, 0, result, l1, l2);

        return result;
    }

    @After
    public void tearDown() throws Exception {
        PAActiveObject.terminateActiveObject(nodeDataspace, true);
        PAActiveObject.terminateActiveObject(schedulerDataspace, true);

        nodeDataspaceProcess.destroy();
        schedulerDataspaceProcess.destroy();
    }

    @Test
    public void testSimpleCopyFromInputSpacesToScratch() throws Exception {
        createFile(globalSpace, "globalSpace.txt", "globalSpace");
        createFile(userSpace, "userSpace.txt", "userSpace");
        createFile(inputSpace, "inputSpace.txt", "inputSpace");
        createFile(outputSpace, "outputSpace.txt", "outputSpace");

        List<InputSelector> inputSelectors = ImmutableList.of(createInputSelector(InputAccessMode.TransferFromGlobalSpace,
                                                                                  "globalSpace.txt"),
                                                              createInputSelector(InputAccessMode.TransferFromUserSpace,
                                                                                  "userSpace.txt"),
                                                              createInputSelector(InputAccessMode.TransferFromInputSpace,
                                                                                  "inputSpace.txt"),
                                                              createInputSelector(InputAccessMode.TransferFromOutputSpace,
                                                                                  "outputSpace.txt"));

        nodeDataspace.copyInputDataToScratch(inputSelectors);

        assertThat(scratchSpace.list()).hasLength(4);

        assertThat(existsAndMatches(scratchSpace, "globalSpace.txt", "globalSpace")).isTrue();
        assertThat(existsAndMatches(scratchSpace, "userSpace.txt", "userSpace")).isTrue();
        assertThat(existsAndMatches(scratchSpace, "inputSpace.txt", "inputSpace")).isTrue();
        assertThat(existsAndMatches(scratchSpace, "outputSpace.txt", "outputSpace")).isTrue();
    }

    @Test
    public void testSimpleCopyFromScratchToOutputSpaces() throws Exception {
        createFile(scratchSpace, "outputGlobalSpace.txt", "outputGlobalSpace");
        createFile(scratchSpace, "outputUserSpace.txt", "outputUserSpace");
        createFile(scratchSpace, "outputOutputSpace.txt", "outputOutputSpace");

        List<OutputSelector> outputSelectors = ImmutableList.of(createOutputSelector(OutputAccessMode.TransferToGlobalSpace,
                                                                                     "outputGlobalSpace.txt"),
                                                                createOutputSelector(OutputAccessMode.TransferToUserSpace,
                                                                                     "outputUserSpace.txt"),
                                                                createOutputSelector(OutputAccessMode.TransferToOutputSpace,
                                                                                     "outputOutputSpace.txt"));

        nodeDataspace.copyScratchDataToOutput(outputSelectors);

        assertThat(globalSpace.list()).hasLength(1);
        assertThat(existsAndMatches(globalSpace, "outputGlobalSpace.txt", "outputGlobalSpace")).isTrue();

        assertThat(userSpace.list()).hasLength(1);
        assertThat(existsAndMatches(userSpace, "outputUserSpace.txt", "outputUserSpace")).isTrue();

        assertThat(outputSpace.list()).hasLength(1);
        assertThat(existsAndMatches(outputSpace, "outputOutputSpace.txt", "outputOutputSpace")).isTrue();
    }

    @Test
    public void testParallelCopyFromInputSpaceToScratch() throws Exception {
        createFilesForParallelCopy(globalSpace, 10, 10, 100);

        List<InputSelector> inputSelectors = ImmutableList.of(createInputSelector(InputAccessMode.TransferFromGlobalSpace,
                                                                                  "**/*"));

        nodeDataspace.copyInputDataToScratch(inputSelectors);
        assertThat(countFiles(scratchSpace)).isEqualTo(10000);
    }

    @Test
    public void testParallelCopyFromScratchToOutputSpace() throws Exception {
        createFilesForParallelCopy(scratchSpace, 10, 10, 100);

        List<OutputSelector> outputSelectors = ImmutableList.of(createOutputSelector(OutputAccessMode.TransferToOutputSpace,
                                                                                     "**/*"));

        nodeDataspace.copyScratchDataToOutput(outputSelectors);
        assertThat(countFiles(outputSpace)).isEqualTo(10000);
    }

    @Test
    public void testPrecedenceCopyFromInputSpacesToScratch1() throws Exception {
        String filename = createFilesForTestingPrecedence();

        List<InputSelector> inputSelectors = ImmutableList.of(createInputSelector(InputAccessMode.TransferFromUserSpace,
                                                                                  filename),
                                                              createInputSelector(InputAccessMode.TransferFromGlobalSpace,
                                                                                  filename),
                                                              createInputSelector(InputAccessMode.TransferFromOutputSpace,
                                                                                  filename),
                                                              createInputSelector(InputAccessMode.TransferFromInputSpace,
                                                                                  filename));

        testPrecedenceCopyFromInputSpacesToScratch(inputSelectors, filename, "outputSpace");
    }

    @Test
    public void testPrecedenceCopyFromInputSpacesToScratch2() throws Exception {
        String filename = createFilesForTestingPrecedence();

        List<InputSelector> inputSelectors = ImmutableList.of(createInputSelector(InputAccessMode.TransferFromUserSpace,
                                                                                  filename),
                                                              createInputSelector(InputAccessMode.TransferFromInputSpace,
                                                                                  filename),
                                                              createInputSelector(InputAccessMode.TransferFromGlobalSpace,
                                                                                  filename));

        testPrecedenceCopyFromInputSpacesToScratch(inputSelectors, filename, "inputSpace");
    }

    @Test
    public void testPrecedenceCopyFromInputSpacesToScratch3() throws Exception {
        String filename = createFilesForTestingPrecedence();

        List<InputSelector> inputSelectors = ImmutableList.of(createInputSelector(InputAccessMode.TransferFromUserSpace,
                                                                                  filename),
                                                              createInputSelector(InputAccessMode.TransferFromGlobalSpace,
                                                                                  filename));

        testPrecedenceCopyFromInputSpacesToScratch(inputSelectors, filename, "userSpace");
    }

    private void createFilesForParallelCopy(File root, int breadth, int depth, int nbFilesPerLevel)
            throws IOException, InterruptedException {
        File subpath;

        for (int b = 0; b < breadth; b++) {
            subpath = new File(root, "breadth" + b);

            for (int d = 0; d < depth; d++) {
                subpath = new File(subpath, "depth" + d);

                for (int i = 0; i < nbFilesPerLevel; i++) {
                    Files.createDirectories(subpath.toPath());
                    File file = new File(subpath, "file" + i);
                    Files.createFile(file.toPath());
                }
            }
        }
    }

    private int countFiles(File directory) {
        int count = 0;

        File[] files = directory.listFiles();

        if (files == null) {
            return 0;
        }

        for (File file : files) {
            if (file.isFile()) {
                count++;
            }

            if (file.isDirectory()) {
                count += countFiles(file);
            }
        }

        return count;
    }

    private String createFilesForTestingPrecedence() throws Exception {
        String filename = "test.txt";

        createFile(globalSpace, filename, "globalSpace");
        createFile(userSpace, filename, "userSpace");
        createFile(inputSpace, filename, "inputSpace");
        createFile(outputSpace, filename, "outputSpace");

        return filename;
    }

    private void testPrecedenceCopyFromInputSpacesToScratch(List<InputSelector> inputSelectors, String filename,
            String expectedContent) throws Exception {

        nodeDataspace.copyInputDataToScratch(inputSelectors);

        assertThat(scratchSpace.list()).hasLength(1);

        assertThat(existsAndMatches(scratchSpace, filename, expectedContent)).isTrue();
    }

    private void createFile(File space, String relativePath, String content) throws Exception {
        createFile(space, relativePath, Optional.of(content));
    }

    private void createFile(File space, String relativePath, Optional<String> content) throws Exception {
        File file = new File(space, relativePath);
        Files.createFile(file.toPath());

        if (content.isPresent()) {
            writeFile(file, content.get());
        }
    }

    private void writeFile(File file, String fileContent) throws Exception {
        writeFile(file, fileContent, Charset.defaultCharset());
    }

    private void writeFile(File file, String fileContent, Charset charset) throws Exception {
        Files.write(file.toPath(), fileContent.getBytes(charset), StandardOpenOption.CREATE);
    }

    private InputSelector createInputSelector(InputAccessMode inputAccessMode, String... includes) {
        org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector fileSelector = new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector(includes);

        return new InputSelector(fileSelector, inputAccessMode);
    }

    private OutputSelector createOutputSelector(OutputAccessMode outputAccessMode, String... includes) {
        org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector fileSelector = new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector(includes);

        return new OutputSelector(fileSelector, outputAccessMode);
    }

    private boolean existsAndMatches(File space, String relativePath, String expectedContent) throws IOException {
        return existsAndMatches(space, relativePath, Optional.of(expectedContent));
    }

    private boolean existsAndMatches(File space, String relativePath, Optional<String> expectedContent)
            throws IOException {
        File file = new File(space, relativePath);

        if (expectedContent.isPresent()) {
            String content = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

            return expectedContent.get().equals(content) && file.exists();
        }

        return file.exists();
    }

}
