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
package functionalTests.group.barrier;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.group.spmd.ProSPMD;


public class A implements Active, java.io.Serializable {

    /**
         *
         */
    private static final long serialVersionUID = 6959297031183046321L;
    private String name;
    private int fooCounter = 0;
    private int barCounter = 0;
    private int geeCounter = 0;
    private String errors = "";

    public A() {
    }

    public A(String s) {
        this.name = s;
    }

    public String getErrors() {
        return this.errors;
    }

    public void foo() {
        this.fooCounter++;
    }

    public void bar() {
        if (this.fooCounter != 3) {
            this.errors += "'bar' invoked before all 'foo'\n";
        }
        this.barCounter++;
    }

    public void gee() {
        if (this.barCounter != 3) {
            this.errors += "'gee' invoked before all 'bar'\n";
        }
        if (this.fooCounter != 3) {
            this.errors += "'gee' invoked before all 'foo'\n";
        }
        this.geeCounter++;
    }

    public void waitFewSecondes() {
        long n = 0;
        if ("Agent0".equals(this.name)) {
            n = 0;
        } else if ("Agent1".equals(this.name)) {
            n = 1000;
        } else if ("Agent2".equals(this.name)) {
            n = 2000;
        }
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            System.err.println("** InterruptedException **");
        }
    }

    public void start() {
        A myspmdgroup = (A) ProSPMD.getSPMDGroup();
        this.waitFewSecondes();
        myspmdgroup.foo();
        ProSPMD.barrier("'1'");
        myspmdgroup.bar();
        ProSPMD.barrier("'2'");
        myspmdgroup.gee();
    }
}
