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
package org.objectweb.proactive.ic2d.gui.dialog;

import org.objectweb.proactive.ic2d.util.ActiveObjectFilter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FilterDialog extends JDialog implements ListSelectionListener {

  private JList list;
  private DefaultListModel listModel;
  private static final String addString = "Hide class";
  private static final String removeString = "Show class";
  private JButton fireButton;
  private JTextField tfMask;
  public boolean success = false;
  private JButton btOK;
  private JButton btCancel;

  public FilterDialog(Frame parent, ActiveObjectFilter filter) {
    super(parent, true);
	
    // Dialog
    setName("Hide/Show classes Dialog");
    setTitle("Hide/Show Classes");
    setSize(new Dimension(500, 200));

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent w) {
        closeDialog(false);
      }
    });

    listModel = new DefaultListModel();

    java.util.Iterator iterator = filter.iterator();
    while (iterator.hasNext()) {
      listModel.addElement((String)iterator.next());
    }

    //Create the list and put it in a scroll pane
    list = new JList(listModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setSelectedIndex(0);
    list.addListSelectionListener(this);
    JScrollPane listScrollPane = new JScrollPane(list);

    JButton addButton = new JButton(addString);
    addButton.setActionCommand(addString);
    addButton.addActionListener(new AddActionListener());

    fireButton = new JButton(removeString);
    fireButton.setActionCommand(removeString);
    fireButton.addActionListener(new RemoveActionListener());

    tfMask = new JTextField(10);
    tfMask.addActionListener(new AddActionListener());

    //Create a panel that uses FlowLayout (the default).
    JPanel ctrl = new JPanel(new GridLayout(2, 2));
    JPanel buttonPane = new JPanel();
    buttonPane.add(tfMask);
    buttonPane.add(addButton);
    buttonPane.add(fireButton);

    // OK/CANCEL
    btCancel = new JButton("Cancel");
    btCancel.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        closeDialog(false);
      }
    });

    btOK = new JButton("OK");
    btOK.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        closeDialog(true);
      }
    });

    JPanel DiagPane = new JPanel();
    DiagPane.add(btOK);
    DiagPane.add(btCancel);

    ctrl.add(buttonPane);
    ctrl.add(DiagPane);
    Container contentPane = getContentPane();
    contentPane.add(listScrollPane, BorderLayout.CENTER);
    contentPane.add(ctrl, BorderLayout.SOUTH);
  }


  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting() == false) {
      if (list.getSelectedIndex() == -1) {
        //No selection, disable fire button.
        fireButton.setEnabled(false);
        tfMask.setText("");
      } else {
        //Selection, update text field.
        fireButton.setEnabled(true);
        String name = list.getSelectedValue().toString();
        tfMask.setText(name);
      }
    }
  }


  public void closeDialog(boolean success) {
    this.success = success;
    setVisible(false);
    dispose();
  }


  public boolean  updateFilter(ActiveObjectFilter filter) {
    boolean updated = false;
    for (int i = 0; i < listModel.size(); i++) {
      boolean b = filter.filterClass((String)listModel.get(i));
      if (b) updated = true;
    }
    return updated;
  }
  
  
  
  private class AddActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      //User didn't type in a name...
      if (tfMask.getText().equals("")) {
        Toolkit.getDefaultToolkit().beep();
        return;
      }
      listModel.addElement(tfMask.getText());
    }
  }


  private class RemoveActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      int index = list.getSelectedIndex();
      listModel.remove(index);
      int size = listModel.getSize();
      if (size == 0) {
        //Nobody's left, disable firing.
        fireButton.setEnabled(false);
      } else {
        //Adjust the selection.
        if (index == listModel.getSize())//removed item in last position
          index--;
        list.setSelectedIndex(index);   //otherwise select same index
      }
    }
  }

}
