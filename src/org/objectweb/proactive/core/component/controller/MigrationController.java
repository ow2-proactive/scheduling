package org.objectweb.proactive.core.component.controller;

import java.net.URL;

import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;

public interface MigrationController {
	
	public void migrateDependentActiveObjectsTo(Node node) throws MigrationException;
	
	public void migrateTo(URL url) throws MigrationException;
	
	public void migrateTo(String stringUrl) throws MigrationException;
	
	public void migrateTo(Node node) throws MigrationException;
}
