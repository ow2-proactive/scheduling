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

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.data.WorldObject;
import org.objectweb.proactive.ic2d.data.HostObject;
import org.objectweb.proactive.ic2d.event.WorldObjectListener;
import org.objectweb.proactive.ic2d.gui.util.DialogUtils;
import org.objectweb.proactive.ic2d.gui.ActiveObjectCommunicationRecorder;
import org.objectweb.proactive.ic2d.gui.menu.StatelessMessageMonitoringMenu;

import java.rmi.dgc.VMID;

public class WorldPanel extends AbstractDataObjectPanel implements WorldObjectListener, javax.swing.Scrollable {

  private WorldObject worldObject;
  private ActiveObjectCommunicationRecorder communicationRecorder;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public WorldPanel(AbstractDataObjectPanel dataObjectPanel, WorldObject targetWorldObject, ActiveObjectCommunicationRecorder communicationRecorder) {
    super(dataObjectPanel, "IC2D", "WorldObject");
    this.worldObject = targetWorldObject;
    this.communicationRecorder = communicationRecorder;    
    setBackground(java.awt.Color.white);

    setLayout(new MyFlowLayout(java.awt.FlowLayout.CENTER, 25, 15));

    //
    // Contextual Menu
    //
    PanelPopupMenu popup = new PanelPopupMenu("World Panel");
    popup.add(new javax.swing.AbstractAction("Monitor new RMI Host", null) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        DialogUtils.openNewRMIHostDialog(parentFrame, worldObject, controller);
      }
    });
    popup.add(new javax.swing.AbstractAction("Monitor new RMI Node", null) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        DialogUtils.openNewNodeDialog(parentFrame, worldObject, controller);
      }
      });
    popup.add(new javax.swing.AbstractAction("Monitor new all JINI Hosts", null) {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  worldObject.addHosts();
	}
      });
    
    popup.add(new javax.swing.AbstractAction("Monitor new JINI Hosts", null) {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  DialogUtils.openNewJINIHostDialog(parentFrame, worldObject, controller);
	}
      });
    popup.addSeparator();
    javax.swing.JCheckBoxMenuItem check = new javax.swing.JCheckBoxMenuItem("Manual Layout", false);
    check.addActionListener(new java.awt.event.ActionListener() {
	public void actionPerformed(java.awt.event.ActionEvent e) {
	  controller.setAutomaticLayout(! controller.isLayoutAutomatic());
	  revalidate();
	  repaint();
	}
      });
    popup.add(check);
    addMouseListener(popup.getMenuMouseListener());
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public void paint(java.awt.Graphics g) {
    super.paint(g);
    if (communicationRecorder.isEnabled()) communicationRecorder.drawAllLinks(g, this.getLocationOnScreen());
  }


  //
  // -- implements WorldObjectListener -----------------------------------------------
  //


  public void hostObjectAdded(HostObject hostObject) {
    HostPanel panel = new HostPanel(this, hostObject);    
    addChild(hostObject, panel);
    hostObject.registerListener(panel);
  }
  
  
  public void hostObjectRemoved(HostObject hostObject) {
    removeChild(hostObject);
  }
  
  
  //
  // -- implements javax.swing.Scrollable -----------------------------------------------
  //

  public java.awt.Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
    return (orientation == javax.swing.SwingConstants.VERTICAL) ? Math.max(4, visibleRect.height / 20) : Math.max(4, visibleRect.width / 20);
  }

  public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
    return (orientation == javax.swing.SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
  }
  
  public boolean getScrollableTracksViewportWidth() {
    if (getParent() instanceof javax.swing.JViewport) {
      return (((javax.swing.JViewport)getParent()).getWidth() > getPreferredSize().width);
    }
    return false;
  }

  public boolean getScrollableTracksViewportHeight() {
    if (getParent() instanceof javax.swing.JViewport) {
      return (((javax.swing.JViewport)getParent()).getHeight() > getPreferredSize().height);
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
    return new Object[][] {
      };
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
          for (int i = 0 ; i < nmembers ; i++) {
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
        for (int i = 0 ; i < nmembers ; i++) {
          java.awt.Component m = target.getComponent(i);
          if (m.isVisible()) {
            int x = m.getX();
            int y = m.getY();
            //if (x < minX) minX = x;
            //if (y < minY) minY = y;
            x += m.getWidth();
            y += m.getHeight();
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
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



}
