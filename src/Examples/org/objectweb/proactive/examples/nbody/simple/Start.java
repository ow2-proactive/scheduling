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
package org.objectweb.proactive.examples.nbody.simple;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Cube;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Planet;


/**
 * <P>
 * This starts the nbody example, on the most simple example. Every Active Object (Domains) is
 * associated to one single Planet. Synchronization is achieved by using another type of Active
 * Object, a Maestro, which waits for all Domains to finish the current iteration before asking
 * them to start the following one.
 * </P>
 *
 * @author  ProActive Team
 * @version 1.0,  2005/04
 * @since   ProActive 2.2
 */
public class Start {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public static void main(String[] args) {
        org.objectweb.proactive.examples.nbody.common.Start.main(args);
    }

    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Node[] nodes,
            org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        logger.info("RUNNING simplest VERSION");

        Cube universe = new Cube(-100, -100, -100, 200, 200, 200);
        Domain[] domainArray = new Domain[totalNbBodies];
        for (int i = 0; i < totalNbBodies; i++) {
            Object[] constructorParams = new Object[] { new Integer(i), new Planet(universe) };
            try {
                // Create all the Domains used in the simulation 
                domainArray[i] = (Domain) PAActiveObject.newActive(Domain.class.getName(), constructorParams,
                        nodes[(i + 1) % nodes.length]);
            } catch (ActiveObjectCreationException e) {
                killsupport.abort(e);
            } catch (NodeException e) {
                killsupport.abort(e);
            }
        }

        logger.info("[NBODY] " + totalNbBodies + " Planets are deployed");

        // Create a maestro, which will orchestrate the whole simulation, synchronizing the computations of the Domains
        Maestro maestro = null;
        try {
            maestro = (Maestro) PAActiveObject.newActive(Maestro.class.getName(), new Object[] { domainArray,
                    new Integer(maxIter), killsupport }, nodes[0]);
        } catch (ActiveObjectCreationException e) {
            killsupport.abort(e);
        } catch (NodeException e) {
            killsupport.abort(e);
        }

        // init workers
        for (int i = 0; i < totalNbBodies; i++)
            domainArray[i].init(domainArray, displayer, maestro);
    }
}
