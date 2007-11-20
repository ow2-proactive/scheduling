package functionalTests.gcmdeployment.virtualnode;

import java.io.File;
import java.io.FileNotFoundException;

import functionalTests.FunctionalTest;


public abstract class Abstract extends FunctionalTest {
    protected File getDescriptor() throws FileNotFoundException {
        String classname = this.getClass().getSimpleName();
        String resource = this.getClass().getResource(classname + ".xml")
                              .getFile();
        File desc = new File(resource);
        if (!(desc.exists() && desc.isFile() && desc.canRead())) {
            throw new FileNotFoundException(desc.getAbsolutePath());
        }

        return desc;
    }

    protected void waitAllocation() {
        wait(5000);
    }

    protected void wait(int sec) {
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
