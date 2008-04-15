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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chronolog.editors.pages.details;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.objectweb.proactive.ic2d.chronolog.data.ResourceData;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.StringArrayBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;
import org.objectweb.proactive.ic2d.chronolog.editors.pages.OverviewPage;


/**
 * Mapping for <code>StringArrayBasedTypeModel</code>. 
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class StringArrayBasedTypeDetailsPage extends AbstractDetailsPage<StringArrayBasedTypeModel> {

    /**
     * The chart type combo box 
     */
    protected Combo chartChoiceCombo;

    /**
     * The associated values combo box 
     */
    protected Combo associatedValuesCombo;

    /**     
     * Creates a new instance of <code>StringArrayBasedTypeDetailsPage</code>.
     *
     * @param editorInput
     * @param tableViewer
     */
    public StringArrayBasedTypeDetailsPage(final ChronologDataEditorInput editorInput,
            final TableViewer tableViewer) {
        super(editorInput, tableViewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.editors.pages.details.AbstractDetailsPage#createInternalContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public FormToolkit createInternalContents(final Composite parent) {
        final FormToolkit toolkit = super.createInternalContents(parent);
        // Add selection button to disable list
        super.editorInput.addControlToDisable(super.selectionButton);

        // Enable the selection button      
        if (!this.editorInput.getStore().getRunnableDataCollector().isRunning()) {
            super.selectionButton.setEnabled(true);
        }

        // Add chart related information
        Label label = toolkit.createLabel(parent, "Chart Type:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

        // Add a chart type selection combo
        this.chartChoiceCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.chartChoiceCombo.setItems(StringArrayBasedTypeModel.charts);
        super.editorInput.addControlToDisable(this.chartChoiceCombo);
        // Set the default selection
        this.chartChoiceCombo.select(0);
        // Add a selection listener
        this.chartChoiceCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                StringArrayBasedTypeDetailsPage.super.type
                        .setChartChoice(StringArrayBasedTypeDetailsPage.this.chartChoiceCombo
                                .getSelectionIndex());
            }
        });

        // Add associated values label
        label = toolkit.createLabel(parent, "Associated Values:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

        // Add a combo for associated values
        this.associatedValuesCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        super.editorInput.addControlToDisable(this.associatedValuesCombo);
        // Add a selection listener
        //        this.associatedValuesCombo.addSelectionListener(new SelectionAdapter() {
        //            public void widgetSelected(SelectionEvent e) {
        //                System.out.println(".widgetSelected() combo box--------> " + type.hashCode());
        //                type.setAssociatedValuesAttribute(associatedValuesCombo.getItem(associatedValuesCombo
        //                        .getSelectionIndex()));
        //            }
        //        });  

        this.associatedValuesCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                //                type.setAssociatedValuesAttribute(associatedValuesCombo.getItem(associatedValuesCombo
                //                      .getSelectionIndex()));
            }

            public void widgetSelected(SelectionEvent e) {
                System.out.println(".widgetSelected()");
                type.setAssociatedValuesAttribute(associatedValuesCombo.getItem(associatedValuesCombo
                        .getSelectionIndex()));
            }
        });

        updateAssociatedValuesCombo();

        // Attach a custom listener to the selection button in order to check if values are setted
        super.selectionButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(SelectionEvent e) {
                if (type.getAssociatedValuesAttribute() == null) {
                    OverviewPage p = (OverviewPage) mform.getContainer();
                    p.getOverviewForm().setMessage("Select at least one attribute for associated values",
                            IMessageProvider.ERROR);
                    selectionButton.setSelection(false);
                    return;
                }
                if (type.isUsed())
                    type.removeFromResource();
                else
                    type.addToRessource();
                // Try to find the associated widget for this type and update it
                final TableItem tableItem = (TableItem) tableViewer.testFindItem(type);
                if (tableItem != null) {
                    // 2 is the "Used" column
                    tableItem.setText(2, "" + type.isUsed());
                }
            }
        });

        return toolkit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.editors.pages.details.AbstractDetailsPage#setFocus()
     */
    @Override
    public void setFocus() {
        this.chartChoiceCombo.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IDetailsPage#inputChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void selectionChanged(IFormPart part, ISelection selection) {
        final IStructuredSelection ssel = (IStructuredSelection) selection;
        if (ssel.size() == 1) {
            super.type = (StringArrayBasedTypeModel) ssel.getFirstElement();
            super.update();
        } else {
            super.type = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.editors.pages.details.AbstractDetailsPage#run()
     */
    @Override
    public void run() {
        // Update the attribute description and value
        super.attributeDescriptionText.setText(super.type.getDataProvider().getDescription());
        final String[] arr = super.type.getProvidedValue();
        super.attributeValueText.setText("");
        for (final String s : arr) {
            super.attributeValueText.append(s + "\n");
        }
        // Update the selection state
        super.selectionButton.setSelection(super.type.isUsed());
        updateAssociatedValuesCombo();
    }

    /**
     * Updates the values of the associated values combo
     */
    private void updateAssociatedValuesCombo() {
        // First get all acceptable attribute names for associated values
        final String[] acceptedAttributeNames = getAllValuesAttributesAcceptedByModel();
        if (acceptedAttributeNames.length == 0) {
            this.associatedValuesCombo.setEnabled(false);
            return;
        }
        this.associatedValuesCombo.setEnabled(true);
        this.associatedValuesCombo.setItems(acceptedAttributeNames);
        // Some times this occur when the type was not updated
        if (super.type == null) {
            return;
        }
        // Get the already selected attribute
        final String associatedValuesAttribute = super.type.getAssociatedValuesAttribute();
        if (associatedValuesAttribute != null) {
            // Find this attribute in the accepted attribute names
            int index = 0;
            for (final String name : acceptedAttributeNames) {
                if (name.equals(associatedValuesAttribute)) {
                    this.associatedValuesCombo.select(index);
                }
                index++;
            }
        }
        //        } else {
        //            this.associatedValuesCombo.select(0);
        //        }
        // Pack the combo to make the widget width fit the selected item
        this.associatedValuesCombo.pack();
    }

    /**
     * Returns the attribute names accepted by this model. 
     * @return An array of names of attributes accepted by the model 
     */
    private String[] getAllValuesAttributesAcceptedByModel() {
        final ArrayList<String> res = new ArrayList<String>();
        AbstractTypeModel model;
        for (final TableItem tableItem : super.tableViewer.getTable().getItems()) {
            model = (AbstractTypeModel) tableItem.getData();
            if (ResourceData.contains(StringArrayBasedTypeModel.associatedValuesTypes, model
                    .getDataProvider().getType())) {
                res.add(model.getDataProvider().getName());
            }
        }
        return res.toArray(new String[] {});
    }
}
