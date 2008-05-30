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
package org.objectweb.proactive.ic2d.chartit.data;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.objectweb.proactive.ic2d.chartit.Activator;


/**
 * The available types of chart that can be built from models
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public enum ChartType {

    PIE, BAR, TUBE, CONE, PYRAMID, AREA, LINE, METER, TIME_SERIES;

    /**
     * All names in an array of string
     */
    public static String[] names = new String[] { PIE.name(), BAR.name(), TUBE.name(), CONE.name(),
            PYRAMID.name(), AREA.name(), LINE.name(), METER.name(), TIME_SERIES.name() };

    /**
     * Returns the icon image corresponding to the chart type.
     * 
     * @param chartType The chart type
     * @return An instance of <code>Image</code> class corresponding to the chart type
     */
    public static Image getImage(final ChartType chartType) {
        String filename;
        switch (chartType) {
            case PIE:
                filename = "piecharticon.gif";
                break;
            case BAR:
                filename = "barcharticon.gif";
                break;
            case TUBE:
                filename = "tubecharticon.gif";
                break;
            case CONE:
                filename = "conecharticon.gif";
                break;
            case PYRAMID:
                filename = "pyramidcharticon.gif";
                break;
            case AREA:
                filename = "areacharticon.gif";
                break;
            case LINE:
                filename = "linecharticon.gif";
                break;
            case METER:
                filename = "metercharticon.gif";
                break;
            case TIME_SERIES:
                filename = "graph.gif";
                break;
            default:
                throw new RuntimeException("Unknown chart type : " + chartType);
        }
        return ImageDescriptor.createFromURL(
                FileLocator.find(Activator.getDefault().getBundle(), new Path("icons/" + filename), null))
                .createImage();
    }
}
