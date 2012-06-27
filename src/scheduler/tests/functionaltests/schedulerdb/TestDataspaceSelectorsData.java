package functionaltests.schedulerdb;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestDataspaceSelectorsData extends BaseSchedulerDBTest {

    @Test
    public void testSelectors() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask task = createDefaultTask("task1");

        String[] inInclude1 = { "inInclude1_1", "inInclude1_2" };
        String[] inExclude1 = { "inExclude1_1", "inExclude1_2" };

        String[] outInclude1 = { "outInclude1_1", "outInclude1_2" };
        String[] outExclude1 = { "outExclude1_1", "outExclude1_2" };

        String[] inInclude2 = { "inInclude" + createString(500) };
        String[] inExclude2 = { "inExclude" + createString(500) };

        String[] outInclude2 = { "outInclude" + createString(500) };
        String[] outExclude2 = { "outExclude" + createString(500) };

        FileSelector fileSelector;

        task.addInputFiles(new FileSelector(inInclude1, inExclude1), InputAccessMode.TransferFromGlobalSpace);
        fileSelector = new FileSelector(inInclude2, inExclude2);
        fileSelector.setCaseSensitive(false);
        task.addInputFiles(fileSelector, InputAccessMode.TransferFromInputSpace);

        task.addOutputFiles(new FileSelector(outInclude1, outExclude1),
                OutputAccessMode.TransferToGlobalSpace);
        fileSelector = new FileSelector(outInclude2, outExclude2);
        fileSelector.setCaseSensitive(false);
        task.addOutputFiles(fileSelector, OutputAccessMode.TransferToGlobalSpace);

        jobDef.addTask(task);

        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobDef);
        InternalTask task1 = job.getTask("task1");
        Assert.assertEquals(2, task1.getInputFilesList().size());
        Assert.assertEquals(InputAccessMode.TransferFromGlobalSpace, task1.getInputFilesList().get(0)
                .getMode());
        Assert.assertEquals(2, task1.getOutputFilesList().size());

        checkSelector(task1.getInputFilesList().get(0).getInputFiles(), inInclude1, inExclude1, true);
        checkSelector(task1.getInputFilesList().get(1).getInputFiles(), inInclude2, inExclude2, false);
        checkSelector(task1.getOutputFilesList().get(0).getOutputFiles(), outInclude1, outExclude1, true);
        checkSelector(task1.getOutputFilesList().get(1).getOutputFiles(), outInclude2, outExclude2, false);
    }

    private void checkSelector(FileSelector selector, String[] inc, String[] exc, boolean cs) {
        Assert.assertArrayEquals(inc, selector.getIncludes());
        Assert.assertArrayEquals(exc, selector.getExcludes());
        Assert.assertEquals(cs, selector.isCaseSensitive());
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
