package org.objectweb.proactive.core.component.adl.behaviour.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.objectweb.proactive.core.component.adl.ADL2NLauncher;
import org.objectweb.proactive.core.component.adl.behaviour.decompiler.Fc2Decompiler;
import org.objectweb.proactive.core.component.adl.behaviour.decompiler.LotosDecompiler;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponent;
import org.objectweb.proactive.core.component.adl.component.ADL2NInterface;

/**
 * GUI of the ADL2N tool
 * @author Nicolas Dalmasso
 *
 */
public class BehaviourGUI {
	/**The FC2 decompiler*/
	Fc2Decompiler fc2Decomp = null;
	/**List of parameters values for FC2 parameterized*/
	Vector params;
	/**Dimension of the textfields*/
	Dimension dim = new Dimension(200,25);
	/**Arguments of the command line*/
	private String[] pargs;
	/**Main frame of the GUI*/
	private JFrame frame;
	/**Split pane : right = description and generated code; left = tree of components*/
	private JSplitPane splitPane;
	/**Tree representing the component architecture*/
	private JTree tree;
	/**Scroll pane for the tree*/
	private JScrollPane treeScrollPane;
	/**Tabbed pane containing tabs for description and generated code*/
	private JTabbedPane tabbedPane;
	/**Text area for the differents panels (description and generation)*/
	private JTextArea FC2Panel,FC2ParamPanel,fullDescription,descriptionArea,instanciationArea;
	/**Panel for the description of a component*/
	private JPanel description,instanciation,instanciationPanel;
	/**Main componet and selected one*/
	private ADL2NComponent rootComponent,selectedComponent;
	/**Status bar at the bottom of the frame*/
	private JLabel statusbar;
	/**Check if a component cn be multiple or not*/
	private JCheckBox isMultiple;
	/**Interfaces types of a multiple component*/
	private JRadioButton broadcast,scatter;
	/**List of interfaces of the component*/
	private JComboBox itfList;
	/**Panel for Lotos generation (can be colorized)*/
	private JEditorPane lotosPanel;
	/**Stream writing on the Lotos generation panel*/
	private JEditorPaneOutputStream outStreamLotos;
	
