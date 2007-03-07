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
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.data.IC2DObject;
import org.objectweb.proactive.ic2d.event.CommunicationEventListener;
import org.objectweb.proactive.ic2d.gui.data.UniqueIDDropTargetListener;
import org.objectweb.proactive.ic2d.gui.recording.PlayerFrameTimeLine;
import org.objectweb.proactive.ic2d.spy.SpyEvent;
import org.objectweb.proactive.ic2d.spy.SpyFutureEvent;
import org.objectweb.proactive.ic2d.spy.SpyMessageEvent;


public class EventListsPanel extends javax.swing.JPanel
    implements CommunicationEventListener, ActiveObjectWatcher {
    private static final String IMAGES_DIR = "org/objectweb/proactive/ic2d/gui/images/";
    private static final String REPLY_RECEIVED_ICON = IMAGES_DIR +
        "ReplyReceived.gif";
    private static final String REPLY_SENT_ICON = IMAGES_DIR + "ReplySent.gif";
    private static final String REQUEST_RECEIVED_ICON = IMAGES_DIR +
        "RequestReceived.gif";
    private static final String REQUEST_SENT_ICON = IMAGES_DIR +
        "RequestSent.gif";
    private static final String WAITING_FOR_REQUEST_ICON = IMAGES_DIR +
        "WaitingForRequest.gif";
    private static final String WAIT_BY_NECESSITY_ICON = IMAGES_DIR +
        "WaitByNecessity.gif";
    private static final String VOID_REQUEST_SERVED_ICON = IMAGES_DIR +
        "VoidRequestServed.gif";
    private static final int RELATED_EVENT_COLOR_MODE = 0;
    private static final int ABSOLUTE_EVENT_COLOR_MODE = 1;
    private static final int NO_COLOR_MODE = 2;
    private static final java.awt.Color LIST_BG_COLOR = new java.awt.Color(0,
            0, 80);
    private static final java.awt.Font MFONT = new java.awt.Font("Dialog", 1, 10);
    private static final java.awt.Color[] SHADES;

    static {
        // Create the SHADES!
        SHADES = new java.awt.Color[21];
        for (int i = 0; i < 10; i++) {
            SHADES[i] = new java.awt.Color((i * 13) + 30, (i * 13) + 30,
                    150 + (i * 10));
        }
        SHADES[10] = java.awt.Color.orange;
        for (int i = 11; i < 21; i++) {
            SHADES[i] = new java.awt.Color(150 + ((20 - i) * 10),
                    ((20 - i) * 13) + 30, ((20 - i) * 13) + 30);
        }
    }

    private javax.swing.ImageIcon requestReceivedIcon;
    private javax.swing.ImageIcon replySentIcon;
    private javax.swing.ImageIcon requestSentIcon;
    private javax.swing.ImageIcon replyReceivedIcon;
    private javax.swing.ImageIcon waitingForRequestIcon;
    private javax.swing.ImageIcon waitByNecessityIcon;
    private javax.swing.ImageIcon voidRequestServedIcon;

    /**
     * KEY: id
     * VAL: corresponding JList [model = default]
     */
    private java.util.HashMap<UniqueID, ObjTrackPanel> objectTrackPanelMap;
    private BoundedCircularArrayList events;

    /**
     * The array of strings representing the state of a requestEvent
     */
    private int colorMode = RELATED_EVENT_COLOR_MODE;
    public javax.swing.JPopupMenu popup;
    private javax.swing.JPanel centerPanel;
    private PlayerFrameTimeLine recorder;

    /** last object in one of the lists that was selected */
    private Object lastSelected;
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
        dropTarget = new java.awt.dnd.DropTarget(this,
                java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE,
                new MyDropTargetListener(), true);

        objectTrackPanelMap = new java.util.HashMap<UniqueID, ObjTrackPanel>();
        events = new BoundedCircularArrayList(1000);
        recorder = new PlayerFrameTimeLine(this, ic2dObject.getController());

        setLayout(new java.awt.BorderLayout());
        {
            javax.swing.JPanel tools = new javax.swing.JPanel();
            final javax.swing.JComboBox colorCombo = new javax.swing.JComboBox(new String[] {
                        "Color related events", "Color chronological order",
                        "No coloring"
                    });
            colorCombo.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        switch (colorCombo.getSelectedIndex()) {
                        case 0:
                            colorMode = RELATED_EVENT_COLOR_MODE;
                            break;
                        case 1:
                            colorMode = ABSOLUTE_EVENT_COLOR_MODE;
                            break;
                        case 2:
                            colorMode = NO_COLOR_MODE;
                            break;
                        }
                        colorEvents();
                    }
                });
            tools.add(colorCombo);
            {
                javax.swing.JButton b = new javax.swing.JButton(
                        "Messages recorder");
                b.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                            java.awt.event.ActionEvent e) {
                            recorder.setVisible(!recorder.isVisible());
                        }
                    });
                tools.add(b);
            }
            {
                javax.swing.JButton b = new javax.swing.JButton(
                        "Clear all events");
                b.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                            java.awt.event.ActionEvent e) {
                            clearAll();
                        }
                    });
                tools.add(b);
            }
            {
                javax.swing.JButton b = new javax.swing.JButton("Legend");
                b.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                            java.awt.event.ActionEvent e) {
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
            java.util.Iterator<ObjTrackPanel> i = objectTrackPanelMap.values()
                                                                     .iterator();
            while (i.hasNext()) {
                // clean the ui
                ObjTrackPanel otp = i.next();
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
        if (objectTrackPanelMap.containsKey(activeObject.getID())) {
            return;
        }
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
            o = objectTrackPanelMap.remove(activeObject.getID());
        }
        if (o == null) {
            return;
        }
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

    public void voidRequestServed(ActiveObject object, SpyEvent spyEvent) {
        recordEvent(object, spyEvent, false);
    }

    public void allEventsProcessed() {
        //Code for downScrolling should be there...
        synchronized (objectTrackPanelMap) {
            java.util.Iterator<ObjTrackPanel> i = objectTrackPanelMap.values()
                                                                     .iterator();
            while (i.hasNext()) {
                // clean the ui
                ObjTrackPanel otp = i.next();
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
        replyReceivedIcon = new javax.swing.ImageIcon(c.getResource(
                    REPLY_RECEIVED_ICON));
        if (replyReceivedIcon == null) {
            controller.log("Can't load image " + REPLY_RECEIVED_ICON);
        }
        replySentIcon = new javax.swing.ImageIcon(c.getResource(REPLY_SENT_ICON));
        if (replySentIcon == null) {
            controller.log("Can't load image " + REPLY_SENT_ICON);
        }
        requestReceivedIcon = new javax.swing.ImageIcon(c.getResource(
                    REQUEST_RECEIVED_ICON));
        if (requestReceivedIcon == null) {
            controller.log("Can't load image " + REQUEST_RECEIVED_ICON);
        }
        requestSentIcon = new javax.swing.ImageIcon(c.getResource(
                    REQUEST_SENT_ICON));
        if (requestSentIcon == null) {
            controller.log("Can't load image " + REQUEST_SENT_ICON);
        }
        waitingForRequestIcon = new javax.swing.ImageIcon(c.getResource(
                    WAITING_FOR_REQUEST_ICON));
        if (waitingForRequestIcon == null) {
            controller.log("Can't load image " + WAITING_FOR_REQUEST_ICON);
        }
        waitByNecessityIcon = new javax.swing.ImageIcon(c.getResource(
                    WAIT_BY_NECESSITY_ICON));
        if (waitingForRequestIcon == null) {
            controller.log("Can't load image " + WAITING_FOR_REQUEST_ICON);
        }
        voidRequestServedIcon = new javax.swing.ImageIcon(c.getResource(
                    VOID_REQUEST_SERVED_ICON));
        if (voidRequestServedIcon == null) {
            controller.log("Can't load image " + VOID_REQUEST_SERVED_ICON);
        }
    }

    private ObjTrackPanel getObjTrackPanel(UniqueID id) {
        synchronized (objectTrackPanelMap) {
            return objectTrackPanelMap.get(id);
        }
    }

    /**
     * Adds the event to the lists and to the event vec
     */
    private void recordEvent(final ActiveObject obj, final SpyEvent evt,
        final boolean dontRepeatSameType) {
        //System.out.println("Record event evt="+evt);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ObjTrackPanel o = getObjTrackPanel(obj.getID());
                    if (o == null) {
                        return;
                    }
                    if (dontRepeatSameType && !o.getListModel().isEmpty()) {
                        SpyEvent lastElt = (SpyEvent) o.getListModel()
                                                       .lastElement();
                        if (lastElt.getType() == evt.getType()) {
                            return;
                        }
                    }
                    o.getListModel().addEvent(evt);
                    events.add(evt);
                    recorder.recordEvent(obj, evt);
                }
            });
    } // end recordEvent

    private void showLegend() {
        javax.swing.JFrame legendFrame = new javax.swing.JFrame(
                "Eventslist Legend");
        legendFrame.getContentPane().add(new LegendPanel());
        legendFrame.setSize(400, 600);
        legendFrame.setVisible(true);
    }

    /** Colors the events correctly. <br>Possible modes:<br><ul>
     * <li>NO_COLOR_MODE: No coloring</li>
     * <li>RELATED_EVENT_COLOR_MODE: The four Qsent/Qrecv, Rsent/Rrecv (or Vserved) messages
     * belonging to the one the user clicked on are colored. Requests in blue, replies in red.
     * All others remain white.</li>
     * <li>ABSOLUTE_EVENT_COLOR_MODE: The message the user clicked on and its corresponding
     * four messages are colored yellow. If the user clicked on a request, older request
     * messages in the same list with their corresponding messages are colored blue and newer ones red.
     * If the user clicked on a reply, older reply
     * messages in the same list with their corresponding messages are colored blue and newer ones red.</li>
     */
    private void colorEvents() {
        for (int i = 0; i < events.size(); i++) {
            SpyEvent tmp = (SpyEvent) events.get(i);
            tmp.setPos(-1);
        }
        if ((lastSelected != null) && lastSelected instanceof SpyMessageEvent) {
            SpyMessageEvent m = (SpyMessageEvent) lastSelected;
            switch (colorMode) {
            case ABSOLUTE_EVENT_COLOR_MODE:
                UniqueID id = m.getBodyID();
                if (id == null) {
                    return;
                }
                java.util.LinkedList<Object> bef = getMsgBefore(m, id);
                java.util.LinkedList<Object> aft = getMsgAfter(m, id);
                computeColor(m, 10);
                for (int i = 0; i < bef.size(); i++)
                    computeColor((SpyEvent) bef.get(i), 9 - i);
                for (int i = 0; i < aft.size(); i++) {
                    computeColor((SpyEvent) aft.get(i), 11 + i);
                }
                break;
            case RELATED_EVENT_COLOR_MODE:
                if (m.getSequenceNumber() == 0) {
                    m.setPos(11);
                } else {
                    for (int i = 0; i < events.size(); i++) {
                        SpyEvent tmp = (SpyEvent) events.get(i);
                        if (tmp instanceof SpyMessageEvent) {
                            SpyMessageEvent myEvent = (SpyMessageEvent) tmp;
                            if (m.matches(myEvent)) {
                                tmp.setPos(getPosFromType(myEvent.getType()));
                            }
                        }
                    }
                }
                break;
            }
        }
        repaint();
    }

    /** calculates the corresponding SHADES index for related events */
    private int getPosFromType(int type) {
        switch (type) {
        case SpyEvent.REQUEST_SENT_MESSAGE_TYPE:
            return 11;
        case SpyEvent.REQUEST_RECEIVED_MESSAGE_TYPE:
            return 11;
        case SpyEvent.REPLY_SENT_MESSAGE_TYPE:
        case SpyEvent.VOID_REQUEST_SERVED_TYPE:
            return 9;
        case SpyEvent.REPLY_RECEIVED_MESSAGE_TYPE:
            return 9;
        }
        return -1;
    }

    /**
     * Compute the color given the position
     * and set the one of the message and its peers for absolute events
     */
    private void computeColor(SpyEvent m, int pos) {
        m.setPos(pos);
        if (!(m instanceof SpyMessageEvent)) {
            return;
        }
        SpyMessageEvent myEvent = (SpyMessageEvent) m;
        if (myEvent.getSequenceNumber() == 0) {
            return;
        }
        for (int i = 0; i < events.size(); i++) {
            Object o = events.get(i);
            if (o instanceof SpyMessageEvent) {
                SpyMessageEvent myEvent2 = (SpyMessageEvent) o;
                if (myEvent.matches(myEvent2)) {
                    myEvent2.setPos(pos);
                }
            }
        }
    }

    /** returns all SpyMessageEvents that belong to the same body,
     * the same type (request or reply) and occured before target */
    private java.util.LinkedList<Object> getMsgBefore(SpyMessageEvent target,
        UniqueID id) {
        java.util.LinkedList<Object> result = new java.util.LinkedList<Object>();
        int index = events.indexOf(target);
        if (index == -1) {
            return result;
        }
        boolean isRequest;
        if (target.isRequestMessage()) {
            isRequest = true;
        } else if (target.isReplyMessage()) {
            isRequest = false;
        } else {
            return result;
        }

        for (int i = index - 1; i >= 0; i--) {
            Object o = events.get(i);
            if (o instanceof SpyMessageEvent) {
                SpyMessageEvent myEvent = (SpyMessageEvent) o;
                if ((isRequest && myEvent.isRequestMessage()) ||
                        (!isRequest && myEvent.isReplyMessage())) {
                    if (myEvent.wasSent()) {
                        if (id.equals(myEvent.getSourceBodyID())) {
                            result.add(o);
                        }
                    } else {
                        if (id.equals(myEvent.getDestinationBodyID())) {
                            result.add(o);
                        }
                    }
                }
            }
        }

        //System.out.println(result.size()+" size");
        return result;
    }

    /** returns all SpyMessageEvents that belong to the same body,
     * the same type (request or reply) and occured after target */
    private java.util.LinkedList<Object> getMsgAfter(SpyMessageEvent target,
        UniqueID id) {
        java.util.LinkedList<Object> result = new java.util.LinkedList<Object>();
        int index = events.indexOf(target);
        if (index == -1) {
            return result;
        }
        boolean isRequest;
        if (target.isRequestMessage()) {
            isRequest = true;
        } else if (target.isReplyMessage()) {
            isRequest = false;
        } else {
            return result;
        }

        for (int i = index + 1; i < events.size(); i++) {
            Object o = events.get(i);
            if (o instanceof SpyMessageEvent) {
                SpyMessageEvent myEvent = (SpyMessageEvent) o;
                if ((isRequest && myEvent.isRequestMessage()) ||
                        (!isRequest && myEvent.isReplyMessage())) {
                    if (myEvent.wasSent()) {
                        if (id.equals(myEvent.getSourceBodyID())) {
                            result.add(o);
                        }
                    } else {
                        if (id.equals(myEvent.getDestinationBodyID())) {
                            result.add(o);
                        }
                    }
                }
            }
        }

        //	System.out.println(result.size()+" size");
        return result;
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //  
    private class MyListSelectionListener implements javax.swing.event.ListSelectionListener {

        /**
         * When an item is selected,
         * this method is fired and changes the colors of the other items
         */
        public void valueChanged(javax.swing.event.ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            lastSelected = ((javax.swing.JList) e.getSource()).getSelectedValue();
            colorEvents();
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
        public java.awt.Component getListCellRendererComponent(
            javax.swing.JList list, Object val, int index, boolean isSelected,
            boolean cellHasFocus) {
            if (!(val instanceof SpyEvent)) {
                return this;
            }
            SpyEvent event = (SpyEvent) val;
            switch (event.getType()) {
            case SpyEvent.REQUEST_SENT_MESSAGE_TYPE: {
                SpyMessageEvent rq = (SpyMessageEvent) val;
                UniqueID id = rq.getDestinationBodyID();
                setToolTipText("Request sent to " + id);
                if (requestSentIcon != null) {
                    setIcon(requestSentIcon);
                }
                formatItem(rq, id);
            }
            break;
            case SpyEvent.REQUEST_RECEIVED_MESSAGE_TYPE: {
                SpyMessageEvent rq = (SpyMessageEvent) val;
                UniqueID id = rq.getSourceBodyID();
                setToolTipText("Request received from " + id);
                if (requestReceivedIcon != null) {
                    setIcon(requestReceivedIcon);
                }
                formatItem(rq, id);
            }
            break;
            case SpyEvent.REPLY_SENT_MESSAGE_TYPE: {
                SpyMessageEvent rp = (SpyMessageEvent) val;
                UniqueID id = rp.getDestinationBodyID();
                setToolTipText("Reply sent to " + id);
                if (replySentIcon != null) {
                    setIcon(replySentIcon);
                }
                formatItem(rp, id);
            }
            break;
            case SpyEvent.REPLY_RECEIVED_MESSAGE_TYPE: {
                SpyMessageEvent rp = (SpyMessageEvent) val;
                UniqueID id = rp.getSourceBodyID();
                setToolTipText("Reply received from " + id);
                if (replyReceivedIcon != null) {
                    setIcon(replyReceivedIcon);
                }
                formatItem(rp, id);
            }
            break;
            case SpyEvent.VOID_REQUEST_SERVED_TYPE: {
                SpyMessageEvent rp = (SpyMessageEvent) val;
                UniqueID id = rp.getSourceBodyID();
                setToolTipText("Void request from " + id + " served");
                if (voidRequestServedIcon != null) {
                    setIcon(voidRequestServedIcon);
                }
                formatItem(rp, id);
            }
            break;
            case SpyEvent.OBJECT_WAIT_BY_NECESSITY_TYPE:
                SpyFutureEvent fe = (SpyFutureEvent) val;
                setToolTipText("Waiting for a future created by " +
                    fe.getCreatorID());
                setText("Waiting by necessity");
                setBackground(java.awt.Color.white);
                if (waitByNecessityIcon != null) {
                    setIcon(waitByNecessityIcon);
                }
                break;
            case SpyEvent.OBJECT_WAIT_FOR_REQUEST_TYPE:
                setToolTipText("");
                setText("Waiting for request");
                setBackground(java.awt.Color.white);
                if (waitingForRequestIcon != null) {
                    setIcon(waitingForRequestIcon);
                }
                break;
            }
            return this;
        }

        private void formatItem(SpyMessageEvent msg, UniqueID peerID) {
            // The sender or receiver is..
            ActiveObject peer = null;
            if (peerID != null) {
                peer = ic2dObject.findActiveObjectById(peerID);
            }
            if (peer != null) {
                setText("[" + peer.getName() + "]" + msg.getMethodName());
            } else {
                setText("[?]" + msg.getMethodName());
            }

            // Background
            if ((msg.getPos() <= -1) || (msg.getPos() >= SHADES.length)) {
                setBackground(java.awt.Color.white);
            } else {
                setBackground(SHADES[msg.getPos()]);
            }
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
            setSize(150, 200);
            // Title
            javax.swing.JLabel lTitle = new javax.swing.JLabel(activeObject.getName(),
                    javax.swing.JLabel.CENTER);
            lTitle.setForeground(java.awt.Color.white);
            lTitle.setBackground(LIST_BG_COLOR);
            lTitle.setOpaque(true);

            setLayout(new java.awt.BorderLayout());
            add(lTitle, java.awt.BorderLayout.NORTH);

            listModel = new EventListModel();
            list = new javax.swing.JList(listModel);
            list.addListSelectionListener(new MyListSelectionListener());
            javax.swing.JScrollPane scList = new javax.swing.JScrollPane(list,
                    javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            verticalScrollBar = scList.getVerticalScrollBar();
            list.setCellRenderer(new TimeCellRenderer());
            list.setBackground(java.awt.Color.white);
            add(scList, java.awt.BorderLayout.CENTER);
            setBorder(javax.swing.BorderFactory.createEtchedBorder(
                    java.awt.Color.white, java.awt.Color.gray));

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
            add(new javax.swing.AbstractAction(
                    "Remove this ActiveObject from the EventList", null) {
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
            setLayout(new java.awt.GridLayout(0, 1, 0, 0));
            add(new javax.swing.JLabel("Object waiting for request",
                    waitingForRequestIcon, javax.swing.JLabel.LEFT));
            add(new javax.swing.JLabel(
                    "Object waiting for result (wait by necessity)",
                    waitByNecessityIcon, javax.swing.JLabel.LEFT));
            add(new javax.swing.JLabel("Object sent a request",
                    requestSentIcon, javax.swing.JLabel.LEFT));
            add(new javax.swing.JLabel("Object received a request",
                    requestReceivedIcon, javax.swing.JLabel.LEFT));
            add(new javax.swing.JLabel(
                    "Object finished serving a request (void return type)",
                    voidRequestServedIcon, javax.swing.JLabel.LEFT));
            add(new javax.swing.JLabel(
                    "Object finished serving a request and sent a reply",
                    replySentIcon, javax.swing.JLabel.LEFT));
            add(new javax.swing.JLabel("Object received a reply",
                    replyReceivedIcon, javax.swing.JLabel.LEFT));
            add(new javax.swing.JLabel());
            // populate panels
            for (int i = SHADES.length - 1; i >= 0; i--) {
                javax.swing.JLabel l = new javax.swing.JLabel((i == 10)
                        ? "Event you clicked on"
                        : ((i == (SHADES.length - 5)) ? "Older events"
                                                      : ((i == 5)
                        ? "Newer events" : "")), javax.swing.JLabel.CENTER);
                l.setBackground(SHADES[i]);
                l.setOpaque(true);
                l.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
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
        protected boolean processDrop(java.awt.dnd.DropTargetDropEvent event,
            UniqueID uniqueID) {
            ActiveObject activeObject = ic2dObject.findActiveObjectById(uniqueID);
            if (activeObject == null) {
                return false;
            }
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
            return eventList.get(eventList.size() - 1);
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
            if (n > 0) {
                eventList.clear();
                fireIntervalRemoved(this, 0, n - 1);
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
            tail = (tail + 1) % fixedSize;
            if (tail == head) {
                head = (head + 1) % fixedSize;
            } else {
                size++;
            }
            return true;
        }

        public boolean addAll(java.util.Collection c) {
            modCount++;
            int numNew = Math.min(c.size(), fixedSize);
            java.util.Iterator e = c.iterator();
            for (int i = 0; i < numNew; i++) {
                array[tail] = e.next();
                tail = (tail + 1) % array.length;
                if (tail == head) {
                    head = (head + 1) % fixedSize;
                } else {
                    size++;
                }
            }
            return numNew != 0;
        }
    } // end inner class BoundedCircularArrayList
}
