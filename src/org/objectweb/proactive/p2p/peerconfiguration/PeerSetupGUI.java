package org.objectweb.proactive.p2p.peerconfiguration;
import javax.swing.*;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;

public class PeerSetupGUI {
    static JFrame frame = null;
	public int rows=-1;
	public JTable table;
	public Object[][] grid;
	public Object[][] lookupValues;
	public String lookup;
	public String protocol;
	
	protected PeerSetupGUI getThis;
	
    public Component createComponents() {


		// this reference
		
		getThis = this;
		lookupValues = new Object[1][2];
        //The main panel.
        
        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder(30, 30,  20, 40));
		// Title
		JLabel title = new JLabel("ProActive P2P System");
		title.setFont(new Font(null,Font.BOLD,16));
		title.setLabelFor(frame);
		
		// Subtitle
		JLabel subtitle = new JLabel("Peer Configuration");

		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

//		>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

         //The configuration table.
        String[] columnNames = {"Days",
                                "Start time",
                                "Work time",
                                "Maximum CPU load",
                                "Validity"};
	  	Object[][] tableData = new Object[7][5];
  		grid = tableData;
	 	 table = new JTable(tableData, columnNames);
		JLabel tableLabel = new JLabel("Add or Remove authorized intervals for participation in the P2P System");
//		TableColumn column = null;
//		for (int i = 0; i < 5; i++) {
//			column = table.getColumnModel().getColumn(i);
//			if (i == 0) {
//				column.setPreferredWidth(250);
//			}
//			if (i == 3) {
//				column.setPreferredWidth(150);
//			}
//			if (i == 4) {
//				column.setPreferredWidth(150);
//			}
//			if ((i!=0)&&(i!=4))
//			   column.setResizable(false) ;
//		}
		
