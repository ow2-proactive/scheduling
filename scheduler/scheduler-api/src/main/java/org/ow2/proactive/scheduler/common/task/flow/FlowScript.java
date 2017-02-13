/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.task.flow;

import java.io.Reader;
import java.io.StringReader;

import javax.script.Bindings;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Predictor;


/**
 * Dynamic evaluation of this script determines at runtime if a specific 
 * Control Flow operation should be performed in a TaskFlow.
 * <p>
 * This class wraps information around a {@link org.ow2.proactive.scripting.Script}
 * to determine which {@link FlowAction} is attached to this script.
 * <p>
 * When using the action type {@link FlowActionType#REPLICATE}, the value of the 
 * {@link FlowScript#replicateRunsVariable} determines the number of parallel runs.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 * @see FlowAction
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class FlowScript extends Script<FlowAction> {

    /** String representation of a FlowActionType
     * see {@link FlowActionType#parse(String)} */
    private String actionType = null;

    // implementation note:
    // target / targetElse / targetContinuation
    // would be much better represented with an InternalTask or a TaskId, but :
    // - InternalTask cannot be used because of project setup :
    //   it is not exported on the worker on which this script executes;
    //   it cannot be used on user side, and this class is PublicAPI
    // - TaskId cannot be used by the user prior to submission
    // A complete solution would involve exposing one class to the user,
    // copying the info onto another more complete InternalFlowScript,
    // and holding a TaskId, which would present some of the problems
    // of the current string-based implementation
    //
    // In the end, using strings is less safe, but faster and simpler ;
    // also, this is only internals never exposed to the user

    /** Name of the target task of this action if it requires one */
    private String target = null;

    /** Name of the 'Else' target task if this action is an 'If' */
    private String targetElse = null;

    /** Name of the 'Continuation' target task if this action is an 'If' */
    private String targetContinuation = null;

    /** Name of the variable that will be set in the script's environment
     * to contain the result of the Task executed along this script */
    public static final String resultVariable = "result";

    /** Name of the boolean variable to set in the script to determine 
     * if a LOOP action is enabled or if the execution should continue */
    public static final String loopVariable = "loop";

    /** Name of the Integer variable to set in the script to determine
     * the number of parallel runs of a REPLICATE action */
    public static final String replicateRunsVariable = "runs";

    /** Name of the variable to set in the script to determine
     * which one of the IF or ELSE branch is selected in an IF
     * control flow action */
    public static final String branchSelectionVariable = "branch";

    /** Value to set {@link #branchSelectionVariable} to
     * signify the IF branch should be selected */
    public static final String ifBranchSelectedVariable = "if";

    /** Value to set {@link #branchSelectionVariable} to
     * signify the ELSE branch should be selected */
    public static final String elseBranchSelectedVariable = "else";

    /**
     * Hibernate default constructor,
     * use {@link #createContinueFlowScript()},
     * {@link #createLoopFlowScript(Script, String)} or
     * {@link #createReplicateFlowScript(Script)} to
     * create a FlowScript
     */
    public FlowScript() {
    }

    @Override
    protected String getDefaultScriptName() {
        return "FlowScript";
    }

    /**
     * Copy constructor 
     * 
     * @param fl Source script
     * @throws InvalidScriptException
     */
    public FlowScript(FlowScript fl) throws InvalidScriptException {
        super(fl);
        if (fl.getActionType() != null) {
            this.actionType = new String(fl.getActionType());
        }
        if (fl.getActionTarget() != null) {
            this.target = new String(fl.getActionTarget());
        }
        if (fl.getActionTargetElse() != null) {
            this.targetElse = new String(fl.getActionTargetElse());
        }
        if (fl.getActionContinuation() != null) {
            this.targetContinuation = new String(fl.getActionContinuation());
        }
    }

    private FlowScript(Script<?> scr) throws InvalidScriptException {
        super(scr);
    }

    public static FlowScript createContinueFlowScript() throws InvalidScriptException {
        FlowScript fs = new FlowScript(new SimpleScript("", "javascript"));
        fs.setActionType(FlowActionType.CONTINUE);
        return fs;
    }

    /**
     * Creates a Control Flow Script configured to perform a LOOP control flow action
     * the code will be run using a javascript engine
     * 
     * @param script code of the Javascript script 
     * @param target target of the LOOP action
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createLoopFlowScript(String script, String target) throws InvalidScriptException {
        return createLoopFlowScript(script, "javascript", target);
    }

    /**
     * Creates a Control Flow Script configured to perform a LOOP control flow action
     * 
     * @param script code of the script
     * @param engine engine running the script
     * @param target target of the LOOP action
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createLoopFlowScript(String script, String engine, String target)
            throws InvalidScriptException {
        Script<?> scr = new SimpleScript(script, engine);
        return createLoopFlowScript(scr, target);
    }

    /**
     * Creates a Control Flow Script configured to perform a LOOP control flow action
     * 
     * @param script the script to execute
     * @param target target of the LOOP action
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createLoopFlowScript(Script<?> script, String target) throws InvalidScriptException {
        FlowScript flow = new FlowScript(script);
        flow.setActionType(FlowActionType.LOOP);
        flow.setActionTarget(target);
        return flow;
    }

    /**
     * Creates a Control Flow Script configured to perform an IF control flow action
     * the code will be run using a javascript engine
     * 
     * @param script code of the Javascript script 
     * @param targetIf IF branch
     * @param targetElse ELSE branch
     * @param targetCont CONTINUATION branch, can be null
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createIfFlowScript(String script, String targetIf, String targetElse, String targetCont)
            throws InvalidScriptException {
        return createIfFlowScript(script, "javascript", targetIf, targetElse, targetCont);
    }

    /**
     * Creates a Control Flow Script configured to perform an IF control flow action
     * 
     * @param script code of the script
     * @param engine engine running the script
     * @param targetIf IF branch
     * @param targetElse ELSE branch
     * @param targetCont CONTINUATION branch, can be null
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createIfFlowScript(String script, String engine, String targetIf, String targetElse,
            String targetCont) throws InvalidScriptException {
        Script<?> scr = new SimpleScript(script, engine);
        return createIfFlowScript(scr, targetIf, targetElse, targetCont);
    }

    /**
     * Creates a Control Flow Script configured to perform an IF control flow action
     * 
     * @param script the script to execute
     * @param targetIf IF branch
     * @param targetElse ELSE branch
     * @param targetCont CONTINUATION branch, can be null
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createIfFlowScript(Script<?> script, String targetIf, String targetElse, String targetCont)
            throws InvalidScriptException {
        FlowScript flow = new FlowScript(script);
        flow.setActionType(FlowActionType.IF);
        flow.setActionTarget(targetIf);
        flow.setActionTargetElse(targetElse);
        flow.setActionContinuation(targetCont);
        return flow;
    }

    /**
     * Creates a Control Flow Script configured to perform a REPLICATE control flow action
     * the code will be run using a javascript engine
     * 
     * @param script code of the Javascript script 
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createReplicateFlowScript(String script) throws InvalidScriptException {
        return createReplicateFlowScript(script, "javascript");
    }

    /**
     * Creates a Control Flow Script configured to perform a Replicate control flow action
     * 
     * @param script code of the script
     * @param engine engine running the script
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createReplicateFlowScript(String script, String engine) throws InvalidScriptException {
        Script<?> scr = new SimpleScript(script, engine);
        return createReplicateFlowScript(scr);
    }

    /**
     * Creates a Control Flow Script configured to perform a REPLICATE control flow action
     * 
     * @param script the script to execute
     * @return a newly allocated and configured Control Flow Script
     * @throws InvalidScriptException
     */
    public static FlowScript createReplicateFlowScript(Script<?> script) throws InvalidScriptException {
        FlowScript flow = new FlowScript(script);
        flow.setActionType(FlowActionType.REPLICATE);
        return flow;
    }

    /**
     * The Action Type does not have any effect on the execution of the script,
     * but will be used after the execution to determine what Control Flow Action
     * should be performed on the TaskFlow.
     * 
     * @param actionType the String representation of the new ActionType of this script,
     * @see FlowActionType#parse(String)
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * The Action Type does not have any effect on the execution of the script,
     * but will be used after the execution to determine what Control Flow Action
     * should be performed on the TaskFlow.
     * 
     * @param type the ActionType of this script,
     */
    public void setActionType(FlowActionType type) {
        this.actionType = type.toString();
    }

    /**
     * The Action Type does not have any effect on the execution of the script,
     * but will be used after the execution to determine what Control Flow Action
     * should be performed on the TaskFlow.
     * 
     * @return the String representation of the ActionType of this script,
     * @see FlowActionType#parse(String)
     */
    public String getActionType() {
        return this.actionType;
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#LOOP}, the target is the entry point of the next loop iteration.
     * If the Action type is {@link FlowActionType#IF}, the target is the branch executed when 
     * the If condition succeeds.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @param target the main target of the action of this script.
     */
    public void setActionTarget(String target) {
        this.target = target;
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#LOOP}, the target is the entry point of the next loop iteration.
     * If the Action type is {@link FlowActionType#IF}, the target is the branch executed when 
     * the If condition succeeds.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @param target the main target of the action of this script.
     */
    public void setActionTarget(Task target) {
        this.target = target.getName();
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#LOOP}, the target is the entry point of the next loop iteration.
     * If the Action type is {@link FlowActionType#IF}, the target is the branch executed when 
     * the If condition succeeds.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @return the main target of the action of this script
     */
    public String getActionTarget() {
        return this.target;
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#IF}, the targetElse is the branch executed when 
     * the If condition fails.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @param target the Else target of the action of this script
     */
    public void setActionTargetElse(String target) {
        this.targetElse = target;
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#IF}, the targetElse is the branch executed when 
     * the If condition fails.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @param target the Else target of the action of this script
     */
    public void setActionTargetElse(Task target) {
        this.targetElse = target.getName();
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#IF}, the targetElse is the branch executed when 
     * the If condition fails.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @return the Else target of the action of this script
     */
    public String getActionTargetElse() {
        return this.targetElse;
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#IF}, the targetContinuation is the Task on which both
     * if and else branches will join after either one has been executed.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @param target the Continuation target of the action of this script
     */
    public void setActionContinuation(String target) {
        this.targetContinuation = target;
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#IF}, the targetContinuation is the Task on which both
     * if and else branches will join after either one has been executed.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @param target the Continuation target of the action of this script
     */
    public void setActionContinuation(Task target) {
        this.targetContinuation = target.getName();
    }

    /**
     * If the Action type (see {@link #getActionType()}) of this FlowScript 
     * is {@link FlowActionType#IF}, the targetContinuation is the Task on which both
     * if and else branches will join after either one has been executed.
     * <p>
     * This value has no effect on the execution of the script
     * 
     * @return the Continuation target of the action of this script
     */
    public String getActionContinuation() {
        return this.targetContinuation;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    protected Reader getReader() {
        return new StringReader(script);
    }

    @Override
    protected ScriptResult<FlowAction> getResult(Object evalResult, Bindings bindings) {
        try {
            FlowAction act = new FlowAction();

            /*
             * no action defined
             */
            if (this.actionType == null || this.actionType.equals(FlowActionType.CONTINUE.toString())) {
                act.setType(FlowActionType.CONTINUE);
            }

            /*
             * loop
             */
            else if (this.actionType.equals(FlowActionType.LOOP.toString())) {
                if (this.target == null) {
                    String msg = "LOOP control flow action requires a target";
                    logger.error(msg);
                    return new ScriptResult<FlowAction>(new Exception(msg));
                } else {
                    if (bindings.containsKey(loopVariable)) {
                        Boolean enabled;
                        String loopValue = bindings.get(loopVariable).toString();
                        if ("true".equalsIgnoreCase(loopValue)) {
                            enabled = Boolean.TRUE;
                        } else if ("false".equalsIgnoreCase(loopValue)) {
                            enabled = Boolean.FALSE;
                        } else {
                            try {
                                (new Predictor(loopValue)).nextMatchingDate();
                                enabled = Boolean.TRUE;
                                act.setCronExpr(loopValue);
                            } catch (InvalidPatternException e) {
                                enabled = Boolean.FALSE;
                            }
                        }
                        if (enabled) {
                            act.setType(FlowActionType.LOOP);
                            act.setTarget(this.target);
                        } else {
                            act.setType(FlowActionType.CONTINUE);
                        }
                    } else {
                        String msg = "Script environment for LOOP action needs to define variable " + loopVariable;
                        logger.error(msg);
                        return new ScriptResult<FlowAction>(new Exception(msg));
                    }
                }
            }
            /*
             * replicate
             */
            else if (this.actionType.equals(FlowActionType.REPLICATE.toString())) {
                if (bindings.containsKey(replicateRunsVariable)) {
                    act.setType(FlowActionType.REPLICATE);
                    int args = 1;
                    Object o = bindings.get(replicateRunsVariable);
                    try {
                        args = Integer.parseInt("" + o);
                    } catch (NumberFormatException e) {
                        try {
                            args = (int) Math.floor(Double.parseDouble("" + o));
                        } catch (Exception e2) {
                            String msg = "REPLICATE action: could not parse value for variable " +
                                         replicateRunsVariable;
                            logger.error(msg);
                            return new ScriptResult<FlowAction>(new Exception(msg, e2));
                        }
                    }
                    if (args < 0) {
                        String msg = "REPLICATE action: value of variable " + replicateRunsVariable +
                                     " cannot be negative";
                        logger.error(msg);
                        return new ScriptResult<FlowAction>(new Exception(msg));
                    }
                    act.setDupNumber(args);
                } else {
                    String msg = "Script environment for REPLICATE action needs to define variable " +
                                 replicateRunsVariable;
                    logger.error(msg);
                    return new ScriptResult<FlowAction>(new Exception(msg));
                }
            }
            /*
             * if
             */
            else if (this.actionType.equals(FlowActionType.IF.toString())) {
                if (this.target == null) {
                    String msg = "IF action requires a target ";
                    logger.error(msg);
                    return new ScriptResult<FlowAction>(new Exception(msg));
                } else if (this.targetElse == null) {
                    String msg = "IF action requires an ELSE target ";
                    logger.error(msg);
                    return new ScriptResult<FlowAction>(new Exception(msg));
                } else {
                    act.setType(FlowActionType.IF);

                    if (bindings.containsKey(branchSelectionVariable)) {
                        String val = new String((String) bindings.get(branchSelectionVariable));
                        if (val.toLowerCase().equals(ifBranchSelectedVariable)) {
                            act.setTarget(this.target);
                            act.setTargetElse(this.targetElse);
                        } else if (val.toLowerCase().equals(elseBranchSelectedVariable)) {
                            act.setTarget(this.targetElse);
                            act.setTargetElse(this.target);
                        } else {
                            String msg = "IF action: value for " + branchSelectionVariable + " needs to be one of " +
                                         ifBranchSelectedVariable + " or " + elseBranchSelectedVariable;
                            logger.error(msg);
                            return new ScriptResult<FlowAction>(new Exception(msg));
                        }
                    } else {
                        String msg = "Environment for IF action needs to define variable " + branchSelectionVariable;
                        logger.error(msg);
                        return new ScriptResult<FlowAction>(new Exception(msg));
                    }

                    if (this.targetContinuation != null) {
                        act.setTargetContinuation(this.targetContinuation);
                    }
                }
            }
            /*
             * unknown action
             */
            else {
                String msg = actionType + " action type unknown";
                logger.error(msg);
                return new ScriptResult<FlowAction>(new Exception(msg));
            }

            return new ScriptResult<FlowAction>(act);
        } catch (Throwable th) {
            return new ScriptResult<FlowAction>(th);
        }
    }

    @Override
    protected void prepareSpecialBindings(Bindings bindings) {
    }
}
