/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


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
@XmlAccessorType(XmlAccessType.FIELD)
public class FlowAction implements Serializable {

    /** Type of the Action stored as a String for convenience,
     * see {@link FlowActionType#parse(String)} */
    private String type;

    /** Number of parallel runs if {@link #type} is {@link FlowActionType#REPLICATE} */
    private int dupNumber = 1;

    /** Main action target if  {@link #type} is {@link FlowActionType#LOOP}
     * or selected branch if {@link #type} {@link FlowActionType#IF} */
    private String target = "";

    /** Continuation task for If and Else branches if  {@link #type} is {@link FlowActionType#IF} */
    private String targetContinuation = "";

    /** Branch that was NOT chosen if  {@link #type} is {@link FlowActionType#IF} */
    private String targetElse = "";

    private String cronExpr = "";

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
     * If a FlowScript cannot be performed and the execution of the
     * job must continue, a default 'neutral' action must be provided
     * <p>
     *
     * @param script the FlowScript defining the action type and parameters
     * @return the neutral FlowAction mathcing the provided parameters
     */
    public static FlowAction getDefaultAction(FlowScript script) {
        FlowAction ret = null;

        switch (FlowActionType.parse(script.getActionType())) {
            case REPLICATE:
                // this is equivalent to REPLICATE with runs==1
                ret = new FlowAction(FlowActionType.CONTINUE);
                break;
            case LOOP:
                ret = new FlowAction(FlowActionType.CONTINUE);
                break;
            case IF:
                // if we CONTINUE here the flow will be blocked indefinitely
                // we perform the IF action as if the first target was selected
                ret = new FlowAction(FlowActionType.IF);
                ret.setTarget(script.getActionTarget());
                ret.setTargetElse(script.getActionTargetElse());
                ret.setTargetContinuation(script.getActionContinuation());
                break;
            case CONTINUE:
                ret = new FlowAction(FlowActionType.CONTINUE);
                break;
        }

        return ret;
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
        if (type != null)
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
        if (t != null)
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
        if (s != null)
            this.targetElse = s;
    }

    /**
     * @param t the Continuation task for the If and Else branches,
     *  if this action is {@link FlowActionType#IF}
     */
    public void setTargetContinuation(String t) {
        if (t != null)
            this.targetContinuation = t;
    }

    /**
     * @return the Continuation task for the If and else branches,
     *  if this action is {@link FlowActionType#IF}
     */
    public String getTargetContinuation() {
        return this.targetContinuation;
    }

    public void setCronExpr(String cronExpr) {
        this.cronExpr = cronExpr;
    }

    public String getCronExpr() {
        return cronExpr;
    }
}
