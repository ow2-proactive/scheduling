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

public class B {

    private static String services;

    private String name;

    public B() {
    }

    public B(String name) {
        services = "";
        this.name = name;
    }

    public void a() {
        services += "a";
    }

    public SlowlySerializableObject b(int i) {
        services += "b";
        return new SlowlySerializableObject("res" + i, 0);
    }

    public void c() {
        services += "c";
    }

    public void d() {
        services += "d";
    }

    public void e() {
        services += "e";
    }

    public void f() {
        services += "f";
    }

    public String takeFast() {
        String result = services;
        services = "";
        return result;
    }
}
