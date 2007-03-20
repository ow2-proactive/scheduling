package org.objectweb.proactive.examples.taskscheduler;

import org.objectweb.proactive.extra.taskscheduler.ProActiveTask;
import org.objectweb.proactive.extra.taskscheduler.SchedulerUserAPI;
import org.objectweb.proactive.extra.taskscheduler.UserResult;


public class HelloWorld2 implements ProActiveTask, java.io.Serializable {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            SchedulerUserAPI scheduler = SchedulerUserAPI.connectTo(args[0]);

            UserResult result1 = scheduler.submit(new HelloWorld2(),
                    System.getProperty("user.name"));

            System.out.println("Is the execution 1finished??" +
                result1.isFinished());

            System.out.println(result1.getResult());
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage() + "\nWill exit");
            System.exit(1);
        }

        System.exit(0);
    }

    public Object run() {
        String message;
        try {
            message = java.net.InetAddress.getLocalHost().toString();
            Thread.sleep(10000);
        } catch (Exception e) {
            message = "crashed";
            e.printStackTrace();
        }

        return ("hi from " + message);
    }
}
