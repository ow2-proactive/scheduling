package org.objectweb.proactive.p2p.peerconfiguration;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.GregorianCalendar;

public class RowModification {

	protected PeerSetupGUI panel;
	protected int row;
	
    protected JFrame frame = null;
	protected JCheckBox sundayCheck = new JCheckBox("Sunday");
	protected JCheckBox mondayCheck = new JCheckBox("Monday");
    protected JCheckBox tuesdayCheck = new JCheckBox("Tuesday");
	protected JCheckBox wednesdayCheck = new JCheckBox("Wednesday");
	protected JCheckBox thursdayCheck = new JCheckBox("Thursday");
	protected JCheckBox fridayCheck = new JCheckBox("Friday");
	protected JCheckBox saturdayCheck = new JCheckBox("Saturday");
	protected JSpinner spinnerStartHour = null;
	protected JSpinner spinnerStartMinutes = null;
	protected JSpinner spinnerStopHour = null;
	protected JSpinner spinnerStopMinutes = null;
	protected JSlider slider = new JSlider();
	protected JCheckBox permanentCheck = new JCheckBox("Permanent");
	protected SpinnerSet spdStop = new SpinnerSet(true);
    protected SpinnerSet spdStart = new SpinnerSet(true);

    public RowModification(JFrame frame, PeerSetupGUI panel, int row) {
		this.frame=frame;
		this.panel = panel;
		this.row=row;
		}

    public Component createComponents() {

        //The main panel.
        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder(
                                                       30,
                                                       30,
                                                       10,
                                                       30)
        );
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

	    //Save button.
        JButton buttonS = new JButton("Save");
        buttonS.setMnemonic('s');
        buttonS.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                      Integer appInt1 = null;
                      Integer appInt2 = null;
                      Integer appInt3 = null;
                      Integer appInt4 = null;
                      Integer appInt5 = null;
                      Integer appInt6 = null;
                   //Control #1: minimum one day of te week selected
                   if (!(mondayCheck.isSelected())&&
                       !(tuesdayCheck.isSelected())&&
                       !(wednesdayCheck.isSelected())&&
                       !(thursdayCheck.isSelected())&&
                       !(fridayCheck.isSelected())&&
                       !(saturdayCheck.isSelected())&&
                       !(sundayCheck.isSelected())
                      )
                   {
                   	//Error message
                   	JOptionPane.showConfirmDialog(frame,
					                              "Please select at least one day",
									              "Input error",
									              JOptionPane.DEFAULT_OPTION,
				                                  JOptionPane.ERROR_MESSAGE);
                    return;
                   }

//                   //Control #2: stop-time < stop-time
                   Integer startHour = new Integer((String) spinnerStartHour.getValue());
                   Integer stopHour = new Integer((String) spinnerStopHour.getValue());
                   Integer startMinutes = new Integer((String) spinnerStartMinutes.getValue());
                   Integer stopMinutes = new Integer((String) spinnerStopMinutes.getValue());
//
//                   if (startHour.intValue() > stopHour.intValue()) {
//                   	JOptionPane.showConfirmDialog(frame,
//					                              "Start hour must be <= Stop hour",
//									              "Input error",
//									              JOptionPane.DEFAULT_OPTION,
//				                                  JOptionPane.ERROR_MESSAGE);
//                    return;
//                   }
//                   if (startHour.intValue() == stopHour.intValue()) {
//                       if (startMinutes.intValue() >= stopMinutes.intValue()) {
//                   	JOptionPane.showConfirmDialog(frame,
//					                              "In this case, Start minutes must be < Stop minutes",
//									              "Input error",
//									              JOptionPane.DEFAULT_OPTION,
//				                                  JOptionPane.ERROR_MESSAGE);
//                    return;
//                   	}
//                   }
                   if (!(permanentCheck.isSelected())) {
                      JSpinner[] stopSpinners = spdStop.getSpinners();
                      JSpinner[] startSpinners = spdStart.getSpinners();
                      appInt1 = new Integer((String) stopSpinners[0].getValue());
                      appInt2 = new Integer((String) stopSpinners[1].getValue());
                      appInt3 = new Integer(((Integer) stopSpinners[2].getValue()).intValue());
                      appInt4 = new Integer((String) startSpinners[0].getValue());
                      appInt5 = new Integer((String) startSpinners[1].getValue());
                      appInt6 = new Integer(((Integer) startSpinners[2].getValue()).intValue());

                      //Control #3: validity stop and stop dates
                      if (
                   	      !(checkDate(0,0,appInt1.intValue(), appInt2.intValue(), appInt3.intValue(), 0))||
                          !(checkDate(0,0,appInt4.intValue(), appInt5.intValue(), appInt6.intValue(),  0))
                         ) {
                   	JOptionPane.showConfirmDialog(frame,
					                              "One or more dates are not correct",
									              "Input error",
									              JOptionPane.DEFAULT_OPTION,
				                                  JOptionPane.ERROR_MESSAGE);
                    return;
                      }

//                      //Control #4: start date <= stop date
						GregorianCalendar stopGC = new GregorianCalendar(appInt3.intValue(), appInt2.intValue(), appInt1.intValue());
                        GregorianCalendar startGC = new GregorianCalendar( appInt6.intValue(), appInt5.intValue(), appInt4.intValue());
                      if (!(startGC.before(stopGC))) {
                   	JOptionPane.showConfirmDialog(frame,
					                              "Start date must be before Stop date",
									              "Input error",
									              JOptionPane.DEFAULT_OPTION,
				                                  JOptionPane.ERROR_MESSAGE);
                    return;
                      }

                   }

