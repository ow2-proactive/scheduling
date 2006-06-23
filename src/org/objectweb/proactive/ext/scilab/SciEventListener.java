package org.objectweb.proactive.ext.scilab;
import java.util.EventListener;

public interface SciEventListener extends EventListener {
	public void actionPerformed(SciEvent evt);
}

