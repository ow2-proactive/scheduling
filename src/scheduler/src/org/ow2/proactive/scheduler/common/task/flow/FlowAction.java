/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.flow;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Describes a Control Flow Action that enables workflow operations in TaskFlow jobs.
 * <p>
 * Different types are described in {@link FlowActionType}
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 * @see FlowActionType
 * 
 */
@PublicAPI
@Entity
@Table(name = "CONTROL_FLOW_ACTION")
@AccessType("field")
@Proxy(lazy = false)
public class FlowAction implements Serializable {

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hId;

    /** Type of the Action stored as a String for convenience,
     * see {@link FlowActionType#parse(String)} */
    @Column(name = "TYPE")
    private String type;

    /** Number of parallel runs if {@link #type} is {@link FlowActionType#REPLICATE} */
    @Column(name = "DUP_NUMBER")
    private int dupNumber = 1;

    /** Main action target if  {@link #type} is {@link FlowActionType#LOOP} 
     * or selected branch if {@link #type} {@link FlowActionType#IF} */
    @Column(name = "TARGET")
    private String target = "";

    /** Join task for If and Else branches if  {@link #type} is {@link FlowActionType#IF} */
    @Column(name = "TARGET_JOIN")
    private String targetJoin = "";

    /** Branch that was NOT chosen if  {@link #type} is {@link FlowActionType#IF} */
    @Column(name = "TARGET_ELSE")
    private String targetElse = "";

    /**
     * Default constructor
     */
    public FlowAction() {
        this.type = FlowActionType.CONTINUE.toString();
    }

    /**
     * Default constructor
     * 
     * @param type the default type
     */
    public FlowAction(FlowActionType type) {
        this.type = type.toString();
    }

    /**
     * @return the type of this FlowAction
     */
    public FlowActionType getType() {
        return FlowActionType.parse(this.type);
    }

    /**
     * @return the number of parallel runs if the type of 
     * this action is {@link FlowActionType#REPLICATE}
     */
    public int getDupNumber() {
        return this.dupNumber;
    }

    /**
     * @return the main action target if 
     * this action is {@link FlowActionType#REPLICATE} or
     * {@link FlowActionType#IF}
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * @param type the type of this FlowAction
     */
    public void setType(FlowActionType type) {
        this.type = type.toString();
    }

    /**
     * @param args the number of parallel runs if the type of 
     * this action is {@link FlowActionType#REPLICATE}
     */
    public void setDupNumber(int args) {
        this.dupNumber = args;
    }

    /**
     * @param t the main action target if 
     * this action is {@link FlowActionType#REPLICATE} or
     * {@link FlowActionType#IF}
     */
    public void setTarget(String t) {
        this.target = t;
    }

    /**
     * @return the name of the task that was NOT selected if
     *  this action is {@link FlowActionType#IF} 
     */
    public String getTargetElse() {
        return this.targetElse;
    }

    /**
     * @param s the name of the task that was NOT selected if
     *  this action is {@link FlowActionType#IF} 
     */
    public void setTargetElse(String s) {
        this.targetElse = s;
    }

    /**
     * @param t the Join task for the If and Else branches,
     *  if this action is {@link FlowActionType#IF}
     */
    public void setTargetJoin(String t) {
        this.targetJoin = t;
    }

    /**
     * @return the Join task for the If and else branches,
     *  if this action is {@link FlowActionType#IF}
     */
    public String getTargetJoin() {
        return this.targetJoin;
    }
}
