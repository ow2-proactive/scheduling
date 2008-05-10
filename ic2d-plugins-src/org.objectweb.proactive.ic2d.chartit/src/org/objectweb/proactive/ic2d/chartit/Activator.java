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
package org.objectweb.proactive.ic2d.chartit;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class Activator extends AbstractUIPlugin {
    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.objectweb.proactive.ic2d.chartit";

    /** The shared instance */
    private static Activator plugin;

    /** The console name */
    public static final String CONSOLE_NAME = "ChartIt";

    /** The shared instance of the form colors */
    private FormColors formColors;

    /**
     * The constructor
     */
    public Activator() {
        Activator.plugin = this;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(final BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        try {
            if (formColors != null) {
                formColors.dispose();
                formColors = null;
            }
        } finally {
            super.stop(context);
        }
    }

    /**
     * Returns the shared forms colors.
     * 
     * @param display
     *            The display to be used for color creation
     * @return The instance of the shared form colors
     */
    public FormColors getFormColors(final Display display) {
        if (formColors == null) {
            formColors = new FormColors(display);
            formColors.markShared();
        }
        return formColors;
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return Activator.plugin;
    }
}
