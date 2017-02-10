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
package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Perform topological sort on a directed acyclic graph represented by
 * a collection of {@link Entry} instances (an entry corresponds to a
 * graph vertex). For each entry, its parent entries should be
 * returned by {@link Entry#getParents()}. The {@link #sort()} method
 * produces an ordering where every entry appears before any of its
 * children.
 *
 * The algorithm is the one based on depth-first search from this
 * wikipedia page:
 * <a href="http://en.wikipedia.org/w/index.php?title=Topological_sorting&oldid=642496240">Topological sorting</a>.
 */
public class TopologicalTaskSorter {

    private final Set<Entry> unmarked;

    private final Set<Entry> markedTemporarily;

    private final Map<Entry, Set<Entry>> entryChildren;

    private final List<Entry> result;

    public interface Entry {
        Collection<Entry> getParents();
    }

    /**
     * Sort a collection of {@link Entry} instances in topological order
     * @return a new List containing the entries in topological order
     * @throws NullPointerException if entries is null
     * @throws IllegalArgumentException if the graph of entries contains a cycle
     */
    public static List<Entry> sort(Collection<Entry> entries) {
        return new TopologicalTaskSorter(entries).sort();
    }

    /**
     * Sort a list of {@link InternalTask} instances in topological order
     * @return a new ArrayList containing the tasks in topological order
     * @throws NullPointerException if taskList is null
     * @throws IllegalArgumentException if the task graph contains a cycle
     */
    public static ArrayList<InternalTask> sortInternalTasks(List<InternalTask> taskList) {
        Collection<Entry> entries = InternalTaskEntry.fromInternalTasks(taskList);
        return InternalTaskEntry.toInternalTasks(sort(entries));
    }

    private TopologicalTaskSorter(Collection<Entry> entries) {
        unmarked = new HashSet<>(entries);
        markedTemporarily = new HashSet<>();
        entryChildren = findEntryChildren(entries);
        result = new LinkedList<>();
    }

    private List<Entry> sort() {
        while (!unmarked.isEmpty()) {
            visit(unmarked.iterator().next());
        }
        return result;
    }

    private void visit(Entry selected) {
        if (markedTemporarily.contains(selected)) {
            throw new IllegalArgumentException("The graph contains a cycle");
        }
        if (unmarked.contains(selected)) {
            markedTemporarily.add(selected);
            Set<Entry> children = entryChildren.get(selected);
            if (children != null) {
                for (Entry child : children) {
                    visit(child);
                }
            }
            unmarked.remove(selected);
            markedTemporarily.remove(selected);
            result.add(0, selected);
        }

    }

    private static Map<Entry, Set<Entry>> findEntryChildren(Collection<Entry> entries) {
        Map<Entry, Set<Entry>> entryChildren = new HashMap<>();
        for (Entry task : entries) {
            for (Entry parent : task.getParents()) {
                Set<Entry> children = entryChildren.get(parent);
                if (children == null) {
                    children = new HashSet<>();
                    entryChildren.put(parent, children);
                }
                children.add(task);
            }
        }
        return entryChildren;
    }

    public static class InternalTaskEntry implements Entry {

        private final InternalTask task;

        public InternalTaskEntry(InternalTask task) {
            this.task = task;
        }

        @Override
        public Collection<Entry> getParents() {
            ArrayList<InternalTask> allParents = new ArrayList<>();
            List<InternalTask> parents = task.getIDependences();
            if (parents != null) {
                allParents.addAll(parents);
            }
            List<InternalTask> joinParents = task.getJoinedBranches();
            if (joinParents != null) {
                allParents.addAll(joinParents);
            }
            InternalTask ifParent = task.getIfBranch();
            if (ifParent != null) {
                allParents.add(ifParent);
            }
            return fromInternalTasks(allParents);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            InternalTaskEntry that = (InternalTaskEntry) o;

            return !(task != null ? !task.equals(that.task) : that.task != null);

        }

        @Override
        public int hashCode() {
            return task != null ? task.hashCode() : 0;
        }

        public static Collection<Entry> fromInternalTasks(List<InternalTask> tasks) {
            Collection<Entry> result = new ArrayList<>(tasks.size());
            for (InternalTask task : tasks) {
                result.add(new InternalTaskEntry(task));
            }
            return result;
        }

        public static ArrayList<InternalTask> toInternalTasks(List<Entry> entries) {
            ArrayList<InternalTask> result = new ArrayList<>(entries.size());
            for (Entry entry : entries) {
                result.add(((InternalTaskEntry) entry).task);
            }
            return result;
        }
    }

}
