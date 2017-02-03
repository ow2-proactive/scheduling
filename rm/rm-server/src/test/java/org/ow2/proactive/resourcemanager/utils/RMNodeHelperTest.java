package org.ow2.proactive.resourcemanager.utils;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;

/**
 * @author ActiveEon Team
 * @since 03/02/17
 */
public class RMNodeHelperTest {

    @Test
    public void testIsDeployingNodeUrlInvalidInput() {
        assertThat(RMNodeHelper.isDeployingNodeURL("invalid")).isFalse();
    }

    @Test
    public void testIsDeployingNodeUrlNullInput() {
        assertThat(RMNodeHelper.isDeployingNodeURL(null)).isFalse();
    }

    @Test
    public void testIsDeployingNodeUrlValidInput() {
        assertThat(RMNodeHelper.isDeployingNodeURL(RMDeployingNode.PROTOCOL_ID)).isFalse();
    }

}