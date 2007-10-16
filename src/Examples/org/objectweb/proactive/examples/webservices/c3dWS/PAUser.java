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
package org.objectweb.proactive.examples.webservices.c3dWS;

public class PAUser implements User {
    private String name;
    private C3DUser c3duser;

    public PAUser(String name, C3DUser c3duser) {
        this.name = name;
        this.c3duser = c3duser;
    }

    public String getName() {
        return name;
    }

    public Object getObject() {
        return c3duser;
    }

    public void setPixels(int[] newPix, Interval inter) {
        this.c3duser.setPixels(newPix, inter);
    }

    public void showMessage(String s) {
        this.c3duser.showMessage(s);
    }

    public void showUserMessage(String s) {
        this.c3duser.showUserMessage(s);
    }

    public void dialogMessage(String subject, String msg) {
        this.c3duser.dialogMessage(subject, msg);
    }

    public void informNewUser(int i, String name) {
        this.c3duser.informNewUser(i, name);
    }

    public void informUserLeft(String name) {
        this.c3duser.informUserLeft(name);
    }
}