                   String recordString = "";
					if (sundayCheck.isSelected()) {
					   recordString = recordString + "Sunday ";
					}
                   if (mondayCheck.isSelected()) {
                      recordString = recordString + "Monday ";
                   }
                   if (tuesdayCheck.isSelected()) {
                      recordString = recordString + "Tuesday ";
                   }
                   if (wednesdayCheck.isSelected()) {
                      recordString = recordString + "Wednesday ";
                   }
                   if (thursdayCheck.isSelected()) {
                      recordString = recordString + "Thursday ";
                   }
                   if (fridayCheck.isSelected()) {
                      recordString = recordString + "Friday ";
                   }
                   if (saturdayCheck.isSelected()) {
                      recordString = recordString + "Saturday";
                   }

				String date = recordString;
                String validity="";
                 if (permanentCheck.isSelected()) {
                                                     validity = "Permanent";
                   } else {
                           validity = validity + appInt4.intValue() + "/" + appInt5.intValue() + "/" +  appInt6.intValue() + " - ";
                           validity = validity + appInt1.intValue() + "/" + appInt2.intValue() + "/" +  appInt3.intValue();
                   }

			   /*******************************************************
				 At this point, if the user confirm, we must add or update the record in the xml file
				 and update the table in the configuration frame
			   */		

				String start = startHour.intValue() + ":" + startMinutes.intValue();
				String stop = stopHour.intValue() + ":" + stopMinutes.intValue();
				String load = slider.getValue() + "%";
				
				panel.table.setValueAt(date,row,0);
				panel.table.setValueAt(start,row,1);
				panel.table.setValueAt(stop,row,2);
				panel.table.setValueAt(load,row,3);
				panel.table.setValueAt(validity,row,4);
				
