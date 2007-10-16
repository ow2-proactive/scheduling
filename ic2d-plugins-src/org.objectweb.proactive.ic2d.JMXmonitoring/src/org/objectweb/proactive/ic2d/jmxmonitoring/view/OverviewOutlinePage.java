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
 *  Contributor(s):
 *
 * ################################################################
 */
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
