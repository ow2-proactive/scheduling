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
package org.objectweb.proactive.core.rmi;


/**
 * @author sbeucler
 *
 */
public class MSG {
    private byte[] message;
    private String action;

    /**
     * @param action
     * @param message
     */
    public MSG(byte[] message, String action) {
        super();
        this.action = action;
        this.message = message;
    }

    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(byte[] message) {
        this.message = message;
    }

    /**
     * @return Returns the message.
     */
    public byte[] getMessage() {
        return message;
    }
}
