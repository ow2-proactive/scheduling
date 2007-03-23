package org.objectweb.proactive.ic2d.infrastructuremanager.dialog;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
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
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.ic2d.infrastructuremanager.IMConstants;

public class IMDialogDeploy  extends Dialog {

	private final String TITLE = "Deploy";
	private Shell shell;
	private Button deployButton;
	
	private SelectionAdapter selectionAdapter;
	private Button chooseFileButton;
	private FileDialog fileDialog;
	private Text filePath;
	private Table table;
	private Composite labelAndCheckComposite;
	private IMAdmin admin;
	
	public IMDialogDeploy(Shell parent, IMAdmin admin) {
		// Pass the default styles here
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				
		this.admin = admin;
		
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

		// Choix du fichier
		fileDialog = new FileDialog(shell, SWT.SINGLE);
		fileDialog.setFilterExtensions(new String[]{"*.xml"});
		fileDialog.setFilterNames(new String[]{ "FileDescriptor .xml"});		
		
		// SelectionAdapter
		selectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				filePath.setText(fileDialog.open());
				addTableItems();
				shell.setSize(500, 400);
				shell.redraw();
			}
		};
		
		// FileChooser Button & Text
		addCompositeFileChooser();
		addLabelAndCheckAll();
		addTable();
		addDeployButton();
		
		shell.setSize(500, 100);
		shell.open();

		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
		
	}
	
	public void addCompositeFileChooser() {
		Composite compositeFileChooser = new Composite(shell, SWT.NONE);
		compositeFileChooser.setLayout(new GridLayout(2, false));
		chooseFileButton = new Button(compositeFileChooser, SWT.PUSH);
		chooseFileButton.setText("Choose File");
		chooseFileButton.addSelectionListener(selectionAdapter);
		filePath = new Text(compositeFileChooser, SWT.NONE);
		filePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		compositeFileChooser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
	
	public void addDeployButton() {
		deployButton = new Button(shell, SWT.PUSH);
		deployButton.setText("Deploy");
		deployButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		deployButton.addSelectionListener(new SelectionAdapter() {
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
					// TODO
				}
				else if (nbChecked == 1) {
					System.out.println("One check ...");
					// TODO
					try {
						admin.deployAllVirtualNodes(new File(filePath.getText()), IMConstants.nodeTransfert);
					} 
					catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				else if (nbChecked == size) {
					System.out.println("All check ...");
					try {
						admin.deployAllVirtualNodes(new File(filePath.getText()), IMConstants.nodeTransfert);
					} 
					catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				else {
					System.out.println("Autre...");
					// TODO
				}
				shell.close();
			}
		});
		deployButton.setVisible(false);
	}
	
	public void addTableItems() {
		try {
			table.removeAll();
			ProActiveDescriptor pad = ProActive.getProactiveDescriptor(filePath.getText());
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
			deployButton.setVisible(true);
		}
		catch (ProActiveException e1) {
			e1.printStackTrace();
		}
	}
	
}