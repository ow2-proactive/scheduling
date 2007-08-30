package javasci;


/**
 * SciString
 * @deprecated
 */
public class SciString {
    private SciStringMatrix sciMatrix;

    public SciString(String name) {
        sciMatrix = new SciStringMatrix(name, 1, 1);
    }

    public SciString(String name, String str) {
        sciMatrix = new SciStringMatrix(name, 1, 1, new String[] { str });
    }

    public SciString(String name, SciString obj) {
        sciMatrix = new SciStringMatrix(name, 1, 1,
                new String[] { obj.getData() });
    }

    /**
    * Job
    * @deprecated
    * @param job
    */
    public boolean Job(String job) {
        return Scilab.Exec(job);
    }

    /**
    * Send
    * @deprecated
    */
    public void Send() {
        Scilab.sendStringMatrix(sciMatrix);
    }

    /**
     * Get
     * @deprecated
     */
    public void Get() {
        Scilab.receiveStringMatrix(sciMatrix);
    }

    /**
     * getName
     * @deprecated
     */
    public String getName() {
        return sciMatrix.getName();
    }

    /**
    * getData
    * @deprecated
    */
    public String getData() {
        return sciMatrix.getData()[0];
    }

    /**
    * disp
    * @deprecated
    */
    public void disp() {
        System.out.println("String " + sciMatrix.getName() + "=");
        Scilab.Exec("disp(" + sciMatrix.getName() + ");");
    }
}
