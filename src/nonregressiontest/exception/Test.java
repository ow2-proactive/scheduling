/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest.exception;

import testsuite.test.FunctionalTest;


/**
 * @author ProActiveTeam
 * @version 1.0, 25 mars 2005
 * @since ProActive 2.2
 *
 */
public class Test extends FunctionalTest {
    int counter = 0;

    public Test() {
        super("Exception", "Test exceptions");
    }
    public boolean postConditions() throws Exception {
        return counter == 3;
    }
    public void action() throws Exception {

        /* Server */
        Exc r = (Exc) org.objectweb.proactive.ProActive.newActive(Exc.class.getName(),
                null);

        /* Client */
        /* voidRT() */
        r.voidRT();

        /* futureRT() */
        Exc res = r.futureRT();
        try {
            res.nothing();
        } catch (RuntimeException re) {
            counter++;
        }

        /* voidExc() */
        try {
            r.voidExc();
        } catch (Exception e) {
            if (e.getMessage().startsWith("Test")) {
                counter++;
            }
        }

        /* futureExc() */
        try {
            r.futureExc();
        } catch (Exception e) {
            if (e.getMessage().startsWith("Test")) {
                counter++;
            }
        }
    }

    public void initTest() throws Exception {
        // TODO Auto-generated method stub
    }

    public void endTest() throws Exception {
        // TODO Auto-generated method stub
    }
    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            System.out.println(test.postConditions());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
        }
        
    }
}
