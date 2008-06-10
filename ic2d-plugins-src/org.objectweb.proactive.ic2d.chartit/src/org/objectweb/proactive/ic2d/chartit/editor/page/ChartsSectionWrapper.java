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

import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.actions.LoadChartsConfigAction;
import org.objectweb.proactive.ic2d.chartit.actions.SafeSaveDialog;
import org.objectweb.proactive.ic2d.chartit.actions.SaveChartsConfigAction;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.ChartType;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.console.Console;


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
        toolBarManager.add(new SaveChartsConfigAction(this));
        toolBarManager.add(new LoadChartsConfigAction(this));
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

    /**
     * Saves the current charts configuration into a file.
     */
    public void saveConfigToXML() {
        try {
            final java.util.List<ChartModel> models = super.getResourceData().getModelsContainer()
                    .getModels();
            // If nothing to save just return quietly
            if (models.size() == 0)
                return;

            // Once the data has been dumped to an xml file ask user for report
            // output path
            final SafeSaveDialog safeSaveDialog = new SafeSaveDialog(Display.getDefault().getActiveShell());
            safeSaveDialog.setText("Save Configuration");
            final String path = safeSaveDialog.open();

            // If incorrect path just return quietly
            if (path == null || path.equals(""))
                return;

            // Create the encoder
            final XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(path)));

            // Set a persistence delegate to make enum encodable
            encoder.setPersistenceDelegate(ChartType.class, new ChartTypePersistenceDelegate());

            // Prepare a bean info to specify all serialized fields
            final BeanInfo info = Introspector.getBeanInfo(ChartModel.class);
            for (final PropertyDescriptor pd : info.getPropertyDescriptors()) {
                // if non transient property continue else set transient
                if (pd.getName().equals("name") || pd.getName().equals("chartType") ||
                    pd.getName().equals("refreshPeriod") || pd.getName().equals("runtimeNames")) {
                    continue;
                } else {
                    pd.setValue("transient", Boolean.TRUE);
                }
            }

            // First encode the number of models
            encoder.writeObject(models.size());

            // Serialize models into XML
            for (final ChartModel chartModel : models) {
                // Fill runtime names
                chartModel.fillRuntimeNames();
                // Encode each model
                encoder.writeObject(chartModel);
            }

            encoder.flush();
            encoder.close();
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Could not save the current configuration : " + e.getMessage());
        }
    }

    /**
     * Used to handle enums see java sun 1.5 Bug ID: 5015403 (fixed in java 1.6)
     * 
     * @author vbodnart
     */
    public static final class ChartTypePersistenceDelegate extends DefaultPersistenceDelegate {
        static final String METHOD_NAME = "valueOf";

        protected Expression instantiate(final Object oldInstance, final Encoder out) {
            final ChartType e = (ChartType) oldInstance;
            return new Expression(ChartType.class, METHOD_NAME, new Object[] { e.getDeclaringClass(),
                    e.name() });
        }

        protected boolean mutatesTo(final Object oldInstance, final Object newInstance) {
            return oldInstance == newInstance;
        }
    }

    /**
     * Loads a charts configuration from a file.
     * 
     * @param configFilename
     *            The name of the file containing the charts configuration
     */
    public void loadConfigFromXML(final String configFilename) {
        // Load a config from encoded xml file
        try {
            final XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(
                configFilename)));

            // Fixing a ClassNotFoundException due to bad class loader Bug ID:
            // 4993777
            final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            // First get the total number of models to decode
            final int numberOfModels = (Integer) decoder.readObject();

            final String[] modelNames = new String[numberOfModels];
            for (int i = 0; i < numberOfModels; i++) {
                // Decode a chart model
                final ChartModel o = (ChartModel) decoder.readObject();

                // Retrieve all its providers by names				
                for (final String providerName : o.getRuntimeNames()) {
                    final IDataProvider provider = super.overviewPage.availableDataProvidersSW
                            .getProviderByName(providerName);
                    if (provider != null) {
                        o.addProvider(provider);
                    } else {
                        Console.getInstance(Activator.CONSOLE_NAME).log(
                                "Cannot find the provider " + providerName + " for the chart " + o.getName());
                    }
                }

                // Add the model to the container
                if (super.getResourceData().getModelsContainer().getModelByName(o.getName()) == null) {
                    super.getResourceData().getModelsContainer().addModel(o);
                }
                modelNames[i] = o.getName();
            }

            this.allChartsListWidget.setItems(modelNames);

            decoder.close();

            // Set the old classLoader back for this thread
            Thread.currentThread().setContextClassLoader(oldClassLoader);

        } catch (FileNotFoundException e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Could not load the configuration : " + e.getMessage());
        }
    }
}
