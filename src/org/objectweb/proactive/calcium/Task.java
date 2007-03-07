/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.calcium;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.calcium.skeletons.Instruction;
import org.objectweb.proactive.calcium.statistics.StatsImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * This class is the main wrapper class for the objects passed to the Skernel.
 * A regular user should never know that this class exists.
 * 
 * Among others, this class: provides a wrapper for the user
 * parameters, holds the intstruction stack for this task, handles the creation 
 * and conquer of child tasks (subtasks).
 * 
 * @author The ProActive Team (mleyton)
 *
 * @param <T>
 */
public class Task<T> implements Serializable, Comparable<Task>{
	static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS);
	
	public static int DEFAULT_PRIORITY=0;
	public static int DEFAULT_INTRA_FAMILY_PRIORITY=0;
	static private int DEFAULT_ROOT_PARENT_ID=-1;

	
	//Rebirth preserved parameters
	private int familyId; //Id of the root task
	private int parentId;
	private T param;
	
	private int id;
	private int priority; //higher number => higher priority
	private int intraFamilyPri; //higher number => higher priority
	private boolean isTainted;
	private boolean isDummy;
	private StatsImpl stats;
	//private int streamId;
	
	//The program stack. Higher indexed elements are served first (LIFO).
	private Vector<Instruction<?,?>> stack;
	
	/*
	 * Children (sub task) state queues (not preserved by Rebirth)
	 */
	private Vector<Task<T>> childrenReady;
	private Hashtable<Task<?>,Task<T>> childrenWaiting;
	private Vector<Task<T>> childrenFinished;
	private Exception exception;
	
	public Task(){
		
	}
	
	private Task(T object, int id, int priority, int intraFamilyPriority, int parentId,  int familyId) {
		this.param = object;
		this.id = id;
		this.priority = priority;
		this.intraFamilyPri=intraFamilyPriority;
		this.parentId= parentId;
		this.familyId=familyId;
		
		
		isDummy=false;
		stats = new StatsImpl();
		stack=new Vector<Instruction<?,?>>();
		
		childrenReady=new Vector<Task<T>>();
		childrenWaiting=new Hashtable<Task<?>,Task<T>>();
		childrenFinished=new Vector<Task<T>>();
		exception=null;
	}
		
	public Task(T object){
		this(object, (int)(Math.random()*Integer.MAX_VALUE), DEFAULT_PRIORITY, DEFAULT_INTRA_FAMILY_PRIORITY,
				DEFAULT_ROOT_PARENT_ID, DEFAULT_ROOT_PARENT_ID );
		this.familyId=this.id; //Root task, head of the family
	}
	
	
	/**
	 * Makes a new task that represents a rebirth of the current one.
	 * All parameters like: id, priority, parentId, computationTime, and current 
	 * instruction stack are preserved.
	 * The child (subtasks) references are not preserved,
	 * and the contained object is the one passed as parameter.
	 * @param object The new object to be hold in this task.
	 * @return A new birth of the current task containting object
	 */
	public <R> Task<R> reBirth(R object){
		Task<R> newMe = new Task<R>(object,id, priority, intraFamilyPri,parentId, familyId);
		newMe.setStack(this.stack);
		
		newMe.isDummy=this.isDummy;
		newMe.isTainted=this.isTainted;
		
		newMe.stats=this.stats;
		
		return newMe;
	}
	
	public int compareTo(Task task){
		//return task.priority - this.priority;
		int comp;
		
		//priority tasks go first
		comp = task.priority - this.priority;
		if(comp !=0) return comp;

		//if priority is tied then consider fifo order of root task
		comp= task.familyId - this.familyId;
		if(comp !=0) return comp;
		
		//if two tasks belong to the same famility then consider family hierarchy
		return task.intraFamilyPri-this.intraFamilyPri;
	}
	
	@Override
	public int hashCode(){
		return id;
	}
	
	
	public boolean equals(Task<T> task){
		return this.hashCode()==task.hashCode();
		
	}
	
	@Override
	public boolean equals(Object o){
		return this.hashCode() == o.hashCode();
	}
	
	/**
	 * Gives a not so shallow copy of the stack. Modifications to the return value
	 * stack will not be reflected on the Task's stack. But, modifications on the
	 * stack objects will be reflected. 
	 * @return
	 */
	public Vector<Instruction<?,?>> getStack(){
		return getVector(stack);
	}
	
	/**
	 * Sets a not so shallow reference to the parameter stack.
	 * Further modifications on the parameter will not modify the internal stack.
	 * But, modifications on the stack values will be modified.
	 * @param v
	 */
	public void setStack(Vector<Instruction<?,?>> v){		
		setVector(stack, v);
	}
	
	private <E> Vector<E> getVector(Vector<E> vector){
		
		Iterator<E> it = vector.iterator();
		
		Vector<E> v = new Vector<E>();
		while(it.hasNext()){
			v.add(it.next());
		}
		return v;	
	}
	
	private <E> void setVector(Vector<E> oldVector, Vector<E> newVector){
		oldVector.clear();
		
		Iterator<E> it = newVector.iterator();
		while(it.hasNext()){
			oldVector.add(it.next());
		}
	}

	/**
	 * @return Returns the parent task's id.
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parent Sets the parent task's id.
	 */
	private void setParent(int parentId) {
		this.parentId = parentId;
	}

	public void setFamily(int familyId) {
		this.familyId=familyId;
	}
	
	public boolean hasInstruction(){
		return !stack.isEmpty();
	}
	
	public boolean hasReadyChildTask(){
		return !childrenReady.isEmpty();
	}
	
	public Instruction<?,?> popInstruction(){
		
		Instruction<?,?> c = stack.remove(stack.size()-1);

		return c;
	}
	
	public Instruction<?,?> peekInstruction(){
		
		return stack.get(stack.size()-1);
	}
	
	public void pushInstruction(Instruction<?,?> inst){
		stack.add(inst);
	}
		
	public T getObject(){
		return param;	
	}
	
	public void setObject(T object){
		this.param=object;
	}

	/**
	 * @return Returns the id of the task.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return Returns the priority.
	 */
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int newPriority) {
		this.priority=newPriority;
	}
	
	/**
	 * Gets a sub task ready for execution. Internally,
	 * the subtask will be remembered by putting it in
	 * the waite queue.
	 * @return A sub task ready for execution.
	 */
	public synchronized Task<T> getReadyChild(){
		
		if(childrenReady.isEmpty()) return null;
		
		Task<T> task=this.childrenReady.remove(0);
		this.childrenWaiting.put(task, task);
		return task;
	}
	
	/**
	 * Adds a subtask ready for execution. This subtask
	 * will be configured with the parent id (this task's id),
	 * and also with a priority higher than it's parent.
	 * @param child The sub task.
	 */
	public synchronized void addReadyChild(Task<?> child){
		child.setPriority(getPriority()); //child has better priority than parent
		child.setParent(getId());
		child.setFamily(getFamilyId());
		child.setIntraFamilyPri(getIntraFamilyPri());
		this.childrenReady.add((Task<T>)child);
	}

	@SuppressWarnings("unchecked")
	public synchronized boolean setFinishedChild(Task<?> task){
		
		if(!task.isFinished()){
			logger.error("Task id="+task+" claims to be unfinished.");
			return false;
		}
		
		if(task.getParentId()!=this.getId()) {
			logger.error("Setting other task's child as my child: child.id="+" task.parent.id="+task.parentId);
			return false; //not my child
		}
		
		if(!this.childrenWaiting.containsKey(task)){
			logger.error("Parent id="+this.id+" not waiting for child: task.id="+task.id);
			return false;
		}
		
		childrenWaiting.remove(task);
		childrenFinished.add((Task<T>)task);
		
		stats.addChildStats(task.getStats());
		
		return true;
	}
	
	public synchronized boolean isFinished(){
		return isReady() &&  !hasInstruction();
	}
	
	public synchronized boolean isReady(){
		return childrenReady.isEmpty() &&
		   childrenWaiting.isEmpty();
	}
	
	
	public synchronized boolean hasFinishedChild(){
		return !this.childrenFinished.isEmpty();
	}
	
	public synchronized Task<T> getFinishedChild(){
		
		if(childrenFinished.isEmpty()){
			logger.error("No finished child available");
			return null;
		}
		
		return childrenFinished.remove(0);
	}
	
	public boolean isRootTask(){
		return this.parentId==DEFAULT_ROOT_PARENT_ID;
	}
	
	@Override
	public String toString(){
		return this.familyId+"|"+parentId+"."+this.id;
	}

	public boolean isDummy() {
		return isDummy;
	}

	public void setDummy() {
		isDummy=true;
	}

	/**
	 * @return Returns the exception.
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @param exception The exception to set.
	 */
	public void setException(Exception exception) {
		exception.printStackTrace();
		this.exception = exception;
	}
	
	public boolean hasException(){
		return this.exception !=null;
	}

	/**
	 * @return Returns the familyId.
	 */
	public int getFamilyId() {
		return familyId;
	}

	/**
	 * @return true if this task is tainted
	 */
	public boolean isTainted() {
		return isTainted;
	}

	/**
	 * @param isTainted true sets this task to tainted
	 */
	public void setTainted(boolean isTainted) {
		this.isTainted = isTainted;
	}
	
	public void markFinishTime(){
		stats.markFinishTime();
	}
	
	public StatsImpl getStats(){
		return stats;
	}

	/**
	 * @param intraFamilyPri the intraFamilyPri to set
	 */
	public void setIntraFamilyPri(int intraFamilyPri) {
		this.intraFamilyPri = intraFamilyPri;
	}

	/**
	 * @return the intraFamilyPri
	 */
	public int getIntraFamilyPri() {
		return intraFamilyPri;
	}
}
