/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;


/**
 * @author The ProActive Team
 */
public class NodeInfoView extends ViewPart {

    /** the view part id */
    public static final String ID = "org.ow2.proactive.resourcemanager.gui.views.NodeInfoView";

    private static NodeInfoView instance = null;

    private static final int textFlags = SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY;

    private Text text = null;
    private Text textWrap = null;
    private boolean wrap = false;

    /**
     * The constructor.
     */
    public NodeInfoView() {
        instance = this;
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(final Composite parent) {

        GridLayout g = new GridLayout(1, false);
        parent.setLayout(g);

        final GridData hidden = new GridData();
        hidden.exclude = true;

        final GridData expand = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);

        // create two Text fields, one with wrap enabled, and switch the visibility
        // of each one when checking 'wrap lines'
        // styles can not be dynamically changed (SWT < Swing)

        text = new Text(parent, textFlags);
        text.setLayoutData(expand);

        textWrap = new Text(parent, textFlags | SWT.WRAP);
        textWrap.setVisible(false);
        textWrap.setLayoutData(hidden);

        Button check = new Button(parent, SWT.CHECK);
        GridData tc = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
        check.setLayoutData(tc);

        check.setText("Wrap lines");
        check.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                wrap = !wrap;

                if (wrap) {
                    text.setVisible(false);
                    text.setLayoutData(hidden);

                    textWrap.setVisible(true);
                    textWrap.setLayoutData(expand);

                } else {
                    text.setVisible(true);
                    text.setLayoutData(expand);

                    textWrap.setVisible(false);
                    textWrap.setLayoutData(hidden);
                }

                parent.layout();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

    }

    private void updateText(String text) {
        this.text.setText(text);
        this.textWrap.setText(text);
    }

    public static void setNode(Node node) {
        if (instance != null) {
            instance.updateText(node.getDescription());
        }
    }

    /**
     * Passing the focus request to the viewer's control.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    }
}
