package org.objectweb.proactive.ic2d.dgc.data;

import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.NodeObject;

public class DgcAOObjectWrapper extends AOObject {
	private AOObject wrappedObject;

	public DgcAOObjectWrapper(AOObject wrappedObject) {
		super((NodeObject)wrappedObject.getParent(), null, wrappedObject.getID(), "");
		this.wrappedObject = wrappedObject;
	}

	public AOObject getWrappedObject() {
		return this.wrappedObject;
	}
}
