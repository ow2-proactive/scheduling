package org.objectweb.proactive.examples.scheduler;

import java.util.Vector;

import org.objectweb.proactive.extra.scheduler.ProActiveTask;
import org.objectweb.proactive.extra.scheduler.SchedulerUserAPI;
import org.objectweb.proactive.extra.scheduler.UserResult;


public class HelloWorld2 implements ProActiveTask, java.io.Serializable {
    int sleepTime;
    int number;

    //PA noArg contructor
    public HelloWorld2(){}
    public static void main(String[] args) {
        SchedulerUserAPI scheduler = null;
        try {
            String SNode;
            if (args.length == 3) {
                SNode = args[2];
            } else {
                SNode = "//localhost/SCHEDULER_NODE";
            }
            scheduler = SchedulerUserAPI.connectTo(SNode);

            Vector<ProActiveTask> tasks = new Vector<ProActiveTask>();
            for (int i = 0; i < Integer.parseInt(args[0]); i++) {
                HelloWorld2 hello = new HelloWorld2();
                hello.sleepTime = Integer.parseInt(args[1]);
                hello.number = i + 1;
                tasks.add(hello);
            }

            Vector<UserResult> resultVector = scheduler.submit(tasks,
                    System.getProperty("user.name"));
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
            Thread.sleep(sleepTime * 1000);
        } catch (Exception e) {
            message = "crashed";
            e.printStackTrace();
        }

        return ("No." + this.number + " hi from " + message + "\t slept for " +
        sleepTime + "Seconds");
    }
}
