/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.scheduler.common.job.factories;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.FlowError.FlowErrorType;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;


/**
 * Static checking utility for TaskFlow Jobs
 * <p>
 * Checks a TaskFlow is correct against a set of well defined rules,
 * and attempts to hint at the problem when detected.
 * 
 *  
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 * 
 */
@PublicAPI
public class FlowChecker {

    /**
     * Dummy pair class containing two Task
     */
    public static class Block {
        public TaskTree start = null;
        public TaskTree end = null;

        public Block(TaskTree a, TaskTree b) {
            this.start = a;
            this.end = b;
        }
    }

    /**
     * Dummy double linked Task tree structure
     * natural Tasks only have bottom-up links
     */
    public static class TaskTree {
        // top-down dependencies
        public List<TaskTree> children = new ArrayList<>();
        // bottom-up dependencies
        public List<TaskTree> parents = new ArrayList<>();
        // enclosed Task
        public Task element = null;

        // if / else top-down links
        public List<TaskTree> targets = new ArrayList<>();
        // if / else bottom-up links
        public TaskTree targetOf = null;
        // join top-down target
        public TaskTree targetJoin = null;
        // join bottom-up links
        public List<TaskTree> joins = new ArrayList<>();
        // join top-down link
        public TaskTree joinedBy = null;

        public boolean joinTrigger = false;

        public TaskTree(Task e) {
            this.element = e;
        }

        @Override
        public String toString() {
            return element.getName();
        }
    }

    /**
     * Job to check
     */
    private TaskFlowJob job;

    /**
     * task blocks in the job
     */
    private List<Block> blocks;

    /**
     *  the tasks of the job in a double-linked tree structure
     *  only roots are exposed in the list
     */
    private List<TaskTree> roots;

    /**
     * the tasks of the job in a double-linked tree structure      
     */
    private List<TaskTree> tasksFlat;

    /**
     * Constructor
     * 
     * @param job the job to check
     * @throws FlowError 
     */
    private FlowChecker(TaskFlowJob job, List<Block> blocks) throws FlowError {
        this.job = job;
        if (blocks != null) {
            this.blocks = blocks;
        } else {
            this.blocks = new ArrayList<>();
        }
        this.blocks.clear();
        createTaskTree(job);
    }

    /**
     * Checks if the provided Job is valid and can be scheduled.
     * <p>
     * A call to this method should have no side-effect on the provided job,
     * nor the tasks contained.
     * 
     * @param job the job to validate
     * @return a FlowError if the Job is not valid, or null if it is valid.
     */
    public static FlowError validate(TaskFlowJob job) {
        return validate(job, null);
    }

    /**
     * Checks if the provided Job is valid and can be scheduled.
     * <p>
     * A call to this method should have no side-effect on the provided job,
     * nor the tasks contained.
     * 
     * @param job the job to validate
     * @param an empty list that will be filled by this method with
     *        the validated task blocks (pair of tasks) detected by the validator
     * @return a FlowError if the Job is not valid, or null if it is valid.
     */
    public static FlowError validate(TaskFlowJob job, List<Block> blocks) {
        FlowError error = null;
        try {
            FlowChecker fc = new FlowChecker(job, blocks);

            fc.checkNames();
            fc.checkRecursion();
            fc.checkReachable();
            fc.checkBlocks();
            fc.checkReplicate();
            fc.checkLoop();
            fc.checkIf();
        } catch (FlowError e) {
            error = e;
        }

        return error;
    }

    /**
     * Enforces job name uniqueness ; already done on XML side, but API manipulations
     * are unchecked
     * 
     * @param job the job to validate
     * @throws FlowError
     */
    private void checkNames() throws FlowError {
        HashSet<String> tasks = new HashSet<>();
        for (Task task : job.getTasks()) {
            String name = task.getName();

            if (name.indexOf(TaskId.iterationSeparator) != -1) {
                throw new FlowError("Task name cannot contain special character '" +
                    TaskId.iterationSeparator + "'", FlowErrorType.NAME, name);
            }
            if (name.indexOf(TaskId.replicationSeparator) != -1) {
                throw new FlowError("Task name cannot contain special character '" +
                    TaskId.replicationSeparator + "'", FlowErrorType.NAME, name);
            }

            if (tasks.contains(name)) {
                throw new FlowError("Task names are not unique", FlowErrorType.NAME, name);
            } else {
                tasks.add(name);
            }
        }
    }

