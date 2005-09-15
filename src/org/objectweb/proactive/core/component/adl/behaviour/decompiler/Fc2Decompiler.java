package org.objectweb.proactive.core.component.adl.behaviour.decompiler;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Vector;

import org.objectweb.proactive.core.component.adl.component.ADL2NComponent;
import org.objectweb.proactive.core.component.adl.component.ADL2NInterface;

/**
 * Decompiles the tree and generates FC2 and FC2parameterized code
 * @author Nicolas Dalmasso
 *
 */
public class Fc2Decompiler {
	/**Root component of the hierarchy*/
	private ADL2NComponent rootComponent;
	/**Where to print the generated code*/
	private PrintStream out;
	/**List of the component types*/
	private Vector types;
	/**Number of different types*/
	private int typeCpt = 1;
	
	/**
	 * Constructor that initialize th different types of the hierarchy
	 * @param rootComponent Root component of the hierarchy
	 * @param out Where to print the FC2 code
	 */
	public Fc2Decompiler(ADL2NComponent rootComponent,PrintStream out){
		this.rootComponent = rootComponent;
		types = new Vector();
		this.out = out;
		//Search differents instances of the same component -> must have same type
		Vector subComponents = rootComponent.getComponents();
		for(int i=0;i<subComponents.size();i++){
			ADL2NComponent lcomp = (ADL2NComponent) subComponents.get(i);
			if(!types.contains(lcomp)){
				lcomp.setType(typeCpt);
				types.add(lcomp);
				typeCpt++;
			}
			//On met le composant au bon type
			else{
				for(int j=0;j<types.size();j++){
					if(types.get(j).equals(lcomp))
						lcomp.setType(((ADL2NComponent)types.get(j)).getType());
				}
			}
		}
	}
	
	/**
	 * Decompilation of the tree, generation of FC2
	 * @param parameterized True if we wants to generate FC2parameterized
	 */
	public void decompile(boolean parameterized){
		//Non-primitive sub components
		Vector nonPrimitives = new Vector();
		//Prints the declaration section for parameters
		if(parameterized)
			decls();
		//Prints the heading of the FC2 file
		heading();
		//Prints the sub components net
		for(int i=0;i<types.size();i++){
			if(parameterized)
				componentTypeParameterized((ADL2NComponent) types.get(i));
			else
				componentType((ADL2NComponent) types.get(i));
		}
		//Print the component net
		out.print("\n\tnet 0\n");
		int NB_ACTIONS = 0;
		StringBuffer actions = new StringBuffer();
		//Prints the behaviour of the system
		Vector itfs = rootComponent.getInterfaces();
		for(int j=0;j<itfs.size();j++){
			ADL2NInterface itf = (ADL2NInterface)itfs.get(j);
			Vector bindings = itf.getBindings();
			for(int k=0;k<bindings.size();k++){
				ADL2NInterface bind = (ADL2NInterface) bindings.get(k);
				if(bind.getComponent().getType()==0)
					continue;
				Method[] methods = bind.getMethods();
				for(int l=0;l<methods.length;l++){
					actions.append("\t:"+NB_ACTIONS+" \""+methods[l].getName()+"("+bind.getComponent().getName()+"."+bind.getName()+")\""+(parameterized ? (methods[l].getParameterTypes().length!=0?(rootComponent.isMultiple()?"&(who,X)":"&X"):(rootComponent.isMultiple()?"&who":"")):"")+"\n");
					NB_ACTIONS++;
				}
			}
		}
		out.print("\tbehavs "+NB_ACTIONS+"\n");
		out.print(actions.toString());
		//Prints the structure of the system
		out.print("\tstruct _< ");
		for(int i=0;i<rootComponent.getComponents().size();i++){
			ADL2NComponent lcomp = (ADL2NComponent) rootComponent.getComponents().get(i);
			if(!parameterized)
				out.print(lcomp.getType()+(i == rootComponent.getComponents().size()-1 ? "" : ","));
			if(lcomp.isComposite())
				nonPrimitives.add(lcomp);
		}
		//In FC2Parameterized we wants to add parameters for multiples instance of a component
		if(parameterized){
			for(int i=0;i<types.size();i++){
				int cpt = 0;
				ADL2NComponent lcomp = (ADL2NComponent) types.get(i);
				if(lcomp.isMultiple())
					out.print(lcomp.getType()+"&"+lcomp.getName()+(i == types.size()-1 ? "" : ","));
				
				for(int j=0;j<rootComponent.getComponents().size();j++){
					ADL2NComponent scomp = (ADL2NComponent) rootComponent.getComponents().get(j);
					if(scomp.getType() == lcomp.getType()){
						cpt++;
						scomp.setInstances(cpt);
					}
				}
				if(!lcomp.isMultiple()){
					if(cpt == 1){
						out.print(lcomp.getType());
						lcomp.setInstances(0);
					}
					else
						out.print(lcomp.getType()+"&set(1,"+cpt+")");
					out.print(i == types.size()-1 ? "" : ",");
				}
			}	
		}
		//Prints the synch vectors
		out.print("\n");
		if(parameterized)
			synchVectorsParameterized();
		else 
			synchVectors();
	}
	
