package org.objectweb.proactive.examples.taskscheduler;

import java.util.*;

import org.objectweb.proactive.extra.taskscheduler.*;


public class HelloWorld implements ProActiveTask, java.io.Serializable {

    /**
     * @param args
     */
    public static void main(String[] args) {
        SchedulerUserAPI scheduler = null;
        try {
            scheduler = SchedulerUserAPI.connectTo(args[0]);

            Vector<ProActiveTask> tasks = new Vector<ProActiveTask>();
            for (int i = 0; i < 10; i++)
                tasks.add(new HelloWorld());

            Vector<UserResult> resultVector = scheduler.submit(tasks,
                    System.getProperty("user.name"));

            System.out.println(resultVector.get(0).getResult());
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage() + " will exit");
            System.exit(1);
        }

        System.exit(0);
    }

    public Object run() {
        String message;
        try {
            message = java.net.InetAddress.getLocalHost().toString();
            Thread.sleep(30000);
        } catch (Exception e) {
            message = "crashed";
            e.printStackTrace();
        }

        return ("hi from " + message);
    }
}
