/*
 * Created on Jul 21, 2003
 *
 */
package testsuite.group;

import org.apache.log4j.Logger;

import testsuite.exception.BrowsePackageException;

import testsuite.manager.AbstractManager;

import testsuite.result.ResultsCollections;

import testsuite.test.AbstractTest;

import java.io.File;
import java.io.FileFilter;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/** It is a collection of tests.
 * @author Alexandre di Costanzo
 */
public class Group {

    /** A group name.
     */
    private String name = "Group with no name";

    /** A group description.
     */
    private String description = "Group with no description.";

    /** A logger for a group.
     */
    protected static Logger logger = null;

    /** All results of a group.
     */
    private ResultsCollections results = new ResultsCollections();

    /** All the tests of a group.
     */
    private ArrayList tests = new ArrayList();

    /** To construct a new group with default params.
     */
    public Group() {
        logger = Logger.getLogger(getClass().getName());
    }

    /** To construct a new group from an another group.
     * @param group an ancestor group.
     */
    public Group(Group group) {
        logger = Logger.getLogger(getClass().getName());
        this.results = new ResultsCollections(group.getResults());
        this.tests = new ArrayList(group.tests);
        this.name = group.name;
        this.description = group.description;
    }

    /** To construct a new group.
     * @param name a string name for a group.
     * @param description a string description for a group.
     */
    public Group(String name, String description) {
        logger = Logger.getLogger(getClass().getName());
        this.name = name;
        this.description = description;
    }

    /** To construct a new group from tests in a specified package.
     * @param name a group name
     * @param description a group description.
     * @param directory the root of the package to scan.
     * @param packageName the package name to add : com.toto.gui.
     * @param params params for construct test.
     * @param useInitFile true if you want to build tests with their init's files.
     * @throws BrowsePackageException if some errors.
     */
    public Group(String name, String description, File directory,
        String packageName, Object[] params, boolean useInitFile,
        AbstractManager manager) throws BrowsePackageException {
        logger = Logger.getLogger(getClass().getName());
        this.name = name;
        this.description = description;

        addTests(directory, packageName, packageName, params, useInitFile,
            manager);
    }

