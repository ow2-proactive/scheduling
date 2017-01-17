package org.ow2.proactive.resourcemanager.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;


/**
 * @author ActiveEon Team
 * @since 17/01/17
 */
public class NodeStateTest {

    @Test
    public void testParseValidInput() throws Exception {
        for (NodeState nodeState : NodeState.values()) {
            assertThat(NodeState.parse(nodeState.toString())).isEqualTo(nodeState);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidInput() throws Exception {
        NodeState.parse("Unknown Input Value");
    }

    @Test
    public void testToString() {
        assertThat(NodeState.FREE.toString()).isNotEqualTo(NodeState.FREE.name());
    }

}
