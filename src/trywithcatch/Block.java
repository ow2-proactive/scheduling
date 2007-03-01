package trywithcatch;

import java.io.IOException;
import java.util.List;


public class Block extends Anything {
    private Terminal start;
    private Terminal end;
    private List<Anything> things;

    public Block(Terminal start, Terminal end, List<Anything> things) {
        this.start = start;
        this.end = end;
        this.things = things;
    }

    public String toString() {
        return "{@" + start.getLeft() + "-" + end.getRight() + "}";
    }

    public boolean isEmpty() {
        for (Anything a : things) {
        	if (!a.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    protected void prettyPrint(int indent) {
        start.prettyPrint(indent);
        indent++;
        for (Anything a : things) {
        	a.prettyPrint(indent);
        }
        indent--;
        end.prettyPrint(indent);
    }

    public void work(Catcher c) throws IOException {
    	for (Anything a : things) {
    		 a.work(c);
    	}
    }

    public Terminal getStart() {
        return start;
    }

    public Terminal getEnd() {
        return end;
    }
}
