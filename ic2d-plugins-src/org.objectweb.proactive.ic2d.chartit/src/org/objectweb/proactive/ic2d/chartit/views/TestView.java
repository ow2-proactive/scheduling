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
package org.objectweb.proactive.ic2d.chartit.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.chartit.actions.CurrentJVMChartItAction;


/**
 * For test purpose.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class TestView extends ViewPart {

    public static final String ID = "org.objectweb.proactive.ic2d.chronolog.views.TestView";

    private FormToolkit toolkit;
    private Form form;

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {

        this.toolkit = new FormToolkit(parent.getDisplay());
        // Create and decorate form1
        this.form = toolkit.createForm(parent);
        this.form.setText("Welcome to Chronolog !");
        this.toolkit.decorateFormHeading(form);
        this.form.getToolBarManager().add(new CurrentJVMChartItAction());
        this.form.getToolBarManager().update(true);

        // Create and customize the ColumnLayout
        ColumnLayout layout = new ColumnLayout();
        layout.topMargin = 0;
        layout.bottomMargin = 5;
        layout.leftMargin = 10;
        layout.rightMargin = 10;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 10;
        layout.maxNumColumns = 4;
        layout.minNumColumns = 1;
        form.getBody().setLayout(layout);
    }

    @Override
    public void setFocus() {
        this.form.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        this.toolkit.dispose();
        super.dispose();
    }
}