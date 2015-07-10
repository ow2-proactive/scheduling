package org.ow2.proactive.scheduler.task;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;

import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class TaskLauncherInitializerTest {

    private TaskLauncherInitializer initializer;

    @Before
    public void setUp() {
        initializer = new TaskLauncherInitializer();
    }

    @Test
    public void input_files_can_be_filtered() throws Exception {
        initializer.setTaskInputFiles(inputSelectors("$TEST/a", "b"));
        initializer.getTaskInputFiles().get(0).getInputFiles().setExcludes("$TEST/excluded");

        List<InputSelector> filteredInputFiles = initializer.getFilteredInputFiles(
                Collections.<String, Serializable>singletonMap("TEST", "folder"));

        InputSelector selector = filteredInputFiles.get(0);
        assertEquals(ImmutableSet.of("folder/a", "b"), selector.getInputFiles().getIncludes());
        assertEquals(ImmutableSet.of("folder/excluded"), selector.getInputFiles().getExcludes());
    }

    @Test
    public void output_files_can_be_filtered() throws Exception {
        initializer.setTaskOutputFiles(outputSelectors("${TEST}/a", "b"));
        initializer.getTaskOutputFiles().get(0).getOutputFiles().setExcludes("$TEST/excluded");

        List<OutputSelector> filteredOutputFiles = initializer.getFilteredOutputFiles(
                Collections.<String, Serializable>singletonMap("TEST", "folder"));

        OutputSelector selector = filteredOutputFiles.get(0);
        assertEquals(ImmutableSet.of("folder/a", "b"), selector.getOutputFiles().getIncludes());
        assertEquals(ImmutableSet.of("folder/excluded"), selector.getOutputFiles().getExcludes());
    }

    @Test
    public void output_empty_filters() throws Exception {
        initializer.setTaskOutputFiles(outputSelectors("$TEST/a", "b"));

        List<OutputSelector> filteredOutputFiles = initializer.getFilteredOutputFiles(
                Collections.<String, Serializable>emptyMap());

        OutputSelector selector = filteredOutputFiles.get(0);
        assertEquals(ImmutableSet.of("$TEST/a", "b"), selector.getOutputFiles().getIncludes());
    }

    @Test
    public void input_empty_filters() throws Exception {
        initializer.setTaskInputFiles(inputSelectors("$TEST/a", "b"));

        List<InputSelector> filteredInputFiles = initializer.getFilteredInputFiles(
                Collections.<String, Serializable>emptyMap());

        InputSelector selector = filteredInputFiles.get(0);
        assertEquals(ImmutableSet.of("$TEST/a", "b"), selector.getInputFiles().getIncludes());
    }

    @Test
    public void input_null_filters() throws Exception {
        initializer.setTaskInputFiles(inputSelectors("$TEST/a", "b"));

        List<InputSelector> filteredInputFiles = initializer.getFilteredInputFiles(null);

        InputSelector selector = filteredInputFiles.get(0);
        assertEquals(ImmutableSet.of("$TEST/a", "b"), selector.getInputFiles().getIncludes());
    }

    @Test
    public void output_null_filters() throws Exception {
        initializer.setTaskOutputFiles(outputSelectors("$TEST/a", "b"));

        List<OutputSelector> filteredOutputFiles = initializer.getFilteredOutputFiles(null);

        OutputSelector selector = filteredOutputFiles.get(0);
        assertEquals(ImmutableSet.of("$TEST/a", "b"), selector.getOutputFiles().getIncludes());
    }

    private List<InputSelector> inputSelectors(String... selectors) {
        return singletonList(new InputSelector(new FileSelector(selectors),
                InputAccessMode.TransferFromUserSpace));
    }

    private List<OutputSelector> outputSelectors(String... selectors) {
        return singletonList(new OutputSelector(new FileSelector(selectors),
                OutputAccessMode.TransferToUserSpace));
    }
}