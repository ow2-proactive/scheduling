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

import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chronolog.Activator;
import org.objectweb.proactive.ic2d.chronolog.data.ResourceDataBuilder;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.StringArrayBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.UnknownBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chronolog.data.provider.ProviderDescriptor;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;
import org.objectweb.proactive.ic2d.chronolog.editors.pages.details.NumberBasedTypeDetailsPage;
import org.objectweb.proactive.ic2d.chronolog.editors.pages.details.StringArrayBasedTypeDetailsPage;
import org.objectweb.proactive.ic2d.chronolog.editors.pages.details.UnknownBasedTypeDetailsPage;


/**
 * @author The ProActive Team
 */
public final class ScrolledPropertiesBlock extends MasterDetailsBlock {
    /**
     * 
     */
    public static final String NAME_COLUMN = "Name";
    /**
     * 
     */
    public static final String TYPE_COLUMN = "Type";
    /**
     * 
     */
    public static final String USED_COLUMN = "Used";
    /**
     * 
     */
    private final ChronologDataEditorInput editorInput;
    /**
     * 
     */
    protected TableViewer tableViewer;

    /**
     * @param input
     */
    public ScrolledPropertiesBlock(final ChronologDataEditorInput input) {
        this.editorInput = input;
    }

    /**
     * @author The ProActive Team
     */
    final class MasterContentProvider implements IStructuredContentProvider {
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(final Object inputElement) {
            return editorInput.getRessourceData().findAndCreateElementModels();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    /**
     * @author The ProActive Team
     * 
     */
    final class MasterLabelProvider extends LabelProvider implements ITableLabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        @SuppressWarnings("unchecked")
        public String getColumnText(final Object obj, final int index) {
            final AbstractTypeModel val = (AbstractTypeModel) obj;
            switch (index) {
                case 0:
                    return val.getDataProvider().getName();
                case 1:
                    return val.getDataProvider().getType();
                case 2:
                    return "" + val.isUsed();
                default:
                    return "Unknown";
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(final Object obj, final int index) {
            if (index == 0) {
                if (obj instanceof StringArrayBasedTypeModel) {
                    return ImageDescriptor.createFromURL(
                            FileLocator.find(Activator.getDefault().getBundle(), new Path(
                                "icons/strarr_type.gif"), null)).createImage();
                    // PlatformUI.getWorkbench().getSharedImages()
                    // .getImage(ISharedImages.IMG_OBJ_ELEMENT);
                } else if (obj instanceof NumberBasedTypeModel) {
                    return ImageDescriptor.createFromURL(
                            FileLocator.find(Activator.getDefault().getBundle(), new Path(
                                "icons/number_type.gif"), null)).createImage();
                    // PlatformUI.getWorkbench().getSharedImages()
                    // .getImage(ISharedImages.IMG_OBJ_FILE);
                } else {
                    return ImageDescriptor.createFromURL(
                            FileLocator.find(Activator.getDefault().getBundle(), new Path(
                                "icons/unknown_type.gif"), null)).createImage();
                    // PlatformUI.getWorkbench().getSharedImages()
                    // .getImage(ISharedImages.IMG_OBJ_FOLDER);
                }
            } else {
                return null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.MasterDetailsBlock#createMasterPart(org.eclipse.ui.forms.IManagedForm,
     *      org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createMasterPart(final IManagedForm managedForm, final Composite parent) {
        final FormToolkit toolkit = managedForm.getToolkit();
        // Create the master section
        final Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
        section.setText("Available MBean Attributes");
        section.setDescription("Choose attributes for charting input.");
        section.marginWidth = 10;
        section.marginHeight = 5;

        // Create a client for the table in the master section
        final Composite client = toolkit.createComposite(section, SWT.WRAP);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout(layout);
        section.setClient(client);

        final Table t = toolkit.createTable(client, SWT.NULL);
        t.setHeaderVisible(true);
        t.setLinesVisible(true);

        // Name Column
        final TableColumn nameColumn = new TableColumn(t, SWT.LEFT);
        nameColumn.setText(NAME_COLUMN);
        nameColumn.pack();
        nameColumn.setWidth(200);

        // Type Column
        final TableColumn typeColumn = new TableColumn(t, SWT.LEFT);
        typeColumn.setText(TYPE_COLUMN);
        typeColumn.pack();
        typeColumn.setWidth(400);

        // Used column
        final TableColumn usedColumn = new TableColumn(t, SWT.RIGHT);
        usedColumn.setText(USED_COLUMN);
        usedColumn.pack();
        usedColumn.setWidth(10);

        // Layout the table
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 20;
        gd.widthHint = 100;
        t.setLayoutData(gd);
        toolkit.paintBordersFor(client);

        // Create the composite and its layout for the buttons area
        final Composite buttonsClient = toolkit.createComposite(client);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        buttonsClient.setLayoutData(gd);
        buttonsClient.setLayout(new FillLayout(SWT.VERTICAL));

        // Create the button for the predefined dialog
        final Button b = toolkit.createButton(buttonsClient, "Add Predefined", SWT.PUSH);
        this.editorInput.addControlToDisable(b);
        b.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                handleAdd();
            }
        });

        // Create the button for the remove action
        final Button removeButton = toolkit.createButton(buttonsClient, "Remove", SWT.PUSH);
        this.editorInput.addControlToDisable(removeButton);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                if (tableViewer != null) {
                    final IStructuredSelection ssel = (IStructuredSelection) tableViewer.getSelection();
                    if (ssel.size() > 0) {
                        // When removing the model first check if its used and
                        // remove from
                        // data store then remove from table
                        final AbstractTypeModel model = (AbstractTypeModel) ssel.getFirstElement();
                        if (model.isUsed()) {
                            model.removeFromRessource();
                        }
                        tableViewer.remove(model);
                    }
                }
            }
        });

