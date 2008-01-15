package org.objectweb.proactive.extensions.resourcemanager.gui.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.gui.views.StatisticsView;


/**
 * @author FRADJ Johann
 */
public class TreeManager extends LabelProvider implements IStructuredContentProvider, ITreeContentProvider {

    private static TreeManager instance = null;
    private ViewPart view = null;
    private Root root = null;
    private TreeStatistic statistic = null;

    private TreeManager(ViewPart view) {
        this.view = view;
        this.root = new Root();
        this.statistic = new TreeStatistic();
    }

    public static void newInstance(ViewPart view) {
        instance = new TreeManager(view);
    }

    public static TreeManager getInstance() {
        return instance;
    }

    public TreeStatistic getStatistic() {
        return statistic;
    }

    public void clear() {
        this.root = new Root();
        this.statistic = new TreeStatistic();
    }

    public String[] getSourcesNames(boolean dynamicToo, boolean staticToo, boolean defaultToo) {
        TreeLeafElement[] children = root.getChildren();
        List<String> res = new ArrayList<String>();
        for (TreeLeafElement leaf : children) {
            Source src = (Source) leaf;
            if (dynamicToo && src.isDynamic()) {
                res.add(src.getName());
            } else if (staticToo && src.isStatic()) {
                if (src.isTheDefault()) {
                    if (defaultToo) {
                        res.add(src.getName());
                    }
                } else {
                    res.add(src.getName());
                }
            }
        }
        String[] tmp = new String[res.size()];
        res.toArray(tmp);
        Arrays.sort(tmp);
        return tmp;
    }

