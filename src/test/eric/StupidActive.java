package test.eric;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.body.migration.MigrationException;

import java.io.Serializable;


public class StupidActive implements RunActive, Serializable {
    public static final int MAX_MIG = 800;
    private String itsNode1 = "";
    private String itsNode2 = "";
    private String itsName;
    private boolean itsInNode1 = true;
    private int itsMigCount = 0;

    public StupidActive() {
        super();
    }

    public StupidActive(String aNode1, String aNode2, String aName) {
        itsNode1 = aNode1;
        itsNode2 = aNode2;
        itsName = aName;
    }

    public void runActivity(Body body) {
        if (itsMigCount < MAX_MIG) {
            try {
                if (inNode1()) {
                    System.out.println(this + ": moving to Node2 (" +
                        itsMigCount + ")");
                    changeNode();
                    ProActive.migrateTo(itsNode2);
                } else {
                    System.out.println(this + ": moving to Node1 (" +
                        itsMigCount + ")");
                    changeNode();
                    ProActive.migrateTo(itsNode1);
                }
            } catch (MigrationException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(this + ": I am tired. Bye!");
        }
    }

    protected boolean inNode1() {
        return itsInNode1;
    }

    protected void changeNode() {
        itsInNode1 = !itsInNode1;
        itsMigCount++;
    }

    public String toString() {
        return getClass().getName() + "[" + itsName + "]";
    }
}
