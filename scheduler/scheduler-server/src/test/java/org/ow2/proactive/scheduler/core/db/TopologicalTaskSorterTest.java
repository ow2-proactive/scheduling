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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive.scheduler.core.db.TopologicalTaskSorter.Entry;


public class TopologicalTaskSorterTest {

    static class TestEntry implements Entry {

        private final int id;

        List<Entry> parents = new ArrayList<>();

        public TestEntry(int i) {
            id = i;
        }

        @Override
        public Collection<Entry> getParents() {
            return parents;
        }

        public void addParent(Entry entry) {
            parents.add(entry);
        }

        @Override
        public String toString() {
            return "TestEntry{id=" + id + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            TestEntry testEntry = (TestEntry) o;

            return id == testEntry.id;

        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    @Test
    public void testEmpty() throws Exception {
        List<Entry> sorted = TopologicalTaskSorter.sort(Collections.<Entry> emptyList());
        assertEquals(0, sorted.size());
    }

    @Test(expected = NullPointerException.class)
    public void testNull() throws Exception {
        TopologicalTaskSorter.sort(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCycle() throws Exception {
        TestEntry e1 = new TestEntry(1);
        TestEntry e2 = new TestEntry(2);
        TestEntry e3 = new TestEntry(3);
        List<Entry> input = Arrays.<Entry> asList(e1, e2, e3);
        e2.addParent(e1);
        e3.addParent(e2);
        e1.addParent(e3);

        TopologicalTaskSorter.sort(input);
    }

    @Test
    public void testSort() throws Exception {
        for (int i = 0; i < 100; i++) {
            testSortRandomized();
        }
    }

    public void testSortRandomized() throws Exception {
        TestEntry e1 = new TestEntry(1);
        TestEntry e2 = new TestEntry(2);
        TestEntry e3 = new TestEntry(3);
        TestEntry e4 = new TestEntry(4);
        TestEntry e5 = new TestEntry(5);
        TestEntry e6 = new TestEntry(6);
        TestEntry e7 = new TestEntry(7);
        TestEntry e8 = new TestEntry(8);
        TestEntry e9 = new TestEntry(9);
        e3.addParent(e1);
        e3.addParent(e2);
        e4.addParent(e3);
        e5.addParent(e4);
        e6.addParent(e5);
        e6.addParent(e3);
        e7.addParent(e8);
        e4.addParent(e9);
        List<Entry> input = Arrays.<Entry> asList(e1, e2, e3, e4, e5, e6, e7, e8, e9);
        Collections.shuffle(input);

        List<Entry> sorted = TopologicalTaskSorter.sort(input);

        assertBefore(sorted, e1, e3);
        assertBefore(sorted, e2, e3);
        assertBefore(sorted, e3, e4);
        assertBefore(sorted, e4, e5);
        assertBefore(sorted, e5, e6);
        assertBefore(sorted, e3, e6);
        assertBefore(sorted, e8, e7);
        assertBefore(sorted, e9, e4);
    }

    @Ignore("Should work when TopologicalTaskSorter will use an iterative method")
    public void testBigGraph() throws Exception {
        List<Entry> entries = new ArrayList<>();
        TestEntry e = new TestEntry(0);
        entries.add(e);
        for (int i = 1; i < 1000000; i++) {
            TestEntry next = new TestEntry(i);
            next.addParent(e);
            entries.add(next);
            e = next;
        }
        Collections.shuffle(entries);
        assertEquals(entries.size(), 1000000);
        TopologicalTaskSorter.sort(entries);

    }

    private void assertBefore(List<Entry> entries, Entry a, Entry b) {
        int aIndex = entries.indexOf(a);
        int bIndex = entries.indexOf(b);
        assertTrue(0 <= aIndex && aIndex < bIndex);
    }
}
