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
package org.objectweb.proactive.ext.implicit;

public interface Implicit {
    public void forbid(String shortcut, String condition)
        throws InvalidAssociateDeclaration;

    /**
     *   Creates a shortcut <code>shortcut</code> for the method of name <code>nameOfMethod</code>
     *   and parameters of types <code>argumentsType</code>. This array is composed of the
     *   fully-qualified names of the types. Wrapping and unwrapping of the primitive types
     *   follows the same rule as in the Reflection API. For example, if a parameters is an <code>int</code>,
     *   its type should be declated as <code>java.lang.Integer</code>.
     */
    public abstract void addShortcut(String shortcut, String nameOfMethod,
        String[] argumentsType);

    // public abstract void serveOldestWithoutBlocking (String shortcut);
    // public abstract void serveOldestWithoutBlockingThatIsNot (String shortcut);
}