    public String[] getNodesNames() {
        List<String> res = new ArrayList<String>();

        // FIXME A synchronize more efficient may be done ?

        synchronized (root) {
            for (TreeLeafElement src : root.getChildren()) {
                TreeParentElement source = (TreeParentElement) src;
                for (TreeLeafElement hst : source.getChildren()) {
                    TreeParentElement host = (TreeParentElement) hst;
                    for (TreeLeafElement jvms : host.getChildren()) {
                        TreeParentElement jvm = (TreeParentElement) jvms;
                        for (TreeLeafElement node : jvm.getChildren()) {
                            res.add(node.getName());
                        }
                    }
                }
            }
        }

        String[] tmp = new String[res.size()];
        res.toArray(tmp);
        Arrays.sort(tmp);
        return tmp;
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object parent) {
        if (parent.equals(view.getViewSite())) {
            return getChildren(root);
        }
        return getChildren(parent);
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object child) {
        if (child instanceof TreeLeafElement) {
            return ((TreeLeafElement) child).getParent();
        }
        return null;
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parent) {
        if (parent instanceof TreeParentElement) {
            return ((TreeParentElement) parent).getChildren();
        }
        return new Object[0];
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object parent) {
        if (parent instanceof TreeParentElement)
            return ((TreeParentElement) parent).hasChildren();
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object obj) {
        return obj.toString();
    }

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object obj) {
        switch (((TreeLeafElement) obj).getType()) {
            case HOST:
                return ImageDescriptor.createFromFile(this.getClass(), "icons/host.gif").createImage();
            case NODE:
                switch (((Node) obj).getState()) {
                    case DOWN:
                        return ImageDescriptor.createFromFile(this.getClass(), "icons/down.gif")
                                .createImage();
                    case FREE:
                        return ImageDescriptor.createFromFile(this.getClass(), "icons/free.gif")
                                .createImage();
                    case BUSY:
                        return ImageDescriptor.createFromFile(this.getClass(), "icons/busy.gif")
                                .createImage();
                    case TO_BE_RELEASED:
                        return ImageDescriptor.createFromFile(this.getClass(), "icons/to_release.gif")
                                .createImage();
                }
                break;
            case SOURCE:
                return ImageDescriptor.createFromFile(this.getClass(), "icons/source.gif").createImage();
            case VIRTUAL_MACHINE:
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
        return null;
    }

    public TreeParentElement addNode(RMNodeEvent nodeEvent) {
        TreeParentElement parentToRefresh = null;
        synchronized (root) {

            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());
            // the source cannot be null

            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            if (host == null) { // if the host is null, then add it
                host = new Host(nodeEvent.getHostName());
                source.addChild(host);
                if (parentToRefresh == null)
                    parentToRefresh = source;
            }
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());
            if (vm == null) { // if the vm is null, then add it
                vm = new VirtualMachine(nodeEvent.getVMName());
                host.addChild(vm);
                if (parentToRefresh == null)
                    parentToRefresh = host;
            }
            vm.addChild(new Node(nodeEvent.getNodeUrl(), nodeEvent.getState()));

            if (parentToRefresh == null)
                parentToRefresh = vm;

            switch (nodeEvent.getState()) {
                case BUSY:
                case TO_BE_RELEASED:
                    statistic.increaseBusyNodes();
                    break;
                case DOWN:
                    statistic.increaseDownNodes();
                    break;
                case FREE:
                    statistic.increaseFreeNodes();
                    break;
            }
            refreshStatisticsView();
        }
        return parentToRefresh;
    }

    public TreeParentElement removeNode(RMNodeEvent nodeEvent) {
        TreeParentElement parentToRefresh = null;
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());
            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());

            remove(vm, nodeEvent.getNodeUrl());
            parentToRefresh = vm;

            if (vm.getChildren().length == 0) {
                remove(host, nodeEvent.getVMName());
                parentToRefresh = host;

                if (host.getChildren().length == 0) {
                    remove(source, nodeEvent.getHostName());
                    parentToRefresh = source;
                }
            }

            switch (nodeEvent.getState()) {
                case BUSY:
                case TO_BE_RELEASED:
                    statistic.decreaseBusyNodes();
                    break;
                case DOWN:
                    statistic.decreaseDownNodes();
                    break;
                case FREE:
                    statistic.decreaseFreeNodes();
                    break;
            }
            refreshStatisticsView();
        }

        return parentToRefresh;
    }

    public TreeLeafElement changeNodeState(RMNodeEvent nodeEvent) {
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeEvent.getNodeSource());
            TreeParentElement host = (TreeParentElement) find(source, nodeEvent.getHostName());
            TreeParentElement vm = (TreeParentElement) find(host, nodeEvent.getVMName());
            Node node = (Node) find(vm, nodeEvent.getNodeUrl());

            switch (node.getState()) {
                case BUSY:
                case TO_BE_RELEASED:
                    statistic.decreaseBusyNodes();
                    break;
                case DOWN:
                    statistic.decreaseDownNodes();
                    break;
                case FREE:
                    statistic.decreaseFreeNodes();
                    break;
            }

            node.setState(nodeEvent.getState());

            switch (node.getState()) {
                case BUSY:
                case TO_BE_RELEASED:
                    statistic.increaseBusyNodes();
                    break;
                case DOWN:
                    statistic.increaseDownNodes();
                    break;
                case FREE:
                    statistic.increaseFreeNodes();
                    break;
            }
            refreshStatisticsView();
            return node;
        }
    }

    public void addNodeSource(RMNodeSourceEvent nodeSourceEvent) {
        synchronized (root) {
            TreeParentElement source = (TreeParentElement) find(root, nodeSourceEvent.getSourceName());
            if (source == null) {
                source = new Source(nodeSourceEvent.getSourceName(), nodeSourceEvent.getSourceType());
                root.addChild(source);
            }
        }
    }

    public void removeNodeSource(RMNodeSourceEvent nodeSourceEvent) {
        synchronized (root) {
            for (TreeLeafElement n : root.getChildren()) {
                if (n.getName().equals(nodeSourceEvent.getSourceName())) {
                    root.removeChild(n);
                    break;
                }
            }
        }
    }

    private TreeLeafElement find(TreeParentElement parent, String name) {
        for (TreeLeafElement child : parent.getChildren())
            if (child.getName().equals(name))
                return child;
        return null;
    }

    private void remove(TreeParentElement parent, String name) {
        for (TreeLeafElement child : parent.getChildren())
            if (child.getName().equals(name)) {
                parent.removeChild(child);
                break;
            }
    }

    private void refreshStatisticsView() {
        StatisticsView view = StatisticsView.getInstance();
        if (view != null)
            view.maj();
    }
}