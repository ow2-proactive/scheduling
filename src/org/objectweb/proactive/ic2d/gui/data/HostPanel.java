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
import org.objectweb.proactive.ic2d.data.HostObject;
import org.objectweb.proactive.ic2d.data.VMObject;
import org.objectweb.proactive.ic2d.event.HostObjectListener;


public class HostPanel extends AbstractDataObjectPanel
    implements HostObjectListener {
    private HostObject hostObject;
    protected java.awt.Dimension minimumSize = new java.awt.Dimension(150, 80);
    protected PanelPopupMenu popup;
    public String alignLayout; //keep state of layout H or V
    public javax.swing.ButtonGroup group;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public HostPanel(AbstractDataObjectPanel parentDataObjectPanel,
        HostObject targetHostObject) {
        super(parentDataObjectPanel, targetHostObject.getHostName(),
            "HostObject");
        this.hostObject = targetHostObject;
        // Component init
        //    if (hostObject.isGlobusEnabled())
        //      setBackground(new java.awt.Color(0xff, 0xd0, 0xd0));
        //else 
        setBackground(new java.awt.Color(0xd0, 0xd0, 0xd0));
        createBorder(hostObject.getOperatingSystem());
        WorldPanel parent = (WorldPanel) getParentDataObjectPanel(); // get parent

        alignLayout(parent.getAlignLayout()); //the host default alignement is the worldpanel alignement
        // Popup menu
        popup = new PanelPopupMenu("Host " + name + " OS " +
                hostObject.getOperatingSystem());
        popup.addGenericMenu();
        popup.add(new javax.swing.AbstractAction("Look for new nodes", null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    hostObject.createAllNodes();
                }
            });
        popup.addSeparator();
        popup.add(new javax.swing.AbstractAction("Stop Monitoring this host",
                null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    hostObject.destroyObject();
                }
            });

        /*** ebe 17/06/2004 adding layout menu item rb H V *************/
        javax.swing.JMenu jvmLayoutJmenu = new javax.swing.JMenu("VM Layout");
        popup.add(jvmLayoutJmenu);
        //menu rb Horiz
        group = new javax.swing.ButtonGroup();
        javax.swing.JRadioButtonMenuItem JRadioButtonMenuItemHoriz = new javax.swing.JRadioButtonMenuItem(
                "Horizontal");
        JRadioButtonMenuItemHoriz.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    alignLayout("H");
                }
            });

        //menu rb Vertic
        javax.swing.JRadioButtonMenuItem JRadioButtonMenuItemVertic = new javax.swing.JRadioButtonMenuItem(
                "Vertical");
        JRadioButtonMenuItemVertic.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    alignLayout("V");
                }
            });

        if (alignLayout == "H") {
            JRadioButtonMenuItemHoriz.setSelected(true);
        } else {
            JRadioButtonMenuItemVertic.setSelected(true);
        }

        group.add(JRadioButtonMenuItemHoriz);
        group.add(JRadioButtonMenuItemVertic);

        jvmLayoutJmenu.add(JRadioButtonMenuItemHoriz);
        jvmLayoutJmenu.add(JRadioButtonMenuItemVertic);

        /***********************************************************/
        //Monitoring Event 
        addMouseListener(popup.getMenuMouseListener());

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                public void mouseDragged(java.awt.event.MouseEvent e) {
                    if (controller.isLayoutAutomatic()) {
                        return;
                    }

                    //ebe repaint worlpanel on host drag
                    HostPanel.this.parentDataObjectPanel.revalidate();
                    HostPanel.this.parentDataObjectPanel.repaint();
                    e.translatePoint(getX(), getY());
                    setLocation(e.getX(), e.getY());
                }
            });
        addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    HostPanel.this.parentDataObjectPanel.revalidate();
                    HostPanel.this.parentDataObjectPanel.repaint();
                }
            });
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    // ebe 06/2004
    // set VM horiz or vertic layout for that host
    public void alignLayout(String align) {
        alignLayout = align;
        setPreferredSize(null);
        if (align.equals(new String("H"))) {
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 9, 5));
        } else {
            setLayout(new javax.swing.BoxLayout(HostPanel.this,
                    javax.swing.BoxLayout.Y_AXIS));
        }

        this.alignLayoutChild(align); // 
        revalidate();
        repaint();
        HostPanel.this.parentDataObjectPanel.revalidate();
        HostPanel.this.parentDataObjectPanel.repaint();
    }

    // Set child (vm) Alignement H  / V 
    public void alignLayoutChild(String align) {
        java.util.Iterator iterator = childsIterator();
        while (iterator.hasNext()) {
            ((VMPanel) iterator.next()).alignLayout(align);
        }
    }

    // switch rb for cohence called by worldpanel
    public void switchAlignRb() {
        java.util.Enumeration e = group.getElements();
        javax.swing.JRadioButtonMenuItem rb1 = (javax.swing.JRadioButtonMenuItem) e.nextElement();
        javax.swing.JRadioButtonMenuItem rb2 = (javax.swing.JRadioButtonMenuItem) e.nextElement();
        if (rb1.isSelected()) {
            rb2.setSelected(true);
        } else {
            rb1.setSelected(true);
        }
    }

    //
    // -- implements HostObjectListener -----------------------------------------------
    //
    public void vmObjectAdded(VMObject vmObject) {
        VMPanel panel = new VMPanel(this, vmObject);
        addChild(vmObject, panel);
        vmObject.registerListener(panel);
    }

    public void vmObjectRemoved(VMObject vmObject) {
        removeChild(vmObject);
    }

    public void operatingSystemFound(String os) {
        createBorder(os);
        popup.setName("Host " + name + " OS " + os);
        repaint();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected AbstractDataObject getAbstractDataObject() {
        return hostObject;
    }

    protected VMPanel getVMPanel(VMObject vmObject) {
        return (VMPanel) getChild(vmObject);
    }

    protected Object[][] getDataObjectInfo() {
        return new Object[][] {
            { "Hostname", name }
        };
    }

    protected java.awt.Dimension getMinimumSizeInternal() {
        return minimumSize;
    }

    protected void setFontSize(java.awt.Font font) {
        super.setFontSize(font);
        createBorder(hostObject.getOperatingSystem());
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private String getNodeNameFromDialog() {
        Object result = javax.swing.JOptionPane.showInputDialog(parentFrame, // Component parentComponent,
                "Enter the name of the node to create on host " +
                hostObject.getHostName(), // Object message,
                "Creating a new remote JVM", // String title,
                javax.swing.JOptionPane.PLAIN_MESSAGE // int messageType,
            );
        if ((result == null) || (!(result instanceof String))) {
            return null;
        }
        return (String) result;
    }

    private void createBorder(String os) {
        if (os == null) {
            os = "OS Undefined";
        }
        setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1,
                    new java.awt.Color(0, 0, 128)), name + ":" + os,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION, defaultFont));
    }

    /**
     * @return Returns the alignLayout.
     */
    public String getAlignLayout() {
        return alignLayout;
    }
}
