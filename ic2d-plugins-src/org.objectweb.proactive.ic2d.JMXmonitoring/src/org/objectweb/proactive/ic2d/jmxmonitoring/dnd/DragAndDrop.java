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
package org.objectweb.proactive.ic2d.jmxmonitoring.dnd;

import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.NodeFigure;


/**
 * This class allows us to make drag and drop for a migration.
 */
public class DragAndDrop {

    /** The source object */
    private ActiveObject source;

    /** The source node */
    private NodeObject sourceNode;

    /** The source figure */
    private AOFigure sourceFigure;

    /** The current nodeFigure */
    private NodeFigure nodeFigure;

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * Returns the source of the migration
     * @return the source of the migration
     */
    public ActiveObject getSource() {
        return this.source;
    }

    public NodeObject getSourceNode() {
        return this.sourceNode;
    }

    /**
     * Sets the source of the migration
     * @param source The new source
     */
    public void setSource(ActiveObject source) {
        this.source = source;
        if (source == null) {
            this.sourceNode = null;
        } else {
            this.sourceNode = (NodeObject) source.getParent();
        }
    }

    /**
     * Returns the figure of the source of the migration
     * @return the figure of the source of the migration
     */
    public AOFigure getSourceFigure() {
        return this.sourceFigure;
    }

    /**
     * Sets the figure of the source of the mirgation
     * @param figure The new figure
     */
    public void setSourceFigure(AOFigure figure) {
        this.sourceFigure = figure;
    }

    /**
     * Highlights the new nodeFigure, and cleans the previous nodeFigure.
     * @param nodeFigure
     */
    public void refresh(NodeFigure nodeFigure) {
        if (this.nodeFigure != null) {
            this.nodeFigure.setHighlight(null);
        }
        this.nodeFigure = nodeFigure;
    }

    /**
     * Resets all recorded data in order to allow a new migration.
     */
    public void reset() {
        setSource(null);
        nodeFigure = null;
        AOFigure figure = getSourceFigure();
        if (figure != null) {
            figure.setHighlight(null);
            setSourceFigure(null);
        }
    }
}