    /**
     * Check no infinite loop is defined using dependencies or IF/ELSE/JOIN links;
     * loop termination through LOOP cannot be checked statically
     * 
     * @throws FlowError
     */
    private void checkRecursion() throws FlowError {
        for (TaskTree tree : this.roots) {
            LinkedList<TaskTree> env = new LinkedList<>();
            internalCheckRecursion(env, tree);
        }
    }

    private void internalCheckRecursion(LinkedList<TaskTree> env, TaskTree cur) throws FlowError {

        for (TaskTree t : env) {
            if (t.equals(cur)) {
                throw new FlowError("Infinite recursion detected", FlowErrorType.RECURSION, t.element
                        .getName());
            }
        }
        env.addFirst(cur);

        if (cur.children != null && cur.children.size() > 0) {
            for (TaskTree child : cur.children) {
                LinkedList<TaskTree> n = new LinkedList<>(env);
                internalCheckRecursion(n, child);
            }
        }
        if (cur.targets != null && cur.targets.size() > 0) {
            for (TaskTree child : cur.targets) {
                LinkedList<TaskTree> n = new LinkedList<>(env);
                internalCheckRecursion(n, child);
            }
        }
        if (cur.joinedBy != null) {
            LinkedList<TaskTree> n = new LinkedList<>(env);
            internalCheckRecursion(n, cur.joinedBy);
        }
    }

    /**
     * Check whether or not every tasks of the given tasks flow can be reached.
     * Happens with dependency cycles.
     * 
     * @return FlowError
     */
    private void checkReachable() throws FlowError {
        HashSet<Task> tasks = new HashSet<>();
        HashSet<Task> reached = new HashSet<>();
        for (Task t : job.getTasks()) {
            if (t.getDependencesList() == null) {
                reached.add(t);
            } else {
                tasks.add(t);
            }
        }
        boolean change;
        do {
            change = false;
            Iterator<Task> it = tasks.iterator();
            while (it.hasNext()) {
                Task t = it.next();
                if (reached.containsAll(t.getDependencesList())) {
                    it.remove();
                    reached.add(t);
                    change = true;
                }
            }
        } while (change);
        if (reached.size() != job.getTasks().size()) {
            for (Task t : job.getTasks()) {
                if (!reached.contains(t)) {
                    throw new FlowError("Unreachable task", FlowErrorType.UNREACHABLE, t.getName());
                }
            }
        }
    }

    /**
     * Checks all declared blocks are correct
     * 
     * @param job the job to check
     * @return a list of valid blocks
     * @throws FlowError
     */
    private void checkBlocks() throws FlowError {
        Set<String> done = new HashSet<>();
        // detect blocks
        for (TaskTree tt : roots) {
            Stack<TaskTree> env = new Stack<>();
            Stack<TaskTree> join = new Stack<>();

            dfsBlocks(tt, done, env, join);
            if (env.size() > 0) {
                throw new FlowError("Unmatched start block", FlowErrorType.BLOCK, env.firstElement().element
                        .getName());
            }
        }

        // check blocks
        for (Block b : blocks) {
            checkBlockDown(b.end, b.start);
            checkBlockUp(b.start, b.end);
        }
    }

