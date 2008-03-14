package functionalTests.component.conform.components;

public class StreamImpl implements ItfWithStream {
    public void hello() {
        System.out.println("Hello from StreamTestClass");
    }
    
    public void hello(String name) {
        System.out.println("Hello " + name);
    }
}
