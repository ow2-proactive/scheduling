package org.ow2.proactive.scheduler.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;


/**
 * Restrict use for regular tasks of specific NodeSources
 * <p>
 * This policy needs to be configured to restrict access to a set of NodeSources.
 * A sample configuration can be:
 * <pre>
 * ns.restricted=foo;bar;baz
 * ns.auth.group1=foo;bar
 * ns.auth.group2=bar;baz
 * ns.auth.group3=bar
 * </pre>
 * Any task scheduled using this Policy will be attached a SelectionScript
 * that will prevent execution on nodes from nodesources foo, bar or baz.
 * <p>
 * If a task is attached a Generic Information using {@link Task#addGenericInformation(String, String)}
 * with the key {@link #authDescriptorKey} and the value "group1",
 * this policy will place a SelectionScript for nodes from NodeSources "foo" of "bar".
 * If the value for {@link #authDescriptorKey} is "group3", this policy
 * will place a SelectionScript for nodes from the NodeSource "bar".
 * <p>
 * 
 *
 * 
 * @author mschnoor
 * @since ProActive Scheduling 3.2.1
 *
 */
@SuppressWarnings("serial")
public class RestrictedNodeSourcesPolicy extends Policy {

    /**
     * key to use with {@link Task#addGenericInformation(String, String)},
     * the value should represent one of the mapping described with an {@value #authorizedNodeSourcesProperty}
     * in the policy configuration file
     */
    public final static String authDescriptorKey = "NS_AUTH";

    /**
     * Key to use in policy configuration to describe restricted nodesources
     * ie: <code>ns.restricted=ns1;ns2;ns3</code> 
     */
    public final static String restrictedNodeSourcesProperty = "ns.restricted";

    /**
     * Key to use in policy configuration to describe groups of authorized nodesources
     * ie:
     * <pre>
     * ns.auth.group1=ns1;ns2
     * ns.auth.group2=ns2;ns3
     * </pre>
     */
    public final static String authorizedNodeSourcesProperty = "ns.auth";

    /**
     * Separator character for nodesources list in configuration file
     * or GenericInformation
     */
    public final static String nodeSourcesSeparator = ";";

    /**
     * Script that will select one of the NodeSources enumerated as parameter
     */
    private final static String selectionScriptContent = "" //
        + "var selected = false;" //
        + "var nsProp = java.lang.System.getProperty(\"proactive.node.nodesource\");" //
        + "for (var i in args) {" //
        + "  if (args[i].equals(nsProp)) {" //
        + "    selected = true;" //
        + "    break;" //
        + "  }" //
        + "}";

    /**
     * Script that will select any NodeSource except one enumerated as parameter
     */
    private final static String exclusionScriptContent = "" //
        + "selected = true;" //
        + "var nsProp = java.lang.System.getProperty(\"proactive.node.nodesource\");" //
        + "for (var i in args) {" //
        + "  if (args[i].equals(nsProp)) {" //
        + "    selected = false;" //
        + "    break;" //
        + "  }" //
        + "}";

    /**
     * simple instanciation of {@link #exclusionScriptContent} 
     * with the value of {@link #restrictedNodeSourcesProperty} as parameter
     */
    private SelectionScript exclusionScript;

    /**
     * key: name of each group defined by {@link #authorizedNodeSourcesProperty} in config
     * value: instanciation of {@link #selectionScriptContent} with the corresponding NS list as parameter 
     */
    private Hashtable<String, SelectionScript> selectionScripts;

    /**
     * {@inheritDoc}
     * Override reload to avoid reading config file
     * Attempting to read a non-existing file will fail policy changes or renewal.
     */
    @Override
    public boolean reloadConfig() {
        if (!super.reloadConfig()) {
            return false;
        }
        try {
            this.selectionScripts = new Hashtable<String, SelectionScript>();

            for (Object okey : this.configProperties.keySet()) {
                String key = (String) okey;

                // define groups of selectable NodeSources that are bound to a certain keyword
                // example in config file: property.keyword = ns1;ns2
                // this means if the user uses 'keyword' as authorized NodeSource in the job descriptor,
                // either ns1 or ns2 will be selected for execution.
                if (key.startsWith(authorizedNodeSourcesProperty)) {
                    if (key.indexOf('.') == -1) {
                        logger.warn("Invalid authorized Node Sources property. Should be: '" +
                            authorizedNodeSourcesProperty + ".key=value1;value2'");
                        continue;
                    }
                    // keyword bound to the nodesource list, used in task descriptor
                    String keyword = key.substring(key.lastIndexOf('.') + 1);
                    // nodesource list used for selection scripts
                    String[] nsList = getNodeSourcesList(configProperties.getProperty(key));

                    SelectionScript script = new SelectionScript(new SimpleScript(selectionScriptContent,
                        "javascript", nsList), false);
                    this.selectionScripts.put(keyword, script);
                }
            }

            // list of NodeSources that can only be selected by tasks that explicitely require it
            if (!this.configProperties.containsKey(restrictedNodeSourcesProperty)) {
                logger
                        .error("Property " + restrictedNodeSourcesProperty +
                            " is not defined in configuration");
                return false;
            }
            final String rns = this.configProperties.getProperty(restrictedNodeSourcesProperty);
            final String[] restrictedNs = getNodeSourcesList(rns);
            logger.info("Node sources with restricted access : " + rns);
            this.exclusionScript = new SelectionScript(new SimpleScript(exclusionScriptContent, "javascript",
                restrictedNs), false);

            return true;
        } catch (InvalidScriptException e) {
            e.printStackTrace();
            logger.error("Failed to create script: ", e);
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            logger.error("Failed to load config: ", e);
            return false;
        }
    }

