package trywithcatch;

import java.io.IOException;


public class Terminal extends Anything {
    private int left;
    private int right;
    private String str;
    private int column;

    public Terminal(int l, int r, int c, String s) {
        left = l;
        right = r;
        column = c;
        str = s;
    }

    public String toString() {
        return str + "@" + left + "";
    }

    protected void prettyPrint(int indent) {
        super.prettyPrint(indent);
        System.out.println(this);
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getColumn() {
        return column;
    }

    public void work(Catcher c) throws IOException {
        c.removeCallAtOffset(left - column);
    }
}
