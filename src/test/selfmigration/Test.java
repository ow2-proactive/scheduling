/*
 * Created on 15 mai 2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package test.selfmigration;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.body.migration.MigrationException;

import java.io.Serializable;


/**
 * @author fhuet
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Test implements RunActive, Serializable {

    protected String dest;
    protected boolean alreadyMigrated;

    public Test() {
    }

    public Test(String dest) {
        this.dest = dest;
    }

    public void echo() {
        System.out.println("Echo");
    }

    public void runActivity(Body body) {

        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (body.isActive()) {
            if (!alreadyMigrated) {
                this.alreadyMigrated = true;
                System.out.println("Object created, ready to migrate to " +
                    dest);
                   try {
						 ProActive.migrateTo(dest);
					} catch (MigrationException e) {					
						e.printStackTrace();
					}
            } else {
                System.out.println("done");
                service.fifoServing();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: " + Test.class.getName() + " <node>");
            System.exit(-1);
        }

        Test test = null;
        Object[] arguments = { args[0] };
        try {
            System.out.println("Creating object");
            test = (Test) ProActive.newActive(Test.class.getName(), arguments,
                    (String) null);
            Thread.sleep(5000);
            System.out.println("Calling echo");
            test.echo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
