package modelisation.forwarder;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class EvaluateGamma implements org.objectweb.proactive.RunActive, Serializable {

    private String[] destinations;
    int index;
    int max;
    int numberOfMigrations;
    boolean running;


    public EvaluateGamma() {
        System.out.println("EvaluateGamma constructor");
    }


    public EvaluateGamma(String[] nodes) {
        System.out.println("EvaluateGamma constructor with " + nodes + " destinations");
        index = 0;
        destinations = nodes;
    }


    public void echo() {
        System.out.println("Hello, I am here");
    }

    public int getInt() {
        return 1;
    }

    public void go(int n) {
        this.max = n;
        this.numberOfMigrations = 0;
        this.running = true;
    }

    public void runActivity(Body body) {
        try {
            while (body.isActive()) {
                while (!this.running) {
                    body.serve(body.getRequestQueue().blockingRemoveOldest());
                }

                if (numberOfMigrations == max)
                    this.running = false;
                else {
                    if (index < destinations.length) {
                        index++;
                        numberOfMigrations++;
                        ProActive.migrateTo(destinations[index - 1]);
                    } else {
                        System.out.println("---- Done");
                        index = 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static long communicate(EvaluateGamma e) {
        long startTime = System.currentTimeMillis();
        //	e.echo();
        e.getInt();
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }


    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java modelisation.forwarder.EvaluateGamma <NumberOfMigrations> <tries> <nodeName> ... <nodeName>");
            System.exit(1);
        }

        EvaluateGamma eGamma = null;
        Object[] arg = new Object[1];
        String[] args2 = new String[args.length - 2];
        System.arraycopy(args, 2, args2, 0, args.length - 2);
        arg[0] = args2;
        long t = 0;
        long t2 = 0;
        int migrations = Integer.parseInt(args[0]);
        int maxRounds = Integer.parseInt(args[1]);
        try {
            System.out.println("Creating object");
            eGamma = (EvaluateGamma) ProActive.newActive("modelisation.forwarder.EvaluateGamma", arg);

            for (int i = 0; i < maxRounds; i++) {
                eGamma.go(migrations);
                Thread.sleep(20000);
                t2 = EvaluateGamma.communicate(eGamma);
                t += t2;
                System.out.println("Time = " + t2 + " gamma= " + (1000 / (t2 / (migrations + 1))));
            }
            System.out.println("Average time for " + migrations + " forwarding is " + (t / maxRounds));
            System.out.println("Average time for one hop is   " + (t / maxRounds / (migrations + 1)));
            System.out.println("Gamma =  " + (1000 / (t / maxRounds / (migrations + 1))));
            //System.out.println("Calling the object");
            //test.echo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
