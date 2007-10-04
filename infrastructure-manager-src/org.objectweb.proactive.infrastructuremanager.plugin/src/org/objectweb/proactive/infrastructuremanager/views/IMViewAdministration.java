/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.infrastructuremanager.views;

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
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.infrastructuremanager.actions.NewViewAction;


public class IMViewAdministration extends ViewPart {
    public static final String ID = "org.objectweb.proactive.infrastructuremanager.gui.views.IMViewAdministration";
    private Button deployButton;
    private Button redeployButton;
    private Button killButton;
    private Button connectionButton;
    private Button creationButton;
    private Button shutdownButton;
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

        p.setLayout(new FillLayout());
        ScrolledComposite sc = new ScrolledComposite(p,
                SWT.H_SCROLL | SWT.V_SCROLL);
        Composite child = new Composite(sc, SWT.NONE);
        child.setLayout(glv);

        // Composite : connect, create, shutdown
        Composite comp = new Composite(child, SWT.NONE);
        comp.setLayout(rlh);
        comp.setLayoutData(line);
        connectionButton = new Button(comp, SWT.PUSH);
        connectionButton.setText("Connection");
        connectionButton.setToolTipText(
            "Connect to an existing Infrastructure Manager");
        connectionButton.setLayoutData(buttonSize);
        connectionButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Console.getInstance()
                           .log("Connect to an existing Infrastructure Manager");
                    new NewViewAction().run();
                }
            });

        creationButton = new Button(comp, SWT.PUSH);
        creationButton.setText("Creation");
        creationButton.setToolTipText("Create a new Infrastructure Manager");
        creationButton.setLayoutData(buttonSize);
        creationButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    try {
                        Console.getInstance()
                               .log("Create a new Infrastructure Manager");
                        IMFactory.startLocal();
                        new NewViewAction().run();
                    }
                    // TODO Auto-generated catch block
                    catch (NodeException e1) {
                        e1.printStackTrace();
                    } catch (ActiveObjectCreationException e1) {
                        e1.printStackTrace();
                    } catch (AlreadyBoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

        shutdownButton = new Button(comp, SWT.PUSH);
        shutdownButton.setText("Shutdown");
        shutdownButton.setToolTipText(
            "Shutdown an existing Infrastructure Manager");
        shutdownButton.setLayoutData(buttonSize);
        shutdownButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Console.getInstance()
                           .warn("Shutdown an existing Infrastructure Manager");
                    String viewName = viewList.getItem(viewList.getSelectionIndex());
                    hashmap.get(viewName).shutdown();
                    Console.getInstance().warn("TODO : Dispose the view");
                }
            });

        // Composite : IMViewInfrastructure List
        viewList = new List(child,
                SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        viewList.setLayoutData(listSize);
        viewList.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    IWorkbenchPage iwp = PlatformUI.getWorkbench()
                                                   .getActiveWorkbenchWindow()
                                                   .getActivePage();
                    IViewReference[] views = iwp.getViewReferences();
                    for (IViewReference view : views) {
                        if (view.getPartName().equals(viewList.getSelection()[0])) {
                            iwp.activate(view.getPart(true));
                        }
                    }
                }
            });

        // Composite : deploy, redeploy, kill
        comp = new Composite(child, SWT.NONE);
        comp.setLayout(rlh);
        comp.setLayoutData(line);
        deployButton = new Button(comp, SWT.PUSH);
        deployButton.setText("Deploy");
        deployButton.setToolTipText("Load & Deploy a file descriptor");
        deployButton.setLayoutData(buttonSize);
        deployButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Console.getInstance()
                           .log("Load and Deploy a file descriptor");
                    String viewName = viewList.getItem(viewList.getSelectionIndex());
                    hashmap.get(viewName).deploy();
                }
            });

        redeployButton = new Button(comp, SWT.PUSH);
        redeployButton.setText("Redeploy");
        redeployButton.setToolTipText("Redeploy a file descriptor");
        redeployButton.setLayoutData(buttonSize);
        redeployButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Console.getInstance().log("Redeploy a file descriptor");
                    String viewName = viewList.getItem(viewList.getSelectionIndex());
                    hashmap.get(viewName).redeploy();
                }
            });

        killButton = new Button(comp, SWT.PUSH);
        killButton.setText("Kill");
        killButton.setToolTipText("Kill a file descriptor");
        killButton.setLayoutData(buttonSize);
        killButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Console.getInstance().log("Kill a file descriptor");
                    String viewName = viewList.getItem(viewList.getSelectionIndex());
                    hashmap.get(viewName).kill();
                }
            });

        sc.setContent(child);
        child.setSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        sc.setMinSize(child.getSize().x, child.getSize().y);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
    }

    @Override
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
}
