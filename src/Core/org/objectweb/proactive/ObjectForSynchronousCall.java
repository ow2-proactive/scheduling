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
package org.objectweb.proactive;

/**
 * <P>
 * An object instance of this class is to be returned when a method of an active
 * object needs to be called synchronously. No real data is expected as a result,
 * but the caller needs to wait the actual execution (service) of the request.
 * Because the class is final, the reified call cannot return a future and will
 * do a synchronous call.
 * </P><P>
 * This type can be used instead of void to perform the synchronous call. Note that
 * any final object will result in the same behaviour.
 * </P>
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public final class ObjectForSynchronousCall extends Object implements java.io.Serializable {

    /**
     * No arg constructor for Serializable
     */
    public ObjectForSynchronousCall() {
    }
}
