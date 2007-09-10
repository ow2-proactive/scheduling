package unitTests.deployment.descriptorParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers.AbstractApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptorImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParserImpl;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder.CommandBuilderScript;
import org.w3c.dom.Node;


public class TestApplicationDescriptorParser {
    final static String TEST_APP_DIR = TestApplicationDescriptorParser.class.getClass()
                                                                            .getResource("/unitTests/deployment/descriptorParser/testfiles/application")
                                                                            .getFile();

    //    @Test
    public void test() throws IOException {
        for (File descriptor : getApplicationDescriptors()) {
            if (descriptor.toString().contains("script_ext")) {
                continue;
            }

            GCMApplicationParserImpl parser = new GCMApplicationParserImpl(descriptor);

            parser.getCommandBuilder();
            parser.getVirtualNodes();
            parser.getResourceProviders();
        }
    }

    /**
     * User application node parser used to demonstrate how to install custom app parsers
     * @author glaurent
     *
     */
    protected static class UserApplicationNodeParser
        extends AbstractApplicationParser {
        @Override
        protected CommandBuilder createCommandBuilder() {
            return new CommandBuilderScript();
        }

        public String getNodeName() {
            return "paext:myapplication";
        }

        @Override
        public void parseApplicationNode(Node paNode,
            GCMApplicationParser applicationParser, XPath xpath) {
            super.parseApplicationNode(paNode, applicationParser, xpath);

            System.out.println("User Application Parser - someattr value = " +
                paNode.getAttributes().getNamedItem("someattr").getNodeValue());
        }
    }

    @Test
    public void userSchemaTest() throws IOException {
        for (File file : getApplicationDescriptors()) {
            if (!file.toString().contains("script_ext")) {
                continue;
            }
            System.out.println(file);

            String userSchema = getClass()
                                    .getResource("/unitTests/deployment/descriptorParser/testfiles/application/SampleApplicationExtension.xsd")
                                    .toString();

            ArrayList<String> schemas = new ArrayList<String>();
            schemas.add(userSchema);

            GCMApplicationParserImpl parser = new GCMApplicationParserImpl(file,
                    schemas);

            parser.registerApplicationParser(new UserApplicationNodeParser());

            parser.getCommandBuilder();
            parser.getVirtualNodes();
            parser.getResourceProviders();
        }
    }

    //    @Test
    public void doit() throws IOException {
        for (File file : getApplicationDescriptors()) {
            if (!file.toString().contains("scriptHostname")) {
                continue;
            }
            System.out.println(file);

            GCMApplicationDescriptor gcma = new GCMApplicationDescriptorImpl(file);
            gcma.awaitTermination();
        }
    }

    private List<File> getApplicationDescriptors() {
        List<File> ret = new ArrayList<File>();
        File dir = new File(TEST_APP_DIR);

        for (String file : dir.list()) {
            ret.add(new File(dir, file));
        }
        return ret;
    }
}
