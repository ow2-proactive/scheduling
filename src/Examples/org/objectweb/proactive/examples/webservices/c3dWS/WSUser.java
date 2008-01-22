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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.webservices.c3dWS;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.objectweb.proactive.examples.webservices.c3dWS.ws.ArrayOfInt;
import org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1Locator;
import org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1Soap;


public class WSUser implements User, Serializable {
    private String name;
    private String urlCallback;
    private Service1Locator locator;
    private Service1Soap service;

    public WSUser(String name, String urlCallback) {
        System.out.println("New web service  User  at : " + urlCallback);
        this.name = name;
        this.urlCallback = urlCallback;
        this.locator = new Service1Locator(this.urlCallback);

        try {
            this.service = locator.getService1Soap();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.c3d.User#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.c3d.User#getObject()
     */
    public Object getObject() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.c3d.User#setPixels(int[],
     *      org.objectweb.proactive.examples.c3d.Interval)
     */
    public void setPixels(int[] newPix, Interval inter) {
        ArrayOfInt aoi = new ArrayOfInt();

        aoi.set_int(newPix);
        //System.out.println("length = " + newPix.length);
        try {
            service.setPixels(this.name, aoi, inter.number, inter.total);
        } catch (RemoteException e) {
        }
    }

    public String getUrl() {
        return this.urlCallback;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.c3d.User#showMessage(java.lang.String)
     */
    public void showMessage(String s) {
        try {
            if (s.length() > 0) {
                service.showDialog(this.name, s);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.c3d.User#showUserMessage(java.lang.String)
     */
    public void showUserMessage(String s) {
        try {
            service.showUserMessage(this.name, s);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.c3d.User#dialogMessage(java.lang.String,
     *      java.lang.String)
     */
    public void dialogMessage(String subject, String message) {
        try {
            service.showUserMessageFrom(this.name, subject, message);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.c3d.User#informNewUser(int, java.lang.String)
     */
    public void informNewUser(int i, String name) {
        try {
            service.informNewUser(this.name, name, i);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.c3d.User#informUserLeft(java.lang.String)
     */
    public void informUserLeft(String name) {
        try {
            service.informUserLeft(this.name, name);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
