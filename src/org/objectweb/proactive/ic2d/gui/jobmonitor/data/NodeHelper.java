package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitcherModel;

public class NodeHelper
{
	private final SwitcherModel model;
	
	public NodeHelper (SwitcherModel _model)
	{
		model = _model;
	}
	
	public boolean isNodeDisplayed (DataModelNode node)
	{
		return model.isStateONKey (node.getKey());
	}
}
