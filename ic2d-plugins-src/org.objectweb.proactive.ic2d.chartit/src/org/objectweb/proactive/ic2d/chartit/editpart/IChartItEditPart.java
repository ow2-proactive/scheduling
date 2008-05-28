package org.objectweb.proactive.ic2d.chartit.editpart;

import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.IChartModelListener;


public interface IChartItEditPart extends IChartModelListener, Runnable {

    /**
     * Returns the model
     * @return
     */
    public ChartModel getModel();

    /**
     * For example subclasses may use GEF based elements (if so direct sub class must extends SimpleRootEditPart),
     * override this method like :
     * <code>
     *   final Canvas c = new Canvas(client, SWT.FILL);
     *   final GraphicalViewerImpl gv = new GraphicalViewerImpl();
     *   gv.setControl(c);       
     *   gv.setRootEditPart((SimpleRootEditPart)ep);
     * </code>
     */
    public void fillSWTCompositeClient(final Composite client, final int style);

    public void activate();

    public void deactivate();
}
