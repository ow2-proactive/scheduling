package org.objectweb.proactive.ic2d.chronolog.editparts;

import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;


public interface IChronologEditPart<M extends AbstractTypeModel<?>> extends PropertyChangeListener, Runnable {

    /**
     * Returns the model
     * @return
     */
    public M getModel();

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
