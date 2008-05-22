package org.objectweb.proactive.ic2d.chartit.editors.pages;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;


/**
 * This class acts as a wrapper for the resource description section.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ResourceDescriptionSectionWrapper extends AbstractChartItSectionWrapper {

    /**
     * Create a new instance of this class.
     * 
     * @param overviewPage The overview page that contains all sections
     * @param parent The parent Composite
     * @param toolkit The toolkit used to create widgets
     */
    public ResourceDescriptionSectionWrapper(final OverviewPage overviewPage, final Composite parent,
            final FormToolkit toolkit) {
        super(overviewPage);

        final Section resourceDescriptionSection = toolkit.createSection(parent, Section.TITLE_BAR |
            Section.TWISTIE | Section.EXPANDED);
        resourceDescriptionSection.setText("Resource Description");
        resourceDescriptionSection.marginWidth = 0;
        resourceDescriptionSection.marginHeight = 0;
        resourceDescriptionSection.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

        // Fill the section with a composite with a grid layout
        final Composite rdsClient = toolkit.createComposite(resourceDescriptionSection, SWT.WRAP);
        final GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 5;
        layout.verticalSpacing = 10;
        layout.numColumns = 2;
        rdsClient.setLayout(layout);
        resourceDescriptionSection.setClient(rdsClient);
        // Ressource name
        Label l = toolkit.createLabel(rdsClient, "Name:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(rdsClient, getResourceDescriptor().getName());
        // Ressource JMX ObjectName
        l = toolkit.createLabel(rdsClient, "JMX ObjectName:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(rdsClient, getResourceDescriptor().getObjectName().getCanonicalName());
        // Ressource location
        l = toolkit.createLabel(rdsClient, "URL:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(rdsClient, getResourceDescriptor().getHostUrlServer());
        // Monitored since
        l = toolkit.createLabel(rdsClient, "Monitored Since:");
        l.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        toolkit.createLabel(rdsClient, new Date().toString());
    }
}
