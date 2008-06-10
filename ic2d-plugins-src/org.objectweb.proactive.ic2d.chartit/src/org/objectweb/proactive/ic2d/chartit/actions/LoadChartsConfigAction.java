package org.objectweb.proactive.ic2d.chartit.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.editor.page.ChartsSectionWrapper;


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
     * The chart description handler
     */
    private final ChartsSectionWrapper chartsSW;

    /**
     * 
     * @param chartsSW The charts section wrapper
     */
    public LoadChartsConfigAction(final ChartsSectionWrapper chartsSW) {
        this.chartsSW = chartsSW;
        super.setId(LOAD_CHARTS_CONFIG_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/fldr_obj.gif"), null)));
        super.setToolTipText(LOAD_CHARTS_CONFIG_ACTION);
        super.setEnabled(true);
    }

    @Override
    public void run() {
        // Open a file dialog
        final FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
        fileDialog.setText("Load Configuration");
        fileDialog.setFilterExtensions(new String[] { "*.xml" });
        // Get path
        final String path = fileDialog.open();
        this.chartsSW.loadConfigFromXML(path);
    }
}
