package org.objectweb.proactive.core.component.adl.component;

import java.lang.reflect.Method;
import java.util.Vector;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;

/**
 * Implementation of a parsed component
 * @author Nicolas Dalmasso
 *
 */
public class ADL2NComponentImpl implements ADL2NComponent{
	/**Name of the component*/
	private String name;
	/**Definition of the component*/
	private String definition;
	/**Filename of the behaviour file*/
	private String lotosFile;
	/**List of all interfaces on this component*/
	private Vector interfaces;
	/**List of all subcomponents of this component*/
	private Vector components;
	/**ID of the component*/
	private int IID = 0;
	/**Type of the component (when there are multiples 
	 * instances of the component in the ADL)
	 */ 
	private int type;
	/**True if the component can be multiple(instnces)*/
	private boolean multiple;
	/**Used to know if a component is already in the tree
	 * of Lotos processes or not
	 */
	private boolean MARKED;

	/**
	 * Constructor of component
	 * @param name Name of the component
	 * @param definition Definition of the component
	 * @param controller Controller associated with the component
	 * @param content Content of the component
	 */
	public ADL2NComponentImpl(String name,String definition, Object controller, Object content){
		this.name = name;
		this.definition = definition;
		components = new Vector();
		interfaces = new Vector();
		multiple = false;
		MARKED = false;
	}
	
	/**
	 * Gets the name of this component
	 * @return Name of the component
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this component
	 * @param name New name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the definition of this component
	 * @return Definition of the component
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * Sets the definition of this component
	 * @param name New definition
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * Gets all sub components of this component
	 * @return Array of sub components
	 */
	public Vector getComponents() {
		return components;
	}

	/**
	 * Adds a sub component to this component
	 * @param component Sub components to add
	 */
	public void addComponent(ADL2NComponent component) {
		components.add(component);
	}

	/**
	 * Removes a sub component of this component
	 * @param component Component to remove
	 */
	public void removeComponent(ADL2NComponent component) {
		components.remove(component);
	}

	/**
	 * All the interfaces binded on this component
	 * @return Array of Interfaces binded on this component
	 */
	public Vector getInterfaces() {
		return interfaces;
	}

	/**
	 * Set the list of interfaces available on this component
	 * @param interfaces List of interfaces
	 */
	public void setInterface(Vector interfaces){
		this.interfaces = interfaces;
	}
	
	/**
	 * Add an interface to this component  (virtual)
	 * @param i Interface to add
	 */
	public void addInterface(ADL2NInterface i) {
		i.setComponent(this);
		interfaces.add(i);
	}

	/**
	 * Remove an interface to this component  (virtual)
	 * @param i Interface to add
	 */
	public void removeInterface(ADL2NInterface i) {
		interfaces.remove(i);
	}
	
	/**
	 * Lotos file associated with this component (for behavioural specifications)
	 * @return Lotos file on the current file system
	 */
	public String getLotosFile() {
		return lotosFile;
	}

	/**
	 * Sets the lotos file associated with this component (for behavioural specifications)
	 * @param lotosFile Lotos file on the current file system
	 */
	public void setLotosFile(String lotosFile) {
		this.lotosFile = lotosFile;
	}

	/**
	 * Complete recursive description of the component
	 * @param depth Depth of the component in the hierarchy
	 * @return Description of the component
	 */
	public String toNString(int depth){
		StringBuffer sb = new StringBuffer();
		//Name and definition of the component
		for(int j=0;j<depth;j++)
			sb.append("\t");
		sb.append("<component name="+name+"  definition="+definition+"  type="+(isPrimitive()?"primitif":"composite")+">\n");
		//Name of the lotos file associated with this component
		for(int j=0;j<depth+1;j++)
			sb.append("\t");
		sb.append("<lotos file = "+lotosFile+"/>\n");
		//List of interfaces....
		for(int i=0;i<interfaces.size();i++){
			ADL2NInterface inter = (ADL2NInterface) interfaces.get(i);
			for(int j=0;j<depth+1;j++)
				sb.append("\t");
			//Name and methods
			sb.append("<interface name="+inter.getName()+" component="+((ADL2NComponentImpl)inter.getComponent()).getName()+">\n");
			Method[] methodList = inter.getMethods();
			for(int j=0;j<methodList.length;j++){
				for(int k=0;k<depth+2;k++)
					sb.append("\t");
				sb.append(methodList[j]+"\n");
			}
			//Bindings
			Vector bindings = inter.getBindings();
			for(int j=0;j<depth+2;j++)
				sb.append("\t");
			sb.append("<bindings>\n");
			for(int j=0;j<bindings.size();j++){
				for(int k=0;k<depth+3;k++)
					sb.append("\t");
				ADL2NInterface bItf = ((ADL2NInterface)bindings.get(j));
				sb.append("<binding name="+bItf.getName()+" component="+bItf.getComponent().getName()+" role="+(bItf.isClientInterface() ? "client" : "server")+"/>\n");
			}
			for(int j=0;j<depth+2;j++)
				sb.append("\t");
			sb.append("</bindings>\n");
			
			//Close the interfaces
			for(int j=0;j<depth+1;j++)
				sb.append("\t");
			sb.append("</interface>\n");
		}
		//And prints sub components recursively
		for(int i=0;i<components.size();i++){
			sb.append(((ADL2NComponentImpl)components.get(i)).toNString(depth+1));
		}
		//Close this component
		for(int j=0;j<depth;j++)
			sb.append("\t");
		sb.append("</component>\n");
		return sb.toString();
	}

	/**
	 * Unused here
	 */
	public Type getFcType() {
		return null;
	}

	/**
	 * Unused here
	 */
	public Object[] getFcInterfaces() {
		return null;
	}

