package org.ow2.proactive.scheduler.common.jmx.graphics;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This Class uses Java Swing library to display the informations coming from the Scheduler MBean
 */
@SuppressWarnings("serial")
public class DisplayResults extends JFrame {

	/**
	 * This is the constructor for Displaying the results in a List with Java Swing library
	 */
	public DisplayResults(String title, String[] resultList) {
		super(title);
		JList list;
		Container contentpane = getContentPane();
		contentpane.setLayout(new FlowLayout());
		list = new JList(resultList);
		list.setVisibleRowCount(26);
		JScrollPane jScroll = new JScrollPane(list);
		contentpane.add(jScroll);
		setLocation(600, 250);
		setSize(750, 600);
		setVisible(true);
	}

	/**
	 * This is the constructor for Displaying the results in a Text Area with Java Swing library
	 */
	public DisplayResults(String title, String resultString) {
		super(title);
		JTextArea textArea;
		Container contentpane = getContentPane();
		contentpane.setLayout(new FlowLayout());
		textArea = new JTextArea(resultString);
		contentpane.add(new JScrollPane(textArea));
		setLocation(600, 250);
		setSize(750, 600);
		setVisible(true);
	}
}
