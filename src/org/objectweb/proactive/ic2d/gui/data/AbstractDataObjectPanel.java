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
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.event.MessageMonitoringListener;
import org.objectweb.proactive.ic2d.gui.ActiveObjectWatcher;
import org.objectweb.proactive.ic2d.gui.IC2DGUIController;
import org.objectweb.proactive.ic2d.gui.menu.MessageMonitoringMenu;
import org.objectweb.proactive.ic2d.util.ActiveObjectFilter;

/**
 * A panel for the representation of a DataObject
 */
public abstract class AbstractDataObjectPanel extends javax.swing.JPanel implements MessageMonitoringListener {

  private static final String[] COLUMN_NAMES = { "Property", "Value" };
  private static final java.awt.Color INFO_PANEL_BG_COLOR = new java.awt.Color(0xd0, 0xd0, 0xd0);

  protected static final java.awt.Font REGULAR_FONT = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12);
  protected static final java.awt.Font BIG_FONT = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14);
  protected static final java.awt.Font VERY_BIG_FONT = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16);
  protected static final java.awt.Font SMALL_FONT = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 10);
  protected static final java.awt.Font VERY_SMALL_FONT = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8);

  protected static final int REGULAR_FONT_SIZE = 10;
  protected static final int BIG_FONT_SIZE = 20;
  protected static final int VERY_BIG_FONT_SIZE = 30;
  protected static final int SMALL_FONT_SIZE = 50;
  protected static final int VERY_SMALL_FONT_SIZE = 70;

  protected static final int MINIMUM_WIDTH = 100;
  protected static final int MINIMUM_HEIGHT = 40;

  protected java.awt.Dimension minimumSize = new java.awt.Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT);

  protected String name;
  protected String type;
  protected java.awt.Frame parentFrame;
  protected ActiveObjectFilter activeObjectFilter;
  protected ActiveObjectWatcher activeObjectWatcher;
  protected AbstractDataObjectPanel parentDataObjectPanel;
  protected IC2DGUIController controller;

  protected MessageMonitoringMenu monitoringMenu;

  protected java.awt.Font defaultFont;

  // Data representation
  private java.util.HashMap childs;

  protected boolean isDestroyed;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public AbstractDataObjectPanel(
    java.awt.Frame parentFrame,
    ActiveObjectFilter activeObjectFilter,
    IC2DGUIController controller,
    ActiveObjectWatcher activeObjectWatcher,
    String name,
    String type) {
    this(name, type);
    this.activeObjectFilter = activeObjectFilter;
    this.activeObjectWatcher = activeObjectWatcher;
    this.controller = controller;
    this.parentFrame = parentFrame;
    this.defaultFont = REGULAR_FONT;
  }

  public AbstractDataObjectPanel(AbstractDataObjectPanel parentDataObjectPanel, String name, String type) {
    this(name, type);
    this.parentDataObjectPanel = parentDataObjectPanel;
    this.activeObjectFilter = parentDataObjectPanel.activeObjectFilter;
    this.activeObjectWatcher = parentDataObjectPanel.activeObjectWatcher;
    this.controller = parentDataObjectPanel.controller;
    this.parentFrame = parentDataObjectPanel.parentFrame;
    this.defaultFont = parentDataObjectPanel.defaultFont;
    //controller.log("AbstractDataObjectPanel "+name+":"+type+" created");
  }

  private AbstractDataObjectPanel(String name, String type) {
    this.name = name;
    this.type = type;
    this.childs = new java.util.HashMap();
    setSize(minimumSize);
    setToolTipText(name);
  }

  //
  // -- PUBLICS METHODS -----------------------------------------------
  //

  public java.awt.Dimension getMinimumSize() {
    if (childs.isEmpty()) {
      java.awt.Dimension d = getMinimumSizeInternal();
      if (d == null)
        return super.getMinimumSize();
      else
        return d;
    } else {
      return super.getMinimumSize();
    }
  }

  public java.awt.Dimension getPreferredSize() {
    if (childs.isEmpty()) {
      java.awt.Dimension d = getMinimumSizeInternal();
      if (d == null)
        return super.getPreferredSize();
      else
        return d;
    } else {
      return super.getPreferredSize();
    }
  }

  public boolean isDestroyed() {
    return isDestroyed;
  }

  //
  // -- implements MessageMonitoringListener -----------------------------------------------
  //

  public void viewingInEventListChanged(boolean b) {
    monitoringMenu.viewingInEventListChanged(b);
    if (b) {
      addAllActiveObjectsToWatcher();
    } else {
      removeAllActiveObjectsFromWatcher();
    }
  }

  public void monitoringRequestReceiverChanged(boolean b) {
    monitoringMenu.monitoringRequestReceiverChanged(b);
  }

  public void monitoringRequestSenderChanged(boolean b) {
    monitoringMenu.monitoringRequestSenderChanged(b);
  }

  public void monitoringReplyReceiverChanged(boolean b) {
    monitoringMenu.monitoringReplyReceiverChanged(b);
  }

  public void monitoringReplySenderChanged(boolean b) {
    monitoringMenu.monitoringReplySenderChanged(b);
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected java.awt.Dimension getMinimumSizeInternal() {
    return minimumSize;
  }

  protected AbstractDataObjectPanel getParentDataObjectPanel() {
    return parentDataObjectPanel;
  }

  protected abstract Object[][] getDataObjectInfo();

  protected abstract AbstractDataObject getAbstractDataObject();

  protected void displayDataObjectInfo() {
    javax.swing.JOptionPane.showMessageDialog(parentFrame, // Component parentComponent,
    new DataObjectInfoPanel(), // Object message,
    type + " info :" + name, // String title,
    javax.swing.JOptionPane.INFORMATION_MESSAGE // int messageType,
    );
  }

  protected void filterChangeParentNotification(String qname) {
    if (parentDataObjectPanel != null)
      parentDataObjectPanel.filterChangeParentNotification(qname);
  }

  protected synchronized ActiveObjectPanel findActiveObjectPanelByActiveObject(ActiveObject activeObject) {
    java.util.Iterator iterator = childsIterator();
    while (iterator.hasNext()) {
      AbstractDataObjectPanel o = (AbstractDataObjectPanel) iterator.next();
      ActiveObjectPanel activeObjectPanel = o.findActiveObjectPanelByActiveObject(activeObject);
      if (activeObjectPanel != null)
        return activeObjectPanel;
    }
    return null;
  }

  /**
   * return the top level parent
   */
  protected AbstractDataObjectPanel getTopLevelParent() {
    if (parentDataObjectPanel == null) {
      return this;
    } else {
      return parentDataObjectPanel.getTopLevelParent();
    }
  }

  /**
   * destroy
   */
  protected void destroy() {
    if (isDestroyed)
      return;
    isDestroyed = true;
    clearChilds();
    parentFrame = null;
    activeObjectFilter = null;
    parentDataObjectPanel = null;
    controller = null;
  }

  /**
   * clear Child
   */
  protected synchronized void clearChilds() {
    childs.clear();
  }

  /**
   * put Child
   */
  protected synchronized void putChild(AbstractDataObject key, AbstractDataObjectPanel child) {
    childs.put(key, child);
  }

  /**
   * add Child
   */
  protected synchronized void addChild(AbstractDataObject key, AbstractDataObjectPanel child) {
    //System.out.println("AbstractDataObjectPanel addChild name="+child.name+" type="+child.type);
    putChild(key, child);
    add(child);
    revalidate();
    repaint();
  }

  /**
   * remove Child
   */
  protected synchronized AbstractDataObjectPanel removeChild(AbstractDataObject key) {
    AbstractDataObjectPanel panel = (AbstractDataObjectPanel) childs.remove(key);
    if (panel != null) {
      //System.out.println("AbstractDataObjectPanel removeChild name="+panel.name+" type="+panel.type);
      panel.destroy();
      remove(panel);
      revalidate();
      repaint();
    }
    return panel;
  }

  /**
   * get Child
   */
  protected synchronized AbstractDataObjectPanel getChild(AbstractDataObject key) {
    return (AbstractDataObjectPanel) childs.get(key);
  }

  protected java.util.Iterator childsIterator() {
    return childs.values().iterator();
  }

  protected void setFontSize(int size) {
    switch (size) {
      case REGULAR_FONT_SIZE :
        setFontSize(REGULAR_FONT);
        break;
      case BIG_FONT_SIZE :
        setFontSize(BIG_FONT);
        break;
      case VERY_BIG_FONT_SIZE :
        setFontSize(VERY_BIG_FONT);
        break;
      case SMALL_FONT_SIZE :
        setFontSize(SMALL_FONT);
        break;
      case VERY_SMALL_FONT_SIZE :
        setFontSize(VERY_SMALL_FONT);
        break;
    }
    revalidate();
    repaint();
  }

  protected synchronized void setFontSize(java.awt.Font font) {
    defaultFont = font;
    java.util.Iterator iterator = childsIterator();
    while (iterator.hasNext()) {
      AbstractDataObjectPanel o = (AbstractDataObjectPanel) iterator.next();
      o.setFontSize(font);
    }
  }

  protected synchronized void addAllActiveObjectsToWatcher() {
    java.util.Iterator iterator = childsIterator();
    while (iterator.hasNext()) {
      AbstractDataObjectPanel o = (AbstractDataObjectPanel) iterator.next();
      o.addAllActiveObjectsToWatcher();
    }
  }

  protected synchronized void removeAllActiveObjectsFromWatcher() {
    java.util.Iterator iterator = childsIterator();
    while (iterator.hasNext()) {
      AbstractDataObjectPanel o = (AbstractDataObjectPanel) iterator.next();
      o.removeAllActiveObjectsFromWatcher();
    }
  }

  protected synchronized void activeObjectAddedToFilter() {
    java.util.Iterator iterator = childsIterator();
    while (iterator.hasNext()) {
      AbstractDataObjectPanel o = (AbstractDataObjectPanel) iterator.next();
      o.activeObjectAddedToFilter();
    }
  }

  protected synchronized void activeObjectRemovedFromFilter() {
    java.util.Iterator iterator = childsIterator();
    while (iterator.hasNext()) {
      AbstractDataObjectPanel o = (AbstractDataObjectPanel) iterator.next();
      o.activeObjectRemovedFromFilter();
    }
  }

  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  //
  // -- INNER CLASSES -----------------------------------------------
  //

  protected class DataObjectInfoPanel extends javax.swing.JPanel {
    public DataObjectInfoPanel() {
      super(new java.awt.GridLayout(1, 1));
      javax.swing.JTable table = new javax.swing.JTable(getDataObjectInfo(), COLUMN_NAMES);
      javax.swing.JScrollPane sp = new javax.swing.JScrollPane(table);
      setSize(table.getPreferredSize());
      sp.setBackground(INFO_PANEL_BG_COLOR);
      add(sp);
    }
  }

  protected class PanelPopupMenu extends javax.swing.JPopupMenu {

    javax.swing.JMenuItem titleItem;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public PanelPopupMenu(String name) {
      super(name);
      titleItem = add(name + " Menu");
      titleItem.setEnabled(false);
      addSeparator();
      add(new FontSizeMenu());
      monitoringMenu = new MessageMonitoringMenu("Monitor events", getAbstractDataObject());
      add(monitoringMenu);
      add(new javax.swing.AbstractAction("Informations", null) {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          displayDataObjectInfo();
        }
      });
      addSeparator();
    }

    public java.awt.event.MouseListener getMenuMouseListener() {
      return new MyMouseListener();
    }

    public void setName(String name) {
      super.setName(name);
      titleItem.setText(name);
    }

    public void addRefreshDisplayItem() {
      add(new javax.swing.AbstractAction("Refresh display") {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          AbstractDataObjectPanel.this.revalidate();
          AbstractDataObjectPanel.this.repaint();
        }
      });
    }

    //
    // -- INNER CLASSES -------------------------------------------------
    //
    private class MyMouseListener extends java.awt.event.MouseAdapter {
      public void mousePressed(java.awt.event.MouseEvent e) {
        if (e.isPopupTrigger()) {
          show(e.getComponent(), e.getX(), e.getY());
        }
      }
      public void mouseReleased(java.awt.event.MouseEvent e) {
        if (e.isPopupTrigger()) {
          show(e.getComponent(), e.getX(), e.getY());
        }
      }
    } // end inner class MyMouseListener

    private class FontSizeMenu extends javax.swing.JMenu {
      public FontSizeMenu() {
        super("Change font size");
        add(createItem("Very large font size", VERY_BIG_FONT_SIZE));
        add(createItem("Large font size", BIG_FONT_SIZE));
        add(createItem("Regular font size", REGULAR_FONT_SIZE));
        add(createItem("Small font size", SMALL_FONT_SIZE));
        add(createItem("Very small font size", VERY_SMALL_FONT_SIZE));
      }

      private javax.swing.JMenuItem createItem(String text, final int size) {
        javax.swing.JMenuItem menuItem = new javax.swing.JMenuItem(text);
        menuItem.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent event) {
            setFontSize(size);
          }
        });
        return menuItem;
      }
    } // end inner class FontSizeMenu
  } // end inner class PanelPopupMenu

} // end class AbstractDataObjectPanelDataObjectPanel