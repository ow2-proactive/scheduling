package functionaltests.schedulerdb;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import java.util.Set;


public class TestDataspaceSelectorsData extends BaseSchedulerDBTest {

    @Test
    public void testSelectors() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask task = createDefaultTask("task1");

        Set<String> inInclude1 = Sets.newHashSet("inInclude1_1", "inInclude1_2");
        Set<String> inExclude1 = Sets.newHashSet("inExclude1_1", "inExclude1_2");

        Set<String> outInclude1 = Sets.newHashSet("outInclude1_1", "outInclude1_2");
        Set<String> outExclude1 = Sets.newHashSet("outExclude1_1", "outExclude1_2");

        Set<String> inInclude2 = Sets.newHashSet("inInclude" + createString(500));
        Set<String> inExclude2 = Sets.newHashSet("inExclude" + createString(500));

        Set<String> outInclude2 = Sets.newHashSet("outInclude" + createString(500));
        Set<String> outExclude2 = Sets.newHashSet("outExclude" + createString(500));

        FileSelector fileSelector;

        task.addInputFiles(new FileSelector(inInclude1, inExclude1), InputAccessMode.TransferFromGlobalSpace);
        task.addInputFiles(new FileSelector(inInclude1, inExclude1), InputAccessMode.TransferFromUserSpace);
        fileSelector = new FileSelector(inInclude2, inExclude2);
        task.addInputFiles(fileSelector, InputAccessMode.TransferFromInputSpace);

        task.addOutputFiles(new FileSelector(outInclude1, outExclude1),
                OutputAccessMode.TransferToGlobalSpace);
        task.addOutputFiles(new FileSelector(outInclude1, outExclude1), OutputAccessMode.TransferToUserSpace);
        fileSelector = new FileSelector(outInclude2, outExclude2);
        task.addOutputFiles(fileSelector, OutputAccessMode.TransferToOutputSpace);

        jobDef.addTask(task);

        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobDef);
        InternalTask task1 = job.getTask("task1");
        Assert.assertEquals(3, task1.getInputFilesList().size());
        Assert.assertEquals(InputAccessMode.TransferFromGlobalSpace, task1.getInputFilesList().get(0)
                .getMode());
        Assert
                .assertEquals(InputAccessMode.TransferFromUserSpace, task1.getInputFilesList().get(1)
                        .getMode());
        Assert.assertEquals(3, task1.getOutputFilesList().size());

        checkSelector(task1.getInputFilesList().get(0).getInputFiles(), inInclude1, inExclude1, true);
        checkSelector(task1.getInputFilesList().get(1).getInputFiles(), inInclude1, inExclude1, true);
        checkSelector(task1.getInputFilesList().get(2).getInputFiles(), inInclude2, inExclude2, false);
        checkSelector(task1.getOutputFilesList().get(0).getOutputFiles(), outInclude1, outExclude1, true);
        checkSelector(task1.getOutputFilesList().get(1).getOutputFiles(), outInclude1, outExclude1, true);
        checkSelector(task1.getOutputFilesList().get(2).getOutputFiles(), outInclude2, outExclude2, false);
    }

    private void checkSelector(FileSelector selector, Set<String> inc, Set<String> exc, boolean cs) {
        Assert.assertEquals(inc, selector.getIncludes());
        Assert.assertEquals(exc, selector.getExcludes());
    }

    @Test
    public void testEmptyFileSelector() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask task = createDefaultTask("task1");
        task.addInputFiles(new FileSelector(), InputAccessMode.none);
        task.addOutputFiles(new FileSelector(), OutputAccessMode.none);
        jobDef.addTask(task);

        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobDef);
        InternalTask task1 = job.getTask("task1");
        Assert.assertEquals(1, task1.getInputFilesList().size());
        Assert.assertEquals(InputAccessMode.none, task1.getInputFilesList().get(0).getMode());
        Assert.assertEquals(1, task1.getOutputFilesList().size());
        Assert.assertEquals(OutputAccessMode.none, task1.getOutputFilesList().get(0).getMode());
    }

}