	/**
	 * GUI constructor
	 * @param pargsCommand line arguments
	 */
	public BehaviourGUI(String[] pargs){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		this.pargs = pargs;
		frame = new JFrame("Architecture Description Language to Net  -  INRIA (Sophia Antipolis)");
		createMenus();
		statusbar = new JLabel("Loading...");
		params = new Vector();
		frame.add(getSplitPaneInstance());
		frame.add(statusbar,BorderLayout.SOUTH);
		frame.setPreferredSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener(){
			public void windowOpened(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				if(JOptionPane.showConfirmDialog(null,"Quit ADL2Net?", "ADL2N", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					System.exit(0);
			}
			public void windowClosed(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
		});
	}

	/**
	 * Saves the content of a textarea to a file
	 * @param ta TextArea to save
	 */
	private void saveTextAreaToFile(JTextComponent ta){
		JFileChooser jfc = new JFileChooser(".");
		int result = jfc.showSaveDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			try {
				if(ta.equals(FC2ParamPanel) || ta.equals(FC2Panel))
					if(!file.toString().endsWith(".fc2"))
						file = new File(file.toString()+".fc2");
				Writer writer = new FileWriter(file);
				ta.write(writer);
				writer.close();
				//Generate the instanciation file for FC2Parameterized
				if(ta.equals(FC2ParamPanel))
					saveInstanciationFile(file.toString());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Generates the instanciation file for FC2 parameterized
	 * @param string Filename
	 */
	private void saveInstanciationFile(String filename) {
		if(fc2Decomp == null)
			return;
		if(filename.endsWith(".fc2"))
			filename = filename.substring(0,filename.length()-4)+"_Inst.fc2";
		else
			filename+="_Inst";
		try {
			PrintStream out = new PrintStream(new FileOutputStream(filename));
			out.println("D");
			for(int i=0;i<fc2Decomp.getParmetersNames().size();i++)
				out.println("constant "+fc2Decomp.getParmetersNames().get(i)+"() ->any");
			out.println("infix & (any any) ->any priority 8");
			out.println("infix = (any any) ->any priority 8");
			out.println("infix $ (any any) ->any priority 8");
			out.println("prefix set (any) ->set");
			out.println("nets 1");
			out.println("   net 0");
			String value = ((JTextField)params.get(0)).getText();
			out.println("      hook X="+value);
			for(int i=0;i<fc2Decomp.getParmetersNames().size();i++){
				String name = fc2Decomp.getParmetersNames().get(i).toString();
				value = ((JTextField)params.get(i+1)).getText();
				out.println("      hook "+name+"="+value);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate the instanciation
	 *
	 */
	private void generateInstanciation(){
		if(instanciationArea == null)
			return;
		instanciationArea.setText("");
		instanciationArea.append("D\n");
		for(int i=0;i<fc2Decomp.getParmetersNames().size();i++)
			instanciationArea.append("constant "+fc2Decomp.getParmetersNames().get(i)+"() ->any\n");
		instanciationArea.append("infix & (any any) ->any priority 8\n");
		instanciationArea.append("infix = (any any) ->any priority 8\n");
		instanciationArea.append("infix $ (any any) ->any priority 8\n");
		instanciationArea.append("prefix set (any) ->set\n");
		instanciationArea.append("nets 1\n");
		instanciationArea.append("   net 0\n");
		String value = ((JTextField)params.get(0)).getText();
		instanciationArea.append("      hook X="+value+"\n");
		for(int i=0;i<fc2Decomp.getParmetersNames().size();i++){
			String name = fc2Decomp.getParmetersNames().get(i).toString();
			value = ((JTextField)params.get(i+1)).getText();
			instanciationArea.append("      hook "+name+"="+value+"\n");
		}
	}
	
	/**
	 * Creation of the menus
	 *
	 */
	private void createMenus() {
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu fc2 = new JMenu("FC2");
		JMenu fc2param = new JMenu("FC2 Param.");
		JMenu lotos = new JMenu("Lotos");
		JMenu help = new JMenu("Help");
		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser(".");
				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file1 = fc.getSelectedFile();
		            try {
		            	pargs[1] = file1.getName().substring(0,file1.getName().length()-8);
		            	System.out.println(pargs[1]);
		            	ADL2NComponent comp = (ADL2NComponent) ADL2NLauncher.createComponent(pargs);
						loadComponent(comp);
		            } catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		JMenuItem saveFC2 = new JMenuItem("Save FC2");
		saveFC2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveTextAreaToFile(FC2Panel);
			}
		});
		JMenuItem printFC2 = new JMenuItem("Print FC2");
		printFC2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			}
		});
		JMenuItem saveParam = new JMenuItem("Save FC2 parameterized");
		saveParam.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveTextAreaToFile(FC2ParamPanel);
			}
		});
		JMenuItem printParam = new JMenuItem("Print FC2 parameterized");
		printParam.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			}
		});
		JMenuItem colorizeLotos = new JMenuItem("Colorize/Uncolorize Lotos Code (HTML)");
		colorizeLotos.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(outStreamLotos != null)
					outStreamLotos.switch_state();
			}
		});
		JMenuItem saveLotos = new JMenuItem("Save Lotos");
		saveLotos.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveTextAreaToFile(lotosPanel);
			}
		});
		JMenuItem printLotos = new JMenuItem("Print Lotos");
		printLotos.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			}
		});
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(null,"Quit ADL2Net?", "ADL2N", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					System.exit(0);
			}
		});
		file.add(open);
		fc2.add(saveFC2);
		fc2param.add(saveParam);
		lotos.add(saveLotos);
		fc2.add(printFC2);
		fc2param.add(printParam);
		lotos.add(printLotos);
		lotos.add(colorizeLotos);
		file.add(exit);
		bar.add(file);
		bar.add(fc2);
		bar.add(fc2param);
		bar.add(lotos);
		bar.add(help);
		frame.setJMenuBar(bar);
	}

	/**
	 * Gets the split pane or creates a new instance if doesn't exists
	 * @return The split pane
	 */
	private Component getSplitPaneInstance() {
		if(splitPane == null){
			splitPane = new JSplitPane();
			splitPane.setOneTouchExpandable(true);
			tabbedPane = getTabbedPaneInstance();
			treeScrollPane = new JScrollPane();
			splitPane.setRightComponent(tabbedPane);
			splitPane.setLeftComponent(treeScrollPane);
		}
		return splitPane;
	}

	/**
	 * Gets the tabbed pane or creates a new instance if doesn't exists
	 * @return The tabbed pane
	 */
	private JTabbedPane getTabbedPaneInstance() {
		if(tabbedPane == null){
			tabbedPane = new JTabbedPane();
			//Description Panel
			description = new JPanel(new BorderLayout());
			descriptionArea = new JTextArea();
			description.add(descriptionArea,BorderLayout.CENTER);
			//Instanciation panel
			instanciationPanel = new JPanel(new BorderLayout());
			instanciation = new JPanel(new FlowLayout());
			instanciation.setBackground(Color.WHITE);
			instanciation.setBorder(BorderFactory.createLineBorder (Color.black, 1));
			instanciationArea = new JTextArea();
			//Multiple instance panel
			JPanel mult = new JPanel();
			mult.setBorder(BorderFactory.createLineBorder (Color.black, 1));
			mult.setBackground(Color.WHITE);
			isMultiple = new JCheckBox("isMultiple");
			isMultiple.setBackground(Color.WHITE);
			broadcast = new JRadioButton("Bradcast");
			broadcast.setBackground(Color.WHITE);
			scatter = new JRadioButton("Scatter");
			scatter.setBackground(Color.WHITE);
			ButtonGroup group = new ButtonGroup();
			group.add(broadcast);
			group.add(scatter);
			//Action Listener on the Multiple checkbox
			isMultiple.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					selectedComponent.setMultiple(isMultiple.isSelected());
					for(int i=0;i<rootComponent.getComponents().size();i++){
						ADL2NComponent lcomp = (ADL2NComponent) rootComponent.getComponents().get(i);
						if(lcomp.getType() == selectedComponent.getType())
							lcomp.setMultiple(selectedComponent.isMultiple());
					}
					if(isMultiple.isSelected()){
						broadcast.setVisible(true);
						scatter.setVisible(true);
					}
					else{
						broadcast.setVisible(false);
						scatter.setVisible(false);
					}
					//Re-generate FC2 code
					FC2Panel.setText("");
	            	FC2ParamPanel.setText("");
	            	JTextAreaOutputStream outStream = new JTextAreaOutputStream(FC2Panel);
	        		JTextAreaOutputStream outStreamParam = new JTextAreaOutputStream(FC2ParamPanel);
	        		ADL2NComponent parsed = selectedComponent;
	        		if(selectedComponent.isPrimitive())
	        			parsed = rootComponent;
	        		new Fc2Decompiler(parsed,new PrintStream(outStream)).decompile(false);
	        		fc2Decomp = new Fc2Decompiler(parsed,new PrintStream(outStreamParam));
	        		fc2Decomp.decompile(true);
	        		resetInstanciationPane();
				}
			});
			itfList = new JComboBox();
			mult.add(isMultiple);
			mult.add(new JLabel("  Interfaces"));
			mult.add(itfList);
			mult.add(broadcast);
			mult.add(scatter);
			broadcast.setVisible(false);
			scatter.setVisible(false);
			description.add(mult,BorderLayout.NORTH);
			fullDescription = new JTextArea();
			lotosPanel=new JEditorPane();
			lotosPanel.setContentType("text/html");
			FC2Panel=new JTextArea();
			FC2ParamPanel=new JTextArea();
			descriptionArea.setEditable(false);
			fullDescription.setEditable(false);
			instanciationPanel.add(instanciation,BorderLayout.NORTH);
			instanciationPanel.add(instanciationArea);
			tabbedPane.addTab("Description",new JScrollPane(description));
			tabbedPane.addTab("Full description",new JScrollPane(fullDescription));
			tabbedPane.addTab("FC2",new JScrollPane(FC2Panel));
			tabbedPane.addTab("FC2 Parameterized",new JScrollPane(FC2ParamPanel));
			tabbedPane.addTab("Instanciation",new JScrollPane(instanciationPanel));
			tabbedPane.addTab("Lotos",new JScrollPane(lotosPanel));
		}
		return tabbedPane;
	}
	
	/**
	 * Loads a component into the GUI
	 * @param comp Component to load
	 */
	public void loadComponent(ADL2NComponent comp){
		FC2Panel.setText("");
		FC2ParamPanel.setText("");
		lotosPanel.setText("");
		statusbar.setText("Parsing component "+comp.getName()+"...");
		rootComponent = comp;
		DefaultMutableTreeNode top =
	        new DefaultMutableTreeNode(comp.getName());
		createHierarchy(top,comp);
		tree = new JTree(top);
		tree.addTreeSelectionListener(new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent e) {
				 DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
				 if (node == null) return;
				 String name = (String) node.getUserObject();
				 changeSelectedComponent(name);
			}
		});
		treeScrollPane = new JScrollPane(tree);
		splitPane.setLeftComponent(treeScrollPane);
		JTextAreaOutputStream outStream = new JTextAreaOutputStream(FC2Panel);
		JTextAreaOutputStream outStreamParam = new JTextAreaOutputStream(FC2ParamPanel);
		outStreamLotos = new JEditorPaneOutputStream(lotosPanel);
		statusbar.setText("Generating code for "+comp.getName()+"...");
		new LotosDecompiler(comp,new PrintStream(outStreamLotos)).decompile();
		new Fc2Decompiler(comp,new PrintStream(outStream)).decompile(false);
		fc2Decomp = new Fc2Decompiler(comp,new PrintStream(outStreamParam));
		fc2Decomp.decompile(true);
		resetInstanciationPane();
		statusbar.setText("Done parsing "+comp.getName()+".");
		selectedComponent = comp;
		outStreamLotos.setText();
	}
	
	/**
	 * Resets the instanciation pane and adds testfields to modify each parameter
	 *
	 */
	private void resetInstanciationPane() {
		instanciation.removeAll();
		params.clear();
		//Add the default prameter to the Instanciation panel
		JLabel name = new JLabel("X (method parameter)");
		name.setAlignmentX(Component.CENTER_ALIGNMENT);
		JTextField value = new JTextField("0");
		value.setPreferredSize(dim);
		value.addKeyListener(new KeyListener(){
			public void keyTyped(KeyEvent e) {
			}
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				generateInstanciation();
			}
		});
		name.setBackground(Color.WHITE);
		instanciation.add(name);
		instanciation.add(value);
		params.add(value);
		for(int i=0;i<fc2Decomp.getParmetersNames().size();i++){
			name = new JLabel(fc2Decomp.getParmetersNames().get(i).toString());
			instanciation.add(name);
			name.setBackground(Color.WHITE);
			value = new JTextField("0");
			value.addKeyListener(new KeyListener(){
				public void keyTyped(KeyEvent e) {
				}
				public void keyPressed(KeyEvent e) {
				}
				public void keyReleased(KeyEvent e) {
					generateInstanciation();
				}
			});
			value.setPreferredSize(dim);
			instanciation.add(value);
			params.add(value);
		}
		generateInstanciation();
	}

	/**
	 * The component selected on the tree had changed
	 * @param name Name of the selected component
	 */
	private void changeSelectedComponent(String name) {
		if(rootComponent == null){
			System.out.println("This can't be normal");
			return;
		}
		ADL2NComponent searchedComponent = rootComponent.searchForComponent(name);
		if(searchedComponent == null){
			System.out.println("This can't be normal");
			return;
		}
		itfList.removeAllItems();
		isMultiple.setSelected(searchedComponent.isMultiple());
		if(isMultiple.isSelected()){
			broadcast.setVisible(true);
			scatter.setVisible(true);
		}
		else{
			broadcast.setVisible(false);
			scatter.setVisible(false);
		}
		for(int i=0;i<searchedComponent.getInterfaces().size();i++){
			ADL2NInterface tmpItf = ((ADL2NInterface)searchedComponent.getInterfaces().get(i));
			itfList.addItem( tmpItf.getName() + (tmpItf.isClientInterface()?"(client)":"(server)"));
		}
		fullDescription.setText(searchedComponent.toString());
		descriptionArea.setText(searchedComponent.toShortString());
		statusbar.setText("Component "+searchedComponent.getName()+" selected.");
		selectedComponent = searchedComponent;
	}

	/**
	 * Creates the tree representing the component
	 * @param top Root node of the tree
	 * @param comp Root component of the hierarchy
	 */
	private void createHierarchy(DefaultMutableTreeNode top, ADL2NComponent comp) {
		Vector subComponents = comp.getComponents();
		for(int i=0;i<subComponents.size();i++){
			ADL2NComponent tmp = (ADL2NComponent) subComponents.get(i);
			DefaultMutableTreeNode tmpTreeComponent =
		        new DefaultMutableTreeNode(tmp.getName());
			top.add(tmpTreeComponent);
			if(tmp.isComposite())
				createHierarchy(tmpTreeComponent,tmp);
		}
	}
	
}
