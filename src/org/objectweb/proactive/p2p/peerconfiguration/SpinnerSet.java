package org.objectweb.proactive.p2p.peerconfiguration;
import javax.swing.*;
import java.awt.Container;
import java.util.Calendar;

public class SpinnerSet extends JPanel {
    protected JFormattedTextField ftf = null;
    protected JSpinner spinnerMinutes = null;
	protected JSpinner spinnerHour = null;
	protected JSpinner spinnerDay = null;
	protected JSpinner spinnerMonth = null;
	protected JSpinner spinnerYear = null;

    public SpinnerSet(boolean cycleMonths) {
        super(new SpringLayout());

        String[] labels = {"Day: ", "Month: ", "Year: "};
        int numPairs = labels.length;
        Calendar calendar = Calendar.getInstance();

//        // Minutes
//        int currentMinutes = calendar.get(Calendar.MINUTE);
//        String[] minutes = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20",
//	                    "21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40",
//			    "41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"};
//        SpinnerModel minutesModel = new CyclingSpinnerListModel(minutes);
//        spinnerMinutes = addLabeledSpinner(this, labels[0], minutesModel);
//        ftf = getTextField(spinnerMinutes);
//        if (ftf != null ) {
//           ftf.setColumns(8); //specify more width than we need
//           ftf.setHorizontalAlignment(JTextField.RIGHT);
//        }
//
//        // Hour
//        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
//        String[] hour = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"};
//        SpinnerModel hourModel = new CyclingSpinnerListModel(hour);
//        spinnerHour = addLabeledSpinner(this, labels[1], hourModel);
//        ftf = getTextField(spinnerHour);
//        if (ftf != null ) {
//           ftf.setColumns(8); //specify more width than we need
//           ftf.setHorizontalAlignment(JTextField.RIGHT);
//        }

        // Day
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        String[] days =   {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15",
	                   "16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
        SpinnerModel dayModel = new CyclingSpinnerListModel(days);
        spinnerDay = addLabeledSpinner(this, labels[0], dayModel);
        ftf = getTextField(spinnerDay);
        if (ftf != null ) {
           ftf.setColumns(8); //specify more width than we need
           ftf.setHorizontalAlignment(JTextField.RIGHT);
        }

        //Month
        String[] monthStrings = {"1","2","3","4","5","6","7","8","9","10","11","12"};
        SpinnerListModel monthModel = null;
        if (cycleMonths) { //use custom model
            monthModel = new CyclingSpinnerListModel(monthStrings);
        } else { //use standard model
            monthModel = new SpinnerListModel(monthStrings);
        }
        spinnerMonth = addLabeledSpinner(this,
                                    labels[1],
                                    monthModel);
        ftf = getTextField(spinnerMonth);
        if (ftf != null ) {
            ftf.setColumns(8); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }


        //Year
        int currentYear = calendar.get(Calendar.YEAR);
        SpinnerModel yearModel = new SpinnerNumberModel(currentYear, //initial value
                                       currentYear - 100, //min
                                       currentYear + 100, //max
                                       1);                //step
        //If we're cycling, hook this model up to the month model.
        if (monthModel instanceof CyclingSpinnerListModel) {

//	    ((CyclingSpinnerListModel)minutesModel).setLinkedModel(hourModel);
//	    ((CyclingSpinnerListModel)hourModel).setLinkedModel(dayModel);

	    ((CyclingSpinnerListModel)dayModel).setLinkedModel(monthModel);
            ((CyclingSpinnerListModel)monthModel).setLinkedModel(yearModel);
        }
        spinnerYear = addLabeledSpinner(this, labels[2], yearModel);
        //Make the year be formatted without a thousands separator.
        spinnerYear.setEditor(new JSpinner.NumberEditor(spinnerYear, "#"));

        //Lay out the panel.
        SpringUtilities.makeCompactGrid(this,
                                        numPairs, 2, //rows, cols
                                        10, 10,        //initX, initY
                                        6, 10);       //xPad, yPad
    }

    /**
     * Return the formatted text field used by the editor, or
     * null if the editor doesn't descend from JSpinner.DefaultEditor.
     */
    public JFormattedTextField getTextField(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            return ((JSpinner.DefaultEditor)editor).getTextField();
        } else {
            System.err.println("Unexpected editor type: "
                               + spinner.getEditor().getClass()
                               + " isn't a descendant of DefaultEditor");
            return null;
        }
    }

    /**
     * DateFormatSymbols returns an extra, empty value at the
     * end of the array of months.  Remove it.
     */
    static protected String[] getMonthStrings() {
        String[] months = new java.text.DateFormatSymbols().getMonths();
        int lastIndex = months.length - 1;

        if (months[lastIndex] == null
           || months[lastIndex].length() <= 0) { //last item empty
            String[] monthStrings = new String[lastIndex];
            System.arraycopy(months, 0,
                             monthStrings, 0, lastIndex);
            return monthStrings;
        } else { //last item not empty
            return months;
        }
    }

    static protected JSpinner addLabeledSpinner(Container c,
                                                String label,
                                                SpinnerModel model) {
        JLabel l = new JLabel(label);
        c.add(l);

        JSpinner spinner = new JSpinner(model);
        l.setLabelFor(spinner);
        c.add(spinner);

        return spinner;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("SpinnerSet");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new SpinnerSet(false);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public int getYear() {
           return (((Integer) spinnerYear.getValue()).intValue());
    }
    public int getMonth() {
	   String monthString = (String) spinnerMonth.getValue();
	   if (monthString.compareTo("January")==0) return (1);
	   if (monthString.compareTo("February")==0) return (2);
	   if (monthString.compareTo("March")==0) return (3);
	   if (monthString.compareTo("April")==0) return (4);
	   if (monthString.compareTo("May")==0) return (5);
	   if (monthString.compareTo("June")==0) return (6);
	   if (monthString.compareTo("July")==0) return (7);
	   if (monthString.compareTo("August")==0) return (8);
	   if (monthString.compareTo("September")==0) return (9);
	   if (monthString.compareTo("October")==0) return (10);
	   if (monthString.compareTo("November")==0) return (11);
           if (monthString.compareTo("December")==0) return (12);
	   return(1);
    }
    public int getDay() {
           return ((new Integer((String) spinnerDay.getValue())).intValue());
    }

    public JSpinner[] getSpinners() {
		    JSpinner[] result = {spinnerDay, spinnerMonth, spinnerYear};
		    return (result);
	}

}