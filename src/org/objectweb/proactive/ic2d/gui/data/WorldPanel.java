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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.data.HostObject;
import org.objectweb.proactive.ic2d.data.WorldObject;
import org.objectweb.proactive.ic2d.event.WorldObjectListener;
import org.objectweb.proactive.ic2d.gui.ActiveObjectCommunicationRecorder;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredHost;
import org.objectweb.proactive.ic2d.gui.util.DialogUtils;
import org.objectweb.proactive.ic2d.util.MonitorThread;


public class WorldPanel extends AbstractDataObjectPanel
    implements WorldObjectListener, javax.swing.Scrollable {
    private WorldObject worldObject;
    private ActiveObjectCommunicationRecorder communicationRecorder;
    private String alignLayout = "H"; //keep state of layout H or V
    private MonitorThread monitorThread;
    private Timer timer;
    private int w;
    private int h;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public WorldPanel(AbstractDataObjectPanel dataObjectPanel,
        WorldObject targetWorldObject,
        ActiveObjectCommunicationRecorder communicationRecorder) {
        super(dataObjectPanel, "IC2D", "WorldObject");
        this.worldObject = targetWorldObject;
        this.communicationRecorder = communicationRecorder;

        setBackground(java.awt.Color.white);

        setLayout(new MyFlowLayout(java.awt.FlowLayout.CENTER, 25, 15));

        //
        // Contextual Menu
        //
        final WorldPanel thisPanel = this;
        PanelPopupMenu popup = new PanelPopupMenu("World Panel");
        popup.add(new javax.swing.AbstractAction("Monitor a new RMI Host", null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    DialogUtils.openNewRMIHostDialog(parentFrame, thisPanel,
                        controller);
                }
            });

        //        popup.add(new javax.swing.AbstractAction("Monitor a new RMI Node", null) {
        //                public void actionPerformed(java.awt.event.ActionEvent e) {
        //                    DialogUtils.openNewNodeDialog(parentFrame, worldObject,
        //                        controller);
        //                }
        //            });
        popup.add(new javax.swing.AbstractAction("Monitor all JINI Hosts", null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    DialogUtils.openNewJINIHostsDialog(parentFrame, thisPanel,
                        controller);
                }
            });

        popup.add(new javax.swing.AbstractAction("Monitor a new JINI Host", null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    DialogUtils.openNewJINIHostDialog(parentFrame, thisPanel,
                        controller);
                }
            });
        popup.addSeparator();
        popup.addGenericMenu();

        /*** modifying automatic Hostlayout menu item rb automatic / Manual ebe 06-2004 ***/

        //    javax.swing.JCheckBoxMenuItem check = new javax.swing.JCheckBoxMenuItem("Manual Layout", false);
        //    check.addActionListener(new java.awt.event.ActionListener() {
        //	public void actionPerformed(java.awt.event.ActionEvent e) {
        //	  controller.setAutomaticLayout(! controller.isLayoutAutomatic());
        //	  revalidate();
        //	  repaint();
        //	}
        //      });
        //    popup.add(check);
        javax.swing.JMenu LayoutJmenu = new javax.swing.JMenu("Host Layout");
        popup.add(LayoutJmenu);

        javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
        javax.swing.JRadioButtonMenuItem JRadioButtonMenuItemOn = new javax.swing.JRadioButtonMenuItem(
                "Automatic");
        JRadioButtonMenuItemOn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    controller.setAutomaticLayout(!controller.isLayoutAutomatic());
                    revalidate();
                    repaint();
                }
            });

        javax.swing.JRadioButtonMenuItem JRadioButtonMenuItemOff = new javax.swing.JRadioButtonMenuItem(
                "Manual");
        JRadioButtonMenuItemOff.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    controller.setAutomaticLayout(!controller.isLayoutAutomatic());
                    revalidate();
                    repaint();
                }
            });

        JRadioButtonMenuItemOn.setSelected(true);
        group.add(JRadioButtonMenuItemOn);
        popup.add(JRadioButtonMenuItemOn);
        group.add(JRadioButtonMenuItemOff);
        popup.add(JRadioButtonMenuItemOff);

        LayoutJmenu.add(JRadioButtonMenuItemOn);
        LayoutJmenu.add(JRadioButtonMenuItemOff);

        /***********************************************************/
        /*** adding Horizontal/vertical Hostlayout radio button menu item ebe 06-2004 ***/
        javax.swing.JMenu hostLayoutJmenu = new javax.swing.JMenu("VM Layout");
        popup.add(hostLayoutJmenu);

        //menu rb Horiz
        javax.swing.ButtonGroup group2 = new javax.swing.ButtonGroup();
        javax.swing.JRadioButtonMenuItem JRadioButtonMenuItemHoriz = new javax.swing.JRadioButtonMenuItem(
                "Horizontal");
        JRadioButtonMenuItemHoriz.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    alignLayoutChild("H");
                }
            });

        //menu rb Vertic
        javax.swing.JRadioButtonMenuItem JRadioButtonMenuItemVertic = new javax.swing.JRadioButtonMenuItem(
                "Vertical");
        JRadioButtonMenuItemVertic.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    alignLayoutChild("V");
                }
            });

        JRadioButtonMenuItemHoriz.setSelected(true);
        group2.add(JRadioButtonMenuItemHoriz);
        popup.add(JRadioButtonMenuItemHoriz);
        group2.add(JRadioButtonMenuItemVertic);
        popup.add(JRadioButtonMenuItemVertic);

        hostLayoutJmenu.add(JRadioButtonMenuItemHoriz);
        hostLayoutJmenu.add(JRadioButtonMenuItemVertic);

        /***********************************************************/
        addMouseListener(popup.getMenuMouseListener());

        // initialize the monitor thread
        monitorThread = new MonitorThread("3", worldObject, controller);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void paint(java.awt.Graphics g) {
        Dimension dim = getSize();
        int w2 = dim.width;
        int h2 = dim.height;

        //first time we are on display
        if ((w == 0) && (h == 0)) {
            w = w2;
            h = h2;
            this.setDirty(true);
        }

        //we have been moved since the last time, we need 
        //to force a redraw of the arrows
        if ((w2 != w) || (h2 != h)) {
            w = w2;
            h = h2;
            this.setDirty(true);
        }
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        if (communicationRecorder.isEnabled()) {
            //we create a fully transparent image
            //we indicate to the communication recorder if we are dirty
            //which means the image would have to be redrawn
            BufferedImage bi = communicationRecorder.drawAllLinksOffScreen(w2,
                    h2, this.getLocationOnScreen(), this.isDirty());
            this.setDirty(false);
            Graphics tempGraphics = bi.createGraphics();
            g2.drawImage(bi, 0, 0, this);
        }
    }

    /**
     * Change the layout
     * Set the dirty flag to true
     * @param align
     */
    public void alignLayoutChild(String align) {
        alignLayout = align;
        java.util.Iterator iterator = childsIterator();
        while (iterator.hasNext()) {
            HostPanel hostchild = (HostPanel) iterator.next();
            if (hostchild.alignLayout != align) {
                hostchild.alignLayout(align);
                hostchild.switchAlignRb();
            }
        }
        this.setDirty(true);
    }

    //
    // -- implements WorldObjectListener -----------------------------------------------
    //
    public void hostObjectAdded(HostObject hostObject) {
        HostPanel panel = new HostPanel(this, hostObject);
        addChild(hostObject, panel);
        hostObject.registerListener(panel);
    }

    public void monitoredHostAdded(MonitoredHost host) {
        monitorThread.addMonitoredHost(host);
    }

    public void monitoredHostAdded(String host, String protocol) {
        monitorThread.addMonitoredHost(host, protocol);
    }

    public void hostObjectRemoved(HostObject hostObject) {
        removeChild(hostObject);
    }

    public void monitoredHostRemoved(MonitoredHost host) {
        monitorThread.addMonitoredHost(host);
    }

    public MonitorThread getMonitorThread() {
        return monitorThread;
    }

    public WorldObject getWorldObject() {
        return worldObject;
    }

    public ActiveObjectCommunicationRecorder getCommunicationRecorder() {
        return communicationRecorder;
    }

    /**
     * stop to monitor an host
     * @param hostObject host to do not monitor anymore.
     */
    public void stopMonitorHost(HostObject hostObject) {
        String hostname = UrlBuilder.removePortFromHost(hostObject.getHostName());
        int port = UrlBuilder.getPortFromUrl(hostObject.getHostName());
        MonitoredHost object = new MonitoredHost(hostname, port,
                hostObject.getMonitoredProtocol());
        monitorThread.addObjectToSkip(object);
        monitorThread.removeAsso(object);
    }

    //
    // -- implements javax.swing.Scrollable -----------------------------------------------
    //
    public java.awt.Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect,
        int orientation, int direction) {
        return (orientation == javax.swing.SwingConstants.VERTICAL)
        ? Math.max(4, visibleRect.height / 20)
        : Math.max(4, visibleRect.width / 20);
    }

    public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect,
        int orientation, int direction) {
        return (orientation == javax.swing.SwingConstants.VERTICAL)
        ? visibleRect.height : visibleRect.width;
    }

    public boolean getScrollableTracksViewportWidth() {
        if (getParent() instanceof javax.swing.JViewport) {
            return (((javax.swing.JViewport) getParent()).getWidth() > getPreferredSize().width);
        }
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof javax.swing.JViewport) {
            return (((javax.swing.JViewport) getParent()).getHeight() > getPreferredSize().height);
        }
        return false;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected AbstractDataObject getAbstractDataObject() {
        return worldObject;
    }

    protected HostPanel getHostPanel(HostObject hostObject) {
        return (HostPanel) getChild(hostObject);
    }

    protected Object[][] getDataObjectInfo() {
        return new Object[][] {  };
    }

    protected void filterChangeParentNotification(String qname) {
        activeObjectAddedToFilter();
        revalidate();
        repaint();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    //
    // -- INNER CLASSES -----------------------------------------------
    //
    public class MyFlowLayout extends java.awt.FlowLayout {

        /**
         * Constructs a new Flow Layout with a centered alignment and a
         * default 5-unit horizontal and vertical gap.
         */
        public MyFlowLayout() {
            super();
        }

        /**
         * Constructs a new Flow Layout with the specified alignment and a
         * default 5-unit horizontal and vertical gap.
         * The value of the alignment argument must be one of
         * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
         * or <code>FlowLayout.CENTER</code>.
         * @param align the alignment value
         */
        public MyFlowLayout(int align) {
            super(align);
        }

        /**
         * Creates a new flow layout manager with the indicated alignment
         * and the indicated horizontal and vertical gaps.
         * <p>
         * The value of the alignment argument must be one of
         * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
         * or <code>FlowLayout.CENTER</code>.
         * @param      align   the alignment value.
         * @param      hgap    the horizontal gap between components.
         * @param      vgap    the vertical gap between components.
         */
        public MyFlowLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        /**
         * Lays out the container. This method lets each component take
         * its preferred size by reshaping the components in the
         * target container in order to satisfy the constraints of
         * this <code>FlowLayout</code> object.
         * @param target the specified component being laid out.
         * @see java.awt.Container
         * @see java.awt.Container#doLayout
         */
        public void layoutContainer(java.awt.Container target) {
            if (controller.isLayoutAutomatic()) {
                super.layoutContainer(target);
            } else {
                synchronized (target.getTreeLock()) {
                    int nmembers = target.getComponentCount();
                    for (int i = 0; i < nmembers; i++) {
                        java.awt.Component m = target.getComponent(i);
                        if (m.isVisible()) {
                            java.awt.Dimension d = m.getPreferredSize();
                            m.setSize(d.width, d.height);
                        }
                    }
                }
            }
        }

        /**
         * Returns the preferred dimensions for this layout given the components
         * in the specified target container.
         * @param target the component which needs to be laid out
         * @return    the preferred dimensions to lay out the
         *                    subcomponents of the specified container.
         * @see java.awt.Container
         * @see #minimumLayoutSize
         * @see java.awt.Container#getPreferredSize
         */
        public java.awt.Dimension preferredLayoutSize(java.awt.Container target) {
            synchronized (target.getTreeLock()) {
                int maxX = 0;
                int maxY = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    java.awt.Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        int x = m.getX();
                        int y = m.getY();

                        //if (x < minX) minX = x;
                        //if (y < minY) minY = y;
                        x += m.getWidth();
                        y += m.getHeight();
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                    }
                }
                return new java.awt.Dimension(maxX, maxY);
            }
        }

        /**
         * Returns the minimum dimensions needed to layout the components
         * contained in the specified target container.
         * @param target the component which needs to be laid out
         * @return    the minimum dimensions to lay out the
         *                    subcomponents of the specified container.
         * @see #preferredLayoutSize
         * @see java.awt.Container
         * @see java.awt.Container#doLayout
         */
        public java.awt.Dimension minimumLayoutSize(java.awt.Container target) {
            return preferredLayoutSize(target);
        }
    }

    /**
     * @return Returns the alignLayout.
     */
    public String getAlignLayout() {
        return alignLayout;
    }
}