        JScrollPane scrollPane = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(600, 120));

		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new GridLayout(1,0));

		tablePanel.add(scrollPane);


		//Add button.
		JButton buttonA = new JButton("Add");
		buttonA.setMnemonic('a');
		buttonA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					rows++;
					if (rows > 10) {rows=10; return;} 
				   JFrame frame = new JFrame("ProActive daemon configuration panel -> Adding one record");
				   RowModification mPanel = new RowModification(frame, getThis, rows);
				   frame.setResizable(false);
				   Component contents = mPanel.createComponents();
				   frame.getContentPane().add(contents, BorderLayout.CENTER);
				   frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				   frame.pack();
				   frame.setVisible(true);
			}
		});

		//Remove button.
		JButton buttonR = new JButton("Remove");
		buttonR.setMnemonic('r');
		buttonR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});

		//Modify button.
		JButton buttonM = new JButton("Modify");
		buttonM.setMnemonic('m');
		buttonM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				   JFrame frame = new JFrame("ProActive daemon configuration panel -> Modifing one record");
				   RowModification mPanel = new RowModification(frame, getThis, table.getSelectedRow());
				   frame.setResizable(false);
				   Component contents = mPanel.createComponents();
				   frame.getContentPane().add(contents, BorderLayout.CENTER);
				   frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				   frame.pack();
				   frame.setVisible(true);
			}
		});

		//Save button.
		JButton buttonS = new JButton("Save");
		buttonS.setMnemonic('s');
		buttonS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				ScheduleBean[] schedule = new ScheduleBean[rows+1];
				
				for (int k=0; k<= rows; k++) {
			
				// Init time
				schedule[k] = new ScheduleBean();
								
				String aux = (String) grid[k][1];
				String[] auxArray= aux.split(":");
				int startTime = Integer.parseInt(auxArray[0])*100 + Integer.parseInt(auxArray[1]);
				
				// Finish time
				aux = (String) grid[k][2];
				auxArray= aux.split(":");
				int stopTime = Integer.parseInt(auxArray[0])*100 + Integer.parseInt(auxArray[1]);
				
				// Worktime
				
				int workTime = 0;
				if (stopTime > startTime) workTime = stopTime - startTime;
				else workTime = (startTime + 2400) - stopTime;
				 
				// Max Load
				aux = (String) grid[k][3];
				auxArray = aux.split("%");
				int maxLoad = Integer.parseInt(auxArray[0]);
				
				
				schedule[k].setDays(schedule[k].Days2Byte((String) grid[k][0]));
				schedule[k].setStartTime(startTime);
				schedule[k].setWorkTime(workTime);
				schedule[k].setMaxLoad(maxLoad/100.00);

				/* VALIDITY */
				
			 	aux = (String) grid[k][4];
				if ("Permanent".equals(aux)) {
					schedule[k].setBegin(null);
					schedule[k].setEnd(null);
				} else {
					auxArray = aux.split(" - ");
					String[] beginDate = auxArray[0].split("/");
					String[] endDate = auxArray[1].split("/");
					
					int day = Integer.parseInt(beginDate[0]);
					int month = Integer.parseInt(beginDate[1]) - 1;  // FIRST MONTH = 0
					int year = Integer.parseInt(beginDate[2]);
					GregorianCalendar begin = new GregorianCalendar(year,month,day);
					
					day = Integer.parseInt(endDate[0]);
					month = Integer.parseInt(endDate[1]) -1;
					year = Integer.parseInt(endDate[2]);
					GregorianCalendar end = new GregorianCalendar(year,month,day);
					// add +1 day to be comparable...
					end.add(Calendar.DAY_OF_YEAR,1);
					schedule[k].setBegin(begin);
					schedule[k].setEnd(end);
				}
				}
				/* END VALIDITY */
				
				String machineName = (String) lookupValues[0][0];
				String protocol = (String) lookupValues[0][1];
				
				XMLEncoder encoder;
				try {
					encoder =
						new XMLEncoder(
							new BufferedOutputStream(
								new FileOutputStream("ProActiveP2PSchedule.xml")));
					encoder.writeObject(schedule);
					encoder.writeObject(machineName);
					encoder.writeObject(protocol);
					encoder.close();
				} catch (FileNotFoundException e1) {
					System.err.println("[ERROR] Cannot write Schedule.xml");
				}
			}
		});


		//Quit button.
		JButton buttonQ = new JButton("Quit");
		buttonQ.setMnemonic('q');
		buttonQ.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			 System.exit(0);
			}
		});

		//The button panel.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(
													   30,
													   30,
													   10,
													   30)
		);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(buttonA);
		buttonPanel.add(buttonR);
		buttonPanel.add(buttonM);
		buttonPanel.add(buttonS);
		buttonPanel.add(buttonQ);
		//The configuration table.
	   	String[] colNames = {"Machine Name", "Protocol"};
	   	lookupValues[0][0]=new String("//sakuraii.inria.fr/P2PRegistry");
		lookupValues[0][1]=new String("RMI");
	   	
		JTable lookupTable = new JTable(lookupValues, colNames);
				
	   	JLabel lookupLabel = new JLabel("Registry Lookup");

		lookupTable.setPreferredScrollableViewportSize(new Dimension(600, 20));
		TableColumnModel tcm = lookupTable.getColumnModel();
		JScrollPane jsp = new JScrollPane(lookupTable);

		pane.add(title);		
		pane.add(subtitle);

        pane.add(buttonPanel);
		pane.add(tableLabel);
		pane.add(tablePanel);

		pane.add(lookupLabel);
		pane.add(jsp);
		
        return pane;
    }

    public static void main(String[] args) {
        try {
             UIManager.setLookAndFeel(
             UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
	}

        //Create the top-level container and add contents to it.

        frame = new JFrame("ProActive P2P System");
	    frame.setResizable(false);
        PeerSetupGUI app = new PeerSetupGUI();

        Component contents = app.createComponents();

        frame.getContentPane().add(contents, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
}