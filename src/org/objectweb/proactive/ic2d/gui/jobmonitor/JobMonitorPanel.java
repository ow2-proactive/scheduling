package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.*;
import java.awt.event.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntimeAdapter;
import org.objectweb.proactive.ic2d.gui.IC2DGUIController;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.*;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.Switcher;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitcherModel;

public class JobMonitorPanel extends JPanel implements JobMonitorConstants
{
	private static final String PA_JVM = "PA_JVM";
	
	private static final String VN_VIEW_LABEL = "Job view / Virtual Nodes";
	private static final String JOB_VIEW_LABEL = "Job view / Hosts";
	private static final String HOST_VIEW_LABEL = "Host view";
	
	private static final String JOB_VIEW_ROOT = "Jobs";
	private static final String HOST_VIEW_ROOT = "Hosts";
	
	private static final int DEFAULT_RMI_PORT = Registry.REGISTRY_PORT;
	
	private static final String EXTRACT_MENU_LABEL = "Extract view to a new window";
	
	private static final String [] JOB_VIEW_KEYS 	= {JOB, HOST, JVM, VN, NODE, AO};
	private static final String [] HOST_VIEW_KEYS = {HOST, JOB, JVM, VN, NODE, AO};
	private static final String [] VN_VIEW_KEYS	 	= {JOB, VN, HOST, JVM, NODE, AO};
	
	private IC2DGUIController controller;
	
	private JTabbedPane tabs;
	private Vector frames;
	
	private DataModelNode modelRoot = DataModelNode.createModelRootInstance ();
	private DataTreeModel jobViewModel;
	private DataTreeModel vnViewModel;
	private DataTreeModel hostViewModel;
	
	private Map aos;
	private Vector monitoredHosts;
	private Vector filteredJobs;
	
	private JPopupMenu popupmenu;
	
	private Thread refresher;
	private volatile boolean refresh = true;
	
	private int ttr = 60;
	
