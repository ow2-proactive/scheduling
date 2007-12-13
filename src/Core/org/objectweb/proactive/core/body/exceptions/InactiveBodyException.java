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
package org.objectweb.proactive.core.body.exceptions;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;


// end inner class LocalInactiveBody
public class InactiveBodyException extends ProActiveRuntimeException {
    public InactiveBodyException(UniversalBody body) {
        super("Cannot perform this call because body " + body.getID() + "is inactive");
    }

    public InactiveBodyException(UniversalBody body, String nodeURL, UniqueID id, String remoteMethodCallName) {
        // TODO when the class of the remote reified object will be available through UniversalBody, add this info.
        super("Cannot send request \"" + remoteMethodCallName + "\" to Body \"" + id + "\" located at " +
            nodeURL + " because body " + body.getID() + " is inactive");
    }

    public InactiveBodyException(UniversalBody body, String localMethodName) {
        super("Cannot serve method \"" + localMethodName + "\" because body " + body.getID() + " is inactive");
    }

    public InactiveBodyException(String string, Throwable e) {
        super(string, e);
    }
}
