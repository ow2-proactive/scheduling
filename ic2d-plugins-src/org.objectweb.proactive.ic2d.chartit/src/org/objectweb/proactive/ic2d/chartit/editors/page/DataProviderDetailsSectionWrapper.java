package org.objectweb.proactive.ic2d.chartit.editors.page;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


/**
 * This class acts as a wrapper for the data provider details section.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class DataProviderDetailsSectionWrapper extends AbstractChartItSectionWrapper implements
        Runnable {

    /**
     * The generic reference of the associated type model
     */
    protected IDataProvider type;

    /**
     * The text widget used for the description of the attribute
     */
    protected final Text attributeDescriptionText;
    /**
     * The text widget used for the value of the attribute
     */
    protected final Text attributeValueText;

    /**
     * Create a new instance of this class.
     * 
     * @param overviewPage
     *            The overview page that contains all sections
     * @param parent
     *            The parent Composite
     * @param toolkit
     *            The toolkit used to create widgets
     */
    public DataProviderDetailsSectionWrapper(final OverviewPage overviewPage, final Composite parent,
            final FormToolkit toolkit) {
        super(overviewPage);

        // Attribute Details and Charting Section
        final Section section = toolkit.createSection(parent, Section.TWISTIE | ExpandableComposite.COMPACT |
        /*Section.EXPANDED |*/Section.TITLE_BAR);

        final ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        final ToolBar toolbar = toolBarManager.createControl(section);
        // Add save and load actions
        final Action refreshAction = new Action() {
            @Override
            public void run() {
                update();
            }
        };
        refreshAction.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator
                .getDefault().getBundle(), new Path("icons/nav_refresh.gif"), null)));
        refreshAction.setToolTipText("Refresh Value");
        toolBarManager.add(refreshAction);
        toolBarManager.update(true);
        section.setTextClient(toolbar);

        section.marginWidth = section.marginHeight = 0;
        section.setText("Data Provider Details");
        section.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));

        final Composite client = toolkit.createComposite(section);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 10;
        gridLayout.numColumns = 2;
        client.setLayout(gridLayout);
        section.setClient(client);

        // Attribute Description
        Label label = toolkit.createLabel(client, "Description:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.attributeDescriptionText = toolkit.createText(client, "", SWT.SINGLE);
        this.attributeDescriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Attribute Value
        label = toolkit.createLabel(client, "Value:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        this.attributeValueText = toolkit.createText(client, "", SWT.BORDER | SWT.MULTI | SWT.H_SCROLL |
            SWT.V_SCROLL /* | SWT.WRAP */);
        this.attributeValueText.setEditable(false);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 100;
        gridData.widthHint = 100;
        this.attributeValueText.setLayoutData(gridData);
    }

    /**
     * 
     */
    public void update() {
        Display.getDefault().asyncExec(this);
    }

    public void run() {
        if (this.type == null) {
            this.attributeDescriptionText.setText("");
            this.attributeValueText.setText("");
            return;
        }
        // Update the attribute description and value        
        this.attributeDescriptionText.setText(this.type.getDescription());
        this.attributeValueText.setText(this.type.provideValue().toString());
    }
}
