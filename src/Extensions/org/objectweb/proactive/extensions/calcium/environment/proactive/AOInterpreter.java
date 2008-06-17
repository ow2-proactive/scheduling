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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class AOInterpreter {
    AOStageIn stageIn;
    AOStageOut stageOut;
    AOStageCompute stageCompute;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOInterpreter() {
    }

    public AOInterpreter(AOTaskPool taskpool, FileServerClientImpl fserver) throws NodeException,
            ActiveObjectCreationException {
        Node localnode = NodeFactory.getDefaultNode();

        this.stageOut = (AOStageOut) PAActiveObject.newActive(AOStageOut.class.getName(), new Object[] {
                taskpool, fserver }, localnode);

        this.stageCompute = (AOStageCompute) PAActiveObject.newActive(AOStageCompute.class.getName(),
                new Object[] { taskpool, stageOut }, localnode);

        this.stageIn = (AOStageIn) PAActiveObject.newActive(AOStageIn.class.getName(), new Object[] {
                taskpool, fserver, stageCompute }, localnode);
    }

    public AOStageIn getStageIn(AOInterpreterPool interpool) {

        stageOut.setStageInAndInterPool(stageIn, interpool);

        return stageIn;
    }
}
