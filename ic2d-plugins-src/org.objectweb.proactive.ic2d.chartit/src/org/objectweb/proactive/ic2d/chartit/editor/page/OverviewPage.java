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
package org.objectweb.proactive.ic2d.chartit.editor.page;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelValidator;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditor;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditorInput;


/**
 * This class represents a page with an overview of the monitored resource.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class OverviewPage extends FormPage {

    public static final String NO_CHARTS_ERROR_MESSAGE = "At least one chart is needed";

    /**
     * The overview form
     */
    protected Form overviewForm;

    /**
     * A section for resource description
     */
    protected ResourceDescriptionSectionWrapper resourceDescriptionSW;

    /**
     * A section for available data providers 
     */
    protected AvailableDataProvidersSectionWrapper availableDataProvidersSW;

    /**
     * A section for data provider details
     */
    protected DataProviderDetailsSectionWrapper dataProviderDetailsSW;

    /**
     * A section for chart description
     */
    protected ChartDescriptionSectionWrapper chartDescriptionSW;

    /**
     * A section for charts
     */
    protected ChartsSectionWrapper chartsSW;

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

        GridLayout gridLayout = new GridLayout(2, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 5;
        gridLayout.marginBottom = 5;
        gridLayout.marginLeft = 5;
        gridLayout.marginRight = 5;

        final Composite bodyComposite = managedForm.getForm().getBody();
        bodyComposite.setLayout(gridLayout);

        // Create a section for the resource description
        this.resourceDescriptionSW = new ResourceDescriptionSectionWrapper(this, bodyComposite, toolkit);

        // Left composite with 1 column
        final Composite left = toolkit.createComposite(bodyComposite, SWT.NONE);
        left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        left.setLayout(gridLayout);

        // Right composite with 1 column
        final Composite right = toolkit.createComposite(bodyComposite, SWT.NONE);
        right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        right.setLayout(gridLayout);

        // Create a section in the left column for the available data providers
        this.availableDataProvidersSW = new AvailableDataProvidersSectionWrapper(this, managedForm, left,
            toolkit);

        // Create a section in the left column for the data provider details
        this.dataProviderDetailsSW = new DataProviderDetailsSectionWrapper(this, left, toolkit);

        // Create a section in the right column for the chart description
        this.chartDescriptionSW = new ChartDescriptionSectionWrapper(this, right, toolkit);

        // Create a section in the right column for the charts list
        this.chartsSW = new ChartsSectionWrapper(this, right, toolkit);
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

        // If the collector is not running start collecting data
        if (!store.isRunning()) {

            // Validate all models
            for (final ChartModel modelToValidate : store.getModels()) {
                if (!ChartModelValidator.validate(modelToValidate)) {
                    this.overviewForm.setMessage("The chart " + modelToValidate.getName() + " is invalid !",
                            IMessageProvider.ERROR);
                    return false;
                }
            }

            // IMPORTANT Start data collector before creating editparts from
            // models because the edit part creation relies on an correctly
            // initialized data store !!!
            store.startCollector();
            final ChartsPage p = ((ChartsPage) ((ChartItDataEditor) this.getEditor())
                    .findPage(ChartsPage.CHARTS_PAGE_NAME));
            p.fillForm();
            // Disable all editable controls
            ((ChartItDataEditorInput) this.getEditorInput()).setEnabledControls(false);
            return true;
        }
        return true;
    }

    /**
     * Returns the typed reference of the editor input.
     * @return The typed reference of the editor input
     */
    public ChartItDataEditorInput getChartItDataEditorInput() {
        return (ChartItDataEditorInput) this.getEditorInput();
    }

    /**
     * Returns an instance of the charts section wrapper. 
     * @return An instance of the charts section wrapper
     */
    public ChartsSectionWrapper getChartsSW() {
        return this.chartsSW;
    }
}