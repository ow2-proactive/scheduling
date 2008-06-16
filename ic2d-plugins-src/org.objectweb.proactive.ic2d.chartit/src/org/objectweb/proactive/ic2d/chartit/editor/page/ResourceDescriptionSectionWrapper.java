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
package org.objectweb.proactive.ic2d.chartit.editor.page;

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
        // Check if the object name is null
        final String objectName = (getResourceDescriptor().getObjectName() == null ? "N/A"
                : getResourceDescriptor().getObjectName().getCanonicalName());
        toolkit.createLabel(rdsClient, objectName);
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
