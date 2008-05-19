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
package org.objectweb.proactive.ic2d.chartit.editors.pages;

import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
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
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelValidator;
import org.objectweb.proactive.ic2d.chartit.data.ResourceData;
import org.objectweb.proactive.ic2d.chartit.editors.ChartItDataEditor;
import org.objectweb.proactive.ic2d.chartit.editors.ChartItDataEditorInput;


/**
 * This class represents a page with an overview of the monitored resource.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class OverviewPage extends FormPage {

    public static final String NO_CHARTS_ERROR_MESSAGE = "At least one chart is needed";

    /**
     * The underlying scrolled block with
     */
    protected ScrolledPropertiesBlock block;

    /**
     * The overview form
     */
    protected Form overviewForm;

    /**
     * The reference on the chart description handler
     */
    protected ChartDescriptionHandler chartDescriptionHandler;

    /**
     * Creates a new instance of <code>OverviewPage</code>.
     * 
     * @param editor
     *            The parent editor
     */
    public OverviewPage(ChartItDataEditor editor) {
        super(editor, "Overview", "Overview");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        final FormToolkit toolkit = managedForm.getToolkit();
        this.overviewForm = managedForm.getForm().getForm();
        this.overviewForm.getToolBarManager().add(new Action("Go to Charts") {
            @Override
            public void run() {
                if (canLeaveThePage()) {
                    ((ChartItDataEditor) getEditor()).setActivePage("Charts");
                }
            }

        });
        this.overviewForm.getToolBarManager().update(true);
        toolkit.decorateFormHeading(this.overviewForm);
        this.overviewForm.setText("Overview");
        // Add an hyperlink listener to handle messages activation
        this.overviewForm.addMessageHyperlinkListener(new IHyperlinkListener() {

            public final void linkActivated(final HyperlinkEvent e) {
                overviewForm.setMessage(null, IMessageProvider.NONE);
            }

            public final void linkEntered(HyperlinkEvent e) {
            }

            public final void linkExited(HyperlinkEvent e) {
            }
        });

        final Composite bodyComposite = managedForm.getForm().getBody();

        // Set form body grid layout
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 5;
        gridLayout.verticalSpacing = 5;
        gridLayout.numColumns = 2;
        bodyComposite.setLayout(gridLayout);

        // Create a section for the Resource Description section
        this.createResourceDescriptionSection(bodyComposite, toolkit);

        // Create Available attributes section
        this.block = new ScrolledPropertiesBlock((ChartItDataEditorInput) this.getEditorInput(), this);
        this.block.createContent(managedForm);
        bodyComposite.setLayout(gridLayout);

        // Create the chart description section handler
        this.chartDescriptionHandler = new ChartDescriptionHandler((ChartItDataEditorInput) this
                .getEditorInput(), bodyComposite, this.block, toolkit);

    }

    private void createResourceDescriptionSection(final Composite bodyComposite, final FormToolkit toolkit) {
        final ResourceData resourceData = ((ChartItDataEditorInput) this.getEditorInput()).getResourceData();

        final Section resourceDescriptionSection = toolkit.createSection(bodyComposite, Section.TITLE_BAR |
            Section.TWISTIE | Section.EXPANDED);
        resourceDescriptionSection.setText("Resource Description");
        resourceDescriptionSection.marginWidth = 0;
        resourceDescriptionSection.marginHeight = 0;
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalSpan = 2;
        resourceDescriptionSection.setLayoutData(gd);

        // Fill the section with a composite with a grid layout
        final Composite rdsClient = toolkit.createComposite(resourceDescriptionSection, SWT.WRAP);
        final GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 5;
        layout.verticalSpacing = 10;
        layout.numColumns = 2;
        rdsClient.setLayout(layout);
        resourceDescriptionSection.setClient(rdsClient);
        // Ressource name
        Label l = toolkit.createLabel(rdsClient, "Name:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(rdsClient, resourceData.getResourceDescriptor().getName());
        // Ressource JMX ObjectName
        l = toolkit.createLabel(rdsClient, "JMX ObjectName:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(rdsClient, resourceData.getResourceDescriptor().getObjectName()
                .getCanonicalName());
        // Ressource location
        l = toolkit.createLabel(rdsClient, "URL:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(rdsClient, resourceData.getResourceDescriptor().getHostUrlServer());
        // Monitored since
        l = toolkit.createLabel(rdsClient, "Monitored Since:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(rdsClient, new Date().toString());
        // Add a dummy label to avoid graphical bug
        // l = toolkit.createLabel(rdsClient, "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormPage#canLeaveThePage()
     */
    @Override
    public boolean canLeaveThePage() {
        // First get the models container
        final ChartModelContainer store = ((ChartItDataEditorInput) this.getEditorInput())
                .getModelsContainer();
        // If store is empty show an error message and refuse to leave page
        if (store.getModels().size() == 0) {
            this.overviewForm.setMessage(NO_CHARTS_ERROR_MESSAGE, IMessageProvider.ERROR);
            return false;
        }
        // Validate all models
        for (final ChartModel modelToValidate : store.getModels()) {
            if (!ChartModelValidator.validate(modelToValidate)) {
                this.overviewForm.setMessage("The chart " + modelToValidate.getName() + " is invalid !",
                        IMessageProvider.ERROR);
                return false;
            }
        }

        // If the collector is not running start collecting data
        if (!store.isRunning()) {
            // IMPORTANT Start data collector before creating editparts from
            // models because the edit part creation relies on an correctly
            // initialized data store !!!
            store.startCollector();
            final ChartsPage p = ((ChartsPage) ((ChartItDataEditor) this.getEditor()).findPage("Charts"));
            p.fillForm();
            // Disable all editable controls
            ((ChartItDataEditorInput) this.getEditorInput()).setEnabledControls(false);
            return true;
        }
        return true;
    }

    public ChartDescriptionHandler getChartDescriptionHandler() {
        return chartDescriptionHandler;
    }
}