    @Override
    public Vector<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs) {
        Vector<EligibleTaskDescriptor> toReturn = new Vector<EligibleTaskDescriptor>();
        Collections.sort(jobs);

        for (JobDescriptor jd : jobs) {

            Collection<EligibleTaskDescriptor> allTasks = jd.getEligibleTasks();

            for (EligibleTaskDescriptor t : allTasks) {
                final InternalTask task = t.getInternal();
                final String authorizedNodeSource = task.getGenericInformations().get(authDescriptorKey);
                logger.debug("Task" + task.getName() + " authorizes NodeSource: '" + authorizedNodeSource +
                    "'");

                // task does not request a specific NodeSource; it will be restricted access
                // to the Restricted NodeSources using a selection script
                if (authorizedNodeSource == null || authorizedNodeSource.trim() == "") {

                    // exclusion script is already present on this task
                    if (hasScript(task, this.exclusionScript)) {
                        logger.debug("Do not reattach exclusion scripts to " + task.getName() + "#" +
                            task.getId().value());
                        continue;
                    }

                    logger.debug("Attaching exclusion script to " + task.getName() + "#" +
                        task.getId().value());
                    t.getInternal().addSelectionScript(this.exclusionScript);
                }
                // task demands access to a specific set of NodeSources
                else {
                    // lookup the SelectionScript corresponding this set of NodeSources
                    SelectionScript script = this.selectionScripts.get(authorizedNodeSource);

                    // User has requested an unknown set of NodeSources, ignore the property and place ExclusionScript
                    if (script == null) {
                        logger.warn("Task " + task.getName() + "#" + task.getId().value() +
                            " requires unknown nodesource '" + authorizedNodeSource +
                            "' : no selection script will be run");

                        // exclusion script is already present on this task
                        if (hasScript(task, this.exclusionScript)) {
                            logger.debug("Do not reattach exclusion scripts to " + task.getName() + "#" +
                                task.getId().value());
                            continue;
                        }
                        t.getInternal().addSelectionScript(this.exclusionScript);
                    }
                    // selection script exists but is already present on this task
                    else if (hasScript(task, script)) {
                        logger.debug("Do not reattach selection script to " + task.getName() + "#" +
                            task.getId().value());
                        continue;
                    }
                    // selection script will be attached to the task
                    else {
                        logger.debug("Attaching selection script to " + task.getName() + "#" +
                            task.getId().value());
                        t.getInternal().addSelectionScript(script);
                    }
                }
            }

            toReturn.addAll(allTasks);
        }
        return toReturn;
    }

    /**
     * Parse a NodeSources list as given in the policy configuration or the
     * generic information selection parameter
     * 
     * @param nsList list of NodeSource names separated with {@link #nodeSourcesSeparator}
     * @return an array of nodesource names, cannot be empty or null
     * @throws IllegalArgumentException the nsList parameter does not describe at least one nodesource, or is malformed
     */
    private static String[] getNodeSourcesList(String nsList) throws IllegalArgumentException {
        if (nsList == null || nsList.trim().length() == 0) {
            throw new IllegalArgumentException("Empty NodeSources list");
        }
        String[] list = nsList.split(nodeSourcesSeparator);
        for (String ns : list) {
            if (ns.trim().length() == 0) {
                throw new IllegalArgumentException("NodeSources list '" + nsList + "' contains empty value.");
            }
        }
        return list;
    }

    /**
     * @param task an InternalTask picked for scheduling
     * @param script a Selection script
     * @return true if the task in parameter is already attached the script in parameter, or false
     */
    private static boolean hasScript(InternalTask task, SelectionScript script) {
        if (task.getSelectionScripts() != null) {
            for (SelectionScript sel : task.getSelectionScripts()) {
                if (sel.hashCode() == script.hashCode()) {
                    return true;
                }
            }
        }
        return false;
    }

}
