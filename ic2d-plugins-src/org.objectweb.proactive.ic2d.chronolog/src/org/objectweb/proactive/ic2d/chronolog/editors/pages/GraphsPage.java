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
package org.objectweb.proactive.ic2d.chronolog.editors.pages;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chronolog.Activator;
import org.objectweb.proactive.ic2d.chronolog.data.ModelsCollector;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.GroupedNumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.StringArrayBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditor;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;
import org.objectweb.proactive.ic2d.chronolog.editparts.ChronologicalEditPart;
import org.objectweb.proactive.ic2d.chronolog.editparts.GroupedEditPart;
import org.objectweb.proactive.ic2d.chronolog.editparts.IChronologEditPart;
import org.objectweb.proactive.ic2d.chronolog.editparts.SeriesEditPart;


/**
 * This class represents a page with some graphs/charts contained in sections.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class GraphsPage extends FormPage {
    /**
     * A boolean variable used to know if the underlying form must be re-filed with sections 
     */
    protected boolean cleared;

    /**
     * Creates a new instance of <code>GraphsPage</code>.
     * 
     * @param editor The parent editor
     */
    public GraphsPage(final ChronologDataEditor editor) {
        super(editor, "Graphs", "Graphs");
        this.cleared = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        // Create the form and name it
        final ScrolledForm form = managedForm.getForm();
        final FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading(form.getForm());
        form.setText("Graphs");

        // Add a clear action to be able to remove all sections in that page
        final Action clearAction = new Action("Clear", Action.AS_RADIO_BUTTON) {
            public final void run() {
                // Dispose all children
                for (final Control control : form.getBody().getChildren()) {
                    if (!control.isDisposed()) {
                        control.dispose();
                    }
                }
                // Set this page cleared
                cleared = true;
                // Close data store
                ((ChronologDataEditorInput) getEditorInput()).getCollector().stopCollector();
                // Re-enable all controls from editor input
                ((ChronologDataEditorInput) getEditorInput()).setEnabledControls(true);
                // Reflow the form to fire all modifications
                form.reflow(true);
                this.setChecked(false);
            }
        };
        clearAction.setToolTipText("Clear");
        clearAction.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/clear.gif"), null)));
        // Add action to toolbar manager
        form.getToolBarManager().add(clearAction);
        form.getToolBarManager().update(true);

        final ColumnLayout layout = new ColumnLayout();
        layout.topMargin = 5;
        layout.bottomMargin = 5;
        layout.leftMargin = 10;
        layout.rightMargin = 10;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 10;
        layout.maxNumColumns = 4;
        layout.minNumColumns = 1;
        form.getBody().setLayout(layout);
        // On creation we need to fill the form
        this.cleared = true;
        this.fillForm();
    }

    /**
     * Fills the form with as many sections as contained elements in the data
     * store.
     * <p>
     * Use this method carefully !
     */
    protected void fillForm() {
        if (this.cleared) {
            // Create as many sections as contained elements in the models collector
            final ModelsCollector ae = ((ChronologDataEditorInput) this.getEditorInput()).getCollector();
            for (final AbstractTypeModel<?> ap : ae.getModels()) {
                Composite client = this.createSectionForModel(ap);

                // Create the correct edit part from the model                
                IChronologEditPart<? extends AbstractTypeModel<?>> ep = null;
                // Create the corresponding edit part from the model 
                if (ap instanceof NumberBasedTypeModel) {
                    ep = new ChronologicalEditPart((NumberBasedTypeModel) ap);
                } else if (ap instanceof StringArrayBasedTypeModel) {
                    ep = new SeriesEditPart((StringArrayBasedTypeModel) ap);
                } else if (ap instanceof GroupedNumberBasedTypeModel) {
                    ep = new GroupedEditPart((GroupedNumberBasedTypeModel) ap);
                }
                ep.fillSWTCompositeClient(client, SWT.FILL);

            }
            this.cleared = false;
            super.getManagedForm().reflow(true);
        }
    }

    /**
     * Creates a section with everything needed to create an edit part from a
     * specific model.
     * 
     * @param abstractTypeModel
     *            The model used to build an edit part
     */
    private Composite createSectionForModel(final AbstractTypeModel<?> abstractTypeModel) {

        // Create the section
        final ScrolledForm form = super.getManagedForm().getForm();
        final FormToolkit toolkit = super.getManagedForm().getToolkit();
        final Section section = toolkit.createSection(form.getBody(), Section.TWISTIE | Section.TITLE_BAR |
            Section.DESCRIPTION | Section.EXPANDED);
        section.setDescription(abstractTypeModel.getName());
        section.setText(abstractTypeModel.getDescription());

        final Composite client = toolkit.createComposite(section);
        client.setBackground(ColorConstants.white); // optional
        section.setClient(client);

        final GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 5;
        layout.numColumns = 1;
        client.setLayout(layout);

        return client;
    }
}