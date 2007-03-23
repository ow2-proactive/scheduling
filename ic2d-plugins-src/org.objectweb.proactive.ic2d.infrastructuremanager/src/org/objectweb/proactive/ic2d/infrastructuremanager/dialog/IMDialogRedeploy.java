package org.objectweb.proactive.ic2d.infrastructuremanager.dialog;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

public class IMDialogRedeploy  extends Dialog {

	private final String TITLE = "Redeploy";
	private Shell shell;
	private Button redeployButton;
	
	private Combo combo;
	private Table table;
	private Composite labelAndCheckComposite;
	
	private HashMap<String, ProActiveDescriptor> hashMap;
	
	
	public IMDialogRedeploy(Shell parent) {
		// Pass the default styles here
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				
		
		// TODO 
		hashMap = new HashMap<String, ProActiveDescriptor>();
		String directory = "/home/yannlecorse/Desktop/ProActive/descriptors/";
		try {
			hashMap.put("Desc1.xml", ProActive.getProactiveDescriptor(directory+"Descriptor-C3D-Dispatcher.xml"));
			hashMap.put("Desc2.xml", ProActive.getProactiveDescriptor(directory+"helloLocal.xml"));
			hashMap.put("Desc3.xml", ProActive.getProactiveDescriptor(directory+"Workers.xml"));
		} 
		catch (ProActiveException e) {
			e.printStackTrace();
		}
		
		
		
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
		addLabelAndCheckAll();
		addTable();
		addRedeployButton();
		
		shell.setSize(500, 100);
		shell.open();

		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
		
	}
	
	public void addCombo() {
		combo = new Combo(shell, SWT.DROP_DOWN);
    for(String name : hashMap.keySet()) {
      combo.add(name);
    }				
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
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
	
	public void addRedeployButton() {
		redeployButton = new Button(shell, SWT.PUSH);
		redeployButton.setText("Redeploy");
		redeployButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		redeployButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// TODO : IMDialogDeploy Action deployButton ...);
				int size = table.getItems().length;
				int nbChecked = 0;
				String[] vNodeChecked = new String[size];
				for(TableItem item : table.getItems()) {
					if(item.getChecked()) {
						vNodeChecked[nbChecked] = item.getText();
						nbChecked++;
					}
				}
				if (nbChecked == 0) {
					System.out.println("None check ...");
					// TODO : alert 
				}
				else if (nbChecked == 1) {
					System.out.println("One check ...");
					
				}
				else if (nbChecked == size) {
					System.out.println("All check ...");
				}
				else {
					System.out.println("Autre...");
				}
				shell.close();
			}
		});
		redeployButton.setVisible(false);
	}
	
	public void addTableItems() {
		table.removeAll();
		ProActiveDescriptor pad = hashMap.get(combo.getText());
		VirtualNode[] vnodes = pad.getVirtualNodes();
		for(int i = 0 ; i < vnodes.length ; i++ ) {
			TableItem item = new TableItem(table, SWT.NONE, i);
			item.setText(vnodes[i].getName());
			System.out.println(vnodes[i].getName());
		}
		labelAndCheckComposite.setVisible(true);
		table.setVisible(true);
		table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		table.redraw();
		redeployButton.setVisible(true);
	}
	
}