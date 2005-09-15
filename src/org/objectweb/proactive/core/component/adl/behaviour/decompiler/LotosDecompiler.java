package org.objectweb.proactive.core.component.adl.behaviour.decompiler;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Vector;

import org.objectweb.proactive.core.component.adl.component.ADL2NComponent;
import org.objectweb.proactive.core.component.adl.component.ADL2NInterface;

/**
 * 
 * Represents an element used for the list of couple of component
 * Exemple (list of 3 elements):
 * -1------1------3-
 * -*------*------*-
 *  /\     /\     /\
 * A  B   B  C   A  C
 * @author Dalmasso Nicolas
 *
 */
class LotosSingleton{
	/**The two components of the node*/
	private ADL2NComponent A,B;
	/**Number of interfaces beetween components*/
	private int weight;
	/**List of the methods beetween the two components*/
	public Vector synchro_names;
	
	/**
	 * Constructor of node
	 * @param A One of the two component
	 * @param B Second component
	 */
	public LotosSingleton(ADL2NComponent A,ADL2NComponent B){
		this.A = A;
		this.B = B;
		synchro_names = new Vector();
		addSynchro();
	}
	
	/**
	 * Adds the methods between the two components to the synchro_names list
	 *
	 */
	private void addSynchro() {
		for(int i=0;i<A.getInterfaces().size();i++){
			ADL2NInterface itf = (ADL2NInterface) A.getInterfaces().get(i);
			for(int j=0;j<itf.getBindings().size();j++){
				ADL2NInterface binding = (ADL2NInterface) itf.getBindings().get(j);
				if(binding.getComponent().hashCode() == B.hashCode()){
					for(int k=0;k<binding.getMethods().length;k++){
						String name = binding.getMethods()[k].getName();
						synchro_names.add(name+"_"+A.getName());
						weight++;
					}
				}
			}
		}
	}
	
	/**
	 * Number of methods between the two component (could either be synchro_names.size())
	 * @return
	 */
	public int getWeight(){
		return weight;
	}
	
	/**
	 * Gets the first of the two component of the node
	 * @return First component of the node
	 */
	public ADL2NComponent getA(){
		return A;
	}
	
	/**
	 * Gets the second of the two component of the node
	 * @return Second component of the node
	 */
	public ADL2NComponent getB(){
		return B;
	}
	
	/**
	 * String representation of this node
	 */
	public String toString(){
		String ret = "["+A.getName()+","+B.getName()+ "] -> "+weight+"  { ";
		for(int i=0;i<synchro_names.size();i++)
			ret += synchro_names.get(i)+" ";
		ret+="}";
		return ret;
	}
}

/**
 * Represents a node of the Lotso processes tress
 * @author Dalmasso Nicolas
 *
 */
class LotosNode implements Cloneable{
	/**The two child of this node*/
	public LotosNode left,right;
	/**The two components of this node*/
	public ADL2NComponent left_comp,right_comp; 
	/**List of synchronisations between the two components*/
	private Vector synch;
	/**
	 * Constructs a node with two components
	 * @param left_comp The left component
	 * @param right_comp The right component
	 */
	LotosNode(ADL2NComponent left_comp,ADL2NComponent right_comp,Vector synch){
		this.left_comp = left_comp;
		this.right_comp = right_comp;
		this.synch = synch;
	}
	
	/**
	 * Constructs an empty node
	 *
	 */
	public LotosNode() {
	}
	
	/**
	 * Adds a list of synchro to the current list of synchro
	 * @param synch List to append
	 */
	public void addSynchro(Vector synchro){
		for(int i=0;i<synchro.size();i++){
			//Search if it already exists
			boolean found = false;
			for(int j=0;j<synch.size();j++){
				if(synch.get(j).toString().equals(synchro.get(i).toString())){
					found = true;
					break;
				}
			}
			if(!found)
				synch.add(synchro.get(i));
		}
	}
	
	/**
	 * Gets the list of synchro of this node
	 * @return List of synchro
	 */
	public Vector getSynchro(){
		return synch;
	}
	
	/**
	 * Check if a node is in the tree represented by this node as root
	 * @param A Node to check
	 * @return true if the node is in the tree
	 */
	public boolean contains(ADL2NComponent A){
		return A != null && ( (left_comp != null && left_comp.hashCode() == A.hashCode()) || (right_comp != null && right_comp.hashCode() == A.hashCode()));
	}
	
