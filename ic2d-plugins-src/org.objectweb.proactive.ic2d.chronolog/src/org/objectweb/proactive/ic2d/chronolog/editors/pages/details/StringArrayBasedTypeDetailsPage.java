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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.objectweb.proactive.ic2d.chronolog.data.model.StringArrayBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;


/**
 * Mapping for <code>StringArrayBasedTypeModel</code>.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class StringArrayBasedTypeDetailsPage extends AbstractDetailsPage<StringArrayBasedTypeModel> {

    /**
     * 
     */
    protected Button pieChartChoice;
    /**
     * 
     */
    protected Button barChartChoice;

    /**
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
        // Add chart related information
        Label label = toolkit.createLabel(parent, "Chart Type:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

        final Composite client = toolkit.createComposite(parent);

        final GridLayout glayout = new GridLayout();
        glayout.marginWidth = glayout.marginHeight = 0;
        glayout.numColumns = 2;
        client.setLayout(glayout);

        final SelectionListener choiceListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // Integer value = (Integer)e.widget.getData();
                // if (input!=null) {
                // input.setChoice(value.intValue());
                // }
            }
        };

        this.pieChartChoice = toolkit.createButton(client, "Pie Chart", SWT.RADIO);
        super.editorInput.addControlToDisable(this.pieChartChoice);
        this.pieChartChoice.setData(new Integer(1));
        this.pieChartChoice.addSelectionListener(choiceListener);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        this.pieChartChoice.setLayoutData(gd);

        this.barChartChoice = toolkit.createButton(client, "Bar Chart", SWT.RADIO);
        super.editorInput.addControlToDisable(this.barChartChoice);
        this.barChartChoice.setData(new Integer(1));
        this.barChartChoice.addSelectionListener(choiceListener);
        gd = new GridData();
        gd.horizontalSpan = 2;
        this.barChartChoice.setLayoutData(gd);

        // Set default selection
        this.pieChartChoice.setSelection(true);

        // Add selection button to disable list
        super.editorInput.addControlToDisable(super.selectionButton);

        // Enable the selection button		
        if (!this.editorInput.getStore().getRunnableDataCollector().isRunning()) {
            super.selectionButton.setEnabled(true);
        }

        return toolkit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.editors.pages.details.AbstractDetailsPage#setFocus()
     */
    @Override
    public void setFocus() {
        this.pieChartChoice.setFocus();
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
        super.selectionButton.setSelection(super.type.isUsed());
    }
}
