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
package functionalTests.activeobject.creation.local.newactive.constructors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class B {
    private String choosed = null;

    public B() {
    }

    public B(Object o) {
        choosed = "C1";
    }

    public B(String s) {
        choosed = "C2";
    }

    public B(int i) {
        choosed = "C3";
    }

    public B(long j) {
        choosed = "C4";
    }

    public B(Long j) {
        choosed = "C5";
    }

    public B(String s, Object o) {
        choosed = "C6";
    }

    public B(Object o, String s) {
        choosed = "C7";
    }

    public B(Collection o) {
        choosed = "C8";
    }

    public B(List o) {
        choosed = "C9";
    }

    public B(ArrayList o) {
        choosed = "C10";
    }

    public String getChoosed() {
        return choosed;
    }
}
