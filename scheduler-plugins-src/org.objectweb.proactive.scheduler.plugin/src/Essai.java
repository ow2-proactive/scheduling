import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public class Essai {

    /**
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        Display display = new Display();
        Shell parent = new Shell(display);

        //		FillLayout layout = new FillLayout(SWT.VERTICAL);
        //		layout.spacing = 5;
        //		layout.marginHeight = 10;
        //		layout.marginWidth = 10;
        //		parent.setLayout(layout);
        //		new PendingJobComposite(parent, "Pending");
        //		new RunningJobComposite(parent, "Running");
        //		new FinishedJobComposite(parent, "Finished");
        //		parent.setText("Menfou");
        FormLayout rl = new FormLayout();
        parent.setLayout(rl);
        Button bt1 = new Button(parent, SWT.PUSH);
        bt1.setText("salut");
        Button bt2 = new Button(parent, SWT.PUSH);
        bt2.setText("johannnnnnnnnnnn");

        FormData fd = new FormData();
        fd.left = new FormAttachment(0, 20);
        fd.right = new FormAttachment(100, -20);
        fd.top = new FormAttachment(0, 20);
        fd.right = new FormAttachment(bt2, 0, SWT.TOP);
        bt1.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0, 20);
        fd.right = new FormAttachment(100, -20);
        fd.bottom = new FormAttachment(100, -20);
        fd.top = new FormAttachment(1, 2, 0);
        bt2.setLayoutData(fd);

        parent.pack();
        parent.open();

        while (!parent.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
