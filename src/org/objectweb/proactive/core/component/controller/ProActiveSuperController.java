/*
 * Created on May 10, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.controller;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.SuperController;

/**
 * @author Matthieu Morel
 */
public interface ProActiveSuperController extends SuperController {
	
	public abstract void addParent(final Component parent);
	public abstract void removeParent(final Component parent);
}