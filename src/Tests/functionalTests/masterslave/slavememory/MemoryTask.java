package functionalTests.masterslave.slavememory;

import org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;


public class MemoryTask implements Task<String> {
    public String run(SlaveMemory memory) throws Exception {
        String mes = (String) memory.load("message");
        if (mes.equals("Hello0")) {
            memory.save("message", "Hello1");
        } else if (mes.equals("Hello1")) {
            memory.save("message", "Hello2");
        }
        System.out.println(mes);
        return mes;
    }
}