	public JobMonitorPanel (IC2DGUIController _controller)
	{		
		setLayout (new GridLayout (1, 1));
		
		controller = _controller;
		
		createRefresher ();
		
		aos = new HashMap();
		monitoredHosts = new Vector();
		filteredJobs = new Vector();
		filteredJobs.add (ProActive.getJobId());
		
		tabs = new JTabbedPane();
		frames = new Vector();
		
		add (tabs);

		final JPopupMenu extractMenu = new JPopupMenu();
		JMenuItem extract = new JMenuItem (EXTRACT_MENU_LABEL);
		extract.addActionListener (new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final Component tab = tabs.getSelectedComponent();
				final String title = tabs.getTitleAt (tabs.getSelectedIndex());
				
				final JFrame f = new JFrame (title);
				f.setSize (tab.getSize());
				Container c = f.getContentPane();
				c.setLayout (new GridLayout (1, 1));
				c.add (tab);
				
				f.addWindowListener (new WindowAdapter()
				{
					public void windowClosing (WindowEvent e)
					{
						tabs.addTab (title, tab);
						frames.remove (f);
					}
				});
				
				frames.add (f);
				
				f.show();
			}
		});
		
		extractMenu.add (extract);
		
		tabs.addTab (JOB_VIEW_LABEL, createJobView());
		tabs.addTab (VN_VIEW_LABEL, createVNView());
		tabs.addTab (HOST_VIEW_LABEL, createHostView());

		tabs.addMouseListener (new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					Component menu = extractMenu.getComponent(0); 
					if (tabs.getTabCount() > 1)
					{
						if (! menu.isEnabled())
							menu.setEnabled (true);
					}
					else
					{
						if (menu.isEnabled())
							menu.setEnabled (false);
					}
					
					extractMenu.show (tabs, e.getX(), e.getY());
				}
			}
		});	
	}
	
	private void createRefresher ()
	{
		refresh = true;
		refresher = new Thread (new Runnable()
		{
			public void run ()
			{
//				System.out.println ("Start of refresher thread");
				while (refresh)
				{
					try
					{		
//						System.out.println ("Waiting for refresh - ttr = " + ttr + " seconds");
						Thread.sleep (ttr * 1000);
//						System.out.println ("Automatic refresh starting");
						handleHosts();
					}
					catch (InterruptedException e)
					{
//						e.printStackTrace();
					}
				}
//				System.out.println ("Stop of refresher thread");
			}
		});
	}

	protected void finalize () throws Throwable
	{
		stopRefreshing ();
		super.finalize ();
	}
	
	private void stopRefreshing ()
	{
		if (refresh)
		{
//			System.out.println ("Stoppping refresher thread");
			refresh = false;
			refresher.interrupt();
		}
	}

	public int getTtr ()
	{
		return ttr;
	}
	
	public void setTtr (int _ttr)
	{
		ttr = _ttr;
	}
	
	public void addMonitoredHost (String host)
	{
		if (! monitoredHosts.contains (host))
		{
			monitoredHosts.add (host);
			if (monitoredHosts.size() == 1)
				refresher.start();
		}
		
//		System.out.println ("There are now " + monitoredHosts.size() + " monitored hosts");
	}

	public void updateHosts ()
	{
		new Thread (new Runnable()
		{
			public void run ()
			{
				handleHosts ();
			}
		}).start();
	}
	
	void hideOwnedFrames()
	{
//		System.out.println("Hiding frames: " + frames.size());
		hideOrShow (true);
		stopRefreshing ();
	}

	void showOwnedFrames()
	{
//		System.out.println("Showing frames: " + frames.size());
		if (! monitoredHosts.isEmpty())
		{
			createRefresher ();
			refresher.start();
		}
		hideOrShow (false);
	}	
	
	private void handleHosts ()
	{
		((DataModelNode) vnViewModel.getRoot()).removeAllChildren();
		((DataModelNode) jobViewModel.getRoot()).removeAllChildren();
		((DataModelNode) hostViewModel.getRoot()).removeAllChildren();
		
		synchronized (monitoredHosts)
		{
			for (int i = 0, size = monitoredHosts.size(); i < size; ++i)
			{
				String host = (String) monitoredHosts.get (i);
//				System.out.println ("\nMonitoring host " + (i + 1) + " / " + size + ": " + host);
				handleHost (host);
			}
		}
	
		vnViewModel.update();
        jobViewModel.update();
        hostViewModel.update();
	}
	
	public void updateHost (final String host)
	{
		new Thread (new Runnable()
		{
			public void run ()
			{
				vnViewModel.removeHostChildren (host);
				jobViewModel.removeHostChildren (host);
				hostViewModel.removeHostChildren (host);
				
				handleHost (host);
				
				vnViewModel.update();
		        jobViewModel.update();
		        hostViewModel.update();
			}
		}).start();
	}
	
	private void handleHost (String host)
	{
		String hostname = host;
        int port = DEFAULT_RMI_PORT;
        int pos = host.lastIndexOf (":");
        if (pos != -1)
        {
            // if the hostname is host:port
            try
			{
                port = Integer.parseInt (host.substring (1 + pos));
            }
            catch (NumberFormatException e)
			{
                port = DEFAULT_RMI_PORT;
            }
            
            hostname = host.substring (0, pos);
        }
        
        handleHost (hostname, port);
	}
	
	private void handleHost (String hostname, int port)
	{
		try
		{
			Registry registry = LocateRegistry.getRegistry (hostname, port);
			String [] list = registry.list();
				
			for (int idx = 0; idx < list.length; ++idx)
			{
				String id = list [idx];
				if (id.indexOf (PA_JVM) != -1)
				{
					RemoteProActiveRuntime r = (RemoteProActiveRuntime) registry.lookup (id);
//					System.out.println("Found runtime id: " + id);

					List x = new ArrayList();
					try
					{
						ProActiveRuntime part = new RemoteProActiveRuntimeAdapter (r);
						x.add (part);

						ProActiveRuntime [] runtimes = r.getProActiveRuntimes();
						x.addAll (Arrays.asList (runtimes));
						
//						System.out.println ("Found " + runtimes.length + " ProActiveRuntimes in this RemoteProActiveRuntime");
						
						for (int i = 0, size = x.size(); i < size; ++i)
						{
//							System.out.println ("\nRuntime " + (i + 1) + " / " + size);
							handleProActiveRuntime ((ProActiveRuntime) x.get (i));
						}
					}
					catch (ProActiveException e)
					{
//						System.out.println ("Unexpected ProActive exception caught while obtaining runtime reference from the RemoteProActiveRuntime instance - The RMI reference might be dead: " + e);
//						e.printStackTrace();
					}
					catch (RemoteException e)
					{
//						System.out.println ("Unexpected remote exception caught while getting proactive runtimes: " + e);
//						e.printStackTrace();
					}
				}
			}
		}
		catch (RemoteException e)
		{
//			System.out.println("Unexpected exception caught while getting registry reference: " + e);
//			e.printStackTrace();
		}
		catch (NotBoundException e)
		{
//			System.out.println("Unexpected not bound exception caught while looking up object reference: " + e);
//			e.printStackTrace();
		}
	}

	public boolean isJobFiltered (String jobId)
	{
		for (int i = 0, size = filteredJobs.size(); i < size; ++i)
		{
			String job = (String) filteredJobs.get (i);
			if (job.equalsIgnoreCase (jobId))
				return true;
		}
		
		return false;
	}
	
	private void handleProActiveRuntime (ProActiveRuntime pr) throws ProActiveException
	{
		if (isJobFiltered (pr.getJobID()))
			return;
		
//		System.out.println ("Runtimes: " + pr.getProActiveRuntimes().length);
		
//		System.out.println ("Job id: " + pr.getJobID() + " - vm info [job id: " + pr.getVMInformation().getJobID()
//				+ ", vm name: " + pr.getVMInformation().getName() + "]");
	
		String jobId = pr.getJobID();
		String hostname = pr.getVMInformation().getInetAddress().getCanonicalHostName();
		String vmName = pr.getVMInformation().getName();
			
//		Prepare root data
		DataModelNode jobA = modelRoot.getChild (jobId, JOB);		
		DataModelNode hostA = modelRoot.getChild (hostname, HOST);
		
		DataModelNode hostB = jobA.getChild (hostname, HOST);		
		DataModelNode jobB = hostA.getChild (jobId, JOB);		
		
//		Prepare data for Job view
		DataModelNode vmA = hostB.getChild (vmName, JVM);
		
//		Prepare data for Host view
		DataModelNode vmB = jobB.getChild (vmName, JVM);
				
//		Fill data
		handleLocalNodes (pr, vmA, vmB, jobA);
	}
	
	private void handleLocalNodes (ProActiveRuntime pr, DataModelNode vmA, DataModelNode vmB, DataModelNode job) throws ProActiveException
	{
		String hostname = pr.getVMInformation().getInetAddress().getCanonicalHostName();
		String vmName = pr.getVMInformation().getName();
		
		String [] nodes = pr.getLocalNodeNames();
//		System.out.println ("Found " + nodes.length + " nodes on this runtime");
		for (int i = 0; i < nodes.length; ++i)
		{
			String nodeName = nodes [i];
			String vnName = pr.getVNName (nodeName);

			if (vnName == null)
				vnName = "NoVirtualNodeName";

//			System.out.println ("node " + (i + 1) + " / " + nodes.length + ": " + nodes [i] + 	" - vn name: " + vnName);
			
			ArrayList activeObjects = null;
			try
			{
				activeObjects = pr.getActiveObjects (nodeName);
			}
			catch (ProActiveException e)
			{
				controller.log ("Unexpected ProActive exception caught while obtaining the active objects list", e);
			}
			
			DataModelNode nodeVmA = handleVMNode (nodeName, vnName, vmA);
			if (activeObjects != null)
				handleActiveObjects (nodeVmA, activeObjects);

			DataModelNode nodeVmB = handleVMNode (nodeName, vnName, vmB);
			if (activeObjects != null)
				handleActiveObjects (nodeVmB, activeObjects);

			DataModelNode nodeJob = handleJobNode (hostname, vmName, nodeName, vnName, job);
			if (activeObjects != null)
				handleActiveObjects (nodeJob, activeObjects);
		}
	}

	private DataModelNode handleJobNode (String hostname, String vmName, String nodeName, String vnName, DataModelNode job)
	{
		DataModelNode vn = job.getChild (vnName, VN);
		DataModelNode host = vn.getChild (hostname, HOST);
		DataModelNode vm =	 host.getChild (vmName, JVM);
		DataModelNode node = 	vm.getChild (nodeName, NODE);
		return node;
	}

	private DataModelNode handleVMNode (String nodeName, String vnName, DataModelNode vm)
	{
		DataModelNode vn = vm.getChild (vnName, VN);
		DataModelNode node = 	vn.getChild (nodeName, NODE);
		return node;
	}

	private void handleActiveObjects (DataModelNode node, ArrayList activeObjects)
	{
		for (int i = 0, size = activeObjects.size(); i < size; ++i)
		{
			ArrayList aoWrapper = (ArrayList) activeObjects.get (i);
			RemoteBodyAdapter rba = (RemoteBodyAdapter) aoWrapper.get (0);					
			
//			System.out.println ("Active object " + (i + 1) + " / " + size + " class: " + aoWrapper.get (1));
			
			String className = (String) aoWrapper.get (1);
			if (className.equalsIgnoreCase ("org.objectweb.proactive.ic2d.spy.Spy"))
				continue;
			
			className = className.substring (className.lastIndexOf (".") + 1);			
			String aoName = (String) aos.get (rba.getID());
			if (aoName == null)
			{
				aoName = className + "#" + (aos.size() + 1);
				aos.put (rba.getID(), aoName);
			}
			
			DataModelNode ao = node.getChild (aoName, AO);
		}
	}
	
	private void dump (Object o)
	{
		System.out.println ("<object class='" + o.getClass() + "'>");
		System.out.println (o.toString());
		System.out.println ("</object>");
		System.out.println ();
	}

	private void hideOrShow (boolean hide)
	{
		for (int i = 0, size = frames.size(); i < size; ++i)
		{
			JFrame f = (JFrame) frames.get (i);
			if (hide)
				f.hide();
			else
				f.show();
		}
	}
	
	private boolean constructPopupMenu (final DataModelNode node)
	{
		boolean showMenu = false;
		
		if (popupmenu == null)
			popupmenu = new JPopupMenu ();
	    	
	    popupmenu.removeAll();
	    
	    if (node == null)
	    {
	    	
	    	AbstractAction a = new AbstractAction ("Refresh monitoring tree")
			{
	    		public void actionPerformed (ActionEvent e)
				{
//	    			System.out.println("Asking for a global refresh");
	    			updateHosts();
				}
			};
	    	
	    	JMenuItem treeMenu = new JMenuItem (a);
	    	treeMenu.setEnabled (monitoredHosts.size() > 0);
	    	popupmenu.add (treeMenu);
	    	
	    	showMenu = true;
	    }
	    else
	    {
	    	String key = node.getKey();
	    	
	    	AbstractAction a = null;
	    	if (key == HOST)
	    	{
	    		a = new AbstractAction ("Refresh host")
				{
					public void actionPerformed (ActionEvent e)
					{
//						System.out.println ("Asking for a host refresh: " + node.getName());
						updateHost (node.getName());
					}
				};
	    	}
	    	else if (key == JOB)
	    	{
	    		a = new AbstractAction ("Stop monitoring this job")
				{
					public void actionPerformed (ActionEvent e)
					{
						String job = node.getName();
//						System.out.println ("Asking for a job to be added to the filtered jobs list: " + job);
						filteredJobs.add (job);
						
						// remove job from tree
					}
				};
	    	}
	    	
	    	if (a != null)
	    	{
	    		JMenuItem nodeMenu = new JMenuItem (a);
	    		popupmenu.add (nodeMenu);
	    		
	    		showMenu = true;
	    	}
	    }
	    
	    return showMenu;
	}
	
	private Container createContent (DataTreeModel model)
	{
		//JSplitPane sp = new JSplitPane ();
		//sp.setOneTouchExpandable (true);
		
		JPanel left = new JPanel (new BorderLayout());
		//JPanel right = new JPanel ();
		
		//sp.setLeftComponent (left);
		//sp.setRightComponent (right);
			
		Switcher s = new Switcher (model.getSwitcherModel());
		JPanel switcher = new JPanel (new GridLayout (1, 1));
		switcher.add (s);
		switcher.setBorder (BorderFactory.createEtchedBorder());
		left.add (switcher, BorderLayout.NORTH);
		
		final JTree j = new JTree (model);
		j.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
		j.setCellRenderer (new JobMonitorTreeCellRenderer());
		
		JScrollPane pane = new JScrollPane (j);
		left.add (pane, BorderLayout.CENTER);
		
		j.addMouseListener (new MouseAdapter ()
		{	
		    public void mousePressed (MouseEvent e)
		    {
		        if (e.isPopupTrigger())
		        {		        	
		        	TreePath selPath = j.getPathForLocation (e.getX(), e.getY());		          
		        	DataModelNode node = null;
		          
		        	if (selPath != null)
		        	{
		        		node = (DataModelNode) selPath.getLastPathComponent();
		        		
				        if (node == null)
				        	return;
				        
				        if (node == vnViewModel.getRoot() || node == jobViewModel.getRoot() || node == hostViewModel.getRoot())
				        	node = null;
				        
				        j.setSelectionPath (selPath);
		        	}
			          
			        if (constructPopupMenu (node))
			        	popupmenu.show (j, e.getX(), e.getY());
		        }
		    }
		});
		
		return left;
	}
	
	private JPanel createPanel (DataTreeModel model)
	{
		JPanel p = new JPanel (new GridLayout (1, 1));
		p.add (createContent (model));
		return p;
	}
	
	private JPanel createJobView ()
	{
		SwitcherModel jobSwitchModel = new SwitcherModel (new String [] {"Job", "Host", "JVM", "VN", "Node", "AO"}, JOB_VIEW_KEYS);
		NodeHelper helper = new NodeHelper (jobSwitchModel);
		
		DataModelTraversal traversal = new DataModelTraversal (JOB_VIEW_KEYS);
		DataModelRoot root = new DataModelRoot (JOB_VIEW_ROOT, traversal, helper, modelRoot);	
		
		jobViewModel = new DataTreeModel (root, jobSwitchModel, traversal, helper);
		
		return createPanel (jobViewModel);
	}
	
	private JPanel createHostView ()
	{
		SwitcherModel hostSwitchModel = new SwitcherModel (new String [] {"Host", "Job", "JVM", "VN", "Node", "AO"}, HOST_VIEW_KEYS);
		NodeHelper helper = new NodeHelper (hostSwitchModel);
		
		DataModelTraversal traversal = new DataModelTraversal (HOST_VIEW_KEYS);
		DataModelRoot root = new DataModelRoot (HOST_VIEW_ROOT, traversal, helper, modelRoot);
		
		hostViewModel = new DataTreeModel (root, hostSwitchModel, traversal, helper);

		return createPanel (hostViewModel);
	}
	
	private JPanel createVNView ()
	{
		SwitcherModel vnSwitchModel = new SwitcherModel (new String [] {"Job", "VN", "Host", "JVM", "Node", "AO"}, VN_VIEW_KEYS);
		NodeHelper helper = new NodeHelper (vnSwitchModel);
		
		DataModelTraversal traversal = new DataModelTraversal (VN_VIEW_KEYS);
		DataModelRoot root = new DataModelRoot (JOB_VIEW_ROOT, traversal, helper, modelRoot);
		
		vnViewModel = new DataTreeModel (root, vnSwitchModel,  traversal, helper);

		return createPanel (vnViewModel);
	}	
}