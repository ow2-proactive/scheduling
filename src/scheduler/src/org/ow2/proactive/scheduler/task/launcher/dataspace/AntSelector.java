/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
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
package org.ow2.proactive.scheduler.task.launcher.dataspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


/** An helper class to select a set of files.
 *
 * Since {@link DataSpacesFileObject#findFiles(org.objectweb.proactive.extensions.dataspaces.api.FileSelector)}
 * cannot only takes a predefined selector as parameter, this class allow to pass a custom
 * selector
 */
public class AntSelector {

    /**
     * Traverses the descendants of this file, and builds a list of selected
     * files.
     * Optimized for non pattern includes.
     */
    public static void findFiles(final DataSpacesFileObject fo, final AntFileSelector selector,
            final boolean depthwise, final List<DataSpacesFileObject> selected) throws FileSystemException {
        try {
            if (!fo.exists() || selector.getIncludes() == null || selector.getIncludes().length == 0) {
                return;
            }
            if (selector.getIncludes() != null) {
                if (selector.getIncludes().length == 0) {
                    return;
                }
                //for each non pattern include, remove it from selector and try to get the file directly
                List<String> newIncludes = new ArrayList<String>();
                for (String include : selector.getIncludes()) {
                    if (!SelectorUtils.hasWildcards(include)) {
                        DataSpacesFileObject dsfo = fo.resolveFile(include);
                        if (dsfo.exists()) {
                            selected.add(dsfo);
                        }
                    } else {
                        newIncludes.add(include);
                    }
                }
                if (newIncludes.size() == 0) {
                    return;
                }
                selector.setIncludes(newIncludes.toArray(new String[] {}));
            }
            // if the DSFileObject cannot be listed, it's just finished as it is not possible to scan it
            if (fo.getType().hasChildren()) {
                // Finally, if the DSFileObject can be listed,
                // traverse remaining wild-carded includes starting at this file
                final FileSelectInfo info = new FileSelectInfo();
                info.setBaseFolder(fo);
                info.setDepth(0);
                info.setFile(fo);
                // make the tree of directory to be traversed
                Tree tree = new Tree();
                for (String include : selector.getIncludes()) {
                    LinkedList<String> incl = new LinkedList<String>();
                    Collections.addAll(incl, include.split("/"));
                    tree.addPath(incl);
                }
                //recurse on dataspace directory
                traverse(tree, info, selector, depthwise, selected);
            }
        } catch (final Exception e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * Traverses a file.
     */
    private static void traverse(final Tree tree, final FileSelectInfo fileInfo,
            final AntFileSelector selector, final boolean depthwise, final List<DataSpacesFileObject> selected)
            throws Exception {
        // Check the file itself
        final DataSpacesFileObject file = fileInfo.getFile();
        final int index = selected.size();
        //TODO currently, if name is "**" we'll traverse every children from this DSFileObject
        //(the tree has no effect anymore)
        //it can be improved when "**" is followed by a directory name that restrict the number of children under "**"
        final boolean traverseAll = (tree == null) || tree.hasDoubleStars();

        // If the file is a folder, traverse it
        if (file.getType().hasChildren() && file.isReadable()) {
            final int curDepth = fileInfo.getDepth();
            fileInfo.setDepth(curDepth + 1);

            // Traverse the children
            final List<DataSpacesFileObject> children = file.getChildren();
            for (int i = 0; i < children.size(); i++) {
                final DataSpacesFileObject child = children.get(i);
                //if child is in a leaf of the tree, traverse it
                if (traverseAll) {
                    fileInfo.setFile(child);
                    traverse(null, fileInfo, selector, depthwise, selected);
                } else {
                    //TODO (PROACTIVE-793)
                    //update the following line by an update in the DataSpacesFileObject API (+ getName)
                    String filename = child.getVirtualURI().substring(
                            child.getVirtualURI().lastIndexOf("/") + 1);
                    Tree treeTmp = tree.matches(filename, selector.isCaseSensitive);
                    if (treeTmp != null) {
                        fileInfo.setFile(child);
                        traverse(treeTmp, fileInfo, selector, depthwise, selected);
                    }
                }
            }

            fileInfo.setFile(file);
            fileInfo.setDepth(curDepth);
        }

        // Add the file if doing depthwise traversal
        if (selector.includeFile(fileInfo)) {
            if (depthwise) {
                // Add this file after its descendents
                selected.add(file);
            } else {
                // Add this file before its descendents
                selected.add(index, file);
            }
        }
    }

    /**
     * Tree is a class representing a tree structure with multi leaves
     *
     * @author The ProActive Team
     * @since ProActive Scheduling 2.0
     */
    static class Tree {

        enum LeafType {
            ROOT, DIRECTORY, ENDPOINT;
        }

        private LeafType type;
        private String name;
        private Set<Tree> leaves = null;
        private int depth = 0;

        /**
         * Create a new instance of Tree that ensure to be the root of the tree
         */
        Tree() {
            this.type = LeafType.ROOT;
            this.name = "root";
            this.leaves = new HashSet<Tree>();
        }

        /**
         * Create a new instance of Tree using type and name
         * 
         * @param type the type of the leaf
         * @param name the name of the new tree (leaf)
         */
        private Tree(LeafType type, String name) {
            this.type = type;
            this.name = name;
            if (type.equals(LeafType.DIRECTORY)) {
                leaves = new HashSet<Tree>();
            }
        }

        /**
         * Add a tree to the list of leaves
         * 
         * @param tree the new tree to add
         */
        void add(Tree tree) {
            if (type.equals(LeafType.ENDPOINT)) {
                throw new IllegalAccessError("Cannot add into endPoint leave type !");
            }
            tree.depth = depth + 1;
            leaves.add(tree);
        }

        /**
         * Add this path to this tree.
         * Create every missing leaves if needed.
         * 
         * @param path the list of directory/filename to add to the tree
         */
        void addPath(LinkedList<String> path) {
            if (path == null || path.size() == 0) {
                return;
            }
            //get and remove the current directory/file
            String s = path.removeFirst();
            //search if this name is already in the tree
            Tree tmp = search(s);
            if (tmp == null) {
                //if not, replace ant char regexp by java one
                if (!s.equals("**")) {
                    s = s.replaceAll("[.]", "[.]").replaceAll("[?]", ".").replaceAll("[*]", ".*");
                }
                //and add the new leaf
                tmp = new Tree((path.size() == 0 ? LeafType.ENDPOINT : LeafType.DIRECTORY), s);
                add(tmp);
            }
            //then continue with the next name
            tmp.addPath(path);
        }

        /**
         * Search if the given name is a leaf of this tree
         * If yes, the selected tree is returned.
         * If no, null is returned.
         * 
         * @param name the name to search for.
         * @return the selected tree if found, null if not.
         */
        private Tree search(String name) {
            for (Tree tree : leaves) {
                if (tree.name.equals(name)) {
                    return tree;
                }
            }
            return null;
        }

        /**
         * Return true if the name is "**"
         * 
         * @return true if the name is "**", false if not
         */
        boolean hasDoubleStars() {
            if (type.equals(LeafType.ENDPOINT)) {
                return false;
            }
            for (Tree tree : leaves) {
                if (tree.name.equals("**")) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Search if the given name matches a leaf of this tree
         * If yes, the selected tree is returned.
         * If no, null is returned.
         * 
         * @param name the name to search for.
         * @param caseSensitive true if the match must be case sensitive, false if not
         * @return the selected tree if found, null if not.
         */
        Tree matches(String name, boolean caseSensitive) {
            for (Tree tree : leaves) {
                if (caseSensitive && name.matches(tree.name)) {
                    return tree;
                } else if (!caseSensitive && name.toUpperCase().matches(tree.name.toUpperCase())) {
                    return tree;
                }
            }
            return null;
        }

        /**
         * Get the leaves of this tree
         * 
         * @return the leaves of this tree
         */
        Set<Tree> getLeaves() {
            return leaves;
        }

        /**
         * Get the type of this tree
         * 
         * @return the type of this tree
         */
        LeafType getType() {
            return type;
        }

        /**
         * Get the name of this tree
         * 
         * @return the name of this tree
         */
        String getName() {
            return name;
        }

        // Override Equals and HashCode to ensure that two different tree representing the same name
        // at the same depth cannot be added two times in a set
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Tree) && (this.hashCode() == obj.hashCode());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return name.hashCode() + depth;
        }

        //helpful toString
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            internalToString(sb, "");
            return sb.toString();
        }

        private void internalToString(StringBuilder s, String tabLevel) {
            s.append(tabLevel);
            s.append(depth + ":" + type + ":" + name);
            s.append(System.getProperty("line.separator"));
            if (leaves == null || leaves.size() == 0) {
                return;
            }
            for (Tree t : leaves) {
                t.internalToString(s, tabLevel + "\t");
            }
        }
    }
}
