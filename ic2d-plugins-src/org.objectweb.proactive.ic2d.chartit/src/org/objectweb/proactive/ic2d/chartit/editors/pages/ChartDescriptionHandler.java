package org.objectweb.proactive.ic2d.chartit.editors.pages;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chartit.actions.LoadChartsConfigAction;
import org.objectweb.proactive.ic2d.chartit.actions.SaveChartsConfigAction;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.ChartType;
import org.objectweb.proactive.ic2d.chartit.data.ResourceData;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.editors.ChartItDataEditorInput;


/**
 * 
 * @author vbodnart
 * 
 */
public final class ChartDescriptionHandler {
    /**
     * The chart type combo box widget
     */
    private final ResourceData resourceData;

    /**
     * The chart type combo box widget
     */
    protected ChartModel chartModel;

    /**
     * A boolean variable to know if this handler is enabled
     */
    protected boolean isEnabled;

    /**
     * The text widget used for the description of the attribute
     */
    protected final Text chartNameTextWidget;

    /**
     * The chart type combo box widget
     */
    protected final Combo chartTypeComboWidget;

    /**
     * The refresh period spinner widget
     */
    protected final Spinner refreshPeriodSpinnerWidget;

    /**
     * The used data providers list widget
     */
    protected final List usedDataProvidersListWidget;

    /**
     * The used data providers list widget
     */
    protected final List allChartsListWidget;

    /**
     * The button to remove used data providers
     */
    protected final Button removeButton;