	/**
	 * Prints the FC2parameterized declarations
	 *
	 */
	private void decls() {
		out.print("declarations\n");
		out.print("\tconstant X() ->any\n");
		for(int i=0;i<types.size();i++){
			ADL2NComponent lcomp = (ADL2NComponent) types.get(i);
			if(lcomp.isMultiple())
				out.print("\tconstant "+lcomp.getName()+"() ->any\n");
		}
		out.print("\tinfix & (any any) ->any priority 8\n");
		out.print("\tinfix = (any any) ->any priority 8\n");
		out.print("\tinfix $ (any any) ->any priority 8\n");
		out.print("\tprefix set (any) ->set\n");
		out.print("\n");
	}
	
	/**
	 * Gets the parameters of the system, used to generate instanciation file
	 * @return List of parameters names
	 */
	public Vector getParmetersNames(){
		Vector res = new Vector();
		for(int i=0;i<types.size();i++){
			ADL2NComponent lcomp = (ADL2NComponent) types.get(i);
			if(lcomp.isMultiple())
				res.add(lcomp.getName());
		}
		return res;
	}
	
	/**
	 * Print the net corresponding to a component
	 * @param component Primitive component to print
	 */
	private void componentType(ADL2NComponent component) {
		out.print("\n\tnet "+component.getType()+"\n");
		out.print("\tstructs 1\n");
		out.print("\t:0 \""+component.getName()+"\"\n");
		//Behaviour
		StringBuffer behavs = new StringBuffer();
		StringBuffer actions = new StringBuffer();
		int bindingsCpt = 0;
		Vector itfs = component.getInterfaces();
		for(int i=0;i<itfs.size();i++){
			ADL2NInterface bind =  (ADL2NInterface)itfs.get(i);
			for(int j=0;j<bind.getMethods().length;j++){
				actions.append("\t:"+bindingsCpt+" \""+bind.getMethods()[j].getName()+"\"\n");
				behavs.append(""+(bind.isClientInterface()?"!":"?")+bindingsCpt+((i==itfs.size()-1&&j==bind.getMethods().length-1)?"\n":"+"));
				bindingsCpt++;
			}
		}
		out.print("\tbehavs "+bindingsCpt+"\n");
		out.print(actions.toString());
		out.print("\tstruct 0\n");
		out.print("\tbehav ");
		out.print(behavs.toString());
		
		out.print("\thook \"synch_vector\"\n");
	}
	
	/**
	 * Print the parameterized net corresponding to a component
	 * @param component Primitive component to print
	 */
	private void componentTypeParameterized(ADL2NComponent component) {
		out.print("\n\tnet "+component.getType()+"\n");
		out.print("\tstructs 1\n");
		out.print("\t:0 \""+component.getName()+"\"\n");
		//Behaviour
		StringBuffer behavs = new StringBuffer();
		StringBuffer actions = new StringBuffer();
		int bindingsCpt = 0;
		Vector itfs = component.getInterfaces();
		for(int i=0;i<itfs.size();i++){
			ADL2NInterface bind =  (ADL2NInterface)itfs.get(i);
			for(int j=0;j<bind.getMethods().length;j++){
				actions.append("\t:"+bindingsCpt+" \""+bind.getMethods()[j].getName()+"\"");
				if(bind.getMethods()[j].getParameterTypes().length != 0){
					if(component.isMultiple())
						actions.append("&("+component.getName()+",X)");
					else
						actions.append("&X");
				}
				else{
					if(component.isMultiple())
						actions.append("&"+component.getName()+"\t");
					else
						actions.append("\t");
				}
				actions.append("\n");
				
				behavs.append(""+(bind.isClientInterface()?"!":"?")+bindingsCpt+((i==itfs.size()-1&&j==bind.getMethods().length-1)?"\n":"+"));
				bindingsCpt++;
			}
		}
		out.print("\tbehavs "+bindingsCpt+"\n");
		out.print(actions.toString());
		out.print("\tstruct 0\n");
		out.print("\tbehav ");
		out.print(behavs.toString());
		
		out.print("\thook \"synch_vector\"\n");
	}
	
