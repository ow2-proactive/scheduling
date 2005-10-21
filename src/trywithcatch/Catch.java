package trywithcatch;

public class Catch extends Anything {
    private String className;
    private Block block;

    public Catch(String c, Block b) {
        className = c;
        block = b;
    }

    public String getClassName() {
        return className + ".class";
    }

    public Block getBlock() {
        return block;
    }
}
