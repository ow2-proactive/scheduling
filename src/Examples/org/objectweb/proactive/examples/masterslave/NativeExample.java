package org.objectweb.proactive.examples.masterslave;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.extra.masterslave.ProActiveMaster;
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
     */
    public static void main(String[] args) throws MalformedURLException {
        NativeExample instance = new NativeExample();

        // Getting command line parameters
        instance.init(args);

        // Creating the Master
        ProActiveMaster<SimpleNativeTask, String[]> master = new ProActiveMaster<SimpleNativeTask, String[]>(instance.descriptor_url,
                instance.vn_name);

        // Creating the tasks to be solved
        List<SimpleNativeTask> tasks = new ArrayList<SimpleNativeTask>();
        for (int i = 0; i < 20; i++) {
            tasks.add(new SimpleNativeTask("hostname"));
        }

        // Submitting the tasks
        master.solve(tasks);
        Collection<String[]> results = null;

        // Collecting the results
        try {
            results = master.waitAllResults();
        } catch (TaskException e) {
            // We catch user exceptions
            e.printStackTrace();
        }
        for (String[] result : results) {
            for (String line : result) {
                System.out.println(line);
            }
        }

        // Terminating the Master
        master.terminate(true);

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
    protected void init_specialized(String[] args) {
        // nothing to do
    }
}
