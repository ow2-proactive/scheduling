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
package org.objectweb.proactive.examples.nbody.groupdistrib;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Cube;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Planet;


/**
 * <P>
 * This starts the nbody example, where communication uses Groups, and synchronization
 * is done through an odd-even scheme : if a value arrives with the wrong iteration number,
 * it gets postponed until the next iteration. This allows for a soft synchronization, where
 * Domains do not have to wait for all the others before starting the following iteration.
 * </P>
 *
 * @author  ProActive Team
 * @version 1.0,  2005/04
 * @since   ProActive 2.2
 */
public class Start {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public static void main(String[] args) {
        // Set arguments as read on command line
        org.objectweb.proactive.examples.nbody.common.Start.main(args);
    }

    public static void main(int totalNbBodies, int maxIter,
        Displayer displayer, Node[] nodes,
        org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        logger.info("RUNNING groupdistrib VERSION");

        Object[][] constructorParams = new Object[totalNbBodies][3];

        Cube universe = new Cube(-100, -100, -100, 200, 200, 200);
        for (int i = 0; i < totalNbBodies; i++) {
            constructorParams[i][0] = new Integer(i);
            // coordinates between -100,-100 and 100,100
            constructorParams[i][1] = new Planet(universe);
            constructorParams[i][2] = killsupport;
        }

        Domain domainGroup = null;
        try {
            // Create a group containing all the Domain in the simulation 
            domainGroup = (Domain) ProGroup.newGroup(Domain.class.getName(),
                    constructorParams, nodes);
        } catch (ClassNotReifiableException e) {
            killsupport.abort(e);
        } catch (ClassNotFoundException e) {
            killsupport.abort(e);
        } catch (ActiveObjectCreationException e) {
            killsupport.abort(e);
        } catch (NodeException e) {
            killsupport.abort(e);
        }

        logger.info("[NBODY] " + totalNbBodies + " Planets are deployed");

        // init workers
        domainGroup.init(domainGroup, displayer, maxIter, killsupport);

        // launch computation
        domainGroup.sendValueToNeighbours();
    }
}
