/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.ic2d.gui.process;

import java.awt.BorderLayout;
import java.util.Dictionary;
import java.util.Hashtable;
import org.objectweb.proactive.ic2d.gui.util.MessagePanel;
import org.objectweb.proactive.core.util.MessageLogger;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.globus.*;
import java.awt.GridLayout;
import java.awt.BorderLayout.*;
import org.objectweb.proactive.core.util.UrlBuilder;    
 
public class GlobusProcessControlPanel extends javax.swing.JPanel {

private int MAX_RETRY=50;
  private static final java.awt.Color FINISHED_PROCESS_COLOR = new java.awt.Color(211,32,47);
  
  private ProcessesListModel processesListModel;
  private ProcessListPanel processListPanel;
  private ProcessDefinitionPanel processDefinitionPanel;
  private ProcessOutputPanel processOutputPanel;
  private javax.swing.JSplitPane mainSplitPanel;
  private GlobusProcess globusProcess;
  // we need a deep copy of the process to be able to run multiple process from the same window
  private GlobusProcess globusCopyProcess;
  private Dictionary globusProcesses=new Hashtable();

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  public GlobusProcessControlPanel() {
    processesListModel = new ProcessesListModel();
    
    setLayout(new java.awt.GridLayout(1,1));
    // create the top split panel
    processListPanel = new ProcessListPanel();
    processDefinitionPanel = new ProcessDefinitionPanel();
    processOutputPanel = new ProcessOutputPanel();
    javax.swing.JSplitPane topSplitPanel = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, false, processListPanel, processDefinitionPanel);
    topSplitPanel.setDividerLocation(250);
    topSplitPanel.setOneTouchExpandable(true);
    
