package nonregressiontest.activeobject.generics;


public class Pair<X, Y> {
	private X first;

	private Y second;
	
	public Pair(){}

	public Pair(X a1, Y a2) {
//		System.out.println("X = " + a1 + " ; Y = " + a2);
		first = a1;
		second = a2;
	}

	public X getFirst() {
//		System.out.println("[PAIR] getFirst called in " + getClass().getName());
		return first;
	}

	public Y getSecond() {
//		System.out.println("[PAIR] getSecond called in " + getClass().getName());
		return second;
	}

	public void setFirst(X arg) {
		first = arg;
	}

	public void setSecond(Y arg) {
		second = arg;
	}
	
	
}
