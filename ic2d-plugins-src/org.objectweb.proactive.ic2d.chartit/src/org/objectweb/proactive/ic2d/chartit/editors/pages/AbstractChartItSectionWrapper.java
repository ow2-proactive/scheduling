package org.objectweb.proactive.ic2d.chartit.editors.pages;

import org.objectweb.proactive.ic2d.chartit.data.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.data.ResourceData;
import org.objectweb.proactive.ic2d.chartit.editors.ChartItDataEditorInput;


/**
 * An abstract section wrapper that provides some shortcuts to its underlying classes.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>. 
 */
public abstract class AbstractChartItSectionWrapper {

    /**
     * A reference on the overview page that contains all sections
     */
    protected final OverviewPage overviewPage;

    /**
     * Creates a new instance of this class.
     * 
     * @param overviewPage The overview page that contains all sections
     */
    public AbstractChartItSectionWrapper(final OverviewPage overviewPage) {
        this.overviewPage = overviewPage;
    }

    /**
     * Returns the editor input.
     * 
     * @return The editor input
     */
    public ChartItDataEditorInput getEditorInput() {
        return this.overviewPage.getChartItDataEditorInput();
    }

    /**
     * Returns the resource data.
     * 
     * @return The resource data
     */
    public ResourceData getResourceData() {
        return this.overviewPage.getChartItDataEditorInput().getResourceData();
    }

    /**
     * Returns the resource descriptor
     * 
     * @return The resource descriptor
     */
    public IResourceDescriptor getResourceDescriptor() {
        return this.overviewPage.getChartItDataEditorInput().getResourceData().getResourceDescriptor();
    }
}
