package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

import com.google.common.collect.Maps;


public class JobKeyValueTransformerTest {

    @Test
    public void transformVariablesToMapTest() {
        Map<String, String> expected = Maps.newHashMap();
        expected.put("name", "devTest");
        expected.put("age", "36");
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        assertThat(jkvTransformer.transformVariablesToMap("{\"name\":\"devTest\",\"age\":\"36\"}"),
                is(expected));
    }

    @Test(expected = CLIException.class)
    public void transformVariablesToMapWithWrongSeparatorTest() {
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{\"name\":\"devTest\";\"age\":\"36\"}");
    }

    @Test
    public void transformVariablesWithoutValueToMapTest() {
        Map<String, String> expected = Maps.newHashMap();
        expected.put("name", "");
        expected.put("age", "");
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        assertThat(jkvTransformer.transformVariablesToMap("{\"name\":\"\",\"age\":\"\"}"), is(expected));

    }

    @Test(expected = CLIException.class)
    public void transformVariablesWithoutkeyToMapTest() {
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{\"\":\"devTest\",\"\":\"36\"}");
    }

    @Test(expected = CLIException.class)
    public void emptyJsonVariablesToMapTest() {
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{}");
    }

    @Test(expected = CLIException.class)
    public void emptyVariablesToMapTest() {
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap(" ");
    }

    @Test
    public void noVariablesToMapTest() {
        Map<String, String> expected = Maps.newHashMap();
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        assertThat(jkvTransformer.transformVariablesToMap(null), is(expected));
    }

}