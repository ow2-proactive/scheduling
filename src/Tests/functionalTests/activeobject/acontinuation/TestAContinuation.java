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
package functionalTests.activeobject.acontinuation;

import java.util.Vector;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.config.PAProperties;

import functionalTests.FunctionalTest;
import functionalTests.GCMDeploymentReady;
import static junit.framework.Assert.assertTrue;


/**
 * Test automatic continuations by results and parameters
 */
@GCMDeploymentReady
public class TestAContinuation extends FunctionalTest {
    AOAContinuation a;
    AOAContinuation b;
    AOAContinuation t1;
    AOAContinuation t2;
    AOAContinuation lastA;
    Id idPrincipal;
    Id idDeleguate;
    boolean futureByResult;

    @org.junit.Test
    public void action() throws Exception {
        String initial_ca_setting = PAProperties.PA_FUTURE_AC.getValue();
        if (!PAProperties.PA_FUTURE_AC.isTrue()) {
            PAProperties.PA_FUTURE_AC.setValue(PAProperties.TRUE);
        }
        ACThread acthread = new ACThread();
        acthread.start();
        acthread.join();
        PAProperties.PA_FUTURE_AC.setValue(initial_ca_setting);

        assertTrue(futureByResult && a.isSuccessful());
        assertTrue(a.getFinalResult().equals("dummy"));
        assertTrue(lastA.getIdName().equals("e"));
        assertTrue(t1.getIdName().equals("d"));
        assertTrue(t2.getIdName().equals("d"));
    }

    private class ACThread extends Thread {
        @Override
        public void run() {
            try {
                a = (AOAContinuation) PAActiveObject.newActive(AOAContinuation.class.getName(),
                        new Object[] { "principal" });
                //test future by result
                a.initFirstDeleguate();
                idDeleguate = a.getId("deleguate2");
                idPrincipal = a.getId("principal");
                Vector<Id> v = new Vector<Id>(2);
                v.add(idDeleguate);
                v.add(idPrincipal);
                if (PAFuture.waitForAny(v) == 0) {
                    futureByResult = false;
                } else {
                    futureByResult = true;
                }

                //test future passed as parameter
                b = (AOAContinuation) PAActiveObject.newActive(AOAContinuation.class.getName(),
                        new Object[] { "dummy" });
                idPrincipal = b.getIdforFuture();
                a.forwardID(idPrincipal);
                //Test non-blocking when future passed as parameter
                AOAContinuation c = (AOAContinuation) PAActiveObject.newActive(AOAContinuation.class
                        .getName(), new Object[] { "c" });
                AOAContinuation d = (AOAContinuation) PAActiveObject.newActive(AOAContinuation.class
                        .getName(), new Object[] { "d" });
                AOAContinuation e = (AOAContinuation) PAActiveObject.newActive(AOAContinuation.class
                        .getName(), new Object[] { "e" });

                AOAContinuation de = d.getA(e);
                AOAContinuation cde = c.getA(de);
                lastA = e.getA(cde);

                //test multiple wrapped futures with multiples AC destinations
                AOAContinuation f = (AOAContinuation) PAActiveObject.newActive(AOAContinuation.class
                        .getName(), new Object[] { "f" });
                c.initSecondDeleguate();
                AOAContinuation t = c.delegatedGetA(d);
                t1 = e.getA(t);
                t2 = f.getA(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
