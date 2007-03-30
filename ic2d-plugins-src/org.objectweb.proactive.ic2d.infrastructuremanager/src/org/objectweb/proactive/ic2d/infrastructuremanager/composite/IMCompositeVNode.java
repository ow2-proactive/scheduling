package org.objectweb.proactive.ic2d.infrastructuremanager.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.objectweb.proactive.ic2d.infrastructuremanager.IMConstants;

public class IMCompositeVNode extends Composite {

	private IMCompositeDescriptor parent;
	private Composite headerComposite;
	private Composite nodesComposite;	
	private Button button;
	private RowData data;
	private String nameVNode;
	
	private boolean isCollapse = true;

	public IMCompositeVNode(IMCompositeDescriptor p, String name) {
		super(p, SWT.NONE);
		nameVNode = name;
		parent = p;
		parent.addVnode(this);
		setBackground(IMConstants.WHITE_COLOR);

		data = new RowData();
		data.exclude = isCollapse;
		setLayout(new RowLayout(SWT.VERTICAL));
		
		RowLayout rowLayoutHorizontal = new RowLayout(SWT.HORIZONTAL);
		rowLayoutHorizontal.pack = true;

		// Composite Header
		headerComposite = new Composite(this, SWT.NONE);
		headerComposite.setBackground(IMConstants.WHITE_COLOR);
		headerComposite.setLayout(rowLayoutHorizontal);
		button = new Button(headerComposite, SWT.ARROW|SWT.RIGHT);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isCollapse = ! isCollapse;
				data.exclude = isCollapse;
				nodesComposite.setVisible(! isCollapse);
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
		label.setBackground(IMConstants.WHITE_COLOR);

		// Composite des nodes
		nodesComposite = new Composite(this, SWT.NONE);
		nodesComposite.setBackground(IMConstants.WHITE_COLOR);
		nodesComposite.setLayout(rowLayoutHorizontal);
		nodesComposite.setLayoutData(data);
		nodesComposite.setVisible(false);
		
	}
	
	public Composite getNodesComposite() {
		return nodesComposite;
	}
	
	public String getName() {
		return nameVNode;
	}
	
	public boolean isCollapsed() {
		return isCollapse;
	}

	public void setCollapse(boolean b) {
		isCollapse = b;
		data.exclude = isCollapse;
		nodesComposite.setVisible(! isCollapse);
		if(nodesComposite.isVisible()) {
			button.setAlignment(SWT.DOWN);
		}
		else {
			button.setAlignment(SWT.RIGHT);
		}
		parent.pack(true);
	}
	
}
