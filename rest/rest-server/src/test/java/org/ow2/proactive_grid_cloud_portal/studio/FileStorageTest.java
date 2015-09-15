package org.ow2.proactive_grid_cloud_portal.studio;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive_grid_cloud_portal.studio.storage.FileStorage;
import org.ow2.proactive_grid_cloud_portal.studio.storage.generators.SmallestAvailableIdGenerator;
import org.ow2.proactive_grid_cloud_portal.studio.storage.serializers.WorkflowSerializer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FileStorageTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private FileStorage<Workflow> storage;

    @Before
    public void setUp() {
        storage = new FileStorage<>(folder.getRoot(), new WorkflowSerializer(),
                new SmallestAvailableIdGenerator());
    }

    @Test
    public void creation_regular_name() throws Exception {
        checkWorkflowOperations(new Workflow("name", "xml", "metadata"));
    }

    @Test
    public void creation_name_with_slashes() throws Exception {
        checkWorkflowOperations(new Workflow("name/with/slashes", "xml", "metadata"));
    }

    @Test
    public void creation_name_with_spaces() throws Exception {
        checkWorkflowOperations(new Workflow("name with spaces", "xml", "metadata"));
    }

    @Test
    public void multiple() throws IOException {
        storage.store(new Workflow("name", "xml", "metadata"));
        storage.store(new Workflow("name", "xml", "metadata"));
        assertNumberOfStoredWorkflows(2);
        assertEquals(new Workflow(1L, "name", "xml", "metadata"), storage.read("1"));
        assertEquals(new Workflow(2L, "name", "xml", "metadata"), storage.read("2"));
        try {
            storage.read("3");
            fail("Should throw IOException");
        } catch (IOException e) {
        }
    }

    @Test
    public void corrupted_no_name() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("Could not find the file");

        storage.store(new Workflow("name", "xml", "metadata"));
        File root = folder.getRoot();
        FileUtils.forceDelete(new File(root, "1/name"));
        storage.readAll();
    }

    @Test
    public void corrupted_no_xml() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("Could not find the file");

        storage.store(new Workflow("name", "xml", "metadata"));
        File root = folder.getRoot();
        FileUtils.forceDelete(new File(root, "1/job.xml"));
        storage.readAll();
    }

    @Test
    public void corrupted_no_metadata() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("Could not find the file");

        storage.store(new Workflow("name", "xml", "metadata"));
        File root = folder.getRoot();
        FileUtils.forceDelete(new File(root, "1/job.xml"));
        storage.readAll();
    }

    @Test
    public void corrupted_no_dir() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("Could not find the file");

        storage.store(new Workflow("name", "xml", "metadata"));
        File root = folder.getRoot();
        FileUtils.forceDelete(root);
        storage.read("1");
    }

    private void checkWorkflowOperations(Workflow workflow) throws IOException {
        storage.store(workflow);

        assertNumberOfStoredWorkflows(1);
        workflow.setId(1L);
        assertEquals(workflow, storage.readAll().get(0));
        assertEquals(workflow, storage.read("1"));

        Workflow deleted = storage.delete("1");

        assertEquals(workflow, deleted);
        assertNumberOfStoredWorkflows(0);
    }

    private void assertNumberOfStoredWorkflows(int size) throws IOException {
        List<Workflow> read = storage.readAll();
        assertEquals(size, read.size());
    }
}