/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package scalabilityTests.scenarios;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;

import scalabilityTests.fixtures.ActiveFixture;
import scalabilityTests.framework.HelloWorldAction;

/**
 * Just a test scenario.
 * Hello world using active objects - how cool is that huh!
 * 
 * @author fabratu
 *
 */
public class HelloWorldActive {
	
	private static final int ARGS_NO = 2;
	private static final int ERROR_EXIT_CODE = 42;

	public static void main(String[] args) {
		try {
			if(args.length != ARGS_NO) {
				printUsage();
				System.exit(ERROR_EXIT_CODE);
			}

			ActiveFixture hwScenario = new ActiveFixture(args[0],args[1]);
			hwScenario.loadInfrastructure();
			
			HelloWorldAction hwAction = new HelloWorldAction();
			hwScenario.executeSameActionSameParameter(hwAction, "Hello World");
			System.out.println("Press any key to continue");
			BufferedInputStream console = new BufferedInputStream(
					System.in);
			console.read();
			
			System.out.println("Cleanup...");
			hwScenario.cleanup();
		} catch(IllegalArgumentException e){
			System.err.println("Invalid GCM deployment descriptor path: " + e.getMessage());
			printUsage();
			System.exit(ERROR_EXIT_CODE);
		} catch(NodeException e){
			System.err.println("Could not create the Active Actors - there was something wrong with a Node : " + e.getMessage());
			System.exit(ERROR_EXIT_CODE);
		} catch(ActiveObjectCreationException e) {
			System.err.println("Could not create the Active Actors, reason: " + e.getMessage());
			System.exit(ERROR_EXIT_CODE);
		} catch(ProActiveException e) {
			System.err.println("Failed to deploy the nodes from the GCM descriptor, reason:" + e.getMessage());
			System.exit(ERROR_EXIT_CODE);
		} catch (IOException e) {
			// outta here!
			System.exit(0);
		}
	}

	private static void printUsage() {
		System.out.println("Usage: java " + HelloWorldActive.class.getName() + " xmlDescriptor virtualNode");
		System.out.println("\t- xmlDescriptor is the path to the GCM Aplication Descriptor to be used ");
		System.out.println("\tfor creating the nodes onto which the Active Object Actors will be deployed");
		System.out.println("\t- virtualNode is the name of the virtual node which identifies the above-mentioned nodes");
		System.out.println("\twithin the GCM Application Descriptor");
	}
	
}
