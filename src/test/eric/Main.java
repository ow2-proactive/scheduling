package test.eric;
import org.objectweb.proactive.ProActive;

/*
 * Created on Apr 7, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author etanter
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Main {

	public static void main(String[] args) throws Throwable {
		String theNode1 = "rmi://localhost/Node1";
		String theNode2 = "rmi://localhost/Node2";

		ProActive.newActive(
			StupidActive.class.getName(),
			new Object[] { theNode1, theNode2, "Pepe le putois" },
			theNode1);
			
	}
}

