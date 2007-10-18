/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):	Vasile Jureschi
 *
 * ################################################################
 */
package active;
import java.io.IOException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;


public class InitializedHelloWorld extends HelloWorld implements InitActive,
		EndActive {
	public void initActivity(Body body) {
		System.out.println("Starting activity.....");
	}
	public void endActivity(Body body) {
		System.out.println("Ending activity.....");
	}
	public void terminate() throws IOException {
		// the termination of the activity is done through a call on the
		// terminate method of the body associated to the current active object
		ProActiveObject.getBodyOnThis().terminate();
	}
}