    /**
     * Find matching start and end blocks in a task tree using depth first search
     * 
     * @param tree task tree to search
     * @param done already treated tasks; multiple dependencies: multiple passes
     * @param env accumulates the previously read start tags
     * @param join stacks previous join targets
     * @throws FlowError
     */
    private void dfsBlocks(TaskTree tree, Set<String> done, Stack<TaskTree> env, Stack<TaskTree> join)
            throws FlowError {

        if (tree.joins.size() > 0 && !tree.joinTrigger) {
            return;
        }

        if (tree.targetOf != null && !done.contains(tree.targetOf.element.getName())) {
            return;
        }

        FlowBlock fb = tree.element.getFlowBlock();
        String name = tree.element.getName();

        if (done.contains(name)) {
            return;
        } else {
            done.add(name);
        }

        switch (fb) {
            case START:
                // push new opening tag in the environment
                env.push(tree);
                break;
            case END:
                // close the last opened block
                TaskTree start = null;
                try {
                    start = env.pop();
                } catch (EmptyStackException e) {
                    throw new FlowError("Unmatched end block", FlowErrorType.BLOCK, name);
                }
                Block blk = new Block(start, tree);
                blocks.add(blk);
                break;
            case NONE:
                break;
        }

        List<TaskTree> children = new ArrayList<>();
        children.addAll(tree.children);

        if (tree.children.size() == 0) {
            if (tree.element.getFlowScript() != null &&
                tree.element.getFlowScript().getActionType().equals(FlowActionType.IF.toString())) {
                if (tree.targetJoin != null) {
                    join.add(tree.targetJoin);
                }
                for (TaskTree t : tree.targets) {
                    children.add(t);
                }
            } else if (join.size() > 0) {
                TaskTree pop = join.pop();
                children.add(pop);
                pop.joinTrigger = true;
            }
        }

        // recursive call
        for (TaskTree child : children) {
            dfsBlocks(child, done, env, join);
        }
    }

    /**
     * Created a double linked dependency tree of a job
     * 
     * @param job a job
     * @return a double linked tree representation of the parameter, as the list of roots
     * @throws FlowError 
     */
    private void createTaskTree(TaskFlowJob job) throws FlowError {
        // list of roots
        List<TaskTree> roots = new ArrayList<>();
        // all tree nodes
        Map<String, TaskTree> tasks = new HashMap<>();

        for (Task t : job.getTasks()) {
            TaskTree tt = new TaskTree(t);
            tasks.put(t.getName(), tt);
        }
        for (TaskTree treeDown : tasks.values()) {
            List<Task> deps = treeDown.element.getDependencesList();
            if (deps == null) {
                roots.add(treeDown);
            } else {
                for (Task dep : deps) {
                    TaskTree treeUp = tasks.get(dep.getName());
                    treeUp.children.add(treeDown);
                    treeDown.parents.add(treeUp);
                }
            }

            if (treeDown.element.getFlowScript() != null &&
                treeDown.element.getFlowScript().getActionType().equals(FlowActionType.IF.toString())) {
                String tT = treeDown.element.getFlowScript().getActionTarget();
                String tE = treeDown.element.getFlowScript().getActionTargetElse();
                String tJ = treeDown.element.getFlowScript().getActionContinuation();
                if (tT != null) {
                    TaskTree tt = tasks.get(tT);
                    if (tt == null) {
                        throw new FlowError("IF target is null", FlowErrorType.IF, treeDown.element.getName());
                    }
                    if (tt.targetOf != null) {
                        throw new FlowError("Task is target of multiple IF actions", FlowErrorType.IF, tT);
                    } else {
                        tt.targetOf = treeDown;
                        treeDown.targets.add(tt);
                    }
                }
                if (tE != null) {
                    TaskTree tt = tasks.get(tE);
                    if (tt == null) {
                        throw new FlowError("ELSE target is null", FlowErrorType.IF, treeDown.element
                                .getName());
                    }
                    if (tt.targetOf != null) {
                        throw new FlowError("Task is target of multiple IF actions", FlowErrorType.IF, tE);
                    } else {
                        tt.targetOf = treeDown;
                        treeDown.targets.add(tt);
                    }
                }
                if (tJ != null) {
                    treeDown.targetJoin = tasks.get(tJ);
                }
            }
        }

        for (TaskTree tree : tasks.values()) {
            if (tree.element.getFlowScript() != null &&
                tree.element.getFlowScript().getActionType().equals(FlowActionType.IF.toString())) {
                String tJ = tree.element.getFlowScript().getActionContinuation();

                if (tJ != null && tJ.length() > 0) {

                    TaskTree ifT = tasks.get(tree.element.getFlowScript().getActionTarget());
                    TaskTree elseT = tasks.get(tree.element.getFlowScript().getActionTargetElse());
                    List<TaskTree> tgs = new ArrayList<>();
                    tgs.add(ifT);
                    tgs.add(elseT);
                    for (TaskTree tree2 : tgs) {

                        TaskTree target = tree2, target2 = null;
                        Stack<String> jOpen = new Stack<>();
                        TaskTree joinTask = tasks.get(tJ);

                        do {
                            target2 = target;
                            if (target.element.getFlowScript() != null &&
                                target.element.getFlowScript().getActionContinuation() != null) {
                                String jT = target.element.getFlowScript().getActionContinuation();
                                if (jT != null && jT.length() > 0) {
                                    jOpen.push(jT);
                                }
                            }
                            target = null;
                            if (target2.children.size() > 0) {
                                target = target2.children.get(0);
                            }
                            if (target == null && target2.element.getFlowScript() != null) {
                                target = tasks.get(target2.element.getFlowScript().getActionTargetElse());
                            }
                            if (target == null && jOpen.size() > 0) {
                                target = tasks.get(jOpen.pop());
                            }
                        } while (target != null);

                        if (joinTask != null) {
                            joinTask.joins.add(target2);
                            target2.joinedBy = joinTask;
                        }
                    }
                }
            }
        }

        this.tasksFlat = new ArrayList<>();
        for (TaskTree t : tasks.values()) {
            tasksFlat.add(t);
        }

        this.roots = roots;
    }

