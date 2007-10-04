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
package org.objectweb.proactive.infrastructuremanager.views;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.infrastructuremanager.IMConstants;
import org.objectweb.proactive.infrastructuremanager.figures.HostFigure;
import org.objectweb.proactive.infrastructuremanager.figures.IMFigureNode;
import org.objectweb.proactive.infrastructuremanager.figures.VMFigure;


public class IMViewLegend extends ViewPart {
    public static final String ID = "org.objectweb.proactive.infrastructuremanager.gui.views.IMViewLegend";

    @Override
    public void createPartControl(Composite p) {
        RowLayout rlh = new RowLayout(SWT.HORIZONTAL);
        rlh.justify = true;

        GridData line = new GridData();
        line.horizontalAlignment = GridData.FILL;
        line.grabExcessHorizontalSpace = true;

        GridData listSize = new GridData();
        listSize.horizontalAlignment = GridData.FILL;
        listSize.verticalAlignment = GridData.FILL;
        listSize.grabExcessHorizontalSpace = true;
        listSize.grabExcessVerticalSpace = true;

        FormLayout generalLayout = new FormLayout();
        generalLayout.marginHeight = 5;
        generalLayout.marginWidth = 5;

        p.setLayout(new FillLayout());
        ScrolledComposite sc = new ScrolledComposite(p,
                SWT.H_SCROLL | SWT.V_SCROLL);
        Composite child = new Composite(sc, SWT.NONE);
        child.setLayout(generalLayout);

        //--------- Hosts ---------//
        Group hostDef = new Group(child, 0);
        GridLayout hostLayout = new GridLayout();
        hostLayout.numColumns = 2;
        hostDef.setLayout(hostLayout);
        hostDef.setText("Hosts");
        FormData hostDefFormData = new FormData();
        hostDefFormData.top = new FormAttachment(0, 0);
        hostDefFormData.left = new FormAttachment(0, 0);
        hostDefFormData.right = new FormAttachment(100, 0);
        hostDef.setLayoutData(hostDefFormData);
        // Standard Host
        FigureCanvas hostContainer = new FigureCanvas(hostDef);
        hostContainer.setContents(new HostFigure());
        Label hostText = new Label(hostDef, 0);
        hostText.setText("Standard Host");

        //--------- JVMs ---------//
        Group jvmDef = new Group(child, 0);
        GridLayout jvmLayout = new GridLayout();
        jvmLayout.numColumns = 2;
        jvmLayout.verticalSpacing = 0;
        jvmDef.setLayout(jvmLayout);
        jvmDef.setText("JVMs");
        FormData jvmDefFormData = new FormData();
        jvmDefFormData.top = new FormAttachment(hostDef, 0);
        jvmDefFormData.left = new FormAttachment(0, 0);
        jvmDefFormData.right = new FormAttachment(100, 0);
        jvmDef.setLayoutData(jvmDefFormData);
        // Standard JVM
        FigureCanvas jvm1Container = new FigureCanvas(jvmDef);
        jvm1Container.setContents(new VMFigure());
        Label jvm1Text = new Label(jvmDef, 0);
        jvm1Text.setText("Standard JVM");

        //--------- Nodes ---------//
        Group nodeDef = new Group(child, 0);
        GridLayout nodeLayout = new GridLayout();
        nodeLayout.numColumns = 2;
        nodeLayout.verticalSpacing = 0;
        nodeDef.setLayout(nodeLayout);
        nodeDef.setText("Nodes");
        FormData nodeDefFormData = new FormData();
        nodeDefFormData.top = new FormAttachment(jvmDef, 0);
        nodeDefFormData.left = new FormAttachment(0, 0);
        nodeDefFormData.right = new FormAttachment(100, 0);
        nodeDef.setLayoutData(nodeDefFormData);
        // Available Node
        FigureCanvas node1Container = new FigureCanvas(nodeDef);
        node1Container.setContents(new IMFigureNode(
                IMConstants.STATUS_AVAILABLE));
        Label node1Text = new Label(nodeDef, 0);
        node1Text.setText("Available Node");
        // Busy Node
        FigureCanvas node2Container = new FigureCanvas(nodeDef);
        node2Container.setContents(new IMFigureNode(IMConstants.STATUS_BUSY));
        Label node2Text = new Label(nodeDef, 0);
        node2Text.setText("Busy Node");
        // Down Node
        FigureCanvas node3Container = new FigureCanvas(nodeDef);
        node3Container.setContents(new IMFigureNode(IMConstants.STATUS_DOWN));
        Label node3Text = new Label(nodeDef, 0);
        node3Text.setText("Down Node");

        sc.setContent(child);
        child.setSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        sc.setMinSize(child.getSize().x, child.getSize().y);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
    }

    @Override
    public void setFocus() {
    }
}
