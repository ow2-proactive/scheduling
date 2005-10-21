package trywithcatch;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class Block extends Anything {
    private Terminal start;
    private Terminal end;
    private List things;

    public Block(Terminal start, Terminal end, List things) {
        this.start = start;
        this.end = end;
        this.things = things;
    }

    public String toString() {
        return "{@" + start.getLeft() + "-" + end.getRight() + "}";
    }

    public boolean isEmpty() {
        Iterator iter = things.iterator();
        while (iter.hasNext()) {
            Anything a = (Anything) iter.next();
            if (!a.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    protected void prettyPrint(int indent) {
        start.prettyPrint(indent);
        Iterator iter = things.iterator();
        indent++;
        while (iter.hasNext()) {
            Anything a = (Anything) iter.next();
            a.prettyPrint(indent);
        }
        indent--;
        end.prettyPrint(indent);
    }

    public void work(Catcher c) throws IOException {
        Iterator iter = things.iterator();
        while (iter.hasNext()) {
            Anything a = (Anything) iter.next();
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
