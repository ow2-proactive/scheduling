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

import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;


public class FTObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String services;
    private FTObject b;

    public FTObject() {
    }

    public FTObject(String name) {
        this.name = name;
        this.services = "";
    }

    public void init(FTObject b) {
        PAActiveObject.setForgetOnSend(b, "a");
        PAActiveObject.setForgetOnSend(b, "b");
        PAActiveObject.setForgetOnSend(b, "c");

        this.b = b;

        b.a(new SlowlySerializableObject("a", 3000));
        b.b(new SlowlySerializableObject("b", 3000));
        b.c(new SlowlySerializableObject("c", 3000));
    }

    public boolean getResult() {
        return b.getServices().equals("abc");
    }

    public void a(SlowlySerializableObject o) {
        services += "a";
    }

    public void b(SlowlySerializableObject o) {
        services += "b";
    }

    public void c(SlowlySerializableObject o) {
        services += "c";
    }

    public String getServices() {
        return services;
    }
}
