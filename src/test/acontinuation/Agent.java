package test.acontinuation;

import org.objectweb.proactive.ProActive;

public class Agent implements java.io.Serializable {

	private Agent deleguate;

	public Agent() {
	}

	public void init() {
		try {
			this.deleguate = (Agent) ProActive.newActive(Agent.class.getName(), new Object[] {
			});
		} catch (Exception e) {
		}
	}

	public void print() {
		System.out.println("Si cette phrase apparait avant les (ou entre 2) \"****\", la continuation automatique marche !");
	}

	public Dummy getDummy() {
		return deleguate.getInternDummy();
	}

	public Dummy getInternDummy() {
		try {
			for (int i = 0; i < 5; i++) {
				Thread.sleep(1000);
				System.out.println("****");
			}
			return new Dummy();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