	/**
	 * String representation of the node
	 */
	public String toString(){
		StringBuffer ret = new StringBuffer();
		ret.append("["+(left_comp!=null?left_comp.getName():"null")+";"+(right_comp!=null?right_comp.getName():"null")+"]  ->  ");
		if(left != null)
			ret.append(left.toString());
		else
			ret.append("null");
		ret.append(";");
		if(right != null)
			ret.append(right.toString());
		else
			ret.append("null");
		return ret.toString();
	}
	
	/**
	 * Clones the current node
	 */
	public Object clone() { 
		try {
			return super.clone(); 
		} catch (CloneNotSupportedException e) {
			throw new InternalError("But we are Cloneable!!!");
		}
	}

	public String printNodeRecursive(int depth) {
		StringBuffer res = new StringBuffer();
		for(int i=0;i<depth;i++)
			res.append("   ");
		/*
		 * Both component aren't null,
		 * so we synchronise them both
		 */
		if(left_comp != null && right_comp !=null){
			//Left component
			res.append("("+left_comp.getName());
			//Synchro
			res.append("    | [");
			for(int i=0;i<synch.size();i++)
				res.append(" "+synch.get(i));
			res.append(" ] |    ");
			//Right component
			res.append(right_comp.getName()+")\n");
		}
		/*
		 * Left component isn't null, right one is
		 */
		else if(left_comp != null){
			//Left component
			res.append(left_comp.getName());
			//Synchro
			res.append("    | [");
			for(int i=0;i<synch.size();i++)
				res.append(" "+synch.get(i));
			res.append(" ] | \n");
		}
		/*
		 * Right component isn't null, left one is
		 */
		else if(right_comp != null){
			//Left component
			res.append(right_comp.getName());
			//Synchro
			res.append("    | [");
			for(int i=0;i<synch.size();i++)
				res.append(" "+synch.get(i));
			res.append(" ] | ");
		}
		/*
		 * Print the childs recursively
		 */
		if(left != null)
			res.append(left.printNodeRecursive(depth+1));
		if(right != null)
			res.append(right.printNodeRecursive(depth+1));
		return res.toString();
	}

	/**
	 * Returns the first node that contains a or b
	 * @param a Child to find
	 * @param b Child to find
	 * @return The first node that have a or b as direct child
	 */
	public LotosNode searchForNode(ADL2NComponent a, ADL2NComponent b) {
		if(a == null || b == null)
			return null;
		if(left_comp != null){
			if(a.hashCode() == left_comp.hashCode() || b.hashCode() == left_comp.hashCode())
				return this;
		}
		if(right_comp != null){
			if(a.hashCode() == right_comp.hashCode() || b.hashCode() == right_comp.hashCode())
				return this;
		}
		return null;
	}
}

/**
 * Decompiles the tree and generates Lotos code
 * @author Dalmasso Nicolas
 *
 */
public class LotosDecompiler {
	/**The tree is represented by this component as root*/
	private ADL2NComponent rootComponent;
	/**Where to print the code*/
	private PrintStream out;
	/**List of couple of components used to generate the tree of Lotos processes*/
	private Vector processesList = new Vector();
	/**Root node of the Lotos processes tree*/
	private LotosNode root_node;
	
	/**
	 * Constructor
	 * @param rootComponent Root component of the tree
	 * @param out Stream where to print the code
	 */
	public LotosDecompiler(ADL2NComponent rootComponent,PrintStream out){
		this.rootComponent = rootComponent;
		this.out = out;
		root_node = new LotosNode();
	}
	
