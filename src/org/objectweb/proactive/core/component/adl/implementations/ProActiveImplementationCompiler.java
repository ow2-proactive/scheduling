/*
 * Created on Apr 20, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component..adl.implementations;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.component.api.FactoryProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractInstanceProviderTask;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;

/**
 * @author Matthieu Morel
 */
public class ProActiveImplementationCompiler extends ImplementationCompiler {

	public void compile(final List path, final ComponentContainer container, final TaskMap tasks, final Map context) throws ADLException {
		if (!System.getProperty("fractal.provider").equals("org.objectweb.proactive.core.component.Fractive")) {
			throw new ADLException(
				"ProActiveImplementationCompiler is only usable with ProActive implementation of the fractal API",
				(Node) container);
		}
		boolean template = context != null && "true".equals(context.get("template"));

		String implementation = null;
		if (container instanceof ImplementationContainer) {
			ImplementationContainer ic = (ImplementationContainer) container;
			Implementation i = ic.getImplementation();
			if (i != null) {
				implementation = i.getClassName();
			}
		}

		String controller = null;
		if (container instanceof ControllerContainer) {
			ControllerContainer cc = (ControllerContainer) container;
			if (cc.getController() != null) {
				controller = cc.getController().getDescriptor();
			}
		}

		String templateController = null;
		if (container instanceof TemplateControllerContainer) {
			TemplateControllerContainer tcc = (TemplateControllerContainer) container;
			if (tcc.getTemplateController() != null) {
				templateController = tcc.getTemplateController().getDescriptor();
				template = true;
			}
		}

		String name = null;
		if (container instanceof Definition) {
			name = ((Definition) container).getName();
		} else if (container instanceof Component) {
			name = ((Component) container).getName();
		}

		String definition = null;
		if (container instanceof Definition) {
			definition = name;
		} else {
			definition = (String) ((Node) container).astGetDecoration("definition");
		}

		Component[] comps = ((ComponentContainer) container).getComponents();

		try {
			// the task may already exist, in case of a shared component
			tasks.getTask("create", container);
		} catch (NoSuchElementException e) {
			AbstractInstanceProviderTask createTask;
			if (comps.length > 0 || implementation == null) {
				if (implementation != null) {
					throw new ADLException("Implementation must be empty", (Node) container);
				}
				if (controller == null) {
					controller = "composite";
				}
				if (template) {
					if (templateController == null) {
						templateController = "compositeTemplate";
					}
					createTask = newCreateTask(path, container, name, definition, templateController, new Object[] { controller, null }, context);
				} else {
					ControllerDescription controllerDesc = null;
					if (controller == "composite") {
						controllerDesc = new ControllerDescription(name, Constants.COMPOSITE);
					}
					if (controller == "parallel") {
						controllerDesc = new ControllerDescription(name, Constants.PARALLEL);
					}
					//	PROACTIVE. This is a composite of a parallel component
					createTask = newCreateTask(path, container, name, definition, controllerDesc, null, context);
				}
			} else {
				if (controller == null) {
					controller = "primitive";
				}
				if (template) {
					if (templateController == null) {
						templateController = "primitiveTemplate";
					}
					createTask = newCreateTask(path, container, name, definition, templateController, new Object[] { controller, implementation }, context);
				} else {
					//					PROACTIVE. This is a primitive component
					createTask =
						newCreateTask(path, 
							container,
							name,
							definition,
							new ControllerDescription(name, Constants.PRIMITIVE),
							new ContentDescription(implementation),
							context);
				}
			}

			FactoryProviderTask typeTask = (FactoryProviderTask) tasks.getTask("type", container);
			createTask.setFactoryProviderTask(typeTask);

			tasks.addTask("create", container, createTask);
		}
	}
}
