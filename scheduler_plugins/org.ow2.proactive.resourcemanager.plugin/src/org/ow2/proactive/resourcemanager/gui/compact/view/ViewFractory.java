/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://proactive.inria.fr/team_members.htm Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.resourcemanager.gui.compact.view;

import org.ow2.proactive.resourcemanager.gui.data.model.Host;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.Source;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;


/**
 *
 * Class represents the factory for view creation.
 *
 */
public class ViewFractory {

    /**
     * Creates views based on the specified element.
     */
    public static View createView(TreeLeafElement element) {
        View view = null;
        if (element instanceof Node) {
            view = new NodeView(element);
        } else if (element instanceof Host) {
            view = new HostView(element);
        } else if (element instanceof Source) {
            view = new SourceView(element);
        } else {
            view = new View(element);
        }

        return view;
    }

}
