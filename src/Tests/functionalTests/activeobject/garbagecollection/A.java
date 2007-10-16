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
package functionalTests.activeobject.garbagecollection;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;


public class A implements InitActive {
    private Collection<A> references;
    public static final Collection<WeakReference<A>> weak = new LinkedList<WeakReference<A>>();

    public A() {
    }

    public void initActivity(Body body) {
        // this is not a stub
        A.weak.add(new WeakReference<A>(this));
        references = new Vector<A>();
    }

    public void addRef(A ref) {
        this.references.add(ref);
    }

    public static int countCollected() {
        int count = 0;
        for (WeakReference<A> wr : A.weak) {
            if (wr.get() == null) {
                count++;
            }
        }
        return count;
    }
}
