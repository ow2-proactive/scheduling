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
package functionalTests.activeobject.generics;

public class Pair<X, Y> {
    private X first;
    private Y second;

    public Pair() {
    }

    public Pair(X a1, Y a2) {
        //		System.out.println("X = " + a1 + " ; Y = " + a2);
        first = a1;
        second = a2;
    }

    public X getFirst() {
        //		System.out.println("[PAIR] getFirst called in " + getClass().getName());
        return first;
    }

    public Y getSecond() {
        //		System.out.println("[PAIR] getSecond called in " + getClass().getName());
        return second;
    }

    public void setFirst(X arg) {
        first = arg;
    }

    public void setSecond(Y arg) {
        second = arg;
    }
}
