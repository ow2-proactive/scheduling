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
package org.objectweb.proactive.ic2d.p2PMonitoring.views;

import java.awt.GridLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.ic2d.p2PMonitoring.jung.JungGUI;
import org.objectweb.proactive.p2p.v2.monitoring.Dumper;


public class P2PView extends ViewPart implements IPerspectiveListener4 {
    javax.swing.JPanel panel = new javax.swing.JPanel();
    static Display test = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                    .getShell().getDisplay();
    static protected JungGUI gui = new JungGUI(PlatformUI.getWorkbench()
                                                         .getActiveWorkbenchWindow()
                                                         .getShell().getDisplay());

    //    public static JungGUI getGUI() {
    //        return gui;
    //    }
    @Override
    public void createPartControl(Composite parent) {
        Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
        java.awt.Frame frame = SWT_AWT.new_Frame(swtAwtComponent);

        panel.setLayout(new GridLayout(1, 1));
        panel.add(gui.getPanel());
        frame.add(panel);
        System.out.println(
            "----------------------------------------- P2PView.createPartControl() static display is " +
            test);
        System.out.println("P2PView.createPartControl() non static display is " +
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
                      .getDisplay());
        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                  .addPerspectiveListener(this);
    }

    @Override
    public void setFocus() {
    }

    public static void loadDumpFile(final String filename) {
        new Thread() {
                public void run() {
                    Dumper d = new Dumper();
                    d.getP2PNetwork().addListener(gui);
                    d.createGraphFromFile(filename);
                    //   gui.createGraphFromFile2(filename);
                }
            }.start();
    }

    public static void dumpP2PNetwork(final String URL) {
        Dumper dump = new Dumper();
        dump.getP2PNetwork().addListener(gui);
        //dump.createGraphFromFile2(args[0]);
        try {
            Dumper aDump = (Dumper) ProActive.turnActive(dump);
            Dumper.requestAcquaintances(URL + "/P2PNode", aDump);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    public void perspectivePreDeactivate(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        // TODO Auto-generated method stub
    }

    public void perspectiveClosed(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        System.out.println("P2PView.perspectiveClosed()");
    }

    public void perspectiveDeactivated(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        // TODO Auto-generated method stub
    }

    public void perspectiveOpened(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        // TODO Auto-generated method stub
    }

    public void perspectiveSavedAs(IWorkbenchPage page,
        IPerspectiveDescriptor oldPerspective,
        IPerspectiveDescriptor newPerspective) {
        // TODO Auto-generated method stub
    }

    public void perspectiveChanged(IWorkbenchPage page,
        IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef,
        String changeId) {
        System.out.println("P2PView.perspectiveChanged( ) " + changeId);
        if (changeId.equals("viewHide")) {
            System.out.println("FERMETURE");
            this.gui.clear();
        }
    }

    public void perspectiveActivated(IWorkbenchPage page,
        IPerspectiveDescriptor perspective) {
        // TODO Auto-generated method stub
    }

    public void perspectiveChanged(IWorkbenchPage page,
        IPerspectiveDescriptor perspective, String changeId) {
        // TODO Auto-generated method stub
    }
}
