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
package org.ow2.proactive.scheduler.common.job.factories;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
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
        public List<TaskTree> children = new ArrayList<TaskTree>();
        // bottom-up dependencies
        public List<TaskTree> parents = new ArrayList<TaskTree>();
        // enclosed Task
        public Task element = null;

        // if / else top-down links
        public List<TaskTree> targets = new ArrayList<TaskTree>();
        // if / else bottom-up links
        public TaskTree targetOf = null;
        // join top-down target
        public TaskTree targetJoin = null;
        // join bottom-up links
        public List<TaskTree> joins = new ArrayList<TaskTree>();
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
            this.blocks = new ArrayList<Block>();
        }
        blocks.clear();
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
            fc.checkReachable();
            fc.checkBlocks();
            fc.checkDuplicate();
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
        HashSet<String> tasks = new HashSet<String>();
        for (Task task : job.getTasks()) {
            String name = task.getName();

            if (name.indexOf(TaskId.iterationSeparator) != -1) {
                throw new FlowError("Task name cannot contain special character '" +
                    TaskId.iterationSeparator + "':" + name);
            }
            if (name.indexOf(TaskId.duplicationSeparator) != -1) {
                throw new FlowError("Task name cannot contain special character '" +
                    TaskId.duplicationSeparator + "':" + name);
            }

            if (tasks.contains(name)) {
                FlowError err = new FlowError("Task names are not unique");
                err.addTask(name);
                throw err;
            } else {
                tasks.add(name);
            }
        }
    }

    /**
     * Check whether or not every tasks of the given tasks flow can be reached.
     * Happens with dependency cycles.
     * 
     * @return FlowError
     */
    private void checkReachable() throws FlowError {
        HashSet<Task> tasks = new HashSet<Task>();
        HashSet<Task> reached = new HashSet<Task>();
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
            FlowError err = new FlowError("Some tasks can not be reached");
            for (Task t : job.getTasks()) {
                if (!reached.contains(t)) {
                    err.addTask(t.getName());
                }
            }
            throw err;
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
        Set<String> done = new HashSet<String>();
        // detect blocks
        for (TaskTree tt : roots) {
            Stack<TaskTree> env = new Stack<TaskTree>();
            Stack<TaskTree> join = new Stack<TaskTree>();

            dfsBlocks(tt, done, env, join);
            if (env.size() > 0) {
                FlowError err = new FlowError("Unmatched start block");
                err.addTask(env.firstElement().element.getName());
                throw err;
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
                    FlowError err = new FlowError("Unmatched end block", e);
                    err.addTask(name);
                    throw err;
                }
                Block blk = new Block(start, tree);
                //   start.element.setMatchingBlock(tree.element.getName());
                //   tree.element.setMatchingBlock(start.element.getName());
                blocks.add(blk);
                break;
            case NONE:
                break;
        }

        List<TaskTree> children = new ArrayList<TaskTree>();
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
        List<TaskTree> roots = new ArrayList<TaskTree>();
        // all tree nodes
        Map<String, TaskTree> tasks = new HashMap<String, TaskTree>();

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
                String tJ = treeDown.element.getFlowScript().getActionJoin();
                if (tT != null) {
                    TaskTree tt = tasks.get(tT);
                    if (tt == null) {
                        FlowError err = new FlowError("IF target if null");
                        err.addTask(tT);
                        throw err;
                    }
                    if (tt.targetOf != null) {
                        FlowError err = new FlowError("Task is target of multiple IF actions");
                        err.addTask(tT);
                        throw err;
                    } else {
                        tt.targetOf = treeDown;
                        treeDown.targets.add(tt);
                    }
                }
                if (tE != null) {
                    TaskTree tt = tasks.get(tE);
                    if (tt == null) {
                        FlowError err = new FlowError("ELSE target if null");
                        err.addTask(tE);
                        throw err;
                    }
                    if (tt.targetOf != null) {
                        FlowError err = new FlowError("Task is target of multiple IF actions");
                        err.addTask(tE);
                        throw err;
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
                String tJ = tree.element.getFlowScript().getActionJoin();

                if (tJ != null && tJ.length() > 0) {

                    TaskTree ifT = tasks.get(tree.element.getFlowScript().getActionTarget());
                    TaskTree elseT = tasks.get(tree.element.getFlowScript().getActionTargetElse());
                    List<TaskTree> tgs = new ArrayList<TaskTree>();
                    tgs.add(ifT);
                    tgs.add(elseT);
                    for (TaskTree tree2 : tgs) {

                        TaskTree target = tree2, target2 = null;
                        Stack<String> jOpen = new Stack<String>();
                        TaskTree joinTask = tasks.get(tJ);

                        do {
                            target2 = target;
                            if (target.element.getFlowScript() != null &&
                                target.element.getFlowScript().getActionJoin() != null) {
                                String jT = target.element.getFlowScript().getActionJoin();
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

        this.tasksFlat = new ArrayList<TaskTree>();
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
        List<TaskTree> children = new ArrayList<TaskTree>();
        children.addAll(node.children);

        children.addAll(node.targets);
        if (node.joinedBy != null) {
            children.add(node.joinedBy);
        }

        if (node.element.getName().equals(endBlock.element.getName())) {
            return;
        } else if (children.size() == 0) {
            FlowError err = new FlowError("Task Block ending at " + endBlock.element.getName() +
                " does not join all its flows");
            err.addTask(node.element.getName());
            throw err;
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
        List<TaskTree> parents = new ArrayList<TaskTree>();
        parents.addAll(node.parents);

        parents.addAll(node.joins);
        if (node.targetOf != null) {
            parents.add(node.targetOf);
        }

        if (node.element.getName().equals(startBlock.element.getName())) {
            return;
        } else if (parents.size() == 0) {
            FlowError err = new FlowError("Task Block starting at " + startBlock.element.getName() +
                " has external dependencies");
            err.addTask(node.element.getName());
            throw err;
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
     * Checks the provided taskflow against rules specific to the DUPLICATE control flow action
     * 
     * @param job the job to check
     * @throws FlowError
     */
    private void checkDuplicate() throws FlowError {
        for (TaskTree tree : tasksFlat) {
            if (tree.element.getFlowScript() != null &&
                tree.element.getFlowScript().getActionType().equals(FlowActionType.DUPLICATE.toString())) {

                for (TaskTree child : tree.children) {
                    if (child.parents.size() != 1) {
                        FlowError err = new FlowError(
                            "The Target of a DUPLICATE must have only one dependency");
                        err.addTask(child.element.getName());
                        throw err;
                    }
                    if (child.element.getFlowBlock().equals(FlowBlock.END)) {
                        FlowError err = new FlowError(
                            "The target of a DUPLICATE cannot be the end of a task block");
                        err.addTask(child.element.getName());
                        throw err;
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
                        FlowError err = new FlowError("No merge point for DUPLICATE block");
                        err.addTask(endBlock.element.getName());
                        throw err;
                    }

                    if (endBlock.element.getFlowScript() != null) {
                        if (endBlock.element.getFlowScript().getActionType().equals(
                                FlowActionType.DUPLICATE.toString()) ||
                            endBlock.element.getFlowScript().getActionType().equals(
                                    FlowActionType.IF.toString())) {
                            FlowError err = new FlowError(
                                "Last action of a DUPLICATE block cannot perform IF or DUPLICATE action");
                            err.addTask(endBlock.element.getName());
                            throw err;
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
                TaskTree targetJoin = findTask(tree.element.getFlowScript().getActionJoin());

                if (targetIf == null) {
                    FlowError err = new FlowError("IF action has no target");
                    err.addTask(tree.element.getName());
                    throw err;
                }
                if (targetElse == null) {
                    FlowError err = new FlowError("IF action has no ELSE target");
                    err.addTask(tree.element.getName());
                    throw err;
                }
                if (targetIf.equals(targetElse)) {
                    FlowError err = new FlowError("IF and ELSE targets are the same");
                    err.addTask(targetIf.element.getName());
                    err.addTask(targetElse.element.getName());
                    throw err;
                }

                // No join : IF and ELSE are /loose/ blocks
                if (targetJoin == null) {
                    if (targetIf.parents.size() > 0) {
                        FlowError err = new FlowError("IF target task cannot have dependencies");
                        err.addTask(targetIf.element.getName());
                        throw err;
                    }
                    if (targetElse.parents.size() > 0) {
                        FlowError err = new FlowError("IF target task ELSE cannot have dependencies");
                        err.addTask(targetElse.element.getName());
                        throw err;
                    }

                    List<TaskTree> targets = new ArrayList<TaskTree>();
                    targets.add(targetIf);
                    targets.add(targetElse);

                    for (TaskTree target : targets) {
                        Map<String, TaskTree> ifTasks = new HashMap<String, TaskTree>();
                        Stack<TaskTree> stack = new Stack<TaskTree>();
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
                                FlowError err = new FlowError("IF block at " + target.element.getName() +
                                    " has external dependencies");
                                for (String str : e.getTasks()) {
                                    err.addTask(str);
                                }
                                throw err;
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
                            FlowError err = new FlowError("IF action target is not a Task Block");
                            err.addTask(targetIf.element.getName());
                            throw err;
                        }
                    }

                    // else is a block or a single task
                    if (elseBlock == null) {
                        if (!(targetElse.children.size() > 0 || targetElse.targets.size() > 0)) {
                            elseBlock = new Block(targetElse, targetElse);
                        } else {
                            FlowError err = new FlowError("IF action ELSE target is not a Task Block");
                            err.addTask(targetElse.element.getName());
                            throw err;
                        }
                    }

                    // join joins only one if
                    if (targetJoin != null) {
                        for (TaskTree join : targetJoin.joins) {
                            String jN = join.element.getName();

                            if (!(jN.equals(ifBlock.end.element.getName()) || jN.equals(elseBlock.end.element
                                    .getName()))) {
                                FlowError err = new FlowError("JOIN task merges multiple IF actions");
                                err.addTask(targetJoin.element.getName());
                                throw err;
                            }
                        }
                    }

                    if (ifBlock.start.parents.size() > 0) {
                        FlowError err = new FlowError("IF task block cannot have dependencies");
                        err.addTask(ifBlock.start.element.getName());
                        throw err;
                    }
                    if (ifBlock.end.children.size() > 0) {
                        FlowError err = new FlowError("IF task block cannot have children");
                        err.addTask(ifBlock.end.element.getName());
                        throw err;
                    }
                    if (elseBlock.start.parents.size() > 0) {
                        FlowError err = new FlowError("ELSE task block cannot have dependencies");
                        err.addTask(elseBlock.start.element.getName());
                        throw err;
                    }
                    if (elseBlock.end.children.size() > 0) {
                        FlowError err = new FlowError("ELSE task block cannot have children");
                        err.addTask(elseBlock.end.element.getName());
                        throw err;
                    }
                    if (targetJoin.parents.size() > 0) {
                        FlowError err = new FlowError("JOIN task cannot have dependencies");
                        err.addTask(targetJoin.element.getName());
                        throw err;
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
                    FlowError err = new FlowError("LOOP action has no target");
                    err.addTask(tree.element.getName());
                    throw err;
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
                        FlowError err = new FlowError("The scope of a LOOP action should be a Task Block");
                        err.addTask(tree.element.getName());
                        err.addTask(target.element.getName());
                        throw err;
                    }

                    if (target.parents.size() > 1) {
                        FlowError err = new FlowError("The Target of a LOOP must have only one dependency");
                        err.addTask(target.element.getName());
                        throw err;
                    }
                    /***
                    if (target.targetOf != null) {
                        FlowError err = new FlowError("Target of an IF/ELSE cannot be target of a LOOP");
                        err.addTask(target.element.getName());
                        err.addTask(target.targetOf.element.getName());
                        throw err;
                    }
                    if (target.joins != null && target.joins.size() > 0) {
                        FlowError err = new FlowError("JOIN task of an if action cannot be target of a LOOP");
                        err.addTask(target.element.getName());
                        throw err;
                    }
                    ***/
                }
            }
        }
    }
}
