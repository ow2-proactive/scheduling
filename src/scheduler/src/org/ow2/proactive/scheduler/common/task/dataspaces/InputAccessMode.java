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
package org.ow2.proactive.scheduler.common.task.dataspaces;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * InputAccessMode provide a way to define how files should be accessed
 * in the executable.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
@PublicAPI
public enum InputAccessMode {
    /** Transfer files from INPUT space to LOCAL space */
    TransferFromInputSpace("TransferFromInputSpace"),
    /** Transfer files from OUTPUT space to LOCAL space */
    TransferFromOutputSpace("TransferFromOutputSpace"),
    /** Do nothing */
    none("none");

    String title;

    private InputAccessMode(String s) {
        title = s;
    }

    public static InputAccessMode getAccessMode(String accessMode) {
        if (TransferFromInputSpace.title.equalsIgnoreCase(accessMode)) {
            return TransferFromInputSpace;
        } else if (TransferFromOutputSpace.title.equalsIgnoreCase(accessMode)) {
            return TransferFromOutputSpace;
        } else if (none.title.equalsIgnoreCase(accessMode)) {
            return none;
        } else {
            throw new IllegalArgumentException("Unknow input access mode : " + accessMode);
        }
    }
}
