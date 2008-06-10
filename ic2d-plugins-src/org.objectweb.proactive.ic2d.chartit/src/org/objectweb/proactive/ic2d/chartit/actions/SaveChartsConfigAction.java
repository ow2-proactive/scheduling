package org.objectweb.proactive.ic2d.chartit.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.editor.page.ChartsSectionWrapper;


/**
 * 
 * @author vbodnart
 * 
 */
public final class SaveChartsConfigAction extends Action {

    /**
     * A name for this action
     */
    public static final String SAVE_CHARTS_CONFIG_ACTION = "Save Current Configuration";

    /**
     * The charts section wrapper
     */
    private final ChartsSectionWrapper chartsSW;

    /**
     * 
     * @param chartModelContainer
     */
    public SaveChartsConfigAction(final ChartsSectionWrapper chartsSW) {
        this.chartsSW = chartsSW;
        super.setId(SAVE_CHARTS_CONFIG_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/save_edit.gif"), null)));
        super.setToolTipText(SAVE_CHARTS_CONFIG_ACTION);
        super.setEnabled(true);
    }

    @Override
    public void run() {
        this.chartsSW.saveConfigToXML();
    }
}
