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
package org.objectweb.proactive.ic2d.gui;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.event.MessageEvent;
import org.objectweb.proactive.ic2d.event.CommunicationEventListener;
import org.objectweb.proactive.ic2d.spy.SpyEvent;
import org.objectweb.proactive.ic2d.spy.SpyMessageEvent;
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.data.IC2DObject;
import org.objectweb.proactive.ic2d.gui.data.UniqueIDDropTargetListener;
import org.objectweb.proactive.ic2d.gui.recording.PlayerFrameTimeLine;


public class EventListsPanel extends javax.swing.JPanel implements CommunicationEventListener, ActiveObjectWatcher {

  private static final String IMAGES_DIR = "org/objectweb/proactive/ic2d/gui/images/";
  private static final String REPLY_RECEIVED_ICON = IMAGES_DIR+"ReplyReceived.gif";
  private static final String REPLY_SENT_ICON = IMAGES_DIR+"ReplySent.gif";
  private static final String REQUEST_RECEIVED_ICON = IMAGES_DIR+"RequestReceived.gif";
  private static final String REQUEST_SENT_ICON = IMAGES_DIR+"RequestSent.gif";
  private static final String WAITING_FOR_REQUEST_ICON = IMAGES_DIR+"WaitingForRequest.gif";
  
  private static final int RELATED_EVENT_COLOR_MODE = 1;
  private static final int ABSOLUTE_EVENT_COLOR_MODE = 2;
  
  private static final java.awt.Color LIST_BG_COLOR = new java.awt.Color(0, 0, 80);
  private static final java.awt.Font MFONT = new java.awt.Font("Dialog", 1, 10);
  private static final java.awt.Color[] SHADES;
  static {
    // Create the SHADES!
    SHADES = new java.awt.Color[21];
    for (int i = 0; i < 10; i++) {
      SHADES[i] = new java.awt.Color(0, 0, 50 + i * 20);
    }
    SHADES[10] = java.awt.Color.orange;
    for (int i = 11; i < 21; i++) {
      SHADES[20 - (i - 11)] = new java.awt.Color(50 + (i - 11) * 20, 0, 0);
    }
  }

  private javax.swing.ImageIcon requestReceivedIcon;
  private javax.swing.ImageIcon replySentIcon;
  private javax.swing.ImageIcon requestSentIcon;
  private javax.swing.ImageIcon replyReceivedIcon;
  private javax.swing.ImageIcon waitingForRequestIcon;
                     
  /**
   * KEY: id
   * VAL: corresponding JList [model = default]
   */
  private java.util.HashMap objectTrackPanelMap;
  private BoundedCircularArrayList events;
  
  /**
   * The array of strings representing the state of a requestEvent
   */
  private int colorMode = RELATED_EVENT_COLOR_MODE;
  public javax.swing.JPopupMenu popup;
  private javax.swing.JPanel centerPanel;
  private PlayerFrameTimeLine recorder;

  protected IC2DGUIController controller;
  protected IC2DObject ic2dObject;
  