    /**
     * Recursively checks a block's top-down dependency chain is consistent:
     * all flows passing through start should go through the end only
     * 
     * @param endBlock task at the end of the block
     * @param node the current node
     * @throws FlowError
     */
    private static void checkBlockDown(TaskTree endBlock, TaskTree node) throws FlowError {
        List<TaskTree> children = new ArrayList<>();
        children.addAll(node.children);

        children.addAll(node.targets);
        if (node.joinedBy != null) {
            children.add(node.joinedBy);
        }

        if (node.element.getName().equals(endBlock.element.getName())) {
            return;
        } else if (children.size() == 0) {
            throw new FlowError("Task Block ending at " + endBlock.element.getName() +
                " does not join all its flows", FlowErrorType.BLOCK, node.element.getName());
        } else {
            for (TaskTree child : children) {
                if (child != null) {
                    checkBlockDown(endBlock, child);
                }
            }
        }
    }

    /**
     * Recursively checks a block's bottom-up dependency chain is consistent:
     * all tasks depending from tasks in the block should pass through the start task only
     * 
     * @param startBlock task at the beginning of the block
     * @param node the current node
     * @throws FlowError
     */
    private static void checkBlockUp(TaskTree startBlock, TaskTree node) throws FlowError {
        List<TaskTree> parents = new ArrayList<>();
        parents.addAll(node.parents);

        parents.addAll(node.joins);
        if (node.targetOf != null) {
            parents.add(node.targetOf);
        }

        if (node.element.getName().equals(startBlock.element.getName())) {
            return;
        } else if (parents.size() == 0) {
            throw new FlowError("Task Block starting at " + startBlock.element.getName() +
                " has external dependencies", FlowErrorType.BLOCK, node.element.getName());
        } else {
            for (TaskTree parent : parents) {
                if (parent != null) {
                    checkBlockUp(startBlock, parent);
                }
            }
        }
    }

    /**
     * Find the TaskTree with the given name
     * 
     * @param task name of a TaskTree to find
     * @return the corresponding TaskTree, or null
     */
    private TaskTree findTask(String task) {
        for (TaskTree tree : this.tasksFlat) {
            if (tree.element.getName().equals(task)) {
                return tree;
            }
        }
        return null;
    }

