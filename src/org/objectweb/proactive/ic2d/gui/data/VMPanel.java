/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.ic2d.gui.data;

import org.objectweb.proactive.ic2d.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.data.NodeObject;
import org.objectweb.proactive.ic2d.data.SpyListenerImpl;
import org.objectweb.proactive.ic2d.data.VMObject;
import org.objectweb.proactive.ic2d.event.VMObjectListener;
import org.objectweb.proactive.ic2d.spy.Spy;

import java.awt.Color;


public class VMPanel extends AbstractDataObjectPanel implements VMObjectListener {
    private VMObject vmObject;
    protected PanelPopupMenu popup;
    private String alignLayout; //keep state of layout H or V

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public VMPanel(AbstractDataObjectPanel parentDataObjectPanel,
        VMObject targetVMObject) {
        super(parentDataObjectPanel,
            "VM id=" + targetVMObject.getID().toString(), "VMObject");
        activeObjectFilter.addClass(SpyListenerImpl.class.getName());
//      ebe la classe Spy est aussi filtrée par defaut
        activeObjectFilter.addClass(Spy.class.getName());
        this.vmObject = targetVMObject;
        //this.setLayout(new java.awt.GridLayout(1, 0, 4, 4));
        HostPanel parent = (HostPanel) getParentDataObjectPanel(); // get parent
        alignLayout(parent.getAlignLayout()); //the host default alignement is the worldpanel alignement
        if (targetVMObject.getProtocolId().indexOf("globus") >= 0) {
            this.setBackground(new Color(0xff, 0xd0, 0xd0));
        }
        createBorder(name);

        // The popup
        popup = new PanelPopupMenu(name);
        popup.addGenericMenu();
        popup.add(new javax.swing.AbstractAction(
                "Look for new Active Objects", null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    vmObject.sendEventsForAllActiveObjects();
                }
            });
        popup.add(new javax.swing.AbstractAction("Set update frequence", null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    changeUpdateFrequence();
                }
            });
        popup.addSeparator();
        popup.add(new javax.swing.AbstractAction("Stop monitoring this VM", null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    vmObject.destroyObject();
                }
            });

        addMouseListener(popup.getMenuMouseListener());
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements VMObjectListener -----------------------------------------------
    //
    public void nodeObjectAdded(NodeObject nodeObject) {
        NodePanel panel = new NodePanel(this, nodeObject);
        addChild(nodeObject, panel);
        nodeObject.registerListener(panel);
    }

    public void nodeObjectRemoved(NodeObject nodeObject) {
        removeChild(nodeObject);
    }

    // ebe 06/2004
    // set VM horiz or vertic layout for that VM
    // needed if there's more than one node in one VM
    public void alignLayout(String align) {
        alignLayout = align;
        setPreferredSize(null);
        if (align == "H") {
            setLayout(new java.awt.GridLayout(1, 0, 4, 4));
        } else {
            this.setLayout(new java.awt.GridLayout(0, 1, 4, 4));
        }
        revalidate();
        repaint();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected AbstractDataObject getAbstractDataObject() {
        return vmObject;
    }

    protected NodePanel getNodePanel(NodeObject nodeObject) {
        return (NodePanel) getChild(nodeObject);
    }

    protected Object[][] getDataObjectInfo() {
        return new Object[][] {
            { "ID", vmObject.getID() },
            { "Active objects", new Integer(vmObject.getActiveObjectsCount()) },
            { "Protocol identifier", vmObject.getProtocolId() }
        };
    }

    protected void setFontSize(java.awt.Font font) {
        super.setFontSize(font);
        createBorder(name);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void changeUpdateFrequence() {
        Object result = javax.swing.JOptionPane.showInputDialog(parentFrame, // Component parentComponent,
                "Please enter the new value for the frequence of the update for vm id=" +
                vmObject.getID(), // Object message,
                "Spy updates frequence", // String title,
                javax.swing.JOptionPane.PLAIN_MESSAGE, // int messageType,
                null, // Icon icon,
                null, // Object[] selectionValues,
                new Long(vmObject.getUpdateFrequence()) // Object initialSelectionValue)
            );
        if ((result == null) || (!(result instanceof String))) {
            return;
        }
        try {
            long f = Long.parseLong((String) result);
            controller.log("Setting spy update frequence for VM " +
                vmObject.getID() + "  to " + f + " ms");
            vmObject.setUpdateFrequence(f);
        } catch (NumberFormatException e) {
        }
    }

    private void createBorder(String name) {
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, name,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, defaultFont));
    }
}