	/**
	 * Prints the heading of a FC2 file(number of nets)
	 *
	 */
	public void heading(){
		out.print("nets "+(types.size()+1)+"\n");
		out.print("\thook\"main\" > 0\n");
		out.print("\tstruct\""+(rootComponent.getDefinition() == null ? "DefaultComponent" : rootComponent.getDefinition())+"\"");
		out.print("\n");
	}
	
	/**
	 * Unused here
	 * @param comp
	 */
	public void netForPrimitive(ADL2NComponent comp){
		//out.println("\t\n%pLTS pour un composant primitif");
		out.println("\nnets 1");
		out.println("\thook\"main\" > 0");
		out.println("\tstruct\""+comp.getName()+"\"");
		out.println("\tnet 0");
		Vector interfaces = comp.getInterfaces();
		out.println("\tbehavs "+interfaces.size());
		for(int i=0;i<interfaces.size();i++){
			out.println("\t  :"+i+" \""+((ADL2NInterface)interfaces.get(i)).getName()+"\"");
		}
		//Here comes the automata description obtained by static analysis
	}
	
	/**
	 * Prints the synchronisation vectors for FC2
	 *
	 */
	public void synchVectors(){
		out.print("\thook \"synch_vector\"\n");
		out.print("\tvertice 1\n");
		out.print("\t  vertex 0\n");
		int NB_BINDINGS = 0;
		Vector rootSubComponents = rootComponent.getComponents();
		for(int c=0;c<rootSubComponents.size();c++){
			Vector interfaces = ((ADL2NComponent)rootSubComponents.get(c)).getInterfaces();
			for(int i=0;i<interfaces.size();i++){
				Vector bindings = ((ADL2NInterface)interfaces.get(i)).getBindings();
				for(int j=0;j<bindings.size();j++){
					ADL2NInterface bind = (ADL2NInterface)bindings.get(j);
					if(!bind.isClientInterface())
						NB_BINDINGS+=(bind.getMethods().length);
				}
			}
		}
		int bindingsCpt=0;
		out.print("\t    edges "+NB_BINDINGS+"\n");
		for(int c=0;c<rootSubComponents.size();c++){
			ADL2NComponent lcomp = ((ADL2NComponent)rootSubComponents.get(c));
			Vector interfaces = lcomp.getInterfaces();
			for(int i=0;i<interfaces.size();i++){
				Vector bindings = ((ADL2NInterface)interfaces.get(i)).getBindings();
				for(int j=0;j<bindings.size();j++){
					ADL2NInterface bind = (ADL2NInterface)bindings.get(j);
					if(!bind.isClientInterface()){
						Method[] methods = bind.getMethods();
						for(int m=0;m<methods.length;m++){
							out.print("\t      edge "+bindingsCpt+"\n");
							out.print("\t        behav "+bindingsCpt+" < ");
							ADL2NComponent binded_to = bind.getComponent();
							for(int k=0;k<rootSubComponents.size();k++){
								ADL2NComponent tmp = (ADL2NComponent)rootSubComponents.get(k);
								if(k==c || tmp.hashCode() == binded_to.hashCode())
									out.print(tmp.getMethodNumber(methods[m].getName())+(k==rootSubComponents.size()-1?"":","));
								else
									out.print("*"+(k==rootSubComponents.size()-1?"":","));
							}
							out.print(" ->0\t%Method "+methods[m].getName()+"_"+c+"(");
							for(int p=0;p<methods[m].getParameterTypes().length;p++)
								out.print(methods[m].getParameterTypes()[p].getName()+(p == methods[m].getParameterTypes().length-1?"":", "));
							out.print(")\n");
							bindingsCpt++;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Prints the synchronisation vectors for FC2 parameterized
	 *
	 */
	private void synchVectorsParameterized() {
		out.print("\thook \"synch_vector\"\n");
		out.print("\tvertice 1\n");
		out.print("\t  vertex 0\n");
		//int NB_BINDINGS = 0;
		int bindingsCpt=0;
		//Instances counter (by type)
		//int[] instances = new int[types.size()];
		//External bindings
		StringBuffer actions = new StringBuffer();
		Vector root_interfaces = rootComponent.getInterfaces();
		for(int i=0;i<root_interfaces.size();i++){
			Vector bindings = ((ADL2NInterface)root_interfaces.get(i)).getBindings();
			for(int j=0;j<bindings.size();j++){
				ADL2NInterface bind = (ADL2NInterface)bindings.get(j);
				//if(!bind.isClientInterface()){
				//NB_BINDINGS+=(bind.getMethods().length);
				Method[] methods = bind.getMethods();
				for(int m=0;m<methods.length;m++){
					ADL2NComponent binded_to = bind.getComponent();
					if(binded_to.isMultiple() && binded_to.getInstances() > 1)
						continue;
					if(binded_to.getType() == 0)
						continue;
					actions.append("\t      edge "+bindingsCpt+"\n");
					if(binded_to.isMultiple())
						actions.append("\t        hook "+binded_to.getName()+"="+binded_to.getName()+"\n");
					if(methods[m].getParameterTypes().length!=0)
						actions.append("\t        hook X=X\n");
					actions.append("\t        behav ");
					
					if(methods[m].getParameterTypes().length!=0)
						actions.append((bind.isClientInterface()?"!(":"?(")+bindingsCpt+"&X) < ");
					else
						actions.append((bind.isClientInterface()?"!":"?")+bindingsCpt+" < ");
					//Have Parameter
					if(methods[m].getParameterTypes().length!=0)
						actions.append((bind.isClientInterface()?"!(":"?(")+binded_to.getMethodNumber(methods[m].getName())+"&X)");
					else 
						actions.append((bind.isClientInterface()?"!":"?")+binded_to.getMethodNumber(methods[m].getName()));
					//Multiple instances
					if(binded_to.isMultiple())
						actions.append("$("+binded_to.getType()+"&"+binded_to.getName()+") -> 0\n");
					else if(((ADL2NComponent)types.get(binded_to.getType()-1)).getInstances() > 0)
						actions.append("$("+binded_to.getType()+"&"+binded_to.getInstances()+") -> 0\n");
					else
						actions.append("$"+binded_to.getType()+" -> 0\n");
					
					bindingsCpt++;
					//}
				}
			}
		}
		
		//Internal bindings
		Vector rootSubComponents = rootComponent.getComponents();
		for(int c=0;c<rootSubComponents.size();c++){
			ADL2NComponent curr_comp = ((ADL2NComponent)rootSubComponents.get(c));
			Vector interfaces = curr_comp.getInterfaces();
			for(int i=0;i<interfaces.size();i++){
				Vector bindings = ((ADL2NInterface)interfaces.get(i)).getBindings();
				for(int j=0;j<bindings.size();j++){
					ADL2NInterface bind = (ADL2NInterface)bindings.get(j);
					ADL2NComponent binded_to = bind.getComponent();
					if(!bind.isClientInterface() && binded_to.getType()!=0){
						if(binded_to.isMultiple() && binded_to.getInstances() > 1)
							continue;
						if(curr_comp.isMultiple() && curr_comp.getInstances() > 1)
							continue;
						//NB_BINDINGS+=(bind.getMethods().length);
						Method[] methods = bind.getMethods();
						for(int m=0;m<methods.length;m++){
							actions.append("\t      edge "+bindingsCpt+"\n");
							if(methods[m].getParameterTypes().length!=0)
								actions.append("\t        hook X=X\n");
							if(curr_comp.isMultiple())
								actions.append("\t        hook "+curr_comp.getName()+"="+curr_comp.getName()+"\n");
							if(binded_to.isMultiple())
								actions.append("\t        hook "+binded_to.getName()+"="+binded_to.getName()+"\n");
							actions.append("\t        behav ");
							actions.append("tau < ");
							
							//Have Parameter
							if(methods[m].getParameterTypes().length!=0)
								actions.append("!("+curr_comp.getMethodNumber(methods[m].getName())+"&X)");
							else
								actions.append("!"+curr_comp.getMethodNumber(methods[m].getName()));
							//Multiple Instances
							if(curr_comp.isMultiple())
								actions.append("$("+curr_comp.getType()+"&"+curr_comp.getName()+")");
							else if(curr_comp.getInstances() >0)
								actions.append("$("+curr_comp.getType()+"&"+curr_comp.getInstances()+")");
							else
								actions.append("$"+curr_comp.getType());
							//Have Parameter
							if(methods[m].getParameterTypes().length!=0)
								actions.append(", ?("+binded_to.getMethodNumber(methods[m].getName())+"&X)");
							else
								actions.append(", ?"+binded_to.getMethodNumber(methods[m].getName()));
							//Multiple Instances
							if(binded_to.isMultiple())
								actions.append("$("+binded_to.getType()+"&"+binded_to.getName()+") -> 0\n");
							else if(binded_to.getInstances()>0)
								actions.append("$("+binded_to.getType()+"&"+binded_to.getInstances()+") -> 0\n");
							else
								actions.append("$"+binded_to.getType()+" -> 0\n");
							bindingsCpt++;
						}
					}
				}
			}
		}
		out.print("\t    edges "+bindingsCpt+"\n");
		//External/Internal actions
		out.print(actions.toString()+"\n");
	}
}
