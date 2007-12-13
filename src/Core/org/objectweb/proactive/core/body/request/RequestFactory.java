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
package org.objectweb.proactive.core.body.request;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * <p>
 * A class implementing this interface is a factory of request objects.
 * It is able to create Request tailored for a particular purpose.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/12/23
 * @since   ProActive 0.91
 *
 */
public interface RequestFactory {

    /**
     * Creates a request object based on the given parameter
     * @return the newly created Request object.
     */
    public Request newRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay,
            long sequenceID);
}
