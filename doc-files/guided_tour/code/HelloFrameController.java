package org.objectweb.proactive.examples.hello;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.ext.migration.MigrationStrategyManager;
import org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl;


/** This class allows the "migration" of a graphical interface. A gui object is attached
 * to the current class, and the gui is removed before migration, thanks to the use
 * of a MigrationStrategyManager */
public class HelloFrameController extends MigratableHello {
    HelloFrame helloFrame;
    MigrationStrategyManager migrationStrategyManager;

    /**required empty constructor */
    public HelloFrameController() {
    }

    /**constructor */
    public HelloFrameController(String name) {
        super(name);
    }

    /** This method attaches a migration strategy manager to the current active object.
     * The migration strategy manager will help to define which actions to take before
     * and after migrating */
    public void initActivity(Body body) {
        // add a migration strategy manager on the current active object
        migrationStrategyManager = new MigrationStrategyManagerImpl((Migratable) ProActive.getBodyOnThis());
        // specify what to do when the active object is about to migrate
        // the specified method is then invoked by reflection
        migrationStrategyManager.onDeparture("clean");
    }

    /** Factory for local creation of the active object
    * @param name the name of the agent
    * @return an instance of a ProActive active object of type HelloFrameController */
    public static HelloFrameController createHelloFrameController(String name) {
        try {
            // creates (and initialize) the active object
            HelloFrameController obj = (HelloFrameController) ProActive.newActive(
                    HelloFrameController.class.getName(),
                    new Object[] { name });

            return obj;
        } catch (ActiveObjectCreationException aoce) {
            System.out.println("creation of the active object failed");
            aoce.printStackTrace();

            return null;
        } catch (NodeException ne) {
            System.out.println("creation of default node failed");
            ne.printStackTrace();

            return null;
        }
    }

    public String sayHello() {
        if (helloFrame == null) {
            helloFrame = new HelloFrame("Hello from " +
                    ProActive.getBodyOnThis().getNodeURL());
            helloFrame.show();
        }

        return "Hello from " + ProActive.getBodyOnThis().getNodeURL();
    }

    public void clean() {
        System.out.println("killing frame");
        helloFrame.dispose();
        helloFrame = null;
        System.out.println("frame is killed");
    }
}
