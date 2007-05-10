package org.objectweb.proactive.examples.masterslave;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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
public class TestNative extends NativeTask {
    public static URL descriptor_url;
    public static String vn_name;

    /**
     * A task executing a native command
     * @param command
     */
    public TestNative(String command) {
        super(command);
    }

    /**
     * Initializing the example with command line arguments
     * @param args
     * @throws MalformedURLException
     */
    public static void init(String[] args) throws MalformedURLException {
        if (args.length == 0) {
            descriptor_url = (new File(args[0])).toURI().toURL();
            vn_name = args[1];
        } else if (args.length == 2) {
            descriptor_url = (new File(args[0])).toURI().toURL();
            vn_name = args[1];
        } else {
            System.out.println(
                "Usage: <java_command> descriptor_path virtual_node_name");
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws MalformedURLException {
        // Getting command line parameters
        init(args);

        // Creating the Master
        ProActiveMaster master = new ProActiveMaster(descriptor_url, vn_name);

        // Creating the tasks to be solved
        List<TestNative> tasks = new ArrayList<TestNative>();
        for (int i = 0; i < 20; i++) {
            tasks.add(new TestNative("hostname"));
        }

        // Submitting the tasks
        master.solveAll(tasks);
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
}
