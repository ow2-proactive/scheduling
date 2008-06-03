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
package org.objectweb.proactive.ic2d.chartit.editors.page;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chartit.actions.LoadChartsConfigAction;
import org.objectweb.proactive.ic2d.chartit.actions.SaveChartsConfigAction;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;


/**
 * This class acts as a wrapper for the charts section.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ChartsSectionWrapper extends AbstractChartItSectionWrapper {

    /**
     * The used data providers list widget
     */
    protected final List allChartsListWidget;

    /**
     * Create a new instance of this class.
     * 
     * @param overviewPage
     *            The overview page that contains all sections
     * @param parent
     *            The parent Composite
     * @param toolkit
     *            The toolkit used to create widgets
     */
    public ChartsSectionWrapper(final OverviewPage overviewPage, final Composite bodyComposite,
            final FormToolkit toolkit) {
        super(overviewPage);

        final Section chartsSection = toolkit.createSection(bodyComposite, Section.TWISTIE |
            Section.EXPANDED | ExpandableComposite.COMPACT | Section.TITLE_BAR);
        // Create a toolbar manager
        final ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        final ToolBar toolbar = toolBarManager.createControl(chartsSection);
        // Add save and load actions
        toolBarManager.add(new SaveChartsConfigAction(getEditorInput().getModelsContainer()));
        toolBarManager.add(new LoadChartsConfigAction(getEditorInput().getModelsContainer(), this,
            overviewPage.availableDataProvidersSW));
        getEditorInput().addControlToDisable(toolbar);
        toolBarManager.update(true);
        chartsSection.setTextClient(toolbar);

        chartsSection.setText("Charts");
        chartsSection.marginWidth = chartsSection.marginHeight = 0;
        chartsSection.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));

        // Fill the section with a composite with a grid layout
        final Composite rdsClient = toolkit.createComposite(chartsSection, SWT.WRAP);
        chartsSection.setClient(rdsClient);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        rdsClient.setLayout(layout);

        this.allChartsListWidget = new List(rdsClient, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        // Layout the table
        GridData gd = new GridData(GridData.FILL_BOTH);
        // gd.widthHint = 80;
        gd.heightHint = 100;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        this.allChartsListWidget.setLayoutData(gd);
        this.allChartsListWidget.addListener(SWT.Selection, new Listener() {
            public final void handleEvent(final Event e) {
                final String[] selection = allChartsListWidget.getSelection();
                if (selection.length == 1)
                    overviewPage.chartDescriptionSW.handleModelByName(selection[0]);
                else
                    overviewPage.chartDescriptionSW.emptyAllWidgets();
            }
        });
        getEditorInput().addControlToDisable(this.allChartsListWidget);

        // A composite to group the buttons
        final Composite buttonComposite = toolkit.createComposite(rdsClient, SWT.WRAP);
        buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        buttonComposite.setLayout(new FillLayout(SWT.VERTICAL));

        // Add the create button
        final Button createButton = toolkit.createButton(buttonComposite, "Create", SWT.PUSH);
        createButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                // Create a chart model with default values
                final ChartModel c = getResourceData().getModelsContainer().createNewChartModel();
                // Reflect the ui
                allChartsListWidget.add(c.getName());
                allChartsListWidget.setSelection(new String[] { c.getName() });
                overviewPage.chartDescriptionSW.handleModel(c);
            }
        });
        getEditorInput().addControlToDisable(createButton);

        // Add the remove button
        final Button removeButton = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                // Impact the model
                for (final String name : allChartsListWidget.getSelection()) {
                    getResourceData().getModelsContainer().removeByName(name);
                }

                // Then impact the widget and disable all widgets
                allChartsListWidget.remove(allChartsListWidget.getSelectionIndices());
                overviewPage.chartDescriptionSW.emptyAllWidgets();
            }
        });
        getEditorInput().addControlToDisable(removeButton);

    }

    /**
     * Returns the charts list widget
     * 
     * @return The charts list widget
     */
    public List getAllChartsListWidget() {
        return allChartsListWidget;
    }
}
