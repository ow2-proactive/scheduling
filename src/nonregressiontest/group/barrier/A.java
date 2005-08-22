package nonregressiontest.group.barrier;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.group.spmd.ProSPMD;


public class A implements Active, java.io.Serializable {
    private String name;
    private int fooCounter = 0;
    private int barCounter = 0;
    private int geeCounter = 0;
    private String errors = "";

    public A() {
    }

    public A(String s) {
        this.name = s;
    }

    public String getErrors() {
        return this.errors;
    }

    public void foo() {
        this.fooCounter++;
    }

    public void bar() {
        if (this.fooCounter != 3) {
            this.errors += "'bar' invoked before all 'foo'\n";
        }
        this.barCounter++;
    }

    public void gee() {
        if (this.barCounter != 3) {
            this.errors += "'gee' invoked before all 'bar'\n";
        }
        if (this.fooCounter != 3) {
            this.errors += "'gee' invoked before all 'foo'\n";
        }
        this.geeCounter++;
    }

    public void waitFewSecondes() {
        long n = 0;
        if ("Agent0".equals(this.name)) {
            n = 0;
        } else if ("Agent1".equals(this.name)) {
            n = 1000;
        } else if ("Agent2".equals(this.name)) {
            n = 2000;
        }
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            System.err.println("** InterruptedException **");
        }
    }

    public void start() {
        A myspmdgroup = (A) ProSPMD.getSPMDGroup();
        this.waitFewSecondes();
        myspmdgroup.foo();
        ProSPMD.barrier("'1'");
        myspmdgroup.bar();
        ProSPMD.barrier("'2'");
        myspmdgroup.gee();
    }
}
