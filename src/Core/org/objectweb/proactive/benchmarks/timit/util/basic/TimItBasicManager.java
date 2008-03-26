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
package org.objectweb.proactive.benchmarks.timit.util.basic;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;


/**
 * A common TimIt manager used to create a common reductor.
 *
 * @author The ProActive Team
 */
public class TimItBasicManager {

    /** The unique instance of this manager */
    private static TimItBasicManager instance = new TimItBasicManager();

    /** The reference on the reductor */
    private TimItBasicReductor timItBasicReductor;

    /**
     * Singleton pattern
     */
    private TimItBasicManager() {
    }

    /**
     * Returns the single instance of the manager.
     * @return The instance of the manager
     */
    public static TimItBasicManager getInstance() {
        return TimItBasicManager.instance;
    }

    /**
     * Creates the reductor as an active object and returns
     * its reference. If the reductor is already created the same
     * instance is returned.
     * @return The single reference on the common reductor
     */
    public TimItBasicReductor createReductor() {
        if (this.timItBasicReductor != null) {
            this.timItBasicReductor.incrementAwaitedResult();
            return this.timItBasicReductor;
        }
        try {
            this.timItBasicReductor = (TimItBasicReductor) PAActiveObject.newActive(TimItBasicReductor.class
                    .getName(), new Object[] {});
            this.timItBasicReductor.registerShutdownHook();
            this.timItBasicReductor.incrementAwaitedResult();
            return this.timItBasicReductor;
        } catch (Exception e) {
            System.err
                    .println("*** TimIt [TimItCommonManager] : Cannot create the active object TimItCommonReductor");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * A getter for the reductor.
     * @return The active reference on the reductor
     */
    public TimItBasicReductor getTimItCommonReductor() {
        return this.timItBasicReductor;
    }

    /**
     * Provides the classname of the reductor.
     * @return The classname of the reductor
     */
    public static String getReductorClassName() {
        return TimItBasicReductor.class.getName();
    }

    /**
     * Checks the current configuration specified by the
     * properties of the location node for a timed object.
     * Used to check the need for the creation of a common reductor.
     * @param node The location of the timed active object.
     * @return True if timers are activated false otherwise
     * @throws ProActiveException If there is a problem with the node
     */
    public static boolean checkNodeProperties(Node node) throws ProActiveException {
        if (node == null) {
            return false;
        }
        String s = node.getProperty("timitActivation");

        return (s != null) && !"".equals(s) && !"false".equalsIgnoreCase(node.getProperty("reduceResults"));
    }

    /**
     * Sets the reductor reference at null.
     * Be sure to do this after terminating the precedent reductor if there
     * were one.
     */
    public void setReductorNull() {
        this.timItBasicReductor = null;
    }
}
