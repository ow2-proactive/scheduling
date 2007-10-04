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
package org.objectweb.proactive.infrastructuremanager.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.infrastructuremanager.views.Console;
import org.objectweb.proactive.infrastructuremanager.views.IMViewInfrastructure;


public class IMData implements Runnable {
    private URI uri;
    private IMViewInfrastructure view;
    private IMAdmin admin;
    private IMMonitoring monitoring;
    private int freeNode;
    private int busyNode;
    private int downNode;
    private ArrayList<IMNode> infrastructure;
    private long ttr = 5;

    public IMData() {
    }

    public IMData(String urlString, IMViewInfrastructure view) {
        try {
            uri = new URI(urlString);
            this.view = view;
            admin = IMFactory.getAdmin(uri);
            monitoring = IMFactory.getMonitoring(uri);
            ProFuture.waitFor(monitoring);
            ProFuture.waitFor(admin);
        } catch (ActiveObjectCreationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<IMNode> getInfrastructure() {
        return infrastructure;
    }

    public IMAdmin getAdmin() {
        return admin;
    }

    public int getFree() {
        return freeNode;
    }

    public int getBusy() {
        return busyNode;
    }

    public int getDown() {
        return downNode;
    }

    public void updateInfrastructure() {
        infrastructure = monitoring.getListAllIMNodes();
        freeNode = monitoring.getNumberOfFreeResource().intValue();
        busyNode = monitoring.getNumberOfBusyResource().intValue();
        downNode = monitoring.getNumberOfDownResource().intValue();

        Collections.sort(infrastructure);
    }

    public void run() {
        while (view != null) {
            Console.getInstance().log("Refresh");
            updateInfrastructure();
            view.getParent().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        view.drawInfrastructure();
                    }
                });
            try {
                Thread.sleep(ttr * 1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public long getTTR() {
        return ttr;
    }

    public void setTTR(long t) {
        Console.getInstance()
               .log("Set TTR : Time To Refresh = " + t + " seconds");
        ttr = t;
        view.threadRefresh.interrupt();
    }
}
