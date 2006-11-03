package org.objectweb.proactive.ic2d.monitoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.ic2d.monitoring.editparts.AOEditPart;
import org.objectweb.proactive.ic2d.monitoring.editparts.AbstractMonitoringEditPart;

public class Test {

	public static void main(String[] args) {
		Set<AbstractMonitoringEditPart> l = new HashSet<AbstractMonitoringEditPart>();
		
		
		AOEditPart editpart = new AOEditPart(null);
		l.add(editpart);
		l.add(editpart);
		
		Iterator iterator = l.iterator();
		while (iterator.hasNext()) {
			AbstractMonitoringEditPart elem = (AbstractMonitoringEditPart) iterator.next();
			elem.test();
		}
	}
	
}
