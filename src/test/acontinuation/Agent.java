package test.acontinuation;

import org.objectweb.proactive.ProActive;


public class Agent implements java.io.Serializable {
    private Agent deleguate;

    public Agent() {
    }

    public void init() {
        try {
            this.deleguate = (Agent) ProActive.newActive(Agent.class.getName(),
                    new Object[] {  });
        } catch (Exception e) {
        }
    }

    public Dummy getDummyWait(Dummy d) {
        return deleguate.getInternDummyWait(d);
    }

    public Dummy getInternDummyWait(Dummy d) {
        try {
            Thread.sleep(3000);

            return new Dummy();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public Dummy getDummy(Dummy d) {
        return new Dummy();
    }

}
