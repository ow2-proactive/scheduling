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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.selection.policies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionPolicy;


/**
 * The selection policy that arranges nodes according to node sources names in the config file.
 * 
 * The config file should be in the following format:
 * ns1
 * ns2
 * ...
 * nsN
 * 
 */
public class NodeSourcePriorityPolicy implements SelectionPolicy {

    public final static String CONFIG_NAME_PROPERTY = "pa.rm.selection.policy.nodesources";
    private final static Logger logger = Logger.getLogger(NodeSourcePriorityPolicy.class);

    // when the file was modified
    private long lastModified = 0;
    private List<String> nodeSources;
    private final String configFileName;

    public NodeSourcePriorityPolicy() {
        configFileName = System.getProperty(CONFIG_NAME_PROPERTY);
        if (configFileName != null) {
            reloadConfig();
        } else {
            throw new IllegalArgumentException("Config file is not specified");
        }
    }

    /**
     * Reload config if it has been changed.
     * @param configFileName
     */
    private void reloadConfig() {
        File config = new File(configFileName);
        if (config.exists()) {
            if (lastModified < config.lastModified()) {
                lastModified = config.lastModified();

                logger.debug("Reading the NodeSourcePriorityPolicy config file");
                nodeSources = new LinkedList<>();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(config));
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        logger.debug("Node source name found: " + strLine);
                        nodeSources.add(strLine);
                    }
                    br.close();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        } else {
            throw new IllegalArgumentException("Config file does not exist");
        }
    }

    /**
     * Sort by node source priorities.
     * 
     * @return the list arranged according to the node source priorities
     */
    public List<RMNode> arrangeNodes(int number, List<RMNode> nodes, Client client) {

        if (nodeSources == null) {
            logger.error("The policy config was not loaded");
            return nodes;
        }
        if (nodeSources.size() == 0) {
            logger.debug("The policy config is empty");
            return nodes;
        }

        reloadConfig();

        logger.debug("Arranging nodes according to node sources priorities");

        HashMap<String, List<RMNode>> nodesMap = new HashMap<>();
        for (RMNode node : nodes) {
            if (!nodesMap.containsKey(node.getNodeSourceName())) {
                nodesMap.put(node.getNodeSourceName(), new LinkedList<RMNode>());
            }
            nodesMap.get(node.getNodeSourceName()).add(node);
        }

        List<RMNode> arranged = new LinkedList<>();
        for (String ns : nodeSources) {
            if (nodesMap.containsKey(ns)) {
                logger.debug("Adding " + nodesMap.get(ns).size() + " nodes from " + ns);
                arranged.addAll(nodesMap.get(ns));
                nodesMap.remove(ns);

                if (arranged.size() >= number) {
                    break;
                }
            }
        }

        for (String ns : nodesMap.keySet()) {
            logger.debug("Adding nodes from " + ns);
            arranged.addAll(nodesMap.get(ns));
        }

        return arranged;
    }
}
