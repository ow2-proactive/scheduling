/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.stateness;

public interface Handler<T> {

    /**
     * This method is called when an object matching the specified type is found.
     *
     * References to the parameter object will be replaced by a reference to the returned object.
     *
     * @param o The original object found matching the pattern.
     * @return The object that will replace the original one.
     *
     * @throws Exception
     */
    public T transform(T o) throws Exception;

    /**
     * This method is used to determine if the handle(...) method will be called.
     *
     * @param o The candidate object to test
     * @return true if the handle method should be called on the specified object.
     */
    public boolean matches(Object o);
}
