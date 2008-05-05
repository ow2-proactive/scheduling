package unitTests.gcmdeployment.variablecontract;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationImpl;
import org.objectweb.proactive.extensions.gcmdeployment.Helpers;


public class TestVariableContract {
    static final String VAR_NAME = "VARIABLE";
    static final String VAR_VALUE = "value";
    static final String VAR_DEFAULTVALUE = "plop";

    @Test
    public void test() throws ProActiveException {
        File desc = new File(this.getClass().getResource("TestVariableContractApplication.xml").getFile());

        VariableContractImpl vContractRes;
        GCMApplicationImpl gcmad;

        gcmad = new GCMApplicationImpl(Helpers.fileToURL(desc));
        vContractRes = gcmad.getVariableContract();
        Assert.assertEquals(VAR_DEFAULTVALUE, vContractRes.getValue(VAR_NAME));

        VariableContractImpl vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(VAR_NAME, VAR_VALUE, VariableContractType.DescriptorDefaultVariable);
        gcmad = new GCMApplicationImpl(Helpers.fileToURL(desc), vContract);
        vContractRes = gcmad.getVariableContract();
        Assert.assertEquals(VAR_VALUE, vContractRes.getValue(VAR_NAME));

    }
}