    /**
     * Checks the provided taskflow against rules specific to the REPLICATE control flow action
     * 
     * @param job the job to check
     * @throws FlowError
     */
    private void checkReplicate() throws FlowError {
        for (TaskTree tree : tasksFlat) {
            if (tree.element.getFlowScript() != null &&
                tree.element.getFlowScript().getActionType().equals(FlowActionType.REPLICATE.toString())) {

                for (TaskTree child : tree.children) {
                    if (child.parents.size() != 1) {
                        throw new FlowError("The Target of a REPLICATE must have only one dependency",
                            FlowErrorType.REPLICATE, child.element.getName());
                    }
                    if (child.element.getFlowBlock().equals(FlowBlock.END)) {
                        throw new FlowError("The target of a REPLICATE cannot be the end of a task block",
                            FlowErrorType.REPLICATE, child.element.getName());
                    }
                    Block block = null;
                    for (Block b : this.blocks) {
                        if (b.start.element.getName().equals(child.element.getName())) {
                            block = b;
                        }
                    }
                    TaskTree endBlock = null;
                    if (block != null) {
                        endBlock = block.end;
                    } else {
                        endBlock = child;
                    }
                    if (endBlock.children.size() < 1) {
                        throw new FlowError("No merge point for REPLICATE block", FlowErrorType.REPLICATE,
                            endBlock.element.getName());
                    }

                    if (endBlock.element.getFlowScript() != null) {
                        if (endBlock.element.getFlowScript().getActionType().equals(
                                FlowActionType.REPLICATE.toString()) ||
                            endBlock.element.getFlowScript().getActionType().equals(
                                    FlowActionType.IF.toString())) {
                            throw new FlowError(
                                "Last action of a REPLICATE block cannot perform IF or REPLICATE action",
                                FlowErrorType.REPLICATE, endBlock.element.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks the provided taskflow against rules specific to the IF control flow action
     * 
     * @param job the job to check
     * @throws FlowError
     */
    private void checkIf() throws FlowError {
        for (TaskTree tree : tasksFlat) {
            if (tree.element.getFlowScript() != null &&
                tree.element.getFlowScript().getActionType().equals(FlowActionType.IF.toString())) {
                TaskTree targetIf = findTask(tree.element.getFlowScript().getActionTarget());
                TaskTree targetElse = findTask(tree.element.getFlowScript().getActionTargetElse());
                TaskTree targetJoin = findTask(tree.element.getFlowScript().getActionContinuation());

                if (targetIf == null) {
                    throw new FlowError("IF action has no target", FlowErrorType.IF, tree.element.getName());
                }
                if (targetElse == null) {
                    throw new FlowError("IF action has no ELSE target", FlowErrorType.IF, tree.element
                            .getName());
                }
                if (targetIf.equals(targetElse)) {
                    throw new FlowError("IF and ELSE targets are the same", FlowErrorType.IF,
                        targetIf.element.getName());
                }

                // No join : IF and ELSE are /loose/ blocks
                if (targetJoin == null) {
                    if (targetIf.parents.size() > 0) {
                        throw new FlowError("IF target task cannot have dependencies", FlowErrorType.IF,
                            targetIf.element.getName());
                    }
                    if (targetElse.parents.size() > 0) {
                        throw new FlowError("IF target task ELSE cannot have dependencies", FlowErrorType.IF,
                            targetElse.element.getName());
                    }

                    List<TaskTree> targets = new ArrayList<>();
                    targets.add(targetIf);
                    targets.add(targetElse);

                    for (TaskTree target : targets) {
                        Map<String, TaskTree> ifTasks = new HashMap<>();
                        Stack<TaskTree> stack = new Stack<>();
                        stack.push(target);
                        while (stack.size() > 0) {
                            TaskTree cur = stack.pop();
                            if (ifTasks.containsKey(cur.element.getName())) {
                                continue;
                            } else {
                                ifTasks.put(cur.element.getName(), cur);
                                for (TaskTree t : cur.children) {
                                    stack.push(t);
                                }
                                for (TaskTree t : cur.targets) {
                                    stack.push(t);
                                }
                            }
                        }

                        for (TaskTree t : ifTasks.values()) {
                            if (t.element.getName().equals(target.element.getName())) {
                                continue;
                            }
                            try {
                                checkBlockUp(target, t);
                            } catch (FlowError e) {
                                throw new FlowError("IF block at " + target.element.getName() +
                                    " has external dependencies", FlowErrorType.IF, e.getTask());
                            }
                        }
                    }

                }
                // join : IF and ELSE are blocks
                else {
                    Block ifBlock = null;
                    Block elseBlock = null;

                    // detect blocks
                    for (Block b : this.blocks) {
                        if (b.start.element.getName().equals(targetIf.element.getName())) {
                            ifBlock = b;
                        }
                        if (b.start.element.getName().equals(targetElse.element.getName())) {
                            elseBlock = b;
                        }
                    }

                    // if is a block or a single task
                    if (ifBlock == null) {
                        if (!(targetIf.children.size() > 0 || targetIf.targets.size() > 0)) {
                            ifBlock = new Block(targetIf, targetIf);
                        } else {
                            throw new FlowError("IF action target is not a Task Block", FlowErrorType.IF,
                                targetIf.element.getName());
                        }
                    }

                    // else is a block or a single task
                    if (elseBlock == null) {
                        if (!(targetElse.children.size() > 0 || targetElse.targets.size() > 0)) {
                            elseBlock = new Block(targetElse, targetElse);
                        } else {
                            throw new FlowError("IF action ELSE target is not a Task Block",
                                FlowErrorType.IF, targetElse.element.getName());
                        }
                    }

                    // join joins only one if
                    if (targetJoin != null) {
                        for (TaskTree join : targetJoin.joins) {
                            String jN = join.element.getName();

                            if (!(jN.equals(ifBlock.end.element.getName()) || jN.equals(elseBlock.end.element
                                    .getName()))) {
                                throw new FlowError("JOIN task merges multiple IF actions", FlowErrorType.IF,
                                    targetJoin.element.getName());
                            }
                        }
                    }

                    if (ifBlock.start.parents.size() > 0) {
                        throw new FlowError("IF task block cannot have dependencies", FlowErrorType.IF,
                            ifBlock.start.element.getName());
                    }
                    if (ifBlock.end.children.size() > 0) {
                        throw new FlowError("IF task block cannot have children", FlowErrorType.IF,
                            ifBlock.end.element.getName());
                    }
                    if (elseBlock.start.parents.size() > 0) {
                        throw new FlowError("ELSE task block cannot have dependencies", FlowErrorType.IF,
                            elseBlock.start.element.getName());
                    }
                    if (elseBlock.end.children.size() > 0) {
                        throw new FlowError("ELSE task block cannot have children", FlowErrorType.IF,
                            elseBlock.end.element.getName());
                    }
                    if (targetJoin.parents.size() > 0) {
                        throw new FlowError("JOIN task cannot have dependencies", FlowErrorType.IF,
                            targetJoin.element.getName());
                    }
                }
            }
        }
    }

    /**
     * Checks the provided taskflow against rules specific to the LOOP control flow action
     * 
     * @param job the job to check
     * @throws FlowError
     */
    private void checkLoop() throws FlowError {
        for (TaskTree tree : tasksFlat) {
            if (tree.element.getFlowScript() != null &&
                tree.element.getFlowScript().getActionType().equals(FlowActionType.LOOP.toString())) {
                TaskTree target = findTask(tree.element.getFlowScript().getActionTarget());
                if (target == null) {
                    throw new FlowError("LOOP action has no target", FlowErrorType.LOOP, tree.element
                            .getName());
                } else {
                    boolean isBlock = false;
                    for (Block b : this.blocks) {
                        if (b.start.element.getName().equals(target.element.getName()) &&
                            b.end.element.getName().equals(tree.element.getName())) {
                            isBlock = true;
                            break;
                        }
                    }
                    if (target.element.getName().equals(tree.element.getName()) &&
                        target.element.getFlowBlock().equals(FlowBlock.NONE) &&
                        tree.element.getFlowBlock().equals(FlowBlock.NONE)) {
                        isBlock = true;
                    }
                    if (!isBlock) {
                        throw new FlowError("The scope of a LOOP action should be a Task Block",
                            FlowErrorType.LOOP, tree.element.getName());
                    }

                    if (target.parents.size() > 1) {
                        throw new FlowError("The Target of a LOOP must have only one dependency",
                            FlowErrorType.LOOP, target.element.getName());
                    }
                }
            }
        }
    }
}
