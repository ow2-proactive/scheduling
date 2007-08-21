package org.objectweb.proactive.examples.masterslave;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
import org.objectweb.proactive.extra.masterslave.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterslave.TaskException;
import org.objectweb.proactive.extra.masterslave.tasks.NativeTask;


/**
 * This simple test class is an example on how to launch Native commands using the Master/Slave API
 * The program launches the command "hostname" on a set of remote machines and display the results.
 * @author fviale
 *
 */
public class NativeExample extends AbstractExample {

    /**
     * @param args
     * @throws TaskAlreadySubmittedException
     */
    public static void main(String[] args)
        throws MalformedURLException, TaskAlreadySubmittedException {
        NativeExample instance = new NativeExample();

        //   Getting command line parameters and creating the master (see AbstractExample)
        instance.init(args);

        // Creating the tasks to be solved
        List<SimpleNativeTask> tasks = new ArrayList<SimpleNativeTask>();
        for (int i = 0; i < 20; i++) {
            tasks.add(new SimpleNativeTask("hostname"));
        }

        // Submitting the tasks
        instance.master.solve(tasks);
        Collection<String[]> results = null;

        // Collecting the results
        try {
            results = instance.master.waitAllResults();
        } catch (TaskException e) {
            // We catch user exceptions
            e.printStackTrace();
        }
        for (String[] result : results) {
            for (String line : result) {
                System.out.println(line);
            }
        }

        System.exit(0);
    }

    /**
     * A task executing a native command
     * @author fviale
     *
     */
    public static class SimpleNativeTask extends NativeTask {
        public SimpleNativeTask(String command) {
            super(command);
        }
    }

    @Override
    protected void before_init() {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("NativeExample", command_options);
    }

    @Override
    protected void after_init() {
        // nothing to do
    }
}
