package org.objectweb.proactive.extensions.resourcemanager.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/**
 * @author FRADJ Johann
 */
public class NodeInfoView extends ViewPart {
	/** the view part id */
	public static final String ID = "org.objectweb.proactive.extensions.resourcemanager.gui.views.NodeInfoView";

	/**
	 * The constructor.
	 */
	public NodeInfoView() {}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		Table table = new Table(parent, SWT.BORDER | SWT.SINGLE);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);
        tc1.setText("Chepa");
        tc2.setText("Toujours pas");
        tc1.setWidth(100);
        tc2.setWidth(100);
        tc1.setMoveable(true);
        tc2.setMoveable(true);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		//viewer.getControl().setFocus();
	}
}