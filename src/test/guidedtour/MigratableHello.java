package test.guidedtour;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.NodeException;
import sun.security.action.GetLongAction;
/** This class adds non functionnal behavior to Hello, so that we can
* manipulate it as a ProActive active object.
* @author mmorel
*/
// the object that will be migrated active has to be Serializable
public class MigratableHello extends InitializedHello implements Serializable {
	/**
	* Creates a new MigratableHello object.
	*/
	public MigratableHello() {
	}
	/**
	* Creates a new MigratableHello object.
	* 
	* @param name the name of the agent
	*/
	// ProActive requires the active object to explicitely define (or redefine)
	// the constructors, so that they can be reified
	public MigratableHello(String name) {
		super(name);
	}

	/** factory for locally creating the active object
	* @param name the name of the agent
	* @return an instance of a ProActive active object of type MigratableHello
	*
	*/
	public static MigratableHello createMigratableHello(String name) {
		try {
			return (MigratableHello) ProActive.newActive(MigratableHello.class.getName(), new Object[] { name });
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
	/** overriding the locator method so that we can add the ProActive node name
	* @return the name of the host currently containing the object
	*/
	protected String getCurrentNodeName() {
			return getLocalHostName() + "/" + ProActive.getBodyOnThis().getNodeURL();
	}
	/** method for migrating
	* @param destination_node destination node
	*/
	public void moveTo(String destination_node) {
		System.out.println("\n-----------------------------");
		System.out.println("starting migration to node : " + destination_node);
		System.out.println("...");
		try {
			// THIS MUST BE THE LAST CALL OF THE METHOD
			ProActive.migrateTo(destination_node);
		} catch (MigrationException me) {
			System.out.println("migration failed : " + me.toString());
		}
	}
	
	
		public void moveTo(Object destination_object_node) {
		System.out.println("\n-----------------------------");
		System.out.println("starting migration to node " );
		System.out.println("...");
		try {
			// THIS MUST BE THE LAST CALL OF THE METHOD
			ProActive.migrateTo(destination_object_node);
		} catch (MigrationException me) {
			System.out.println("migration failed : " + me.toString());
		}
	}

}
