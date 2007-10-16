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
package org.objectweb.proactive.ic2d.jmxmonitoring.view;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.State;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.HostFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.NodeFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.VMFigure;


public class Legend extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.jmxmonitoring.view.Legend";

    public Legend() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        // Create the ScrolledComposite to scroll horizontally and vertically
        ScrolledComposite sc = new ScrolledComposite(parent,
                SWT.H_SCROLL | SWT.V_SCROLL);

        Composite child = new Composite(sc, SWT.NONE);

        FormLayout generalLayout = new FormLayout();
        generalLayout.marginHeight = 5;
        generalLayout.marginWidth = 5;

        child.setLayout(generalLayout);

        /*--------- Active objects ---------*/
        Group aoDef = new Group(child, 0);
        GridLayout aoLayout = new GridLayout();
        aoLayout.verticalSpacing = 2;
        aoLayout.numColumns = 2;
        aoDef.setLayout(aoLayout);
        aoDef.setText("Active objects");
        FormData aoDefFormData = new FormData();
        aoDefFormData.left = new FormAttachment(0, 0);
        aoDefFormData.right = new FormAttachment(100, 0);
        aoDef.setLayoutData(aoDefFormData);

        // Active by itself
        FigureCanvas ao1Container = new FigureCanvas(aoDef);
        ao1Container.setContents(new AOFigure(State.ACTIVE, 0, false));

        Label ao1Text = new Label(aoDef, 0);
        ao1Text.setText("Active by itself");

        // Serving request
        FigureCanvas ao2Container = new FigureCanvas(aoDef);
        ao2Container.setContents(new AOFigure(State.SERVING_REQUEST, 0, false));

        Label ao2Text = new Label(aoDef, 0);
        ao2Text.setText("Serving request");

        // Waiting for request
        FigureCanvas ao3Container = new FigureCanvas(aoDef);
        ao3Container.setContents(new AOFigure(State.WAITING_FOR_REQUEST, 0,
                false));

        Label ao3Text = new Label(aoDef, 0);
        ao3Text.setText("Waiting for request");

        // Waiting for result (wait by necessity)
        FigureCanvas ao4Container = new FigureCanvas(aoDef);
        ao4Container.setContents(new AOFigure(
                State.WAITING_BY_NECESSITY_WHILE_ACTIVE, 0, false));

        Label ao4Text = new Label(aoDef, 0);
        ao4Text.setText("Waiting for result\n(wait by necessity)");
        ao4Text.setSize(ao4Text.getSize().x / 2, ao4Text.getSize().y);

        // Migrating
        FigureCanvas ao5Container = new FigureCanvas(aoDef);
        ao5Container.setContents(new AOFigure(State.MIGRATING, 0, false));

        Label ao5Text = new Label(aoDef, 0);
        ao5Text.setText("Migrating");

        // Secure
        ao5Container = new FigureCanvas(aoDef);
        ao5Container.setContents(new AOFigure(State.ACTIVE, 0, true));
        ao5Text = new Label(aoDef, 0);
        ao5Text.setText("Secure and Active");

        /*--------- Pendings Request ---------*/
        Group requestDef = new Group(child, 0);
        GridLayout requestLayout = new GridLayout();
        requestLayout.numColumns = 2;
        requestDef.setLayout(requestLayout);
        requestDef.setText("Pending Requests");
        //requestDef.setBackground(ColorConstants.white);
        FormData requestDefFormData = new FormData();
        requestDefFormData.top = new FormAttachment(aoDef, 0);
        requestDefFormData.left = new FormAttachment(0, 0);
        requestDefFormData.right = new FormAttachment(100, 0);
        requestDef.setLayoutData(requestDefFormData);

        FigureCanvas requestContainer = new FigureCanvas(requestDef);
        AOFigure pendingRequestFigure = new AOFigure(State.SERVING_REQUEST, 64,
                false);
        requestContainer.setContents(pendingRequestFigure);

        Composite requestLabels = new Composite(requestDef, 0);
        FormLayout requestLayout2 = new FormLayout();
        requestLabels.setLayout(requestLayout2);

        Label requestLabel = new Label(requestLabels, 0);
        requestLabel.setText("Pending requests :");
        FormData requestLabelData = new FormData();
        //		requestLabelData.top = new FormAttachment(0, 0);
        //		requestLabelData.left = new FormAttachment(0, 0);
        requestLabel.setLayoutData(requestLabelData);

        FigureCanvas singleRequestCanvas = new FigureCanvas(requestLabels);
        singleRequestCanvas.setContents(pendingRequestFigure.new RequestQueueFigure(
                AOFigure.COLOR_REQUEST_SINGLE));
        FormData singleRequestData = new FormData();
        singleRequestData.top = new FormAttachment(requestLabel, 8);
        singleRequestCanvas.setLayoutData(singleRequestData);

        Label singleRequest = new Label(requestLabels, 0);
        singleRequest.setText("1");
        FormData singleRequestData2 = new FormData();
        singleRequestData2.top = new FormAttachment(requestLabel, 0);
        singleRequestData2.left = new FormAttachment(singleRequestCanvas, 4);
        singleRequest.setLayoutData(singleRequestData2);

        FigureCanvas severalRequestCanvas = new FigureCanvas(requestLabels);
        severalRequestCanvas.setContents(pendingRequestFigure.new RequestQueueFigure(
                AOFigure.COLOR_REQUEST_SEVERAL));
        FormData severalRequestData = new FormData();
        severalRequestData.top = new FormAttachment(requestLabel, 8);
        severalRequestData.left = new FormAttachment(singleRequest, 20);
        severalRequestCanvas.setLayoutData(severalRequestData);

        Label severalRequest = new Label(requestLabels, 0);
        severalRequest.setText(AOFigure.NUMBER_OF_REQUESTS_FOR_SEVERAL + "");
        FormData severalRequestData2 = new FormData();
        severalRequestData2.top = new FormAttachment(requestLabel, 0);
        severalRequestData2.left = new FormAttachment(severalRequestCanvas, 4);
        severalRequest.setLayoutData(severalRequestData2);

        FigureCanvas manyRequestCanvas = new FigureCanvas(requestLabels);
        manyRequestCanvas.setContents(pendingRequestFigure.new RequestQueueFigure(
                AOFigure.COLOR_REQUEST_MANY));
        FormData manyRequestData = new FormData();
        manyRequestData.top = new FormAttachment(requestLabel, 8);
        manyRequestData.left = new FormAttachment(severalRequest, 20);
        manyRequestCanvas.setLayoutData(manyRequestData);

        Label manyRequest = new Label(requestLabels, 0);
        manyRequest.setText(AOFigure.NUMBER_OF_REQUESTS_FOR_MANY + "");
        FormData manyRequestData2 = new FormData();
        manyRequestData2.top = new FormAttachment(requestLabel, 0);
        manyRequestData2.left = new FormAttachment(manyRequestCanvas, 4);
        manyRequest.setLayoutData(manyRequestData2);

        /*--------- Nodes ---------*/
        Group nodeDef = new Group(child, 0);
        GridLayout nodeLayout = new GridLayout();
        nodeLayout.numColumns = 2;
        nodeLayout.verticalSpacing = 0;
        nodeDef.setLayout(nodeLayout);
        nodeDef.setText("Nodes");
        FormData nodeDefFormData = new FormData();
        nodeDefFormData.top = new FormAttachment(requestDef, 0);
        nodeDefFormData.left = new FormAttachment(0, 0);
        nodeDefFormData.right = new FormAttachment(100, 0);
        nodeDef.setLayoutData(nodeDefFormData);

        // RMI Node
        FigureCanvas node1Container = new FigureCanvas(nodeDef);
        node1Container.setContents(new NodeFigure(
                Constants.RMI_PROTOCOL_IDENTIFIER));

        Label node1Text = new Label(nodeDef, 0);
        node1Text.setText("RMI Node");

        // HTTP Node
        FigureCanvas node2Container = new FigureCanvas(nodeDef);
        node2Container.setContents(new NodeFigure(
                Constants.XMLHTTP_PROTOCOL_IDENTIFIER));

        Label node2Text = new Label(nodeDef, 0);
        node2Text.setText("HTTP Node");

        // RMI/SSH Node
        FigureCanvas node3Container = new FigureCanvas(nodeDef);
        node3Container.setContents(new NodeFigure(
                Constants.RMISSH_PROTOCOL_IDENTIFIER));

        Label node3Text = new Label(nodeDef, 0);
        node3Text.setText("RMI/SSH Node");

        /*--------- JVMs ---------*/
        Group jvmDef = new Group(child, 0);
        GridLayout jvmLayout = new GridLayout();
        jvmLayout.numColumns = 2;
        jvmLayout.verticalSpacing = 0;
        jvmDef.setLayout(jvmLayout);
        jvmDef.setText("JVMs");
        FormData jvmDefFormData = new FormData();
        jvmDefFormData.top = new FormAttachment(nodeDef, 0);
        jvmDefFormData.left = new FormAttachment(0, 0);
        jvmDefFormData.right = new FormAttachment(100, 0);
        jvmDef.setLayoutData(jvmDefFormData);

        // Standard JVM
        FigureCanvas jvm1Container = new FigureCanvas(jvmDef);
        jvm1Container.setContents(new VMFigure());

        Label jvm1Text = new Label(jvmDef, 0);
        jvm1Text.setText("Standard JVM");

        // JVM started with Globus
        FigureCanvas jvm2Container = new FigureCanvas(jvmDef);
        VMFigure jvm2Figure = new VMFigure();
        jvm2Figure.withGlobus();
        jvm2Container.setContents(jvm2Figure);

        Label jvm2Text = new Label(jvmDef, 0);
        jvm2Text.setText("JVM started with Globus");

        /*--------- Hosts ---------*/
        Group hostDef = new Group(child, 0);
        GridLayout hostLayout = new GridLayout();
        hostLayout.numColumns = 2;
        hostDef.setLayout(hostLayout);
        hostDef.setText("Hosts");
        FormData hostDefFormData = new FormData();
        hostDefFormData.top = new FormAttachment(jvmDef, 0);
        hostDefFormData.left = new FormAttachment(0, 0);
        hostDefFormData.right = new FormAttachment(100, 0);
        hostDef.setLayoutData(hostDefFormData);

        // Standard Host
        FigureCanvas hostContainer = new FigureCanvas(hostDef);
        hostContainer.setContents(new HostFigure());

        Label hostText = new Label(hostDef, 0);
        hostText.setText("Standard Host");

        /*--------- Not Responding ---------*/
        Group noRespondingDef = new Group(child, 0);
        GridLayout noRespondingLayout = new GridLayout();
        noRespondingLayout.numColumns = 2;
        noRespondingLayout.verticalSpacing = 5;
        noRespondingDef.setLayout(noRespondingLayout);
        noRespondingDef.setText("Not Responding");
        FormData noRespondingDefFormData = new FormData();
        noRespondingDefFormData.top = new FormAttachment(hostDef, 0);
        noRespondingDefFormData.left = new FormAttachment(0, 0);
        noRespondingDefFormData.right = new FormAttachment(100, 0);
        noRespondingDef.setLayoutData(noRespondingDefFormData);

        // Active Object
        FigureCanvas aoNoRespondingContainer = new FigureCanvas(noRespondingDef);
        aoNoRespondingContainer.setContents(new AOFigure(State.NOT_RESPONDING,
                0, false));

        Label aoNoRespondingText = new Label(noRespondingDef, 0);
        aoNoRespondingText.setText("Active Object");

        // JVM
        FigureCanvas jvmNoRespondingContainer = new FigureCanvas(noRespondingDef);
        VMFigure jvmFigure = new VMFigure();
        jvmFigure.notResponding();
        jvmNoRespondingContainer.setContents(jvmFigure);

        Label jvmNoRespondingText = new Label(noRespondingDef, 0);
        jvmNoRespondingText.setText("JVM");

        /* --------------------------------*/

        // Set the child as the scrolled content of the ScrolledComposite
        sc.setContent(child);

        // Set the minimum size
        child.setSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        sc.setMinSize(child.getSize().x, child.getSize().y);

        // Expand both horizontally and vertically
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }
}
