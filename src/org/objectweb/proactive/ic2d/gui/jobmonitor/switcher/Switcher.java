package org.objectweb.proactive.ic2d.gui.jobmonitor.switcher;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Switcher extends JPanel implements SwitchListener
{
	private SwitcherModel model;
	private JLabel [] labels;
	
	public Switcher (SwitcherModel _model, final JTree jtree)
	{
		model = _model;
		
		final JPopupMenu popupmenu = new JPopupMenu ();
		
		model.addSwitchListener (this);
		
		int size = model.size();
		labels = new JLabel [size];
		
		setLayout (new GridLayout (1, size, 2, 0));
		
		for (int i = 0; i < size; ++i)
		{
			final JLabel l = new JLabel (model.getLabel (i), model.getIcon(i), SwingConstants.CENTER);
			l.setHorizontalAlignment (SwingConstants.CENTER);
			l.setOpaque (true);
			l.addMouseListener (new MouseAdapter()
			{
				public void mousePressed (MouseEvent e)
				{
					popupmenu.removeAll();
					final JCheckBoxMenuItem hide = new JCheckBoxMenuItem("Hide '" + l.getText() + "'");
					hide.setSelected(model.isHidden(l.getText()));
					hide.addActionListener(new ActionListener () {
						public void actionPerformed (ActionEvent e)
						{
							model.toggleHidden(l.getText());
							hide.setSelected(model.isHidden(l.getText()));
							jtree.repaint();
						}
					});
					popupmenu.add(hide);
					
					final JCheckBoxMenuItem highlight = new JCheckBoxMenuItem("Highlight '" + l.getText() + "'");
					highlight.setSelected(model.isHighlighted(l.getText()));
					highlight.addActionListener(new ActionListener () {
						public void actionPerformed (ActionEvent e)
						{
							model.toggleHighlighted(l.getText());
							highlight.setSelected(model.isHighlighted(l.getText()));
							jtree.repaint();
						}
					});
					popupmenu.add(highlight);

					popupmenu.show (l, e.getX(), e.getY());
				}
			});

			performSwitch (l, true);
			
			add (l);
				
			labels [i] = l;
		}		
	}
	
	
	
	private static final Color ON = Color.WHITE;
	private static final Color OFF = Color.LIGHT_GRAY;
	
	private static Font ON_FONT;
	private static Font OFF_FONT;
	
	private static int FONT_SIZE = 14;
	private static String FONT_NAME = "Dialog";
	
	private Font ON_Font()
	{
		if (ON_FONT == null)
			ON_FONT = new Font (FONT_NAME, Font.BOLD, FONT_SIZE);
		
		return ON_FONT;
	}

	private Font OFF_Font()
	{
		if (OFF_FONT == null)
			OFF_FONT = new Font (FONT_NAME, Font.PLAIN, FONT_SIZE);
		
		return OFF_FONT;
	}

	private void performSwitch (JLabel label, boolean isNewStateON)
	{
		label.setFont (isNewStateON ? ON_Font() : OFF_Font());
		label.setBackground (isNewStateON ? ON : OFF);
	}

	public SwitcherModel getModel()
	{
		return model;
	}

	private void updateModel (String label)
	{
		model.switchStateLabel (label);
	}
	
	public void switchPerformed (SwitchEvent e)
	{
//		System.out.println("JLabel: " + e.getLabel() + " is now '" + (e.getType() == SwitchEvent.ON ? "on" : "off") +"'");
		for (int i = 0; i < labels.length; i++)
		{
			JLabel l = labels[i];
			if (l.getText().equals (e.getLabel()))
				performSwitch (l, e.getType() == SwitchEvent.ON);
		}
	}
}
