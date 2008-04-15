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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;


/**
 * A standard details page used for all sort of types.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public abstract class AbstractDetailsPage<E extends AbstractTypeModel> implements IDetailsPage, Runnable {

    /**
     * The reference of editor input
     */
    protected final ChronologDataEditorInput editorInput;
    /**
     * The generic reference of the associated type model
     */
    protected E type;
    /**
     * The reference of the table viewer
     */
    protected final TableViewer tableViewer;
    /**
     * The reference of the managed form
     */
    protected IManagedForm mform;
    /**
     * The text widget used for the description of the attribute
     */
    protected Text attributeDescriptionText;
    /**
     * The text widget used for the value of the attribute
     */
    protected Text attributeValueText;
    /**
     * The selection button used by all underlying pages
     */
    protected Button selectionButton;

    /**
     * Creates details page with the specified input and table viewer.
     * 
     * @param editorInput The input used to create this page
     * @param tableViewer The table viewer used to create this page
     */
    public AbstractDetailsPage(final ChronologDataEditorInput editorInput, final TableViewer tableViewer) {
        this.editorInput = editorInput;
        this.tableViewer = tableViewer;
    }

    /**
     * @return
     */
    public ChronologDataEditorInput getEditorInput() {
        return editorInput;
    }

    /**
     * @return
     */
    public AbstractTypeModel getType() {
        return this.type;
    }

    /**
     * @param parent
     * @return
     */
    public FormToolkit createInternalContents(final Composite parent) {
        final FormToolkit toolkit = mform.getToolkit();
        // Attribute Description
        Label label = toolkit.createLabel(parent, "Description:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.attributeDescriptionText = toolkit.createText(parent, "", SWT.SINGLE);
        this.attributeDescriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Attribute Value
        label = toolkit.createLabel(parent, "Value:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.attributeValueText = toolkit.createText(parent, "", SWT.BORDER | SWT.MULTI | SWT.H_SCROLL |
            SWT.V_SCROLL /* | SWT.WRAP */);
        this.attributeValueText.setEditable(false);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 100;
        gridData.widthHint = 100;
        this.attributeValueText.setLayoutData(gridData);

        // Selection
        label = toolkit.createLabel(parent, "Select :");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

        // Default flag button disabled by default
        // Do not add this control to disable list 
        this.selectionButton = toolkit.createButton(parent, "Use this data provider for charting", SWT.CHECK);
        this.selectionButton.setEnabled(false);
        return toolkit;
    }

    /**
     * Adds a selection listener to the selection button.
     * The listener adds/removes the type model to/from resource 
     * This method must be called AFTER a call to <code>createInternalContents()</code>
     */
    protected void addSelectionButtonListener() {
        // Attach a listener
        this.selectionButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(SelectionEvent e) {
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
    }

    /**
     * 
     */
    public void update() {
        this.tableViewer.getControl().getDisplay().asyncExec(this);
    }

    // ////////////////////////////
    // IDetailsPage implementation
    // ////////////////////////////

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IDetailsPage#createContents(org.eclipse.swt.widgets.Composite)
     */
    public void createContents(final Composite parent) {
        final TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 5;
        layout.leftMargin = 5;
        layout.rightMargin = 2;
        layout.bottomMargin = 2;
        parent.setLayout(layout);

        final FormToolkit toolkit = mform.getToolkit();
        // Attribute Details and Charting Section
        final Section section = toolkit.createSection(parent, Section.TITLE_BAR);
        section.marginWidth = 10;
        section.setText("Data Provider Details and Charting");
        final TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
        td.grabHorizontal = true;
        section.setLayoutData(td);

        final Composite client = toolkit.createComposite(section);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 10;
        gridLayout.numColumns = 2;
        client.setLayout(gridLayout);
        section.setClient(client);

        this.createInternalContents(client);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
     */
    public void commit(boolean onSave) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IFormPart#dispose()
     */
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IDetailsPage#initialize(org.eclipse.ui.forms.IManagedForm)
     */
    public void initialize(IManagedForm form) {
        this.mform = form;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IFormPart#isDirty()
     */
    public boolean isDirty() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IFormPart#isStale()
     */
    public boolean isStale() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IDetailsPage#refresh()
     */
    public void refresh() {
        update();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IFormPart#setFocus()
     */
    public void setFocus() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IFormPart#setFormInput(java.lang.Object)
     */
    public boolean setFormInput(Object input) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IPartSelectionListener#selectionChanged(org.eclipse.ui.forms.IFormPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public abstract void selectionChanged(IFormPart part, ISelection selection);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public abstract void run();
}
