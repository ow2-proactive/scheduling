package nonregressiontest.component.collectiveitf.gathercast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;

import testsuite.test.Assertions;

public class GatherServer implements GatherDummyItf {

    /*
     * @see nonregressiontest.component.collectiveitf.gather.GatherDummyItf#foo(java.util.List)
     */
    public void foo(List<IntMutableWrapper> l) {
        
        // verify values are transmitted correctly
        Assertions.assertTrue(l.contains(new IntMutableWrapper(new Integer(Test.VALUE_1))));
        Assertions.assertTrue(l.contains(new IntMutableWrapper(new Integer(Test.VALUE_2))));
        
    }

    /*
     * @see nonregressiontest.component.collectiveitf.gather.GatherDummyItf#bar(java.util.List)
     */
    public List<B> bar(List<A> l) {
        
        List<B> result = new ArrayList<B>(l.size());
        for(int i=0; i<l.size(); i++) {
            result.add(i, new B(l.get(i).getValue()));
        }
        return result;
    }
    
    
    public List<B> timeout() {
    	List<B> l = new ArrayList<B>();
    	l.add(new B());
    	l.add(new B());
    	return l;
    }
    
    

}
