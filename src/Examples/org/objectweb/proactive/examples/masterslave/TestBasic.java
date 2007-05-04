package org.objectweb.proactive.examples.masterslave;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.extra.masterslave.ProActiveMaster;
import org.objectweb.proactive.extra.masterslave.TaskException;
import org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;


/**
 * This simple test class is an example on how to use the Master/Slave API
 * The tasks wait for a period between 15 and 20 seconds (by ex)
 * The main program displays statistics about the speedup due to parallelization
 * @author fviale
 *
 */
public class TestBasic implements Task<Integer> {
    public static final int DEFAULT_NUMBER_OF_TASKS = 50;
    public static final int DEFAULT_TASK_FIXED_WAIT_TIME = 15000;
    public static final int DEFAULT_TASK_RANDOM_WAIT_TIME = 5000;
    public static final String DEFAULT_DESCRIPTOR = "./RSHListbyHost_Example.xml";
    public static final String DEFAULT_VN_NAME = "matrixNode";
    public static int number_of_tasks;
    public static int fixed_wait_time;
    public static int random_wait_time;
    public static String descriptor_path;
    public static String vn_name;

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Task#run(org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory)
     */
    public Integer run(SlaveMemory memory) {
        long time = fixed_wait_time +
            Math.round((Math.random() * random_wait_time));
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // ok interrrupted
        }
        return (int) time;
    }

    /**
     * Initializing the example with command line arguments
     * @param args
     */
    public static void init(String[] args) {
        if (args.length == 0) {
            descriptor_path = DEFAULT_DESCRIPTOR;
            vn_name = DEFAULT_VN_NAME;
            number_of_tasks = DEFAULT_NUMBER_OF_TASKS;
            fixed_wait_time = DEFAULT_TASK_FIXED_WAIT_TIME;
            random_wait_time = DEFAULT_TASK_RANDOM_WAIT_TIME;
        } else if (args.length == 5) {
            descriptor_path = args[0];
            vn_name = args[1];
            number_of_tasks = Integer.parseInt(args[2]);
            fixed_wait_time = Integer.parseInt(args[3]);
            random_wait_time = Integer.parseInt(args[4]);
        } else {
            System.out.println(
                "Usage: <java_command> [descriptor_path virtual_node_name number_of_tasks fixed_wait_time random_wait_time]");
        }
    }

    /**
     * @param args
     * @throws TaskException
     * @throws MalformedURLException
     */
    public static void main(String[] args)
        throws TaskException, MalformedURLException {
        //   Getting command line parameters
        init(args);

        //      Creating the Master
        ProActiveMaster master = new ProActiveMaster(new URL(descriptor_path),
                vn_name);

        // Creating the tasks to be solved
        List<TestBasic> tasks = new ArrayList<TestBasic>();
        for (int i = 0; i < number_of_tasks; i++) {
            tasks.add(new TestBasic());
        }
        long startTime = System.currentTimeMillis();
        // Submitting the tasks
        master.solveAll(tasks);

        // Collecting the results
        Collection<Integer> results = master.waitAllResults();
        long endTime = System.currentTimeMillis();
        int nbSlaves = master.slavepoolSize();

        // Post processing, calculates the statistics
        long sum = 0;

        for (Integer result : results) {
            sum += result;
        }
        long avg = sum / number_of_tasks;

        long minimum_time = (avg * (long) Math.ceil(number_of_tasks / nbSlaves));
        long effective_time = (int) (endTime - startTime);

        // Displaying the results
        System.out.println("Total calculation time (ms) = " + sum);
        System.out.println("Effective Calculation time (ms): " +
            effective_time);
        System.out.println("Speedup = " + ((endTime - startTime) / sum * 100) +
            "%");
        System.out.println("Deployment overhead = " +
            ((effective_time - minimum_time) / minimum_time * 100));

        // Terminating the Master
        master.terminate(true);

        System.exit(0);
    }
}