    private static FileFilter dirFilter = new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    private static FileFilter dirClassFilter = new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".class") ||
                        pathname.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        };

    /** Private method to construct group with tests from a package.
     * @param directory the root directory of a package.
     * @param packageName the package name to add : com.toto.gui.
     * @param params params for construct test.
     * @param useInitFile true if you want to init tests with their init file.
     * @throws BrowsePackageException if an error.
     */
    private void addTests(File directory, String packageName,
        String parentPackage, Object[] params, boolean useInitFile,
        AbstractManager manager) throws BrowsePackageException {
        if (!directory.isDirectory()) {
            throw new BrowsePackageException(
                "Directory is not a valid directory");
        }

        if ((packageName != null) && (packageName.length() != 0)) {
            // Find files package
            File[] files = directory.listFiles(dirFilter);

            String nextRep = null;
            if (packageName.indexOf('.') != -1) {
                nextRep = packageName.substring(0, packageName.indexOf('.'));
            } else {
                nextRep = packageName;
            }

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.getName().compareTo(nextRep) == 0) {
                    String nextPackageName = "";
                    if (packageName.indexOf('.') != -1) {
                        nextPackageName = packageName.substring(packageName.indexOf(
                                    '.') + 1);
                    } else {
                        nextPackageName = null;
                    }
                    addTests(file, nextPackageName, parentPackage, params,
                        useInitFile, manager);
                } else {
                    continue;
                }
            }
        } else {
            // Files package founded
            File[] files = directory.listFiles(dirClassFilter);

            Class[] parameterTypes = null;
            if ((params != null) && (params.length != 0)) {
                parameterTypes = new Class[params.length];
                for (int j = 0; j < params.length; j++)
                    parameterTypes[j] = params[j].getClass();
            }

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    addTests(file, null, parentPackage + "." + file.getName(),
                        params, useInitFile, manager);
                } else {
                    String fileTest = parentPackage + "." +
                        file.getName().replaceAll("\\.class", "");
                    try {
                        Class c = getClass().getClassLoader().loadClass(fileTest);

                        //                        if (file.getName().matches("Test.*") ||
                        //                                file.getName().matches("Bench.*")) {
                        Class superClass = c;
                        while (superClass.getSuperclass() != Object.class)
                            superClass = superClass.getSuperclass();

                        if (superClass.getName().compareTo(AbstractTest.class.getName()) == 0) {
                            AbstractTest test = null;

                            if (parameterTypes != null) {
                                Constructor constructor = c.getConstructor(parameterTypes);
                                test = (AbstractTest) constructor.newInstance(params);
                            } else {
                                test = (AbstractTest) c.newInstance();
                            }
                            test.setManager(manager);

                            if (logger.isDebugEnabled()) {
                                logger.debug(test.getName() +
                                    " added in group " + name);
                            }

                            if (useInitFile) {
                                test.loadAttributes();
                            }
                            tests.add(test);
                        } else {
                            continue;
                        }
                    } catch (Exception e) {
                        throw new BrowsePackageException(e);
                    }
                }
            }
        }
    }

    /** Run the init method of all tests and set the tests loggers with the group logger.
     * @throws Exception if error during the initialization of tests.
     */
    public void initGroup() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Beginning group " + name + "'s initialization");
        }
        Iterator it = tests.iterator();
        while (it.hasNext()) {
            AbstractTest test = (AbstractTest) it.next();
            test.initTest();
        }
        if (logger.isInfoEnabled()) {
            logger.info("Finnishing group " + name + "'s initialization");
        }
    }

    /** run the end method of all tests.
     * @throws Exception if errors during ending a tests.
     */
    public void endGroup() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Beginning group " + name + "'s ending");
        }
        Iterator it = tests.iterator();
        while (it.hasNext()) {
            AbstractTest test = (AbstractTest) it.next();
            test.endTest();
        }
        if (logger.isInfoEnabled()) {
            logger.info("Finnishing group " + name + "'s ending");
        }
    }

    /** To get the group description
     * @return a description.
     */
    public String getDescription() {
        return description;
    }

    /** To get the group name.
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /** To set the description.
     * @param string a new description.
     */
    public void setDescription(String string) {
        description = string;
    }

    /** to set the name.
     * @param string a new name.
     */
    public void setName(String string) {
        name = string;
    }

    /** Name and description of a group.
     * @see java.lang.Object#toString()
     * @return a string with the name and the description.
     */
    public String toString() {
        return name + " : " + description;
    }

    /** to get the group logger.
     * @return a logger.
     */
    public Logger getLogger() {
        return logger;
    }

    /** To get all results of the group.
     * @return all the results.
     */
    public ResultsCollections getResults() {
        return results;
    }

    /** Inserts the specified test at the specified position in this list. Shifts the test currently at that position (if any) and any subsequent tests to the right (adds one to their indices).
     * @param index index at which the specified test is to be inserted.
     * @param test test to be inserted.
     */
    public void add(int index, AbstractTest test) {
        tests.add(index, test);
    }

    /** Appends the specified test to the end of this list.
     * @param test test to be appended to this list.
     * @return true (as per the general contract of Collection.add).
     */
    public boolean add(AbstractTest test) {
        return tests.add(test);
    }

    /** Appends all of the tests in the specified Collection to the end of this list, in the order that they are returned by the specified Collection's Iterator. The behavior of this operation is undefined if the specified Collection is modified while the operation is in progress. (This implies that the behavior of this call is undefined if the specified Collection is this list, and this list is nonempty.)
     * @param c the tests to be inserted into this list.
     * @return true if this list changed as a result of the call.
     */
    public boolean addAll(Collection c) {
        return tests.addAll(c);
    }

    /** Inserts all of the tests in the specified Collection into this list, starting at the specified position. Shifts the test currently at that position (if any) and any subsequent tests to the right (increases their indices). The new tests will appear in the list in the order that they are returned by the specified Collection's iterator.
     * @param index index at which to insert first test from the specified collection.
     * @param c tests to be inserted into this list.
     * @return true if this list changed as a result of the call.
     */
    public boolean addAll(int index, Collection c) {
        return tests.addAll(index, c);
    }

    /** Removes all of the tests from this list. The list will be empty after this call returns.
     *
     *
     */
    public void clear() {
        tests.clear();
    }

    /** Returns an iterator over the tests in this list in proper sequence.Returns an iterator over the tests in this list in proper sequence.
     * @return an iterator over the tests in this list in proper sequence.
     */
    public Iterator iterator() {
        return tests.iterator();
    }

    /** Returns true if this list contains the specified test.
     * @param test test whose presence in this List is to be tested.
     * @return true if the specified test is present; false otherwise.
     */
    public boolean contains(AbstractTest test) {
        return tests.contains(test);
    }

    /** Returns the test at the specified position in this list.
     * @param index index of test to return.
     * @return the test at the specified position in this list.
     */
    public AbstractTest get(int index) {
        return (AbstractTest) tests.get(index);
    }

    /** Searches for the first occurence of the given argument, testing for equality using the equals method.
     * @param test an test.
     * @return the index of the first occurrence of the argument in this list; returns -1 if the test is not found.
     */
    public int indexOf(AbstractTest test) {
        return tests.indexOf(test);
    }

    /** Tests if this list has no tests.
     * @return true if this list has no tests; false otherwise.
     */
    public boolean isEmpty() {
        return tests.isEmpty();
    }

    /** Removes the test at the specified position in this list. Shifts any subsequent tests to the left (subtracts one from their indices).
     * @param index the index of the test to removed.
     * @return the test that was removed from the list.
     */
    public AbstractTest remove(int index) {
        return (AbstractTest) tests.remove(index);
    }

    /** Replaces the test at the specified position in this list with the specified test.
     * @param index index of test to replace.
     * @param test test to be stored at the specified position.
     * @return the test previously at the specified position.
     */
    public AbstractTest set(int index, AbstractTest test) {
        return (AbstractTest) tests.set(index, test);
    }

    /** Returns the number of tests in this list.
     * @return the number of tests in this list.
     *
     *
     */
    public int size() {
        return tests.size();
    }

    /** Returns an array containing all of the tests in this list in the correct order.
     * @return an array containing all of the tests in this list in the correct order.
     */
    public AbstractTest[] toArray() {
        return (AbstractTest[]) tests.toArray(new AbstractTest[size()]);
    }
}
