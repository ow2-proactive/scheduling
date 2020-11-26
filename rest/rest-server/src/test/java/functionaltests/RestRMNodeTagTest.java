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
package functionaltests;

import static org.ow2.proactive.resourcemanager.utils.RMNodeStarter.NODE_TAGS_PROP_NAME;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import com.fasterxml.jackson.databind.ObjectMapper;

import functionaltests.utils.WaitUtils;


public class RestRMNodeTagTest extends AbstractRestFuncTestCase {

    private static final String nodesourceName1 = "taggedNodesource1";

    private static final String nodesourceName2 = "taggedNodesource2";

    private static final String nodeTag1 = "testTag";

    private static final String nodeTag2 = "foo";

    private static final String nodeTag3 = "bar";

    private static String nodeUrl1; // the url of the node created in the node source named nodesourceName1

    private static String nodeUrl2; // the url of the node created in the node source named nodesourceName2

    @BeforeClass
    public static void beforeClass() throws Exception {
        init();

        String jvmParam1 = String.format("-D%s=%s", NODE_TAGS_PROP_NAME, nodeTag1);
        deployLocalNodesourceAndWait(nodesourceName1, 1, jvmParam1);
        nodeUrl1 = getNodeUrl(nodesourceName1);

        String jvmParam2 = String.format("-D%s=%s,%s,%s", NODE_TAGS_PROP_NAME, nodeTag1, nodeTag2, nodeTag3);
        deployLocalNodesourceAndWait(nodesourceName2, 1, jvmParam2);
        nodeUrl2 = getNodeUrl(nodesourceName2);
    }

    @AfterClass
    public static void cleanup() {
        removeNodesourceAndWait(nodesourceName1);
        removeNodesourceAndWait(nodesourceName2);
    }

    @Test
    public void testGetAllNodeTags() throws Exception {
        String resourceUrl = getRmResourceUrl("node/tags");
        HttpGet http = new HttpGet(resourceUrl);
        setSessionHeader(http);
        HttpResponse response = executeUriRequest(http);
        assertHttpStatusOK(response);
        String responseContent = getContent(response);
        System.out.println("Retrieved node tags: " + responseContent);
        List<String> responseTags = Arrays.asList(new ObjectMapper().readValue(responseContent, String[].class));
        assertTrue(CollectionUtils.isEqualCollection(responseTags, Arrays.asList(nodeTag1, nodeTag2, nodeTag3)));
    }

    @Test
    public void testSearchNodeTags() throws Exception {
        List<String> responseTags1 = requestSearchNodeTags(nodeUrl1);
        assertTrue(CollectionUtils.isEqualCollection(responseTags1, Collections.singletonList(nodeTag1)));

        List<String> responseTags2 = requestSearchNodeTags(nodeUrl2);
        assertTrue(CollectionUtils.isEqualCollection(responseTags2, Arrays.asList(nodeTag1, nodeTag2, nodeTag3)));
    }

    @Test
    public void testSearchTaggedNodes() throws Exception {
        List<String> responseNodes1 = requestSearchTaggedNodes(Collections.singletonList(nodeTag1), true);
        assertTrue(CollectionUtils.isEqualCollection(responseNodes1, Arrays.asList(nodeUrl1, nodeUrl2)));

        List<String> responseNodes2 = requestSearchTaggedNodes(Arrays.asList(nodeTag1, nodeTag2), true);
        assertTrue(CollectionUtils.isEqualCollection(responseNodes2, Collections.singletonList(nodeUrl2)));

        List<String> responseNodes3 = requestSearchTaggedNodes(Arrays.asList(nodeTag1, nodeTag2), false);
        assertTrue(CollectionUtils.isEqualCollection(responseNodes3, Arrays.asList(nodeUrl1, nodeUrl2)));
    }

    protected static void deployLocalNodesourceAndWait(String nodesourceName, int numberOfNodes,
            String nodeJVMParameters) throws Exception {
        ResourceManager rm = RestFuncTHelper.getResourceManager();
        int nodeTimeout = 60 * 1000;

        rm.defineNodeSource(nodesourceName,
                            LocalInfrastructure.class.getName(),
                            new Object[] { RestFuncTHelper.getRmCredentials().getBase64(), numberOfNodes, nodeTimeout,
                                           nodeJVMParameters },
                            StaticPolicy.class.getName(),
                            null,
                            false);

        rm.deployNodeSource(nodesourceName);

        new WaitUtils(60).until(() -> rm.listAliveNodeUrls(Collections.singleton(nodesourceName))
                                        .size() == numberOfNodes,
                                String.format("node source %s is deployed with %d nodes",
                                              nodesourceName,
                                              numberOfNodes));
    }

    protected static void removeNodesourceAndWait(String nodesourceName) {
        ResourceManager rm = RestFuncTHelper.getResourceManager();
        rm.removeNodeSource(nodesourceName, true);
        new WaitUtils(30).until(() -> rm.getExistingNodeSourcesList()
                                        .stream()
                                        .noneMatch(ns -> nodesourceName.equals(ns.getNodeSourceName())),
                                String.format("node source %s is removed", nodesourceName));
    }

    protected static String getNodeUrl(String nodesourceName) {
        ResourceManager rm = RestFuncTHelper.getResourceManager();
        return rm.listAliveNodeUrls(Collections.singleton(nodesourceName)).toArray(new String[0])[0];
    }

    protected List<String> requestSearchNodeTags(String nodeUrl) throws Exception {
        String resourceUrl = getRmResourceUrl("node/tags/search?nodeurl=" + nodeUrl);
        HttpGet http = new HttpGet(resourceUrl);
        setSessionHeader(http);
        HttpResponse response = executeUriRequest(http);
        assertHttpStatusOK(response);
        String responseContent = getContent(response);
        System.out.printf("Retrieved node tags for the node %s: %s%n", nodeUrl, responseContent);
        return Arrays.asList(new ObjectMapper().readValue(responseContent, String[].class));
    }

    protected List<String> requestSearchTaggedNodes(List<String> tags, boolean all) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(getRmResourceUrl("nodes/search"));
        tags.forEach(tag -> uriBuilder.addParameter("tags", tag));
        uriBuilder.addParameter("all", String.valueOf(all));
        HttpGet http = new HttpGet(uriBuilder.build());
        setSessionHeader(http);
        HttpResponse response = executeUriRequest(http);
        assertHttpStatusOK(response);
        String responseContent = getContent(response);
        System.out.printf("Retrieved nodes with the tags %s, all %s: %s%n", tags, all, responseContent);
        return Arrays.asList(new ObjectMapper().readValue(responseContent, String[].class));
    }
}
