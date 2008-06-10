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

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.ChartType;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


/**
 * This class acts as a wrapper for the chart description section.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public class ChartDescriptionSectionWrapper extends AbstractChartItSectionWrapper {

    /**
     * A boolean variable to know if this handler is enabled
     */
    protected boolean isEnabled;

    /**
     * The chart model associated to this section
     */
    protected ChartModel chartModel;

    /**
     * The text widget used for the description of the attribute
     */
    protected final Text chartNameTextWidget;

    /**
     * The chart type combo box widget
     */
    protected final ImageCombo chartTypeComboWidget;

    /**
     * The refresh period spinner widget
     */
    protected final Spinner refreshPeriodSpinnerWidget;

    /**
     * The used data providers list widget
     */
    protected final List usedDataProvidersListWidget;

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
    public ChartDescriptionSectionWrapper(final OverviewPage overviewPage, final Composite parent,
            final FormToolkit toolkit) {
        super(overviewPage);

        this.isEnabled = true;

        final Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Chart Description");
        section.setDescription("There are 2 possible data providers compositions :\n"
            + "- A single String[] provider and a single Number[] provider\n"
            + "- A set of number typed providers");
        section.marginWidth = section.marginHeight = 0;
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // Fill the section with a composite with a grid layout
        final Composite client = toolkit.createComposite(section, SWT.NONE);
        section.setClient(client);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.verticalSpacing = 10;
        layout.numColumns = 3;
        client.setLayout(layout);

        // Create all graphical widgets

        // Chart name
        Label label = toolkit.createLabel(client, "Name:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.chartNameTextWidget = toolkit.createText(client, "", SWT.BORDER | SWT.SINGLE);
        this.chartNameTextWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        this.chartNameTextWidget.addModifyListener(new ModifyListener() {

            public final void modifyText(final ModifyEvent e) {
                final String enteredText = chartNameTextWidget.getText();
                if ("".equals(enteredText))
                    return;
                // Reflect the model name
                chartModel.setName(enteredText);
                // Reflect the chart list
                final List allChartsListWidget = overviewPage.chartsSW.allChartsListWidget;
                allChartsListWidget.setItem(allChartsListWidget.getSelectionIndex(), enteredText);
            }
        });
        getEditorInput().addControlToDisable(this.chartNameTextWidget);

        // Add the 'Generate Name' button
        final Button generateNameButton = toolkit.createButton(client, "Generate Name", SWT.PUSH);
        generateNameButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        generateNameButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                final String[] items = usedDataProvidersListWidget.getItems();
                if (items.length == 0)
                    return;
                final String currentName = chartNameTextWidget.getText();
                final String generatedName = Arrays.toString(items);
                String name;
                // Generate the name from used data providers
                if (currentName.length() == 0) {
                    name = generatedName;
                } else {
                    name = currentName + " " + generatedName;
                }
                // Reflect the model name
                chartModel.setName(name);
                chartNameTextWidget.setText(name);
                // Reflect the chart list
                List allChartsListWidget = overviewPage.chartsSW.allChartsListWidget;
                allChartsListWidget.setItem(allChartsListWidget.getSelectionIndex(), name);
            }
        });
        getEditorInput().addControlToDisable(generateNameButton);

        // Chart type combo box
        label = toolkit.createLabel(client, "Chart Type:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.chartTypeComboWidget = new ImageCombo(client, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (ChartType chartType : ChartType.values()) {
            this.chartTypeComboWidget.add(chartType.name(), ChartType.getImage(chartType));
        }
        this.chartTypeComboWidget.select(0);
        this.chartTypeComboWidget.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        // Attach a listener to reflect the model
        this.chartTypeComboWidget.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                if (chartModel != null)
                    chartModel.setChartType(ChartType.values()[chartTypeComboWidget.getSelectionIndex()]);
            }
        });
        getEditorInput().addControlToDisable(this.chartTypeComboWidget);

        // Refresh period
        label = toolkit.createLabel(client, "Refresh Period:\n(in seconds)");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.refreshPeriodSpinnerWidget = new Spinner(client, SWT.BORDER);
        this.refreshPeriodSpinnerWidget.setMinimum(ChartModelContainer.MIN_REFRESH_PERIOD_IN_SECS);
        this.refreshPeriodSpinnerWidget.setMaximum(ChartModelContainer.MAX_REFRESH_PERIOD_IN_SECS);
        this.refreshPeriodSpinnerWidget.setSelection(ChartModelContainer.DEFAULT_REFRESH_PERIOD_IN_SECS);
        this.refreshPeriodSpinnerWidget.setIncrement(1);
        this.refreshPeriodSpinnerWidget.setPageIncrement(5);
        this.refreshPeriodSpinnerWidget.pack();
        this.refreshPeriodSpinnerWidget.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false,
            2, 1));
        // Attach a listener to reflect the model
        this.refreshPeriodSpinnerWidget.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                if (chartModel != null)
                    chartModel.setRefreshPeriod(refreshPeriodSpinnerWidget.getSelection() * 1000);
            }
        });
        getEditorInput().addControlToDisable(this.refreshPeriodSpinnerWidget);

        // List of data providers
        label = toolkit.createLabel(client, "Used Data Providers:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.usedDataProvidersListWidget = new List(client, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        // gridData.widthHint = 80;
        gridData.heightHint = 100;
        this.usedDataProvidersListWidget.setLayoutData(gridData);
        getEditorInput().addControlToDisable(this.usedDataProvidersListWidget);

        // Additional button to remove used data providers
        final Button removeButton = toolkit.createButton(client, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
        removeButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                if (chartModel == null || usedDataProvidersListWidget.getItemCount() == 0)
                    return;
                // Impact the model
                chartModel.removeProvidersByNames(usedDataProvidersListWidget.getSelection());
                // Then the widget
                usedDataProvidersListWidget.remove(usedDataProvidersListWidget.getSelectionIndices());
            }
        });
        getEditorInput().addControlToDisable(removeButton);

        // Disable all widgets
        this.setEnabledWidgets(false);
    }

    /**
     * Handles an incoming model by name
     * 
     * @param chartName The name of the chart to find in the chart container. 
     */
    public void handleModelByName(final String chartName) {
        // Find the model by name
        final ChartModel chartModel = getResourceData().getModelsContainer().getModelByName(chartName);
        this.handleModel(chartModel);
    }

    /**
     * Handles an incoming model ie updates all widgets.
     * 
     * @param model The incoming model
     */
    public void handleModel(final ChartModel model) {
        if (!this.isEnabled) {
            this.setEnabledWidgets(true);
        }

        this.chartModel = model;
        // Chart name
        this.chartNameTextWidget.setText(chartModel.getName());
        // Chart type combo box
        this.chartTypeComboWidget.select(chartModel.getChartType().ordinal());
        // Refresh period
        this.refreshPeriodSpinnerWidget.setSelection((int) chartModel.getRefreshPeriod() / 1000);
        // Used Data Providers
        String[] providerNames = new String[chartModel.getProviders().size()];
        int i = 0;
        for (final IDataProvider provider : chartModel.getProviders()) {
            providerNames[i++] = provider.getName();
        }
        this.usedDataProvidersListWidget.setItems(providerNames);
    }

    /**
     * Empty all widgets ie sets their default values then disables them.
     */
    public void emptyAllWidgets() {
        // Chart name
        this.chartNameTextWidget.setText("");
        // Chart type combo box
        this.chartTypeComboWidget.select(0);
        // Refresh period
        this.refreshPeriodSpinnerWidget.setSelection(ChartModelContainer.DEFAULT_REFRESH_PERIOD_IN_SECS);
        // Used Data Providers
        this.usedDataProvidersListWidget.setItems(new String[] {});
        // Set current chart model to null
        this.chartModel = null;
        // Disable all widgets
        this.setEnabledWidgets(false);
    }

    /**
     * Enables or disables all widgets.
     * @param enabled <code>True</code> then widgets are enabled, <code>False</code> otherwise 
     */
    public void setEnabledWidgets(final boolean enabled) {
        if (this.isEnabled == enabled) {
            return;
        }
        this.isEnabled = enabled;
        // Chart name
        this.chartNameTextWidget.setEnabled(enabled);
        // Chart type combo box
        this.chartTypeComboWidget.setEnabled(enabled);
        // Refresh period
        this.refreshPeriodSpinnerWidget.setEnabled(enabled);
        // Used Data Providers
        this.usedDataProvidersListWidget.setEnabled(enabled);
    }

    /**
     * Adds an instance of IDataProvider to the used data providers of the current chart model.
     * 
     * @param dataProvider The data provider to be used by the chart model
     */
    public void addUsedDataProvider(final IDataProvider dataProvider) {
        if (chartModel == null)
            return;
        // Add to model then reflect the ui if the data provider was added
        if (this.chartModel.addProvider(dataProvider)) {
            this.usedDataProvidersListWidget.add(dataProvider.getName());
        }
    }
}
