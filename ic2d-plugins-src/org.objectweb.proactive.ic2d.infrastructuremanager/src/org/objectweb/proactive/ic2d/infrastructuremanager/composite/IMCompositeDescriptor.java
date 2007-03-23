package org.objectweb.proactive.ic2d.infrastructuremanager.composite;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandItem;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;


public class IMCompositeDescriptor extends Composite {

	private ExpandItem expandItem;
	
	public IMCompositeDescriptor(Composite parent, int style, ExpandItem ei, HashMap<String,ArrayList<IMNode>> liste) {
		super(parent, style);
		RowLayout rowLayoutVertical = new RowLayout(SWT.VERTICAL);
		rowLayoutVertical.pack = true;
		setLayout(rowLayoutVertical);
		expandItem = ei;
				
		// Pour tous les VNodes
		for (String nameVNode : liste.keySet()) {
			new IMCompositeVNode(this, SWT.NONE, nameVNode, liste.get(nameVNode));
		}
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		expandItem.setHeight(computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		expandItem.setControl(this);
	}
	
	public void pack(boolean b) {
		super.pack(b);
		expandItem.setHeight(getSize().y);
		expandItem.getParent().redraw();
	}
	
}

