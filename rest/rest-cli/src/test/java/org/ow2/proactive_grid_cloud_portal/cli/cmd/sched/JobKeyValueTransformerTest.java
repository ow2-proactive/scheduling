package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import com.google.common.collect.Maps;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.JobKeyValueTransformer;

import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class JobKeyValueTransformerTest {

    @Test
    public void transformVariablesToMapTest( ){
        Map<String, String> expected = Maps.newHashMap();
        expected.put("name","devTest");
        expected.put("age","36");
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        assertThat(jkvTransformer.transformVariablesToMap("{\"name\":\"devTest\",\"age\":\"36\"}"), is(expected));
    }

    @Test(expected = CLIException.class)
    public void transformVariablesToMapWithWrongSeparatorTest( ){
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{\"name\":\"devTest\";\"age\":\"36\"}");
    }

    @Test(expected = CLIException.class)
    public void transformVariablesWithoutValueToMapTest( ){
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{\"name\":\"\",\"age\":\"\"}");
    }

    @Test(expected = CLIException.class)
    public void transformVariablesWithoutkeyToMapTest( ){
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{\"\":\"devTest\",\"\":\"36\"}");
    }

    @Test(expected = CLIException.class)
    public void emptyJsonVariablesToMapTest( ){
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{}");
    }

    @Test(expected = CLIException.class)
    public void emptyVariablesToMapTest( ){
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap(" ");
    }

    @Test
    public void noVariablesToMapTest( ){
        Map<String, String> expected = Maps.newHashMap();
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        assertThat(jkvTransformer.transformVariablesToMap(null), is(expected));
    }


}