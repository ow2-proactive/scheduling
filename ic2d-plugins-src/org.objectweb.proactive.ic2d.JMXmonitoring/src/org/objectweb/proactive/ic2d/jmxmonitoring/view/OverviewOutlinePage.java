package org.objectweb.proactive.ic2d.jmxmonitoring.view;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


public class OverviewOutlinePage extends Page implements IContentOutlinePage {

    /** the control of the overview */
    private Canvas overview;

    /** the root edit part */
    private ScalableFreeformRootEditPart rootEditPart;

    /** the thumbnail */
    private Thumbnail thumbnail;

    /**
     * Creates a new OverviewOutlinePage instance.
     * @param rootEditPart the root edit part to show the overview from
     */
    public OverviewOutlinePage(ScalableFreeformRootEditPart rootEditPart) {
        super();
        this.rootEditPart = rootEditPart;
    }

    @Override
    public void createControl(Composite parent) {
        // create canvas and lws
        overview = new Canvas(parent, SWT.NONE);
        LightweightSystem lws = new LightweightSystem(overview);

        // create thumbnail
        thumbnail = new ScrollableThumbnail((Viewport) rootEditPart.getFigure());
        thumbnail.setBorder(new MarginBorder(3));
        thumbnail.setSource(rootEditPart.getLayer(
                LayerConstants.PRINTABLE_LAYERS));
        lws.setContents(thumbnail);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#dispose()
     */
    public void dispose() {
        if (null != thumbnail) {
            thumbnail.deactivate();
        }

        super.dispose();
    }

    @Override
    public Control getControl() {
        return overview;
    }

    @Override
    public void setFocus() {
        if (getControl() != null) {
            getControl().setFocus();
        }
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        // TODO Auto-generated method stub
    }

    public ISelection getSelection() {
        return StructuredSelection.EMPTY;
    }

    public void removeSelectionChangedListener(
        ISelectionChangedListener listener) {
        // TODO Auto-generated method stub
    }

    public void setSelection(ISelection selection) {
        // TODO Auto-generated method stub
    }
}
