package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.objectweb.proactive.ic2d.chartit.actions.AbstractWorkbenchChartItAction;
import org.objectweb.proactive.ic2d.chartit.data.resource.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.data.resource.ResourceDataBuilder;


/**
 * A concrete implementation of {@link org.objectweb.proactive.ic2d.chartit.actions.AbstractWorkbenchChartItAction} class
 * used to monitor JMXMonitoring plugin it self.
 * <p>
 * This action plugs into the general top level workbench panel (near File, Edit, Help).
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public class WorkbenchChartItAction extends AbstractWorkbenchChartItAction {

    @Override
    public IResourceDescriptor createResourceDescriptor() {
        // Here in the future vbodnart or other may submit a custom descriptor based on internal metrics
        // of JMXMonitoring plugin (for example nbEvent/s or nbMonitored entities ... )
        // For the moment only a default resource descriptor is used, it's based on a local RuntimeMXBean (provided by ChartIt plugin)		
        return ResourceDataBuilder.createResourceDescriptorForLocalRuntime();
    }

}
