package javasci;

import java.io.Serializable;

public class SciData implements Serializable{
    protected String name;
    
    public SciData(String name){
	this.name = name;
    }
    
    public String getName() {
	return this.name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

}
