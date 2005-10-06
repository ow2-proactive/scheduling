package nonregressiontest.descriptor.property;

import java.io.Serializable;

public class TestProperty implements Serializable {
	static final long serialVersionUID = 1;
	
	private String	name;
	
	public TestProperty () {
	}
	
	public TestProperty( String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getProperty( String name) {
		return System.getProperty( name);
	}

}
