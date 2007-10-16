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
package org.objectweb.proactive.examples.components.helloworld;

import org.objectweb.fractal.api.control.BindingController;


public class ClientImpl implements Runnable, BindingController {
    private Service service;

    public ClientImpl() {
        // the following instruction was removed, because ProActive requires empty no-args constructors
        // otherwise this instruction is executed also at the construction of the stub
        //System.err.println("CLIENT created");
    }

    public void run() {
        service.print("hello world");
    }

    public String[] listFc() {
        return new String[] { "s" };
    }

    public Object lookupFc(final String cItf) {
        if (cItf.equals("s")) {
            return service;
        }
        return null;
    }

    public void bindFc(final String cItf, final Object sItf) {
        if (cItf.equals("s")) {
            service = (Service) sItf;
        }
    }

    public void unbindFc(final String cItf) {
        if (cItf.equals("s")) {
            service = null;
        }
    }
}
