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
package functionalTests.activeobject.request.forgetonsend;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;


public class C implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String services;
    private int fooASerializer;
    private int fooBSerializer;

    public C() {
    }

    public C(String name) {
        this.name = name;
        this.services = "";
    }

    /*
     * C3 _____________________________________
     * 
     * C2 _______|__fos__|_____________________
     * 
     * C1 ___| base |__________________________
     * 
     */
    public void sendTwoFos(C c2) {
        PAActiveObject.setForgetOnSend(c2, "fosA");
        PAActiveObject.setForgetOnSend(c2, "fosB");

        c2.fosA("a", new SlowlySerializableObject("fosA", 3000));
        c2.fosB("b", new SlowlySerializableObject("fosB", 3000));
    }

    public void fosA(String s, SlowlySerializableObject o) {
        System.out.println("-- fosA : " + o.getVmSerializer());
        fooASerializer = o.getVmSerializer();
        services += "a";
    }

    public void fosB(String s, SlowlySerializableObject o) {
        System.out.println("-- fosB : " + o.getVmSerializer());
        fooBSerializer = o.getVmSerializer();
        services += "b";
    }

    public void rdv() {
    }

    public void rdv(C c) {
        c.rdv();
    }

    public void moveTo(Node dest) throws MigrationException {
        PAMobileAgent.migrateTo(dest);
    }

    //
    // -- CHECKING METHODS
    //

    public String getServices() {
        return services;
    }

    public int getRuntimeHashCode() {
        return Runtime.getRuntime().hashCode();
    }

    public int getFooASerializer() {
        return fooASerializer;
    }

    public int getFooBSerializer() {
        return fooBSerializer;
    }
}