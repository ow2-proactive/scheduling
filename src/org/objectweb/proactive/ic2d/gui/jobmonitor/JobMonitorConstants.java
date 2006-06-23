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

public interface JobMonitorConstants {

    /*
     * The values are meaningless, but the order is important.
     * If you change it, change also NAMES and ICONS in Icons.java.
     */
    public static final int NO_KEY = -1;
    public static final int HOST = 0;
    public static final int JVM = 1;
    public static final int NODE = 2;
    public static final int AO = 3;

    /* The hole is important here because we don't have a real tree */
    public static final int JOB = 5;
    public static final int VN = 6;
    public static final String[] NAMES = new String[] {
            "Host", "JVM", "Node", "Active Object", "Job", "Virtual Node"
        };
    public static final int NB_KEYS = NAMES.length;
    public static final int[] KEY2INDEX = { 0, 1, 2, 3, -1, 4, 5 };
    public static final int[] KEYS = { 0, 1, 2, 3, 5, 6 };
}
