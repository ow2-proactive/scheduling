package test.mixedlocation;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ext.mixedlocation.MixedLocationMetaObjectFactory;

import java.io.Serializable;

public class Test implements Serializable {

    protected int index;

    public Test() {
    }

    public void echo() {
        System.out.println("Test.echo");
    }

    public void migrateTo(String url) {
        try {
            ProActive.migrateTo(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java " + Test.class + " <destination>");
            System.exit(0);
        }
        Test t = null;
        Source s = null;
        Object[] arg = new Object[1];
        arg[0] = args;

        try {
            t = (Test) ProActive.newActive(Test.class.getName(), null, MixedLocationMetaObjectFactory.newInstance(), null);
            s = (Source) ProActive.newActive(Source.class.getName(), arg, MixedLocationMetaObjectFactory.newInstance(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        s.setTarget(t);
        s.start(args[0]);
    }

    public static class Source {

        protected Test t;
        protected String[] destinations;

        public Source() {
        }

        public Source(String[] s) {
           this.destinations = s;
        }

        public void setTarget(Test t) {
            this.t = t;
        }

        public void start(String destination) {
            System.out.println("First call");
            t.echo();
            t.migrateTo("//tuba/Node1");
//            try {
//                Thread.currentThread().sleep(2000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            t.migrateTo("//tuba/Node2");
            try {
                Thread.currentThread().sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Second call");
            t.echo();
            try {
                Thread.currentThread().sleep(5000);

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Third call");
            t.echo();
        }
    }
}
