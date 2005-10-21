package trywithcatch;

import java.io.IOException;


public abstract class Anything {
    public boolean isEmpty() {
        return false;
    }

    protected void prettyPrint(int indent) {
        while (indent != 0) {
            System.out.print("\t");
            indent--;
        }
    }

    public void prettyPrint() {
        prettyPrint(0);
    }

    public void work(Catcher c) throws IOException {
    }
}
