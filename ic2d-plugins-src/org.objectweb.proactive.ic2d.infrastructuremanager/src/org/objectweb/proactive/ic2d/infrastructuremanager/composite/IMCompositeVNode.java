package org.objectweb.proactive.ic2d.infrastructuremanager.composite;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;
import org.objectweb.proactive.ic2d.infrastructuremanager.IMConstants;
import org.objectweb.proactive.ic2d.infrastructuremanager.figure.IMNodeFigure;
import org.objectweb.proactive.ic2d.monitoring.figures.HostFigure;
import org.objectweb.proactive.ic2d.monitoring.figures.VMFigure;

public class IMCompositeVNode extends Composite {

	private IMCompositeDescriptor parent;
	private Composite headerComposite;
	private Composite nodesComposite;	
	private Button button;
	private RowData data;
	private String nameVNode;

	public IMCompositeVNode(IMCompositeDescriptor p, int style, String name, ArrayList<IMNode> nodes) {
		super(p, SWT.NONE);

		nameVNode = name;
		parent = p;

		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));


		data = new RowData();
		data.exclude = true;
		setLayout(new RowLayout(SWT.VERTICAL));
		
		RowLayout rowLayoutHorizontal = new RowLayout(SWT.HORIZONTAL);
		rowLayoutHorizontal.pack = true;

		// Composite Header
		headerComposite = new Composite(this, SWT.NONE);
		headerComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		headerComposite.setLayout(rowLayoutHorizontal);
		button = new Button(headerComposite, SWT.ARROW|SWT.RIGHT);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				data.exclude = ! data.exclude;
				nodesComposite.setVisible(! data.exclude);
				if(nodesComposite.isVisible()) {
					button.setAlignment(SWT.DOWN);
				}
				else {
					button.setAlignment(SWT.RIGHT);
				}

				parent.pack(true);
			}
		});
		Label label = new Label(headerComposite, SWT.NONE);
		label.setText(nameVNode);
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));




		// Composite des nodes
		nodesComposite = new Composite(this, SWT.NONE);
		nodesComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		nodesComposite.setLayout(rowLayoutHorizontal);
		nodesComposite.setLayoutData(data);
		nodesComposite.setVisible(false);
		
		HashMap<String, HashMap<String, ArrayList<IMNode>>> structure = sortIMNodeInformation(nodes);
		for (String hostName : structure.keySet()) {
			FigureCanvas hostContainer = new FigureCanvas(nodesComposite);
			hostContainer.setBackground(IMConstants.WHITE_COLOR);
			HostFigure hostFigure = new HostFigure(hostName);
			for (String jvmName : structure.get(hostName).keySet()) {
				VMFigure jvmFigure = new VMFigure(jvmName);
				for(IMNode imNode : structure.get(hostName).get(jvmName)) {
					IMNodeFigure imNodeFigure;
					try {
						String imNodeName = imNode.getNodeInformation().getName();
						if(imNode.isFree()) {
							imNodeFigure = new IMNodeFigure(imNodeName, IMConstants.STATUS_AVAILABLE);
						}
						else {
							imNodeFigure = new IMNodeFigure(imNodeName, IMConstants.STATUS_BUSY);
						}
					}
					catch(NodeException e) {
						imNodeFigure = new IMNodeFigure("Node", IMConstants.STATUS_DOWN);
					}
					
					jvmFigure.getContentPane().add(imNodeFigure);
					
					
					
				}
				hostFigure.getContentPane().add(jvmFigure);
			}
			hostContainer.setContents(hostFigure);
		}

		
		MouseAdapter mouseAdapter = new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				System.out.println("Double Click in VNode : " + nameVNode);
			}
		};
		headerComposite.addMouseListener(mouseAdapter);
		nodesComposite.addMouseListener(mouseAdapter);
		
	}

	public HashMap<String, HashMap<String, ArrayList<IMNode>>> sortIMNodeInformation(ArrayList<IMNode> nodes) {
		HashMap<String, HashMap<String, ArrayList<IMNode>>> hashMapMachines = 
			new HashMap<String, HashMap<String, ArrayList<IMNode>>>();
		for (IMNode imNode : nodes) {
			String hostName = imNode.getHostName();
			String jvmName = imNode.getDescriptorVMName();
			// Si il y a deja la machine dans la HashMap
			if (hashMapMachines.containsKey(hostName)) {
				// Si il y a deja la Jvm dans la HashMap
				if(hashMapMachines.get(hostName).containsKey(jvmName)) {
					hashMapMachines.get(hostName).get(jvmName).add(imNode);
				}
				// Sion on ajoute la Jvm dans la HashMap
				else {
					ArrayList<IMNode> listNodes = new ArrayList<IMNode>();
					listNodes.add(imNode);
					hashMapMachines.get(hostName).put(jvmName, listNodes);
				}
			}
			// Sion on ajoute la machine dans la HashMap
			else {
				ArrayList<IMNode> listNodes = new ArrayList<IMNode>();
				listNodes.add(imNode);
				HashMap<String, ArrayList<IMNode>> hashMapJvm= new HashMap<String, ArrayList<IMNode>>();
				hashMapJvm.put(jvmName, listNodes);
				hashMapMachines.put(hostName, hashMapJvm);
			}
		}
		return hashMapMachines;
	}

}




