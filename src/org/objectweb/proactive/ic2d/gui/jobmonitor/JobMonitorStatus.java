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
package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeNode;


class StatusCell extends JPanel implements JobMonitorConstants {
    private JLabel name;
    private JLabel state;
    private int key;
    private String clearedNameLabel;

    public StatusCell(int key) {
        this.key = key;
        this.clearedNameLabel = "No " + NAMES[KEY2INDEX[key]] +
            " in the hierarchy";
        setLayout(new GridLayout(3, 1));
        setBackground(Color.WHITE);

        JLabel title = new JLabel(NAMES[KEY2INDEX[key]],
                Icons.getIconForKey(key), SwingConstants.LEFT);

        add(title);

        name = prepareLabel();
        add(name);

        state = prepareLabel();
        add(state);
    }

    private static JLabel prepareLabel() {
        JLabel label = new JLabel();
        label.setFont(label.getFont().deriveFont(Font.PLAIN));

        return label;
    }

    public void setNameLabel(String name) {
        this.name.setText(name);
    }

    public void updateDeleted(String deletedTime) {
        if (deletedTime == null) {
            state.setText("Alive");
        } else {
            state.setText("Unresponding for " + deletedTime);
        }
    }

    public void repaint() {
        if (name != null) {
            name.repaint();
        }

        if (state != null) {
            state.repaint();
        }
    }
}


public class JobMonitorStatus extends Box implements JobMonitorConstants,
    TreeSelectionListener {
    private JTree tree;

    public JobMonitorStatus(JTree tree) {
        super(BoxLayout.Y_AXIS);
        this.tree = tree;
        tree.addTreeSelectionListener(this);

        JPanel blank = new JPanel();
        blank.setBackground(Color.WHITE);
        add(blank);
    }

    public void valueChanged(TreeSelectionEvent e) {
        DataTreeNode node = (DataTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        removeAll();

        while (!node.isRoot()) {
            BasicMonitoredObject object = node.getObject();
            StatusCell cell = new StatusCell(object.getKey());
            cell.setNameLabel(object.getFullName());
            cell.updateDeleted(object.getDeletedTime());

            Dimension d = new Dimension(cell.getMaximumSize().width,
                    cell.getMinimumSize().height);
            cell.setMaximumSize(d);

            d = new Dimension(cell.getPreferredSize().width,
                    cell.getMinimumSize().height);
            cell.setPreferredSize(d);

            add(cell, 0);
            node = (DataTreeNode) node.getParent();
        }

        JPanel blank = new JPanel();
        blank.setBackground(Color.WHITE);
        add(blank);
        revalidate();
        repaint();
    }
}
