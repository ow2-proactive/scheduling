package test.looplocationserver;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ext.locationserver.LocationServerMetaObjectFactory;


public class Test implements org.objectweb.proactive.RunActive,
                             Serializable {

    protected String[] destinations;
    int index;

    public Test() {
        System.out.println("Test constructor");
    }

    public Test(String[] nodes) {
        System.out.println(
                "Test constructor with " + nodes.length + " destinations");
        index = 0;
        destinations = nodes;
    }

    public void echo() {
        System.out.println("echo() ");
    }

    public void runActivity(Body body) {
        try {
            //  System.out.println("Test");
            while (body.getRequestQueue().size() != 0) {
                body.getRequestQueue().removeOldest().serve(body);
            } // end of while (body.getRequestQueue().size() != 0)
            //   body.getRequestQueue().clear();
            System.out.println("XXXXX " + destinations);
            while (body.isActive()) {
                if (index < destinations.length) {
                    index++;
                    ProActive.migrateTo(destinations[index - 1]);
                } else {
                    //  System.out.println("---- Done");
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    index = 0;
                    //body.fifoPolicy();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callOther(Test o) {
        o.echo();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println(
                    "Usage: " + Test.class.getName() + 
                    "<destination1> ... <destinationN>");
            System.exit(-1);
        }
        System.out.println("== Test: creating an ObjectWithLocationServer");

        Test obj1 = null;
        Object[] param = new Object[1];
       // String[] stringParam = new String[args.length - 1];
     //   System.arraycopy(args, 1, stringParam, 0, args.length - 1);
       // System.out.println("stringParam= " + stringParam);
        param[0] = args;
        try {
            obj1 = (Test)ProActive.newActive(Test.class.getName(), param, null, 
                                             null, 
                                             LocationServerMetaObjectFactory.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("== Test: creating active object Test");

//        Test test = null;
//        try {
//            test = (Test)ProActive.newActive(Test.class.getName(), null, null, 
//                                             null, 
//                                             LocationServerMetaObjectFactory.newInstance());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("== Test: waiting 5s and calling echo()");
//        try {
//            Thread.sleep(5000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        test.callOther(obj1);
//        System.out.println("== Test:waiting 5s");
//        try {
//            Thread.sleep(5000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("== Test: calling echo()");
//        test.callOther(obj1);
    }
}