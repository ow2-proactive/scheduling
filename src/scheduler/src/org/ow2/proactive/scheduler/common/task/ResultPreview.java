/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import javax.swing.JPanel;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This class defines a way to represent the result of a given task.
 *
 * @author The ProActive Team
 * @since 3.9
 */
@PublicAPI
public abstract class ResultPreview implements Serializable {

    /**  */
    private static final long serialVersionUID = 200;
    /**
     * The textual preview of the result, evaluated on computing node side
     * after the task completion.
     */
    private String textualPreview = null;

    /**
     * Create a textual representation of the result.
     *
     * @param result the result to be described.
     * @return the textual representation.
     */
    public abstract String getTextualDescription(TaskResult result);

    /**
     * Create a graphical representation of the result.
     *
     * @param result the result to be described.
     * @return the graphical representation.
     */
    public abstract JPanel getGraphicalDescription(TaskResult result);
}