	/**
	 * Unused here
	 */
	public Object getFcInterface(String arg0) throws NoSuchInterfaceException {
		return null;
	}
	
	/**
	 * String representation of the component
	 */
	public String toString(){
		return this.toNString(0);
	}

	/**
	 * Gets an interface of name name associated to this component
	 * @param name1 Name of the interface
	 * @return Interface of name name
	 */
	public ADL2NInterface getInterfaceByName(String name1) {
		for(int i=0;i<interfaces.size();i++){
			ADL2NInterfaceImpl itf = (ADL2NInterfaceImpl)interfaces.get(i);
			if(itf.getName().equals(name1))
				return itf;
		}
		return null;
	}

	/**
	 * Gets the type of the component. The integer
	 * returned is unique for each type of component.
	 * @return Type of the component
	 */
	public int getType() {
		return type;
	}

	/**
	 * Tests if the component is primitive or not
	 * @return True if this component is a primitive
	 */
	public boolean isPrimitive() {
		return components.isEmpty();
	}

	/**
	 * Tests if the component is composite or not
	 * @return True if this component is a composite
	 */
	public boolean isComposite() {
		return !isPrimitive();
	}
	
	/**
	 * Redefinition of the method equals
	 */
	public boolean equals(Object component){
		//If they aren't of the same type, they aren't equals
		if(! (component instanceof ADL2NComponent))
			return false;
		ADL2NComponent lcomponent = (ADL2NComponent)component;
		//If they don't have the same behaviour, they aren't equals
		if(lcomponent.getLotosFile() != null && !lcomponent.getLotosFile().equals(this.getLotosFile()))
			return false;
		//If they dont't have the same implementation, they aren't equals
		//If they don't have the same interfaces, they aren't equals
		/*Vector interfaces1 = lcomponent.getInterfaces();
		for(int i=0;i<interfaces1.size();i++){
			boolean found = false;
			for(int j=0;j<interfaces.size();j++){
				if(interfaces.get(j).equals(interfaces1.get(i)))
					found = true;
			}
			if(!found)
				return false;
		}*/
		return true;
	}

	/**
	 * Sets the type of the component.
	 * @param type Type of the component(same for
	 * differents instances of the same component)
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Return the number of the method method, usefull for FC2 generator
	 * @param method Method to seek
	 * @return Number of the method
	 */
	public int getMethodNumber(String method) {
		int res = 0;
		for(int i=0;i<interfaces.size();i++){
			ADL2NInterface itf = (ADL2NInterface)interfaces.get(i);
			for(int j=0;j<itf.getMethods().length;j++){
				if(method.equals(itf.getMethods()[j].getName()))
					return res;
				res++;
			}
		}
		return -2;
	}

	/**
	 * Search a component by his name
	 * @param name1 name of the component
	 * @return Component found or null if no component found
	 */
	public ADL2NComponent searchForComponent(String name1) {
		if(this.name.equals(name1))
			return this;
		for(int i=0;i<components.size();i++){
			ADL2NComponent tmp = ((ADL2NComponent)components.get(i)).searchForComponent(name1);
			if(tmp != null)
				return tmp;
		}
		return null;
	}

	/** 
	 * Brief description of the component
	 * @return String representation of the component
	 */
	public String toShortString() {
		StringBuffer res = new StringBuffer();
		res.append("Name :  "+name+"\n");
		res.append("Definition :  "+(definition == null?"None":definition)+"\n");
		res.append("BehaviourFile :  "+(lotosFile == null?"None":lotosFile)+"\n");
		res.append("Num. of interfaces :  "+interfaces.size()+"\n");
		if(interfaces.size() != 0){
			int num_bind = 0;
			int num_meth = 0;
			res.append("Interfaces :  ");
			for(int i=0;i<interfaces.size();i++){
				ADL2NInterface itf = (ADL2NInterface) interfaces.get(i);
				res.append(itf.getName()+(i==interfaces.size()-1?"\n":", "));
				num_bind+=itf.getBindings().size();
				num_meth+=itf.getMethods().length;
			}
			res.append("\nNum. of bindings :  "+num_bind+"\n");
			for(int i=0;i<interfaces.size();i++){
				ADL2NInterface itf = (ADL2NInterface) interfaces.get(i);
				Vector bindings = itf.getBindings();
				for(int j=0;j<bindings.size();j++){
					ADL2NInterface bind = (ADL2NInterface) bindings.get(j);
					res.append("\t"+name+"."+itf.getName()+" binded to "+bind.getComponent().getName()+"."+bind.getName()+"\n");
				}
			}
			res.append("\nNum. of methods :  "+num_meth+"\n");
			for(int i=0;i<interfaces.size();i++){
				ADL2NInterface itf = (ADL2NInterface) interfaces.get(i);
				for(int j=0;j<itf.getMethods().length;j++)
					res.append("\t"+itf.getMethods()[j]+"\n");
			}
		}
		
		return res.toString();
	}

	/**
	 * Returns true if this component can have multiple instances
	 * @return true if this component can have multiple instances
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * Sets the Id of the instance of the component
	 *
	 */
	public void setInstances(int IID) {
		this.IID = IID;
	}

	/**
	 * Gets the Id of the instance of the component
	 *
	 */
	public int getInstances() {
		return IID;
	}

	/**
	 * Sets this component as a multiple or single component
	 * @param mult True if this component can have multiple instances
	 */
	public void setMultiple(boolean mult) {
		multiple = mult;
	}

	/**
	 * Used for LotosDecompilation to know if a component
	 * is already in the tree or not
	 * @return Component's already in the tree
	 */
	public boolean isMarked() {
		return MARKED;
	}
	
	/**
	 * Used for LotosDecompilation to know if a component
	 * is already in the tree or not
	 */
	public void setMarked(){
		MARKED = true;
	}
}
