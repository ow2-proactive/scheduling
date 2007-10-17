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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.binarytree;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;


public class SearchPane extends JPanel {
    private JScrollPane scrollPane;
    private JTable research;
    private SearchModel model;

    public SearchPane() {
        super(false);

        model = new SearchModel();
        research = new JTable(model);
        model.addTableModelListener(research);

        scrollPane = new JScrollPane(research);

        setLayout(new GridLayout(1, 0));
        add(scrollPane);

        scrollPane.setPreferredSize(new Dimension(90, 292));
    }

    public void updateKeyValue(String[] keyValue) {
        model.updateKeyValue(keyValue);
    }

    public void clear() {
        model.clear();
    }
}
