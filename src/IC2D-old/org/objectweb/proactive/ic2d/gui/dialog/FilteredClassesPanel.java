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
package org.objectweb.proactive.ic2d.gui.dialog;

import org.objectweb.proactive.ic2d.util.ActiveObjectFilter;


public class FilteredClassesPanel extends javax.swing.JPanel {
    private javax.swing.JList list;
    private javax.swing.DefaultListModel listModel;

    public FilteredClassesPanel(ActiveObjectFilter filter) {
        super(new java.awt.GridLayout(1, 1));
        listModel = new javax.swing.DefaultListModel();
        java.util.Iterator iterator = filter.iterator();
        if (!iterator.hasNext()) {
            listModel.addElement(new javax.swing.JCheckBox(
                    "There is no filtered classes."));
        } else {
            listModel.addElement(new javax.swing.JCheckBox(
                    "Uncheck the classes you don't want to filter anymore"));
        }
        while (iterator.hasNext()) {
            javax.swing.JCheckBox cb = new javax.swing.JCheckBox((String) iterator.next());
            cb.setSelected(true);
            listModel.addElement(cb);
        }

        //Create the list and put it in a scroll pane
        list = new javax.swing.JList(listModel);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }
                    if (list.getSelectedIndex() != -1) {
                        javax.swing.JCheckBox cb = (javax.swing.JCheckBox) listModel.get(list.getSelectedIndex());
                        cb.setSelected(!cb.isSelected());
                    }
                }
            });
        list.setCellRenderer(new MyCellRenderer());
        javax.swing.JScrollPane listScrollPane = new javax.swing.JScrollPane(list);
        add(listScrollPane);
    }

    public boolean updateFilter(ActiveObjectFilter filter) {
        boolean updated = false;
        for (int i = 1; i < listModel.size(); i++) {
            javax.swing.JCheckBox cb = (javax.swing.JCheckBox) listModel.get(i);
            if (!cb.isSelected()) {
                updated = filter.removeClass(cb.getText()) || updated;
            }
        }
        return updated;
    }

    private class MyCellRenderer implements javax.swing.ListCellRenderer {
        public java.awt.Component getListCellRendererComponent(
            javax.swing.JList list, Object value, // value to display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // the list and the cell have the focus
         {
            javax.swing.JCheckBox cb = (javax.swing.JCheckBox) value;
            if (isSelected) {
                cb.setBackground(list.getSelectionBackground());
                cb.setForeground(list.getSelectionForeground());
            } else {
                cb.setBackground(list.getBackground());
                cb.setForeground(list.getForeground());
            }
            cb.setEnabled(list.isEnabled());
            cb.setFont(list.getFont());
            return cb;
        }
    }
}