  /** enables this component to be a dropTarget */
  private java.awt.dnd.DropTarget dropTarget;


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public EventListsPanel(IC2DObject ic2dObject, IC2DGUIController controller) {
    this.ic2dObject = ic2dObject;
    this.controller = controller;
        
    // dnd stuff
    dropTarget = new java.awt.dnd.DropTarget(this, java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE, new MyDropTargetListener(), true);

    objectTrackPanelMap = new java.util.HashMap();
    events = new BoundedCircularArrayList(1000);
    recorder = new PlayerFrameTimeLine(this, ic2dObject.getController());

    setLayout(new java.awt.BorderLayout());
    {
      javax.swing.JPanel tools = new javax.swing.JPanel();
      final javax.swing.JButton colorButton = new javax.swing.JButton("Related events");
      colorButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (colorMode == RELATED_EVENT_COLOR_MODE) {
            colorButton.setText("Absolute events");
            colorMode = ABSOLUTE_EVENT_COLOR_MODE;
          } else {
            colorButton.setText("Related events");
            colorMode = RELATED_EVENT_COLOR_MODE;
          }
        }
      });
      tools.add(colorButton); 
      {
      javax.swing.JButton b = new javax.swing.JButton("Messages recorder");
      b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          recorder.setVisible(! recorder.isVisible());
        }
      });
      tools.add(b);
      } 
      {
      javax.swing.JButton b = new javax.swing.JButton("Clear all events");
      b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          clearAll();
        }
      });
      tools.add(b);
      }
      {
      javax.swing.JButton b = new javax.swing.JButton("Legend");
      b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          showLegend();
        }
      });
      tools.add(b);
      }
      add(tools, java.awt.BorderLayout.SOUTH);
    }
    
    centerPanel = new javax.swing.JPanel();
    add(centerPanel, java.awt.BorderLayout.CENTER);
	
    // main area
    centerPanel.setBackground(java.awt.Color.white);

    // Lists pane
    java.awt.GridLayout grid = new java.awt.GridLayout(1, 0);
    grid.setHgap(5);
    centerPanel.setLayout(grid);
    validate();
    loadIcon();
  }


  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  // 

  public void clearAll() {
    synchronized (objectTrackPanelMap) {
      java.util.Iterator i = objectTrackPanelMap.values().iterator();
      while (i.hasNext()) {
        // clean the ui
        ObjTrackPanel otp = (ObjTrackPanel) i.next();
        otp.getListModel().clear();
      }
    }
    // clean the event vector
    events.clear();
  }
  

  //
  // -- implements ActiveObjectWatcher -----------------------------------------------
  //

  public void addActiveObject(ActiveObject activeObject) {
    if (objectTrackPanelMap.containsKey(activeObject.getID())) return;
    ObjTrackPanel otp = new ObjTrackPanel(activeObject);
    synchronized (objectTrackPanelMap) {
      objectTrackPanelMap.put(activeObject.getID(), otp);
    }
    centerPanel.add(otp);
    revalidate();
    repaint();
  }
  
  public void removeActiveObject(ActiveObject activeObject) {
    ObjTrackPanel o;
    synchronized (objectTrackPanelMap) {
      o = (ObjTrackPanel) objectTrackPanelMap.remove(activeObject.getID());
    }
    if (o == null) return;
    centerPanel.remove(o);
    revalidate();
    repaint();
  }
  

  //
  // -- implements CommunicationEventListener -----------------------------------------------
  //

  public void objectWaitingForRequest(ActiveObject object, SpyEvent spyEvent) {
    recordEvent(object, spyEvent, true);
  }


  public void objectWaitingByNecessity(ActiveObject object, SpyEvent spyEvent) {
    recordEvent(object, spyEvent, true);
  }


  public void requestMessageSent(ActiveObject object, SpyEvent spyEvent) {
    recordEvent(object, spyEvent, false);
  }


  public void replyMessageSent(ActiveObject object, SpyEvent spyEvent) {
    recordEvent(object, spyEvent, false);
  }


  public void requestMessageReceived(ActiveObject object, SpyEvent spyEvent) {
    recordEvent(object, spyEvent, false);
  }


  public void replyMessageReceived(ActiveObject object, SpyEvent spyEvent) {
    recordEvent(object, spyEvent, false);
  }

  public void allEventsProcessed() {
    //Code for downScrolling should be there...
    synchronized (objectTrackPanelMap) {
      java.util.Iterator i = objectTrackPanelMap.values().iterator();
      while (i.hasNext()) {
        // clean the ui
        ObjTrackPanel otp = (ObjTrackPanel) i.next();
        otp.scrollDown();
      }
    }
    revalidate();
    repaint();
  }



  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private void loadIcon() {
    // images
    ClassLoader c = this.getClass().getClassLoader();
    replyReceivedIcon = new javax.swing.ImageIcon(c.getResource(REPLY_RECEIVED_ICON));
    if (replyReceivedIcon == null)
      controller.log("Can't load image "+REPLY_RECEIVED_ICON);
    replySentIcon = new javax.swing.ImageIcon(c.getResource(REPLY_SENT_ICON));
    if (replySentIcon == null)
      controller.log("Can't load image "+REPLY_SENT_ICON);
    requestReceivedIcon = new javax.swing.ImageIcon(c.getResource(REQUEST_RECEIVED_ICON));
    if (requestReceivedIcon == null)
      controller.log("Can't load image "+REQUEST_RECEIVED_ICON);
    requestSentIcon = new javax.swing.ImageIcon(c.getResource(REQUEST_SENT_ICON));
    if (requestSentIcon == null)
      controller.log("Can't load image "+REQUEST_SENT_ICON);
    waitingForRequestIcon = new javax.swing.ImageIcon(c.getResource(WAITING_FOR_REQUEST_ICON));
    if (waitingForRequestIcon == null)
      controller.log("Can't load image "+WAITING_FOR_REQUEST_ICON);
  }
  

  private ObjTrackPanel getObjTrackPanel(UniqueID id) {
    synchronized (objectTrackPanelMap) {
      return (ObjTrackPanel) objectTrackPanelMap.get(id);
    }
  }


  /**
   * Adds the event to the lists and to the event vec
   */
  private void recordEvent(final ActiveObject obj, final SpyEvent evt, final boolean dontRepeatSameType) {
    //System.out.println("Record event evt="+evt);
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          ObjTrackPanel o = getObjTrackPanel(obj.getID());
          if (o == null) return;
          if (dontRepeatSameType && ! o.getListModel().isEmpty()) {
            SpyEvent lastElt = (SpyEvent) o.getListModel().lastElement();
            if (lastElt.getType() == evt.getType()) return;
          }
          o.getListModel().addEvent(evt);
          events.add(evt);
          recorder.recordEvent(obj, evt);
        }
      });
  } // end recordEvent



  private void showLegend() {
    javax.swing.JOptionPane.showMessageDialog(
          this,                                         // Component parentComponent,
          new LegendPanel(),                            // Object message,
          "EventsList Legend",                          // String title,
          javax.swing.JOptionPane.INFORMATION_MESSAGE   // int messageType,
        );
  }


  //
  // -- INNER CLASSES -----------------------------------------------
  //  

  private class MyListSelectionListener implements javax.swing.event.ListSelectionListener {
  
    /**
     * When an item is selected, 
     * this method is fired and changes the colors of the other items
     * + last 5 requests in a shade of red [SHADES [0-4]
     * + current shade[5]
     * + next 5 SHADES[6-10]
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) return;
      Object o = ((javax.swing.JList)e.getSource()).getSelectedValue();
      if (! (o instanceof SpyMessageEvent)) return;      
      SpyMessageEvent m = (SpyMessageEvent) o;
      switch (colorMode) {
        case RELATED_EVENT_COLOR_MODE:
          setEventPosition(-1);
          UniqueID id = m.getBodyID();
          if (id == null) return;
          java.util.LinkedList bef = getMsgBefore(m, id);
          java.util.LinkedList aft = getMsgAfter(m, id);
          computeColor(m, 10, bef, aft);
          int a = 4;
          for (int i = 1; i < a && i < bef.size(); i++)
            if (bef.get(i) == null)
              a++;
            else computeColor((SpyEvent)bef.get(i), 10 - 2 * i + 2 * (a - 4), bef, aft);
          int b = 4;
          for (int i = 1; i < b && i < aft.size(); i++) {
            if (aft.get(i) == null)
              b++;
            else computeColor((SpyEvent)aft.get(i), 10 + 2 * i - 2 * (b - 4), bef, aft);
          }
        break;
         
         
        case ABSOLUTE_EVENT_COLOR_MODE:
          if (m.getSequenceNumber() == 0) {
            setEventPosition(-1);
            m.setPos(9);
          } else {
            for (int i = 0; i < events.size(); i++) {
              SpyEvent tmp = (SpyEvent)events.get(i);
              tmp.setPos(-1);
              if (tmp instanceof SpyMessageEvent) {
                SpyMessageEvent myEvent = (SpyMessageEvent) tmp;
                if (m.matches(myEvent)) tmp.setPos(getPosFromType(myEvent.getType()));
              }
            }
          }
        break;
      }
      repaint();
    }
    
    
    private int getPosFromType(int type) {
      switch (type) {
        case SpyEvent.REQUEST_SENT_MESSAGE_TYPE :
          return 19;
        case SpyEvent.REQUEST_RECEIVED_MESSAGE_TYPE :
          return 17;
        case SpyEvent.REPLY_SENT_MESSAGE_TYPE :
          return 13;
        case SpyEvent.REPLY_RECEIVED_MESSAGE_TYPE :
          return 11;
      }
      return -1;
    }
    
    /**
     * Compute the color given the position
     * and set the one of the message and his peer.
     */
    private void computeColor(SpyEvent m, int pos, java.util.LinkedList bef, java.util.LinkedList aft) {
      m.setPos(pos);
      if (! (m instanceof SpyMessageEvent)) return;
      SpyMessageEvent myEvent = (SpyMessageEvent) m;
      if (myEvent.getSequenceNumber() == 0) return;
      for (int i = 0; i < events.size(); i++) {
        Object o = events.get(i);
        if (o instanceof SpyMessageEvent) {
          SpyMessageEvent myEvent2 = (SpyMessageEvent) o;
          if (myEvent.matches(myEvent2)) myEvent2.setPos(pos);
        }
      }
    }
    

    private java.util.LinkedList getMsgBefore(SpyMessageEvent target, UniqueID id) {
      java.util.LinkedList result = new java.util.LinkedList();
      int index = events.indexOf(target);
      if (index == -1) return result;
      for (int i = index; i > 0; i--) {
        Object o = events.get(i);
        if (o instanceof SpyMessageEvent) {      
          SpyMessageEvent myEvent = (SpyMessageEvent) o;
          if (myEvent.wasSent()) {
            if (id.equals(myEvent.getSourceBodyID())) result.add(o);
          } else {
            if (id.equals(myEvent.getDestinationBodyID())) result.add(o);
          }
        }
      }
      //System.out.println(result.size()+" size");
      return result;
    }
  
  
    private java.util.LinkedList getMsgAfter(SpyMessageEvent target, UniqueID id) {
      java.util.LinkedList result = new java.util.LinkedList();
      int index = events.indexOf(target);
      if (index == -1) return result;
      for (int i = index; i < events.size(); i++) {
        Object o = events.get(i);
        if (o instanceof SpyMessageEvent) {      
          SpyMessageEvent myEvent = (SpyMessageEvent) o;
          if (myEvent.wasSent()) {
            if (id.equals(myEvent.getSourceBodyID())) result.add(o);
          } else {
            if (id.equals(myEvent.getDestinationBodyID())) result.add(o);
          }
        }
      }
      //	System.out.println(result.size()+" size");
      return result;
    }


    private void setEventPosition(int value) {
      for (int i = 0; i < events.size(); i++) {
        SpyEvent tmp = (SpyEvent)events.get(i);
        tmp.setPos(value);
      }
    }

  }
  
  
  
  
  

  /**
   * CELL RENDERER
   */
  private class TimeCellRenderer extends javax.swing.DefaultListCellRenderer {

    public TimeCellRenderer() {
      setOpaque(true);
      setHorizontalAlignment(LEFT);
      setVerticalAlignment(CENTER);
      setFont(MFONT);
    }

    /**
     * This method sorts the events
     */
    public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object val, int index, boolean isSelected, boolean cellHasFocus) {
      if (! (val instanceof SpyEvent)) return this;
      SpyEvent event = (SpyEvent) val;
      switch (event.getType()) {
        case SpyEvent.REQUEST_SENT_MESSAGE_TYPE:
          {
            SpyMessageEvent rq = (SpyMessageEvent)val;
            UniqueID id = rq.getDestinationBodyID();
            setToolTipText("Request sent to " + id);
            if (requestSentIcon != null) setIcon(requestSentIcon);
            formatItem(rq, id);
          }
          break;
        case SpyEvent.REQUEST_RECEIVED_MESSAGE_TYPE:
          {
            SpyMessageEvent rq = (SpyMessageEvent)val;
            UniqueID id = rq.getSourceBodyID();
            setToolTipText("Request received from " + id);
            if (requestReceivedIcon != null) setIcon(requestReceivedIcon);
            formatItem(rq, id);
          }
          break;
          
          
        case SpyEvent.REPLY_SENT_MESSAGE_TYPE:
          {
            SpyMessageEvent rp = (SpyMessageEvent)val;
            UniqueID id = rp.getDestinationBodyID();
            setToolTipText("Reply sent to " + id);
            if (replySentIcon != null) setIcon(replySentIcon);
            formatItem(rp, id);
          }
          break;
        case SpyEvent.REPLY_RECEIVED_MESSAGE_TYPE:
          {
            SpyMessageEvent rp = (SpyMessageEvent)val;
            UniqueID id = rp.getSourceBodyID();
            setToolTipText("Reply received from " + id);
            if (replyReceivedIcon != null) setIcon(replyReceivedIcon);
            formatItem(rp, id);
          }
          break;
          
          
        case SpyEvent.OBJECT_WAIT_BY_NECESSITY_TYPE:
          setToolTipText("");
          setText("-- ObjectWaitByNecessity");
          setBackground(java.awt.Color.white);
          if (waitingForRequestIcon != null) setIcon(waitingForRequestIcon);
          break;
          
          
        case SpyEvent.OBJECT_WAIT_FOR_REQUEST_TYPE:
          setToolTipText("");
          setText("-- ObjectWaitForRequest");
          setBackground(java.awt.Color.white);
          if (waitingForRequestIcon != null) setIcon(waitingForRequestIcon);
          break;
      }
      return this;
    }
    
    private void formatItem(SpyMessageEvent msg, UniqueID peerID) {
      // The sender or receiver is..
      ActiveObject peer = null;
      if (peerID != null)
        peer = ic2dObject.findActiveObjectById(peerID);
      if (peer != null)
        setText("[" + peer.getName() + "]" + msg.getMethodName());
      else
        setText("[?]" + msg.getMethodName());
      // Background
      if (msg.getPos() <= -1 || msg.getPos() > 21)
        setBackground(java.awt.Color.white);
      else setBackground(SHADES[msg.getPos()]);
    }
  }









  /**
   * ObjectTracker 
   * Contains the name of the object
   * and the list
   */
  private class ObjTrackPanel extends javax.swing.JPanel {

    private javax.swing.JList list;
    private EventListModel listModel;
    private javax.swing.JScrollBar verticalScrollBar;


    public ObjTrackPanel(ActiveObject activeObject) {
      setToolTipText(activeObject.getID().toString());
	    setSize(150,200);
      // Title
      javax.swing.JLabel lTitle = new javax.swing.JLabel(activeObject.getName(), javax.swing.JLabel.CENTER);
      lTitle.setForeground(java.awt.Color.white);
      lTitle.setBackground(LIST_BG_COLOR);
      lTitle.setOpaque(true);

      setLayout(new java.awt.BorderLayout());
      add(lTitle, java.awt.BorderLayout.NORTH);

      listModel = new EventListModel();
      list = new javax.swing.JList(listModel);
      list.addListSelectionListener(new MyListSelectionListener());
      javax.swing.JScrollPane scList = new javax.swing.JScrollPane(list, javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      verticalScrollBar = scList.getVerticalScrollBar();
      list.setCellRenderer(new TimeCellRenderer());
      list.setBackground(java.awt.Color.white);
      add(scList, java.awt.BorderLayout.CENTER);
      setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.gray));

      EventListPopupMenu popup = new EventListPopupMenu(activeObject);
      addMouseListener(popup.getMenuMouseListener());
    }
    
    public EventListModel getListModel() {
      return listModel;
    }
    
    public void scrollDown() {
      verticalScrollBar.setValue(verticalScrollBar.getMaximum() + 50);
    }


    public java.awt.Dimension getPreferredSize() {
      java.awt.Dimension d = super.getPreferredSize();
      d.width = 150;
      return d;
    }
  }





  private class EventListPopupMenu extends javax.swing.JPopupMenu {
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public EventListPopupMenu(final ActiveObject activeObject) {
      super("EventList Menu");
      add(new javax.swing.AbstractAction("Remove this ActiveObject from the EventList", null) {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          removeActiveObject(activeObject);
        }
      });
    }  
  
    public java.awt.event.MouseListener getMenuMouseListener() {
      return new MyMouseListener();
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
  
  } // end inner class PanelPopupMenu






  /**
   * Legend Panel
   */
  private class LegendPanel extends javax.swing.JPanel {
    public LegendPanel() {
      setLayout(new java.awt.GridLayout(0, 1));
      add(new javax.swing.JLabel());
      // populate panels
      for (int i = SHADES.length - 1; i >= 0; i--) {
        javax.swing.JLabel l = new javax.swing.JLabel("SpyEvent position: " + (-(i - ((SHADES.length - 1) / 2))), javax.swing.JLabel.LEFT);
        l.setBackground(SHADES[i]);
        l.setOpaque(true);
        l.setVerticalAlignment(l.CENTER);
        add(l);
      }
    }
  }





  /**
   * MyDropTargetListener
   * a listener that tracks the state of the operation
   * @see java.awt.dnd.DropTargetListener
   * @see java.awt.dnd.DropTarget
   */
  private class MyDropTargetListener extends UniqueIDDropTargetListener {
    
   /**
    * processes the drop and return false if the drop is rejected or true if the drop is accepted.
    * The method must not call the rejectDrop as returning false signel that the drop is rejected.
    * On the other hand it is the responsability of this method to call the acceptDrop and dropComplete
    * when accepting the drop and returning true
    */
    protected boolean processDrop(java.awt.dnd.DropTargetDropEvent event, UniqueID uniqueID) {
      ActiveObject activeObject = ic2dObject.findActiveObjectById(uniqueID);
      if (activeObject == null) return false;
      if (event.getDropAction() == java.awt.dnd.DnDConstants.ACTION_MOVE) {
        event.acceptDrop(java.awt.dnd.DnDConstants.ACTION_MOVE);
      } else if (event.getDropAction() == java.awt.dnd.DnDConstants.ACTION_COPY) {
        event.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
      }
      event.dropComplete(false);
      addActiveObject(activeObject);
      return true;
    }
 
 
   /**
    * Displays a user feed back to show that the drag is going on
    */
    protected void showDragFeedBack() {
    }
   
 
   /**
    * Displays a user feed back to show that the drop is going on
    */
    protected void showDropFeedBack() {
    }
   
 
   /**
    * Removes the user feed back that shows the drag
    */
    protected void hideDnDFeedBack() {
    }

  } // end inner class MyDropTargetListener







  private class EventListModel extends javax.swing.AbstractListModel {
  
    private BoundedCircularArrayList eventList;
  
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public EventListModel() {
      eventList = new BoundedCircularArrayList(80);
    }

    //
    // -- Public methods -----------------------------------------------
    //

    //
    // -- implements ListModel -----------------------------------------------
    //
    public boolean isEmpty() {
      return eventList.isEmpty();
    }
    
    public int getSize() {
      return eventList.size();
    }

    public Object lastElement() {
      return eventList.get(eventList.size()-1);
    }
    
    public Object getElementAt(int index) {
      return eventList.get(index);
    }
    
    public void addEvent(SpyEvent evt) {
      int n = eventList.size();
      eventList.add(evt);
      fireIntervalAdded(this, n, n);
    }
    
    public void clear() {
      int n = eventList.size();
      if (n>0) {
        eventList.clear();
        fireIntervalRemoved(this, 0, n-1);
      }
    }
  } // end inner class EventListModel





  public class BoundedCircularArrayList extends org.objectweb.proactive.core.util.CircularArrayList {

    private int fixedSize;
  
    public BoundedCircularArrayList(int size) {
      super(size);
      fixedSize = size;
    }

    public void ensureCapacity(int minCapacity) {
      // size is fixed
    }

    public boolean add(Object o) {
      modCount++;
      array[tail] = o;
      tail = (tail+1)%fixedSize;
      if (tail == head)
        head = (head+1)%fixedSize;
      else size++;
      return true;
    }


    public boolean addAll(java.util.Collection c) {
      modCount++;
      int numNew = Math.min(c.size(), fixedSize);
      java.util.Iterator e = c.iterator();
      for (int i=0; i < numNew; i++) {
        array[tail] = e.next();
        tail = (tail+1)%array.length;
        if (tail == head)
          head = (head+1)%fixedSize;
        else size++;
      }
      return numNew != 0;
    }
  } // end inner class BoundedCircularArrayList
}