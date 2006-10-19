package org.objectweb.proactive.ic2d.dgc.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.monitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.monitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.monitoring.spy.Spy;

public class ObjectGraph {
	private static Map<UniqueID, AOObject> AOObjectByID = new HashMap<UniqueID, AOObject>();

	public static void addObject(AOObject ao) {
		AOObjectByID.put(ao.getID(), ao);
	}
	
	private static Collection<AbstractDataObject> getAllChildren(Collection<AbstractDataObject> parents) {
		Collection<AbstractDataObject> res = new Vector<AbstractDataObject>();
		for (AbstractDataObject o : parents) {
			res.addAll(o.getMonitoredChildren());
		}
		return res;
	}
	
	private static Collection<AOObject> bodyIDToAOObject(Collection<UniqueID> ids) {
		Collection<AOObject> aos = new Vector<AOObject>();
		for (UniqueID id : ids) {
			AOObject ao = AOObjectByID.get(id);
			if (ao != null) {
				aos.add(ao);
			} else {
				System.out.println("Body " + id + " not found");
			}
		}
		return aos;
	}
	
	public static Map<AOObject, Collection<AOObject>> getObjectGraph(WorldObject world) {
		Collection<AbstractDataObject> hosts = world.getMonitoredChildren();
		Collection<AbstractDataObject> runtimes = getAllChildren(hosts);
		Collection<AbstractDataObject> nodes = getAllChildren(runtimes);
		
		Map<AOObject, Collection<AOObject>> res = new HashMap<AOObject, Collection<AOObject>>();
		for (AbstractDataObject o : nodes) {
			Spy spy = ((NodeObject) o).getSpy();
			Collection<AbstractDataObject> aos = o.getMonitoredChildren();
			for (AbstractDataObject oo : aos) {
				AOObject ao = (AOObject) oo;
				UniqueID bodyID = ((AOObject) oo).getID();
				Collection<UniqueID> referencesID = spy.getReferenceList(bodyID);
				Collection<AOObject> referencesAO = bodyIDToAOObject(referencesID);
				res.put(ao, referencesAO);
			}
		}
		return res;
	}
}
