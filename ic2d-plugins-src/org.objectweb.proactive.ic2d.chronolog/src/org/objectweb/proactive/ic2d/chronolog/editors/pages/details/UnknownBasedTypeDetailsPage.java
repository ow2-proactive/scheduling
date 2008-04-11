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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chronolog.editors.pages.details;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.objectweb.proactive.ic2d.chronolog.data.model.UnknownBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;


/**
 * A standard details page used for unknown types
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class UnknownBasedTypeDetailsPage extends AbstractDetailsPage<UnknownBasedTypeModel> {

    /**
     * @param editorInput
     * @param tableViewer
     */
    public UnknownBasedTypeDetailsPage(final ChronologDataEditorInput editorInput,
            final TableViewer tableViewer) {
        super(editorInput, tableViewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.editors.pages.details.AbstractDetailsPage#createInternalContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public FormToolkit createInternalContents(final Composite parent) {
        final FormToolkit toolkit = super.createInternalContents(parent);
        return toolkit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IDetailsPage#inputChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void selectionChanged(final IFormPart part, final ISelection selection) {
        final IStructuredSelection ssel = (IStructuredSelection) selection;
        if (ssel.size() == 1) {
            super.type = (UnknownBasedTypeModel) ssel.getFirstElement();
            this.update(); // see the run method
        } else {
            this.type = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.editors.pages.details.AbstractDetailsPage#run()
     */
    @Override
    public void run() {
        // Update the attribute description and value
        super.attributeDescriptionText.setText(super.type.getDataProvider().getDescription());
        super.attributeValueText.setText(super.type.getProvidedValue().toString());
    }
}
