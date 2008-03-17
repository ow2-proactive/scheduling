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
package org.objectweb.proactive.ic2d.chronolog.editors.pages;

import java.util.Date;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chronolog.data.ResourceData;
import org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditor;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;


/**
 * This class represents a page with an overview of the monitored resource.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class OverviewPage extends FormPage {
    public static final String NO_SELECTED_ATTRIBUTES_ERROR_MESSAGE = "Select at least one attribute";
    /**
     * The underlying scrolled block with 
     */
    protected ScrolledPropertiesBlock block;
    /**
     * The overview form
     */
    protected Form overviewForm;

    /**
     * Creates a new instance of <code>OverviewPage</code>.
     * 
     * @param editor The parent editor
     */
    public OverviewPage(ChronologDataEditor editor) {
        super(editor, "Overview", "Overview");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(final IManagedForm managedForm) {

        // Create as many sections as contained providers in the editor input
        // model
        final ResourceData ressourceData = ((ChronologDataEditorInput) this.getEditorInput())
                .getRessourceData();

        this.overviewForm = managedForm.getForm().getForm();
        final FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading(this.overviewForm);
        this.overviewForm.setText("Overview");
        // Add an hyperlink listener to handle messages activation
        this.overviewForm.addMessageHyperlinkListener(new IHyperlinkListener() {

            public final void linkActivated(final HyperlinkEvent e) {
                if (e.getLabel().equals(NO_SELECTED_ATTRIBUTES_ERROR_MESSAGE)) {
                    if (block != null) {
                        block.tableViewer.getTable().setFocus();
                        overviewForm.setMessage(null, IMessageProvider.NONE);
                    }
                }
            }

            public final void linkEntered(HyperlinkEvent e) {
            }

            public final void linkExited(HyperlinkEvent e) {
            }
        });

        // Create a section for the Ressource Description section
        final Section section = toolkit.createSection(this.overviewForm.getBody(), Section.TITLE_BAR);
        section.setText("Ressource Description");
        section.marginWidth = 10;
        section.marginHeight = 5;
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Fill the section with a composite with a grid layout
        final Composite client = toolkit.createComposite(section);
        final GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.verticalSpacing = 10;
        layout.numColumns = 2;
        client.setLayout(layout);
        section.setClient(client);
        // Ressource name
        Label l = toolkit.createLabel(client, "Name:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(client, ressourceData.getRessourceDescriptor().getName());
        // Ressource JMX ObjectName
        l = toolkit.createLabel(client, "JMX ObjectName:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit
                .createLabel(client, ressourceData.getRessourceDescriptor().getObjectName()
                        .getCanonicalName());
        // Ressource location
        l = toolkit.createLabel(client, "URL:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(client, ressourceData.getRessourceDescriptor().getHostUrlServer());
        // Monitored since
        l = toolkit.createLabel(client, "Monitored Since:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(client, new Date().toString());

        // Add a dummy label to avoid graphical bug
        l = toolkit.createLabel(client, "");

        // Create Available attributes section
        this.block = new ScrolledPropertiesBlock((ChronologDataEditorInput) this.getEditorInput());
        this.block.createContent(managedForm);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormPage#canLeaveThePage()
     */
    @Override
    public boolean canLeaveThePage() {
        // First get the store
        final AbstractDataStore store = ((ChronologDataEditorInput) this.getEditorInput()).getStore();
        // If store is empty show an error message and refuse to leave page
        if (store.getElements().size() == 0) {
            this.overviewForm.setMessage(NO_SELECTED_ATTRIBUTES_ERROR_MESSAGE, IMessageProvider.ERROR);
            return false;
        }
        // If the collector is not running start collecting data
        if (!store.getRunnableDataCollector().isRunning()) {
            // IMPORTANT Start data collector before creating editparts from
            // models because the edit part creation relies on an correctly
            // initialized data store !!!
            if (store.initDataStoreAndStartCollectingData()) {
                final GraphsPage p = ((GraphsPage) ((ChronologDataEditor) this.getEditor())
                        .findPage("Graphs"));
                p.fillForm();
                // Disable all editable controls
                ((ChronologDataEditorInput) this.getEditorInput()).setEnabledControls(false);
                return true;
            }
            return false;
        }
        return true;
    }
}