    /**
     * The chart type combo box widget
     */
    public ChartDescriptionHandler(final ChartItDataEditorInput editorInput, final Composite bodyComposite,
            final ScrolledPropertiesBlock scrolledPropertiesBlock, final FormToolkit toolkit) {
        this.resourceData = editorInput.getResourceData();
        this.isEnabled = true;

        final Composite extraComposite = toolkit.createComposite(bodyComposite);
        extraComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.verticalSpacing = 10;
        layout.numColumns = 1;
        extraComposite.setLayout(layout);

        final Section section = toolkit
                .createSection(extraComposite, Section.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Chart Description");
        section.setDescription("There are 2 possible data providers compositions :\n"
            + "- A single String[] provider and a single Number[] provider\n"
            + "- A set of number typed providers");
        section.marginWidth = section.marginHeight = 0;
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Fill the section with a composite with a grid layout
        final Composite client = toolkit.createComposite(section, SWT.WRAP);
        section.setClient(client);
        layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.verticalSpacing = 10;
        layout.numColumns = 3;
        client.setLayout(layout);

        // Create all graphical widgets

        // Chart name
        Label label = toolkit.createLabel(client, "Name:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.chartNameTextWidget = toolkit.createText(client, "", SWT.BORDER | SWT.SINGLE);
        this.chartNameTextWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        editorInput.addControlToDisable(this.chartNameTextWidget);

        // Chart type combo box
        label = toolkit.createLabel(client, "Chart Type:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.chartTypeComboWidget = new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.chartTypeComboWidget.setItems(ChartType.names);
        this.chartTypeComboWidget.select(0);
        this.chartTypeComboWidget.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        // Attach a listener to reflect the model
        this.chartTypeComboWidget.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                if (chartModel != null)
                    chartModel.setChartType(ChartType.values()[chartTypeComboWidget.getSelectionIndex()]);
            }
        });
        editorInput.addControlToDisable(this.chartTypeComboWidget);

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
        editorInput.addControlToDisable(this.refreshPeriodSpinnerWidget);

        // List of data providers
        label = toolkit.createLabel(client, "Used Data Providers:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.usedDataProvidersListWidget = new List(client, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        // gridData.widthHint = 80;
        gridData.heightHint = 100;
        this.usedDataProvidersListWidget.setLayoutData(gridData);
        editorInput.addControlToDisable(this.usedDataProvidersListWidget);

        // Additional button to remove used data providers
        this.removeButton = toolkit.createButton(client, "Remove", SWT.PUSH);
        this.removeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
        this.removeButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                if (chartModel == null)
                    return;
                // Impact the model
                chartModel.removeProvidersByNames(usedDataProvidersListWidget.getSelection());
                // Then the widget
                usedDataProvidersListWidget.remove(usedDataProvidersListWidget.getSelectionIndices());
            }
        });
        editorInput.addControlToDisable(this.removeButton);

        // Disable all widgets
        this.setEnabledWidgets(false);

        // Create a section for the charts list
        final List list = this.createChartsSection(editorInput, extraComposite, scrolledPropertiesBlock,
                toolkit);

        this.chartNameTextWidget.addListener(SWT.DefaultSelection, new Listener() {
            public final void handleEvent(final Event e) {
                if (chartModel == null) {
                    return;
                }
                // Chart name
                String chartName = chartNameTextWidget.getText();
                if (chartName.equals("")) {
                    chartName = ChartModel.DEFAULT_CHART_NAME +
                        resourceData.getModelsContainer().getModels().size();
                }
                chartModel.setName(chartName);
                // Reflect the chart list
                list.setItem(list.getSelectionIndex(), chartName);
            }
        });
        allChartsListWidget = list;
    }

    private List createChartsSection(final ChartItDataEditorInput editorInput, final Composite bodyComposite,
            final ScrolledPropertiesBlock scrolledPropertiesBlock, final FormToolkit toolkit) {
        final Section chartsSection = toolkit.createSection(bodyComposite, Section.TITLE_BAR); // TODO : activate description

        // Create a toolbar manager
        final ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        final ToolBar toolbar = toolBarManager.createControl(chartsSection);
        // Add save and load actions
        toolBarManager.add(new SaveChartsConfigAction(editorInput.getModelsContainer()));
        toolBarManager.add(new LoadChartsConfigAction(editorInput.getModelsContainer(), this,
            scrolledPropertiesBlock));
        editorInput.addControlToDisable(toolbar);
        toolBarManager.update(true);
        chartsSection.setTextClient(toolbar);

        chartsSection.setText("Charts");
        chartsSection
                .setDescription("This section contains all created charts, that can be modified and saved.");
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

        final List list = new List(rdsClient, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        // Layout the table
        GridData gd = new GridData(GridData.FILL_BOTH);
        // gd.widthHint = 80;
        gd.heightHint = 100;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        list.setLayoutData(gd);
        list.addListener(SWT.Selection, new Listener() {
            public final void handleEvent(final Event e) {
                final String[] selection = list.getSelection();
                if (selection.length == 1)
                    handleModelByName(selection[0]);
                else
                    emptyAllWidgets();
            }
        });
        editorInput.addControlToDisable(list);

        // A composite to group the buttons
        final Composite buttonComposite = toolkit.createComposite(rdsClient, SWT.WRAP);
        buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        buttonComposite.setLayout(new FillLayout(SWT.VERTICAL));

        // Add the create button
        final Button createButton = toolkit.createButton(buttonComposite, "Create", SWT.PUSH);
        createButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                // Create a chart model with default values
                final ChartModel c = resourceData.getModelsContainer().createNewChartModel();
                // Reflect the ui
                list.add(c.getName());
                list.setSelection(new String[] { c.getName() });
                handleModel(c);
            }
        });
        editorInput.addControlToDisable(createButton);

        // Add the remove button
        final Button removeButton = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                // Impact the model
                for (String name : list.getSelection()) {
                    resourceData.getModelsContainer().removeByName(name);
                }

                // Then impact the widget and disable all widgets
                list.remove(list.getSelectionIndices());
                emptyAllWidgets();
            }
        });
        editorInput.addControlToDisable(removeButton);
        return list;
    }

    /**
     * Show the field of an incoming chart model
     * 
     * @param chartName
     */
    public void handleModelByName(final String chartName) {
        // Find the model by name
        final ChartModel chartModel = this.resourceData.getModelsContainer().getModelByName(chartName);
        this.handleModel(chartModel);
    }

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
     * Empty all widgets ie sets their default values then disables them
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
        // The remove button
        this.removeButton.setEnabled(enabled);
    }

    public void addUsedDataProvider(final IDataProvider dataProvider) {
        if (chartModel == null)
            return;
        // Add to model then reflect the ui if the data provider was added
        if (this.chartModel.addProvider(dataProvider)) {
            this.usedDataProvidersListWidget.add(dataProvider.getName());
        }
    }

    public List getAllChartsListWidget() {
        return allChartsListWidget;
    }
}
