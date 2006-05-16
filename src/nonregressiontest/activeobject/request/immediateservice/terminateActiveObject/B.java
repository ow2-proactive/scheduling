package nonregressiontest.activeobject.request.immediateservice.terminateActiveObject;



public class B {
	
	private String color = "blue";
	
	public B() {
    }

    public B(String color) {
        this.color = color;
    }
    
    public void changeColor(String color) {
    	try {
			Thread.sleep(3000);
			this.color = color;
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public String getColor(){
    	return color;
    }
}
