package org.objectweb.proactive.ic2d.gui.jobmonitor.switcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingConstants;

import org.objectweb.proactive.ic2d.gui.jobmonitor.*;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeModel;


public class Switcher extends JPanel implements JobMonitorConstants
{
	private JLabel [] labels;
	
	public Switcher (final JTree jtree, final boolean allowExchange)
	{
		final DataTreeModel treeModel = (DataTreeModel) jtree.getModel();
		final JPopupMenu popupmenu = new JPopupMenu ();
		int size = treeModel.getNbKey();
		labels = new JLabel [size];
		
		setLayout (new GridLayout (1, size, 2, 0));
		
		for (int i = 0; i < size; ++i)
		{
			final Branch b = treeModel.getBranch(i);
			final JLabel l = new JLabel (b.getName(), b.getIcon(), SwingConstants.CENTER);
			
			l.setHorizontalAlignment (SwingConstants.CENTER);
			l.setOpaque (true);
			l.addMouseListener (new MouseAdapter()
			{
				public void mousePressed (MouseEvent e)
				{
					popupmenu.removeAll();
					final JCheckBoxMenuItem hide = new JCheckBoxMenuItem("Hide '" + l.getText() + "'");
					hide.setSelected(b.isHidden());
					hide.addActionListener(new ActionListener () {
						public void actionPerformed (ActionEvent e)
						{
							boolean wantToHide = !b.isHidden();
							performSwitch(treeModel, l, b.getKey(), !wantToHide);
							hide.setSelected(wantToHide);
							jtree.repaint();
						}
					});
					popupmenu.add(hide);
					
					final JCheckBoxMenuItem highlight = new JCheckBoxMenuItem("Highlight '" + l.getText() + "'");
					highlight.setSelected(b.isHighlighted());
					highlight.addActionListener(new ActionListener () {
						public void actionPerformed (ActionEvent e)
						{
							boolean wantToHighlight = !b.isHighlighted();
							treeModel.setHighlighted(b.getKey(), wantToHighlight);
							highlight.setSelected(wantToHighlight);
							jtree.repaint();
						}
					});
					popupmenu.add(highlight);

					if (allowExchange) {
						JMenu exchange = createExchangeMenu(b.getKey(), jtree);
						popupmenu.add(exchange);
					}

					popupmenu.show (l, e.getX(), e.getY());
				}
			});
			
			updateLabel(l, true);
			add (l);
				
			labels [i] = l;
		}		
	}
	
	private void exchange(DataTreeModel treeModel, int fromKey, int toKey) {
		treeModel.exchange(fromKey, toKey);

		int fromId = treeModel.indexOfKey(fromKey);
		int toId = treeModel.indexOfKey(toKey);

		JLabel tmp = labels[fromId];
		labels[fromId] = labels[toId];
		labels[toId] = tmp;
		
		setLayout (new GridLayout (1, labels.length, 2, 0));
		
		for (int i = 0; i < labels.length; i++)
			add(labels[i]);
		
		revalidate();
		repaint();
	}
	
	private JMenu createExchangeMenu(final int key, final JTree jtree) {
		final DataTreeModel treeModel = (DataTreeModel) jtree.getModel();
		JMenu submenu = new JMenu("Exchange '" + NAMES[KEY2INDEX[key]] + "' with");
		
		for (int i = 0, size = treeModel.getNbKey(); i < size; i++) {
			final Branch b = treeModel.getBranch(i);
			if (key != b.getKey()) {
				JMenuItem menuItem = new JMenuItem(new AbstractAction(b.getName(), b.getIcon()) {
					public void actionPerformed(ActionEvent e) {
						exchange(treeModel, key, b.getKey());
						jtree.repaint();
					}
				});
				submenu.add(menuItem);
			}
		}
		return submenu;
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

	private void updateLabel(JLabel label, boolean isNewStateON) {
		label.setFont (isNewStateON ? ON_Font() : OFF_Font());
		label.setBackground (isNewStateON ? ON : OFF);
	}
	
	private void performSwitch (DataTreeModel treeModel, JLabel label, int key, boolean isNewStateON)
	{
		updateLabel(label, isNewStateON);
		treeModel.setHidden(key, !isNewStateON);
	}
}
