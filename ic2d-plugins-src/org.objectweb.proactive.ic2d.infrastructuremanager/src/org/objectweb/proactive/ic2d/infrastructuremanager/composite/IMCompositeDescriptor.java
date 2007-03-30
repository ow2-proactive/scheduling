package org.objectweb.proactive.ic2d.infrastructuremanager.composite;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.objectweb.proactive.ic2d.infrastructuremanager.IMConstants;

public class IMCompositeDescriptor extends Composite {

	private ExpandItem item;
	private HashMap<String, IMCompositeVNode> vnodes;
	
	public IMCompositeDescriptor(Composite parent, ExpandItem ei) {
		super(parent, SWT.NONE);
		item = ei;
		vnodes = new HashMap<String, IMCompositeVNode>();
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.wrap = false;
		setLayout(layout);
		setBackground(IMConstants.WHITE_COLOR);

		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText ("Item Test");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				System.out.println("item selected: Item Test");
			}
		});
		setMenu(menu);
		
	}

	public void pack(boolean b) {
		super.pack(b);
		item.setHeight(computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.getParent().redraw();
	}

	public void addVnode(IMCompositeVNode v) {
		vnodes.put(v.getName(), v);
	}
	
	public HashMap<String, IMCompositeVNode> getVnodes() {
		return vnodes;
	}
	
	public ArrayList<String> getNamesOfExpandedVNodes() {
		ArrayList<String> res = new ArrayList<String>();
		for (IMCompositeVNode vnode : vnodes.values()) {
			if(! vnode.isCollapsed()) {
				res.add(vnode.getName());
			}
		}
		return res;
	}
	
}