    //Create the full split pane
    mainSplitPanel = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT, false, topSplitPanel, processOutputPanel);
    //mainSplitPanel.add(topSplitPanel);
    mainSplitPanel.setOneTouchExpandable(true);
    add(mainSplitPanel);
  }

  public GlobusProcessControlPanel(GlobusProcess gp) {
      globusProcess=gp;
      processesListModel = new ProcessesListModel();
      setLayout(new java.awt.GridLayout(1,1));
      // create the top split panel
      processListPanel = new ProcessListPanel();
      processDefinitionPanel = new ProcessDefinitionPanel();
      processOutputPanel = new ProcessOutputPanel();
      javax.swing.JSplitPane topSplitPanel = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, false, processListPanel, processDefinitionPanel);
      topSplitPanel.setDividerLocation(250);
      topSplitPanel.setOneTouchExpandable(true);
      
      //Create the full split pane
      mainSplitPanel = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT, false, topSplitPanel, processOutputPanel);
      //mainSplitPanel.add(topSplitPanel);
      mainSplitPanel.setOneTouchExpandable(true);
      add(mainSplitPanel);
  }

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private void processChanged(GlobusProcess process) {
    processListPanel.processChanged(process);
    processDefinitionPanel.processChanged(process);
    processOutputPanel.processChanged(process);
  }
  
  private void currentProcessChanged() {
    processChanged(processListPanel.getCurrentProcess());
  }
  
  //
  // -- INNER CLASSES -----------------------------------------------
  //
  
 /**
  * ProcessList
  */
  private class ProcessesListModel extends javax.swing.AbstractListModel {
  
    private java.util.ArrayList processesList;
  
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
  
    public ProcessesListModel() {
      processesList = new java.util.ArrayList();
    }

    //
    // -- Public methods -----------------------------------------------
    //

    //
    // -- implements ListModel -----------------------------------------------
    //
    public int getSize() {
      return processesList.size();
    }

    public Object getElementAt(int index) {
      return processesList.get(index);
    }
    
    public void addProcess(GlobusProcess globusProcess) {
      int n = processesList.size();
      processesList.add(globusProcess.getId());
      System.out.println("***Key added:"+ (globusProcess.getId()).toString() +"***");
      globusProcesses.put(globusProcess.getId() , globusProcess);
      fireIntervalAdded(this, n, n);
    }

    public void removeProcess(int index) {
      System.out.println("***removeProcess***");
      String idToRemove = (String) processesList.remove(index);
      GlobusProcess gp = (GlobusProcess) globusProcesses.get(idToRemove);
      gp.stopProcess();
      System.out.println("***Process have been stop***");
      fireIntervalRemoved(this, index, index);
      globusProcesses.remove(idToRemove);
    }

  } // end inner class ProcessListModel


  
  /**
   * Cell Renderer
   */
  private class ProcessListCellRenderer extends javax.swing.DefaultListCellRenderer  {
    public ProcessListCellRenderer() {}
    public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object o, int index, boolean isSelected, boolean cellHasFocus) {
      java.awt.Component c = super.getListCellRendererComponent(list, o, index, isSelected, cellHasFocus);
      
      //c.setForeground(FINISHED_PROCESS_COLOR);
      
      return c;
    }
  }



  /**
   * ProcessListPanel
   */
  private class ProcessListPanel extends javax.swing.JPanel {
    private javax.swing.JButton stopProcessButton;
    private javax.swing.JList processesJList;
    public ProcessListPanel() {
      setBorder(javax.swing.BorderFactory.createTitledBorder("List of current processes"));
      setLayout(new java.awt.BorderLayout());
      // list
      processesJList = new javax.swing.JList(processesListModel);
      processesJList.setCellRenderer(new ProcessListCellRenderer());
      processesJList.setVisibleRowCount(10);
      processesJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
           /**
            * When an item is selected, this method is fired and updates the display area
            */
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
              if (e.getValueIsAdjusting()) return;
	      stopProcessButton.setEnabled(true);
              // We update the output frame
	      //if(getCurrentProcess()==null){
                processOutputPanel.processChanged(getCurrentProcess());
		//}
		//else {
                //processOutputPanel.processChanged(getCurrentProcess());
		//}
            }
          }
        );
      javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(processesJList);
      scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      add(scrollPane, java.awt.BorderLayout.CENTER);
      // stop button
      stopProcessButton = new javax.swing.JButton("Stop Globus Node");
      stopProcessButton.setEnabled(false);
      stopProcessButton.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            int n = processesJList.getSelectedIndex();
            if (n > -1) {
              processesListModel.removeProcess(n);
              processOutputPanel.refresh();
            }
          }
        });
      stopProcessButton.setToolTipText("Stop the selected process in the list");
      add(stopProcessButton, java.awt.BorderLayout.SOUTH);
    }
      
    public GlobusProcess getCurrentProcess() {
      System.out.println("***Key to remove:"+ (processesJList.getSelectedValue()).toString()+"***" );
      return (GlobusProcess) globusProcesses.get( (processesJList.getSelectedValue()).toString() );
    }
    
    public void processChanged(GlobusProcess process) {
      stopProcessButton.setEnabled(process != null);
    }
  } // end inner class ProcessListPanel




 /**
  * ProcessDefinitionPanel
  */
  private class ProcessDefinitionPanel extends javax.swing.JPanel {
  
    private javax.swing.JTextField GISField = new javax.swing.JTextField(globusProcess.getGISPort());
    private javax.swing.JTextField GRAMField = new javax.swing.JTextField(globusProcess.getGramPort());
    private javax.swing.JTextField STDOUTField = new javax.swing.JTextField("Local");
    private javax.swing.JTextField STDERRField = new javax.swing.JTextField("Local");
    
    private String[] globusHostList = globusProcess.getGlobusHostAdviser().getGlobusHosts();
    private javax.swing.JComboBox globusList = new javax.swing.JComboBox(globusHostList);
    
		
//    private javax.swing.JTextField hostnameField = new javax.swing.JTextField("globus1.inria.fr");


    private javax.swing.JTextField protocolField = new javax.swing.JTextField("rmi");
    private javax.swing.JTextField nodeNameField = new javax.swing.JTextField("GlobusNode");
    
    public ProcessDefinitionPanel() {
      setBorder(javax.swing.BorderFactory.createTitledBorder("Create new node"));
      setLayout(new java.awt.BorderLayout());
      
      // defines fields in the north panel 
      {
      javax.swing.JPanel northPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
      javax.swing.JPanel labelPanel = new javax.swing.JPanel(new java.awt.GridLayout(0,1));
      javax.swing.JPanel fieldPanel = new javax.swing.JPanel(new java.awt.GridLayout(0,1));
     
      labelPanel.add(new javax.swing.JLabel("hostname ", javax.swing.JLabel.RIGHT));
      globusList.setSelectedIndex(0);
      fieldPanel.add(globusList);
      labelPanel.add(new javax.swing.JLabel("name of the node ", javax.swing.JLabel.RIGHT));
      fieldPanel.add(nodeNameField);
      labelPanel.add(new javax.swing.JLabel("GIS Port ", javax.swing.JLabel.RIGHT));
      fieldPanel.add(GISField);
      labelPanel.add(new javax.swing.JLabel("GRAM Port ", javax.swing.JLabel.RIGHT));
      fieldPanel.add(GRAMField);
      labelPanel.add(new javax.swing.JLabel("Stdout ", javax.swing.JLabel.RIGHT));
      fieldPanel.add(STDOUTField);
      labelPanel.add(new javax.swing.JLabel("Stderr ", javax.swing.JLabel.RIGHT));
      fieldPanel.add(STDERRField);

      



      labelPanel.add(new javax.swing.JLabel("Registry ", javax.swing.JLabel.RIGHT));
      fieldPanel.add(protocolField);

      
      northPanel.add(labelPanel, java.awt.BorderLayout.WEST);
      northPanel.add(fieldPanel, java.awt.BorderLayout.CENTER);
      add(northPanel, java.awt.BorderLayout.NORTH);
      } 
      
      {
      // defines text areas in the center panel
      javax.swing.JPanel labelPanel = new javax.swing.JPanel(new java.awt.GridLayout(0,1));
      javax.swing.JPanel fieldPanel = new javax.swing.JPanel(new java.awt.GridLayout(0,1));
      add(labelPanel, java.awt.BorderLayout.WEST);
      add(fieldPanel, java.awt.BorderLayout.CENTER);
      }
      
      // defines start button in the south panel 
      {
      javax.swing.JButton b = new javax.swing.JButton("Create Globus Node");
      b.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
	      //UrlBuilder ub = new UrlBuilder();
	      String hostname = filterEmptyString((String)globusList.getSelectedItem());
	      String protocol = filterEmptyString(protocolField.getText());
	      String nodeName = filterEmptyString(nodeNameField.getText());
	      String nodeURL = UrlBuilder.buildUrl(hostname, nodeName,protocol+":");
	      System.out.println("Node URL:"+nodeURL);
	      
	      //Make the deep copy of the process to be able to run multiple globus process
	      globusCopyProcess = (GlobusProcess)makeDeepCopy(globusProcess);

	      // CREATE THE GLOBUS PROCESS
	      globusCopyProcess.startNodeWithGlobus(nodeURL);
	      //globusProcess = new GlobusProcess();
	      //globusProcess.startNodeWithGlobus(protocol+"://"+hostname+"/"+nodeName); 
	      
	      // Attendre que la sortie soit prete pour mettre a jour la fenetre d'output
	      // ... a faire
	      // while (!gp.isReady() ... and i<MAX_RETRY...
	      for(int i=0; (!globusCopyProcess.isReady() && i<MAX_RETRY) ; i++){
		  try{
		      Thread.sleep(750);
		  }
		  catch(java.lang.InterruptedException ie){
		      System.out.println("exception in GlobusProcessControlPanel");
		  }
		  System.out.println("attente GlobusProcessControlPanel:"+i);
	      }
	      processesListModel.addProcess(globusCopyProcess);

          }
      });
      b.setToolTipText("Start a process based on the above specs");
      add(b, java.awt.BorderLayout.SOUTH);
      }
    }
    
    public void processChanged(GlobusProcess gp) {
      return;
    }
    
    private String filterNull(String s) {
      if (s == null) return "";
      return s;
    }
    
    private String filterEmptyString(String s) {
      if (s.length() == 0) return null;
      return s;
    }
    
    private String stringArrayToString(String[] stringArray) {
      if (stringArray == null) return "";
      StringBuffer sb = new StringBuffer();
      for (int i=0; i<stringArray.length; i++) {
        sb.append(stringArray[i]);
        sb.append("\n");
      }
      return sb.toString();
    }
    
    private String[] stringToStringArray(String string) {
      java.util.StringTokenizer st = new java.util.StringTokenizer(string, "\n");
      java.util.ArrayList result = new java.util.ArrayList();
      while (st.hasMoreTokens()) {
        String s = st.nextToken().trim();
        if (s.length() > 0) {
          result.add(s);
        }
      }
      if (result.size() == 0) return null; 
      String[] stringArray = new String[result.size()];
      result.toArray(stringArray);
      return stringArray;
    }
    
    private javax.swing.JScrollPane createScrollWrapper(javax.swing.JTextArea textArea) {
      javax.swing.JScrollPane areaScrollPane = new javax.swing.JScrollPane(textArea);
      areaScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      areaScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.black));
      return areaScrollPane;
    }
    
    /**
	 * Returns a deepcopy of the process
	 * @param process the process to copy
	 * @return ExternalProcess, the copy version of the process
	 */
  private ExternalProcess makeDeepCopy(ExternalProcess process){
  	ExternalProcess result = null;
  	try{
  	java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
    oos.writeObject(process);
    oos.flush();
    oos.close();
    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
    java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
    result = (ExternalProcess)ois.readObject();
    ois.close();
  	}catch(Exception e){
  		e.printStackTrace();
  	}
    return result; 
  }

  } // end inner class ProcessDefinitionPanel



  public class SynchronizedMessageLogger implements MessageLogger {
    
    private MessageLogger logger;
    
    public SynchronizedMessageLogger(MessageLogger logger) {
      this.logger = logger;
    }

    public synchronized void log(String message) {
      logger.log(message);
    }

    public synchronized void log(String message, Throwable e) {
      logger.log(message, e);
    }

    public synchronized void log(Throwable e) {
      logger.log(e);
    }

  }


 /**
  * ProcessOutputPanel
  */
  private class ProcessOutputPanel extends javax.swing.JPanel {
  
    private javax.swing.JPanel emptyProcessPanel;
    private javax.swing.JPanel processPanel;
    private javax.swing.text.JTextComponent text =new javax.swing.JTextPane();

    public ProcessOutputPanel() {
      setLayout(new java.awt.BorderLayout());
      // create the empty Process Panel
      emptyProcessPanel = new javax.swing.JPanel();
      emptyProcessPanel.add(new javax.swing.JLabel("no process selected"));
      add(emptyProcessPanel);
    }

    public void refresh(){
       removeAll();
       add(emptyProcessPanel);
    }     

    public void processChanged(GlobusProcess process) {
      GridLayout g = new GridLayout(1,1,30,10);
      g.setVgap(10);



      removeAll();
      if (process == null) {
        add(emptyProcessPanel);
      } else {
        setBorder(javax.swing.BorderFactory.createTitledBorder("process selected"));
	//javax.swing.JLabel jl=new javax.swing.JLabel("process selected",0);
	// We add a panel with contaning the output of the process
        processPanel=new javax.swing.JPanel();
        processPanel.setLayout(g);
	System.out.println("Process:"+process.getOutput());
        text.setText( process.getOutput() );
        processPanel.add(text,java.awt.BorderLayout.CENTER );
        add(processPanel,java.awt.BorderLayout.CENTER );
      }
      revalidate();
      repaint();
    }
  
  
  } // end inner class ProcessOutputPanel

}

