package org.objectweb.proactive.extra.ressourceallocator;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;


public class SchemaTest {
    @Test
    public void schemaTest1() {
        try {
            SimpleSchema simpleSchema = new SimpleSchema();

            boolean res = simpleSchema.parseDocument(
                    "/home/glaurent/workspace/proactive_trunk/src/Extra/org/objectweb/proactive/extra/ressourceallocator/testfiles/deployment_sample_1.xml");

            assertEquals(res, true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(SchemaTest.class);
    }
}
