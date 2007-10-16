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
package functionalTests.activeobject.futurecallbacks;

import java.util.concurrent.Future;

import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.util.MutableInteger;


public class A {
    private A brother;
    public static int counter = 0;

    public A() {
    }

    public void myCallback(Future<MutableInteger> f) throws Exception {
        MutableInteger i = f.get();
        i.getValue();
        synchronized (A.class) {
            A.counter++;
            A.class.notifyAll();
        }
    }

    public void start() {
        MutableInteger slow = this.brother.slow();
        ProFuture.addActionOnFuture(slow, "myCallback");
        MutableInteger fast = this.brother.fast();
        ProFuture.waitFor(fast);
        ProFuture.addActionOnFuture(fast, "myCallback");
    }

    public MutableInteger slow() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new MutableInteger();
    }

    public MutableInteger fast() {
        return new MutableInteger();
    }

    public void giveBrother(A a) {
        this.brother = a;
    }
}
