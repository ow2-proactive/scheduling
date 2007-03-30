package org.objectweb.proactive.ic2d.infrastructuremanager.dialog;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;

public class IMDialogKill  extends Dialog {

	private final String TITLE = "Kill";
	private Shell shell;
	private Button killButton, cancelButton;
	private Combo combo;
	private Table table;
	private Composite labelAndCheckComposite, buttonsComposite;
	private IMAdmin admin;
	private HashMap<String, ArrayList<VirtualNode>> hashMap;

	
	public IMDialogKill(Shell parent, IMAdmin admin) {
		// Pass the default styles here
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		this.admin = admin;

		hashMap = admin.getDeployedVirtualNodeByPad();

		// Init the display
		Display display = getParent().getDisplay();

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 10;
		gridLayout.marginBottom = gridLayout.marginTop = 10;
		gridLayout.marginLeft = gridLayout.marginRight = 10;

		// Init the shell
		shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
		shell.setText(TITLE);
		shell.setLayout(gridLayout);

		// FileChooser Button & Text
		addCombo();
		addButtonsComposite();

		shell.setSize(500, 150);
		shell.open();

		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}

	}

	public void addCombo() {
		combo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		for(String name : hashMap.keySet()) {
			combo.add(name);
		}				
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (labelAndCheckComposite == null) {
					buttonsComposite.dispose();
					addLabelAndCheckAll();
					addTable();
					addButtonsComposite();
				}
				addTableItems();
				shell.setSize(500, 400);
			}
		});
	}

	public void addLabelAndCheckAll() {
		labelAndCheckComposite = new Composite(shell, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.justify = true;
		labelAndCheckComposite.setLayout(layout);
		Label label = new Label(labelAndCheckComposite, SWT.CENTER);
		label.setText("Choose at least one Virtual Node to activate");

		Button allButton = new Button(labelAndCheckComposite, SWT.PUSH);
		allButton.setText("All");
		allButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (TableItem item : table.getItems()) {
					item.setChecked(true);
				}
			}
		});

		Button noneButton = new Button(labelAndCheckComposite, SWT.PUSH);
		noneButton.setText("None");
		noneButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (TableItem item : table.getItems()) {
					item.setChecked(false);
				}
			}
		});

		allButton.setLayoutData(new RowData(50, 25));
		noneButton.setLayoutData(new RowData(50, 25));

		labelAndCheckComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		labelAndCheckComposite.setVisible(false);
	}

	public void addTable() {
		table = new Table(shell, SWT.VIRTUAL | SWT.CHECK | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setVisible(false);
	}


	public void addButtonsComposite() {
		buttonsComposite = new Composite(shell, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.spacing = 20;
		layout.justify = true;
		buttonsComposite.setLayout(layout);
		buttonsComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		cancelButton = new Button(buttonsComposite, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});

		killButton = new Button(buttonsComposite, SWT.PUSH);
		killButton.setText("Kill");
		killButton.setEnabled(false);
		killButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// TODO : IMDialogDeploy Action deployButton ...);
				ArrayList<String> listOfCheckedItem = new ArrayList<String>();				
				for(TableItem item : table.getItems()) {
					if(item.getChecked()) {
						listOfCheckedItem.add(item.getText());
					}
				}
				int checkedItemsNumber = listOfCheckedItem.size();
				if (checkedItemsNumber == 0) {
					shell.close();
				}
				else {
					if (checkedItemsNumber == 1) {
						admin.killPAD(combo.getText(), listOfCheckedItem.get(0));
					}
					else {
						String[] tab = new String[checkedItemsNumber];
						listOfCheckedItem.toArray(tab);
						admin.killPAD(combo.getText(), tab);
					}
					shell.close();
				}
			}
		});
	}

	public void addTableItems() {
		table.removeAll();
		for (VirtualNode vnode : hashMap.get(combo.getText())) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(vnode.getName());
		}
		labelAndCheckComposite.setVisible(true);
		table.setVisible(true);
		table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		table.redraw();
		killButton.setEnabled(true);
	}

}