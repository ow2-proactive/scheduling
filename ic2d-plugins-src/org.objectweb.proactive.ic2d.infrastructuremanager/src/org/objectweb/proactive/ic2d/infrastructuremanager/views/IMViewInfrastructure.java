package org.objectweb.proactive.ic2d.infrastructuremanager.views;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.infrastructuremanager.IMConstants;
import org.objectweb.proactive.ic2d.infrastructuremanager.actions.CollapseAllAction;
import org.objectweb.proactive.ic2d.infrastructuremanager.actions.ExpandAllAction;
import org.objectweb.proactive.ic2d.infrastructuremanager.composite.IMCompositeDescriptor;
import org.objectweb.proactive.ic2d.infrastructuremanager.data.IMData;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogConnection;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogDeploy;

public class IMViewInfrastructure extends ViewPart  {

	public static final String ID = "org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewInfrastructure";

	private IMData imData;
	private Composite parent;
	//private GraphicalViewerImpl graphicalViewer;
	private Label allNodeLabel, busyNodeLabel, freeNodeLabel, absentNodeLabel;
	private String allNodeText, busyNodeText, freeNodeText, absentNodeText;
	private int allNodeValue, busyNodeValue, freeNodeValue, absentNodeValue;
	private ExpandBar expandBar;
	
	private String partName, url;
	
	public IMViewInfrastructure() {
	}
	
	public IMViewInfrastructure(String name, String url) {
		this.partName = name;
		this.url = url;
	}
	
	
	
	public void createPartControl(Composite parent) {	
		this.parent = parent;
		IMDialogConnection dialog = new IMDialogConnection(parent.getShell());
		if (dialog.isAccept()) {
			initIMViewInfrastructure(parent, dialog.getNameView(), dialog.getUrl());
			IMViewAdministration.addView(dialog.getNameView(), this);
		}
	}
	
	public void initIMViewInfrastructure(Composite parent, String partName, String url) {		
		// Change the name of the view
		setPartName(partName);
		
		GridData data1 = new GridData(GridData.FILL_HORIZONTAL);

		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;

		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
		gridLayout1.verticalSpacing = 5;
		parent.setLayout(gridLayout1);
		
		// Composite LABELs
		Composite compositeLabel = new Composite (parent, SWT.BORDER);
		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.justify = true;
		compositeLabel.setLayout(rowLayout);
		// Label AllNode
		allNodeText = "Total Node = ";
		allNodeValue = 0;
		allNodeLabel = new Label(compositeLabel, SWT.NONE);
		allNodeLabel.setText(allNodeText + allNodeValue);
		// Label FreeNode
		freeNodeText = "Free Node = ";
		freeNodeValue = 0;
		freeNodeLabel = new Label(compositeLabel, SWT.NONE);
		freeNodeLabel.setText(freeNodeText + freeNodeValue);
		freeNodeLabel.setForeground(IMConstants.AVAILABLE_COLOR);
		// Label BusyNode
		busyNodeText = "Busy Node = ";
		busyNodeValue = 0;
		busyNodeLabel = new Label(compositeLabel, SWT.NONE);
		busyNodeLabel.setText(busyNodeText + busyNodeValue);
		busyNodeLabel.setForeground(IMConstants.BUSY_COLOR);
		// Label AbsentNode
		absentNodeText = "Absent Node = ";
		absentNodeValue = 0;
		absentNodeLabel = new Label(compositeLabel, SWT.NONE);
		absentNodeLabel.setText(absentNodeText + absentNodeValue);
		absentNodeLabel.setForeground(IMConstants.DOWN_COLOR);
		

		expandBar = new ExpandBar(parent, SWT.V_SCROLL);
		expandBar.setSpacing(10);
		expandBar.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		imData = new IMData(url);
		refresh();

		compositeLabel.setLayoutData(data1);
		expandBar.setLayoutData(gridData);
		
		// ToolBar
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();		
		toolBarManager.add(new CollapseAllAction(expandBar));
		toolBarManager.add(new ExpandAllAction(expandBar));
		toolBarManager.add(new Separator());
		
	}
	
	public void setIMData(IMData imData) {
		this.imData = imData;
	}

	
	public void refresh() {
		Console cons = Console.getInstance(IConsoleConstants.ID_CONSOLE_VIEW);
		cons.log("Refresh()");
		System.out.println("Refresh()");
		// Descripteurs
		HashMap<String, HashMap<String,ArrayList<IMNode>>> listDesc = imData.getInfrastructure();
		// Pour tous les Descriptors 
		for (String nameDescriptor : listDesc.keySet()) {
			cons.log("\t" + nameDescriptor);
			System.out.println("\t" + nameDescriptor);
			ExpandItem itemDescriptor = new ExpandItem (expandBar, SWT.NONE);
			itemDescriptor.setText(nameDescriptor);
			new IMCompositeDescriptor(expandBar, SWT.NONE, itemDescriptor, listDesc.get(nameDescriptor));
		}
		expandBar.redraw();
		cons.log("ExpandBar Childrens = " + expandBar.getChildren().length);
		cons.log("End Refresh()");
		System.out.println("ExpandBar Childrens = " + expandBar.getChildren().length);
		System.out.println("End Refresh()");
	}

	public void setFocus() {
		IMViewAdministration.selectViewInList(getPartName());
	}
	
	public void dispose() {
		super.dispose();
		IMViewAdministration.removeView(getPartName());
	}
	
	public void deploy() {
		new IMDialogDeploy(parent.getShell(), imData.getAdmin());
	}
	
}