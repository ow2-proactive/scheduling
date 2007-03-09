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
package org.objectweb.proactive.ic2d.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.objectweb.fractal.gui.menu.control.SimpleFileFilter;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.Launcher;


public class LauncherWindow extends JFrame {
    //status
    public final String STATUS_LAUNCHED = "launched";
    public final String STATUS_TERMINATED = "terminated";
    public final String STATUS_KILLED = "killed";
    private Map descriptorMap;
    private Vector datas;
    private Vector columnsName;
    private Vector descriptorsToLaunch = new Vector();
    private int id;
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JLabel jLabel1 = new JLabel();
    JCheckBox graphicalCheckBox = new JCheckBox();
    JCheckBox jobCheckBox = new JCheckBox();
    Border border1 = BorderFactory.createLineBorder(Color.white, 2);
    Border border2 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
            Color.white, new Color(148, 145, 140));
    JPanel jPanel4 = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    JButton jButton1 = new JButton();
    JList descriptorList = new JList();
    BorderLayout borderLayout4 = new BorderLayout();
    JLabel jLabel2 = new JLabel();
    JPanel jPanel5 = new JPanel();
    MyJTable launchedTable;
    JSplitPane jSplitPane2 = new JSplitPane();
    JPanel jPanel6 = new JPanel();
    BorderLayout borderLayout5 = new BorderLayout();
    JPanel jPanel61 = new JPanel();
    JPanel jPanel7 = new JPanel();
    BorderLayout borderLayout6 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane2 = new JScrollPane();
    JComboBox jComboBox1 = new JComboBox();
    JTextArea jTextDescriptor = new JTextArea();
    private boolean doGraphicalMonitoring;
    private boolean doJobMonitoring;

    public LauncherWindow() {
        // for monitoring
        doGraphicalMonitoring = true;
        doJobMonitoring = true;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        pack();
    }

    private void jbInit() throws Exception {
        descriptorMap = new HashMap();
        columnsName = new Vector();
        jButton1.addActionListener(new LauncherFrame_jButton1_actionAdapter(
                this));
        descriptorList.setFixedCellHeight(16);

        columnsName.add("Id");
        columnsName.add("Name");
        columnsName.add("Status");

        datas = new Vector();
        jobCheckBox.addActionListener(new LauncherWindow_jmCheckBox_actionAdapter(
                this));
        jobCheckBox.setSelected(doJobMonitoring);
        graphicalCheckBox.addActionListener(new LauncherWindow_gmCheckBox_actionAdapter(
                this));
        graphicalCheckBox.setSelected(doGraphicalMonitoring);

        launchedTable = new MyJTable(datas, columnsName);
        launchedTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jSplitPane2.setPreferredSize(new Dimension(650, 500));
        jPanel7.setLayout(borderLayout6);
        this.getContentPane().setLayout(borderLayout1);
        jPanel1.setLayout(borderLayout2);
        jPanel3.setBorder(border2);
        jPanel3.setMinimumSize(new Dimension(10, 60));
        jPanel3.setPreferredSize(new Dimension(10, 40));
        jLabel1.setText("Control:");
        graphicalCheckBox.setText("Graphical Monitoring");
        jobCheckBox.setText("Job Textual Monitoring");
        jPanel2.setLayout(borderLayout3);
        jButton1.setText("Browse and Load XML Descriptor");
        jPanel2.setMinimumSize(new Dimension(199, 150));
        jPanel2.setPreferredSize(new Dimension(300, 400));
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setTitle("Launcher Window");
        jPanel1.setMinimumSize(new Dimension(199, 120));
        jPanel4.setLayout(borderLayout4);
        jLabel2.setText("Activated Applications");
        jPanel4.setPreferredSize(new Dimension(450, 200));
        launchedTable.setPreferredSize(new Dimension(225, 100));
        launchedTable.addMouseListener(new GUI_launchedTable_mouseAdapter(this));
        jPanel5.setPreferredSize(new Dimension(118, 30));
        descriptorList.setPreferredSize(new Dimension(300, 0));
        descriptorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        descriptorList.addMouseListener(new GUI_descriptorlistpopup_mouseAdapter(
                this));

        jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jPanel6.setLayout(borderLayout5);
        jPanel3.add(jLabel1);
        jPanel3.add(graphicalCheckBox);
        jPanel3.add(jobCheckBox);
        jPanel1.add(jPanel6, java.awt.BorderLayout.NORTH);
        jPanel61.add(jButton1, java.awt.BorderLayout.CENTER);
        jPanel6.add(jPanel61, java.awt.BorderLayout.CENTER);
        jSplitPane2.add(jPanel4, JSplitPane.BOTTOM);
        jPanel5.add(jLabel2);
        jSplitPane2.add(jPanel1, JSplitPane.TOP);
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane2.setOneTouchExpandable(true);
        jSplitPane2.setContinuousLayout(true);

        this.getContentPane().add(jSplitPane2, java.awt.BorderLayout.CENTER);
        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);
        jPanel4.add(jPanel7, java.awt.BorderLayout.CENTER);
        jPanel4.add(jPanel5, java.awt.BorderLayout.NORTH);
        jScrollPane2.getViewport().add(descriptorList);
        jPanel7.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);
        this.getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

        jScrollPane2.setSize(new Dimension(4, 1400));
        launchedTable = new MyJTable(datas, columnsName);
        launchedTable.repaint();
        jScrollPane2.repaint();
        jScrollPane2.revalidate();
        launchedTable.revalidate();
        launchedTable.setCellEditor(null);
        launchedTable.setRowSelectionAllowed(true);
        jScrollPane1.setViewportView(launchedTable);
    }

    public static void main(String[] args) {
        LauncherWindow lf = new LauncherWindow();
        lf.setVisible(true);
    }

    public void jButton1_actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser(".");

        fileChooser.addChoosableFileFilter(new SimpleFileFilter("xml",
                "XML Descriptor files"));
        if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File f = fileChooser.getSelectedFile();

        String path = f.getPath();

        if (path.equals("")) {
            return;
        }
        try {
            Launcher launcher = new Launcher(path);
            String id = launcher.getProActiveDescriptor().getMainDefinitions()[0].getVirtualNodes()[0].getJobID();
            ListCell cell = new ListCell("" + id, path);
            descriptorsToLaunch.add(cell);
            descriptorList.setListData(descriptorsToLaunch);
            Dimension dim = descriptorList.getSize();
            dim.setSize(dim.getWidth(), descriptorsToLaunch.size() * 16);
            descriptorList.setPreferredSize(dim);
            descriptorMap.put(cell, launcher);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * popup actions when clicking on the descriptor list
     * @param e
     * @throws IOException
     */
    public void doPopupCall(ActionEvent e) {
        String command = e.getActionCommand();
        Launcher launcher = (Launcher) (descriptorMap.get(descriptorList.getSelectedValue()));
        if (command.equals("Launch the application")) {
            if (!launcher.isActivated()) {
                try {
                    // activate the launcher
                    launcher.activate();
                    // and update the table
                    String path = ((ListCell) descriptorList.getSelectedValue()).getContent();

                    Vector test = new Vector();
                    test.add("" +
                        ((ListCell) descriptorList.getSelectedValue()).getId());
                    test.add(getApplicationName(path));
                    //test.add(getApplicationName (((ListCell) descriptorList.getSelectedValue()).getContent()));
                    test.add("launched");
                    datas.add(test);
                    ListCell cell = (ListCell) descriptorList.getSelectedValue();
                    cell.setContent(cell.getContent() + " - activated");
                    descriptorList.repaint();

                    launchedTable = new MyJTable(datas, columnsName);
                    Dimension dim2 = launchedTable.getSize();
                    dim2.setSize(dim2.getWidth(), (datas.size() * 16));

                    launchedTable.setSize(dim2);
                    launchedTable.setPreferredSize(dim2);
                    launchedTable.revalidate();
                    launchedTable.repaint();
                    launchedTable.doLayout();
                    jScrollPane1.setViewportView(launchedTable);
                    launchedTable.addMouseListener(new GUI_launchedTable_mouseAdapter(
                            this));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if (command.equals("Remove the descriptor")) {
            ListCell cell = ((ListCell) descriptorList.getSelectedValue());
            descriptorMap.remove(cell);
            descriptorsToLaunch.remove(cell);
            descriptorList.setListData(descriptorsToLaunch);
        } else if (command.equals("View")) {
            try {
                //gets the path and the name of the xml descripor
                String descripContent = new String();

                //gets the path and the name of the xml descripor
                String buf;
                String path = ((ListCell) descriptorList.getSelectedValue()).getContent();
                if (path.endsWith("- activated")) {
                    path = path.substring(0, path.indexOf(" - activated"));
                }
                String[] tab = path.split(System.getProperty("file.separator"));
                String name = tab[tab.length - 1];

                //reads the xml descriptor
                BufferedReader br = new BufferedReader(new FileReader(
                            new File(path)));
                while ((buf = br.readLine()) != null) {
                    descripContent += (buf + "\n");
                }

                jTextDescriptor.setText(descripContent);
                jTextDescriptor.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(jTextDescriptor);
                JFrame frame = new JFrame(name);
                frame.getContentPane().add(scrollPane);
                frame.setBounds(200, 100, 900, 800);
                frame.setVisible(true);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } else if (command.equals("Kill")) {
            try {
                if (launcher.isActivated()) {
                    launcher.getProActiveDescriptor().killall(true);
                    launchedTable.setValueAt(STATUS_KILLED,
                        launchedTable.getSelectedRow(), 2);
                    jScrollPane1.setViewportView(launchedTable);
                    launchedTable.addMouseListener(new GUI_launchedTable_mouseAdapter(
                            this));
                }
            } catch (ProActiveException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Popup menu on the descriptor list
     * @param e
     */
    public void descriptorList_popup(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            // right button may select a line in the list
            int index = descriptorList.locationToIndex(e.getPoint());
            Rectangle rectangle = descriptorList.getCellBounds(index, index);
            if ((rectangle != null) &&
                    descriptorList.getCellBounds(index, index)
                                      .contains(e.getPoint())) {
                descriptorList.setSelectedIndex(index);
            } else {
                descriptorList.clearSelection();
            }

            // if a line is selected, process the actions
            if (!descriptorList.isSelectionEmpty()) {
                Launcher launcher = (Launcher) (descriptorMap.get(descriptorList.getSelectedValue()));

                //creation of a popup menu
                JMenuItem menuItem;
                JPopupMenu popup = new JPopupMenu();
                menuItem = new JMenuItem("Launch the application");
                menuItem.addActionListener(new GUI_PopupMenuItem_ActionListener(
                        this));
                popup.add(menuItem);
                menuItem.setEnabled(!launcher.isActivated());

                menuItem = new JMenuItem("Remove the descriptor");
                menuItem.addActionListener(new GUI_PopupMenuItem_ActionListener(
                        this));
                popup.add(menuItem);

                menuItem = new JMenuItem("View");
                menuItem.addActionListener(new GUI_PopupMenuItem_ActionListener(
                        this));
                popup.add(menuItem);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * popup menu on the table of launched applications
     * @param e
     */
    public void launchedTable_popup(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            // right button may select a line in the list
            int index = launchedTable.rowAtPoint(e.getPoint());

            //System.out.println(index);
            if (index >= 0) {
                launchedTable.setRowSelectionInterval(index, index);
            }
        }

        if ((e.getButton() == MouseEvent.BUTTON3) &&
                (launchedTable.rowAtPoint(e.getPoint()) == launchedTable.getSelectedRow()) &&
                (launchedTable.getSelectedRow() >= 0)) {
            Launcher launcher = (Launcher) (descriptorMap.get(descriptorList.getSelectedValue()));

            //creation of a popup menu
            JMenuItem menuItem;
            JPopupMenu popup = new JPopupMenu();
            menuItem = new JMenuItem("Kill");
            menuItem.addActionListener(new GUI_PopupMenuItem_ActionListener(
                    this));
            popup.add(menuItem);
            menuItem.setEnabled(launcher.isActivated());

            /*
               menuItem = new JMenuItem("Restart the application");
               menuItem.addActionListener(new GUI_PopupMenuItem_ActionListener(
                       this));
               popup.add(menuItem);
             */
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void jmCheckBox_actionPerformed(ActionEvent e) {
        doJobMonitoring = jobCheckBox.isSelected();
    }

    public void gmCheckBox_actionPerformed(ActionEvent e) {
        doGraphicalMonitoring = graphicalCheckBox.isSelected();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int largeurEcran = screenSize.width;
            int hauteurEcran = screenSize.height;

            int largeurFrame = getSize().width;
            int hauteurFrame = getSize().height;
            int posX = (largeurEcran - largeurFrame) / 2;
            int posY = (hauteurEcran - hauteurFrame) / 2;

            setLocation(posX, posY);
        }
        super.setVisible(b);
    }

    public boolean isGraphicalMonitoring() {
        return doGraphicalMonitoring;
    }

    public boolean isJobMonitoring() {
        return doJobMonitoring;
    }

    public void setIsJobMonitoring(boolean isJobMonitoring) {
        doJobMonitoring = isJobMonitoring;
    }

    public void setIsGraphicalMonitoring(boolean isGraphicalMonitoring) {
        doGraphicalMonitoring = isGraphicalMonitoring;
    }

    // PRIVATE METHODS

    /**
     * return the application name from the complete descriptor path
     * @param descriptorPath
     * @return
     */
    private String getApplicationName(String descriptorPath) {
        String name = "";
        if (descriptorPath.lastIndexOf("/") >= 0) {
            name = descriptorPath.substring(descriptorPath.lastIndexOf("/") +
                    1);
        } else {
            name = descriptorPath.substring(descriptorPath.lastIndexOf("\\") +
                    1);
        }
        if (name.endsWith(".xml")) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        return name;
    }
}


// Extern classes
class GUI_PopupMenuItem_ActionListener implements ActionListener {
    private LauncherWindow adaptee;

    public GUI_PopupMenuItem_ActionListener(LauncherWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.doPopupCall(e);
    }
}


class GUI_descriptorlistpopup_mouseAdapter extends MouseAdapter {
    private LauncherWindow adaptee;

    GUI_descriptorlistpopup_mouseAdapter(LauncherWindow adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        adaptee.descriptorList_popup(e);
    }
}


class GUI_launchedTable_mouseAdapter extends MouseAdapter {
    private LauncherWindow adaptee;

    GUI_launchedTable_mouseAdapter(LauncherWindow adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        adaptee.launchedTable_popup(e);
    }
}


class LauncherFrame_jButton1_actionAdapter implements ActionListener {
    private LauncherWindow adaptee;

    LauncherFrame_jButton1_actionAdapter(LauncherWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton1_actionPerformed(e);
    }
}


class LauncherWindow_gmCheckBox_actionAdapter implements ActionListener {
    private LauncherWindow adaptee;

    LauncherWindow_gmCheckBox_actionAdapter(LauncherWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.gmCheckBox_actionPerformed(e);
    }
}


class LauncherWindow_jmCheckBox_actionAdapter implements ActionListener {
    private LauncherWindow adaptee;

    LauncherWindow_jmCheckBox_actionAdapter(LauncherWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jmCheckBox_actionPerformed(e);
    }
}


/**
 *
 * disable the cell edit
 *
 */
class MyJTable extends JTable {
    public MyJTable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}


class ListCell {
    private String content;
    private String id;

    public ListCell(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }
}
