/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
package org.objectweb.proactive.ic2d.gui.jobmonitor;

import javax.swing.*;


public class Icons implements JobMonitorConstants {

    /* Filenames */
    private static final String IMAGES_DIRECTORY = "images/";
    private static final String JOB_ICON_PNG = "job_icon.png";
    private static final String VN_ICON_PNG = "vn_icon.png";
    private static final String HOST_ICON_PNG = "host_icon.png";
    private static final String JVM_ICON_PNG = "jvm_icon.png";
    private static final String NODE_ICON_PNG = "node_icon.png";
    private static final String AO_ICON_PNG = "ao_icon.png";
    private static final String SEPARATOR_ICON_PNG = "separator_icon.png";

    /* Icons */
    private static Icon JOB_ICON = createImageIcon(JOB_ICON_PNG);
    private static Icon VN_ICON = createImageIcon(VN_ICON_PNG);
    private static Icon HOST_ICON = createImageIcon(HOST_ICON_PNG);
    private static Icon JVM_ICON = createImageIcon(JVM_ICON_PNG);
    private static Icon NODE_ICON = createImageIcon(NODE_ICON_PNG);
    private static Icon AO_ICON = createImageIcon(AO_ICON_PNG);
    private static Icon SEPARATOR_ICON = createImageIcon(SEPARATOR_ICON_PNG);

    /* Keys icons */
    private static Icon[] ICONS = new Icon[] {
            HOST_ICON, JVM_ICON, NODE_ICON, AO_ICON, JOB_ICON, VN_ICON
        };

    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = JobMonitorPanel.class.getResource(IMAGES_DIRECTORY +
                path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            //           System.err.println ("Couldn't find file: " + path);
            return null;
        }
    }

    public static Icon getIconForKey(int key) {
        if (key != NO_KEY) {
            return ICONS[KEY2INDEX[key]];
        }

        return null;
    }

    public static Icon getSeparatorIcon() {
        return SEPARATOR_ICON;
    }
}
