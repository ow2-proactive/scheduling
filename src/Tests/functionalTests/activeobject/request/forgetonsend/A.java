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

import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.proxy.SendingQueue;


public class A {

    private static ArrayList<String> clockTicks = new ArrayList<String>();
    private static ArrayList<Boolean> sterilityCheck = new ArrayList<Boolean>();
    private String name;

    //
    // ----- CONSTRUCTORS ---------------
    //

    public A() {
    }

    public A(String name) {
        this.name = name;
    }

    //
    // ----- CAUSAL ORDERING CHECK ---------
    //

    public void fos(SlowlySerializableObject obj) {
        tick("fos(" + obj.getName() + ")@" + name);
    }

    public boolean rdv() {
        tick("rdv()@" + name);
        return true;
    }

    synchronized private static void tick(String msg) {
        clockTicks.add(msg);
    }

    public static ArrayList<String> getClockTicks() {
        return clockTicks;
    }

    //
    // ----- STERILITY CHECK
    //

    public void sterilityCheck(A a1, A a2, A a3) {
        PAActiveObject.setForgetOnSend("fosSterilityCheckPart1");
        a2.fosSterilityCheckPart1(a1, a2, a3); // a1 -> a2 : FOS call (Rendezvous is delegated)
    }

    public void fosSterilityCheckPart1(A a1, A a2, A a3) {
        // Test #1
        // The local body should be sterile
        sterilityCheck.add(PAActiveObject.getBodyOnThis().isSteril());

        // Test #2
        // a2 -> a3 : this call should raise a java.io.IOException (because I am on a sterile body)
        try {
            a3.rdvSterilityCheckPart2(a1, a2, a3);
        } catch (Exception e) {
            sterilityCheck.add(e.getMessage().contains("ForgetOnSend"));
        }

        // Test #3 (Begin)
        // a2 -> a1 : this call should be OK. Even if I'm sterile, I can make a sterile call to
        // my parent
        a1.rdvSterilityCheckPart2(a1, a2, a3);

    }

    public void rdvSterilityCheckPart2(A a1, A a2, A a3) {
        // Test #3 (End)
        // If I am here, test #3 works well
        sterilityCheck.add(true);

        // Test #4 (Begin) + #5
        // a1 -> a1 : this call should be OK. Even if I'm sterile, I can make a sterile call to
        // myself
        a1.rdvSterilityCheckPart3(a1, a2, a3);
    }

    public void rdvSterilityCheckPart3(A a1, A a2, A a3) {
        // Test #4 (End) + #5
        // As it comes from a sterile service, this service should be also sterile
        sterilityCheck.add(PAActiveObject.getBodyOnThis().isSteril());
    }

    public static boolean verifySterility() {
        if (sterilityCheck.size() != 4)
            return false;

        Iterator<Boolean> it = sterilityCheck.iterator();

        while (it.hasNext()) {
            if (it.next() == false) {
                return false;
            }
        }
        return true;
    }
}
