package org.objectweb.proactive.extensions.resourcemanager.gui.tree;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMInitialState;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener;

/**
 * @author FRADJ Johann
 */
public class ResourceExplorerTree extends TreeViewer implements RMNodeEventListener {

	public ResourceExplorerTree(ViewPart view, Composite parent) {
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		TreeManager.newInstance(view);
		this.setContentProvider(TreeManager.getInstance());
		this.setLabelProvider(TreeManager.getInstance());
		this.setInput(view.getViewSite());
		// this.setSorter(new NameSorter());
	}

	public void clear() {
		TreeManager.getInstance().clear();
		actualize();
	}

	public void initTree(RMInitialState initialState) {
		if (initialState != null) {
			TreeManager treeManager = TreeManager.getInstance();
			for (RMNodeSourceEvent nodeSourceEvent : initialState.getNodeSource()) {
				treeManager.addNodeSource(nodeSourceEvent);
			}

			for (RMNodeEvent nodeEvent : initialState.getBusyNodes()) {
				treeManager.addNode(nodeEvent);
			}

			for (RMNodeEvent nodeEvent : initialState.getDownNodes()) {
				treeManager.addNode(nodeEvent);
			}

			for (RMNodeEvent nodeEvent : initialState.getFreeNodes()) {
				treeManager.addNode(nodeEvent);
			}

			for (RMNodeEvent nodeEvent : initialState.getToReleaseNodes()) {
				treeManager.addNode(nodeEvent);
			}

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refresh();
					expandAll();
				}
			});
		}
	}

	/**
	 * @see org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener#nodeAddedEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
	 */
	public void nodeAddedEvent(RMNodeEvent nodeEvent) {
		actualize(TreeManager.getInstance().addNode(nodeEvent));
	}

	/**
	 * @see org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener#nodeRemovedEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
	 */
	public void nodeRemovedEvent(RMNodeEvent nodeEvent) {
		actualize(TreeManager.getInstance().removeNode(nodeEvent));
	}

	/**
	 * @see org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener#nodeBusyEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
	 */
	public void nodeBusyEvent(RMNodeEvent nodeEvent) {
		nodeStateChanged(nodeEvent);
	}

	/**
	 * @see org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener#nodeDownEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
	 */
	public void nodeDownEvent(RMNodeEvent nodeEvent) {
		nodeStateChanged(nodeEvent);
	}

	/**
	 * @see org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener#nodeFreeEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
	 */
	public void nodeFreeEvent(RMNodeEvent nodeEvent) {
		nodeStateChanged(nodeEvent);
	}

	/**
	 * @see org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener#nodeToReleaseEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeEvent)
	 */
	public void nodeToReleaseEvent(RMNodeEvent nodeEvent) {
		nodeStateChanged(nodeEvent);
	}

	/**
	 * @see org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener#nodeSourceAddedEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeSourceEvent)
	 */
	public void nodeSourceAddedEvent(RMNodeSourceEvent nodeSourceEvent) {
		TreeManager.getInstance().addNodeSource(nodeSourceEvent);
		actualize();
	}

	/**
	 * @see org.objectweb.proactive.extensions.resourcemanager.gui.interfaces.RMNodeEventListener#nodeSourceRemovedEvent(org.objectweb.proactive.extra.infrastructuremanager.common.RMNodeSourceEvent)
	 */
	public void nodeSourceRemovedEvent(RMNodeSourceEvent nodeSourceEvent) {
		TreeManager.getInstance().removeNodeSource(nodeSourceEvent);
		actualize();
	}

	private void nodeStateChanged(RMNodeEvent nodeEvent) {
		actualize(TreeManager.getInstance().changeNodeState(nodeEvent));
	}

	private void actualize() {
		actualize(null);
	}

	private void actualize(TreeLeafElement element) {
		final TreeLeafElement elem = element;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (elem == null)
					refresh(true);
				else {
					refresh(elem, true);
				}
			}
		});
	}

	// private class NameSorter extends ViewerSorter {}
}
