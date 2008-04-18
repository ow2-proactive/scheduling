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
 *  Initial developer(s):  ActiveEon Team - http://www.activeeon.com
 *  Contributor(s): 
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.util.Observable;

import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.CommunicationEditPart;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.CommunicationEditPart.DrawingStyle;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;


/**
 * A model that represents a communication between two active objects
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public class Communication extends Observable {

    private AbstractData source;
    private AbstractData target;
    private int nbCalls = 1;
    static int MAX_CALLS = 100;

    public Communication(AbstractData s, AbstractData t) {
        connect(s, t);
    }

    public void addOneCall() {
        if (nbCalls < MAX_CALLS)

            nbCalls++;

        //this is usefull when showing connection in proportional and ratio mode
        //need to take a look at this modes
        // need to take a look at the autoreset in order to make these features usefull
        //
        //Uncoment next code in order to send the notification to the edit part and update the view with rtespect to the number of connections

        //		setChanged();
        //		//if (CommunicationEditPart.drawingStyle!=DrawingStyle.FIXED)
        //		{
        //			notifyObservers(new MVCNotification(MVCNotificationTag.ACTIVE_OBJECT_ADD_COMMUNICATION,
        //	            nbCalls));
        //		}

    }

    public float getnumberOfCalls() {
        return nbCalls;
    }

    public void connect(AbstractData newSource, AbstractData newTarget) {

        //		
        //			try {
        //				Thread.sleep(1000);
        //			} catch (InterruptedException e) {
        //				// TODO Auto-generated catch block
        //				e.printStackTrace();
        //			}
        //		

        if (newSource == null || newTarget == null || newSource == newTarget) {
            throw new IllegalArgumentException();
        }

        this.source = newSource;
        this.target = newTarget;

        source.addConnection(this);
        target.addConnection(this);
    }

    public void disconnect() {
        source.removeConnection(this);
        target.removeConnection(this);

    }

    public AbstractData getSource() {
        return source;
    }

    public AbstractData getTarget() {
        return target;
    }

}