				panel.table.repaint();
            }
            });


	    //Quit button.
        JButton buttonQ = new JButton("Quit");
        buttonQ.setMnemonic('q');
        buttonQ.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				frame.dispose();
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
	    buttonPanel.add(buttonS);
        buttonPanel.add(buttonQ);

        //The days panel.
        JPanel daysPanel = new JPanel();
        daysPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        daysPanel.setLayout(new BoxLayout(daysPanel, BoxLayout.Y_AXIS));
        daysPanel.setBorder(BorderFactory.createTitledBorder("Days"));
        daysPanel.add(mondayCheck);
        daysPanel.add(tuesdayCheck);
        daysPanel.add(wednesdayCheck);
        daysPanel.add(thursdayCheck);
        daysPanel.add(fridayCheck);
        daysPanel.add(saturdayCheck);
        daysPanel.add(sundayCheck);
        JButton allDays = new JButton("All days");
        daysPanel.add(allDays);
        JCheckBox[] cbx = {mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck, fridayCheck, saturdayCheck, sundayCheck};
        DaysCheckCangeListener l = new DaysCheckCangeListener(cbx);
        allDays.addChangeListener(l);

        //Time panels
        String[] hour = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20", "21","22","23","24"};
        String[] minutes = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20",
                            "21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40",
		            "41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"};
        JFormattedTextField ftf = null;
        JComponent editor = null;

        JPanel intermediatePanel = new JPanel();
        intermediatePanel.setLayout(new BoxLayout(intermediatePanel, BoxLayout.Y_AXIS));
        //Beginning time panel.
        JPanel bTimePanel = new JPanel();
        bTimePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        bTimePanel.setLayout(new FlowLayout());
        bTimePanel.setBorder(BorderFactory.createTitledBorder("Start time"));
        SpinnerModel hourStartModel = new CyclingSpinnerListModel(hour);
        spinnerStartHour = SpinnerSet.addLabeledSpinner(bTimePanel, "H", hourStartModel);
        SpinnerModel minutesStartModel = new CyclingSpinnerListModel(minutes);
        spinnerStartMinutes = SpinnerSet.addLabeledSpinner(bTimePanel, "M", minutesStartModel);
        ((CyclingSpinnerListModel)minutesStartModel).setLinkedModel(hourStartModel);
        editor = spinnerStartMinutes.getEditor();
        ftf = ((JSpinner.DefaultEditor)editor).getTextField();
        if (ftf != null ) {
                   ftf.setColumns(2); //specify more width than we need
                   ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
        editor = spinnerStartHour.getEditor();
        ftf = ((JSpinner.DefaultEditor)editor).getTextField();
        if (ftf != null ) {
                   ftf.setColumns(2); //specify more width than we need
                   ftf.setHorizontalAlignment(JTextField.RIGHT);
        }

        //Finish time panel.
        JPanel fTimePanel = new JPanel();
        fTimePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        fTimePanel.setLayout(new FlowLayout());
        fTimePanel.setBorder(BorderFactory.createTitledBorder("Work time"));
        SpinnerModel hourStopModel = new CyclingSpinnerListModel(hour);
        spinnerStopHour = SpinnerSet.addLabeledSpinner(fTimePanel, "H", hourStopModel);
        SpinnerModel minutesStopModel = new CyclingSpinnerListModel(minutes);
        spinnerStopMinutes = SpinnerSet.addLabeledSpinner(fTimePanel, "M", minutesStopModel);
        ((CyclingSpinnerListModel)minutesStopModel).setLinkedModel(hourStopModel);
        editor = spinnerStopMinutes.getEditor();
        ftf = ((JSpinner.DefaultEditor)editor).getTextField();
        if (ftf != null ) {
                   ftf.setColumns(2); //specify more width than we need
                   ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
        editor = spinnerStopHour.getEditor();
        ftf = ((JSpinner.DefaultEditor)editor).getTextField();
        if (ftf != null ) {
                   ftf.setColumns(2); //specify more width than we need
                   ftf.setHorizontalAlignment(JTextField.RIGHT);
        }

        //Max CPU panel.
        JPanel mCpuPanel = new JPanel();
        mCpuPanel.setLayout(new FlowLayout());
        mCpuPanel.setBorder(BorderFactory.createTitledBorder("MAX CPU Load"));
        slider.setMajorTickSpacing(9);
        slider.setMinorTickSpacing(3);
        slider.setMinimum(1);
        slider.setMaximum(100);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setOrientation(1);
        mCpuPanel.add(slider);
        JTextField currentValueTXT = new JTextField();
        currentValueTXT.setColumns(3);
        currentValueTXT.setBorder(BorderFactory.createTitledBorder("Value"));
        currentValueTXT.setEditable(false);
        Integer CPULoad = new Integer(slider.getValue());
        SliderValueUpdate cListener = new SliderValueUpdate(slider, currentValueTXT);
        slider.addChangeListener(cListener);
        currentValueTXT.setText(CPULoad.toString() + "%");
        mCpuPanel.add(currentValueTXT);

        //Validity panel.
        JPanel validityPanel = new JPanel();
        validityPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        validityPanel.setLayout(new BoxLayout(validityPanel, BoxLayout.X_AXIS));
        validityPanel.setBorder(BorderFactory.createTitledBorder("Validity"));
        validityPanel.add(permanentCheck);
        JPanel startPanel = new JPanel(new BorderLayout());
        JPanel stopPanel = new JPanel(new BorderLayout());
        JSpinner[] startSArraY = spdStart.getSpinners();
        JSpinner[] stopSArraY = spdStop.getSpinners();
        JSpinner[] spinnerArray = new JSpinner[startSArraY.length * 2];
        int j=0;
        while (j<spinnerArray.length) {
            for (int k=0; k<startSArraY.length; k++) {
        	           spinnerArray[j] = startSArraY[k];
        	           j++;
            }
            for (int k=0; k<stopSArraY.length; k++) {
        	           spinnerArray[j] = stopSArraY[k];
        	           j++;
            }
        }
        validityCheckCangeListener vcl = new validityCheckCangeListener(spinnerArray);
        permanentCheck.addChangeListener(vcl);
        startPanel.add(spdStart);
        stopPanel.add(spdStop);
        startPanel.setBorder(BorderFactory.createTitledBorder("Start"));
        stopPanel.setBorder(BorderFactory.createTitledBorder("Stop"));
        validityPanel.add(startPanel);
        validityPanel.add(stopPanel);

        //The editing panel.
        JPanel editingPanel = new JPanel();
        editingPanel.setBorder(BorderFactory.createEmptyBorder(
                                                       30,
                                                       30,
                                                       10,
                                                       30)
        );
	    editingPanel.setLayout(new FlowLayout());
        editingPanel.add(daysPanel);
	    intermediatePanel.add(bTimePanel);
	    intermediatePanel.add(fTimePanel);
	    editingPanel.add(intermediatePanel);
	    editingPanel.add(mCpuPanel);
	    editingPanel.add(validityPanel);


        pane.add(editingPanel);
	    pane.add(buttonPanel);

        return pane;
    }

    public boolean checkDate( int MIN, int HH, int GG, int MM, int AA, int SS) {
        boolean bis = false;

        bis=((AA % 4)==0);
        if ((AA % 100)==0)
         bis=((AA % 400)==0);

            switch (MM) {

                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                 if (!((GG>=1)&&(GG<=31))){
                     return(false);
                 }
                break;

                case 2:
                 if (bis) {
                  if (!((GG>=1)&&(GG<=29))){
                     return(false);
                  }
                 } else {
                  if (!((GG>=1)&&(GG<=28))){
                     return(false);
                  }
			     }
                break;

                case 4:
                case 6:
                case 9:
                case 11:
                 if (!((GG>=1)&&(GG<=30))){
                     return(false);
                 }
                break;

                default:
                 return(false);
            }

        return(true);
    }

}
