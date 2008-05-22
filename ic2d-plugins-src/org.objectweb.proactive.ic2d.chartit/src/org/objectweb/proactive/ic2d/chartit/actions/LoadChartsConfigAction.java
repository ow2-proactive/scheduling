package org.objectweb.proactive.ic2d.chartit.actions;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.editors.pages.AvailableDataProvidersSectionWrapper;
import org.objectweb.proactive.ic2d.chartit.editors.pages.ChartsSectionWrapper;
import org.objectweb.proactive.ic2d.console.Console;


/**
 * This action allows the user to load a saved previously saved charts configuration.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class LoadChartsConfigAction extends Action {

    /**
     * A name for this action
     */
    public static final String LOAD_CHARTS_CONFIG_ACTION = "Load Configuration";

    /**
     * The container of chart models
     */
    private final ChartModelContainer chartModelContainer;

    /**
     * The chart description handler
     */
    private final ChartsSectionWrapper chartsSW;

    /**
     * The block containing all data providers
     */
    private final AvailableDataProvidersSectionWrapper availableDataProvidersSW;

    /**
     * 
     * @param chartModelContainer
     * @param chartDescriptionHandler
     * @param scrolledPropertiesBlock
     */
    public LoadChartsConfigAction(final ChartModelContainer chartModelContainer,
            final ChartsSectionWrapper chartsSW,
            final AvailableDataProvidersSectionWrapper availableDataProvidersSW) {
        this.chartModelContainer = chartModelContainer;
        this.chartsSW = chartsSW;
        this.availableDataProvidersSW = availableDataProvidersSW;
        super.setId(LOAD_CHARTS_CONFIG_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/fldr_obj.gif"), null)));
        super.setToolTipText(LOAD_CHARTS_CONFIG_ACTION);
        super.setEnabled(true);
    }

    @Override
    public void run() {
        // Load a config from en encoded xml file
        try {
            // Open a file dialog
            final FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
            fileDialog.setText("Load Configuration");
            fileDialog.setFilterExtensions(new String[] { "*.xml" });
            // Get path
            final String path = fileDialog.open();

            final XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(path)));

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
                final String[] providerNames = o.getRuntimeNames();
                for (final String providerName : providerNames) {
                    final IDataProvider provider = this.availableDataProvidersSW
                            .getProviderByName(providerName);
                    if (provider != null) {
                        o.addProvider(provider);
                    } else {
                        Console.getInstance(Activator.CONSOLE_NAME).log(
                                "Cannot find the provider : " + providerName + " for the chart : " +
                                    o.getName());
                    }
                }

                // Add the model to the container
                if (this.chartModelContainer.getModelByName(o.getName()) == null) {
                    this.chartModelContainer.addModel(o);
                }
                modelNames[i] = o.getName();
            }

            this.chartsSW.getAllChartsListWidget().setItems(modelNames);

            decoder.close();

            // Set the old classLoader back for this thread
            Thread.currentThread().setContextClassLoader(oldClassLoader);

        } catch (FileNotFoundException e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Could not load the configuration : " + e.getMessage());
        }
    }
}
