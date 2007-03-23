package org.objectweb.proactive.ic2d.infrastructuremanager.views;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.infrastructuremanager.actions.NewViewAction;
import org.objectweb.proactive.ic2d.infrastructuremanager.dialog.IMDialogRedeploy;


public class IMViewAdministration extends ViewPart {

	public static final String ID = "org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewAdministration";

	private static IMViewAdministration imViewAdministration;
	private Shell shell;
	private Button deployButton, redeployButton, killButton;
	private Button connectionButton, creationButton, shutdownButton;
	private static List viewList;
	
	private static HashMap<String, IMViewInfrastructure> hashmap;
	
	@Override
	public void createPartControl(Composite p) {
		
		hashmap = new HashMap<String, IMViewInfrastructure>();
		
		GridLayout glv = new GridLayout(1, false);

		RowLayout rlh = new RowLayout(SWT.HORIZONTAL);
		rlh.justify = true;
		
		GridData line = new GridData();
		line.horizontalAlignment = GridData.FILL;
		line.grabExcessHorizontalSpace = true;
		
		RowData buttonSize = new RowData(80, 25);
		
		GridData listSize = new GridData();
		listSize.horizontalAlignment = GridData.FILL;
		listSize.verticalAlignment = GridData.FILL;
		listSize.grabExcessHorizontalSpace = true;
		listSize.grabExcessVerticalSpace = true;
		
		
		// IMViewAdministrationButtonListener
		IMViewAdministrationButtonListener myListener = new IMViewAdministrationButtonListener();
						
		shell = p.getShell();
		p.setLayout(new FillLayout());
		ScrolledComposite sc = new ScrolledComposite(p, SWT.H_SCROLL | SWT.V_SCROLL);
	    Composite child = new Composite(sc, SWT.NONE);
	    child.setLayout(glv);
		
		
		// Composite : connect, create, shutdown
		Composite comp = new Composite(child, SWT.NONE);
		comp.setLayout(rlh);
		comp.setLayoutData(line);
		connectionButton = new Button(comp, SWT.PUSH);
		connectionButton.setText("Connection");
		connectionButton.setToolTipText("Connect to an existing InfrastructureManager");
		connectionButton.setLayoutData(buttonSize);
		connectionButton.addSelectionListener(myListener);
		
		creationButton = new Button(comp, SWT.PUSH);
		creationButton.setText("Creation");
		creationButton.setToolTipText("Create a new InfrastructureManager");
		creationButton.setLayoutData(buttonSize);
		creationButton.addSelectionListener(myListener);
		
		shutdownButton = new Button(comp, SWT.PUSH);
		shutdownButton.setText("Shutdown");
		shutdownButton.setToolTipText("Shutdown an existing InfrastructureManager");
		shutdownButton.setLayoutData(buttonSize);
		shutdownButton.addSelectionListener(myListener);
		
		
		
		// Composite : IMViewInfrastructure List
		viewList = new List(child, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewList.setLayoutData(listSize);
		viewList.addSelectionListener(myListener);
		
	    
		// Composite : deploy, redeploy, kill
		comp = new Composite(child, SWT.NONE);
		comp.setLayout(rlh);
		comp.setLayoutData(line);
		deployButton = new Button(comp, SWT.PUSH);
		deployButton.setText("Load \u0026 Deploy");
		deployButton.setToolTipText("Load & Deploy a file descriptor");
		deployButton.setLayoutData(buttonSize);
		deployButton.addSelectionListener(myListener);

		redeployButton = new Button(comp, SWT.PUSH);
		redeployButton.setText("Redeploy");
		redeployButton.setToolTipText("Redeploy a file descriptor");
		redeployButton.setLayoutData(buttonSize);
		redeployButton.addSelectionListener(myListener);
		
		killButton = new Button(comp, SWT.PUSH);
		killButton.setText("Kill");
		killButton.setToolTipText("Kill a file descriptor");
		killButton.setLayoutData(buttonSize);
		killButton.addSelectionListener(myListener);
				
		
		sc.setContent(child);
		child.setSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.setMinSize(child.getSize().x, child.getSize().y);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		
	}

	public void setFocus() {
	}

	public static void selectViewInList(String partName) {
		viewList.select(viewList.indexOf(partName));
	}

	public static void addView(String name, IMViewInfrastructure view) {
		hashmap.put(name, view);
		updateList();
	}

	public static void removeView(String name) {
		hashmap.remove(name);
		updateList();
	}
		
	public static void updateList() {
		viewList.removeAll();
		for (String name : hashmap.keySet()) {
			viewList.add(name);			
		}
	}

	
	private class IMViewAdministrationButtonListener extends SelectionAdapter {
		
		public void widgetSelected(SelectionEvent e) {
			Console cons = Console.getInstance(IConsoleConstants.ID_CONSOLE_VIEW);
			
			if(e.widget.equals(connectionButton)) {
				// TODO
				cons.log("Action : Connection");
				new NewViewAction().run();
			}
			
			
			
			else if(e.widget.equals(creationButton)) {
				// TODO
				cons.log("Action : Creation");
				try {
					IMFactory.startLocal();
				} catch (NodeException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ActiveObjectCreationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (AlreadyBoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if(e.widget.equals(shutdownButton)) {
				// TODO
				cons.log("Action : Shutdown");
			}
			else if(e.widget.equals(deployButton)) {
				// TODO
				cons.log("Action : Deploy");
				String viewName = viewList.getItem(viewList.getSelectionIndex());
				cons.log(viewName);
				hashmap.get(viewName).deploy();
			}
			else if(e.widget.equals(redeployButton)) {
				// TODO
				cons.log("Action : Redeploy");
				new IMDialogRedeploy(shell);
			}
			else if(e.widget.equals(killButton)) {
				// TODO
				cons.log("Action : Kill");
			}
			
			else if (e.widget.equals(viewList)) {
				IWorkbenchPage iwp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewReference[] views =  iwp.getViewReferences();
				for(IViewReference view : views) {
					if (view.getPartName().equals(viewList.getSelection()[0])) {
						iwp.activate(view.getPart(true)); 
					}
				}
			}
		}
	}
	
}
