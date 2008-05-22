package org.objectweb.proactive.ic2d.chartit.actions;

import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.ChartType;
import org.objectweb.proactive.ic2d.console.Console;


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
     * The container of chart models
     */
    private final ChartModelContainer chartModelContainer;

    /**
     * 
     * @param chartModelContainer
     */
    public SaveChartsConfigAction(final ChartModelContainer chartModelContainer) {
        this.chartModelContainer = chartModelContainer;
        super.setId(SAVE_CHARTS_CONFIG_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/save_edit.gif"), null)));
        super.setToolTipText(SAVE_CHARTS_CONFIG_ACTION);
        super.setEnabled(true);
    }

    @Override
    public void run() {
        try {
            // If nothing to save just return quietly
            if (this.chartModelContainer.getModels().size() == 0)
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
            encoder.writeObject(this.chartModelContainer.getModels().size());

            // Serialize models into XML
            for (final ChartModel chartModel : this.chartModelContainer.getModels()) {
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

}
