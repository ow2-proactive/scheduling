package org.objectweb.proactive.ic2d.infrastructuremanager.views;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.draw2d.FigureCanvas;
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
import org.eclipse.ui.part.ViewPart;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;
import org.objectweb.proactive.ic2d.infrastructuremanager.IMConstants;
import org.objectweb.proactive.ic2d.infrastructuremanager.actions.IMActionCollapseAll;
import org.objectweb.proactive.ic2d.infrastructuremanager.actions.IMActionExpandAll;
import org.objectweb.proactive.ic2d.infrastructuremanager.actions.IMActionRefresh;
import org.objectweb.proactive.ic2d.infrastructuremanager.actions.IMActionSetTTR;
import org.objectweb.proactive.ic2d.infrastructuremanager.composite.IMCompositeDescriptor;
import org.objectweb.proactive.ic2d.infrastructuremanager.composite.IMCompositeVNode;
import org.objectweb.proactive.ic2d.infrastructuremanager.data.IMData;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogConnection;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogDeploy;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogKill;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogRedeploy;
import org.objectweb.proactive.ic2d.infrastructuremanager.figure.IMFigureNode;
import org.objectweb.proactive.ic2d.infrastructuremanager.figure.IMFigureHost;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.VMFigure;


public class IMViewInfrastructure extends ViewPart {

	public static final String ID = "org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewInfrastructure";

	private IMData imData;
	private Composite parent;
	private Label allNodeLabel, busyNodeLabel, freeNodeLabel, downNodeLabel;
	private String allNodeText, busyNodeText, freeNodeText, downNodeText;
	private int free = 0, busy = 0, down = 0;
	
	public ExpandBar expandBar;
	
	public Thread threadRefresh;
	
	
	public void createPartControl(Composite parent) {	
		this.parent = parent;
		IMDialogConnection dialog = new IMDialogConnection(parent.getShell());
		if (dialog.isAccept()) {
			initIMViewInfrastructure(parent, dialog.getNameView(), dialog.getUrl());
			IMViewAdministration.addView(dialog.getNameView(), this);
		}
	}
	
	public Composite getParent() {
		return parent;
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
		allNodeLabel = new Label(compositeLabel, SWT.NONE);
		allNodeLabel.setText(allNodeText + (free+busy+down));
		// Label FreeNode
		freeNodeText = "Free Node = ";
		freeNodeLabel = new Label(compositeLabel, SWT.NONE);
		freeNodeLabel.setText(freeNodeText + free);
		freeNodeLabel.setForeground(IMConstants.AVAILABLE_COLOR);
		// Label BusyNode
		busyNodeText = "Busy Node = ";
		busyNodeLabel = new Label(compositeLabel, SWT.NONE);
		busyNodeLabel.setText(busyNodeText + busy);
		busyNodeLabel.setForeground(IMConstants.BUSY_COLOR);
		// Label AbsentNode
		downNodeText = "Absent Node = ";
		downNodeLabel = new Label(compositeLabel, SWT.NONE);
		downNodeLabel.setText(downNodeText + down);
		downNodeLabel.setForeground(IMConstants.DOWN_COLOR);
		

		expandBar = new ExpandBar(parent, SWT.V_SCROLL);
		expandBar.setSpacing(10);
		expandBar.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		
		imData = new IMData(url, this);
		threadRefresh = new Thread(imData);
		threadRefresh.start();
		
		compositeLabel.setLayoutData(data1);
		expandBar.setLayoutData(gridData);
		
		// ToolBar
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();		
		toolBarManager.add(new IMActionSetTTR(parent.getDisplay(), getIMData()));
		toolBarManager.add(new IMActionRefresh(threadRefresh));
		toolBarManager.add(new Separator());
		toolBarManager.add(new IMActionCollapseAll(expandBar));
		toolBarManager.add(new IMActionExpandAll(expandBar));
		
	}

	
	public void incrFree() {
		free += 1;
	}
	public void incrBusy() {
		busy += 1;
	}
	public void incrDown() {
		down += 1;
	}
	
