/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.compact;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.core.util.MutableInteger;
import org.ow2.proactive.resourcemanager.gui.compact.view.NodeView;
import org.ow2.proactive.resourcemanager.gui.compact.view.View;
import org.ow2.proactive.resourcemanager.gui.compact.view.ViewFractory;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeParentElement;


/**
 * Represents all resource manager infrastructure in compact matrix view.
 * It holds a tree of views which correspond to the resource manager data
 * model and displays them in matrix way.
 */
public class CompactViewer implements ISelectionProvider {

    // parent composite
    private Composite parent;
    // view composite
    private Composite composite;
    // selection manager (handle all selection routine)
    private SelectionManager selectionManager = new SelectionManager(this);
    // root of the represented tree
    private View rootView;
    // view position
    private int currentPosition = 0;
    // filter
    private Filter filter = new Filter();

    /**
     * Creates new CompactViewer.
     */
    public CompactViewer(final Composite parent) {
        this.parent = parent;
    }

    /**
     * Initialization of CompactViewer.
     */
    public void init() {
        initComposite();
        initLayout();
        loadMatrix();
    }

    /**
     * Initialization of composite.
     */
    private void initComposite() {
        parent.setLayout(new FillLayout());
        composite = new Composite(parent, SWT.NONE);
        Color white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        composite.setBackground(white);
    }

    /**
     * Layout initialization. SWT RowLayout is used to automatically
     * control resizing of the view.
     */
    private void initLayout() {
        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.spacing = 5;
        layout.fill = false;
        composite.setLayout(layout);
    }

    /**
     * Loads data from resource manager model.
     */
    public void loadMatrix() {
        if (RMStore.isConnected()) {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    rootView = createView(RMStore.getInstance().getModel().getRoot());
                    composite.layout();
                }
            });
        }
    }

    /**
     * Recursively creates graphical representation of the element and all its child.
     */
    private View createView(TreeLeafElement element) {
        View view = ViewFractory.createView(element, filter);
        view.setPosition(currentPosition);

        if (element instanceof TreeParentElement) {
            for (TreeLeafElement elem : ((TreeParentElement) element).getChildren()) {
                View v = createView(elem);
                v.setParent(view);
                view.getChilds().add(v);
            }
        } else {
            // updating position only for NodeView
            currentPosition++;
        }

        return view;
    }

    /**
     * Adds new element to the matrix. If the element is inserted into the middle
     * of the matrix reloads everything (otherwise it's too complex to reloads only affected elements).
     */
    public void addView(final TreeLeafElement element) {
        // checking if element is added into the middle of the matrix
        // if true -> reload everything

        final View parent = rootView.findView(element.getParent());

        if (parent == null || !lastElement(element)) {
            // full reload
            reloadMatrix();
        } else {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    synchronized (reloadRequests) {
                        // if reloadRequests > 0 don't do anything
                        // everything will be reloaded anyway
                        if (reloadRequests.getValue() == 0) {
                            // adding to the end of the matrix
                            View view = createView(element);
                            parent.getChilds().add(view);
                            view.setParent(parent);
                            composite.layout();
                        }
                    }
                }
            });
        }
    }

    // number of compact view reload requests
    MutableInteger reloadRequests = new MutableInteger(0);
    // thread that performs reloading
    Reloader reloader = new Reloader();

    /**
     * As reloading of the whole view is quite an expensive operation, so we
     * cannot do it each time when node is added to the view. In order to
     * optimize reloading especially during initial nodes deployment do it smoothly
     * from this thread.
     */
    private class Reloader extends Thread {

        private boolean stop = false;

        public Reloader() {
            start();
        }

        @Override
        public void run() {
            while (!stop) {
                int numberOfReloadRequests = 0;
                synchronized (reloadRequests) {
                    numberOfReloadRequests = reloadRequests.getValue();
                }

                if (numberOfReloadRequests == 0) {
                    try {
                        synchronized (reloadRequests) {
                            reloadRequests.wait();
                        }
                    } catch (InterruptedException e) {
                    }
                } else {
                    synchronized (reloadRequests) {
                        if (numberOfReloadRequests <= reloadRequests.getValue()) {
                            reloadRequests.setValue(reloadRequests.getValue() - numberOfReloadRequests);
                        }
                    }
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            clear();
                            loadMatrix();
                            composite.layout();
                        }
                    });
                    // sleeping for a while after full reloading
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public void stopThread() {
            stop = true;
            synchronized (reloadRequests) {
                reloadRequests.notifyAll();
            }
        }
    }

    private void reloadMatrix() {

        synchronized (reloadRequests) {
            reloadRequests.add(1);
            reloadRequests.notifyAll();
        }
    }

    /**
     * Checks if elements is correspond the latest view in the matrix.
     */
    private boolean lastElement(TreeLeafElement element) {
        TreeParentElement parent = element.getParent();

        if (parent == null) {
            return true;
        }

        if (parent.getChildren()[parent.getChildren().length - 1] == element) {
            return lastElement(parent);
        }

        return false;
    }

    /**
     * Removes element from the matrix. Disposes all required elements and
     * recalculates all positions in the tree.
     */
    public void removeView(final TreeLeafElement element) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                selectionManager.deselectAll();

                View view = rootView.findView(element);
                if (view != null) {
                    view.dispose();

                    view.getParent().getChilds().remove(view);

                    View root = view;
                    while (root.getParent() != null) {
                        root = root.getParent();
                    }
                    currentPosition = 0;
                    updatePositions(root);
                }
                composite.layout();
            }
        });
    }

    /**
     * Updates all view's positions in the tree.
     */
    private void updatePositions(View view) {
        view.setPosition(currentPosition);
        if (view instanceof NodeView) {
            currentPosition++;
        }
        for (View v : view.getChilds()) {
            updatePositions(v);
        }
    }

    /**
     * Reloads state of the element. Finds all nodes of this element
     * and updates their states.
     */
    public void updateView(final TreeLeafElement element) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    View view = rootView.findView(element);
                    if (view != null) {
                        view.update();
                    }
                    composite.layout();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    /**
     * Finds subset of nodes between specified positions.
     * Used in selection manager.
     */
    public List<NodeView> subset(int position, int position2) {
        int min = position < position2 ? position : position2;
        int max = position < position2 ? position2 : position;

        return rootView.getAllNodeViews().subList(min, max + 1);
    }

    /**
     * Clears view. Removes all elements and resets internal states.
     */
    public void clear() {
        selectionManager.deselectAll();
        rootView.dispose();
        currentPosition = 0;
    }

    public Composite getComposite() {
        return composite;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    /**
     * Dummy implementation of ISelectionProvider
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    }

    /**
     * Dummy implementation of ISelectionProvider
     */
    public ISelection getSelection() {
        return null;
    }

    /**
     * Dummy implementation of ISelectionProvider
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    }

    /**
     * Dummy implementation of ISelectionProvider
     */
    public void setSelection(ISelection selection) {
    }

    public void dispose() {
        reloader.stopThread();
    }

    public void repaint(Filter filter) {
        this.filter = filter;
        reloadMatrix();
    }
}