	/**
	 * Generates the code...
	 *
	 */
	public void decompile(){
		
		/*
		 * Generation of the specification part
		 */
		printSpecification();
		
		/*
		 * Set of predefined types that we may use in the specification
		 */
		printBoolType();
		printIntType();
		printIntIntervalType();
		
		/*
		 * Construct the processes list with wieght
		 * and with no doubles and sorted.
		 */
		{
			//List with no doubles...
			Vector tmpList = new Vector();
			boolean[][] no_doubles = new boolean[rootComponent.getComponents().size()][rootComponent.getComponents().size()];
			for(int i=0;i<rootComponent.getComponents().size();i++){
				for(int j=0;j<rootComponent.getComponents().size();j++){
					if( i != j && !no_doubles[i][j] && !no_doubles[j][i]){
						tmpList.add(new LotosSingleton((ADL2NComponent)rootComponent.getComponents().get(i),(ADL2NComponent)rootComponent.getComponents().get(j)));
						no_doubles[i][j] = true;
						no_doubles[j][i] = true;
					}
				}
			}
			//... and sorted
			while(tmpList.size()>0){
				int min = ((LotosSingleton)tmpList.get(0)).getWeight();
				int ind = 0;
				for(int j=1;j<tmpList.size();j++){
					int cost = ((LotosSingleton)tmpList.get(j)).getWeight();
					if(cost < min)
						ind = j;
				}
				processesList.add(tmpList.get(ind));
				tmpList.remove(ind);
			}
		}
		
		/*
		 * Creates a tree of Lotos processes with the previous list
		 */
		for(int i=processesList.size()-1;i>=0;i--){
			System.out.println(processesList.get(i));
			LotosSingleton tmp = (LotosSingleton) processesList.get(i);
			//Both nodes are in the tree
			if(tmp.getA().isMarked() && tmp.getB().isMarked()){
				LotosNode nod = root_node.searchForNode(tmp.getA(),tmp.getB());
				nod.addSynchro(tmp.synchro_names);
				continue;
			}
			LotosNode new_node = new LotosNode(tmp.getA(),tmp.getB(),tmp.synchro_names);
		
			//Two new processes
			if(!root_node.contains(tmp.getA()) && !root_node.contains(tmp.getB())){
				if(root_node.left == null && root_node.right == null){
					root_node = new_node;
				}
				else if(root_node.left != null){
					root_node.right = new_node;
				}
				else{
					root_node.left = new_node;
				}
				tmp.getA().setMarked();
				tmp.getB().setMarked();
				
			}
			//Left is a new process
			else if(root_node.contains(tmp.getA())){
				new_node.left_comp = null;
				new_node.left = (LotosNode) root_node.clone();
				root_node = new_node;
				tmp.getB().setMarked();
			}
			//Right is a new process
			else if(root_node.contains(tmp.getB())){
				new_node.right_comp = null;
				new_node.right = (LotosNode) root_node.clone();
				root_node = new_node;
				tmp.getA().setMarked();
			}
		}
		
		/*
		 * Now print the behaviour of the main component
		 */
		printBehaviour(root_node);
		
		/*
		 * And to finish, generate all the processes
		 */
		//printProcesses();
	}

	/**
	 * Prints the Lotos processes
	 *
	 */
	private void printProcesses() {
		out.print("\nwhere\n");
		for(int i=0;i<rootComponent.getComponents().size();i++){
			ADL2NComponent lcomp = (ADL2NComponent) rootComponent.getComponents().get(i);
			out.print("   process "+lcomp.getName()+"\n");
			out.print("   endproc\n\n");
		}
		out.print("endspec\n");
	}

	/**
	 * Prints the behaviour of the system
	 * @param root_node1 Root node of the system
	 */
	private void printBehaviour(LotosNode root_node1) {
		out.print("(*\n");
		out.print(" * Behaviour of the main component\n");
		out.print(" *)\n");
		out.print("behaviour\n");
		out.print(root_node1.printNodeRecursive(1));
	}

	/**
	 * Generates the specification of the component
	 *
	 */
	private void printSpecification() {
		out.print("(*\n");
		out.print(" * Specification of the "+rootComponent.getName()+" component.\n");
		out.print(" *)\n");
		out.print("process "+rootComponent.getName()+"[\n");
		boolean comma = false,commaParams = false;
		int paramNumber = 1;
		StringBuffer parameters = new StringBuffer();
		for(int i=0;i<rootComponent.getInterfaces().size();i++){
			ADL2NInterface itf = (ADL2NInterface) rootComponent.getInterfaces().get(i);
			for(int j=0;j<itf.getBindings().size();j++){
				ADL2NInterface binding = (ADL2NInterface) itf.getBindings().get(j);
				for(int k=0;k<binding.getMethods().length;k++){
					Method method = binding.getMethods()[k];
					if(comma)
						out.print(", ");
					//Method name
					out.print(method.getName()+"_"+binding.getComponent().getName());
					//Method parameters
					for(int p=0;p<method.getParameterTypes().length;p++){
						if(commaParams)
							parameters.append(", ");
						else
							parameters.append("(");
						commaParams = true;
						if(method.getParameterTypes()[p].equals(boolean.class))
							parameters.append("param_"+paramNumber+" : BOOLEAN");
						else
							parameters.append("param_"+paramNumber+" : INTEGER");
						paramNumber++;
					}
					out.print("\n");
					comma = true;
				}
			}
		}
		if(parameters.toString().length() > 0)
			parameters.append(")");
		out.print("]"+parameters.toString()+" : noexit\n\n");
	}

