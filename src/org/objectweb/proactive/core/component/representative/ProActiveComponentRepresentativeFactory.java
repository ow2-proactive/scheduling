package org.objectweb.proactive.core.component.representative;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;

import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.mop.Proxy;

public class ProActiveComponentRepresentativeFactory {
	protected static Logger logger = Logger.getLogger(ProActiveComponentRepresentativeFactory.class.getName());
	private static ProActiveComponentRepresentativeFactory INSTANCE = null;

	private ProActiveComponentRepresentativeFactory() {
	}

	public static ProActiveComponentRepresentativeFactory instance() {
		if (INSTANCE == null) {
			return (INSTANCE = new ProActiveComponentRepresentativeFactory());
		} else {
			return INSTANCE;
		}
	}

	public Component createComponentRepresentative(
		ComponentParameters componentParameters,
		Proxy proxy) {
		return (Component) new ProActiveComponentRepresentativeImpl(componentParameters, proxy);
	}
}