package test.rsh;

public class Speaker {

    protected int number;
    
    public Speaker() {} ;

    public Speaker(int n) {
	this.number=n;
    }

    public void speak() {
	while (true) {
	    System.out.println("Speaker " + number + " : speaking...");
	    try {
		Thread.sleep(200);
	    } catch (Exception e) {
		e.printStackTrace();
	    } // end of try-catch	
	} // end of while (true)
    }

    public static void main (String[] args) {
	Speaker s = new Speaker(Integer.parseInt(args[0]));
	s.speak();
    }
}