	/**
	 * Lotos definition of the Boolean type
	 *
	 */
	private void printBoolType() {
		out.print("(*\n * Definition of the boolean type in lotos.\n * Operations are:\n * and, or, wor, implies, iff\n * eq, ne \n *)\n");
		out.print("type BOOLEAN is\n");
		out.print("\tsorts BOOL\n");
		out.print("\topns true, false : -> BOOL\n");
		out.print("\t\tnot : BOOL -> BOOL\n");
		out.print("\t\t_and_, _or_, _xor_, _implies_, _iff_ : BOOL, BOOL -> BOOL\n");
		out.print("\t\t_eq_, _ne_ : BOOL, BOOL -> BOOL\n");
		out.print("\teqns forall X, Y :BOOL\n");
		out.print("\t\tofsort BOOL\n");
		out.print("\t\t\tnot (true) = false;\n");
		out.print("\t\t\tnot (false) = true;\n");
		out.print("\t\t\tX and true = X;\n");
		out.print("\t\t\tX and false = false;\n");
		out.print("\t\t\tX or true = true;\n");
		out.print("\t\t\tX or false = X;\n");
		out.print("\t\t\tX xor Y = (X and not (Y)) or (Y and not (X));\n");
		out.print("\t\t\tX implies Y = Y or not (X);\n");
		out.print("\t\t\tX iff Y = (X implies Y) and (Y implies X);\n");
		out.print("\t\t\tX eq Y = X iff Y;\n");
		out.print("\t\t\tX ne Y = X xor Y;\n");
		out.print("endtype\n\n");
	}

	/**
	 * Lotos definition of the Integer type
	 *
	 */
	private void printIntType() {
		out.print("(*\n * Definition of the integer type in lotos.\n * Operations are:\n * Succ, +, -, equal, not equal\n * lesser than, less or equal, greater than, greater or equal \n *)\n");
		out.print("type NATURAL is BOOLEAN\n");
		out.print("\tsorts NAT\n");
		out.print("\topns 0, 1, ... n : -> NAT\n");
		out.print("\t\tSUCC : NAT -> NAT\n");
		out.print("\t\t_+_, _*_ : NAT, NAT -> NAT\n");
		out.print("\t\t_eq_, _ne_, _lt_, _le_, _gt_, _ge_ : NAT, NAT -> BOOL\n");
		out.print("\teqns forall M, N:NAT\n");
		out.print("\t\tofsort NAT\n");
		out.print("\t\t\tM + 0 = M;\n");
		out.print("\t\t\tM + SUCC (N) = SUCC (M + N);\n");
		out.print("\t\t\tM * 0 = 0;\n");
		out.print("\t\t\tM * SUCC (N) = M + (M * N)\n");
		out.print("\t\tofsort BOOL\n");
		out.print("\t\t\t0 eq 0 = true;\n");
		out.print("\t\t\t0 eq SUCC (M) = false;\n");
		out.print("\t\t\tSUCC (M) eq 0 = false;\n");
		out.print("\t\t\tSUCC (M) eq SUCC (N) = M eq N;\n");
		out.print("\t\t\t0 lt 0 = false;\n");
		out.print("\t\t\t0 lt SUCC (M) = true;\n");
		out.print("\t\t\tSUCC (M) lt 0 = false;\n");
		out.print("\t\t\tSUCC (M) lt SUCC (N) = M lt N;\n");
		out.print("\t\t\tM ne N = not (M eq N);\n");
		out.print("\t\t\tM le N = (M lt N) or (M eq N);\n");
		out.print("\t\t\tM gt N = not (M le N);\n");
		out.print("\t\t\tM ge N = not (M lt N);\n");
		out.print("endtype\n\n");
	}
	
	/**
	 * Lotos definition of the IntegerInterval type
	 *
	 */
	private void printIntIntervalType() {
	}
}
