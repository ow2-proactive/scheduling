package test.testStubInterface;

import org.objectweb.proactive.core.mop.*;

public class Test {

  public static void main(String[] args) {
    
      
      try {
	  
	  Int i1 = (Int) MOP.newInstance("test.testStubInterface.Int", "test.testStubInterface.IntImpl", new Object[0], "test.testStubInterface.ProxyOne", new Object[0]);
	  i1.foo (7);
	  i1.foo (13);

	  IntImpl i2 =  (IntImpl) MOP.newInstance("test.testStubInterface.IntImpl", new Object[0], "test.testStubInterface.ProxyOne", new Object[0]);
	  i2.foo (8);
	  i2.foo (14);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
