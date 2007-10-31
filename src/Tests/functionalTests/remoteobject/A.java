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
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.remoteobject;

import java.io.Serializable;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;


public class A implements Serializable {

    /**
     * generated serialUID
     */
    private static final long serialVersionUID = 7639258429051290167L;

    public A() {
    }

    public void migrateTo(Node n) {
        try {
            ProMobileAgent.migrateTo(n);
        } catch (MigrationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String sayHello() {
        return "hello from " + ProActiveObject.getBodyOnThis().getNodeURL();
    }
}
