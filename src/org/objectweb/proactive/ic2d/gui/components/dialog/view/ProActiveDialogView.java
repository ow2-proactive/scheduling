/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.gui.components.dialog.view;

import org.objectweb.fractal.gui.dialog.model.DialogModel;
import org.objectweb.fractal.gui.dialog.view.BasicDialogView;

import org.objectweb.proactive.ic2d.gui.components.dialog.model.ProActiveDialogModel;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveConfigurationListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;


/**
 * A configuration view that displays the configuration's root component as a
 * dialog.
 */
public class ProActiveDialogView extends BasicDialogView
    implements ProActiveConfigurationListener {
    private JButton exportedVirtualNodeAddButton;
    private JButton exportedVirtualNodesCompositionRemoveButton;
    private JButton checkVirtualNodesCompositionButton;
    private JTable exportedVirtualNodesCompositionTable;
    private JTextField virtualNodeField;
    private JTextField exportedVirtualNodeNameField;
    private JTextField composingVirtualNodeNamesField;
    protected ProActiveButtonListener proActiveButtonListener;
    protected ProActiveListListener proActiveListListener;

    /**
     *
     */
    public ProActiveDialogView() {
        super();
        // exported virtual nodes panel
        proActiveButtonListener = new ProActiveButtonListener();
        proActiveListListener = new ProActiveListListener();
        JPanel evncPanel = new JPanel();
        evncPanel.setLayout(new BorderLayout());
        evncPanel.add(new JLabel("Exported Virtual Nodes Composition"), "North");
        evncPanel.add(viewExportedVirtualNodesCompositionPanel(), "Center");
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 3, 3, 3);
        constraints.gridy = 6;
        constraints.weighty = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        //constraints.weighty = 0;
        ((GridBagLayout) getLayout()).setConstraints(evncPanel, constraints);
        add(evncPanel);

        // exported virtual nodes creation panel
        JPanel createExportedVirtualNodesPanel = new JPanel();
        createExportedVirtualNodesPanel.setLayout(new BorderLayout());
        createExportedVirtualNodesPanel.add(createExportedVirtualNodesPanel(),
            "Center");
        constraints.gridy = 7;
        //constraints_vn.weightx = 1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        ((GridBagLayout) getLayout()).setConstraints(createExportedVirtualNodesPanel,
            constraints);
        add(createExportedVirtualNodesPanel);

        // virtual node panel
        JPanel virtualNodePanel = new JPanel();
        virtualNodePanel.setLayout(new BorderLayout());
        virtualNodePanel.add(createVirtualNodePanel(), "Center");
        //constraints_vn.insets = new Insets(3, 3, 3, 3);
        constraints.gridy = 8;
        //constraints_vn.weightx = 1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        ((GridBagLayout) getLayout()).setConstraints(virtualNodePanel,
            constraints);
        add(virtualNodePanel);
    }

    public void bindFc(final String clientItfName, final Object serverItf) {
        super.bindFc(clientItfName, serverItf);
        if (DIALOG_MODEL_BINDING.equals(clientItfName)) {
            model = (DialogModel) serverItf;
            exportedVirtualNodesCompositionTable.setModel(((ProActiveDialogModel) model).getExportedVirtualNodesCompositionTableModel());
            exportedVirtualNodesCompositionTable.setSelectionModel(((ProActiveDialogModel) model).getExportedVirtualNodesCompositionTableSelectionModel());
            exportedVirtualNodesCompositionTable.getSelectionModel()
                                                .addListSelectionListener(proActiveListListener);
            virtualNodeField.setDocument(((ProActiveDialogModel) model).getVirtualNodeTextFieldModel());
            exportedVirtualNodeNameField.setDocument(((ProActiveDialogModel) model).getExportedVirtualNodeNameTextFieldModel());
            composingVirtualNodeNamesField.setDocument(((ProActiveDialogModel) model).getComposingVirtualNodeNamesTextFieldModel());
        }
    }

    private JPanel viewExportedVirtualNodesCompositionPanel() {
        exportedVirtualNodesCompositionTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(exportedVirtualNodesCompositionTable);

        exportedVirtualNodesCompositionRemoveButton = new JButton("Remove");
        exportedVirtualNodesCompositionRemoveButton.addActionListener(proActiveButtonListener);
        checkVirtualNodesCompositionButton = new JButton("Check composition");
        checkVirtualNodesCompositionButton.addActionListener(proActiveButtonListener);
        exportedVirtualNodesCompositionRemoveButton.setEnabled(false);

        JPanel panel = new JPanel();
        GridBagLayout bagLayout = new GridBagLayout();
        panel.setLayout(bagLayout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridheight = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        bagLayout.setConstraints(scrollPane, constraints);
        panel.add(scrollPane);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0;
        constraints.weighty = 0;
        bagLayout.setConstraints(exportedVirtualNodesCompositionRemoveButton,
            constraints);
        panel.add(exportedVirtualNodesCompositionRemoveButton);

        constraints.gridx = 1;
        constraints.gridy = 1;
        bagLayout.setConstraints(checkVirtualNodesCompositionButton, constraints);
        panel.add(checkVirtualNodesCompositionButton);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridheight = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        JPanel emptyPanel = new JPanel();
        bagLayout.setConstraints(emptyPanel, constraints);
        panel.add(emptyPanel);

        return panel;
    }

    private JPanel createExportedVirtualNodesPanel() {
        JPanel panel = new JPanel();

        GridBagLayout bagLayout = new GridBagLayout();
        panel.setLayout(bagLayout);

        GridBagConstraints constraints = new GridBagConstraints();
        JLabel label = new JLabel("Exported Virtual Node", SwingConstants.LEFT);
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        bagLayout.setConstraints(label, constraints);
        panel.add(label);

        exportedVirtualNodeNameField = new JTextField(30);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        bagLayout.setConstraints(exportedVirtualNodeNameField, constraints);
        panel.add(exportedVirtualNodeNameField);

        label = new JLabel("Composing Virtual Nodes", SwingConstants.LEFT);
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx = 0;
        bagLayout.setConstraints(label, constraints);
        panel.add(label);

        composingVirtualNodeNamesField = new JTextField(30);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        bagLayout.setConstraints(composingVirtualNodeNamesField, constraints);
        panel.add(composingVirtualNodeNamesField);

        exportedVirtualNodeAddButton = new JButton("Add.");
        exportedVirtualNodeAddButton.setPreferredSize(new Dimension(80, 20));
        exportedVirtualNodeAddButton.addActionListener(proActiveButtonListener);
        constraints.gridy = 1;
        constraints.gridx = 2;
        bagLayout.setConstraints(exportedVirtualNodeAddButton, constraints);
        panel.add(exportedVirtualNodeAddButton);

        return panel;
    }

    private JPanel createVirtualNodePanel() {
        JPanel panel = new JPanel();

        GridBagLayout bagLayout = new GridBagLayout();
        panel.setLayout(bagLayout);

        GridBagConstraints constraints = new GridBagConstraints();

        JLabel label = new JLabel("Virtual Node (no spaces, finishes with * if cardinality is multiple) :",
                SwingConstants.LEFT);
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        bagLayout.setConstraints(label, constraints);
        panel.add(label);

        virtualNodeField = new JTextField();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1;
        constraints.weighty = 1;
        bagLayout.setConstraints(virtualNodeField, constraints);
        panel.add(virtualNodeField);

        return panel;
    }

    /**
     *
     */
    public void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNodeName, String oldValue) {
        if (component == configuration.getRootComponent()) {
            statusChanged();
        }
    }

    /**
     *
     */
    public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
        if (component == configuration.getRootComponent()) {
            statusChanged();
        }
    }

    protected class ProActiveListListener extends ListListener {
        public void valueChanged(final ListSelectionEvent e) {
            super.valueChanged(e);
            ListSelectionModel l;
            l = ((ProActiveDialogModel) model).getExportedVirtualNodesCompositionTableSelectionModel();
            if (e.getSource() == l) {
                exportedVirtualNodesCompositionRemoveButton.setEnabled(!l.isSelectionEmpty());
            }

            // 
        }
    }

    protected class ProActiveButtonListener extends ButtonListener {
        public void actionPerformed(final ActionEvent e) {
            super.actionPerformed(e);
            Object o = e.getSource();
            List listeners = new ArrayList(ProActiveDialogView.this.listeners.values());
            if (o == exportedVirtualNodeAddButton) {
                for (int i = 0; i < listeners.size(); ++i) {
                    ProActiveDialogViewListener l = (ProActiveDialogViewListener) listeners.get(i);
                    l.addExportedVirtualNodesCompositionButtonClicked();
                }
            } else if (o == exportedVirtualNodesCompositionRemoveButton) {
                for (int i = 0; i < listeners.size(); ++i) {
                    ProActiveDialogViewListener l = (ProActiveDialogViewListener) listeners.get(i);
                    l.removeExportedVirtualNodesCompositionButtonClicked();
                }
            } else if (o == checkVirtualNodesCompositionButton) {
                for (int i = 0; i < listeners.size(); ++i) {
                    ProActiveDialogViewListener l = (ProActiveDialogViewListener) listeners.get(i);
                    l.checkExportedVirtualNodesCompositionButtonClicked();
                }
            }
        }
    }
}
