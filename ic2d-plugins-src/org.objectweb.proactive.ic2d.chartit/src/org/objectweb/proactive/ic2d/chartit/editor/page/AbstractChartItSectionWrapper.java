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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chartit.editor.page;

import org.objectweb.proactive.ic2d.chartit.data.resource.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.data.resource.ResourceData;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditorInput;


/**
 * An abstract section wrapper that provides some shortcuts to its underlying classes.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>. 
 */
public abstract class AbstractChartItSectionWrapper {

    /**
     * A reference on the overview page that contains all sections
     */
    protected final OverviewPage overviewPage;

    /**
     * Creates a new instance of this class.
     * 
     * @param overviewPage The overview page that contains all sections
     */
    public AbstractChartItSectionWrapper(final OverviewPage overviewPage) {
        this.overviewPage = overviewPage;
    }

    /**
     * Returns the editor input.
     * 
     * @return The editor input
     */
    public ChartItDataEditorInput getEditorInput() {
        return this.overviewPage.getChartItDataEditorInput();
    }

    /**
     * Returns the resource data.
     * 
     * @return The resource data
     */
    public ResourceData getResourceData() {
        return this.overviewPage.getChartItDataEditorInput().getResourceData();
    }

    /**
     * Returns the resource descriptor
     * 
     * @return The resource descriptor
     */
    public IResourceDescriptor getResourceDescriptor() {
        return this.overviewPage.getChartItDataEditorInput().getResourceData().getResourceDescriptor();
    }
}