	public void drawInfrastructure() {
		
		free = 0;
		busy = 0;
		down = 0;
			
		HashMap<String, Boolean> oldExpandItemState = new HashMap<String, Boolean>();
		HashMap<String, ArrayList<String>> oldState = new HashMap<String, ArrayList<String>>();
		
		
		// Remove old item
		if(expandBar.getItemCount() != 0) {
			for (ExpandItem item : expandBar.getItems()) {
				oldExpandItemState.put(item.getText(), item.getExpanded());
				if(item.getExpanded()) {
					oldState.put(item.getText(), 
							((IMCompositeDescriptor) item.getControl()).getNamesOfExpandedVNodes());
				}
				item.getControl().dispose();
				item.dispose();
			}
		}
		
		// Infrastructure
		ArrayList<IMNode> list = imData.getInfrastructure();
		
		// Si la list n'est pas vide
		if (list.size() != 0) {
		
			// Initialisation
			IMNode currentIMNode, oldIMNode;
			currentIMNode = list.get(0);
			ExpandItem currentExpandItem = new ExpandItem(expandBar, SWT.BORDER);
			currentExpandItem.setText(currentIMNode.getPADName());
			IMCompositeDescriptor currentDescriptor = new IMCompositeDescriptor(expandBar, currentExpandItem);
			IMCompositeVNode currentVNode = new IMCompositeVNode(currentDescriptor, currentIMNode.getVNodeName());
			FigureCanvas currentCanvas = new FigureCanvas(currentVNode.getNodesComposite());
			currentCanvas.setBackground(IMConstants.WHITE_COLOR);
			IMFigureHost currentHost = new IMFigureHost(currentIMNode.getHostName());			
			VMFigure currentVM = new VMFigure(currentIMNode.getDescriptorVMName());			
			currentVM.getContentPane().add(new IMFigureNode(currentIMNode, this));
			
			for (int i=1; i<list.size(); i++) {
				oldIMNode = currentIMNode;
				currentIMNode = list.get(i);
				
				// Si les nodes n'ont pas le meme descriptor
				if(! currentIMNode.getPADName().equals(oldIMNode.getPADName())) {
					currentHost.getContentPane().add(currentVM);
					currentCanvas.setContents(currentHost);
					currentExpandItem.setHeight(currentDescriptor.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
					currentExpandItem.setControl(currentDescriptor);
					
					currentExpandItem = new ExpandItem(expandBar, SWT.NONE);
					currentExpandItem.setText(currentIMNode.getPADName());
					currentDescriptor = new IMCompositeDescriptor(expandBar, currentExpandItem);
					currentVNode = new IMCompositeVNode(currentDescriptor, currentIMNode.getVNodeName());
					currentCanvas = new FigureCanvas(currentVNode.getNodesComposite());
					currentCanvas.setBackground(IMConstants.WHITE_COLOR);
					currentHost = new IMFigureHost(currentIMNode.getHostName());
					currentVM = new VMFigure(currentIMNode.getDescriptorVMName());
					currentVM.getContentPane().add(new IMFigureNode(currentIMNode, this));
				}
				// Sinon si les 2 nodes n'ont pas le meme vnode
				else if(! currentIMNode.getVNodeName().equals(oldIMNode.getVNodeName())) {
					currentHost.getContentPane().add(currentVM);
					currentCanvas.setContents(currentHost);
					
					currentVNode = new IMCompositeVNode(currentDescriptor, currentIMNode.getVNodeName());
					currentCanvas = new FigureCanvas(currentVNode.getNodesComposite());
					currentCanvas.setBackground(IMConstants.WHITE_COLOR);
					currentHost = new IMFigureHost(currentIMNode.getHostName());
					currentVM = new VMFigure(currentIMNode.getDescriptorVMName());
					currentVM.getContentPane().add(new IMFigureNode(currentIMNode, this));
				}
				// Sinon si les 2 nodes n'ont pas le meme host
				else if(! currentIMNode.getHostName().equals(oldIMNode.getHostName())) {
					currentHost.getContentPane().add(currentVM);
					currentCanvas.setContents(currentHost);
					currentCanvas = new FigureCanvas(currentVNode.getNodesComposite());
					currentCanvas.setBackground(IMConstants.WHITE_COLOR);
					currentHost = new IMFigureHost(currentIMNode.getHostName());
					currentVM = new VMFigure(currentIMNode.getDescriptorVMName());
					currentVM.getContentPane().add(new IMFigureNode(currentIMNode, this));
				}			
				// Sinon si les 2 nodes n'ont pas la meme vm
				else if(! currentIMNode.getDescriptorVMName().equals(oldIMNode.getDescriptorVMName())) {
					currentHost.getContentPane().add(currentVM);
					currentVM = new VMFigure(currentIMNode.getDescriptorVMName());
					currentVM.getContentPane().add(new IMFigureNode(currentIMNode, this));
				}				
				// Sinon
				else {
					currentVM.getContentPane().add(new IMFigureNode(currentIMNode, this));
				}								
			}
			
			currentHost.getContentPane().add(currentVM);
			currentCanvas.setContents(currentHost);
			currentExpandItem.setHeight(currentDescriptor.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
			currentExpandItem.setControl(currentDescriptor);
			
			// Remet dans l'etat d'avant (Expand/Collapse)
			for(ExpandItem expandItem : expandBar.getItems()) {
				if (oldState.containsKey(expandItem.getText())) {
					expandItem.setExpanded(true);
					IMCompositeDescriptor comp = (IMCompositeDescriptor) expandItem.getControl();
					for(String oldVnodeName : oldState.get(expandItem.getText())) {
						if(comp.getVnodes().containsKey(oldVnodeName)) {
							comp.getVnodes().get(oldVnodeName).setCollapse(false);
						}
					}
				}
			}
			
			busyNodeLabel.setText(busyNodeText + busy);
			freeNodeLabel.setText(freeNodeText + free);
			downNodeLabel.setText(downNodeText + down);
			allNodeLabel.setText(allNodeText + (free+busy+down));

			
			expandBar.redraw();
		}
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

	public void redeploy() {		
		new IMDialogRedeploy(parent.getShell(), imData.getAdmin());
	}

	public void kill() {
		new IMDialogKill(parent.getShell(), imData.getAdmin());
	}
	
	public IMData getIMData() {
		return imData;
	}

	public void shutdown() {
		try {
			imData.getAdmin().shutdown();
			// TODO : 
			dispose();
		} 
		catch (ProActiveException e) {
			e.printStackTrace();
		}		
	}
	
}