        // Create the section part
        final SectionPart spart = new SectionPart(section);
        managedForm.addPart(spart);

        // Create the jface table viewer
        this.tableViewer = new TableViewer(t);
        this.tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public final void selectionChanged(final SelectionChangedEvent event) {
                managedForm.fireSelectionChanged(spart, event.getSelection());
            }
        });
        this.tableViewer.setContentProvider(new MasterContentProvider());
        this.tableViewer.setLabelProvider(new MasterLabelProvider());
        this.tableViewer.setInput(editorInput);
    }

    /**
     * Handles the add predefined dialog
     */
    private void handleAdd() {
        final ElementListSelectionDialog dialog = new ElementListSelectionDialog(this.sashForm.getDisplay()
                .getActiveShell(), new MasterLabelProvider());
        dialog.setTitle("Add Predefined Attribute");
        dialog.setMessage("Select a predefined attribute");
        dialog.setMultipleSelection(true);

        // Available elements must be the difference between all available names
        // and all already known by the table
        final ProviderDescriptor[] allPDs = ProviderDescriptor.values();
        final ArrayList<String> elts = new ArrayList<String>(allPDs.length);
        for (final ProviderDescriptor p : allPDs) {
            // Check if the name of the attribute is already used in the
            // table
            if (!this.alreadyInTable(p.getName())) {
                elts.add(p.getName());
            }
        }
        dialog.setElements(elts.toArray());

        // Open the dialog
        if (dialog.open() == Window.OK) {
            final Object[] selectedNames = dialog.getResult();
            final AbstractTypeModel[] models = new AbstractTypeModel[selectedNames.length];
            int i = 0;
            for (final Object o : selectedNames) {
                final String name = (String) o;
                // Check if the name of the attribute is already used
                if (!this.alreadyInTable(name)) {
                    final IDataProvider provider = ResourceDataBuilder.buildProviderFromName(name,
                            this.editorInput.getRessourceData().getRessourceDescriptor()
                                    .getMBeanServerConnection());
                    models[i++] = editorInput.getRessourceData().buildTypeModelFromProvider(provider);
                }

            }
            tableViewer.add(models);
        }
    }

    /**
     * Check if a name is already in the table
     * 
     * @param name
     *            The name is already is in the table
     * @return <code>true</code> if the name is contained in the table
     */
    private boolean alreadyInTable(final String name) {
        for (final TableItem tableItem : this.tableViewer.getTable().getItems()) {
            if (name.equals(((AbstractTypeModel) tableItem.getData()).getDataProvider().getName())) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.MasterDetailsBlock#createToolBarActions(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createToolBarActions(final IManagedForm managedForm) {
        final ScrolledForm form = managedForm.getForm();
        final Action haction = new Action("hor", Action.AS_RADIO_BUTTON) {
            public final void run() {
                sashForm.setOrientation(SWT.HORIZONTAL);
                form.reflow(true);
            }
        };
        haction.setChecked(true);
        haction.setToolTipText("Horizontal orientation");
        haction.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/th_horizontal.gif"), null)));
        final Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) {
            public final void run() {
                sashForm.setOrientation(SWT.VERTICAL);
                form.reflow(true);
            }
        };
        vaction.setChecked(false);
        vaction.setToolTipText("Vertical orientation");
        vaction.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/th_vertical.gif"), null)));
        form.getToolBarManager().add(haction);
        form.getToolBarManager().add(vaction);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.MasterDetailsBlock#registerPages(org.eclipse.ui.forms.DetailsPart)
     */
    @Override
    protected void registerPages(final DetailsPart detailsPart) {
        detailsPart.registerPage(NumberBasedTypeModel.class, new NumberBasedTypeDetailsPage(
            (ChronologDataEditorInput) this.editorInput, this.tableViewer));
        detailsPart.registerPage(StringArrayBasedTypeModel.class, new StringArrayBasedTypeDetailsPage(
            (ChronologDataEditorInput) this.editorInput, this.tableViewer));
        detailsPart.registerPage(UnknownBasedTypeModel.class, new UnknownBasedTypeDetailsPage(
            (ChronologDataEditorInput) this.editorInput, this.tableViewer));
    }
}