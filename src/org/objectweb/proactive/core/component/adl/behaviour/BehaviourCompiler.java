package org.objectweb.proactive.core.component.adl.behaviour;
import java.util.Map;
import java.util.TreeMap;

import org.objectweb.deployment.scheduling.component.api.InstanceProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractConfigurationTask;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.api.control.BindingController;

/**
 * Compiler used by the parser to detect <behaviour> tag
 * @author Nicolas Dalmasso
 *
 */
public class BehaviourCompiler implements PrimitiveCompiler, BindingController{
	/**Behaviour builder used to buid components*/
	ADL2NBehaviourBuilder builder = new ADL2NBehaviourBuilder();
	/**Binding with the min compiler*/
	public final static String PRIMITIVE_COMPILERS_BINDING = "primitive-compilers";
	/**Others compilers*/
	public Map primitiveCompilers = new TreeMap();
	
	/**
	 * Launch the compilation on the current component
	 */
	public void compile(java.util.List arg0, ComponentContainer container, TaskMap tasks, Map arg3) throws ADLException {
		
		if (container instanceof BehaviourContainer) {
			Behaviour b = ((BehaviourContainer)container).getBehaviour();
			
			if (b != null) {
				InstanceProviderTask c = (InstanceProviderTask)tasks.getTask("create", container);
				SetBehaviourTask task = new SetBehaviourTask(builder,b.getLotos());
				task.setInstanceProviderTask(c);
				tasks.addTask("behaviour", container, task);
			}
		}
	}
	
	/**
	 * Execution of the compilation task
	 * @author Nicolas Dalmasso
	 *
	 */
	static class SetBehaviourTask extends AbstractConfigurationTask {
		private BehaviourBuilder builder;
		private String name;
		
		public SetBehaviourTask (BehaviourBuilder builder,String name) {
			this.builder = builder;
			this.name = name;
		}
		
		public void execute (final Object context) throws Exception {
			Object component = getInstanceProviderTask().getResult();
			builder.setLotos(component, name);
		}
		
		public Object getResult() { return null; }
		public void setResult (Object result) { }
	}
	
	/**
	 * Lists all bindings
	 */
	public String[] listFc() {
		return (String[])primitiveCompilers.keySet().toArray(new String[primitiveCompilers.size()]);
	}
	
	/**
	 * Looks up for  binding
	 */
	public Object lookupFc (final String itf) {
		if (itf.startsWith(PRIMITIVE_COMPILERS_BINDING)) {
			return primitiveCompilers.get(itf);
		}
		return null;
	}
	
	/**
	 * Binds this component to another
	 */
	public void bindFc (final String itf, final Object value) {
		if (itf.startsWith(PRIMITIVE_COMPILERS_BINDING)) {
			primitiveCompilers.put(itf, value);
		}
	}
	
	/**
	 * Unbinds this component to another
	 */
	public void unbindFc (final String itf) {
		if (itf.startsWith(PRIMITIVE_COMPILERS_BINDING)) {
			primitiveCompilers.remove(itf);
		}
	}
	
}