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
package org.objectweb.proactive.ic2d.chartit.editors.pages;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.data.ChartModelValidator;
import org.objectweb.proactive.ic2d.chartit.data.ResourceDataBuilder;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.provider.ProviderDescriptor;
import org.objectweb.proactive.ic2d.chartit.editors.ChartItDataEditorInput;
import org.objectweb.proactive.ic2d.chartit.util.Utils;


/**
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ScrolledPropertiesBlock extends MasterDetailsBlock {

    public static final String SECTION_TEXT = "Available Data Providers";
    public static final String SECTION_DESCRIPTION = "Choose the data providers for charting input. By default the only available data providers are based on attributes of the resource MBean.";

    /**
     * The editor input. 
     */
    private final ChartItDataEditorInput editorInput;

    /**
     * The overview page 
     */
    protected final OverviewPage overviewPage;

    /**
     * A jface viewer that wraps the swt table widget.
     */
    protected TableViewer tableViewer;

    /**
     * Creates a new instance of <code>ScrolledPropertiesBlock</code>
     * 
     * @param input The editor input used by this block 
     */
    public ScrolledPropertiesBlock(final ChartItDataEditorInput input, final OverviewPage overviewPage) {
        this.editorInput = input;
        this.overviewPage = overviewPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.MasterDetailsBlock#createMasterPart(org.eclipse.ui.forms.IManagedForm,
     *      org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createMasterPart(final IManagedForm managedForm, final Composite parent) {
        // Set vertical orientation
        sashForm.setOrientation(SWT.VERTICAL);

        // Set Grid Data for the parent layout
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final FormToolkit toolkit = managedForm.getToolkit();
        // Create the master section
        final Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
        section.setText(SECTION_TEXT);
        section.setDescription(SECTION_DESCRIPTION);
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

        final Table table = toolkit.createTable(client, SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Name Column
        final TableColumn nameColumn = new TableColumn(table, SWT.NONE);
        nameColumn.setText("Name");
        nameColumn.pack();
        nameColumn.setWidth(200);

        // Type Column
        final TableColumn typeColumn = new TableColumn(table, SWT.NONE);
        typeColumn.setText("Type");
        typeColumn.pack();
        typeColumn.setWidth(200);

        // Layout the table
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 20;
        gd.widthHint = 100;
        table.setLayoutData(gd);
        toolkit.paintBordersFor(client);

        // Create the composite and its layout for the buttons area
        final Composite buttonsClient = toolkit.createComposite(client);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        buttonsClient.setLayoutData(gd);
        buttonsClient.setLayout(new FillLayout(SWT.VERTICAL));

        // Create the button for the predefined dialog
        Button b = toolkit.createButton(buttonsClient, "Add Predefined", SWT.PUSH);
        this.editorInput.addControlToDisable(b);
        b.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                handleAdd();
            }
        });

        // Create the button for the predefined dialog
        b = toolkit.createButton(buttonsClient, "Use >>", SWT.PUSH);
        this.editorInput.addControlToDisable(b);
        b.addSelectionListener(new SelectionAdapter() {
            public final void widgetSelected(final SelectionEvent e) {
                final IStructuredSelection ssel = (IStructuredSelection) tableViewer.getSelection();
                Iterator<IDataProvider> it = ssel.iterator();
                while (it.hasNext()) {
                    overviewPage.chartDescriptionHandler.addUsedDataProvider(it.next());
                }
            }
        });

        // Create the section part
        final SectionPart spart = new SectionPart(section);
        managedForm.addPart(spart);

        // Create the jface table viewer
        this.tableViewer = new TableViewer(table);

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
        dialog.setTitle("Add Predefined Data Provider");
        dialog.setMessage("Select a predefined data provider");
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
            final IDataProvider[] dps = new IDataProvider[selectedNames.length];
            int i = 0;
            for (final Object o : selectedNames) {
                final String name = (String) o;
                // Check if the name of the attribute is already used
                if (!this.alreadyInTable(name)) {
                    dps[i++] = ResourceDataBuilder.buildProviderFromName(name, this.editorInput
                            .getResourceData().getResourceDescriptor().getMBeanServerConnection());
                }

            }
            tableViewer.add(dps);
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
            if (name.equals(((IDataProvider) tableItem.getData()).getName())) {
                return true;
            }
        }
        return false;
    }

    public IDataProvider getProviderByName(String name) {
        for (final TableItem tableItem : this.tableViewer.getTable().getItems()) {
            IDataProvider dp = (IDataProvider) tableItem.getData();
            if (name.equals(dp.getName())) {
                return dp;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.MasterDetailsBlock#registerPages(org.eclipse.ui.forms.DetailsPart)
     */
    @Override
    protected void registerPages(final DetailsPart detailsPart) {
        final DetailsPage detailsPage = new DetailsPage((ChartItDataEditorInput) this.editorInput,
            this.tableViewer);
        // Details page
        detailsPart.registerPage(IDataProvider.class, detailsPage);
        // Set a custom page provider
        detailsPart.setPageProvider(new IDetailsPageProvider() {

            public final IDetailsPage getPage(Object key) {
                return detailsPage;
            }

            public final Object getPageKey(Object object) {
                return IDataProvider.class;
            }

        });

    }

    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
    }

    /**
     * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
     */
    final class MasterContentProvider implements IStructuredContentProvider {
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(final Object inputElement) {
            return editorInput.getResourceData().findAndCreateDataProviders();
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
     * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
     * 
     */
    final class MasterLabelProvider extends LabelProvider implements ITableLabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @SuppressWarnings("unchecked")
        public String getColumnText(final Object obj, final int index) {
            final IDataProvider dp = (IDataProvider) obj;
            switch (index) {
                case 0:
                    return dp.getName();
                case 1:
                    return dp.getType();
                default:
                    return "Unknown";
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(final Object obj, final int index) {
            if (index == 0) {
                final IDataProvider dp = (IDataProvider) obj;
                if (Utils.contains(ChartModelValidator.NUMBER_TYPE, dp.getType())) {
                    return ImageDescriptor.createFromURL(
                            FileLocator.find(Activator.getDefault().getBundle(), new Path(
                                "icons/number_type.gif"), null)).createImage();
                    // PlatformUI.getWorkbench().getSharedImages()
                    // .getImage(ISharedImages.IMG_OBJ_ELEMENT);
                } else if (dp.getType().equals("[Ljava.lang.String;")) {
                    return ImageDescriptor.createFromURL(
                            FileLocator.find(Activator.getDefault().getBundle(), new Path(
                                "icons/strarr_type.gif"), null)).createImage();
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
}