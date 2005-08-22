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
package org.objectweb.proactive.examples.c3d;

import java.util.Vector;

import org.objectweb.proactive.examples.c3d.prim.Light;
import org.objectweb.proactive.examples.c3d.prim.Primitive;


public class Scene implements java.io.Serializable {
    private Vector lights = new Vector();
    private Vector objects = new Vector();
    private View view;

    public void addLight(Light l) {
        lights.addElement(l);
    }

    public void addObject(Primitive object) {
        objects.addElement(object);
    }

    public void addView(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public Light getLight(int number) {
        return (Light) lights.elementAt(number);
    }

    public Primitive getObject(int number) {
        return (Primitive) objects.elementAt(number);
    }

    public int getLights() {
        return lights.size();
    }

    public int getObjects() {
        return objects.size();
    }

    public void setObject(Primitive object, int pos) {
        objects.setElementAt(object, pos);
    }
}
