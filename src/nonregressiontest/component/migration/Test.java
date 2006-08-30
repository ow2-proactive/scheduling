/*
 * Created on Apr 22, 2004
 * author : Matthieu Morel
 */
package nonregressiontest.component.migration;

import java.util.List;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.ProActiveGroup;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.Message;


/**
 * This test deploys a distributed component system and makes sure migration is effective by
 * invoking methods on migrated components (through singleton, collection, gathercast and multicast interfaces)
 * 
 * @author Matthieu Morel
 */
public class Test extends ComponentTest {
    public static String MESSAGE = "-->m";

    //ComponentsCache componentsCache;
    public Test() {
        super("migration of components", "migration of components");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        TestAO testAO = (TestAO) ProActive.newActive(TestAO.class.getName(),
                new Object[] {  });
        testAO.go();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        //        Launcher.killNodes(false);
    }

    public boolean postConditions() throws Exception {
        return true;
    }

    private int append(StringBuffer buffer, Message message) {
        int nb_messages = 0;
        if (ProActiveGroup.isGroup(message)) {
            for (int i = 0; i < ProActiveGroup.size(message); i++) {
                nb_messages += append(buffer,
                    (Message) ProActiveGroup.get(message, i));
            }
        } else {
            buffer.append(message.getMessage());
            nb_messages++;
        }
        return nb_messages;
    }

    public static void main(String[] args) {
        //        System.setProperty("fractal.provider", "org.objectweb.proactive.core.component.Fractive");
        //        System.setProperty("java.security.policy", System.getProperty("user.dir")+"/proactive.java.policy");
        //        System.setProperty("log4j.configuration", System.getProperty("user.dir")+"/proactive-log4j");
        //        System.setProperty("log4j.configuration", "file:" + System.getProperty("user.dir")+"/proactive-log4j");
        //        System.setProperty("nonregressiontest.descriptor.defaultnodes.file", "/nonregressiontest/descriptor/defaultnodes/NodesLocal.xml");
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions()) {
                System.out.println("SUCCESS!");
            } else {
                System.out.println("FAILED!");
            }
        } catch (Exception e) {
            System.out.println("FAILED!");
            e.printStackTrace();
        } finally {
            //            try {
            //                test.endTest();
            //                System.exit(0);
            //            } catch (Exception e1) {
            //                e1.printStackTrace();
            //            }
        }
    }
}
